package org.kuali.rice.kim.impl.identity;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.criteria.Predicate;
import org.kuali.rice.core.api.criteria.PredicateUtils;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.group.Group;
import org.kuali.rice.kim.api.group.GroupQueryResults;
import org.kuali.rice.kim.api.group.GroupService;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.impl.KIMPropertyConstants;
import org.kuali.rice.kim.impl.group.GroupBo;
import org.kuali.rice.krad.lookup.LookupableImpl;
import org.kuali.rice.krad.web.form.LookupForm;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.kuali.rice.core.api.criteria.PredicateFactory.*;

/**
 * Custom lookupable for the {@link PersonImpl} lookup to call the person service for searching
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class PersonLookupableImpl extends LookupableImpl {
    private static final long serialVersionUID = -3149952849854425077L;

    /**
     * Lower cases criteria on principal name and calls the person service to carry out the search
     *
     * @return List<PersonImpl>
     */
    @Override
    protected List<?> getSearchResults(LookupForm form, Map<String, String> searchCriteria, boolean unbounded) {
        // lower case principal name
        if (searchCriteria != null && StringUtils.isNotEmpty(searchCriteria.get(
                KIMPropertyConstants.Person.PRINCIPAL_NAME))) {
            searchCriteria.put(KIMPropertyConstants.Person.PRINCIPAL_NAME, searchCriteria.get(
                    KIMPropertyConstants.Person.PRINCIPAL_NAME).toLowerCase());
        }

        return getPersonService().findPeople(searchCriteria, unbounded);
    }

    public PersonService getPersonService() {
        return KimApiServiceLocator.getPersonService();
    }
}
