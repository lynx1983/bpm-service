package com.example.bpm.integration.externaltask;

import com.example.bpm.integration.common.BpmMessage;
import com.example.bpm.integration.common.ErrorDto;
import com.example.bpm.integration.common.ExternalTaskDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/***
 * Запрос на закрытие внешней задачи
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CompleteExternalTaskDto implements BpmMessage {

    private ExternalTaskDto task;           // Инстанс внешней задачи
    private Map<String, Object> variables;  // Переменные процесса
    private ErrorDto error;                 // Информация об ощибке


    public CompleteExternalTaskDto(ExternalTaskDto task) {
        this.task = task;
    }
}
