package com.mryqr.support;

import org.junit.jupiter.api.function.Executable;

import java.time.Duration;
import java.time.Instant;

public class PollingAssertion {
    private Duration timeout = Duration.ofMillis(2000);
    private Duration pollInterval = Duration.ofMillis(300);
    private Duration pollDelay = Duration.ZERO;

    public static PollingAssertion pollAssert() {
        return new PollingAssertion();
    }

    public PollingAssertion timeout(Duration timeout) {
        if (timeout.toSeconds() > 60) {
            throw new RuntimeException("Poll timeout should not be longer than 60 seconds");
        }
        this.timeout = timeout;
        return this;
    }

    public PollingAssertion interval(Duration interval) {
        if (timeout.toSeconds() > 5) {
            throw new RuntimeException("Poll interval not be longer than 5 seconds");
        }
        this.pollInterval = interval;
        return this;
    }

    public PollingAssertion delay(Duration delay) {
        if (timeout.toSeconds() > 5) {
            throw new RuntimeException("Poll delay should not be longer than 5 seconds");
        }
        this.pollDelay = delay;
        return this;
    }

    public void run(Executable assertion) {
        try {
            if (!pollDelay.isZero()) {
                Thread.sleep(pollDelay.toMillis());
            }

            Instant deadline = Instant.now().plus(timeout);
            AssertionError lastError = null;
            while (Instant.now().isBefore(deadline)) {
                try {
                    assertion.execute();
                    return;
                } catch (AssertionError e) {
                    lastError = e;
                    Thread.sleep(pollInterval.toMillis());
                }
            }
            throw new AssertionError("Condition not met within " + timeout.toMillis() + " milliseconds", lastError);
        } catch (AssertionError t) {
            throw t;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}

