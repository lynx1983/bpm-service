package com.example.bpm.integration.externaltask;

import com.example.bpm.integration.common.BpmMessage;
import com.example.bpm.integration.common.ExternalTaskDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/***
 * Запрос на исполнение внешней задачи
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ExecuteExternalTaskDto implements BpmMessage {
    private ExternalTaskDto task;
    private Map<String, Object> variables;
}
