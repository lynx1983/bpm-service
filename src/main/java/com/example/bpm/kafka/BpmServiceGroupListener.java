package com.example.bpm.kafka;

import com.example.bpm.exception.BpmException;
import com.example.bpm.integration.StartProcessRequestDto;
import com.example.bpm.integration.StartProcessResponseDto;
import com.example.bpm.integration.externaltask.CompleteExternalTaskDto;
import com.example.bpm.integration.message.CorrelateProcessMessageDto;
import com.example.bpm.integration.message.SignalMessageDto;
import com.example.bpm.integration.usertask.CompleteUserTaskDto;
import com.example.bpm.service.BpmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;

import java.util.Map;


/**
 * Групповой kafka-listener
 */
@Service
@KafkaListener(groupId = "bpm", topics = {"${kafka.groupConsumer.topic}"})
@Slf4j
@RequiredArgsConstructor
public class BpmServiceGroupListener {

    private final BpmService bpmService;

    /**
     * Старт bpmn-процесса.
     *
     * @param request - Запрос запуска процесса по ключу
     * @param headers - заголовки входящего сообщения kafka
     * @param ack     - Дескриптор для подтверждения обработки kafka-сообщения
     * @return - Ответ на запуск процесса по ключу
     */
    @KafkaHandler
    @SendTo("!{source.headers['kafka_replyTopic']}")
    public Message<StartProcessResponseDto> startProcess(StartProcessRequestDto request, @Headers Map<String, Object> headers, Acknowledgment ack) {
        try {
            return new GenericMessage<>(bpmService.startProcess(request, headers));
        } catch (Exception e) {
            throw new BpmException(e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }


    /**
     * Закрытие пользовательской задачи и загрузка новых переменных в процесс
     *
     * @param request - Запрос на закрытие задачи пользователя
     * @param headers - заголовки входящего сообщения kafka
     * @param ack     - Дескриптор для подтверждения обработки kafka-сообщения
     */
    @KafkaHandler
    public void completeUserTasks(CompleteUserTaskDto request, @Headers Map<String, Object> headers, Acknowledgment ack) {
        try {
            bpmService.completeUserTask(request, headers);
        } catch (Exception e) {
            throw new BpmException(e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }


    /**
     * Закрытие внешней задачи и загрузка новых переменных в процесс
     *
     * @param request - Запрос на закрытие внешней задачи
     * @param headers - заголовки входящего сообщения kafka
     * @param ack     - Дескриптор для подтверждения обработки kafka-сообщения
     */
    @KafkaHandler
    public void completeExternalTask(CompleteExternalTaskDto request, @Headers Map<String, Object> headers, Acknowledgment ack) {
        try {
            bpmService.completeExternalTask(request, headers);
        } catch (Exception e) {
            throw new BpmException(e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }

    /**
     * Корреляция сообщения и загрузка новых переменных в процесс
     *
     * @param request - Запрос на коррешляцию сообщения
     * @param headers - заголовки входящего сообщения kafka
     * @param ack     - Дескриптор для подтверждения обработки kafka-сообщения
     */
    @KafkaHandler
    public void correlateMessage(CorrelateProcessMessageDto request, @Headers Map<String, Object> headers, Acknowledgment ack) {
        try {
            bpmService.correlateMessage(request, headers);
        } catch (Exception e) {
            throw new BpmException(e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }

    /**
     * Публикация сигнала и загрузка новых переменных
     *
     * @param request - Запрос на публикацию сигнала
     * @param headers - заголовки входящего сообщения kafka
     * @param ack     - Дескриптор для подтверждения обработки kafka-сообщения
     */
    @KafkaHandler
    public void signalMessage(SignalMessageDto request, @Headers Map<String, Object> headers, Acknowledgment ack) {
        try {
            bpmService.processSignalMessage(request, headers);
        } catch (Exception e) {
            throw new BpmException(e.getMessage(), e);
        } finally {
            ack.acknowledge();
        }
    }
}