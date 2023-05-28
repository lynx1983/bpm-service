package com.example.bpm.integration;

import com.example.bpm.integration.common.BpmMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class StartProcessRequestDto implements BpmMessage {
    /**
     * Ключ (идентификатор) запускаемого процесса
     */
    private String processKey;

    /**
     *  Business key запускаемого процесса
     */
    private String businessKey;

    /**
     * Переменные для передачи в контекст процесса
     */
    private Map<String, Object> variables;
}
