package com.example.bpm.service;

import com.example.bpm.integration.StartProcessRequestDto;
import com.example.bpm.integration.StartProcessResponseDto;
import com.example.bpm.integration.externaltask.CompleteExternalTaskDto;
import com.example.bpm.integration.message.CorrelateProcessMessageDto;
import com.example.bpm.integration.message.SignalMessageDto;
import com.example.bpm.integration.usertask.CompleteUserTaskDto;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface BpmService {

    /**
     * Старт bpmn-процесса.
     *
     * @param request - Запрос запуска процесса по ключу
     */
    StartProcessResponseDto startProcess(StartProcessRequestDto request, Map<String, Object> headers);

    /**
     * Закрытие пользовательской задачи и загрузка новых переменных в процесс
     *
     * @param request - Запрос на закрытие задачи пользователя
     * @param headers - заголовки входящего сообщения kafka
     */
    void completeUserTask(CompleteUserTaskDto request, Map<String, Object> headers);

    /**
     * Закрытие внешней задачи и загрузка новых переменных в процесс
     *
     * @param request - Запрос на закрытие внешней задачи
     * @param headers - заголовки входящего сообщения kafka
     */
    @Async("completeTaskExecutor")
    void completeExternalTask(CompleteExternalTaskDto request, Map<String, Object> headers);

    /**
     * Корреляция сообщения и загрузка новых переменных в процесс
     *
     * @param request - Запрос на корреляцию сообщения
     * @param headers - заголовки входящего сообщения kafka
     */
    @Async("correlateMessageExecutor")
    void correlateMessage(CorrelateProcessMessageDto request, Map<String, Object> headers);

    /**
     * Корреляция сообщения и загрузка новых переменных в процесс
     *
     * @param request - Запрос на корреляцию сообщения
     * @param headers - заголовки входящего сообщения kafka
     */
    @Async("signalEventExecutor")
    void processSignalMessage(SignalMessageDto request, Map<String, Object> headers);
}


