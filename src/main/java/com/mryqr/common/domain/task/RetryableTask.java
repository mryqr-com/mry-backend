package com.mryqr.common.domain.task;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

//Marker接口，除可配置Retryable外无实际作用
//保证一个Task只做一件事情，即只操作一种聚合，并且task的package跟着其所操作的聚合走
//每个task接收原始数据类型
@Retryable(backoff = @Backoff(delay = 500, multiplier = 3, random = true))
public interface RetryableTask {
}
