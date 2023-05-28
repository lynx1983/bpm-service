package com.example.bpm.integration.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/***
 * Информация об активной внешней задаче
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ExternalTaskDto implements Serializable {
    /**
     * execution-идентификатор инстанса задачи
     */
    private String executionId;

    /**
     * Топик задачи
     */
    private String topic;

    /**
     * Наименование метода на выполнение
     */
    private String method;

    public ExternalTaskDto(String executionId, String method) {
        this.executionId = executionId;
        this.method = method;
    }
}
