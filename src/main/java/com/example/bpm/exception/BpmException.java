package com.example.bpm.exception;


import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Base exception for all exceptions
 */
@Getter
@NoArgsConstructor
public class BpmException extends RuntimeException {

    /**
     * Код ошибки
     */
    private Integer errorCode;

    /**
     * Бизнес описание
     */
    private String businessMessage;

    /**
     * Внутреннее сообщение с техническими деталями.
     */
    private String errorMessage;

    public BpmException(String errorMessage) {
        super(errorMessage);
    }

    public BpmException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }

    public BpmException(Throwable cause) {
        super(cause);
    }

    public BpmException(Integer errorCode, String businessMessage, String errorMessage) {
        super(StringUtils.defaultString(errorMessage, businessMessage));
        this.errorCode = errorCode;
        this.businessMessage = businessMessage;
        this.errorMessage = errorMessage;
    }
}
