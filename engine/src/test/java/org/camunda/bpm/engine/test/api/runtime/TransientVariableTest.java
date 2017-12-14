package org.camunda.bpm.engine.test.api.runtime;

import static org.junit.Assert.*;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.ConditionalModels.*;
import static org.hamcrest.CoreMatchers.containsString;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class TransientVariableTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected TaskService taskService;

  @Before
  public void init() {
    this.runtimeService = engineRule.getRuntimeService();
    this.historyService = engineRule.getHistoryService();
    this.taskService = engineRule.getTaskService();
  }

  @Test
  public void createTransientTypedVariablesUsingVariableMap() throws URISyntaxException {
    // given
    testRule.deploy(ProcessModels.ONE_TASK_PROCESS);

    // when
    runtimeService.startProcessInstanceByKey("Process",
        Variables.createVariables()
            .putValueTyped("a", Variables.stringValue("bar", true))
            .putValueTyped("b", Variables.booleanValue(true, true))
            .putValueTyped("c", Variables.byteArrayValue("test".getBytes(), true))
            .putValueTyped("d", Variables.dateValue(new Date(), true))
            .putValueTyped("e", Variables.doubleValue(20., true))
            .putValueTyped("f", Variables.integerValue(10, true))
            .putValueTyped("g", Variables.longValue((long) 10, true))
            .putValueTyped("h", Variables.shortValue((short) 10, true))
            .putValueTyped("i", Variables.objectValue(new Integer(100), true).create())
            .putValueTyped("j", Variables.untypedValue(null, true))
            .putValueTyped("k", Variables.untypedValue(Variables.booleanValue(true), true))
            .putValueTyped("l", Variables.fileValue(new File(this.getClass().getClassLoader()
                .getResource("org/camunda/bpm/engine/test/standalone/variables/simpleFile.txt").toURI()), true)));

    // then
    List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery().list();
    List<VariableInstance> variableInstances = runtimeService.createVariableInstanceQuery().list();
    assertEquals(0, historicVariableInstances.size());
    assertEquals(0, variableInstances.size());
  }

  @Test
  public void createTransientVariablesUsingVariableMap() throws URISyntaxException {
    // given
    testRule.deploy(ProcessModels.ONE_TASK_PROCESS);

    // when
    runtimeService.startProcessInstanceByKey("Process",
        Variables.createVariables().putValue("a", Variables.stringValue("bar", true))
        .putValue("b", Variables.booleanValue(true, true))
        .putValue("c", Variables.byteArrayValue("test".getBytes(), true))
        .putValue("d", Variables.dateValue(new Date(), true))
        .putValue("e", Variables.doubleValue(20., true))
        .putValue("f", Variables.integerValue(10, true))
        .putValue("g", Variables.longValue((long) 10, true))
        .putValue("h", Variables.shortValue((short) 10, true))
        .putValue("i", Variables.objectValue(new Integer(100), true).create())
        .putValue("j", Variables.untypedValue(null, true))
        .putValue("k", Variables.untypedValue(Variables.booleanValue(true), true))
        .putValue("l", Variables.fileValue(new File(this.getClass().getClassLoader()
            .getResource("org/camunda/bpm/engine/test/standalone/variables/simpleFile.txt").toURI()), true)));

    // then
    List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery().list();
    List<VariableInstance> variableInstances = runtimeService.createVariableInstanceQuery().list();
    assertEquals(0, historicVariableInstances.size());
    assertEquals(0, variableInstances.size());
  }

  @Test
  public void createTransientVariablesUsingFluentBuilder() {
    // given
    testRule.deploy(ProcessModels.ONE_TASK_PROCESS);

    // when
    runtimeService.createProcessInstanceByKey("Process")
      .setVariables(Variables.createVariables().putValue("foo", Variables.stringValue("dlsd", true)))
      .execute();

    // then
    List<VariableInstance> variableInstances = runtimeService.createVariableInstanceQuery().list();
    List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(0, variableInstances.size());
    assertEquals(0, historicVariableInstances.size());
  }

  @Test
  public void createVariablesUsingVariableMap() {
    // given
    testRule.deploy(ProcessModels.ONE_TASK_PROCESS);

    // when
    VariableMap variables = Variables.createVariables();
    variables.put("b", Variables.untypedValue(true, true));
    runtimeService.startProcessInstanceByKey("Process",
       variables
        );

    // then
    List<VariableInstance> variableInstances = runtimeService.createVariableInstanceQuery().list();
    List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(0, variableInstances.size());
    assertEquals(0, historicVariableInstances.size());
  }

  @Test
  public void triggerConditionalEventWithTransientVariable() {
    // given
    BpmnModelInstance instance = Bpmn.createExecutableProcess(CONDITIONAL_PROCESS_KEY)
        .startEvent()
        .serviceTask()
        .camundaClass(SetVariableTransientDelegate.class.getName())
        .intermediateCatchEvent(CONDITION_ID)
        .conditionalEventDefinition()
        .condition(VAR_CONDITION)
        .conditionalEventDefinitionDone()
        .endEvent()
        .done();

    testRule.deploy(instance);

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CONDITIONAL_PROCESS_KEY);

    // then
    assertEquals(true, processInstance.isEnded());
  }


  @Test
  public void testParallelProcessWithSetVariableTransientAfterReachingEventBasedGW() {
    BpmnModelInstance modelInstance =
        Bpmn.createExecutableProcess(CONDITIONAL_PROCESS_KEY)
          .startEvent()
          .parallelGateway()
          .id("parallel")
          .userTask("taskBeforeGw")
          .eventBasedGateway()
          .id("evenBased")
          .intermediateCatchEvent()
          .conditionalEventDefinition()
          .condition(VAR_CONDITION)
          .camundaVariableEvents(Arrays.asList("create", "update"))
          .conditionalEventDefinitionDone()
          .userTask()
          .name("taskAfter")
          .endEvent()
          .moveToNode("parallel")
          .userTask("taskBefore")
          .serviceTask()
          .camundaClass(SetVariableTransientDelegate.class.getName())
          .endEvent()
          .done();

    testRule.deploy(modelInstance);

    //given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task taskBeforeEGW = taskService.createTaskQuery().taskDefinitionKey("taskBeforeGw").singleResult();
    Task taskBeforeServiceTask = taskService.createTaskQuery().taskDefinitionKey("taskBefore").singleResult();

    //when task before event based gateway is completed and after that task before service task
    taskService.complete(taskBeforeEGW.getId());
    taskService.complete(taskBeforeServiceTask.getId());

    //then event based gateway is reached and executions stays there
    //variable is set after reaching event based gateway
    //after setting variable the conditional event is triggered and evaluated to true
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals("taskAfter", task.getName());
    //completing this task ends process instance
    taskService.complete(task.getId());
    assertNull(taskQuery.singleResult());
    assertNull(runtimeService.createProcessInstanceQuery().singleResult());
  }

  @Test
  public void setVariableTransientInRunningProcessInstance() {
    // given
    testRule.deploy(ProcessModels.ONE_TASK_PROCESS);

    // when
    runtimeService.startProcessInstanceByKey(ProcessModels.PROCESS_KEY);
    Execution execution = runtimeService.createExecutionQuery().singleResult();
    runtimeService.setVariable(execution.getId(), "foo", Variables.stringValue("bar", true));

    // then
    List<VariableInstance> variables = runtimeService.createVariableInstanceQuery().list();
    assertEquals(0, variables.size());
  }

  @Test
  public void setVariableTransientForCase() {
    // given
    Deployment deployment = engineRule.getRepositoryService().createDeployment()
    .addClasspathResource("org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn")
    .deploy();

    // when
    engineRule.getCaseService().withCaseDefinitionByKey("oneTaskCase")
        .setVariable("foo", Variables.stringValue("bar", true)).create();

    // then
    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(0, variables.size());

    engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
  }

  @Test
  public void testTransientVariableOvewritesPersistedVariableInSameScope() {
    testRule.deploy(ProcessModels.ONE_TASK_PROCESS);
    runtimeService.startProcessInstanceByKey("Process", Variables.createVariables().putValue("foo", "bar"));
    Execution execution = runtimeService.createExecutionQuery().singleResult();

    try {
      runtimeService.setVariable(execution.getId(), "foo", Variables.stringValue("xyz", true));
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("Cannot set transient variable with name foo"));
    }
  }

  @Test
  public void testSameNamesDifferentScopes() {
    testRule.deploy(ProcessModels.SUBPROCESS_PROCESS);
    runtimeService.startProcessInstanceByKey("Process", Variables.createVariables().putValue("foo", Variables.stringValue("bar")));
    Execution execution = runtimeService.createExecutionQuery().activityId(USER_TASK_ID).singleResult();

    try {
      runtimeService.setVariable(execution.getId(), "foo", Variables.stringValue("xyz", true));
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("Cannot set transient variable with name foo"));
    }
  }

  @Test
  public void testFormFieldsWithCustomTransientFlags() {
    // given
    testRule.deploy("org/camunda/bpm/engine/test/api/form/FormServiceTest.taskFormFieldsWithTransientFlags.bpmn20.xml");
    runtimeService.startProcessInstanceByKey("testProcess");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    Map<String, Object> formValues = new HashMap<String, Object>();
    formValues.put("stringField", Variables.stringValue("foobar", true));
    formValues.put("longField", 9L);
    engineRule.getFormService().submitTaskForm(task.getId(), formValues);

    // then
    List<VariableInstance> variables = runtimeService.createVariableInstanceQuery().list();
    assertEquals(1, variables.size());
    assertEquals(variables.get(0).getValue(), 9L);
  }

  @Test
  public void testStartProcessInstanceWithFormsUsingTransientVariables() {
    // given
    testRule.deploy("org/camunda/bpm/engine/test/api/form/FormServiceTest.startFormFieldsWithTransientFlags.bpmn20.xml");
    ProcessDefinition processDefinition = engineRule.getRepositoryService().createProcessDefinitionQuery().singleResult();

    // when
    Map<String, Object> formValues = new HashMap<String, Object>();
    formValues.put("stringField", Variables.stringValue("foobar", true));
    formValues.put("longField", 9L);
    engineRule.getFormService().submitStartForm(processDefinition.getId(), formValues);

    // then
    List<VariableInstance> variables = runtimeService.createVariableInstanceQuery().list();
    assertEquals(1, variables.size());
    assertEquals(variables.get(0).getValue(), 9L);
  }

  @Test
  public void testSignalWithTransientVariables() {
    // given
    BpmnModelInstance instance = Bpmn.createExecutableProcess("Process")
    .startEvent()
    .intermediateCatchEvent("signalCatch")
      .signal("signal")
    .scriptTask("scriptTask")
      .scriptFormat("javascript")
      .camundaResultVariable("abc")
      .scriptText("execution.setVariable('abc', foo);")
    .endEvent()
    .done();

    testRule.deploy(instance);
    runtimeService.startProcessInstanceByKey("Process");

    // when
    runtimeService.signalEventReceived("signal",
        Variables.createVariables().putValue("foo", Variables.stringValue("bar", true)));

    // then
    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(1, variables.size());
    assertEquals("abc", variables.get(0).getName());
  }

  public static class SetVariableTransientDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
      execution.setVariable(VARIABLE_NAME, Variables.integerValue(1, true));
    }
  }
}
