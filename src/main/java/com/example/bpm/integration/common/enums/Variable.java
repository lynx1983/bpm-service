package com.example.bpm.integration.common.enums;

import lombok.AllArgsConstructor;

/**
 * Имена переменных процесса
 */
@AllArgsConstructor
public enum Variable {
    /**
     * Наименование метода
     */
    METHOD("method"),

    /**
     * Наименование топика
     */
    TOPIC("topic"),

    /**
     * Наименование сервиса
     */
    SERVICE_NAME("serviceName");

    private String name;

    public String getName() {
        return name;
    }
}
