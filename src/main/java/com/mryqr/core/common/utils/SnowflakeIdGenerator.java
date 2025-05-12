package com.mryqr.core.common.utils;

import org.apache.commons.lang3.RandomUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * SnowFlake id generator, inspired by Twitter.
 *
 * @link https://developer.twitter.com/en/docs/basics/twitter-ids
 * This class should be plain java class, and not depends on any framework or other package.
 * <p>
 * 64 bits long type in java
 * |-- 1 bit not use --|-- 41 bits timestamp in milliseconds --|-- 12 bits worker id --|-- 10 bits sequence --|
 * <p>
 * all timestamp values in this class are millisecond.
 */
public class SnowflakeIdGenerator {

    /**
     * timestamp start from (January 1, 2020 1:01:01 AM)
     */
    private static final long EPOCH = 1577840461000L;

    /**
     * worker-id take 12 bits
     */
    private static final long WORKER_ID_BITS = 12L;

    /**
     * sequence take 10 bits
     */
    private static final long SEQUENCE_BITS = 10L;

    /**
     * mask for some bit operation on sequence
     */
    private static final long SEQUENCE_MASK = (1L << SEQUENCE_BITS) - 1;

    /**
     * worker-id left-shift length
     */
    private static final long WORKER_ID_LEFT_SHIFT_BITS = SEQUENCE_BITS;

    /**
     * timestamp left-shift length
     */
    private static final long TIMESTAMP_LEFT_SHIFT_BITS = WORKER_ID_LEFT_SHIFT_BITS + WORKER_ID_BITS;

    /**
     * worker-id should <= this value
     */
    private static final long WORKER_ID_MAX_VALUE = (1L << WORKER_ID_BITS) - 1;

    /**
     * machine time may go backwards, if under 10 milliseconds we wait
     */
    private static final int MAX_TIMESTAMP_BACKWARDS_TO_WAIT = 10;
    private static SnowflakeIdGenerator INSTANCE;
    private final long WORKER_ID;
    private long sequence;
    private long lastTimestamp = -1L;

    private SnowflakeIdGenerator(long workerId) {
        if (workerId > WORKER_ID_MAX_VALUE || workerId < 1) {
            throw new IllegalArgumentException(String.format("worker id can't be greater than %d or less than 1", WORKER_ID_MAX_VALUE));
        }
        this.WORKER_ID = workerId;
    }

    private synchronized static SnowflakeIdGenerator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SnowflakeIdGenerator(getWorkerId());
        }
        return INSTANCE;
    }

    /**
     * return unique worker id.
     * take last 12 bits of ip as worker id.
     * this is a weak solution: depends on network setting, not consider port.
     *
     * @return 12 bits value
     */
    private static long getWorkerId() {
        try {
            String[] ips = InetAddress.getLocalHost().getHostAddress().split("\\.");
            if (ips.length <= 1) {
                return RandomUtils.nextLong(1, 1000);//没有联网时无法获取ip,返回1-1000之间的随机数
            }

            // third fragment (8 bits, from 17 to 24 bit of ip)
            long subnet = Long.parseLong(ips[2]);
            // forth fragment (8 bits, from 25 to 32 bit of ip)
            long machine = Long.parseLong(ips[3]);
            // subnet take last 4 bits
            return (subnet & 0b00001111) << 8 | machine;
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Cannot get worker ID.", e);
        }
    }

    public static long newSnowflakeId() {
        return SnowflakeIdGenerator.getInstance().getNextId();
    }

    private synchronized long getNextId() {
        long currentTimestamp = getCurrentTimestamp();
        // if clock moved back we may wait
        if (tolerateTimestampBackwardsIfNeed(currentTimestamp)) {
            currentTimestamp = getCurrentTimestamp();
        }
        // if current timestamp equals to previous one, we try to increase sequence
        if (lastTimestamp == currentTimestamp) {
            if (sequenceIncreaseIfReachLimitReset()) {
                currentTimestamp = waitUntilNextTime(currentTimestamp);
            }
        } else {// we go into to new timestamp, reset sequence
            sequence = 0L;
        }
        lastTimestamp = currentTimestamp;
        return ((currentTimestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT_BITS)
                | (WORKER_ID << WORKER_ID_LEFT_SHIFT_BITS)
                | sequence;
    }

    private boolean tolerateTimestampBackwardsIfNeed(long curTimestamp) {
        if (lastTimestamp <= curTimestamp) {
            return false;
        }
        long timeDifference = lastTimestamp - curTimestamp;
        if (timeDifference < MAX_TIMESTAMP_BACKWARDS_TO_WAIT) {
            waitUntilNextTime(lastTimestamp);
        } else {
            throw new RuntimeException("machine clock moved backward too much");
        }
        return true;
    }

    private boolean sequenceIncreaseIfReachLimitReset() {
        return 0L == (sequence = (sequence + 1) & SEQUENCE_MASK);
    }

    private long waitUntilNextTime(long timestampToContinue) {
        long timestamp;
        do {
            timestamp = getCurrentTimestamp();
        } while (timestamp <= timestampToContinue);
        return timestamp;
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
}
