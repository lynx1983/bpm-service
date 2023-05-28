package com.example.bpm.integration.usertask;

import com.example.bpm.integration.common.BpmMessage;
import com.example.bpm.integration.common.UserTaskDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/***
 * Запрос на исполнение задачи пользователя
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ExecuteUserTaskDto implements BpmMessage {

    @Valid
    private UserTaskDto task;
}
