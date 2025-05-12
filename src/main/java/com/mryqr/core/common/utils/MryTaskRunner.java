package com.mryqr.core.common.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MryTaskRunner {
    private boolean hasError;

    public static MryTaskRunner newTaskRunner() {
        return new MryTaskRunner();
    }

    public void run(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            log.error("Failed to run task: ", t);
            hasError = true;
        }
    }

    public boolean isHasError() {
        return hasError;
    }

}
