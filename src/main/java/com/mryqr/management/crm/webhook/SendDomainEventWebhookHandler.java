package com.mryqr.management.crm.webhook;

import com.mryqr.common.event.publish.RedisDomainEventSender;
import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionCreatedWebhookPayload;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.date.DateAnswer;
import com.mryqr.core.submission.domain.answer.radio.RadioAnswer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.submission.domain.answer.time.TimeAnswer;
import com.mryqr.core.submission.domain.event.SubmissionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.mryqr.management.crm.MryTenantManageApp.MRY_TENANT_MANAGE_APP_ID;
import static com.mryqr.management.crm.MryTenantManageApp.SEND_EVENT_ALL_TENANT_CONTROL_ID;
import static com.mryqr.management.crm.MryTenantManageApp.SEND_EVENT_ALL_TENANT_YES_OPTION_ID;
import static com.mryqr.management.crm.MryTenantManageApp.SEND_EVENT_APP_CONTROL_ID;
import static com.mryqr.management.crm.MryTenantManageApp.SEND_EVENT_END_DATE_CONTROL_ID;
import static com.mryqr.management.crm.MryTenantManageApp.SEND_EVENT_END_TIME_CONTROL_ID;
import static com.mryqr.management.crm.MryTenantManageApp.SEND_EVENT_PAGE_ID;
import static com.mryqr.management.crm.MryTenantManageApp.SEND_EVENT_START_DATE_CONTROL_ID;
import static com.mryqr.management.crm.MryTenantManageApp.SEND_EVENT_START_TIME_CONTROL_ID;
import static java.time.ZoneId.systemDefault;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendDomainEventWebhookHandler implements TenantWebhookHandler {
    private final TaskExecutor taskExecutor;
    private final MongoTemplate mongoTemplate;
    private final RedisDomainEventSender redisDomainEventSender;

    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload instanceof SubmissionCreatedWebhookPayload && payload.getPageId().equals(SEND_EVENT_PAGE_ID);
    }

    @Override
    public void handle(BaseSubmissionWebhookPayload payload) {
        String tenantId = payload.getQrCustomId();
        Map<String, Answer> answers = payload.allAnswers();
        DateAnswer sendEventStartDateAnswer = (DateAnswer) answers.get(SEND_EVENT_START_DATE_CONTROL_ID);
        TimeAnswer sendEventStartTimeAnswer = (TimeAnswer) answers.get(SEND_EVENT_START_TIME_CONTROL_ID);
        DateAnswer sendEventEndDateAnswer = (DateAnswer) answers.get(SEND_EVENT_END_DATE_CONTROL_ID);
        TimeAnswer sendEventEndTimeAnswer = (TimeAnswer) answers.get(SEND_EVENT_END_TIME_CONTROL_ID);
        RadioAnswer sendEventAllTenantAnswer = (RadioAnswer) answers.get(SEND_EVENT_ALL_TENANT_CONTROL_ID);
        SingleLineTextAnswer appIdAnswer = (SingleLineTextAnswer) answers.get(SEND_EVENT_APP_CONTROL_ID);

        Instant startedAt = instantOf(sendEventStartDateAnswer.getDate(), sendEventStartTimeAnswer.getTime());
        Instant endAt = instantOf(sendEventEndDateAnswer.getDate(), sendEventEndTimeAnswer.getTime());
        boolean isAllTenant = sendEventAllTenantAnswer != null && sendEventAllTenantAnswer.getOptionId().equals(SEND_EVENT_ALL_TENANT_YES_OPTION_ID);
        String appId = appIdAnswer != null ? appIdAnswer.getContent() : null;
        taskExecutor.execute(() -> sendEvent(startedAt, endAt, appId, isAllTenant ? null : tenantId));
    }

    private void sendEvent(Instant startedAt, Instant endAt, String appId, String tenantId) {
        int count = 0;
        String startEventId = "EVT00000000000000001";

        while (true) {
            Criteria criteria = where("raisedAt").gte(startedAt).lte(endAt).and("_id").gt(startEventId);
            if (isNotBlank(appId)) {
                criteria.and("appId").is(appId);
            }

            if (isNotBlank(tenantId)) {
                criteria.and("arTenantId").is(tenantId);
            }

            Query query = Query.query(criteria).with(by(ASC, "raisedAt")).limit(200);
            List<DomainEvent> domainEvents = mongoTemplate.find(query, DomainEvent.class);

            if (isEmpty(domainEvents)) {
                break;
            }

            for (DomainEvent event : domainEvents) {
                if (!(event instanceof SubmissionCreatedEvent submissionCreatedEvent &&//排除触发发送事件的提交本身，否则将导致无限循环
                        Objects.equals(submissionCreatedEvent.getAppId(), MRY_TENANT_MANAGE_APP_ID) &&
                        Objects.equals(submissionCreatedEvent.getPageId(), SEND_EVENT_PAGE_ID))) {
                    redisDomainEventSender.send(event);
                }
            }

            count = domainEvents.size() + count;
            startEventId = domainEvents.get(domainEvents.size() - 1).getId();
        }

        if (isNotBlank(appId)) {
            log.info("Sent {} domain events from {} to {} for app[{}].", count, startedAt, endAt, appId);
        } else if (isNotBlank(tenantId)) {
            log.info("Sent {} domain events from {} to {} for tenant[{}].", count, startedAt, endAt, tenantId);
        } else {
            log.info("Sent {} domain events from {} to {} for all tenants.", count, startedAt, endAt);
        }
    }

    private Instant instantOf(String dateString, String timeString) {
        LocalDate localDate = LocalDate.parse(dateString);
        LocalTime localTime = LocalTime.parse(timeString);
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        return localDateTime.atZone(systemDefault()).toInstant();
    }
}
