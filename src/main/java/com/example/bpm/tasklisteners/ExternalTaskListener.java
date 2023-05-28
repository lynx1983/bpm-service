package com.example.bpm.tasklisteners;

import com.example.bpm.exception.BpmException;
import com.example.bpm.integration.common.ExternalTaskDto;
import com.example.bpm.integration.common.enums.Variable;
import com.example.bpm.integration.externaltask.ExecuteExternalTaskDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.Map;

/**
 * Универсальный ExecutionListener для интеграции с микросервисами через ExternalServiceTask.
 * Отправляет оповещение о появление внешней задачи на требуемый микросервис
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ExternalTaskListener implements ExecutionListener {

    private final KafkaTemplate kafkaTemplate;

    public void notify(DelegateExecution delegate) {
        try {
            ServiceTask serviceTask = (ServiceTask) delegate.getBpmnModelElementInstance();
            String executionId = delegate.getId();

            log.info(MessageFormat.format("Запущен ExternalTaskListener {0} c externalTaskId {1}", serviceTask.getName(), executionId));

            final String serviceName = (String) delegate.getVariableLocal(Variable.SERVICE_NAME.getName());
            String topic = (String) delegate.getVariableLocal(Variable.TOPIC.getName());

            if (StringUtils.hasText(serviceName)) {
                topic = serviceName + "-request";
            } else if (!StringUtils.hasText(topic)) {
                throw new BpmnError(executionId + " - Не указано имя микросервиса или топика");
            }

            final String methodName = (String) delegate.getVariable(Variable.METHOD.getName());
            if (!StringUtils.hasText(methodName)) {
                throw new BpmnError(executionId + " - Не указано имя метода микросервиса");
            }

            final Map<String, Object> localVariables = delegate.getVariablesLocal();

            localVariables.remove(Variable.SERVICE_NAME.getName());
            localVariables.remove(Variable.METHOD.getName());

            final ExecuteExternalTaskDto request = new ExecuteExternalTaskDto();

            request.setTask(new ExternalTaskDto(executionId, serviceTask.getCamundaTopic(), methodName));
            request.setVariables(localVariables);

            final String topicName = topic;

            Context.getCommandContext().getTransactionContext()
                    .addTransactionListener(TransactionState.COMMITTED, commandContext ->
                            kafkaTemplate.send(topicName, request)
                    );
        } catch (Exception e) {
            log.error("Error: ", e);
            throw new BpmException(e.getMessage(), e);
        }
    }
}
