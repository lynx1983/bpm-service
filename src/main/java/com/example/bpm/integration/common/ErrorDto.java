package com.example.bpm.integration.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/***
 * Ошибка при выполнении запроса.
 * Используется при формировании ответа.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ErrorDto {
    /**
     * Код ошибки
     */
    private Integer errorCode;

    /**
     * Сообщение об ошибке.
     */
    private String message;

    @Deprecated
    private String stackTrace;

    private String businessError;

    /**
     * Критическая или нет
     */
    private Boolean isCritical = Boolean.FALSE;

    /**
     * Фатальная или нет.
     * Если true, то процесс немедленно завершается.
     */
    private boolean fatalError;

    @Deprecated
    public ErrorDto(String message, String stackTrace, String businessError) {
        this.message = message;
        this.stackTrace = stackTrace;
        this.businessError = businessError;
    }

    public ErrorDto(Integer errorCode, String businessError, String message) {
        this.errorCode = errorCode;
        this.businessError = businessError;
        this.message = message;
    }

    public ErrorDto(Integer errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public ErrorDto(Integer errorCode, String message, Boolean isCritical) {
        this(errorCode, message);
        this.isCritical = isCritical;
    }
}
