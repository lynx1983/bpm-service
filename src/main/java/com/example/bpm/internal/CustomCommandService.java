package com.example.bpm.internal;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomCommandService {

    private CommandExecutor executor;

    @Autowired
    public CustomCommandService(@Autowired ProcessEngine processEngine) {
        executor = ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).getCommandExecutorTxRequired();
    }

    /**
     * executes a Command which implements the {@link Command} interface
     * @param command the command from type T
     * @return returns the result from type T
     */
    public <T> T execute (Command<T> command){
        return executor.execute(command);
    }

}