package com.example.bpm.integration.usertask;

import com.example.bpm.integration.common.BpmMessage;
import com.example.bpm.integration.common.ErrorDto;
import com.example.bpm.integration.common.UserTaskDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/***
 * Запрос на закрытие задачи пользователя
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CompleteUserTaskDto implements BpmMessage {

    private UserTaskDto task;
    private ErrorDto error;
}
