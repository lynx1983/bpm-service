package com.example.bpm.service.impl;

import com.example.bpm.exception.BpmException;
import com.example.bpm.integration.StartProcessRequestDto;
import com.example.bpm.integration.StartProcessResponseDto;
import com.example.bpm.integration.common.ErrorDto;
import com.example.bpm.integration.externaltask.CompleteExternalTaskDto;
import com.example.bpm.integration.message.CorrelateProcessMessageDto;
import com.example.bpm.integration.message.SignalMessageDto;
import com.example.bpm.integration.usertask.CompleteUserTaskDto;
import com.example.bpm.internal.CustomCommandService;
import com.example.bpm.internal.LockExternalTaskByIdCmd;
import com.example.bpm.service.BpmService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.runtime.*;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.example.bpm.integration.common.enums.ErrorVariable.*;

@Service
@Slf4j
@AllArgsConstructor
public class BpmServiceImpl implements BpmService {

    private static final String WORKER_ID = "bpm";

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final ExternalTaskService externalTaskService;
    private final CustomCommandService commandService;

    public StartProcessResponseDto startProcess(StartProcessRequestDto request, Map<String, Object> headers) {
        String processKey = request.getProcessKey();
        log.info("Инициализация старта процесса: " + processKey);
        Map<String, Object> variables = new HashMap<>();

        if (Objects.nonNull(request.getVariables())) {
            variables.putAll(request.getVariables());
        }

        ProcessInstance processInstance;
        if (StringUtils.hasText(request.getBusinessKey())) {
            processInstance = runtimeService.startProcessInstanceByKey(processKey, request.getBusinessKey(), variables);
        } else {
            processInstance = runtimeService.startProcessInstanceByKey(processKey, variables);
        }
        String processInstanceId = processInstance.getProcessInstanceId();
        log.info("Процесс {} запущен с идентификатором {}", processKey, processInstanceId);
        StartProcessResponseDto response = new StartProcessResponseDto();
        response.setInstanceId(UUID.fromString(processInstanceId));
        return response;
    }

    public void completeUserTask(CompleteUserTaskDto request, Map<String, Object> headers) {
        Task task = taskService.createTaskQuery().executionId(request.getTask().getExecutionId()).active().singleResult();
        String executionId = request.getTask().getExecutionId();
        if(task != null) {
            String method = (String) runtimeService.getVariable(executionId, "method");

            Map<String, Object> localVariables = request.getTask().getVariables();
            if (localVariables == null) {
                localVariables = new HashMap<>();
            }

            log.info("Инициализация закрытия UserTask {}({}): ", method, executionId);

            if (request.getError() != null) {
                ErrorDto error = request.getError();
                localVariables.put(CODE.getName(), error.getErrorCode());
                localVariables.put(MESSAGE.getName(), error.getMessage());
                localVariables.put(BUSINESS_ERROR.getName(), error.getBusinessError());

                taskService.handleBpmnError(task.getId(), ObjectUtils.nullSafeToString(error.getErrorCode()), error.getMessage(), localVariables);
                log.info("UserTask {}, id: {} закрыта с ошибкой {} {}", executionId, task.getId(), error.getErrorCode(), error.getMessage());
            } else {
                taskService.complete(task.getId(), localVariables);
                log.info("Закрытие UserTask: {}({}) удачно", method, executionId);
            }
        } else {
            log.info("Задачи с идентификатором {} не найдено", executionId);
            throw new BpmException("Задача уже была завершена");
        }
    }

