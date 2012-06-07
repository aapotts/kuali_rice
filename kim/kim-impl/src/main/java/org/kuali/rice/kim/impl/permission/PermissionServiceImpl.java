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
package org.kuali.rice.kim.impl.permission;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.cache.CacheKeyUtils;
import org.kuali.rice.core.api.criteria.CriteriaLookupService;
import org.kuali.rice.core.api.criteria.GenericQueryResults;
import org.kuali.rice.core.api.criteria.LookupCustomizer;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.exception.RiceIllegalArgumentException;
import org.kuali.rice.core.api.exception.RiceIllegalStateException;
import org.kuali.rice.core.api.membership.MemberType;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.impl.cache.DistributedCacheManagerDecorator;
import org.kuali.rice.kim.api.KimApiConstants;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.common.assignee.Assignee;
import org.kuali.rice.kim.api.common.delegate.DelegateType;
import org.kuali.rice.kim.api.common.template.Template;
import org.kuali.rice.kim.api.common.template.TemplateQueryResults;
import org.kuali.rice.kim.api.identity.principal.Principal;
import org.kuali.rice.kim.api.permission.Permission;
import org.kuali.rice.kim.api.permission.PermissionQueryResults;
import org.kuali.rice.kim.api.permission.PermissionService;
import org.kuali.rice.kim.api.role.RoleMember;
import org.kuali.rice.kim.api.role.RoleMembership;
import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.api.type.KimType;
import org.kuali.rice.kim.api.type.KimTypeInfoService;
import org.kuali.rice.kim.framework.permission.PermissionTypeService;
import org.kuali.rice.kim.impl.common.attribute.AttributeTransform;
import org.kuali.rice.kim.impl.common.attribute.KimAttributeDataBo;
import org.kuali.rice.kim.impl.role.RolePermissionBo;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.KRADPropertyConstants;
import org.springframework.cache.Cache;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.kuali.rice.core.api.criteria.PredicateFactory.*;

public class PermissionServiceImpl implements PermissionService {
    private static final Logger LOG = Logger.getLogger( PermissionServiceImpl.class );

	private RoleService roleService;
    private PermissionTypeService defaultPermissionTypeService;
    private KimTypeInfoService kimTypeInfoService;
	private BusinessObjectService businessObjectService;
	private CriteriaLookupService criteriaLookupService;

 	private final CopyOnWriteArrayList<Template> allTemplates = new CopyOnWriteArrayList<Template>();

    // --------------------
    // Authorization Checks
    // --------------------

    protected PermissionTypeService getPermissionTypeService( PermissionTemplateBo permissionTemplate ) {
    	if ( permissionTemplate == null ) {
    		throw new IllegalArgumentException( "permissionTemplate may not be null" );
    	}
    	KimType kimType = kimTypeInfoService.getKimType( permissionTemplate.getKimTypeId() );
    	String serviceName = kimType.getServiceName();
    	// if no service specified, return a default implementation
    	if ( StringUtils.isBlank( serviceName ) ) {
    		return defaultPermissionTypeService;
    	}
    	try {
	    	Object service = GlobalResourceLoader.getService(QName.valueOf(serviceName));
	    	// if we have a service name, it must exist
	    	if ( service == null ) {
				throw new RuntimeException("null returned for permission type service for service name: " + serviceName);
	    	}
	    	// whatever we retrieved must be of the correct type
	    	if ( !(service instanceof PermissionTypeService)  ) {
	    		throw new RuntimeException( "Service " + serviceName + " was not a PermissionTypeService.  Was: " + service.getClass().getName() );
	    	}
	    	return (PermissionTypeService)service;
    	} catch( Exception ex ) {
    		// sometimes service locators throw exceptions rather than returning null, handle that
    		throw new RuntimeException( "Error retrieving service: " + serviceName + " from the KimImplServiceLocator.", ex );
    	}
    }

    @Override
    public boolean hasPermission(String principalId, String namespaceCode,
                                 String permissionName) throws RiceIllegalArgumentException  {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionName, "permissionName");

