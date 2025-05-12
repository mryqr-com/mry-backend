package com.mryqr.core.tenant.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static java.time.format.DateTimeFormatter.ofPattern;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.MapUtils.isEmpty;

@EqualsAndHashCode
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public final class ResourceUsage {
    private int appCount;//已创建应用总数
    private int memberCount;//已创建成员总数
    private int departmentCount;//已创建的部门数
    private float storage;//已使用的存储占用量(GB)
    private int plateCount;//已创建码牌总数
    private SmsSentCount smsSentCount;//本月短信发送量

    private Map<String, Integer> qrCountPerApp;//每个应用对应的QR数量，appId -> qr count
    private Map<String, Integer> groupCountPerApp;//每个应用对应的group数量，appId -> group count
    private Map<String, Integer> submissionCountPerApp;//每个应用的提交数量，appId -> submission count

    public static ResourceUsage init() {
        return ResourceUsage.builder()
                .appCount(0)
                .memberCount(0)
                .departmentCount(0)
                .storage(0)
                .plateCount(0)
                .qrCountPerApp(new HashMap<>())
                .groupCountPerApp(new HashMap<>())
                .submissionCountPerApp(new HashMap<>())
                .smsSentCount(new SmsSentCount())
                .build();
    }

    public void updateAppCount(int appCount) {
        this.appCount = appCount;
    }

    public void updateMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public void updateDepartmentCount(int count) {
        this.departmentCount = count;
    }

    public void setStorage(float amount) {
        this.storage = amount;
    }

    public void updatePlateCount(int plateCount) {
        this.plateCount = plateCount;
    }

    public void updateAppQrCount(String appId, int qrCount) {
        this.qrCountPerApp().put(appId, qrCount);
    }

    public void updateAppGroupCount(String appId, int groupCount) {
        this.groupCountPerApp().put(appId, groupCount);
    }

    public void updateAppSubmissionCount(String appId, int submissionCount) {
        this.submissionCountPerApp().put(appId, submissionCount);
    }

    public void removeApp(String appId) {
        this.qrCountPerApp().remove(appId);
        this.groupCountPerApp().remove(appId);
        this.submissionCountPerApp().remove(appId);
    }

    public int getQrCountForApp(String appId) {
        Integer count = qrCountPerApp().get(appId);
        return count == null ? 0 : count;
    }

    public int getGroupCountForApp(String appId) {
        Integer count = groupCountPerApp().get(appId);
        return count == null ? 0 : count;
    }

    public int getSubmissionCountForApp(String appId) {
        Integer count = submissionCountPerApp().get(appId);
        return count == null ? 0 : count;
    }

    public int getAppCount() {
        return appCount;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public int getDepartmentCount() {
        return departmentCount;
    }

    public float getStorage() {
        return storage;
    }

    public int getPlateCount() {
        return plateCount;
    }

    public int allQrCount() {
        return this.qrCountPerApp().values().stream().reduce(0, Integer::sum);
    }

    public int allSubmissionCount() {
        return this.submissionCountPerApp().values().stream().reduce(0, Integer::sum);
    }

    private Map<String, Integer> qrCountPerApp() {
        if (isEmpty(this.qrCountPerApp)) {
            this.qrCountPerApp = new HashMap<>();
        }

        return this.qrCountPerApp;
    }

    private Map<String, Integer> groupCountPerApp() {
        if (isEmpty(this.groupCountPerApp)) {
            this.groupCountPerApp = new HashMap<>();
        }

        return this.groupCountPerApp;
    }

    private Map<String, Integer> submissionCountPerApp() {
        if (isEmpty(this.submissionCountPerApp)) {
            this.submissionCountPerApp = new HashMap<>();
        }

        return this.submissionCountPerApp;
    }

    public int getSmsSentCountForCurrentMonth() {
        return smsSentCount.getSmsSentCountForCurrentMonth();
    }

    public void increaseSmsSentCountForCurrentMonth() {
        smsSentCount.increaseSentCountForCurrentMonth();
    }

    private static class SmsSentCount {
        private static final DateTimeFormatter MONTH_FORMATTER = ofPattern("yyyy-MM").withZone(systemDefault());
        private String month;
        private int count;

        private int getSmsSentCountForCurrentMonth() {
            return isAtCurrentMonth() ? count : 0;
        }

        private void increaseSentCountForCurrentMonth() {
            if (isAtCurrentMonth()) {
                count++;
            } else {
                this.month = currentMonth();
                this.count = 1;
            }
        }

        private boolean isAtCurrentMonth() {
            return Objects.equals(month, currentMonth());
        }

        private String currentMonth() {
            return MONTH_FORMATTER.format(now());
        }
    }
}