    public void correlateMessage(CorrelateProcessMessageDto request, Map<String, Object> headers) {
        String messageName = request.getMessageName();
        if (Objects.isNull(messageName)) {
            throw new BpmException("Не указано имя сообщения");
        }

        MessageCorrelationBuilder builder = runtimeService.createMessageCorrelation(messageName);

        if (StringUtils.hasText(request.getBusinessKey())) {
            builder.processInstanceBusinessKey(request.getBusinessKey());
        }

        if (Objects.nonNull(request.getCorrelationVariables())) {
            builder.localVariablesEqual(request.getCorrelationVariables());
        }

        if (Objects.nonNull(request.getVariables())) {
            builder.setVariablesLocal(request.getVariables());
        }

        log.info("Попытка корреляции сообщения {}, с переменными {}", messageName, request.getCorrelationVariables());

        MessageCorrelationResult correlationResult = null;

        try {
            correlationResult = builder.correlateWithResult();
        } catch (MismatchingMessageCorrelationException | OptimisticLockingException e) {
            log.error("Ошибка корреляции сообщения {}, с переменными {}: {}", messageName, request.getCorrelationVariables(), e.getMessage());
        }

        if (correlationResult != null) {
            if (correlationResult.getResultType().equals(MessageCorrelationResultType.ProcessDefinition)) {
                ProcessInstance processInstance = correlationResult.getProcessInstance();

                log.info("Запущен процесс {}", processInstance.getProcessDefinitionId());
            } else if (correlationResult.getResultType().equals(MessageCorrelationResultType.Execution)) {
                Execution execution = correlationResult.getExecution();

                log.info("Скоррелирован элемент {} в процессе {}, с переменными {}", execution.getId(), execution.getProcessInstanceId(), request.getCorrelationVariables());
            }
        }
    }

    public void completeExternalTask(CompleteExternalTaskDto request, Map<String, Object> headers) {
        String executionId = request.getTask().getExecutionId();
        ExternalTask externalTask = externalTaskService.createExternalTaskQuery().executionId(executionId).active().singleResult();
        if (externalTask == null) {
            log.error("Активной задачи с таким идентификатором не найдено: {}, переменные: {}", executionId, request.getVariables());
            return;
        }

        log.info("Инициализация закрытия ExternalServiceTask {}, id: {}", executionId, externalTask.getId());

        Map<String, Object> variables = new HashMap<>();
        Map<String, Object> localVariables = new HashMap<>();
        Map<String, Object> taskVariables = request.getVariables();
        if (taskVariables != null) {
            localVariables.putAll(taskVariables);
        }
        ErrorDto error = request.getError();
        if (error != null) {
            variables.put(CODE.getName(), error.getErrorCode());
            variables.put(MESSAGE.getName(), error.getMessage());
            variables.put(BUSINESS_ERROR.getName(), error.getBusinessError());
        }

        commandService.execute(new LockExternalTaskByIdCmd().setIds(Arrays.asList(externalTask.getId())).setLockDuration(5000).setWorkerId(WORKER_ID));

        try {
            if (error != null) {
                externalTaskService.handleBpmnError(externalTask.getId(), WORKER_ID, ObjectUtils.nullSafeToString(error.getErrorCode()), error.getMessage(), variables);
                log.info("ExternalServiceTask {}, id: {} закрыта с ошибкой", executionId, externalTask.getId());
            } else {
                externalTaskService.complete(externalTask.getId(), WORKER_ID, variables, localVariables);
                log.info("ExternalServiceTask: {}, id: {} закрыта успешно", executionId, externalTask.getId());
            }
        } catch (OptimisticLockingException e) {
            throw new BpmException("Ошибка OptimisticLockingException при завершении задачи", e);
        }
    }

    @Override
    public void processSignalMessage(SignalMessageDto request, Map<String, Object> headers) {
        String signalName = request.getSignalName();
        if (Objects.isNull(signalName)) {
            throw new BpmnError("Не указано имя сигнала");
        }

        SignalEventReceivedBuilder builder = runtimeService.createSignalEvent(signalName);

        if (Objects.nonNull(request.getVariables())) {
            builder.setVariables(request.getVariables());
        }

        log.info("Попытка публикации сигнала {}, с переменными {}", signalName, request.getVariables());

        try {
            builder.send();
        } catch (MismatchingMessageCorrelationException e) {
            log.error("Ошибка публикации сигнала {}, с переменными {}: {}", signalName, request.getVariables(), e.getMessage());
        }
    }
}

