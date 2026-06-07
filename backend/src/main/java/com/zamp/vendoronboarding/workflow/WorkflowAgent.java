package com.zamp.vendoronboarding.workflow;

public interface WorkflowAgent {

    WorkflowStepDefinition getStep();

    AgentResult execute(WorkflowContext context);
}
