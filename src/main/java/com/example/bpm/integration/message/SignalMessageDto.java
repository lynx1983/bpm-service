package com.example.bpm.integration.message;

import com.example.bpm.integration.common.BpmMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/***
 * Запрос на корреляцию сообщения
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SignalMessageDto implements BpmMessage {
    /**
     * Имя сигнала
     */
    private String signalName;

    /**
     * Переменные для передачи в процесс
     */
    private Map<String, Object> variables;
}
