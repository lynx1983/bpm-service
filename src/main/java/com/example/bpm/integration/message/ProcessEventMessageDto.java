package com.example.bpm.integration.message;

import com.example.bpm.integration.common.BpmMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/***
 * Сообщение о событии в BPM процессе
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ProcessEventMessageDto implements BpmMessage {
    /**
     * Имя сообщения
     */
    private String messageName;

    /**
     * Переменные
     */
    private Map<String, Object> variables;
}
