package com.mryqr.core.common.domain.task;

//Marker接口，无实际作用
//保证一个Task只做一件事情，即只操作一种聚合，并且task的package跟着其所操作的聚合走
//每个task接收原始数据类型
public interface OnetimeTask {
}
