package com.mryqr.management.platform.domain;

public interface PlatformRepository {

    Platform getPlatform();

    void save(Platform it);

    boolean platformExists();

    void increaseMobileAccessCount();

    void increaseNonMobileAccessCount();

}
