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
public class CorrelateProcessMessageDto implements BpmMessage {
    /**
     * Имя сообщения
     */
    private String messageName;

    /**
     * Business key для корреляции сообщения.
     */
    private String businessKey;

    /**
     * Переменные для корреляции. Значения будут проверены на равенство с переменными в процессе.
     */
    private Map<String, Object> correlationVariables;

    /**
     * Переменные для передачи в процесс.
     */
    private Map<String, Object> variables;
}
