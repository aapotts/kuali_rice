/**
 * Copyright 2005-2011 The Kuali Foundation
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
package org.kuali.rice.krms.test;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.kuali.rice.kew.util.PerformanceLogger;
import org.kuali.rice.krms.api.KrmsApiServiceLocator;
import org.kuali.rice.krms.api.engine.EngineResults;
import org.kuali.rice.krms.api.engine.ExecutionFlag;
import org.kuali.rice.krms.api.engine.ExecutionOptions;
import org.kuali.rice.krms.api.engine.Facts;
import org.kuali.rice.krms.api.engine.ResultEvent;
import org.kuali.rice.krms.api.engine.SelectionCriteria;
import org.kuali.rice.krms.api.repository.LogicalOperator;
import org.kuali.rice.krms.api.repository.action.ActionDefinition;
import org.kuali.rice.krms.api.repository.agenda.AgendaDefinition;
import org.kuali.rice.krms.api.repository.agenda.AgendaItemDefinition;
import org.kuali.rice.krms.api.repository.context.ContextDefinition;
import org.kuali.rice.krms.api.repository.function.FunctionDefinition;
import org.kuali.rice.krms.api.repository.function.FunctionParameterDefinition;
import org.kuali.rice.krms.api.repository.proposition.PropositionDefinition;
import org.kuali.rice.krms.api.repository.proposition.PropositionParameter;
import org.kuali.rice.krms.api.repository.proposition.PropositionParameterType;
import org.kuali.rice.krms.api.repository.proposition.PropositionType;
import org.kuali.rice.krms.api.repository.rule.RuleDefinition;
import org.kuali.rice.krms.api.repository.term.TermDefinition;
import org.kuali.rice.krms.api.repository.term.TermParameterDefinition;
import org.kuali.rice.krms.api.repository.term.TermResolverDefinition;
import org.kuali.rice.krms.api.repository.term.TermSpecificationDefinition;
import org.kuali.rice.krms.api.repository.type.KrmsTypeDefinition;
import org.kuali.rice.krms.impl.repository.ActionBoService;
import org.kuali.rice.krms.impl.repository.AgendaBoService;
import org.kuali.rice.krms.impl.repository.FunctionBoServiceImpl;
import org.kuali.rice.krms.impl.repository.KrmsRepositoryServiceLocator;
import org.kuali.rice.krms.impl.repository.RuleBoService;
import org.kuali.rice.krms.impl.repository.TermBo;
import org.kuali.rice.krms.impl.repository.TermBoService;
import org.kuali.rice.test.BaselineTestCase.BaselineMode;
import org.kuali.rice.test.BaselineTestCase.Mode;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@BaselineMode(Mode.CLEAR_DB)
public class RepositoryCreateAndExecuteIntegrationTest extends AbstractBoTest {

    public static final String CAMPUS_CODE_TERM_NAME = "campusCodeTermSpec";

    static final String NAMESPACE1 = "KRMS_TEST_1";
    static final String NAMESPACE2 = "KRMS_TEST_2";
    static final String TSUNAMI_EVENT = "Tsunami";
    static final String EARTHQUAKE_EVENT = "Earthquake";
    static final String CONTEXT1 = "Context1";
    static final String CONTEXT2 = "Context2";
    static final String NAME = "name";
    static final String CONTEXT1_QUALIFIER = "Context1Qualifier";
    static final String CONTEXT1_QUALIFIER_VALUE = "BLAH1";
    static final String CONTEXT2_QUALIFIER = "Context2Qualifier";
    static final String CONTEXT2_QUALIFIER_VALUE = "BLAH2";
    static final String AGENDA1 = "TestAgenda1";
    static final String AGENDA2 = "Agenda2";
    static final String AGENDA3 = "Agenda3";
    static final String AGENDA4 = "Agenda4";
    static final String PREREQ_TERM_NAME = "prereqTermSpec";
    static final String PREREQ_TERM_VALUE = "prereqValue";
    static final String NAMESPACE_CODE = "namespaceCode";
    static final String BOOL1 = "bool1";
    static final String BOOL2 = "bool2";

//    // Services needed for creation:
	private TermBoService termBoService;

    private RuleBoService ruleBoService;
    private AgendaBoService agendaBoService;
    private ActionBoService actionBoService;
    private FunctionBoServiceImpl functionBoService;

    /**
     *
     * Setting it up so that KRMS tables are not reset between test methods to make it run much faster
     *
     * @return
     */
    protected List<String> getPerTestTablesNotToClear() {
        List<String> tablesNotToClear = super.getPerTestTablesNotToClear();

        tablesNotToClear.add("KRMS_.*");

        return tablesNotToClear;
    }



	@Before
    public void setup() {
        // Reset TestActionTypeService
        TestActionTypeService.resetActionsFired();

        termBoService = KrmsRepositoryServiceLocator.getTermBoService();
        contextRepository = KrmsRepositoryServiceLocator.getContextBoService();
        krmsTypeRepository = KrmsRepositoryServiceLocator.getKrmsTypeRepositoryService();

        ruleBoService = KrmsRepositoryServiceLocator.getRuleBoService();
        agendaBoService = KrmsRepositoryServiceLocator.getAgendaBoService();
        actionBoService = KrmsRepositoryServiceLocator.getBean("actionBoService");
        functionBoService = KrmsRepositoryServiceLocator.getBean("functionRepositoryService");
        krmsAttributeDefinitionService = KrmsRepositoryServiceLocator.getKrmsAttributeDefinitionService();

        ContextDefinition contextDefintion1 = contextRepository.getContextByNameAndNamespace(CONTEXT1, NAMESPACE1);

        // only set this stuff up if we don't already have Context1 (we don't clear out KRMS tables between test methods)
        if (contextDefintion1 == null) {
            PerformanceLogger perfLog = new PerformanceLogger();
            perfLog.log("starting agenda creation");

            contextDefintion1 = createContextDefinition(NAMESPACE1, CONTEXT1, Collections.singletonMap(
                    CONTEXT1_QUALIFIER, CONTEXT1_QUALIFIER_VALUE));
            createAgendaDefinition(AGENDA1, contextDefintion1, TSUNAMI_EVENT, NAMESPACE1);

            ContextDefinition contextDefinition2 = createContextDefinition(NAMESPACE2, CONTEXT2, Collections.singletonMap(
                    CONTEXT2_QUALIFIER, CONTEXT2_QUALIFIER_VALUE));

            // Create multiple agendas so that we can test selection
            createAgendaDefinition(AGENDA2, contextDefinition2, EARTHQUAKE_EVENT, NAMESPACE2);
            createAgendaDefinition(AGENDA3, contextDefinition2, EARTHQUAKE_EVENT, NAMESPACE2);
            createAgendaDefinition(AGENDA4, contextDefinition2, TSUNAMI_EVENT, NAMESPACE2);

            perfLog.log("finished agenda creation", true);
        }
    }


    @Transactional
    @Test
    public void testSelectAgendaByAttributeAndName() {

        Map<String,String> contextQualifiers = new HashMap<String,String>();
        contextQualifiers.put(NAMESPACE_CODE, NAMESPACE1);
        contextQualifiers.put(NAME, CONTEXT1);
        contextQualifiers.put(CONTEXT1_QUALIFIER, CONTEXT1_QUALIFIER_VALUE);

        Map<String,String> agendaQualifiers = new HashMap<String,String>();
        agendaQualifiers.put(AgendaDefinition.Constants.EVENT, TSUNAMI_EVENT);
        agendaQualifiers.put(NAME, AGENDA1);

        DateTime now = new DateTime();

        SelectionCriteria sc1 = SelectionCriteria.createCriteria(now, contextQualifiers,
                Collections.singletonMap(AgendaDefinition.Constants.EVENT, TSUNAMI_EVENT));

        Facts.Builder factsBuilder1 = Facts.Builder.create();
        factsBuilder1.addFact(CAMPUS_CODE_TERM_NAME, "BL");
        factsBuilder1.addFact(BOOL1, "true");
        factsBuilder1.addFact(BOOL2, Boolean.TRUE);
        factsBuilder1.addFact(PREREQ_TERM_NAME, PREREQ_TERM_VALUE);

        ExecutionOptions xOptions1 = new ExecutionOptions();
        xOptions1.setFlag(ExecutionFlag.LOG_EXECUTION, true);

        PerformanceLogger perfLog = new PerformanceLogger();
        perfLog.log("starting rule execution");
        EngineResults eResults1 = KrmsApiServiceLocator.getEngine().execute(sc1, factsBuilder1.build(), xOptions1);
        perfLog.log("finished rule execution", true);
        List<ResultEvent> rEvents1 = eResults1.getAllResults();

        List<ResultEvent> ruleEvaluationResults1 = eResults1.getResultsOfType(ResultEvent.RULE_EVALUATED.toString());

        assertEquals("4 rules should have been evaluated", 4, ruleEvaluationResults1.size());

        assertTrue("rule 0 should have evaluated to true", ruleEvaluationResults1.get(0).getResult());
        assertFalse("rule 1 should have evaluated to false", ruleEvaluationResults1.get(1).getResult());
        assertTrue("rule 2 should have evaluated to true", ruleEvaluationResults1.get(2).getResult());

        // ONLY agenda 1 should have been selected
        assertTrue(TestActionTypeService.actionFired("TestAgenda1::Rule1::TestAction"));
        assertFalse(TestActionTypeService.actionFired("TestAgenda1::Rule2::TestAction"));
        assertTrue(TestActionTypeService.actionFired("TestAgenda1::Rule3::TestAction"));

        assertAgendaDidNotExecute(AGENDA2);
        assertAgendaDidNotExecute(AGENDA3);
        assertAgendaDidNotExecute(AGENDA4);
    }

    @Transactional
    @Test
    public void testSelectAgendaByName() {
        Map<String,String> contextQualifiers = new HashMap<String,String>();
        contextQualifiers.put(NAMESPACE_CODE, NAMESPACE2);
        contextQualifiers.put(NAME, CONTEXT2);
        contextQualifiers.put(CONTEXT2_QUALIFIER, CONTEXT2_QUALIFIER_VALUE);
        Map<String,String> agendaQualifiers = new HashMap<String,String>();

        /*
         * We'll specifically NOT select this attribute to make sure that matching only takes place against qualifiers
         * in the selection criteria
         */
        // agendaQualifiers.put(AgendaDefinition.Constants.EVENT, EARTHQUAKE_EVENT);

        agendaQualifiers.put(NAME, AGENDA3);
        DateTime now = new DateTime();

        SelectionCriteria selectionCriteria = SelectionCriteria.createCriteria(now, contextQualifiers, agendaQualifiers);

        Facts.Builder factsBuilder2 = Facts.Builder.create();
        factsBuilder2.addFact(BOOL1, "true");
        factsBuilder2.addFact(BOOL2, Boolean.TRUE);
        factsBuilder2.addFact(CAMPUS_CODE_TERM_NAME, "BL");
        factsBuilder2.addFact(PREREQ_TERM_NAME, PREREQ_TERM_VALUE);

        ExecutionOptions xOptions2 = new ExecutionOptions();
        xOptions2.setFlag(ExecutionFlag.LOG_EXECUTION, true);


        PerformanceLogger perfLog = new PerformanceLogger();
        perfLog.log("starting rule execution 1");
        EngineResults eResults1 = KrmsApiServiceLocator.getEngine().execute(selectionCriteria, factsBuilder2.build(), xOptions2);
        perfLog.log("finished rule execution 1");
        List<ResultEvent> rEvents1 = eResults1.getAllResults();

        List<ResultEvent> ruleEvaluationResults1 = eResults1.getResultsOfType(ResultEvent.RULE_EVALUATED.toString());

        selectionCriteria = SelectionCriteria.createCriteria(now, contextQualifiers, agendaQualifiers);

        assertEquals("4 rules should have been evaluated", 4, ruleEvaluationResults1.size());

        assertAgendaDidNotExecute(AGENDA1);
        assertAgendaDidNotExecute(AGENDA2);

        // ONLY agenda 3 should have been selected
        assertTrue(TestActionTypeService.actionFired("Agenda3::Rule1::TestAction"));
        assertFalse(TestActionTypeService.actionFired("Agenda3::Rule2::TestAction"));
        assertTrue(TestActionTypeService.actionFired("Agenda3::Rule3::TestAction"));

        assertAgendaDidNotExecute(AGENDA4);
    }


    @Transactional
    @Test
    public void testSelectMultipleAgendasByAttribute() {
        Map<String,String> contextQualifiers = new HashMap<String,String>();
        contextQualifiers.put(NAMESPACE_CODE, NAMESPACE2);
        contextQualifiers.put(NAME, CONTEXT2);
        contextQualifiers.put(CONTEXT2_QUALIFIER, CONTEXT2_QUALIFIER_VALUE);

        Map<String,String> agendaQualifiers = new HashMap<String,String>();
        agendaQualifiers.put(AgendaDefinition.Constants.EVENT, EARTHQUAKE_EVENT);

        DateTime now = new DateTime();

        SelectionCriteria selectionCriteria = SelectionCriteria.createCriteria(now, contextQualifiers, agendaQualifiers);

        Facts.Builder factsBuilder2 = Facts.Builder.create();
        factsBuilder2.addFact(BOOL1, "true");
        factsBuilder2.addFact(BOOL2, Boolean.TRUE);
        factsBuilder2.addFact(CAMPUS_CODE_TERM_NAME, "BL");
        factsBuilder2.addFact(PREREQ_TERM_NAME, PREREQ_TERM_VALUE);

        ExecutionOptions xOptions2 = new ExecutionOptions();
        xOptions2.setFlag(ExecutionFlag.LOG_EXECUTION, true);


        PerformanceLogger perfLog = new PerformanceLogger();
        perfLog.log("starting rule execution 1");
        EngineResults eResults1 = KrmsApiServiceLocator.getEngine().execute(selectionCriteria, factsBuilder2.build(), xOptions2);
        perfLog.log("finished rule execution 1");
        List<ResultEvent> rEvents1 = eResults1.getAllResults();

        List<ResultEvent> ruleEvaluationResults1 = eResults1.getResultsOfType(ResultEvent.RULE_EVALUATED.toString());

        selectionCriteria = SelectionCriteria.createCriteria(now, contextQualifiers, agendaQualifiers);

        assertEquals("8 rules should have been evaluated", 8, ruleEvaluationResults1.size());

        assertAgendaDidNotExecute(AGENDA1);

        // ONLY agendas 2 & 3 should have been selected

        assertTrue(TestActionTypeService.actionFired("Agenda2::Rule1::TestAction"));
        assertFalse(TestActionTypeService.actionFired("Agenda2::Rule2::TestAction"));
        assertTrue(TestActionTypeService.actionFired("Agenda2::Rule3::TestAction"));

        assertTrue(TestActionTypeService.actionFired("Agenda3::Rule1::TestAction"));
        assertFalse(TestActionTypeService.actionFired("Agenda3::Rule2::TestAction"));
        assertTrue(TestActionTypeService.actionFired("Agenda3::Rule3::TestAction"));

        assertAgendaDidNotExecute(AGENDA4);
    }

    private void assertAgendaDidNotExecute(String agendaName) {
        assertFalse(TestActionTypeService.actionFired(agendaName+"::Rule1::TestAction"));
        assertFalse(TestActionTypeService.actionFired(agendaName+"::Rule2::TestAction"));
        assertFalse(TestActionTypeService.actionFired(agendaName+"::Rule3::TestAction"));
    }

    private void createAgendaDefinition(String agendaName, ContextDefinition contextDefinition, String eventName, String nameSpace ) {
        KrmsTypeDefinition krmsGenericTypeDefinition = createKrmsGenericTypeDefinition(nameSpace, "testAgendaTypeService", "event name", "Event");

        AgendaDefinition agendaDef =
            AgendaDefinition.Builder.create(null, agendaName, krmsGenericTypeDefinition.getId(), contextDefinition.getId()).build();
        agendaDef = agendaBoService.createAgenda(agendaDef);

        AgendaItemDefinition.Builder agendaItemBuilder1 = AgendaItemDefinition.Builder.create(null, agendaDef.getId());
        agendaItemBuilder1.setRuleId(createRuleDefinition1(contextDefinition, agendaName, nameSpace).getId());

        AgendaItemDefinition.Builder agendaItemBuilder2 = AgendaItemDefinition.Builder.create(null, agendaDef.getId());
        agendaItemBuilder1.setAlways(agendaItemBuilder2);
        agendaItemBuilder2.setRuleId(createRuleDefinition2(contextDefinition, agendaName, nameSpace).getId());

        AgendaItemDefinition.Builder agendaItemBuilder3 = AgendaItemDefinition.Builder.create(null, agendaDef.getId());
        agendaItemBuilder2.setAlways(agendaItemBuilder3);
        agendaItemBuilder3.setRuleId(createRuleDefinition3(contextDefinition, agendaName, nameSpace).getId());

        AgendaItemDefinition.Builder agendaItemBuilder4 = AgendaItemDefinition.Builder.create(null, agendaDef.getId());
        agendaItemBuilder3.setAlways(agendaItemBuilder4);
        agendaItemBuilder4.setRuleId(createRuleDefinition4(contextDefinition, agendaName, nameSpace).getId());

        // String these puppies together.  Kind of a PITA because you need the id from the next item before you insert the previous one
        AgendaItemDefinition agendaItem4 = agendaBoService.createAgendaItem(agendaItemBuilder4.build());
        agendaItemBuilder3.setAlwaysId(agendaItem4.getId());
        AgendaItemDefinition agendaItem3 = agendaBoService.createAgendaItem(agendaItemBuilder3.build());
        agendaItemBuilder2.setAlwaysId(agendaItem3.getId());
        AgendaItemDefinition agendaItem2 = agendaBoService.createAgendaItem(agendaItemBuilder2.build());
        agendaItemBuilder1.setAlwaysId(agendaItem2.getId());
        AgendaItemDefinition agendaItem1 = agendaBoService.createAgendaItem(agendaItemBuilder1.build());

        AgendaDefinition.Builder agendaDefBuilder1 = AgendaDefinition.Builder.create(agendaDef);
        agendaDefBuilder1.setAttributes(Collections.singletonMap("Event", eventName));
        agendaDefBuilder1.setFirstItemId(agendaItem1.getId());
        agendaDef = agendaDefBuilder1.build();

        agendaBoService.updateAgenda(agendaDef);
    }

    private KrmsTypeDefinition createKrmsActionTypeDefinition(String nameSpace) {
        String ACTION_TYPE_NAME = "KrmsActionResolverType";
        KrmsTypeDefinition krmsActionTypeDefinition =  krmsTypeRepository.getTypeByName(nameSpace, ACTION_TYPE_NAME);

        if (krmsActionTypeDefinition == null) {
            KrmsTypeDefinition.Builder krmsActionTypeDefnBuilder = KrmsTypeDefinition.Builder.create(ACTION_TYPE_NAME, nameSpace);
            krmsActionTypeDefnBuilder.setServiceName("testActionTypeService");
            krmsActionTypeDefinition = krmsTypeRepository.createKrmsType(krmsActionTypeDefnBuilder.build());
        }

        return krmsActionTypeDefinition;
    }
    

    private RuleDefinition createRuleDefinition(String nameSpace, String ruleName, ContextDefinition contextDefinition,
            LogicalOperator operator, PropositionParametersBuilder ... pbs) {
        RuleDefinition.Builder ruleDefBuilder =
                RuleDefinition.Builder.create(null, ruleName, nameSpace, null, null);
        RuleDefinition ruleDef1 = ruleBoService.createRule(ruleDefBuilder.build());

        PropositionDefinition.Builder parentProposition =
                PropositionDefinition.Builder.create(null, PropositionType.COMPOUND.getCode(), ruleDef1.getId(), null, null);
        parentProposition.setCompoundComponents(new ArrayList<PropositionDefinition.Builder>());

        if (operator != null ) { parentProposition.setCompoundOpCode(operator.getCode()); }

        ruleDefBuilder = RuleDefinition.Builder.create(ruleDef1);

        for (PropositionParametersBuilder params : pbs) {

            StringBuilder propositionNameBuilder = new StringBuilder(ruleName);

            propositionNameBuilder.append("::");
            for (Object[] param : params.params) {
                propositionNameBuilder.append(param[0].toString());
                propositionNameBuilder.append("--");
            }

            PropositionDefinition.Builder propositionBuilder =
                    createPropositionDefinition(propositionNameBuilder.toString(), params, ruleDef1);

            if (pbs.length > 1) {
                // add it to the compound prop
                parentProposition.getCompoundComponents().add(propositionBuilder);
            } else {
                // if there is only one proposition to build, make it the parent
                parentProposition = propositionBuilder;
            }
        }

        ruleDefBuilder.setProposition(parentProposition);
        ruleDef1 = ruleDefBuilder.build();
        ruleBoService.updateRule(ruleDef1);

        // Action
        ActionDefinition.Builder actionDefBuilder1 = ActionDefinition.Builder.create(null, ruleName + "::TestAction", nameSpace, createKrmsActionTypeDefinition(nameSpace).getId(), ruleDef1.getId(), 1);
        ActionDefinition actionDef1 = actionBoService.createAction(actionDefBuilder1.build());

        return ruleDef1;
    }



    private RuleDefinition createRuleDefinition1(ContextDefinition contextDefinition, String agendaName, String nameSpace) {

        PropositionParametersBuilder params1 = new PropositionParametersBuilder();
        params1.add(createTermDefinition(CAMPUS_CODE_TERM_NAME, String.class, contextDefinition).getId(), PropositionParameterType.TERM);
        params1.add("QC", PropositionParameterType.CONSTANT);
        params1.add("=", PropositionParameterType.OPERATOR);

        PropositionParametersBuilder params2 = new PropositionParametersBuilder();
        params2.add(createTermDefinition(CAMPUS_CODE_TERM_NAME, String.class, contextDefinition).getId(), PropositionParameterType.TERM);
        params2.add("BL", PropositionParameterType.CONSTANT);
        params2.add("=", PropositionParameterType.OPERATOR);

        return createRuleDefinition(nameSpace, agendaName+"::Rule1", contextDefinition, LogicalOperator.OR, params1, params2);
    }


    private PropositionDefinition.Builder createPropositionDefinition(String propDescription, PropositionParametersBuilder params, RuleDefinition parentRule) {
        // Proposition for rule 2
        PropositionDefinition.Builder propositionDefBuilder1 =
                PropositionDefinition.Builder.create(null, PropositionType.SIMPLE.getCode(), parentRule.getId(),
                        null /* type code is only for custom props */, Collections.<PropositionParameter.Builder>emptyList());
        propositionDefBuilder1.setDescription(propDescription);

        // PropositionParams for rule 2
        List<PropositionParameter.Builder> propositionParams1 = params.build();


        // set the parent proposition so the builder will not puke
        for (PropositionParameter.Builder propositionParamBuilder : propositionParams1) {
            propositionParamBuilder.setProposition(propositionDefBuilder1);
        }

        propositionDefBuilder1.setParameters(propositionParams1);

        return propositionDefBuilder1;
    }
    
    
    
    private TermDefinition createTermDefinition(String termName, Class termValueType, ContextDefinition contextDefinition) {

        // this may be called more than once, we only want to create one though
        Map<String, String> queryArgs = new HashMap<String, String>();
        queryArgs.put("specification.namespace", contextDefinition.getNamespace());
        queryArgs.put("specification.name", termName);
        TermBo termBo = getBoService().findByPrimaryKey(TermBo.class, queryArgs);
        if (termBo != null) {
            return TermBo.to(termBo);
        }
        
        // campusCode TermSpec
        TermSpecificationDefinition termSpec =
            TermSpecificationDefinition.Builder.create(null, termName, contextDefinition.getNamespace(),
                    termValueType.getCanonicalName()).build();

        termSpec = termBoService.createTermSpecification(termSpec);
        
        // Term 1
        TermDefinition termDefinition =
            TermDefinition.Builder.create(null, TermSpecificationDefinition.Builder.create(termSpec), null).build();
        termDefinition = termBoService.createTermDefinition(termDefinition);

        return termDefinition;
    }


    private RuleDefinition createRuleDefinition2(ContextDefinition contextDefinition, String agendaName, String nameSpace) {

        PropositionParametersBuilder params1 = new PropositionParametersBuilder();
        params1.add(createTermDefinition2(contextDefinition, nameSpace).getId(), PropositionParameterType.TERM);
        params1.add("RESULT1", PropositionParameterType.CONSTANT);
        params1.add("=", PropositionParameterType.OPERATOR);

        PropositionParametersBuilder params2 = new PropositionParametersBuilder();
        params2.add(createTermDefinition2(contextDefinition, nameSpace).getId(), PropositionParameterType.TERM);
        params2.add("NotGonnaBeEqual", PropositionParameterType.CONSTANT);
        params2.add("=", PropositionParameterType.OPERATOR);

        return createRuleDefinition(nameSpace, agendaName+"::Rule2", contextDefinition, LogicalOperator.AND, params1, params2);
    }

    private RuleDefinition createRuleDefinition3(ContextDefinition contextDefinition, String agendaName, String nameSpace) {

        FunctionDefinition gcdFunction = functionBoService.getFunctionByNameAndNamespace("gcd", contextDefinition.getNamespace());

        if (null == gcdFunction) {
            // better configure a custom fuction for this
            // KrmsType for custom function
            KrmsTypeDefinition.Builder krmsFunctionTypeDefnBuilder = KrmsTypeDefinition.Builder.create("KrmsTestFunctionType", nameSpace);
            krmsFunctionTypeDefnBuilder.setServiceName("testFunctionTypeService");
            KrmsTypeDefinition krmsFunctionTypeDefinition = krmsTypeRepository.createKrmsType(krmsFunctionTypeDefnBuilder.build());

            FunctionDefinition.Builder functionBuilder =
                    FunctionDefinition.Builder.create(contextDefinition.getNamespace(), "gcd", Integer.class.getName(), krmsFunctionTypeDefinition.getId());

            functionBuilder.getParameters().add(FunctionParameterDefinition.Builder.create("arg0", Integer.class.getName(), 0));
            functionBuilder.getParameters().add(FunctionParameterDefinition.Builder.create("arg1", Integer.class.getName(), 1));
            functionBuilder.setReturnType(Integer.class.getName());

            gcdFunction = functionBoService.createFunction(functionBuilder.build());
        }

        PropositionParametersBuilder params = new PropositionParametersBuilder();

        // leverage our stack based evaluation in reverse polish notation
        params.add("1024", PropositionParameterType.CONSTANT);
        params.add("768", PropositionParameterType.CONSTANT);
        params.add(gcdFunction.getId(), PropositionParameterType.FUNCTION); // this should evaluate first: gcd(1024, 768)
        params.add("256", PropositionParameterType.CONSTANT);
        params.add("=", PropositionParameterType.OPERATOR); // this should evaluate second: gcdResult == 256

        return createRuleDefinition(nameSpace, agendaName+"::Rule3", contextDefinition, null, params);
    }


    private RuleDefinition createRuleDefinition4(ContextDefinition contextDefinition, String agendaName, String nameSpace) {

        PropositionParametersBuilder params1 = new PropositionParametersBuilder();
        params1.add(createTermDefinition(BOOL1, Boolean.class, contextDefinition).getId(), PropositionParameterType.TERM);
        params1.add(createTermDefinition(BOOL2, Boolean.class, contextDefinition).getId(), PropositionParameterType.TERM);
        params1.add("=", PropositionParameterType.OPERATOR);

        PropositionParametersBuilder params2 = new PropositionParametersBuilder();
        params2.add(createTermDefinition(BOOL2, Boolean.class, contextDefinition).getId(), PropositionParameterType.TERM);
        params2.add(createTermDefinition(BOOL1, Boolean.class, contextDefinition).getId(), PropositionParameterType.TERM);
        params2.add("=", PropositionParameterType.OPERATOR);

        return createRuleDefinition(nameSpace, agendaName+"::Rule4", contextDefinition, LogicalOperator.AND, params1, params2);
    }


    private TermDefinition createTermDefinition2(ContextDefinition contextDefinition, String nameSpace) {

        Map<String, String> queryArgs = new HashMap<String, String>();
        queryArgs.put("specification.namespace", contextDefinition.getNamespace());
        queryArgs.put("specification.name", "outputTermSpec");
        TermBo result = getBoService().findByPrimaryKey(TermBo.class, queryArgs);
        if (result != null) return TermBo.to(result);

        // output TermSpec
        TermSpecificationDefinition outputTermSpec =
            TermSpecificationDefinition.Builder.create(null, "outputTermSpec", contextDefinition.getNamespace(),
                    "java.lang.String").build();
        outputTermSpec = termBoService.createTermSpecification(outputTermSpec);

        // prereq TermSpec
        TermSpecificationDefinition prereqTermSpec =
            TermSpecificationDefinition.Builder.create(null, PREREQ_TERM_NAME, contextDefinition.getNamespace(),
                    "java.lang.String").build();
        prereqTermSpec = termBoService.createTermSpecification(prereqTermSpec);

        // Term Param
        TermParameterDefinition.Builder termParamBuilder2 =
            TermParameterDefinition.Builder.create(null, null, "testParamName", "testParamValue");

        // Term
        TermDefinition termDefinition2 =
            TermDefinition.Builder.create(null, TermSpecificationDefinition.Builder.create(outputTermSpec), Collections.singletonList(termParamBuilder2)).build();
        termDefinition2 = termBoService.createTermDefinition(termDefinition2);

		// KrmsType for TermResolver
		KrmsTypeDefinition.Builder krmsTermResolverTypeDefnBuilder = KrmsTypeDefinition.Builder.create("KrmsTestResolverType", nameSpace);
		krmsTermResolverTypeDefnBuilder.setServiceName("testTermResolverTypeService");

		KrmsTypeDefinition krmsTermResolverTypeDefinition = krmsTypeRepository.createKrmsType(krmsTermResolverTypeDefnBuilder.build());

        // TermResolver
		TermResolverDefinition termResolverDef =
			TermResolverDefinition.Builder.create(null, contextDefinition.getNamespace(), "testResolver1", krmsTermResolverTypeDefinition.getId(),
					TermSpecificationDefinition.Builder.create(outputTermSpec),
					Collections.singleton(TermSpecificationDefinition.Builder.create(prereqTermSpec)),
					null,
					Collections.singleton("testParamName")).build();
		termResolverDef = termBoService.createTermResolver(termResolverDef);

        return termDefinition2;
    }

    private static class PropositionParametersBuilder {
        
        // poor OOD but this is quick and dirty :-P
        private List<Object[]> params = new ArrayList<Object[]>();
        
        public PropositionParametersBuilder add(String value, PropositionParameterType type) {
            if (type == null) throw new IllegalArgumentException("type must not be null");
            params.add(new Object[]{value, type});
            return this;
        }
        
        public List<PropositionParameter.Builder> build() {
            int seqCounter = 0;
            
            List<PropositionParameter.Builder> results = new ArrayList<PropositionParameter.Builder>();
            
            for (Object[] param : params) {
                results.add(PropositionParameter.Builder.create(null, null, (String)param[0], ((PropositionParameterType)param[1]).getCode(), seqCounter++));
            }

            return results;
        }
    }

}
