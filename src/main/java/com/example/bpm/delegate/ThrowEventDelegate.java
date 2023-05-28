package com.example.bpm.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.*;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.ThrowEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class ThrowEventDelegate implements JavaDelegate {

    private static final String CORRELATION_VARIABLES = "correlationVariables";

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        String executionId = execution.getId();

        ThrowEvent event = (ThrowEvent) execution.getBpmnModelElementInstance();

        Optional<EventDefinition> eventDefinition = event.getEventDefinitions().stream().filter(ed -> ed instanceof MessageEventDefinition).findFirst();

        if (eventDefinition.isPresent()) {
            MessageEventDefinition messageEventDefinition = (MessageEventDefinition) eventDefinition.get();

            String messageName = messageEventDefinition.getMessage().getName();

            if (!StringUtils.hasText(messageName)) {
                throw new BpmnError(executionId + " - Не указано имя сообщения");
            }

            log.info(MessageFormat.format("Попытка корреляции сообщения {0} executionId {1}", messageName, executionId));

            MessageCorrelationResult messageCorrelationResult = null;

            final Map<String, Object> localVariables = execution.getVariablesLocal();

            Map<String, Object> correlationVariables = (Map<String, Object>) localVariables.remove(CORRELATION_VARIABLES);

            MessageCorrelationBuilder builder = execution.getProcessEngine().getRuntimeService()
                    .createMessageCorrelation(messageName);

            if (!Objects.isNull(correlationVariables) && !correlationVariables.isEmpty()) {
                builder.processInstanceVariablesEqual(correlationVariables);
            }

            builder.setVariablesLocal(localVariables);

            try {
                messageCorrelationResult = builder.correlateWithResult();
            } catch (MismatchingMessageCorrelationException e) {
                log.error(MessageFormat.format("Ошибка корреляции сообщения {0}", messageName), e);
            }

            if (messageCorrelationResult != null) {

                if (messageCorrelationResult.getResultType().equals(MessageCorrelationResultType.ProcessDefinition)) {
                    ProcessInstance processInstance = messageCorrelationResult.getProcessInstance();

                    log.info(MessageFormat.format("Запущен процесс {0}", processInstance.getProcessDefinitionId()));
                } else if (messageCorrelationResult.getResultType().equals(MessageCorrelationResultType.Execution)) {
                    Execution execution1 = messageCorrelationResult.getExecution();

                    log.info(MessageFormat.format("Скоррелирован элемент {0} в процессе {1}", execution1.getId(), execution1.getProcessInstanceId()));
                }
            }
        }
    }
}
