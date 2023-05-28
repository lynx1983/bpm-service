package com.example.bpm.integration.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import java.util.Map;

/***
 * Информация об активной задаче пользователя
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserTaskDto {

    private String executionId;

    private String method;
    private String assignee;

    @Valid
    private Map<String, Object> variables;
}