        return isAuthorized( principalId, namespaceCode, permissionName, Collections.<String, String>emptyMap() );
    }

    @Override
    public boolean isAuthorized(String principalId, String namespaceCode,
                                String permissionName, Map<String, String> qualification ) throws RiceIllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionName, "permissionName");
        incomingParamCheck(qualification, "qualification");

        if ( LOG.isDebugEnabled() ) {
            logAuthorizationCheck("Permission", principalId, namespaceCode, permissionName, qualification);
        }

        DistributedCacheManagerDecorator distributedKimCache = getKimDistributedCacheManager();

        StringBuffer cacheKey =  new StringBuffer("{isAuthorized}principalId=").append(principalId).append("|")
                .append("namespaceCode=").append(namespaceCode).append("|")
                .append("permissionName=").append(permissionName).append("|")
                .append("qualification=").append(CacheKeyUtils.mapKey(qualification));
        if (distributedKimCache != null) {
            Cache.ValueWrapper cachedValue = distributedKimCache.getCache(Permission.Cache.NAME).get(cacheKey);
            if (cachedValue != null && cachedValue.get() instanceof Boolean) {
                return ((Boolean)cachedValue.get()).booleanValue();
            }
        }
        List<String> roleIds = getRoleIdsForPermission(namespaceCode, permissionName);
    	if ( roleIds.isEmpty() ) {
    		if ( LOG.isDebugEnabled() ) {
    			LOG.debug( "Result: false");
    		}
    		return false;
    	}

    	Boolean isAuthorized = roleService.principalHasRole(principalId, roleIds, qualification);
        
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Result: " + isAuthorized );
        }

        //check if anything exists in RoleMember Cache to see if we should cache in Permission Cache as well
        //if not cached in RoleMember cache, that means we are dealing with derived roles and don't want to mess with it
        if (distributedKimCache != null) {
            StringBuffer roleMembercacheKey =  new StringBuffer("{principalHasRole}principalId=")
                    .append(principalId).append("|")
                    .append("roleIds=").append(CacheKeyUtils.key(roleIds)).append("|")
                    .append("qualification=").append(CacheKeyUtils.mapKey(qualification));
            if (distributedKimCache.getCache(RoleMember.Cache.NAME).get(roleMembercacheKey) != null) {
                distributedKimCache.getCache(Permission.Cache.NAME).put(cacheKey, isAuthorized);
            }
        }

        return isAuthorized;

    }
    @Override
    public boolean hasPermissionByTemplate(String principalId, String namespaceCode, String permissionTemplateName,
            Map<String, String> permissionDetails) throws RiceIllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionTemplateName, "permissionTemplateName");

        return isAuthorizedByTemplate(principalId, namespaceCode, permissionTemplateName, permissionDetails,
                Collections.<String, String>emptyMap());
    }
    @Override
    public boolean isAuthorizedByTemplate(String principalId, String namespaceCode, String permissionTemplateName,
            Map<String, String> permissionDetails, Map<String, String> qualification) throws RiceIllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionTemplateName, "permissionTemplateName");
        incomingParamCheck(qualification, "qualification");

        if ( LOG.isDebugEnabled() ) {
            logAuthorizationCheckByTemplate("Perm Templ", principalId, namespaceCode, permissionTemplateName, permissionDetails, qualification);
        }

        //DistributedCacheManagerDecorator distributedKimCache = getKimDistributedCacheManager();

        //StringBuffer cacheKey =  new StringBuffer("{isAuthorizedByTemplate}principalId=").append(principalId).append("|")
        //        .append("namespaceCode=").append(namespaceCode).append("|")
        //        .append("permissionTemplateName=").append(permissionTemplateName).append("|")
        //        .append("permissionDetails=").append(CacheKeyUtils.mapKey(permissionDetails)).append("|")
        //        .append("qualification=").append(CacheKeyUtils.mapKey(qualification));
        //if (distributedKimCache != null) {
        //    Cache.ValueWrapper cachedValue = distributedKimCache.getCache(Permission.Cache.NAME).get(cacheKey);
        //    if (cachedValue != null && cachedValue.get() instanceof Boolean) {
        //        return ((Boolean)cachedValue.get()).booleanValue();
        //    }
        //}

        List<String> roleIds = getRoleIdsForPermissionTemplate( namespaceCode, permissionTemplateName, permissionDetails );
    	if ( roleIds.isEmpty() ) {
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "Result: false");
            }
    		return false;
    	}
    		
        Boolean isAuthorized = roleService.principalHasRole( principalId, roleIds, qualification);
    
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Result: " + isAuthorized );
        }

        //check if anything exists in RoleMember Cache to see if we should cache in Permission Cache as well
        //if not cached in RoleMember cache, that means we are dealing with derived roles and don't want to mess with it
        //if (distributedKimCache != null) {
        //    StringBuffer roleMembercacheKey =  new StringBuffer("{principalHasRole}principalId=")
        //            .append(principalId).append("|")
        //            .append("roleIds=").append(CacheKeyUtils.key(roleIds)).append("|")
        //            .append("qualification=").append(CacheKeyUtils.mapKey(qualification));
        //    if (distributedKimCache.getCache(RoleMember.Cache.NAME).get(roleMembercacheKey) != null) {
        //        distributedKimCache.getCache(Permission.Cache.NAME).put(cacheKey, isAuthorized);
        //    }
        //}

		return isAuthorized;
    	
    }
    @Override
    public List<Permission> getAuthorizedPermissions( String principalId,
            String namespaceCode, String permissionName,
            Map<String, String> qualification ) throws RiceIllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionName, "permissionName");
        incomingParamCheck(qualification, "qualification");

        DistributedCacheManagerDecorator distributedKimCache = getKimDistributedCacheManager();

        StringBuffer cacheKey =  new StringBuffer("{getAuthorizedPermissions}principalId=").append(principalId).append("|")
                .append("namespaceCode=").append(namespaceCode).append("|")
                .append("permissionName=").append(permissionName).append("|")
                .append("qualification=").append(CacheKeyUtils.mapKey(qualification));
        if (distributedKimCache != null) {
            Cache.ValueWrapper cachedValue = distributedKimCache.getCache(Permission.Cache.NAME).get(cacheKey);
            if (cachedValue != null && cachedValue.get() instanceof List) {
                return ((List<Permission>)cachedValue.get());
            }
        }
        // get all the permission objects whose name match that requested
    	List<PermissionBo> permissions = getPermissionImplsByName( namespaceCode, permissionName );
    	// now, filter the full list by the detail passed
    	List<Permission> applicablePermissions = getMatchingPermissions( permissions, null );

        List<Permission> permissionsForUser = getPermissionsForUser(principalId, applicablePermissions, qualification);

        //check to see if we should cache...  this is messy
        if (distributedKimCache != null) {
            List<String> roleIds = getRoleIdsForPermissions(applicablePermissions);
            // make this call and see if it gets cached...
            // it is either this, or grabbing and looping through roleTypeServices for
            boolean hasRole = roleService.principalHasRole(principalId, roleIds, qualification);
            StringBuffer roleMembercacheKey =  new StringBuffer("{principalHasRole}principalId=")
                    .append(principalId).append("|")
                    .append("roleIds=").append(CacheKeyUtils.key(roleIds)).append("|")
                    .append("qualification=").append(CacheKeyUtils.mapKey(qualification));
            if (distributedKimCache.getCache(RoleMember.Cache.NAME).get(roleMembercacheKey) != null) {
                distributedKimCache.getCache(Permission.Cache.NAME).put(cacheKey, permissionsForUser);
            }
        }

    	return permissionsForUser;
    }
    @Override
    public List<Permission> getAuthorizedPermissionsByTemplate(String principalId, String namespaceCode,
            String permissionTemplateName, Map<String, String> permissionDetails, Map<String, String> qualification) throws RiceIllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionTemplateName, "permissionTemplateName");
        incomingParamCheck(qualification, "qualification");

        DistributedCacheManagerDecorator distributedKimCache = getKimDistributedCacheManager();

        StringBuffer cacheKey =  new StringBuffer("{getAuthorizedPermissionsByTemplate}principalId=").append(principalId).append("|")
                .append("namespaceCode=").append(namespaceCode).append("|")
                .append("permissionTemplateName=").append(permissionTemplateName).append("|")
                .append("permissionDetails=").append(CacheKeyUtils.mapKey(permissionDetails)).append("|")
                .append("qualification=").append(CacheKeyUtils.mapKey(qualification));
        if (distributedKimCache != null) {
            Cache.ValueWrapper cachedValue = distributedKimCache.getCache(Permission.Cache.NAME).get(cacheKey);
            if (cachedValue != null && cachedValue.get() instanceof List) {
                return ((List<Permission>)cachedValue.get());
            }
        }
        // get all the permission objects whose name match that requested
    	List<PermissionBo> permissions = getPermissionImplsByTemplateName( namespaceCode, permissionTemplateName );
    	// now, filter the full list by the detail passed
    	List<Permission> applicablePermissions = getMatchingPermissions( permissions, permissionDetails );

        List<Permission> permissionsForUser = getPermissionsForUser(principalId, applicablePermissions, qualification);

        //check to see if we should cache...  this is messy
        if (distributedKimCache != null) {
            List<String> roleIds = getRoleIdsForPermissions(applicablePermissions);
            // make this call and see if it gets cached...
            // it is either this, or grabbing and looping through roleTypeServices for
            boolean hasRole = roleService.principalHasRole(principalId, roleIds, qualification);
            StringBuffer roleMembercacheKey =  new StringBuffer("{principalHasRole}principalId=")
                    .append(principalId).append("|")
                    .append("roleIds=").append(CacheKeyUtils.key(roleIds)).append("|")
                    .append("qualification=").append(CacheKeyUtils.mapKey(qualification));
            if (distributedKimCache.getCache(RoleMember.Cache.NAME).get(roleMembercacheKey) != null) {
                distributedKimCache.getCache(Permission.Cache.NAME).put(cacheKey, permissionsForUser);
            }
        }

        return permissionsForUser;
    }
    
    /**
     * Checks the list of permissions against the principal's roles and returns a subset of the list which match.
     */
    protected List<Permission> getPermissionsForUser( String principalId, List<Permission> permissions,
            Map<String, String> qualification ) {


    	ArrayList<Permission> results = new ArrayList<Permission>();
    	List<Permission> tempList = new ArrayList<Permission>(1);
        List<String> allRoleIds = new ArrayList<String>();
    	for ( Permission perm : permissions ) {
    		tempList.clear();
    		tempList.add( perm );
    		List<String> roleIds = getRoleIdsForPermissions( tempList );
            allRoleIds.addAll(roleIds);
    		// TODO: This could be made a little better by collecting the role IDs into
    		// a set and then processing the distinct list rather than a check
    		// for every permission
    		if ( roleIds != null && !roleIds.isEmpty() ) {
    			if ( roleService.principalHasRole( principalId, roleIds, qualification) ) {
    				results.add( perm );
    			}
    		}
    	}
    	return Collections.unmodifiableList(results);
    }

    protected Map<String,PermissionTypeService> getPermissionTypeServicesByTemplateId( Collection<PermissionBo> permissions ) {
    	Map<String,PermissionTypeService> permissionTypeServices = new HashMap<String, PermissionTypeService>( permissions.size() );
    	for ( PermissionBo perm : permissions ) {
    		permissionTypeServices.put(perm.getTemplateId(), getPermissionTypeService( perm.getTemplate() ) );    				
    	}
    	return permissionTypeServices;
    }
    
    @SuppressWarnings("static-access")
	protected Map<String,List<Permission>> groupPermissionsByTemplate( Collection<PermissionBo> permissions ) {
    	Map<String,List<Permission>> results = new HashMap<String,List<Permission>>();
    	for ( PermissionBo perm : permissions ) {
    		List<Permission> perms = results.get( perm.getTemplateId() );
    		if ( perms == null ) {
    			perms = new ArrayList<Permission>();
    			results.put( perm.getTemplateId(), perms );
    		}
    		perms.add( PermissionBo.to(perm) );
    	}
    	return results;
    }
    
	/**
     * Compare each of the passed in permissions with the given permissionDetails.  Those that
     * match are added to the result list.
     */
    protected List<Permission> getMatchingPermissions( List<PermissionBo> permissions, Map<String, String> permissionDetails ) {
    	List<Permission> applicablePermissions = new ArrayList<Permission>();    	
    	if ( permissionDetails == null || permissionDetails.isEmpty() ) {
    		// if no details passed, assume that all match
    		for ( PermissionBo perm : permissions ) {
    			applicablePermissions.add( PermissionBo.to(perm) );
    		}
    	} else {
    		// otherwise, attempt to match the permission details
    		// build a map of the template IDs to the type services
    		Map<String,PermissionTypeService> permissionTypeServices = getPermissionTypeServicesByTemplateId( permissions );
    		// build a map of permissions by template ID
    		Map<String,List<Permission>> permissionMap = groupPermissionsByTemplate( permissions );
    		// loop over the different templates, matching all of the same template against the type
    		// service at once
    		for ( Map.Entry<String,List<Permission>> entry : permissionMap.entrySet() ) {
    			PermissionTypeService permissionTypeService = permissionTypeServices.get( entry.getKey() );
    			List<Permission> permissionList = entry.getValue();
				applicablePermissions.addAll( permissionTypeService.getMatchingPermissions( permissionDetails, permissionList ) );    				
    		}
    	}
    	return applicablePermissions;
    }


    @Override
    public List<Assignee> getPermissionAssignees( String namespaceCode, String permissionName,
            Map<String, String> qualification ) throws RiceIllegalArgumentException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionName, "permissionName");
        incomingParamCheck(qualification, "qualification");

        DistributedCacheManagerDecorator distributedKimCache = getKimDistributedCacheManager();

        StringBuffer cacheKey =  new StringBuffer("{getPermissionAssignees}")
                .append("namespaceCode=").append(namespaceCode).append("|")
                .append("permissionName=").append(permissionName).append("|")
                .append("qualification=").append(CacheKeyUtils.mapKey(qualification));
        if (distributedKimCache != null) {
            Cache.ValueWrapper cachedValue = distributedKimCache.getCache(Permission.Cache.NAME).get(cacheKey);
            if (cachedValue != null && cachedValue.get() instanceof List) {
                return ((List<Assignee>)cachedValue.get());
            }
        }
    	List<String> roleIds = getRoleIdsForPermission( namespaceCode, permissionName);
    	if ( roleIds.isEmpty() ) {
    		return Collections.emptyList();
    	}
    	Collection<RoleMembership> roleMembers = roleService.getRoleMembers( roleIds,qualification );
    	List<Assignee> results = new ArrayList<Assignee>();
        for ( RoleMembership rm : roleMembers ) {
			List<DelegateType.Builder> delegateBuilderList = new ArrayList<DelegateType.Builder>();
			if (!rm.getDelegates().isEmpty()) {
    			for (DelegateType delegate : rm.getDelegates()){
                    delegateBuilderList.add(DelegateType.Builder.create(delegate));
    			}
			}
    		if ( MemberType.PRINCIPAL.equals(rm.getType()) ) {
    			results.add (Assignee.Builder.create(rm.getMemberId(), null, delegateBuilderList).build());
    		} else if ( MemberType.GROUP.equals(rm.getType()) ) {
    			results.add (Assignee.Builder.create(null, rm.getMemberId(), delegateBuilderList).build());
    		}
    	}

        //check RoleMember cache.  if cache value exists for the method we just called, no derived roles were used.
        if (distributedKimCache != null) {
            StringBuffer roleMemberCacheKey =  new StringBuffer("{getRoleMembers}")
                    .append("roleIds=").append(CacheKeyUtils.key(roleIds)).append("|")
                    .append("qualification=").append(CacheKeyUtils.mapKey(qualification));
            if (distributedKimCache.getCache(RoleMember.Cache.NAME).get(roleMemberCacheKey) != null) {
                distributedKimCache.getCache(Permission.Cache.NAME).put(cacheKey, Collections.unmodifiableList(results));
            }
        }
    	return Collections.unmodifiableList(results);
    }

    @Override
    public List<Assignee> getPermissionAssigneesByTemplate(String namespaceCode, String permissionTemplateName,
            Map<String, String> permissionDetails, Map<String, String> qualification) throws RiceIllegalArgumentException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionTemplateName, "permissionTemplateName");
        incomingParamCheck(qualification, "qualification");

        DistributedCacheManagerDecorator distributedKimCache = getKimDistributedCacheManager();
        StringBuffer cacheKey =  new StringBuffer("{getPermissionAssigneesByTemplate}")
                .append("namespaceCode=").append(namespaceCode).append("|")
                .append("permissionTemplateName=").append(permissionTemplateName).append("|")
                .append("permissionDetails=").append(CacheKeyUtils.mapKey(permissionDetails)).append("|")
                .append("qualification=").append(CacheKeyUtils.mapKey(qualification));
        if (distributedKimCache != null) {
            Cache.ValueWrapper cachedValue = distributedKimCache.getCache(Permission.Cache.NAME).get(cacheKey);
            if (cachedValue != null && cachedValue.get() instanceof List) {
                return ((List<Assignee>)cachedValue.get());
            }
        }

    	List<String> roleIds = getRoleIdsForPermissionTemplate( namespaceCode, permissionTemplateName, permissionDetails);
    	if ( roleIds.isEmpty() ) {
    		return Collections.emptyList();
    	}
    	Collection<RoleMembership> roleMembers = roleService.getRoleMembers( roleIds,qualification);
    	List<Assignee> results = new ArrayList<Assignee>();
        for ( RoleMembership rm : roleMembers ) {
			List<DelegateType.Builder> delegateBuilderList = new ArrayList<DelegateType.Builder>();
			if (!rm.getDelegates().isEmpty()) {
    			for (DelegateType delegate : rm.getDelegates()){
                    delegateBuilderList.add(DelegateType.Builder.create(delegate));
    			}
			}
    		if ( MemberType.PRINCIPAL.equals(rm.getType()) ) {
    			results.add (Assignee.Builder.create(rm.getMemberId(), null, delegateBuilderList).build());
    		} else { // a group membership
    			results.add (Assignee.Builder.create(null, rm.getMemberId(), delegateBuilderList).build());
    		}
    	}
        //check RoleMember cache.  if cache value exists for the method we just called, no derived roles were used.
        if (distributedKimCache != null) {
            StringBuffer roleMemberCacheKey =  new StringBuffer("{getRoleMembers}")
                .append("roleIds=").append(CacheKeyUtils.key(roleIds)).append("|")
                .append("qualification=").append(CacheKeyUtils.mapKey(qualification));
            if (distributedKimCache.getCache(RoleMember.Cache.NAME).get(roleMemberCacheKey) != null) {
                distributedKimCache.getCache(Permission.Cache.NAME).put(cacheKey, Collections.unmodifiableList(results));
            }
        }
    	return Collections.unmodifiableList(results);
    }

    @Override
    public boolean isPermissionDefined( String namespaceCode, String permissionName ) throws RiceIllegalArgumentException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionName, "permissionName");

    	// get all the permission objects whose name match that requested
    	List<PermissionBo> permissions = getPermissionImplsByName( namespaceCode, permissionName );
    	// now, filter the full list by the detail passed
    	return !getMatchingPermissions( permissions, null ).isEmpty();
    }

    @Override
    public boolean isPermissionDefinedByTemplate(String namespaceCode, String permissionTemplateName,
            Map<String, String> permissionDetails) throws RiceIllegalArgumentException {

        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionTemplateName, "permissionTemplateName");

    	// get all the permission objects whose name match that requested
    	List<PermissionBo> permissions = getPermissionImplsByTemplateName( namespaceCode, permissionTemplateName );
    	// now, filter the full list by the detail passed
    	return !getMatchingPermissions( permissions, permissionDetails ).isEmpty();
    }

    @Override
    public List<String> getRoleIdsForPermission(String namespaceCode, String permissionName) throws RiceIllegalArgumentException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionName, "permissionName");

        // get all the permission objects whose name match that requested
        List<PermissionBo> permissions = getPermissionImplsByName(namespaceCode, permissionName);
        // now, filter the full list by the detail passed
        List<Permission> applicablePermissions = getMatchingPermissions(permissions, null);
        return getRoleIdsForPermissions(applicablePermissions);
    }

    protected List<String> getRoleIdsForPermissionTemplate( String namespaceCode, String permissionTemplateName, Map<String, String> permissionDetails ) {
    	// get all the permission objects whose name match that requested
    	List<PermissionBo> permissions = getPermissionImplsByTemplateName( namespaceCode, permissionTemplateName );
    	// now, filter the full list by the detail passed
    	List<Permission> applicablePermissions = getMatchingPermissions( permissions, permissionDetails );
    	return getRoleIdsForPermissions( applicablePermissions );
    }

    // --------------------
    // Permission Data
    // --------------------
    
    @Override
    public Permission getPermission(String permissionId) throws RiceIllegalArgumentException {
        incomingParamCheck(permissionId, "permissionId");

        PermissionBo impl = getPermissionImpl( permissionId );
    	if ( impl != null ) {
    		return PermissionBo.to(impl);
    	}
    	return null;
    }
    
    @Override
    public List<Permission> findPermissionsByTemplate(String namespaceCode, String permissionTemplateName) throws RiceIllegalArgumentException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionTemplateName, "permissionTemplateName");

        List<PermissionBo> impls = getPermissionImplsByTemplateName( namespaceCode, permissionTemplateName );
    	List<Permission> results = new ArrayList<Permission>(impls.size());
    	for (PermissionBo impl : impls) {
    	    results.add(PermissionBo.to(impl));
    	}
    	return Collections.unmodifiableList(results);
    }

	protected PermissionBo getPermissionImpl(String permissionId) throws RiceIllegalArgumentException {
    	incomingParamCheck(permissionId, "permissionId");

        HashMap<String,Object> pk = new HashMap<String,Object>( 1 );
        pk.put( KimConstants.PrimaryKeyConstants.PERMISSION_ID, permissionId );
        return businessObjectService.findByPrimaryKey( PermissionBo.class, pk );
    }
    
    protected List<PermissionBo> getPermissionImplsByTemplateName( String namespaceCode, String permissionTemplateName ){
        HashMap<String,Object> pk = new HashMap<String,Object>( 3 );
        pk.put( "template.namespaceCode", namespaceCode );
        pk.put( "template.name", permissionTemplateName );
        pk.put( KRADPropertyConstants.ACTIVE, "Y" );
        return ((List<PermissionBo>) businessObjectService.findMatching( PermissionBo.class, pk ));
    }

    protected List<PermissionBo> getPermissionImplsByName( String namespaceCode, String permissionName ) {
        HashMap<String,Object> pk = new HashMap<String,Object>( 3 );
        pk.put( KimConstants.UniqueKeyConstants.NAMESPACE_CODE, namespaceCode );
        pk.put( KimConstants.UniqueKeyConstants.PERMISSION_NAME, permissionName );
        pk.put( KRADPropertyConstants.ACTIVE, "Y" );
        
        return ((List<PermissionBo>) businessObjectService.findMatching( PermissionBo.class, pk ));
    }
	
    @Override
	public Template getPermissionTemplate(String permissionTemplateId) throws RiceIllegalArgumentException {
        incomingParamCheck(permissionTemplateId, "permissionTemplateId");

        PermissionTemplateBo impl = businessObjectService.findBySinglePrimaryKey( PermissionTemplateBo.class, permissionTemplateId );
		if ( impl != null ) {
			return PermissionTemplateBo.to(impl);
		}
		return null;
	}

    @Override
	public Template findPermTemplateByNamespaceCodeAndName(String namespaceCode,
            String permissionTemplateName) throws RiceIllegalArgumentException {
		incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionTemplateName, "permissionTemplateName");

        Map<String,String> criteria = new HashMap<String,String>(2);
		criteria.put( KimConstants.UniqueKeyConstants.NAMESPACE_CODE, namespaceCode );
		criteria.put( KimConstants.UniqueKeyConstants.PERMISSION_TEMPLATE_NAME, permissionTemplateName );
		PermissionTemplateBo impl = businessObjectService.findByPrimaryKey( PermissionTemplateBo.class, criteria );
		if ( impl != null ) {
			return PermissionTemplateBo.to(impl);
		}
		return null;
	}

    @Override
	public List<Template> getAllTemplates() {
		if ( allTemplates.isEmpty() ) {
			Map<String,String> criteria = new HashMap<String,String>(1);
			criteria.put( KRADPropertyConstants.ACTIVE, "Y" );
			List<PermissionTemplateBo> impls = (List<PermissionTemplateBo>) businessObjectService.findMatching( PermissionTemplateBo.class, criteria );
			List<Template> infos = new ArrayList<Template>( impls.size() );
			for ( PermissionTemplateBo impl : impls ) {
				infos.add( PermissionTemplateBo.to(impl) );
			}
			Collections.sort(infos, new Comparator<Template>() {
				@Override public int compare(Template tmpl1,
						Template tmpl2) {

					int result = tmpl1.getNamespaceCode().compareTo(tmpl2.getNamespaceCode());
					if ( result != 0 ) {
						return result;
					}
					result = tmpl1.getName().compareTo(tmpl2.getName());
					return result;
				}
			});
			allTemplates.addAll(infos);
		}
		return Collections.unmodifiableList(allTemplates);
    }


	@Override
	public Permission createPermission(Permission permission)
			throws RiceIllegalArgumentException, RiceIllegalStateException {
        incomingParamCheck(permission, "permission");

        if (StringUtils.isNotBlank(permission.getId()) && getPermission(permission.getId()) != null) {
            throw new RiceIllegalStateException("the permission to create already exists: " + permission);
        }
        List<PermissionAttributeBo> attrBos = Collections.emptyList();
        if (permission.getTemplate() != null) {
            attrBos = KimAttributeDataBo.createFrom(PermissionAttributeBo.class, permission.getAttributes(), permission.getTemplate().getKimTypeId());
        }
        PermissionBo bo = PermissionBo.from(permission);
        if (bo.getTemplate() == null && bo.getTemplateId() != null) {
            bo.setTemplate(PermissionTemplateBo.from(getPermissionTemplate(bo.getTemplateId())));
        }
        bo.setAttributeDetails(attrBos);
        return PermissionBo.to(businessObjectService.save(bo));
	}

	@Override
	public Permission updatePermission(Permission permission)
			throws RiceIllegalArgumentException, RiceIllegalStateException {
        incomingParamCheck(permission, "permission");

        PermissionBo oldPermission = getPermissionImpl(permission.getId());
        if (StringUtils.isBlank(permission.getId()) || oldPermission == null) {
            throw new RiceIllegalStateException("the permission does not exist: " + permission);
        }

        //List<PermissionAttributeBo> attrBos = KimAttributeDataBo.createFrom(PermissionAttributeBo.class, permission.getAttributes(), permission.getTemplate().getKimTypeId());

        List<PermissionAttributeBo> oldAttrBos = oldPermission.getAttributeDetails();
        //put old attributes in map for easier updating
        Map<String, PermissionAttributeBo> oldAttrMap = new HashMap<String, PermissionAttributeBo>();
        for (PermissionAttributeBo oldAttr : oldAttrBos) {
            oldAttrMap.put(oldAttr.getKimAttribute().getAttributeName(), oldAttr);
        }
        List<PermissionAttributeBo> newAttrBos = new ArrayList<PermissionAttributeBo>();
        for (String key : permission.getAttributes().keySet()) {
            if (oldAttrMap.containsKey(key)) {
                PermissionAttributeBo updatedAttr = oldAttrMap.get(key);
                updatedAttr.setAttributeValue(permission.getAttributes().get(key));
                newAttrBos.add(updatedAttr);
            } else { //new attribute
                newAttrBos.addAll(KimAttributeDataBo.createFrom(PermissionAttributeBo.class, Collections.singletonMap(key, permission.getAttributes().get(key)), permission.getTemplate().getKimTypeId()));
            }
        }
        PermissionBo bo = PermissionBo.from(permission);
        if (CollectionUtils.isNotEmpty(newAttrBos)) {
            if(null!= bo.getAttributeDetails())  {
                bo.getAttributeDetails().clear();
            }
            bo.setAttributeDetails(newAttrBos);
        }
        if (bo.getTemplate() == null && bo.getTemplateId() != null) {
            bo.setTemplate(PermissionTemplateBo.from(getPermissionTemplate(bo.getTemplateId())));
        }

        return PermissionBo.to(businessObjectService.save(bo));		
	}
	
    @Override
    public Permission findPermByNamespaceCodeAndName(String namespaceCode, String permissionName)
            throws RiceIllegalArgumentException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionName, "permissionName");

        PermissionBo permissionBo = getPermissionBoByName(namespaceCode, permissionName);
        if (permissionBo != null) {
            return PermissionBo.to(permissionBo);
        }
        return null;
    }
    
    protected PermissionBo getPermissionBoByName(String namespaceCode, String permissionName) {
        if (StringUtils.isBlank(namespaceCode)
                || StringUtils.isBlank(permissionName)) {
            return null;
        }
        Map<String, String> criteria = new HashMap<String, String>();
        criteria.put(KimConstants.UniqueKeyConstants.NAMESPACE_CODE, namespaceCode);
        criteria.put(KimConstants.UniqueKeyConstants.NAME, permissionName);
        criteria.put(KRADPropertyConstants.ACTIVE, "Y");
        // while this is not actually the primary key - there will be at most one row with these criteria
        return businessObjectService.findByPrimaryKey(PermissionBo.class, criteria);
    }

    @Override
    public PermissionQueryResults findPermissions(final QueryByCriteria queryByCriteria)
            throws RiceIllegalArgumentException {
        incomingParamCheck(queryByCriteria, "queryByCriteria");

        LookupCustomizer.Builder<PermissionBo> lc = LookupCustomizer.Builder.create();
        lc.setPredicateTransform(AttributeTransform.getInstance());

        GenericQueryResults<PermissionBo> results = criteriaLookupService.lookup(PermissionBo.class, queryByCriteria, lc.build());

        PermissionQueryResults.Builder builder = PermissionQueryResults.Builder.create();
        builder.setMoreResultsAvailable(results.isMoreResultsAvailable());
        builder.setTotalRowCount(results.getTotalRowCount());

        final List<Permission.Builder> ims = new ArrayList<Permission.Builder>();
        for (PermissionBo bo : results.getResults()) {
            ims.add(Permission.Builder.create(bo));
        }

        builder.setResults(ims);
        return builder.build();
    }

    @Override
    public TemplateQueryResults findPermissionTemplates(final QueryByCriteria queryByCriteria)
            throws RiceIllegalArgumentException {
        incomingParamCheck(queryByCriteria, "queryByCriteria");

        GenericQueryResults<PermissionTemplateBo> results = criteriaLookupService.lookup(PermissionTemplateBo.class, queryByCriteria);

        TemplateQueryResults.Builder builder = TemplateQueryResults.Builder.create();
        builder.setMoreResultsAvailable(results.isMoreResultsAvailable());
        builder.setTotalRowCount(results.getTotalRowCount());

        final List<Template.Builder> ims = new ArrayList<Template.Builder>();
        for (PermissionTemplateBo bo : results.getResults()) {
            ims.add(Template.Builder.create(bo));
        }

        builder.setResults(ims);
        return builder.build();
    }

    private List<String> getRoleIdsForPermissions( Collection<Permission> permissions ) {
        if (CollectionUtils.isEmpty(permissions)) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<String>();
        for (Permission p : permissions) {
            ids.add(p.getId());
        }

        return getRoleIdsForPermissionIds(ids);
    }

    private List<String> getRoleIdsForPermissionIds( Collection<String> permissionIds ) {
        if (CollectionUtils.isEmpty(permissionIds)) {
            return Collections.emptyList();
        }

        QueryByCriteria query = QueryByCriteria.Builder.fromPredicates(equal("active", "true"), in("permissionId", permissionIds.toArray(new String[]{})));

        GenericQueryResults<RolePermissionBo> results = criteriaLookupService.lookup(RolePermissionBo.class, query);
        List<String> roleIds = new ArrayList<String>();
        for (RolePermissionBo bo : results.getResults()) {
            roleIds.add(bo.getRoleId());
        }
        return Collections.unmodifiableList(roleIds);
    }
	
	/**
     * Sets the kimTypeInfoService attribute value.
     *
     * @param kimTypeInfoService The kimTypeInfoService to set.
     */
	public void setKimTypeInfoService(KimTypeInfoService kimTypeInfoService) {
		this.kimTypeInfoService = kimTypeInfoService;
	}
	
	/**
     * Sets the defaultPermissionTypeService attribute value.
     *
     * @param defaultPermissionTypeService The defaultPermissionTypeService to set.
     */
	public void setDefaultPermissionTypeService(PermissionTypeService defaultPermissionTypeService) {
    	this.defaultPermissionTypeService = defaultPermissionTypeService;
	}
	
	/**
     * Sets the roleService attribute value.
     *
     * @param roleService The roleService to set.
     */
	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}

    /**
     * Sets the businessObjectService attribute value.
     *
     * @param businessObjectService The businessObjectService to set.
     */
    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * Sets the criteriaLookupService attribute value.
     *
     * @param criteriaLookupService The criteriaLookupService to set.
     */
    public void setCriteriaLookupService(final CriteriaLookupService criteriaLookupService) {
        this.criteriaLookupService = criteriaLookupService;
    }
    
    protected void logAuthorizationCheck(String checkType, String principalId, String namespaceCode, String permissionName, Map<String, String> qualification ) {
        StringBuilder sb = new StringBuilder();
        sb.append(  '\n' );
        sb.append( "Is AuthZ for " ).append( checkType ).append( ": " ).append( namespaceCode ).append( "/" ).append( permissionName ).append( '\n' );
        sb.append( "             Principal:  " ).append( principalId );
        if ( principalId != null ) {
            Principal principal = KimApiServiceLocator.getIdentityService().getPrincipal(principalId);
            if ( principal != null ) {
                sb.append( " (" ).append( principal.getPrincipalName() ).append( ')' );
            }
        }
        sb.append( '\n' );
        sb.append( "             Qualifiers:\n" );
        if ( qualification != null && !qualification.isEmpty() ) {
            sb.append( qualification );
        } else {
            sb.append( "                         [null]\n" );
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace( sb.append(ExceptionUtils.getStackTrace(new Throwable())));
        } else {
            LOG.debug(sb.toString());
        }
    }
    
    protected void logAuthorizationCheckByTemplate(String checkType, String principalId, String namespaceCode, String permissionName, 
                                                   Map<String, String> permissionDetails, Map<String, String> qualification ) {
        StringBuilder sb = new StringBuilder();
        sb.append(  '\n' );
        sb.append( "Is AuthZ for " ).append( checkType ).append( ": " ).append( namespaceCode ).append( "/" ).append( permissionName ).append( '\n' );
        sb.append( "             Principal:  " ).append( principalId );
        if ( principalId != null ) {
            Principal principal = KimApiServiceLocator.getIdentityService().getPrincipal(principalId);
            if ( principal != null ) {
                sb.append( " (" ).append( principal.getPrincipalName() ).append( ')' );
            }
        }
        sb.append( '\n' );
        sb.append( "             Details:\n" );
        if ( permissionDetails != null ) {
            sb.append( permissionDetails );
        } else {
            sb.append( "                         [null]\n" );
        }
        sb.append( "             Qualifiers:\n" );
        if ( qualification != null && !qualification.isEmpty() ) {
            sb.append( qualification );
        } else {
            sb.append( "                         [null]\n" );
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

    private DistributedCacheManagerDecorator getKimDistributedCacheManager() {
        try {
            return GlobalResourceLoader.getService(KimApiConstants.Cache.KIM_DISTRIBUTED_CACHE_MANAGER);
        } catch (Exception e) {
            //caching service not found for some reason.  We can go on without caching
            LOG.warn(KimApiConstants.Cache.KIM_DISTRIBUTED_CACHE_MANAGER + " was not able to be loaded.  Method will not be cached.");
            return null;
        }
    }


}
