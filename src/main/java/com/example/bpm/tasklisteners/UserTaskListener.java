package com.example.bpm.tasklisteners;

import com.example.bpm.exception.BpmException;
import com.example.bpm.integration.common.UserTaskDto;
import com.example.bpm.integration.common.enums.Variable;
import com.example.bpm.integration.usertask.ExecuteUserTaskDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Универсальный ExecutionListener для интеграции с микросервисами через UserTask.
 * Отправляет оповещение о появление задачи пользователя на требуемый микросервис
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserTaskListener implements ExecutionListener {

    private final KafkaTemplate kafkaTemplate;

    public void notify(DelegateExecution delegate) throws Exception {
        String executionId = delegate.getId();
        try {
            UserTask userTask = (UserTask) delegate.getBpmnModelElementInstance();

            final String serviceName = (String) delegate.getVariableLocal(Variable.SERVICE_NAME.getName());
            String topic = (String) delegate.getVariableLocal(Variable.TOPIC.getName());

            if (StringUtils.hasText(serviceName)) {
                topic = serviceName + "-request";
            } else if (!StringUtils.hasText(topic)) {
                throw new BpmnError(executionId + " - Не указано имя микросервиса или топика");
            }

            final Map<String, Object> localVariables = delegate.getVariablesLocal();

            localVariables.remove(Variable.SERVICE_NAME.getName());
            localVariables.remove(Variable.METHOD.getName());
            localVariables.remove(Variable.TOPIC.getName());

            log.info("Запущен UserTaskListener {} с executionId {}", userTask.getName(), executionId);

            String methodName = (String) delegate.getVariable(Variable.METHOD.getName());
            if (!StringUtils.hasText(methodName)) {
                throw new BpmnError("Не указано имя метода UI: " + executionId);
            }

            ExecuteUserTaskDto request = new ExecuteUserTaskDto();

            UserTaskDto userTaskDto = new UserTaskDto();

            userTaskDto.setExecutionId(executionId);
            userTaskDto.setVariables(localVariables);

            request.setTask(userTaskDto);

            this.kafkaTemplate.send(topic, request);
        } catch (IllegalArgumentException e) {
            throw new BpmnError("Неверное имя метода UI: " + executionId);
        } catch (Exception e) {
            throw new BpmException(e.getMessage(), e);
        }
    }
}
