package com.example.bpm.integration.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorVariable {
    CODE("errorCode"),
    MESSAGE("errorMessage"),
    STACK_TRACE("errorStackTrace"),
    BUSINESS_ERROR("businessError"),
    ;

    private final String name;

    public String getName() {
        return this.name;
    }
}
