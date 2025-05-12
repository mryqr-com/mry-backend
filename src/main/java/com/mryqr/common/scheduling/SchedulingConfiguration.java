package com.mryqr.common.scheduling;

import com.mryqr.common.event.DomainEventJobs;
import com.mryqr.common.wx.accesstoken.WxAccessTokenService;
import com.mryqr.common.wx.jssdk.WxJsSdkService;
import com.mryqr.core.assignment.job.CreateAssignmentsJob;
import com.mryqr.core.assignment.job.ExpireAssignmentsJob;
import com.mryqr.core.assignment.job.NearExpireAssignmentsJob;
import com.mryqr.core.qr.job.RemoveQrRangedAttributeValuesForAllTenantsJob;
import com.mryqr.core.tenant.job.CountStorageForAllTenantJob;
import com.mryqr.management.operation.OperationalStatisticsJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.THIS_MONTH;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.THIS_SEASON;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.THIS_WEEK;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.THIS_YEAR;
import static java.time.LocalDateTime.now;

@Slf4j
@Profile("!ci")
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@EnableSchedulerLock(defaultLockAtMostFor = "60m", defaultLockAtLeastFor = "10s")
public class SchedulingConfiguration {
    private final RemoveQrRangedAttributeValuesForAllTenantsJob removeQrRangedAttributeValuesForAllTenantsJob;
    private final WxAccessTokenService wxAccessTokenService;
    private final WxJsSdkService wxJsSdkService;
    private final DomainEventJobs domainEventJobs;
    private final CountStorageForAllTenantJob countStorageForAllTenantJob;
    private final OperationalStatisticsJob operationalStatisticsJob;
    private final CreateAssignmentsJob createAssignmentsJob;
    private final ExpireAssignmentsJob expireAssignmentsJob;
    private final NearExpireAssignmentsJob nearExpireAssignmentsJob;

    //定时任务尽量放到前半个小时运行，以将后半个多小时留给部署时间

    //兜底发送尚未发送的事件，每2分钟运行，不能用@SchedulerLock，因为publishDomainEvents本身有分布式锁
    @Scheduled(cron = "0 */2 * * * ?")
    public void houseKeepPublishDomainEvent() {
        int count = domainEventJobs.publishDomainEvents();
        if (count > 0) {
            log.info("House keep published {} domain events.", count);
        }
    }

    //刷新微信access token，每29分钟运行
    @Scheduled(cron = "0 */29 * * * ?")
    @SchedulerLock(name = "refreshWxTokens", lockAtMostFor = "2m", lockAtLeastFor = "1m")
    public void refreshWxTokens() {
        try {
            wxAccessTokenService.refreshAccessToken();
        } catch (Throwable t) {
            log.error("Failed to refresh wx access token.", t);
        }

        try {
            wxJsSdkService.refreshJsApiTicket();
        } catch (Throwable t) {
            log.error("Failed to refresh wx JS API ticket.", t);
        }
    }

    //根据AssignmentPlan创建Assignment，每小时第1分钟运行，不能整点，因为整点有可能会被计算成上一小时
    @Scheduled(cron = "0 1 */1 * * ?")
    @SchedulerLock(name = "createAssignments", lockAtMostFor = "40m", lockAtLeastFor = "1m")
    public void createAssignments() {
        createAssignmentsJob.run(now());
    }

    //检查超期的Assignment，每小时运行的第5分钟运行，为了和createAssignments和nearExpireAssignments错开
    @Scheduled(cron = "0 4 */1 * * ?")
    @SchedulerLock(name = "expireAssignments", lockAtMostFor = "40m", lockAtLeastFor = "1m")
    public void expireAssignments() {
        expireAssignmentsJob.run(now());
    }

    //检查即将超期的Assignment，每小时运行的第8分钟运行，为了和createAssignments和expireAssignments错开
    @Scheduled(cron = "0 8 */1 * * ?")
    @SchedulerLock(name = "nearExpireAssignments", lockAtMostFor = "40m", lockAtLeastFor = "1m")
    public void nearExpireAssignments() {
        nearExpireAssignmentsJob.run(now());
    }

    //统计每个tenant的存储用量，每4个小时的第15分钟运行一次
    @Scheduled(cron = "0 15 */4 * * ?")
    @SchedulerLock(name = "countStoragesForAllTenants", lockAtMostFor = "60m", lockAtLeastFor = "1m")
    public void countStoragesForAllTenants() {
        countStorageForAllTenantJob.run();
    }

    //每周开始重置所有按周统计的属性，每周一1点10分运行
    @Scheduled(cron = "0 10 1 ? * MON")
    @SchedulerLock(name = "resetAllWeeklyRangedAttributes", lockAtMostFor = "120m", lockAtLeastFor = "1m")
    public void resetAllWeeklyRangedAttributes() {
        removeQrRangedAttributeValuesForAllTenantsJob.run(THIS_WEEK);
    }

    //每月开始重置所有按周统计的属性，每月第一天2点10分运行
    @Scheduled(cron = "0 10 2 1 * ?")
    @SchedulerLock(name = "resetAllMonthlyRangedAttributes", lockAtMostFor = "120m", lockAtLeastFor = "1m")
    public void resetAllMonthlyRangedAttributes() {
        removeQrRangedAttributeValuesForAllTenantsJob.run(THIS_MONTH);
    }

    //每季度开始重置所有按周统计的属性，每季度第一天3点10分运行
    @Scheduled(cron = "0 10 3 1 1,4,7,10 ?")
    @SchedulerLock(name = "resetAllSeasonlyRangedAttributes", lockAtMostFor = "120m", lockAtLeastFor = "1m")
    public void resetAllSeasonlyRangedAttributes() {
        removeQrRangedAttributeValuesForAllTenantsJob.run(THIS_SEASON);
    }

    //每年开始重置所有按周统计的属性，每年第一天4点10分运行
    @Scheduled(cron = "0 10 4 1 1 ?")
    @SchedulerLock(name = "resetAllYearlyRangedAttributes", lockAtMostFor = "120m", lockAtLeastFor = "1m")
    public void resetAllYearlyRangedAttributes() {
        removeQrRangedAttributeValuesForAllTenantsJob.run(THIS_YEAR);
    }

    //删除老的领域事件，包含mongo和redis，每天5点10分运行
    @Scheduled(cron = "0 10 5 * * ?")
    @SchedulerLock(name = "removeOldEvents", lockAtMostFor = "30m", lockAtLeastFor = "1m")
    public void removeOldEvents() {
        try {
            domainEventJobs.removeOldDomainEventsFromMongo(100);
        } catch (Throwable t) {
            log.error("Failed remove old domain events from mongo.", t);
        }

        try {
            domainEventJobs.removeOldDomainEventsFromRedis(1000000, true);
        } catch (Throwable t) {
            log.error("Failed remove old domain events from redis.", t);
        }

        try {
            domainEventJobs.removeOldWebhookEventsFromRedis(1000000, true);
        } catch (Throwable t) {
            log.error("Failed remove old webhook events from redis.", t);
        }

        try {
            domainEventJobs.removeOldNotificationEventsFromRedis(500000, true);
        } catch (Throwable t) {
            log.error("Failed remove old notification events from redis.", t);
        }
    }

    //统计整体运营数据，每天6点10分运行
    @Scheduled(cron = "0 10 6 * * ?")
    @SchedulerLock(name = "operationalStatistics", lockAtMostFor = "30m", lockAtLeastFor = "1m")
    public void operationalStatistics() {
        operationalStatisticsJob.run();
    }

    @Bean
    public SchedulingTaskExecutor threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(10);
        return threadPoolTaskScheduler;
    }

}
