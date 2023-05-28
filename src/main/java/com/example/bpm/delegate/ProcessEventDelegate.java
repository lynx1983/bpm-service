package com.example.bpm.delegate;

import com.example.bpm.integration.common.enums.Variable;
import com.example.bpm.integration.message.ProcessEventMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.ThrowEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProcessEventDelegate implements JavaDelegate {

    private final KafkaTemplate kafkaTemplate;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String executionId = delegateExecution.getId();

        ThrowEvent event = (ThrowEvent) delegateExecution.getBpmnModelElementInstance();

        Optional<EventDefinition> eventDefinition = event.getEventDefinitions().stream().filter(ed -> ed instanceof MessageEventDefinition).findFirst();

        if (eventDefinition.isPresent()) {
            MessageEventDefinition messageEventDefinition = (MessageEventDefinition) eventDefinition.get();

            String messageName = messageEventDefinition.getMessage().getName();

            if (!StringUtils.hasText(messageName)) {
                throw new BpmnError(executionId + " - Не указано имя сообщения");
            }

            final String serviceName = (String) delegateExecution.getVariableLocal(Variable.SERVICE_NAME.getName());
            String topic = (String) delegateExecution.getVariableLocal(Variable.TOPIC.getName());

            if (StringUtils.hasText(serviceName)) {
                topic = serviceName + "-request";
            } else if (!StringUtils.hasText(topic)) {
                throw new BpmnError(executionId + " - Не указано имя микросервиса или топика");
            }

            final Map<String, Object> localVariables = delegateExecution.getVariablesLocal();

            localVariables.remove(Variable.SERVICE_NAME.getName());
            localVariables.remove(Variable.TOPIC.getName());

            ProcessEventMessageDto processEventMessage = new ProcessEventMessageDto();

            processEventMessage.setMessageName(messageName);
            processEventMessage.setVariables(localVariables);

            log.info(MessageFormat.format("Отправка сообщения {0} executionId {1} в топик {2}", messageName, executionId, topic));

            kafkaTemplate.send(topic, processEventMessage);
        }
    }
}
