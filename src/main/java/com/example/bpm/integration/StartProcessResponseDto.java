package com.example.bpm.integration;

import com.example.bpm.integration.common.BpmMessage;
import com.example.bpm.integration.common.ExternalTaskDto;
import com.example.bpm.integration.common.UserTaskDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/***
 * Ответ на запуск процесса по ключу
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class StartProcessResponseDto implements BpmMessage {

    @NotNull
    private UUID instanceId;
    private List<ExternalTaskDto> activeExternalTasks;
    private List<UserTaskDto> activeUserTasks;
}
