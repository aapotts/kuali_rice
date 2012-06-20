/**
 * Copyright 2005-2012 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kim.impl.role;

import static org.kuali.rice.core.api.criteria.PredicateFactory.equal;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.WebParam;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.kuali.rice.core.api.criteria.GenericQueryResults;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.delegation.DelegationType;
import org.kuali.rice.core.api.exception.RiceIllegalArgumentException;
import org.kuali.rice.core.api.exception.RiceIllegalStateException;
import org.kuali.rice.core.api.membership.MemberType;
import org.kuali.rice.core.api.mo.ModelObjectUtils;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.common.delegate.DelegateMember;
import org.kuali.rice.kim.api.common.delegate.DelegateType;
import org.kuali.rice.kim.api.identity.principal.Principal;
import org.kuali.rice.kim.api.role.DelegateMemberQueryResults;
import org.kuali.rice.kim.api.role.Role;
import org.kuali.rice.kim.api.role.RoleMember;
import org.kuali.rice.kim.api.role.RoleMemberQueryResults;
import org.kuali.rice.kim.api.role.RoleMembership;
import org.kuali.rice.kim.api.role.RoleMembershipQueryResults;
import org.kuali.rice.kim.api.role.RoleQueryResults;
import org.kuali.rice.kim.api.role.RoleResponsibility;
import org.kuali.rice.kim.api.role.RoleResponsibilityAction;
import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.api.type.KimType;
import org.kuali.rice.kim.framework.common.delegate.DelegationTypeService;
import org.kuali.rice.kim.framework.role.RoleTypeService;
import org.kuali.rice.kim.framework.services.KimFrameworkServiceLocator;
import org.kuali.rice.kim.framework.type.KimTypeService;
import org.kuali.rice.kim.impl.common.attribute.KimAttributeDataBo;
import org.kuali.rice.kim.impl.common.delegate.DelegateMemberAttributeDataBo;
import org.kuali.rice.kim.impl.common.delegate.DelegateMemberBo;
import org.kuali.rice.kim.impl.common.delegate.DelegateTypeBo;
import org.kuali.rice.kim.impl.services.KimImplServiceLocator;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class RoleServiceImpl extends RoleServiceBase implements RoleService {
    private static final Logger LOG = Logger.getLogger(RoleServiceImpl.class);

    private static final Map<String, RoleDaoAction> memberTypeToRoleDaoActionMap = populateMemberTypeToRoleDaoActionMap();

    private static Map<String, RoleDaoAction> populateMemberTypeToRoleDaoActionMap() {
        Map<String, RoleDaoAction> map = new HashMap<String, RoleDaoAction>();
        map.put(MemberType.GROUP.getCode(), RoleDaoAction.ROLE_GROUPS_FOR_GROUP_IDS_AND_ROLE_IDS);
        map.put(MemberType.PRINCIPAL.getCode(), RoleDaoAction.ROLE_PRINCIPALS_FOR_PRINCIPAL_ID_AND_ROLE_IDS);
        map.put(MemberType.ROLE.getCode(), RoleDaoAction.ROLE_MEMBERSHIPS_FOR_ROLE_IDS_AS_MEMBERS);
        return Collections.unmodifiableMap(map);
    }

    private RoleService proxiedRoleService;

    @Override
    public Role createRole(final Role role) throws RiceIllegalArgumentException, RiceIllegalStateException {
        incomingParamCheck(role, "role");

        if (StringUtils.isNotBlank(role.getId()) && getRole(role.getId()) != null) {
            throw new RiceIllegalStateException("the role to create already exists: " + role);
        }
        RoleBo bo = RoleBo.from(role);
        return RoleBo.to(getBusinessObjectService().save(bo));
    }

    @Override
    public Role updateRole(final Role role) throws RiceIllegalArgumentException, RiceIllegalStateException {
        incomingParamCheck(role, "role");

        RoleBoLite originalRole = getRoleBoLite(role.getId());
        if (StringUtils.isBlank(role.getId()) || originalRole == null) {
            throw new RiceIllegalStateException("the role does not exist: " + role);
        }

        RoleBo bo = RoleBo.from(role);

        RoleBo updatedRole = getBusinessObjectService().save(bo);
        if (originalRole.isActive()
                && !updatedRole.isActive()) {
            KimImplServiceLocator.getRoleInternalService().roleInactivated(updatedRole.getId());
        }
        return RoleBo.to(updatedRole);
    }

    /**
     * This method tests to see if assigning a roleBo to another roleBo will create a circular reference.
     * The Role is checked to see if it is a member (direct or nested) of the roleBo to be assigned as a member.
     *
     * @param newMemberId
     * @param roleBo
     * @return true  - assignment is allowed, no circular reference will be created.
     *         false - illegal assignment, it will create a circular membership
     */
    protected boolean checkForCircularRoleMembership(String newMemberId, RoleBo roleBo) {
        // get all nested roleBo members that are of type roleBo
        Set<String> newRoleMemberIds = getRoleTypeRoleMemberIds(newMemberId);
        return !newRoleMemberIds.contains(roleBo.getId());
    }

    protected RoleMember findRoleMember(String roleMemberId) {
        final List<RoleMember> roleMembers = findRoleMembers(QueryByCriteria.Builder.fromPredicates(equal(KimConstants.PrimaryKeyConstants.ID, roleMemberId))).getResults();
        if (roleMembers != null && !roleMembers.isEmpty()) {
            return roleMembers.get(0);
        }
        return null;
    }

    @Override
    public RoleMemberQueryResults findRoleMembers(QueryByCriteria queryByCriteria) throws RiceIllegalStateException {
        incomingParamCheck(queryByCriteria, "queryByCriteria");

        GenericQueryResults<RoleMemberBo> results = getCriteriaLookupService().lookup(RoleMemberBo.class, queryByCriteria);

        RoleMemberQueryResults.Builder builder = RoleMemberQueryResults.Builder.create();
        builder.setMoreResultsAvailable(results.isMoreResultsAvailable());
        builder.setTotalRowCount(results.getTotalRowCount());

        final List<RoleMember.Builder> ims = new ArrayList<RoleMember.Builder>();
        for (RoleMemberBo bo : results.getResults()) {
            ims.add(RoleMember.Builder.create(bo));
        }

        builder.setResults(ims);
        return builder.build();
    }

    @Override
    public Set<String> getRoleTypeRoleMemberIds(String roleId) throws RiceIllegalArgumentException  {
        incomingParamCheck(roleId, "roleId");

        Set<String> results = new HashSet<String>();
        getNestedRoleTypeMemberIds(roleId, results);
        return Collections.unmodifiableSet(results);
    }

    @Override
    public List<String> getMemberParentRoleIds(String memberType, String memberId) throws RiceIllegalStateException  {
        incomingParamCheck(memberType, "memberType");
        incomingParamCheck(memberId, "memberId");

        List<RoleMemberBo> parentRoleMembers = getRoleDao().getRoleMembershipsForMemberId(memberType, memberId,
                Collections.<String, String>emptyMap());

        List<String> parentRoleIds = new ArrayList<String>(parentRoleMembers.size());
        for (RoleMemberBo parentRoleMember : parentRoleMembers) {
            parentRoleIds.add(parentRoleMember.getRoleId());
        }

        return parentRoleIds;
    }

    @Override
    public List<RoleResponsibilityAction> getRoleMemberResponsibilityActions(String roleMemberId) throws RiceIllegalStateException  {
        incomingParamCheck(roleMemberId, "roleMemberId");

        Map<String, String> criteria = new HashMap<String, String>(1);
        criteria.put(KimConstants.PrimaryKeyConstants.ROLE_MEMBER_ID, roleMemberId);

        List<RoleResponsibilityActionBo> responsibilityActionBoList = (List<RoleResponsibilityActionBo>)
                getBusinessObjectService().findMatching(RoleResponsibilityActionBo.class, criteria);

        List<RoleResponsibilityAction> roleResponsibilityActionsList = new ArrayList<RoleResponsibilityAction>();
        for (RoleResponsibilityActionBo roleResponsibilityActionBo : responsibilityActionBoList) {
            RoleResponsibilityAction roleResponsibility = RoleResponsibilityActionBo.to(roleResponsibilityActionBo);
            roleResponsibilityActionsList.add(roleResponsibility);
        }
        return roleResponsibilityActionsList;
    }

    @Override
    public DelegateMemberQueryResults findDelegateMembers(QueryByCriteria queryByCriteria) throws RiceIllegalStateException  {
        incomingParamCheck(queryByCriteria, "queryByCriteria");

        GenericQueryResults<DelegateMemberBo> results = getCriteriaLookupService().lookup(DelegateMemberBo.class, queryByCriteria);

        DelegateMemberQueryResults.Builder builder = DelegateMemberQueryResults.Builder.create();
        builder.setMoreResultsAvailable(results.isMoreResultsAvailable());
        builder.setTotalRowCount(results.getTotalRowCount());

        final List<DelegateMember.Builder> ims = new ArrayList<DelegateMember.Builder>();
        for (DelegateMemberBo bo : results.getResults()) {
            ims.add(DelegateMember.Builder.create(bo));
        }

        builder.setResults(ims);
        return builder.build();
    }

    @Override
    public Role getRole(String roleId) throws RiceIllegalStateException  {
        incomingParamCheck(roleId, "roleId");

        RoleBoLite roleBo = getRoleBoLite(roleId);
        if (roleBo == null) {
            return null;
        }
        return RoleBoLite.to(roleBo);
    }

    protected Map<String, RoleBo> getRoleBoMap(Collection<String> roleIds) {
        Map<String, RoleBo> result;
        // check for a non-null result in the cache, return it if found
        if (roleIds.size() == 1) {
            String roleId = roleIds.iterator().next();
            RoleBo bo = getRoleBo(roleId);
            result = bo.isActive() ? Collections.singletonMap(roleId, bo) :  Collections.<String, RoleBo>emptyMap();
        } else {
            result = new HashMap<String, RoleBo>(roleIds.size());
            for (String roleId : roleIds) {
                RoleBo bo = getRoleBo(roleId);
                if (bo.isActive()) {
                    result.put(roleId, bo);
                }
            }
        }
        return result;
    }
    
    protected Map<String, RoleBoLite> getRoleBoLiteMap(Collection<String> roleIds) {
        Map<String, RoleBoLite> result;
        // check for a non-null result in the cache, return it if found
        if (roleIds.size() == 1) {
            String roleId = roleIds.iterator().next();
            RoleBoLite bo = getRoleBoLite(roleId);
            result = bo.isActive() ? Collections.singletonMap(roleId, bo) :  Collections.<String, RoleBoLite>emptyMap();
        } else {
            result = new HashMap<String, RoleBoLite>(roleIds.size());
            for (String roleId : roleIds) {
                RoleBoLite bo = getRoleBoLite(roleId);
                if (bo.isActive()) {
                    result.put(roleId, bo);
                }
            }
        }
        return result;
    }

    @Override
    public List<Role> getRoles(List<String> roleIds) throws RiceIllegalStateException  {
        if (CollectionUtils.isEmpty(roleIds)) {
            throw new RiceIllegalArgumentException("roleIds is null or empty");
        }

        Collection<RoleBoLite> roleBos = getRoleBoLiteMap(roleIds).values();
        List<Role> roles = new ArrayList<Role>(roleBos.size());
        for (RoleBoLite bo : roleBos) {
            roles.add(RoleBoLite.to(bo));
        }
        return Collections.unmodifiableList(roles);
    }

    @Override
    public Role getRoleByNamespaceCodeAndName(String namespaceCode, String roleName) throws RiceIllegalStateException  {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");

        RoleBo roleBo = getRoleBoByName(namespaceCode, roleName);
        if (roleBo != null) {
            return RoleBo.to(roleBo);
        }
        return null;
    }

    @Override
    public String getRoleIdByNamespaceCodeAndName(String namespaceCode, String roleName) throws RiceIllegalStateException  {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");

        Role role = getRoleByNamespaceCodeAndName(namespaceCode, roleName);
        if (role != null) {
            return role.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean isRoleActive(String roleId) throws RiceIllegalStateException  {
        incomingParamCheck(roleId, "roleId");

        RoleBoLite roleBo = getRoleBoLite(roleId);
        return roleBo != null && roleBo.isActive();
    }

    @Override
    public List<Map<String, String>> getRoleQualifersForPrincipalByRoleIds(String principalId, List<String> roleIds,
            Map<String, String> qualification) throws RiceIllegalStateException  {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(roleIds, "roleIds");

        List<Map<String, String>> results = new ArrayList<Map<String, String>>();

        List<RoleMemberBo> roleMemberBoList = getStoredRoleMembersUsingExactMatchOnQualification(principalId, null,
                roleIds, qualification);

        Map<String, List<RoleMembership>> roleIdToMembershipMap = new HashMap<String, List<RoleMembership>>();
        for (RoleMemberBo roleMemberBo : roleMemberBoList) {
            // gather up the qualifier sets and the service they go with
            if (MemberType.PRINCIPAL.equals(roleMemberBo.getType())) {
                RoleTypeService roleTypeService = getRoleTypeService(roleMemberBo.getRoleId());
                if (roleTypeService != null) {
                    List<RoleMembership> las = roleIdToMembershipMap.get(roleMemberBo.getRoleId());
                    if (las == null) {
                        las = new ArrayList<RoleMembership>();
                        roleIdToMembershipMap.put(roleMemberBo.getRoleId(), las);
                    }
                    RoleMembership mi = RoleMembership.Builder.create(
                            roleMemberBo.getRoleId(),
                            roleMemberBo.getId(),
                            roleMemberBo.getMemberId(),
                            roleMemberBo.getType(),
                            roleMemberBo.getAttributes()).build();

                    las.add(mi);
                } else {
                    results.add(roleMemberBo.getAttributes());
                }
            }
        }
        for (Map.Entry<String, List<RoleMembership>> entry : roleIdToMembershipMap.entrySet()) {
            RoleTypeService roleTypeService = getRoleTypeService(entry.getKey());
            //it is possible that the the roleTypeService is coming from a remote application
            // and therefore it can't be guaranteed that it is up and working, so using a try/catch to catch this possibility.
            try {
                List<RoleMembership> matchingMembers = roleTypeService.getMatchingRoleMemberships(qualification, entry.getValue());
                for (RoleMembership rmi : matchingMembers) {
                    results.add(rmi.getQualifier());
                }
            } catch (Exception ex) {
                LOG.warn("Not able to retrieve RoleTypeService from remote system for role Id: " + entry.getKey(), ex);
            }
        }
        return Collections.unmodifiableList(results);
    }

    @Override
    public List<Map<String, String>> getRoleQualifersForPrincipalByNamespaceAndRolename(String principalId,
            String namespaceCode, String roleName, Map<String, String> qualification)
            throws RiceIllegalStateException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");

        String roleId = getRoleIdByNamespaceCodeAndName(namespaceCode, roleName);
        if (roleId == null) {
            return Collections.emptyList();
        }
        return getNestedRoleQualifiersForPrincipalByRoleIds(principalId, Collections.singletonList(roleId),
                qualification);
    }

    @Override
    public List<Map<String, String>> getNestedRoleQualifersForPrincipalByNamespaceAndRolename(String principalId,
            String namespaceCode, String roleName, Map<String, String> qualification) throws RiceIllegalStateException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");

        String roleId = getRoleIdByNamespaceCodeAndName(namespaceCode, roleName);
        if (roleId == null) {
            return new ArrayList<Map<String, String>>(0);
        }
        return getNestedRoleQualifiersForPrincipalByRoleIds(principalId, Collections.singletonList(roleId),
                qualification);
    }

    @Override
    public List<Map<String, String>> getNestedRoleQualifiersForPrincipalByRoleIds(String principalId,
            List<String> roleIds, Map<String, String> qualification) throws RiceIllegalStateException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(roleIds, "roleIds");


        List<Map<String, String>> results = new ArrayList<Map<String, String>>();

        Map<String, RoleBoLite> roleBosById = getRoleBoLiteMap(roleIds);

        // get the person's groups
        List<String> groupIds = getGroupService().getGroupIdsByPrincipalId(principalId);
        List<RoleMemberBo> roleMemberBos = getStoredRoleMembersUsingExactMatchOnQualification(principalId, groupIds, roleIds, qualification);

        Map<String, List<RoleMembership>> roleIdToMembershipMap = new HashMap<String, List<RoleMembership>>();
        for (RoleMemberBo roleMemberBo : roleMemberBos) {
            RoleTypeService roleTypeService = getRoleTypeService(roleMemberBo.getRoleId());
            // gather up the qualifier sets and the service they go with
            if (MemberType.PRINCIPAL.equals(roleMemberBo.getType())
                    || MemberType.GROUP.equals(roleMemberBo.getType())) {
                if (roleTypeService != null) {
                    List<RoleMembership> las = roleIdToMembershipMap.get(roleMemberBo.getRoleId());
                    if (las == null) {
                        las = new ArrayList<RoleMembership>();
                        roleIdToMembershipMap.put(roleMemberBo.getRoleId(), las);
                    }
                    RoleMembership mi = RoleMembership.Builder.create(
                            roleMemberBo.getRoleId(),
                            roleMemberBo.getId(),
                            roleMemberBo.getMemberId(),
                            roleMemberBo.getType(),
                            roleMemberBo.getAttributes()).build();

                    las.add(mi);
                } else {
                    results.add(roleMemberBo.getAttributes());
                }
            } else if (MemberType.ROLE.equals(roleMemberBo.getType())) {
                // find out if the user has the role
                // need to convert qualification using this role's service
                Map<String, String> nestedQualification = qualification;
                if (roleTypeService != null) {
                    RoleBoLite roleBo = roleBosById.get(roleMemberBo.getRoleId());
                    // pulling from here as the nested roleBo is not necessarily (and likely is not)
                    // in the roleBosById Map created earlier
                    RoleBoLite nestedRole = getRoleBoLite(roleMemberBo.getMemberId());
                    //it is possible that the the roleTypeService is coming from a remote application
                    // and therefore it can't be guaranteed that it is up and working, so using a try/catch to catch this possibility.
                    try {
                        nestedQualification = roleTypeService.convertQualificationForMemberRoles(roleBo.getNamespaceCode(), roleBo.getName(), nestedRole.getNamespaceCode(), nestedRole.getName(), qualification);
                    } catch (Exception ex) {
                        LOG.warn("Not able to retrieve RoleTypeService from remote system for roleBo Id: " + roleBo.getId(), ex);
                    }
                }
                List<String> nestedRoleId = new ArrayList<String>(1);
                nestedRoleId.add(roleMemberBo.getMemberId());
                // if the user has the given role, add the qualifier the *nested role* has with the
                // originally queries role
                if (this.getProxiedRoleService().principalHasRole(principalId, nestedRoleId, nestedQualification, false)) {
                    results.add(roleMemberBo.getAttributes());
                }
            }
        }
        for (Map.Entry<String, List<RoleMembership>> entry : roleIdToMembershipMap.entrySet()) {
            RoleTypeService roleTypeService = getRoleTypeService(entry.getKey());
            //it is possible that the the roleTypeService is coming from a remote   
            // and therefore it can't be guaranteed that it is up and working, so using a try/catch to catch this possibility.
            try {
                List<RoleMembership> matchingMembers = roleTypeService.getMatchingRoleMemberships(qualification,
                        entry.getValue());
                for (RoleMembership roleMembership : matchingMembers) {
                    results.add(roleMembership.getQualifier());
                }
            } catch (Exception ex) {
                LOG.warn("Not able to retrieve RoleTypeService from remote system for role Id: " + entry.getKey(), ex);
            }
        }
        return Collections.unmodifiableList(results);
    }

    @Override
    public List<RoleMembership> getRoleMembers(List<String> roleIds, Map<String, String> qualification) throws RiceIllegalStateException {
        incomingParamCheck(roleIds, "roleIds");

        Set<String> foundRoleTypeMembers = new HashSet<String>();
        List<RoleMembership> roleMembers = getRoleMembers(roleIds, qualification, true, foundRoleTypeMembers);

        return Collections.unmodifiableList(roleMembers);
    }

    @Override
    public Collection<String> getRoleMemberPrincipalIds(String namespaceCode, String roleName, Map<String, String> qualification) throws RiceIllegalStateException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");

        Set<String> principalIds = new HashSet<String>();
        Set<String> foundRoleTypeMembers = new HashSet<String>();
        List<String> roleIds = Collections.singletonList(getRoleIdByNamespaceCodeAndName(namespaceCode, roleName));
        for (RoleMembership roleMembership : getRoleMembers(roleIds, qualification, false, foundRoleTypeMembers)) {
            if (MemberType.GROUP.equals(roleMembership.getType())) {
                principalIds.addAll(getGroupService().getMemberPrincipalIds(roleMembership.getMemberId()));
            } else {
                principalIds.add(roleMembership.getMemberId());
            }
        }

        return Collections.unmodifiableSet(principalIds);
    }

    @Override
    public boolean principalHasRole(String principalId, List<String> roleIds, Map<String, String> qualification) throws RiceIllegalStateException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(roleIds, "roleIds");

        if ( LOG.isDebugEnabled() ) {
            logPrincipalHasRoleCheck(principalId, roleIds, qualification);
        }

        Boolean hasRole = this.getProxiedRoleService().principalHasRole(principalId, roleIds, qualification, true);
        
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Result: " + hasRole );
        }

        return hasRole;
    }

    @Override
    public List<String> getPrincipalIdSubListWithRole(List<String> principalIds,
            String roleNamespaceCode, String roleName, Map<String, String> qualification) throws RiceIllegalStateException {
        incomingParamCheck(principalIds, "principalIds");
        incomingParamCheck(roleNamespaceCode, "roleNamespaceCode");
        incomingParamCheck(roleName, "roleName");

        List<String> subList = new ArrayList<String>();
        RoleBo role = getRoleBoByName(roleNamespaceCode, roleName);
        for (String principalId : principalIds) {
            if (this.getProxiedRoleService().principalHasRole(principalId, Collections.singletonList(role.getId()), qualification)) {
                subList.add(principalId);
            }
        }
        return Collections.unmodifiableList(subList);
    }

    @Override
    public RoleQueryResults findRoles(QueryByCriteria queryByCriteria) throws RiceIllegalStateException {
        incomingParamCheck(queryByCriteria, "queryByCriteria");

        GenericQueryResults<RoleBo> results = getCriteriaLookupService().lookup(RoleBo.class, queryByCriteria);

        RoleQueryResults.Builder builder = RoleQueryResults.Builder.create();
        builder.setMoreResultsAvailable(results.isMoreResultsAvailable());
        builder.setTotalRowCount(results.getTotalRowCount());

        final List<Role.Builder> ims = new ArrayList<Role.Builder>();
        for (RoleBo bo : results.getResults()) {
            ims.add(Role.Builder.create(bo));
        }

        builder.setResults(ims);
        return builder.build();
    }

    @Override
    public List<RoleMembership> getFirstLevelRoleMembers(List<String> roleIds) throws RiceIllegalStateException {
        incomingParamCheck(roleIds, "roleIds");
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<RoleMemberBo> roleMemberBoList = getStoredRoleMembersForRoleIds(roleIds, null, null);
        List<RoleMembership> roleMemberships = new ArrayList<RoleMembership>();
        for (RoleMemberBo roleMemberBo : roleMemberBoList) {
            RoleMembership roleMembeship = RoleMembership.Builder.create(
                    roleMemberBo.getRoleId(),
                    roleMemberBo.getId(),
                    roleMemberBo.getMemberId(),
                    roleMemberBo.getType(),
                    roleMemberBo.getAttributes()).build();
            roleMemberships.add(roleMembeship);
        }
        return Collections.unmodifiableList(roleMemberships);
    }

    @Override
    public RoleMembershipQueryResults findRoleMemberships( QueryByCriteria queryByCriteria) throws RiceIllegalStateException {
        incomingParamCheck(queryByCriteria, "queryByCriteria");

        GenericQueryResults<RoleMemberBo> results = getCriteriaLookupService().lookup(RoleMemberBo.class, queryByCriteria);

        RoleMembershipQueryResults.Builder builder = RoleMembershipQueryResults.Builder.create();
        builder.setMoreResultsAvailable(results.isMoreResultsAvailable());
        builder.setTotalRowCount(results.getTotalRowCount());

        final List<RoleMembership.Builder> ims = new ArrayList<RoleMembership.Builder>();
        for (RoleMemberBo bo : results.getResults()) {
            RoleMembership.Builder roleMembership = RoleMembership.Builder.create(
                    bo.getRoleId(),
                    bo.getId(),
                    bo.getMemberId(),
                    bo.getType(),
                    bo.getAttributes());
            ims.add(roleMembership);
        }

        builder.setResults(ims);
        return builder.build();
    }

    @Override
    public List<DelegateMember> getDelegationMembersByDelegationId(String delegationId) throws RiceIllegalStateException {
        incomingParamCheck(delegationId, "delegationId");

        DelegateTypeBo delegateBo = getKimDelegationImpl(delegationId);
        if (delegateBo == null) {return Collections.emptyList();}

        return getDelegateMembersForDelegation(delegateBo);
    }

    @Override
    public DelegateMember getDelegationMemberByDelegationAndMemberId(String delegationId, String memberId) throws RiceIllegalStateException {
        incomingParamCheck(delegationId, "delegationId");
        incomingParamCheck(memberId, "memberId");

        DelegateTypeBo delegateBo = getKimDelegationImpl(delegationId);
        DelegateMemberBo delegationMember = getKimDelegationMemberImplByDelegationAndId(delegationId, memberId);

        return getDelegateCompleteInfo(delegateBo, delegationMember);
    }

    @Override
    public DelegateMember getDelegationMemberById(String delegationMemberId) throws RiceIllegalStateException {
        incomingParamCheck(delegationMemberId, "delegationMemberId");

        DelegateMemberBo delegateMemberBo = getDelegateMemberBo(delegationMemberId);
        if (delegateMemberBo == null) {
            return null;
        }

        DelegateTypeBo delegateBo = getKimDelegationImpl(delegateMemberBo.getDelegationId());

        return getDelegateCompleteInfo(delegateBo, delegateMemberBo);
    }

    @Override
    public List<RoleResponsibility> getRoleResponsibilities(String roleId) throws RiceIllegalStateException {
        incomingParamCheck(roleId, "roleId");

        Map<String, String> criteria = new HashMap<String, String>(1);
        criteria.put(KimConstants.PrimaryKeyConstants.SUB_ROLE_ID, roleId);
        List<RoleResponsibilityBo> roleResponsibilityBos = (List<RoleResponsibilityBo>)
                getBusinessObjectService().findMatching(RoleResponsibilityBo.class, criteria);
        List<RoleResponsibility> roleResponsibilities = new ArrayList<RoleResponsibility>();

        for (RoleResponsibilityBo roleResponsibilityImpl : roleResponsibilityBos) {
            roleResponsibilities.add(RoleResponsibilityBo.to(roleResponsibilityImpl));
        }
        return Collections.unmodifiableList(roleResponsibilities);
    }

    @Override
    public DelegateType getDelegateTypeByRoleIdAndDelegateTypeCode(String roleId, DelegationType delegationType) throws RiceIllegalStateException {
        incomingParamCheck(roleId, "roleId");
        incomingParamCheck(delegationType, "delegationType");

        DelegateTypeBo delegateBo = getDelegationOfType(roleId, delegationType);
        return DelegateTypeBo.to(delegateBo);
    }

    @Override
    public DelegateType getDelegateTypeByDelegationId(String delegationId) throws RiceIllegalStateException {
        incomingParamCheck(delegationId, "delegationId");

        DelegateTypeBo delegateBo = getKimDelegationImpl(delegationId);
        return DelegateTypeBo.to(delegateBo);
    }

    protected List<RoleMembership> getRoleMembers(List<String> roleIds, Map<String, String> qualification, boolean followDelegations, Set<String> foundRoleTypeMembers) {
        List<RoleMembership> results = new ArrayList<RoleMembership>();
        Set<String> allRoleIds = new HashSet<String>();
        for (String roleId : roleIds) {
            if (this.getProxiedRoleService().isRoleActive(roleId)) {
                allRoleIds.add(roleId);
            }
        }
        // short-circuit if no roles match
        if (allRoleIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> matchingRoleIds = new HashSet<String>(allRoleIds.size());
        // for efficiency, retrieve all roles and store in a map
        Map<String, RoleBoLite> roles = getRoleBoLiteMap(allRoleIds);

        List<String> copyRoleIds = new ArrayList<String>(allRoleIds);
        List<RoleMemberBo> rms = new ArrayList<RoleMemberBo>();

        for (String roleId : allRoleIds) {
            RoleTypeService roleTypeService = getRoleTypeService(roleId);
            if (roleTypeService != null) {
                List<String> attributesForExactMatch = roleTypeService.getQualifiersForExactMatch();
                if (CollectionUtils.isNotEmpty(attributesForExactMatch)) {
                    copyRoleIds.remove(roleId);
                    rms.addAll(getStoredRoleMembersForRoleIds(Collections.singletonList(roleId), null, populateQualifiersForExactMatch(qualification, attributesForExactMatch)));
                }
            }
        }
        if (CollectionUtils.isNotEmpty(copyRoleIds)) {
            rms.addAll(getStoredRoleMembersForRoleIds(copyRoleIds, null, null));
        }

        // build a map of role ID to membership information
        // this will be used for later qualification checks
        Map<String, List<RoleMembership>> roleIdToMembershipMap = new HashMap<String, List<RoleMembership>>();
        for (RoleMemberBo roleMemberBo : rms) {
            RoleMembership mi = RoleMembership.Builder.create(
                    roleMemberBo.getRoleId(),
                    roleMemberBo.getId(),
                    roleMemberBo.getMemberId(),
                    roleMemberBo.getType(),
                    roleMemberBo.getAttributes()).build();

            // if the qualification check does not need to be made, just add the result
            if ((qualification == null || qualification.isEmpty()) || getRoleTypeService(roleMemberBo.getRoleId()) == null) {
                if (MemberType.ROLE.equals(roleMemberBo.getType())) {
                    // if a role member type, do a non-recursive role member check
                    // to obtain the group and principal members of that role
                    // given the qualification
                    Map<String, String> nestedRoleQualification = qualification;
                    if (getRoleTypeService(roleMemberBo.getRoleId()) != null) {
                        // get the member role object
                        RoleBoLite memberRole = getRoleBoLite(mi.getMemberId());
                        nestedRoleQualification = getRoleTypeService(roleMemberBo.getRoleId())
                                .convertQualificationForMemberRoles(
                                        roles.get(roleMemberBo.getRoleId()).getNamespaceCode(),
                                        roles.get(roleMemberBo.getRoleId()).getName(),
                                        memberRole.getNamespaceCode(),
                                        memberRole.getName(),
                                        qualification);
                    }
                    if (this.getProxiedRoleService().isRoleActive(roleMemberBo.getRoleId())) {
                        Collection<RoleMembership> nestedRoleMembers = getNestedRoleMembers(nestedRoleQualification, mi, foundRoleTypeMembers);
                        if (!nestedRoleMembers.isEmpty()) {
                            results.addAll(nestedRoleMembers);
                            matchingRoleIds.add(roleMemberBo.getRoleId());
                        }
                    }
                } else { // not a role member type
                    results.add(mi);
                    matchingRoleIds.add(roleMemberBo.getRoleId());
                }
                matchingRoleIds.add(roleMemberBo.getRoleId());
            } else {
                List<RoleMembership> lrmi = roleIdToMembershipMap.get(mi.getRoleId());
                if (lrmi == null) {
                    lrmi = new ArrayList<RoleMembership>();
                    roleIdToMembershipMap.put(mi.getRoleId(), lrmi);
                }
                lrmi.add(mi);
            }
        }
        // if there is anything in the role to membership map, we need to check the role type services
        // for those entries
        if (!roleIdToMembershipMap.isEmpty()) {
            // for each role, send in all the qualifiers for that role to the type service
            // for evaluation, the service will return those which match
            for (Map.Entry<String, List<RoleMembership>> entry : roleIdToMembershipMap.entrySet()) {
                //it is possible that the the roleTypeService is coming from a remote application
                // and therefore it can't be guaranteed that it is up and working, so using a try/catch to catch this possibility.
                try {
                    RoleTypeService roleTypeService = getRoleTypeService(entry.getKey());
                    List<RoleMembership> matchingMembers = roleTypeService.getMatchingRoleMemberships(qualification,
                            entry.getValue());
                    // loop over the matching entries, adding them to the results
                    for (RoleMembership roleMemberships : matchingMembers) {
                        if (MemberType.ROLE.equals(roleMemberships.getType())) {
                            // if a role member type, do a non-recursive role member check
                            // to obtain the group and principal members of that role
                            // given the qualification
                            // get the member role object
                            RoleBoLite memberRole = getRoleBoLite(roleMemberships.getMemberId());
                            if (memberRole.isActive()) {
                                Map<String, String> nestedRoleQualification = roleTypeService.convertQualificationForMemberRoles(
                                        roles.get(roleMemberships.getRoleId()).getNamespaceCode(),
                                        roles.get(roleMemberships.getRoleId()).getName(),
                                        memberRole.getNamespaceCode(),
                                        memberRole.getName(),
                                        qualification);
                                Collection<RoleMembership> nestedRoleMembers = getNestedRoleMembers(nestedRoleQualification, roleMemberships, foundRoleTypeMembers);
                                if (!nestedRoleMembers.isEmpty()) {
                                    results.addAll(nestedRoleMembers);
                                    matchingRoleIds.add(roleMemberships.getRoleId());
                                }
                            }
                        } else { // not a role member
                            results.add(roleMemberships);
                            matchingRoleIds.add(roleMemberships.getRoleId());
                        }
                    }
                } catch (Exception ex) {
                    LOG.warn("Not able to retrieve RoleTypeService from remote system for role Id: " + entry.getKey(), ex);
                }
            }
        }

        // handle derived roles
    	for ( String roleId : allRoleIds ) {
    		RoleTypeService roleTypeService = getRoleTypeService( roleId );
			RoleBoLite role = roles.get( roleId );
    		// check if a derived role
            try {
        		if ( isDerivedRoleType(role.getKimTypeId(), roleTypeService) ) {
                    // for each derived role, get the list of principals and groups which are in that role given the qualification (per the role type service)
        			List<RoleMembership> roleMembers = roleTypeService.getRoleMembersFromDerivedRole(role.getNamespaceCode(), role.getName(), qualification);
        			if ( !roleMembers.isEmpty()  ) {
        				matchingRoleIds.add( roleId );
        			}
        			for ( RoleMembership rm : roleMembers ) {
                        RoleMembership.Builder builder = RoleMembership.Builder.create(rm);
                        builder.setRoleId(roleId);
                        builder.setId("*");
                        results.add(builder.build());
        			}
        		}
            } catch (Exception ex) {
                LOG.warn("Not able to retrieve RoleTypeService from remote system for role Id: " + roleId, ex);
            }
    	}

    	if ( followDelegations && !matchingRoleIds.isEmpty() ) {
	    	// we have a list of RoleMembershipInfo objects
	    	// need to get delegations for distinct list of roles in that list
	    	Map<String, DelegateTypeBo> delegationIdToDelegationMap = getStoredDelegationImplMapFromRoleIds(matchingRoleIds);
            if (!delegationIdToDelegationMap.isEmpty()) {
                List<RoleMembership.Builder> membershipsWithDelegations =
                        applyDelegationsToRoleMembers(results, delegationIdToDelegationMap.values(), qualification);
                resolveDelegationMemberRoles(membershipsWithDelegations, qualification, foundRoleTypeMembers);
                results = ModelObjectUtils.buildImmutableCopy(membershipsWithDelegations);
            }
    	}
    	
    	// sort the results if a single role type service can be identified for
        // all the matching role members
        if ( results.size() > 1 ) {
            // if a single role: easy case
            if ( matchingRoleIds.size() == 1 ) {
                String roleId = matchingRoleIds.iterator().next();
                RoleTypeService roleTypeService = getRoleTypeService( roleId );
                //it is possible that the the roleTypeService is coming from a remote application 
                // and therefore it can't be guaranteed that it is up and working, so using a try/catch to catch this possibility.
                try {
                    if ( roleTypeService != null ) {
                        results = roleTypeService.sortRoleMembers( results );
                    }
                } catch (Exception ex) {
                    LOG.warn("Not able to retrieve RoleTypeService from remote system for role Id: " + roleId, ex);
                }
            } else if ( matchingRoleIds.size() > 1 ) {
                // if more than one, check if there is only a single role type service
                String prevServiceName = null;
                boolean multipleServices = false;
                for ( String roleId : matchingRoleIds ) {
                    String serviceName = KimApiServiceLocator.getKimTypeInfoService().getKimType(getRole(roleId).getKimTypeId()).getServiceName();
                    if ( prevServiceName != null && !StringUtils.equals( prevServiceName, serviceName ) ) {
                        multipleServices = true;
                        break;
                    }
                    prevServiceName = serviceName;
                }
                if ( !multipleServices ) {
                    String roleId = matchingRoleIds.iterator().next();
                    //it is possible that the the roleTypeService is coming from a remote application 
                    // and therefore it can't be guaranteed that it is up and working, so using a try/catch to catch this possibility.
                    try {                       
                        RoleTypeService kimRoleTypeService = getRoleTypeService( roleId );
                        if ( kimRoleTypeService != null ) {
                            results = kimRoleTypeService.sortRoleMembers( results );
                        }
                    } catch (Exception ex) {
                        LOG.warn("Not able to retrieve RoleTypeService from remote system for role Id: " + roleId, ex);
                    }
                } else {
                    LOG.warn( "Did not sort role members - multiple role type services found.  Role Ids: " + matchingRoleIds );
                }
            }
        }
        return Collections.unmodifiableList(results);
    }

    /**
     * Checks each of the result records to determine if there are potentially applicable delegation members for that
     * role membership.  If there are, applicable delegations and members will be linked to the RoleMemberships in the
     * given list.  An updated list will be returned from this method which includes the appropriate linked delegations.
     */
    protected List<RoleMembership.Builder> applyDelegationsToRoleMembers(List<RoleMembership> roleMemberships,
            Collection<DelegateTypeBo> delegations, Map<String, String> qualification) {
        MultiValueMap<String, String> roleIdToRoleMembershipIds = new LinkedMultiValueMap<String, String>();
        Map<String, RoleMembership.Builder> roleMembershipIdToBuilder = new HashMap<String, RoleMembership.Builder>();
        List<RoleMembership.Builder> roleMembershipBuilders = new ArrayList<RoleMembership.Builder>();
        // to make our algorithm less painful, let's do some indexing and load the given list of RoleMemberships into
        // builders
        for (RoleMembership roleMembership : roleMemberships) {
            roleIdToRoleMembershipIds.add(roleMembership.getRoleId(), roleMembership.getId());
            RoleMembership.Builder builder = RoleMembership.Builder.create(roleMembership);
            roleMembershipBuilders.add(builder);
            roleMembershipIdToBuilder.put(roleMembership.getId(), builder);
        }
        for (DelegateTypeBo delegation : delegations) {
            // determine the candidate role memberships where this delegation can be mapped
            List<String> candidateRoleMembershipIds = roleIdToRoleMembershipIds.get(delegation.getRoleId());
            if (CollectionUtils.isNotEmpty(candidateRoleMembershipIds)) {
                DelegationTypeService delegationTypeService = getDelegationTypeService(delegation.getDelegationId());
                for (DelegateMemberBo delegationMember : delegation.getMembers()) {
                    // Make sure that the delegation member is active
    	    		if (delegationMember.isActive(DateTime.now()) && (delegationTypeService == null ||
                            delegationTypeService.doesDelegationQualifierMatchQualification(qualification, delegationMember.getQualifier()))) {
                        DelegateMember.Builder delegateMemberBuilder = DelegateMember.Builder.create(delegationMember);
                        // if the member has no role member id, check qualifications and apply to all matching role memberships on the role
	    	    	    if (StringUtils.isBlank(delegationMember.getRoleMemberId())) {
                            RoleTypeService roleTypeService = getRoleTypeService(delegation.getRoleId());
                            for (String roleMembershipId : candidateRoleMembershipIds) {
                                RoleMembership.Builder roleMembershipBuilder = roleMembershipIdToBuilder.get(roleMembershipId);
                                if (roleTypeService == null || roleTypeService.doesRoleQualifierMatchQualification(roleMembershipBuilder.getQualifier(), delegationMember.getQualifier())) {
                                    linkDelegateToRoleMembership(delegation, delegateMemberBuilder, roleMembershipBuilder);
                                }
                            }
                        } else if (candidateRoleMembershipIds.contains(delegationMember.getRoleMemberId())) {
                            RoleMembership.Builder roleMembershipBuilder = roleMembershipIdToBuilder.get(delegationMember.getRoleMemberId());
                            linkDelegateToRoleMembership(delegation, delegateMemberBuilder, roleMembershipBuilder);
                        }
                    }
                }
            }
        }
        return roleMembershipBuilders;
    }

    protected void linkDelegateToRoleMembership(DelegateTypeBo delegation, DelegateMember.Builder delegateMemberBuilder,
            RoleMembership.Builder roleMembershipBuilder) {
        DelegateType.Builder delegateBuilder = null;
        for(DelegateType.Builder existingDelegateBuilder : roleMembershipBuilder.getDelegates()) {
            if (existingDelegateBuilder.getDelegationId().equals(delegation.getDelegationId())) {
                delegateBuilder = existingDelegateBuilder;
            }
        }
        if (delegateBuilder == null) {
            delegateBuilder = DelegateType.Builder.create(delegation);
            delegateBuilder.setMembers(new ArrayList<DelegateMember.Builder>());
            roleMembershipBuilder.getDelegates().add(delegateBuilder);
        }
        delegateBuilder.getMembers().add(delegateMemberBuilder);

    }

    /**
     * Once the delegations for a RoleMembershipInfo object have been determined,
     * any "role" member types need to be resolved into groups and principals so that
     * further KIM requests are not needed.
     */
    protected void resolveDelegationMemberRoles(List<RoleMembership.Builder> membershipBuilders,
            Map<String, String> qualification, Set<String> foundRoleTypeMembers) {
        // check delegations assigned to this role
        for (RoleMembership.Builder roleMembership : membershipBuilders) {
            // the applicable delegation IDs will already be set in the RoleMembership.Builder
            // this code examines those delegations and obtains the member groups and principals
            for (DelegateType.Builder delegation : roleMembership.getDelegates()) {
                List<DelegateMember.Builder> newMembers = new ArrayList<DelegateMember.Builder>();
                for (DelegateMember.Builder member : delegation.getMembers()) {
                    if (MemberType.ROLE.equals(member.getType())) {
                        // loop over delegation roles and extract the role IDs where the qualifications match
                        Collection<RoleMembership> delegateMembers = getRoleMembers(Collections.singletonList(
                                member.getMemberId()), qualification, false, foundRoleTypeMembers);
                        // loop over the role members and create the needed DelegationMember builders
                        for (RoleMembership rmi : delegateMembers) {
                            DelegateMember.Builder delegateMember = DelegateMember.Builder.create(member);
                            delegateMember.setMemberId(rmi.getMemberId());
                            delegateMember.setType(rmi.getType());
                            newMembers.add(delegateMember);
                        }
                    } else {
                        newMembers.add(member);
                    }
                }
                delegation.setMembers(newMembers);
            }
        }
    }

    public boolean principalHasRole(String principalId, List<String> roleIds, Map<String, String> qualification, boolean checkDelegations) {
        //want to cache if none of the roles are a derived role.  otherwise abort caching!
        boolean cacheResults = true;
        if (StringUtils.isBlank(principalId)) {
            return false;
        }


        Set<String> allRoleIds = new HashSet<String>();
        // remove inactive roles
        for (String roleId : roleIds) {
            if (isRoleActive(roleId)) {
                allRoleIds.add(roleId);
            }
        }
        // short-circuit if no roles match
        if (allRoleIds.isEmpty()) {
            return false;
        }
        // for efficiency, retrieve all roles and store in a map
        Map<String, RoleBoLite> roles = getRoleBoLiteMap(allRoleIds);
        // get all roles to which the principal is assigned
        List<String> copyRoleIds = new ArrayList<String>(allRoleIds);
        List<RoleMemberBo> rps = new ArrayList<RoleMemberBo>();

        for (String roleId : allRoleIds) {
            RoleTypeService roleTypeService = getRoleTypeService(roleId);
            if (roleTypeService != null) {
                List<String> attributesForExactMatch = roleTypeService.getQualifiersForExactMatch();
                if (CollectionUtils.isNotEmpty(attributesForExactMatch)) {
                    copyRoleIds.remove(roleId);
                    rps.addAll(getStoredRolePrincipalsForPrincipalIdAndRoleIds(Collections.singletonList(roleId), principalId, populateQualifiersForExactMatch(qualification, attributesForExactMatch)));
                }
            }
        }
        if (CollectionUtils.isNotEmpty(copyRoleIds)) {
            rps.addAll(getStoredRolePrincipalsForPrincipalIdAndRoleIds(copyRoleIds, principalId, null));
        }

        // if the qualification is null and the role list is not, then any role in the list will match
        // so since the role ID list is not blank, we can return true at this point
        if ((qualification == null || qualification.isEmpty()) && !rps.isEmpty()) {
            return true;
        }

        // check each membership to see if the principal matches

        // build a map of role ID to membership information
        // this will be used for later qualification checks
        Map<String, List<RoleMembership>> roleIdToMembershipMap = new HashMap<String, List<RoleMembership>>();
        if (getRoleIdToMembershipMap(roleIdToMembershipMap, rps)) {
            return true;
        }

        // perform the checks against the role type services
        for (Map.Entry<String, List<RoleMembership>> entry : roleIdToMembershipMap.entrySet()) {
            try {
                RoleTypeService roleTypeService = getRoleTypeService(entry.getKey());
                if (!roleTypeService.getMatchingRoleMemberships(qualification, entry.getValue()).isEmpty()) {
                    return true;
                }
            } catch (Exception ex) {
                LOG.warn("Unable to find role type service with id: " + entry.getKey());
            }
        }

        // find the groups that the principal belongs to
        List<String> principalGroupIds = getGroupService().getGroupIdsByPrincipalId(principalId);
        // find the role/group associations
        if (!principalGroupIds.isEmpty()) {
            List<RoleMemberBo> rgs = getStoredRoleGroupsUsingExactMatchOnQualification(principalGroupIds, allRoleIds, qualification);
            roleIdToMembershipMap.clear(); // clear the role/member map for further use
            if (getRoleIdToMembershipMap(roleIdToMembershipMap, rgs)) {
                return true;
            }

            // perform the checks against the role type services
            for (Map.Entry<String, List<RoleMembership>> entry : roleIdToMembershipMap.entrySet()) {
                try {
                    RoleTypeService roleTypeService = getRoleTypeService(entry.getKey());
                    if (!roleTypeService.getMatchingRoleMemberships(qualification, entry.getValue()).isEmpty()) {
                        return true;
                    }
                } catch (Exception ex) {
                    LOG.warn("Unable to find role type service with id: " + entry.getKey(), ex);
                }
            }
        }

        // check member roles
        // first, check that the qualifiers on the role membership match
        // then, perform a principalHasRole on the embedded role
        List<RoleMemberBo> roleMemberBos = getStoredRoleMembersForRoleIds(roleIds, MemberType.ROLE.getCode(), null);
        for (RoleMemberBo roleMemberBo : roleMemberBos) {
            RoleTypeService roleTypeService = getRoleTypeService(roleMemberBo.getRoleId());
            if (roleTypeService != null) {
                //it is possible that the the roleTypeService is coming from a remote application
                // and therefore it can't be guaranteed that it is up and working, so using a try/catch to catch this possibility.
                try {
                    if (roleTypeService.doesRoleQualifierMatchQualification(qualification, roleMemberBo.getAttributes())) {
                        RoleBoLite memberRole = getRoleBoLite(roleMemberBo.getMemberId());
                        Map<String, String> nestedRoleQualification = roleTypeService.convertQualificationForMemberRoles(
                                roles.get(roleMemberBo.getRoleId()).getNamespaceCode(),
                                roles.get(roleMemberBo.getRoleId()).getName(),
                                memberRole.getNamespaceCode(),
                                memberRole.getName(),
                                qualification);
                        ArrayList<String> roleIdTempList = new ArrayList<String>(1);
                        roleIdTempList.add(roleMemberBo.getMemberId());
                        if (this.getProxiedRoleService().principalHasRole(principalId, roleIdTempList, nestedRoleQualification, true)) {
                            return true;
                        }
                    }
                } catch (Exception ex) {
                    LOG.warn("Not able to retrieve RoleTypeService from remote system for role Id: " + roleMemberBo.getRoleId(), ex);
                    //return false;
                }
            } else {
                // no qualifiers - role is always used - check membership
                ArrayList<String> roleIdTempList = new ArrayList<String>(1);
                roleIdTempList.add(roleMemberBo.getMemberId());
                // no role type service, so can't convert qualification - just pass as is
                if (this.getProxiedRoleService().principalHasRole(principalId, roleIdTempList, qualification, true)) {
                    return true;
                }
            }

        }


        // check for derived roles and extract principals and groups from that - then check them against the
        // role type service passing in the qualification and principal - the qualifier comes from the
        // external system (application)

        // loop over the allRoleIds list
        for (String roleId : allRoleIds) {
            RoleBoLite role = roles.get(roleId);
            RoleTypeService roleTypeService = getRoleTypeService(roleId);
            // check if an derived role
            //it is possible that the the roleTypeService is coming from a remote application
            // and therefore it can't be guaranteed that it is up and working, so using a try/catch to catch this possibility.
            try {
                if (isDerivedRoleType(role.getKimTypeId(), roleTypeService)) {
                    //cache nothing that even has a derived role in roleIds list
                    cacheResults = false;
                    if (roleTypeService.hasDerivedRole(principalId, principalGroupIds, role.getNamespaceCode(), role.getName(), qualification)) {
                        return true;
                    }
                }
            } catch (Exception ex) {
                LOG.warn("Not able to retrieve RoleTypeService from remote system for role Id: " + roleId, ex);
                //return false;
            }
        }

        // delegations
        if (checkDelegations) {
            if (matchesOnDelegation(allRoleIds, principalId, principalGroupIds, qualification)) {
                return true;
            }
        }

        // NOTE: this logic is a little different from the getRoleMembers method
        // If there is no primary (matching non-delegate), this method will still return true
        return false;
    }


    protected boolean isDerivedRoleType(String roleTypeId, RoleTypeService service) {
        return service != null && service.isDerivedRoleType();
    }

    public boolean isDerivedRoleType(RoleTypeService service) {
        return service != null && service.isDerivedRoleType();
    }

    private boolean dynamicRoleMembership(RoleTypeService service, Role role) {
        return service != null && service.dynamicRoleMembership(role.getNamespaceCode(), role.getName());
    }

    @Override
    public boolean isDerivedRole(String roleId) {
        incomingParamCheck(roleId, "roleId");
        RoleTypeService service = getRoleTypeService(roleId);
        return isDerivedRoleType(service);
    }

    @Override
    public boolean isDynamicRoleMembership(String roleId) {
        incomingParamCheck(roleId, "roleId");
        RoleTypeService service = getRoleTypeService(roleId);
        return dynamicRoleMembership(service, getRole(roleId));
    }

    /**
     * Support method for principalHasRole.  Checks delegations on the passed in roles for the given principal and groups.  (It's assumed that the principal
     * belongs to the given groups.)
     * <p/>
     * Delegation checks are mostly the same as role checks except that the delegateBo itself is qualified against the original role (like a RolePrincipal
     * or RoleGroup.)  And then, the members of that delegateBo may have additional qualifiers which are not part of the original role qualifiers.
     * <p/>
     * For example:
     * <p/>
     * A role could be qualified by organization.  So, there is a person in the organization with primary authority for that org.  But, then they delegate authority
     * for that organization (not their authority - the delegateBo is attached to the org.)  So, in this case the delegateBo has a qualifier of the organization
     * when it is attached to the role.
     * <p/>
     * The principals then attached to that delegateBo (which is specific to the organization), may have additional qualifiers.
     * For Example: dollar amount range, effective dates, document types.
     * As a subsequent step, those qualifiers are checked against the qualification passed in from the client.
     */
    protected boolean matchesOnDelegation(Set<String> allRoleIds, String principalId, List<String> principalGroupIds, Map<String, String> qualification) {
        // get the list of delegations for the roles
        Map<String, DelegateTypeBo> delegations = getStoredDelegationImplMapFromRoleIds(allRoleIds);
        // loop over the delegations - determine those which need to be inspected more directly
        for (DelegateTypeBo delegation : delegations.values()) {
            // check if each one matches via the original role type service
            if (!delegation.isActive()) {
                continue;
            }
            RoleTypeService roleTypeService = getRoleTypeService(delegation.getRoleId());
            for (DelegateMemberBo delegateMemberBo : delegation.getMembers()) {
                if (!delegateMemberBo.isActive(new Timestamp(new Date().getTime()))) {
                    continue;
                }
                // check if this delegateBo record applies to the given person
                if (MemberType.PRINCIPAL.equals(delegateMemberBo.getType())
                        && !delegateMemberBo.getMemberId().equals(principalId)) {
                    continue; // no match on principal
                }
                // or if a group
                if (MemberType.GROUP.equals(delegateMemberBo.getType())
                        && !principalGroupIds.contains(delegateMemberBo.getMemberId())) {
                    continue; // no match on group
                }
                // or if a role
                if (MemberType.ROLE.equals(delegateMemberBo.getType())
                        && !this.getProxiedRoleService().principalHasRole(principalId, Collections.singletonList(delegateMemberBo.getMemberId()), qualification, false)) {
                    continue; // no match on role
                }
                // OK, the member matches the current user, now check the qualifications

                // NOTE: this compare is slightly different than the member enumeration
                // since the requested qualifier is always being used rather than
                // the role qualifier for the member (which is not available)

                //it is possible that the the roleTypeService is coming from a remote application
                // and therefore it can't be guaranteed that it is up and working, so using a try/catch to catch this possibility.
                try {
                    //TODO: remove reference to Attributes here and use Attributes instead.
                    if (roleTypeService != null && !roleTypeService.doesRoleQualifierMatchQualification(qualification, delegateMemberBo.getQualifier())) {
                        continue; // no match - skip to next record
                    }
                } catch (Exception ex) {
                    LOG.warn("Unable to call doesRoleQualifierMatchQualification on role type service for role Id: " + delegation.getRoleId() + " / " + qualification + " / " + delegateMemberBo.getQualifier(), ex);
                    continue;
                }

                // role service matches this qualifier
                // now try the delegateBo service
                DelegationTypeService delegationTypeService = getDelegationTypeService(delegateMemberBo.getDelegationId());
                // QUESTION: does the qualifier map need to be merged with the main delegateBo qualification?
                if (delegationTypeService != null && !delegationTypeService.doesDelegationQualifierMatchQualification(qualification, delegateMemberBo.getQualifier())) {
                    continue; // no match - skip to next record
                }
                // check if a role member ID is present on the delegateBo record
                // if so, check that the original role member would match the given qualifiers
                if (StringUtils.isNotBlank(delegateMemberBo.getRoleMemberId())) {
                    RoleMemberBo rm = getRoleMemberBo(delegateMemberBo.getRoleMemberId());
                    if (rm != null) {
                        // check that the original role member's is active and that their
                        // qualifier would have matched this request's
                        // qualifications (that the original person would have the permission/responsibility
                        // for an action)
                        // this prevents a role-membership based delegateBo from surviving the inactivation/
                        // changing of the main person's role membership
                        if (!rm.isActive(new Timestamp(new Date().getTime()))) {
                            continue;
                        }
                        Map<String, String> roleQualifier = rm.getAttributes();
                        //it is possible that the the roleTypeService is coming from a remote application
                        // and therefore it can't be guaranteed that it is up and working, so using a try/catch to catch this possibility.
                        try {
                            if (roleTypeService != null && !roleTypeService.doesRoleQualifierMatchQualification(qualification, roleQualifier)) {
                                continue;
                            }
                        } catch (Exception ex) {
                            LOG.warn("Unable to call doesRoleQualifierMatchQualification on role type service for role Id: " + delegation.getRoleId() + " / " + qualification + " / " + roleQualifier, ex);
                            continue;
                        }
                    } else {
                        LOG.warn("Unknown role member ID cited in the delegateBo member table:");
                        LOG.warn("       assignedToId: " + delegateMemberBo.getDelegationMemberId() + " / roleMemberId: " + delegateMemberBo.getRoleMemberId());
                    }
                }
                // all tests passed, return true
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method used by principalHasRole to build the role ID -> list of members map.
     *
     * @return <b>true</b> if no further checks are needed because no role service is defined
     */
    protected boolean getRoleIdToMembershipMap(Map<String, List<RoleMembership>> roleIdToMembershipMap, List<RoleMemberBo> roleMembers) {
        for (RoleMemberBo roleMemberBo : roleMembers) {
            RoleMembership roleMembership = RoleMembership.Builder.create(
                    roleMemberBo.getRoleId(),
                    roleMemberBo.getId(),
                    roleMemberBo.getMemberId(),
                    roleMemberBo.getType(),
                    roleMemberBo.getAttributes()).build();

            // if the role type service is null, assume that all qualifiers match
            if (getRoleTypeService(roleMemberBo.getRoleId()) == null) {
                return true;
            }
            List<RoleMembership> lrmi = roleIdToMembershipMap.get(roleMembership.getRoleId());
            if (lrmi == null) {
                lrmi = new ArrayList<RoleMembership>();
                roleIdToMembershipMap.put(roleMembership.getRoleId(), lrmi);
            }
            lrmi.add(roleMembership);
        }
        return false;
    }

    /**
     * Retrieves a KimDelegationImpl object by its ID. If the delegateBo already exists in the cache, this method will return the cached
     * version; otherwise, it will retrieve the uncached version from the database and then cache it before returning it.
     */
    protected DelegateTypeBo getKimDelegationImpl(String delegationId) {
        if (StringUtils.isBlank(delegationId)) {
            return null;
        }

        return getBusinessObjectService().findByPrimaryKey(DelegateTypeBo.class,
                Collections.singletonMap(KimConstants.PrimaryKeyConstants.DELEGATION_ID, delegationId));
    }

    protected DelegationTypeService getDelegationTypeService(String delegationId) {
        DelegationTypeService service = null;
        DelegateTypeBo delegateBo = getKimDelegationImpl(delegationId);
        KimType delegationType = KimApiServiceLocator.getKimTypeInfoService().getKimType(delegateBo.getKimTypeId());
        if (delegationType != null) {
            KimTypeService tempService = KimFrameworkServiceLocator.getKimTypeService(delegationType);
            if (tempService != null && tempService instanceof DelegationTypeService) {
                service = (DelegationTypeService) tempService;
            } else {
                LOG.error("Service returned for type " + delegationType + "(" + delegationType.getName() + ") was not a DelegationTypeService.  Was a " + (tempService != null ? tempService.getClass() : "(null)"));
            }
        } else { // delegateBo has no type - default to role type if possible
            RoleTypeService roleTypeService = getRoleTypeService(delegateBo.getRoleId());
            if (roleTypeService != null && roleTypeService instanceof DelegationTypeService) {
                service = (DelegationTypeService) roleTypeService;
            }
        }
        return service;
    }

    protected Collection<RoleMembership> getNestedRoleMembers(Map<String, String> qualification, RoleMembership rm, Set<String> foundRoleTypeMembers) {
        // If this role has already been traversed, skip it
        if (foundRoleTypeMembers.contains(rm.getMemberId())) {
            return new ArrayList<RoleMembership>();  // return an empty list
        }
        foundRoleTypeMembers.add(rm.getMemberId());

        ArrayList<String> roleIdList = new ArrayList<String>(1);
        roleIdList.add(rm.getMemberId());

        // get the list of members from the nested role - ignore delegations on those sub-roles
        Collection<RoleMembership> currentNestedRoleMembers = getRoleMembers(roleIdList, qualification, false, foundRoleTypeMembers);

        // add the roles whose members matched to the list for delegateBo checks later
        Collection<RoleMembership> returnRoleMembers = new ArrayList<RoleMembership>();
        for (RoleMembership roleMembership : currentNestedRoleMembers) {
            RoleMembership.Builder rmBuilder = RoleMembership.Builder.create(roleMembership);

            // use the member ID of the parent role (needed for responsibility joining)
            rmBuilder.setId(rm.getId());
            // store the role ID, so we know where this member actually came from
            rmBuilder.setRoleId(rm.getRoleId());
            rmBuilder.setEmbeddedRoleId(rm.getMemberId());
            returnRoleMembers.add(rmBuilder.build());
        }
        return returnRoleMembers;
    }

    /**
     * Retrieves a KimDelegationMemberImpl object by its ID and the ID of the delegation it belongs to. If the delegation member exists in the cache,
     * this method will return the cached one; otherwise, it will retrieve the uncached version from the database and then cache it before returning it.
     */
    protected DelegateMemberBo getKimDelegationMemberImplByDelegationAndId(String delegationId, String delegationMemberId) {
        if (StringUtils.isBlank(delegationId) || StringUtils.isBlank(delegationMemberId)) {
            return null;
        }

        Map<String, String> searchCriteria = new HashMap<String, String>();
        searchCriteria.put(KimConstants.PrimaryKeyConstants.DELEGATION_ID, delegationId);
        searchCriteria.put(KimConstants.PrimaryKeyConstants.DELEGATION_MEMBER_ID, delegationMemberId);
        List<DelegateMemberBo> memberList =
                (List<DelegateMemberBo>) getBusinessObjectService().findMatching(DelegateMemberBo.class, searchCriteria);
        if (memberList != null && !memberList.isEmpty()) {
            return memberList.get(0);
        }
        return null;
    }

    private List<RoleMemberBo> getStoredRoleMembersUsingExactMatchOnQualification(String principalId, List<String> groupIds, List<String> roleIds, Map<String, String> qualification) {
        List<String> copyRoleIds = new ArrayList<String>(roleIds);
        List<RoleMemberBo> roleMemberBoList = new ArrayList<RoleMemberBo>();

        for (String roleId : roleIds) {
            RoleTypeService roleTypeService = getRoleTypeService(roleId);
            if (roleTypeService != null) {
                List<String> attributesForExactMatch = roleTypeService.getQualifiersForExactMatch();
                if (CollectionUtils.isNotEmpty(attributesForExactMatch)) {
                    copyRoleIds.remove(roleId);
                    roleMemberBoList.addAll(getStoredRoleMembersForRoleIdsWithFilters(Collections.singletonList(roleId), principalId, groupIds, populateQualifiersForExactMatch(qualification, attributesForExactMatch)));
                }
            }
        }
        if (CollectionUtils.isNotEmpty(copyRoleIds)) {
            roleMemberBoList.addAll(getStoredRoleMembersForRoleIdsWithFilters(copyRoleIds, principalId, groupIds, null));
        }
        return roleMemberBoList;
    }

    private List<RoleMemberBo> getStoredRoleGroupsUsingExactMatchOnQualification(List<String> groupIds, Set<String> roleIds, Map<String, String> qualification) {
        List<String> copyRoleIds = new ArrayList<String>(roleIds);
        List<RoleMemberBo> roleMemberBos = new ArrayList<RoleMemberBo>();

        for (String roleId : roleIds) {
            RoleTypeService roleTypeService = getRoleTypeService(roleId);
            if (roleTypeService != null) {
                List<String> attributesForExactMatch = roleTypeService.getQualifiersForExactMatch();
                if (CollectionUtils.isNotEmpty(attributesForExactMatch)) {
                    copyRoleIds.remove(roleId);
                    roleMemberBos.addAll(getStoredRoleGroupsForGroupIdsAndRoleIds(Collections.singletonList(roleId), groupIds, populateQualifiersForExactMatch(qualification, attributesForExactMatch)));
                }
            }
        }
        if (CollectionUtils.isNotEmpty(copyRoleIds)) {
            roleMemberBos.addAll(getStoredRoleGroupsForGroupIdsAndRoleIds(copyRoleIds, groupIds, null));
        }
        return roleMemberBos;
    }

    private List<DelegateMember> getDelegateMembersForDelegation(DelegateTypeBo delegateBo) {
        if (delegateBo == null || delegateBo.getMembers() == null) {return null;}
        List<DelegateMember> delegateMembersReturnList = new ArrayList<DelegateMember>();
        for (DelegateMemberBo delegateMemberBo : delegateBo.getMembers()) {
            //FIXME: What is up with this!?!
            DelegateMember delegateMember = getDelegateCompleteInfo(delegateBo, delegateMemberBo);

            delegateMembersReturnList.add(DelegateMemberBo.to(delegateMemberBo));
        }
        return Collections.unmodifiableList(delegateMembersReturnList);
    }

    private DelegateMember getDelegateCompleteInfo(DelegateTypeBo delegateBo, DelegateMemberBo delegateMemberBo) {
        if (delegateBo == null || delegateMemberBo == null) {return null;}

        DelegateMember.Builder delegateMemberBuilder = DelegateMember.Builder.create(delegateMemberBo);
        delegateMemberBuilder.setType(delegateMemberBo.getType());
        return delegateMemberBuilder.build();
    }

    @Override
    public RoleMember assignPrincipalToRole(String principalId,
            String namespaceCode, String roleName, Map<String, String> qualifier) 
            throws RiceIllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");
        incomingParamCheck(qualifier, "qualifier");

        // look up the role
        RoleBo role = getRoleBoByName(namespaceCode, roleName);
        role.refreshReferenceObject("members");

        // check that identical member does not already exist
        List<RoleMember> membersMatchByExactQualifiers = doAnyMemberRecordsMatchByExactQualifier(role, principalId, memberTypeToRoleDaoActionMap.get(MemberType.PRINCIPAL.getCode()), qualifier);
        if (CollectionUtils.isNotEmpty(membersMatchByExactQualifiers)) {
            return membersMatchByExactQualifiers.get(0);
        }
        RoleMember anyMemberMatch = doAnyMemberRecordsMatch( role.getMembers(), principalId, MemberType.PRINCIPAL.getCode(), qualifier );
        if (null != anyMemberMatch) {
            return anyMemberMatch;
        }

        // create the new role member object
        RoleMemberBo newRoleMember = new RoleMemberBo();

        newRoleMember.setRoleId(role.getId());
        newRoleMember.setMemberId(principalId);
        newRoleMember.setType(MemberType.PRINCIPAL);

        // build role member attribute objects from the given Map<String, String>
        addMemberAttributeData(newRoleMember, qualifier, role.getKimTypeId());

        // add row to member table
        // When members are added to roles, clients must be notified.
        return RoleMemberBo.to(getResponsibilityInternalService().saveRoleMember(newRoleMember));
    }

    @Override
    public RoleMember assignGroupToRole(String groupId, String namespaceCode,
            String roleName, Map<String, String> qualifier) throws RiceIllegalStateException {
        incomingParamCheck(groupId, "groupId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");
        incomingParamCheck(qualifier, "qualifier");

        // look up the role
        RoleBo role = getRoleBoByName(namespaceCode, roleName);

        // check that identical member does not already exist
        List<RoleMember> membersMatchByExactQualifiers = doAnyMemberRecordsMatchByExactQualifier(role, groupId, memberTypeToRoleDaoActionMap.get(MemberType.GROUP.getCode()), qualifier);
        if (CollectionUtils.isNotEmpty(membersMatchByExactQualifiers)) {
            return membersMatchByExactQualifiers.get(0);
        }
        RoleMember anyMemberMatch = doAnyMemberRecordsMatch( role.getMembers(), groupId, MemberType.GROUP.getCode(), qualifier );
        if (null != anyMemberMatch) {
            return anyMemberMatch;
        }

        // create the new role member object
        RoleMemberBo newRoleMember = new RoleMemberBo();
        newRoleMember.setRoleId(role.getId());
        newRoleMember.setMemberId(groupId);
        newRoleMember.setType(MemberType.GROUP);

        // build role member attribute objects from the given Map<String, String>
        addMemberAttributeData(newRoleMember, qualifier, role.getKimTypeId());

        // When members are added to roles, clients must be notified.
        return RoleMemberBo.to(getResponsibilityInternalService().saveRoleMember(newRoleMember));
    }

    @Override
    public RoleMember assignRoleToRole(String roleId,
            String namespaceCode, String roleName, Map<String, String> qualifier) 
            throws RiceIllegalStateException {
        incomingParamCheck(roleId, "roleId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");
        incomingParamCheck(qualifier, "qualifier");

        // look up the roleBo
        RoleBo roleBo = getRoleBoByName(namespaceCode, roleName);

        // check that identical member does not already exist
        List<RoleMember> membersMatchByExactQualifiers = doAnyMemberRecordsMatchByExactQualifier(roleBo, roleId, memberTypeToRoleDaoActionMap.get(MemberType.ROLE.getCode()), qualifier);
        if (CollectionUtils.isNotEmpty(membersMatchByExactQualifiers)) {
            return membersMatchByExactQualifiers.get(0);
        }
        RoleMember anyMemberMatch = doAnyMemberRecordsMatch( roleBo.getMembers(), roleId, MemberType.ROLE.getCode(), qualifier);
        if (null != anyMemberMatch) {
            return anyMemberMatch;
        }

        
        // Check to make sure this doesn't create a circular membership
        if (!checkForCircularRoleMembership(roleId, roleBo)) {
            throw new IllegalArgumentException("Circular roleBo reference.");
        }
        // create the new roleBo member object
        RoleMemberBo newRoleMember = new RoleMemberBo();
        newRoleMember.setRoleId(roleBo.getId());
        newRoleMember.setMemberId(roleId);
        newRoleMember.setType(MemberType.ROLE);
        // build roleBo member attribute objects from the given Map<String, String>
        addMemberAttributeData(newRoleMember, qualifier, roleBo.getKimTypeId());

        // When members are added to roles, clients must be notified.
        return RoleMemberBo.to(getResponsibilityInternalService().saveRoleMember(newRoleMember));
    }

    @Override
    public RoleMember createRoleMember(RoleMember roleMember) throws RiceIllegalStateException {
        incomingParamCheck(roleMember, "roleMember");

        if (StringUtils.isNotBlank(roleMember.getId()) && getRoleMemberBo(roleMember.getId()) != null) {
            throw new RiceIllegalStateException("the roleMember to create already exists: " + roleMember);
        }

        String kimTypeId = getRoleBoLite(roleMember.getRoleId()).getKimTypeId();
        List<RoleMemberAttributeDataBo> attrBos = Collections.emptyList();
        attrBos = KimAttributeDataBo.createFrom(RoleMemberAttributeDataBo.class, roleMember.getAttributes(), kimTypeId);

        RoleMemberBo bo = RoleMemberBo.from(roleMember);
        bo.setAttributeDetails(attrBos);
        return RoleMemberBo.to(getResponsibilityInternalService().saveRoleMember(bo));
    }

    @Override
    public RoleMember updateRoleMember(@WebParam(
            name = "roleMember") RoleMember roleMember) throws RiceIllegalArgumentException, RiceIllegalStateException {
        incomingParamCheck(roleMember, "roleMember");

        if (StringUtils.isBlank(roleMember.getId()) || getRoleMemberBo(roleMember.getId()) == null) {
            throw new RiceIllegalStateException("the roleMember to update does not exists: " + roleMember);
        }

        String kimTypeId = getRoleBoLite(roleMember.getRoleId()).getKimTypeId();
        List<RoleMemberAttributeDataBo> attrBos = Collections.emptyList();
        attrBos = KimAttributeDataBo.createFrom(RoleMemberAttributeDataBo.class, roleMember.getAttributes(), kimTypeId);

        RoleMemberBo bo = RoleMemberBo.from(roleMember);
        bo.setAttributeDetails(attrBos);
        return RoleMemberBo.to(getResponsibilityInternalService().saveRoleMember(bo));
    }

    @Override
    public DelegateMember updateDelegateMember(@WebParam(
            name = "delegateMember") DelegateMember delegateMember) throws RiceIllegalArgumentException, RiceIllegalStateException {

        //check delegateMember not empty
        incomingParamCheck(delegateMember, "delegateMember");

        //check key is null
        if(delegateMember.getDelegationMemberId()!=null )   {
            throw new RiceIllegalStateException("the delegate member already exists: " + delegateMember.getDelegationMemberId());
        }

        //check delegate exists
        String delegationId =  delegateMember.getDelegationId();
        incomingParamCheck(delegationId,"delegationId");
        DelegateTypeBo delegate = getKimDelegationImpl(delegationId);
        if(delegate==null)   {
            throw new RiceIllegalStateException("the delegate does not exist: " + delegationId);
        }

        //save the delegateMember  (actually updates)
        String kimTypeId = getRoleBoLite(delegate.getRoleId()).getKimTypeId();
        List<DelegateMemberAttributeDataBo> attrBos = Collections.emptyList();
        attrBos = KimAttributeDataBo.createFrom(DelegateMemberAttributeDataBo.class, delegateMember.getAttributes(), kimTypeId);
        DelegateMemberBo bo = DelegateMemberBo.from(delegateMember);
        bo.setAttributeDetails(attrBos);
        return DelegateMemberBo.to(getResponsibilityInternalService().saveDelegateMember(bo));
    }

    @Override
    public DelegateMember createDelegateMember(@WebParam(
            name = "delegateMember") DelegateMember delegateMember) throws RiceIllegalArgumentException, RiceIllegalStateException {
        //ensure object not empty
        incomingParamCheck(delegateMember, "delegateMember");

        //check key is null
        if(delegateMember.getDelegationMemberId()!=null )   {
            throw new RiceIllegalStateException("the delegate member already exists: " + delegateMember.getDelegationMemberId());
        }

        //check delegate exists
        String delegationId =  delegateMember.getDelegationId();
        incomingParamCheck(delegationId,"delegationId");
        DelegateTypeBo delegate = getKimDelegationImpl(delegationId);
        if(delegate==null)   {
            throw new RiceIllegalStateException("the delegate does not exist: " + delegationId); 
        }

        //check member exists
        String memberId = delegateMember.getMemberId();
        incomingParamCheck(memberId,"memberId");
        Principal kPrincipal = KimApiServiceLocator.getIdentityService().getPrincipal(memberId);
        if(kPrincipal==null){
            throw new RiceIllegalStateException("the user does not exist: " + memberId);
        }

        //create member delegate
        String kimTypeId = getRoleBoLite(delegate.getRoleId()).getKimTypeId();
        List<DelegateMemberAttributeDataBo> attrBos = Collections.emptyList();
        attrBos = KimAttributeDataBo.createFrom(DelegateMemberAttributeDataBo.class, delegateMember.getAttributes(), kimTypeId);
        DelegateMemberBo bo = DelegateMemberBo.from(delegateMember);
        bo.setAttributeDetails(attrBos);
        return DelegateMemberBo.to(getResponsibilityInternalService().saveDelegateMember(bo));
    }

    @Override
    public void removeDelegateMembers(@WebParam(
            name = "delegateMembers") List<DelegateMember> delegateMembers) throws RiceIllegalArgumentException, RiceIllegalStateException {
        incomingParamCheck(delegateMembers, "delegateMembers");
        for (DelegateMember delegateMember : delegateMembers) {
            DelegateMember.Builder delegateMemberInfo = DelegateMember.Builder.create();
            delegateMemberInfo.setAttributes(delegateMember.getAttributes());
            delegateMemberInfo.setDelegationId(delegateMember.getDelegationId());
            delegateMemberInfo.setMemberId(delegateMember.getMemberId());
            delegateMemberInfo.setRoleMemberId(delegateMember.getRoleMemberId());
            delegateMemberInfo.setType(delegateMember.getType());
            delegateMemberInfo.setActiveFromDate(delegateMember.getActiveFromDate());
            delegateMemberInfo.setActiveToDate(DateTime.now());
            updateDelegateMember(delegateMemberInfo.build());
        }
    }

    @Override
    public RoleResponsibilityAction createRoleResponsibilityAction(RoleResponsibilityAction roleResponsibilityAction)
            throws RiceIllegalArgumentException, RiceIllegalStateException {
        incomingParamCheck(roleResponsibilityAction, "roleResponsibilityAction");


        if (StringUtils.isNotBlank(roleResponsibilityAction.getId())
                && getBusinessObjectService().findByPrimaryKey
                (RoleResponsibilityActionBo.class, Collections.singletonMap("id", roleResponsibilityAction.getId()))  != null) {
            throw new RiceIllegalStateException("the roleResponsibilityAction to create already exists: " + roleResponsibilityAction);
        }

        RoleResponsibilityActionBo bo = RoleResponsibilityActionBo.from(roleResponsibilityAction);
        return RoleResponsibilityActionBo.to(getBusinessObjectService().save(bo));
    }

    @Override
    public DelegateType createDelegateType(DelegateType delegateType) throws RiceIllegalArgumentException, RiceIllegalStateException {
        incomingParamCheck(delegateType, "delegateType");

        if (StringUtils.isNotBlank(delegateType.getDelegationId())
                && getDelegateTypeByDelegationId(delegateType.getDelegationId()) != null) {
            throw new RiceIllegalStateException("the delegateType to create already exists: " + delegateType);
        }

        DelegateTypeBo bo = DelegateTypeBo.from(delegateType);
        return DelegateTypeBo.to(getBusinessObjectService().save(bo));
    	// look up the role
        /*RoleBo role = getRoleBo(delegationType.getRoleId());
    	DelegateTypeBo delegation = getDelegationOfType(role.getId(), delegationType.getDelegationTypeCode());
    	// create the new role member object
    	DelegateMemberBo newDelegationMember = new DelegateMemberBo();

    	DelegateMemberBo origDelegationMember;
    	if(StringUtils.isNotEmpty(delegationMemberId)){
    		origDelegationMember = getDelegateMemberBo(delegationMemberId);
    	} else{
    		List<DelegateMemberBo> origDelegationMembers =
                    this.getDelegationMemberBoListByMemberAndDelegationId(memberId, delegation.getDelegationId());
	    	origDelegationMember =
	    		(origDelegationMembers!=null && !origDelegationMembers.isEmpty()) ? origDelegationMembers.get(0) : null;
    	}
    	if(origDelegationMember!=null){
    		newDelegationMember.setDelegationMemberId(origDelegationMember.getDelegationMemberId());
    		newDelegationMember.setVersionNumber(origDelegationMember.getVersionNumber());
    	}
    	newDelegationMember.setMemberId(memberId);
    	newDelegationMember.setDelegationId(delegation.getDelegationId());
    	newDelegationMember.setRoleMemberId(roleMemberId);
    	newDelegationMember.setTypeCode(memberTypeCode);
		if (activeFromDate != null) {
			newDelegationMember.setActiveFromDateValue(new java.sql.Timestamp(activeFromDate.getMillis()));
		}
		if (activeToDate != null) {
			newDelegationMember.setActiveToDateValue(new java.sql.Timestamp(activeToDate.getMillis()));
		}

    	// build role member attribute objects from the given Map<String, String>
    	addDelegationMemberAttributeData( newDelegationMember, qualifications, role.getKimTypeId() );

    	List<DelegateMemberBo> delegationMembers = new ArrayList<DelegateMemberBo>();
    	delegationMembers.add(newDelegationMember);
    	delegation.setMembers(delegationMembers);

    	getBusinessObjectService().save(delegation);
    	for(DelegateMemberBo delegationMember: delegation.getMembers()){
    		deleteNullDelegationMemberAttributeData(delegationMember.getAttributes());
    	}*/
    }

    @Override
    public DelegateType updateDelegateType(DelegateType delegateType) throws RiceIllegalArgumentException, RiceIllegalStateException {
        incomingParamCheck(delegateType, "delegateType");

        if (StringUtils.isBlank(delegateType.getDelegationId())
                || getDelegateTypeByDelegationId(delegateType.getDelegationId()) == null) {
            throw new RiceIllegalStateException("the delegateType to update does not exist: " + delegateType);
        }

        DelegateTypeBo bo = DelegateTypeBo.from(delegateType);
        return DelegateTypeBo.to(getBusinessObjectService().save(bo));
    }


    private void removeRoleMembers(List<RoleMemberBo> members) {
        if(CollectionUtils.isNotEmpty(members)) {
            for ( RoleMemberBo rm : members ) {
                getResponsibilityInternalService().removeRoleMember(rm);
            }
        }
    }


    private List<RoleMemberBo> getRoleMembersByDefaultStrategy(RoleBo role, String memberId, String memberTypeCode, Map<String, String> qualifier) {
        List<RoleMemberBo> rms = new ArrayList<RoleMemberBo>();
        role.refreshReferenceObject("members");
        for ( RoleMemberBo rm : role.getMembers() ) {
            if ( doesMemberMatch( rm, memberId, memberTypeCode, qualifier ) ) {
                // if found, remove
                rms.add(rm);
            }
        }
        return rms;
    }

    @Override
    public void removePrincipalFromRole(String principalId,
            String namespaceCode, String roleName, Map<String, String> qualifier) throws RiceIllegalArgumentException {
        if (StringUtils.isBlank(principalId)) {
            throw new RiceIllegalArgumentException("principalId is null");
        }

        if (StringUtils.isBlank(namespaceCode)) {
            throw new RiceIllegalArgumentException("namespaceCode is null");
        }

        if (StringUtils.isBlank(roleName)) {
            throw new RiceIllegalArgumentException("roleName is null");
        }

        if (qualifier == null) {
            throw new RiceIllegalArgumentException("qualifier is null");
        }

        // look up the role
    	RoleBo role = getRoleBoByName(namespaceCode, roleName);
        role.refreshReferenceObject("members");
    	// pull all the principal members
    	// look for an exact qualifier match
        List<RoleMemberBo> rms = getRoleMembersByExactQualifierMatch(role, principalId, memberTypeToRoleDaoActionMap.get(MemberType.PRINCIPAL.getCode()), qualifier);
        if(CollectionUtils.isEmpty(rms)) {
            rms = getRoleMembersByDefaultStrategy(role, principalId, MemberType.PRINCIPAL.getCode(), qualifier);
        }
        removeRoleMembers(rms);
    }

    @Override
    public void removeGroupFromRole(String groupId,
            String namespaceCode, String roleName, Map<String, String> qualifier) throws RiceIllegalArgumentException {
        if (StringUtils.isBlank(groupId)) {
            throw new RiceIllegalArgumentException("groupId is null");
        }

        if (StringUtils.isBlank(namespaceCode)) {
            throw new RiceIllegalArgumentException("namespaceCode is null");
        }

        if (StringUtils.isBlank(roleName)) {
            throw new RiceIllegalArgumentException("roleName is null");
        }

        if (qualifier == null) {
            throw new RiceIllegalArgumentException("qualifier is null");
        }

        // look up the roleBo
    	RoleBo roleBo = getRoleBoByName(namespaceCode, roleName);
    	// pull all the group roleBo members
    	// look for an exact qualifier match
        List<RoleMemberBo> rms = getRoleMembersByExactQualifierMatch(roleBo, groupId, memberTypeToRoleDaoActionMap.get(MemberType.GROUP.getCode()), qualifier);
        if(CollectionUtils.isEmpty(rms)) {
            rms = getRoleMembersByDefaultStrategy(roleBo, groupId, MemberType.GROUP.getCode(), qualifier);
        }
        removeRoleMembers(rms);
    }

    @Override
    public void removeRoleFromRole(String roleId,
            String namespaceCode, String roleName, Map<String, String> qualifier) throws RiceIllegalArgumentException {
        incomingParamCheck(roleId, "roleId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(roleName, "roleName");
        incomingParamCheck(qualifier, "qualifier");


        // look up the role
    	RoleBo role = getRoleBoByName(namespaceCode, roleName);
    	// pull all the group role members
    	// look for an exact qualifier match
        List<RoleMemberBo> rms = getRoleMembersByExactQualifierMatch(role, roleId, memberTypeToRoleDaoActionMap.get(MemberType.ROLE.getCode()), qualifier);
        if(CollectionUtils.isEmpty(rms)) {
            rms = getRoleMembersByDefaultStrategy(role, roleId, MemberType.ROLE.getCode(), qualifier);
        }
        removeRoleMembers(rms);
    }

    @Override
    public void assignPermissionToRole(String permissionId, String roleId) throws RiceIllegalArgumentException {
        incomingParamCheck(permissionId, "permissionId");
        incomingParamCheck(roleId, "roleId");

        RolePermissionBo newRolePermission = new RolePermissionBo();

        Long nextSeq = KRADServiceLocator.getSequenceAccessorService().getNextAvailableSequenceNumber(KimConstants.SequenceNames.KRIM_ROLE_PERM_ID_S, RolePermissionBo.class);

        if (nextSeq == null) {
            LOG.error("Unable to get new role permission id from sequence " + KimConstants.SequenceNames.KRIM_ROLE_PERM_ID_S);
            throw new RuntimeException("Unable to get new role permission id from sequence " + KimConstants.SequenceNames.KRIM_ROLE_PERM_ID_S);
        }

        newRolePermission.setId(nextSeq.toString());
        newRolePermission.setRoleId(roleId);
        newRolePermission.setPermissionId(permissionId);
        newRolePermission.setActive(true);

        getBusinessObjectService().save(newRolePermission);
    }

    @Override
    public void revokePermissionFromRole(String permissionId, String roleId) throws RiceIllegalArgumentException {
        incomingParamCheck(permissionId, "permissionId");
        incomingParamCheck(roleId, "roleId");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("roleId", roleId);
        params.put("permissionId", permissionId);
        params.put("active", Boolean.TRUE);
        Collection<RolePermissionBo> rolePermissionBos = getBusinessObjectService().findMatching(RolePermissionBo.class, params);
        List<RolePermissionBo> rolePermsToSave = new ArrayList<RolePermissionBo>();
        for (RolePermissionBo rolePerm : rolePermissionBos) {
            rolePerm.setActive(false);
            rolePermsToSave.add(rolePerm);
        }

        getBusinessObjectService().save(rolePermsToSave);
    }

    protected void addMemberAttributeData(RoleMemberBo roleMember, Map<String, String> qualifier, String kimTypeId) {
        List<RoleMemberAttributeDataBo> attributes = new ArrayList<RoleMemberAttributeDataBo>();
        for (Map.Entry<String, String> entry : qualifier.entrySet()) {
            RoleMemberAttributeDataBo roleMemberAttrBo = new RoleMemberAttributeDataBo();
            roleMemberAttrBo.setAttributeValue(entry.getValue());
            roleMemberAttrBo.setKimTypeId(kimTypeId);
            roleMemberAttrBo.setAssignedToId(roleMember.getId());
            // look up the attribute ID
            roleMemberAttrBo.setKimAttributeId(getKimAttributeId(entry.getKey()));

            Map<String, String> criteria = new HashMap<String, String>();
            criteria.put(KimConstants.PrimaryKeyConstants.KIM_ATTRIBUTE_ID, roleMemberAttrBo.getKimAttributeId());
            //criteria.put(KimConstants.PrimaryKeyConstants.ROLE_MEMBER_ID, roleMember.getId());
            criteria.put("assignedToId", roleMember.getId());
            List<RoleMemberAttributeDataBo> origRoleMemberAttributes =
                    (List<RoleMemberAttributeDataBo>) getBusinessObjectService().findMatching(RoleMemberAttributeDataBo.class, criteria);
            RoleMemberAttributeDataBo origRoleMemberAttribute =
                    (origRoleMemberAttributes != null && !origRoleMemberAttributes.isEmpty()) ? origRoleMemberAttributes.get(0) : null;
            if (origRoleMemberAttribute != null) {
                roleMemberAttrBo.setId(origRoleMemberAttribute.getId());
                roleMemberAttrBo.setVersionNumber(origRoleMemberAttribute.getVersionNumber());
            }
            attributes.add(roleMemberAttrBo);
        }
        roleMember.setAttributeDetails(attributes);
    }

    protected void addDelegationMemberAttributeData( DelegateMemberBo delegationMember, Map<String, String> qualifier, String kimTypeId ) {
		List<DelegateMemberAttributeDataBo> attributes = new ArrayList<DelegateMemberAttributeDataBo>();
		for (  Map.Entry<String, String> entry : qualifier.entrySet() ) {
			DelegateMemberAttributeDataBo delegateMemberAttrBo = new DelegateMemberAttributeDataBo();
			delegateMemberAttrBo.setAttributeValue(entry.getValue());
			delegateMemberAttrBo.setKimTypeId(kimTypeId);
			delegateMemberAttrBo.setAssignedToId(delegationMember.getDelegationMemberId());
			// look up the attribute ID
			delegateMemberAttrBo.setKimAttributeId(getKimAttributeId(entry.getKey()));
	    	Map<String, String> criteria = new HashMap<String, String>();
	    	criteria.put(KimConstants.PrimaryKeyConstants.KIM_ATTRIBUTE_ID, delegateMemberAttrBo.getKimAttributeId());
	    	criteria.put(KimConstants.PrimaryKeyConstants.DELEGATION_MEMBER_ID, delegationMember.getDelegationMemberId());
			List<DelegateMemberAttributeDataBo> origDelegationMemberAttributes =
	    		(List<DelegateMemberAttributeDataBo>)getBusinessObjectService().findMatching(DelegateMemberAttributeDataBo.class, criteria);
			DelegateMemberAttributeDataBo origDelegationMemberAttribute =
	    		(origDelegationMemberAttributes!=null && !origDelegationMemberAttributes.isEmpty()) ? origDelegationMemberAttributes.get(0) : null;
	    	if(origDelegationMemberAttribute!=null){
	    		delegateMemberAttrBo.setId(origDelegationMemberAttribute.getId());
	    		delegateMemberAttrBo.setVersionNumber(origDelegationMemberAttribute.getVersionNumber());
	    	}
			attributes.add( delegateMemberAttrBo );
		}
		delegationMember.setAttributeDetails( attributes );
	}



    // --------------------
    // Persistence Methods
    // --------------------

	private void deleteNullMemberAttributeData(List<RoleMemberAttributeDataBo> attributes) {
		List<RoleMemberAttributeDataBo> attributesToDelete = new ArrayList<RoleMemberAttributeDataBo>();
		for(RoleMemberAttributeDataBo attribute: attributes){
			if(attribute.getAttributeValue()==null){
				attributesToDelete.add(attribute);
			}
		}
		getBusinessObjectService().delete(attributesToDelete);
	}

    private void deleteNullDelegationMemberAttributeData(List<DelegateMemberAttributeDataBo> attributes) {
        List<DelegateMemberAttributeDataBo> attributesToDelete = new ArrayList<DelegateMemberAttributeDataBo>();

		for(DelegateMemberAttributeDataBo attribute: attributes){
			if(attribute.getAttributeValue()==null){
				attributesToDelete.add(attribute);
			}
		}
		getBusinessObjectService().delete(attributesToDelete);
	}

    protected void logPrincipalHasRoleCheck(String principalId, List<String> roleIds, Map<String, String> roleQualifiers ) {
        StringBuilder sb = new StringBuilder();
        sb.append(  '\n' );
        sb.append( "Has Role     : " ).append( roleIds ).append( '\n' );
        if ( roleIds != null ) {
            for ( String roleId : roleIds ) {
                Role role = getRole( roleId );
                if ( role != null ) {
                    sb.append( "        Name : " ).append( role.getNamespaceCode() ).append( '/').append( role.getName() );
                    sb.append( " (" ).append( roleId ).append( ')' );
                    sb.append( '\n' );
                }
            }
        }
        sb.append( "   Principal : " ).append( principalId );
        if ( principalId != null ) {
            Principal principal = KimApiServiceLocator.getIdentityService().getPrincipal(principalId);
            if ( principal != null ) {
                sb.append( " (" ).append( principal.getPrincipalName() ).append( ')' );
            }
        }
        sb.append( '\n' );
        sb.append( "     Details :\n" );
        if ( roleQualifiers != null ) {
            sb.append( roleQualifiers );
        } else {
            sb.append( "               [null]\n" );
        }
        if (LOG.isTraceEnabled()) { 
            LOG.trace( sb.append(ExceptionUtils.getStackTrace(new Throwable())));
        } else {
            LOG.debug(sb.toString());
        }
    }

    private void incomingParamCheck(Object object, String name) {
        if (object == null) {
            throw new RiceIllegalArgumentException(name + " was null");
        } else if (object instanceof String
                && StringUtils.isBlank((String) object)) {
            throw new RiceIllegalArgumentException(name + " was blank");
        }
    }

    /**
     * This gets the proxied version of the role service which will go through
     * Spring's caching mechanism for method calls rather than skipping it when
     * methods are called directly.
     * 
     * @return The proxied role service
     */
    protected RoleService getProxiedRoleService() {
        if(this.proxiedRoleService == null) {
            this.proxiedRoleService = KimApiServiceLocator.getRoleService();
        }
        return this.proxiedRoleService;
    }
}
