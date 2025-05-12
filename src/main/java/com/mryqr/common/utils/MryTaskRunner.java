package com.mryqr.common.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MryTaskRunner {

    public static void run(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            log.error("Failed to run task: ", t);
        }
    }

}
