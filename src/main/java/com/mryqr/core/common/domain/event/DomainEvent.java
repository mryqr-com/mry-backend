package com.mryqr.core.common.domain.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mryqr.core.app.domain.event.AppAttributesCreatedEvent;
import com.mryqr.core.app.domain.event.AppAttributesDeletedEvent;
import com.mryqr.core.app.domain.event.AppControlOptionsDeletedEvent;
import com.mryqr.core.app.domain.event.AppControlsDeletedEvent;
import com.mryqr.core.app.domain.event.AppCreatedEvent;
import com.mryqr.core.app.domain.event.AppCreatedFromTemplateEvent;
import com.mryqr.core.app.domain.event.AppDeletedEvent;
import com.mryqr.core.app.domain.event.AppPagesDeletedEvent;
import com.mryqr.core.app.domain.event.GroupSyncEnabledEvent;
import com.mryqr.core.app.domain.event.PageChangedToSubmitPerInstanceEvent;
import com.mryqr.core.app.domain.event.PageChangedToSubmitPerMemberEvent;
import com.mryqr.core.assignment.event.AssignmentCreatedEvent;
import com.mryqr.core.assignmentplan.domain.event.AssignmentPlanDeletedEvent;
import com.mryqr.core.common.domain.AggregateRoot;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.department.domain.event.DepartmentCreatedEvent;
import com.mryqr.core.department.domain.event.DepartmentDeletedEvent;
import com.mryqr.core.department.domain.event.DepartmentManagersChangedEvent;
import com.mryqr.core.department.domain.event.DepartmentRenamedEvent;
import com.mryqr.core.departmenthierarchy.domain.event.DepartmentHierarchyChangedEvent;
import com.mryqr.core.group.domain.event.GroupActivatedEvent;
import com.mryqr.core.group.domain.event.GroupCreatedEvent;
import com.mryqr.core.group.domain.event.GroupDeactivatedEvent;
import com.mryqr.core.group.domain.event.GroupDeletedEvent;
import com.mryqr.core.group.domain.event.GroupManagersChangedEvent;
import com.mryqr.core.member.domain.event.MemberAddedToDepartmentEvent;
import com.mryqr.core.member.domain.event.MemberCreatedEvent;
import com.mryqr.core.member.domain.event.MemberDeletedEvent;
import com.mryqr.core.member.domain.event.MemberDepartmentsChangedEvent;
import com.mryqr.core.member.domain.event.MemberNameChangedEvent;
import com.mryqr.core.member.domain.event.MemberRemovedFromDepartmentEvent;
import com.mryqr.core.order.domain.event.OrderBankTransferUpdatedEvent;
import com.mryqr.core.order.domain.event.OrderCreatedEvent;
import com.mryqr.core.order.domain.event.OrderDeliveryUpdatedEvent;
import com.mryqr.core.order.domain.event.OrderInvoiceIssuedEvent;
import com.mryqr.core.order.domain.event.OrderInvoiceRequestedEvent;
import com.mryqr.core.order.domain.event.OrderRefundUpdatedEvent;
import com.mryqr.core.order.domain.event.OrderWxPayUpdatedEvent;
import com.mryqr.core.order.domain.event.OrderWxTransferUpdatedEvent;
import com.mryqr.core.plate.domain.event.PlateBoundEvent;
import com.mryqr.core.plate.domain.event.PlateUnboundEvent;
import com.mryqr.core.platebatch.domain.event.PlateBatchCreatedEvent;
import com.mryqr.core.platebatch.domain.event.PlateBatchDeletedEvent;
import com.mryqr.core.qr.domain.QrCreatedEvent;
import com.mryqr.core.qr.domain.event.QrActivatedEvent;
import com.mryqr.core.qr.domain.event.QrAttributesUpdatedEvent;
import com.mryqr.core.qr.domain.event.QrBaseSettingUpdatedEvent;
import com.mryqr.core.qr.domain.event.QrCirculationStatusChangedEvent;
import com.mryqr.core.qr.domain.event.QrCustomIdUpdatedEvent;
import com.mryqr.core.qr.domain.event.QrDeactivatedEvent;
import com.mryqr.core.qr.domain.event.QrDeletedEvent;
import com.mryqr.core.qr.domain.event.QrDescriptionUpdatedEvent;
import com.mryqr.core.qr.domain.event.QrGeolocationUpdatedEvent;
import com.mryqr.core.qr.domain.event.QrGroupChangedEvent;
import com.mryqr.core.qr.domain.event.QrHeaderImageUpdatedEvent;
import com.mryqr.core.qr.domain.event.QrMarkedAsTemplateEvent;
import com.mryqr.core.qr.domain.event.QrPlateResetEvent;
import com.mryqr.core.qr.domain.event.QrRenamedEvent;
import com.mryqr.core.qr.domain.event.QrUnMarkedAsTemplateEvent;
import com.mryqr.core.submission.domain.event.SubmissionApprovedEvent;
import com.mryqr.core.submission.domain.event.SubmissionCreatedEvent;
import com.mryqr.core.submission.domain.event.SubmissionDeletedEvent;
import com.mryqr.core.submission.domain.event.SubmissionUpdatedEvent;
import com.mryqr.core.tenant.domain.event.TenantActivatedEvent;
import com.mryqr.core.tenant.domain.event.TenantBaseSettingUpdatedEvent;
import com.mryqr.core.tenant.domain.event.TenantCreatedEvent;
import com.mryqr.core.tenant.domain.event.TenantDeactivatedEvent;
import com.mryqr.core.tenant.domain.event.TenantInvoiceTitleUpdatedEvent;
import com.mryqr.core.tenant.domain.event.TenantOrderAppliedEvent;
import com.mryqr.core.tenant.domain.event.TenantPlanUpdatedEvent;
import com.mryqr.core.tenant.domain.event.TenantResourceUsageUpdatedEvent;
import com.mryqr.core.tenant.domain.event.TenantSubdomainReadyStatusUpdatedEvent;
import com.mryqr.core.tenant.domain.event.TenantSubdomainUpdatedEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

import static com.mryqr.core.common.utils.MryConstants.EVENT_COLLECTION;
import static com.mryqr.core.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static java.time.Instant.now;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = AppAttributesCreatedEvent.class, name = "ATTRIBUTES_CREATED"),
        @JsonSubTypes.Type(value = AppAttributesDeletedEvent.class, name = "ATTRIBUTES_DELETED"),
        @JsonSubTypes.Type(value = AppControlOptionsDeletedEvent.class, name = "CONTROL_OPTIONS_DELETED"),
        @JsonSubTypes.Type(value = AppControlsDeletedEvent.class, name = "CONTROLS_DELETED"),
        @JsonSubTypes.Type(value = AppCreatedEvent.class, name = "APP_CREATED"),
        @JsonSubTypes.Type(value = AppCreatedFromTemplateEvent.class, name = "APP_CREATED_FROM_TEMPLATE"),
        @JsonSubTypes.Type(value = AppDeletedEvent.class, name = "APP_DELETED"),
        @JsonSubTypes.Type(value = AppPagesDeletedEvent.class, name = "PAGES_DELETED"),
        @JsonSubTypes.Type(value = GroupCreatedEvent.class, name = "GROUP_CREATED"),
        @JsonSubTypes.Type(value = GroupDeactivatedEvent.class, name = "GROUP_DEACTIVATED"),
        @JsonSubTypes.Type(value = GroupActivatedEvent.class, name = "GROUP_ACTIVATED"),
        @JsonSubTypes.Type(value = GroupDeletedEvent.class, name = "GROUP_DELETED"),
        @JsonSubTypes.Type(value = GroupManagersChangedEvent.class, name = "GROUP_MANAGERS_CHANGED"),
        @JsonSubTypes.Type(value = MemberCreatedEvent.class, name = "MEMBER_CREATED"),
        @JsonSubTypes.Type(value = MemberDeletedEvent.class, name = "MEMBER_DELETED"),
        @JsonSubTypes.Type(value = MemberNameChangedEvent.class, name = "MEMBER_NAME_CHANGED"),
        @JsonSubTypes.Type(value = MemberDepartmentsChangedEvent.class, name = "MEMBER_DEPARTMENTS_CHANGED"),
        @JsonSubTypes.Type(value = PageChangedToSubmitPerInstanceEvent.class, name = "PAGE_CHANGED_TO_SUBMIT_PER_INSTANCE"),
        @JsonSubTypes.Type(value = PageChangedToSubmitPerMemberEvent.class, name = "PAGE_CHANGED_TO_SUBMIT_PER_MEMBER"),
        @JsonSubTypes.Type(value = PlateBatchCreatedEvent.class, name = "PLATE_BATCH_CREATED"),
        @JsonSubTypes.Type(value = PlateBatchDeletedEvent.class, name = "PLATE_BATCH_DELETED"),
        @JsonSubTypes.Type(value = PlateBoundEvent.class, name = "PLATE_BOUND"),
        @JsonSubTypes.Type(value = PlateUnboundEvent.class, name = "PLATE_UNBOUND"),
        @JsonSubTypes.Type(value = QrBaseSettingUpdatedEvent.class, name = "QR_BASE_SETTING_UPDATED"),
        @JsonSubTypes.Type(value = QrCreatedEvent.class, name = "QR_CREATED"),
        @JsonSubTypes.Type(value = QrDeletedEvent.class, name = "QR_DELETED"),
        @JsonSubTypes.Type(value = QrGroupChangedEvent.class, name = "QR_GROUP_CHANGED"),
        @JsonSubTypes.Type(value = QrMarkedAsTemplateEvent.class, name = "QR_MARKED_AS_TEMPLATE"),
        @JsonSubTypes.Type(value = QrPlateResetEvent.class, name = "QR_PLATE_RESET"),
        @JsonSubTypes.Type(value = QrRenamedEvent.class, name = "QR_RENAMED"),
        @JsonSubTypes.Type(value = SubmissionCreatedEvent.class, name = "SUBMISSION_CREATED"),
        @JsonSubTypes.Type(value = SubmissionDeletedEvent.class, name = "SUBMISSION_DELETED"),
        @JsonSubTypes.Type(value = SubmissionUpdatedEvent.class, name = "SUBMISSION_UPDATED"),
        @JsonSubTypes.Type(value = SubmissionApprovedEvent.class, name = "SUBMISSION_APPROVED"),
        @JsonSubTypes.Type(value = TenantCreatedEvent.class, name = "TENANT_CREATED"),
        @JsonSubTypes.Type(value = TenantSubdomainUpdatedEvent.class, name = "TENANT_SUBDOMAIN_UPDATED"),
        @JsonSubTypes.Type(value = TenantActivatedEvent.class, name = "TENANT_ACTIVATED"),
        @JsonSubTypes.Type(value = TenantDeactivatedEvent.class, name = "TENANT_DEACTIVATED"),
        @JsonSubTypes.Type(value = TenantBaseSettingUpdatedEvent.class, name = "TENANT_BASE_SETTING_UPDATED"),
        @JsonSubTypes.Type(value = TenantInvoiceTitleUpdatedEvent.class, name = "TENANT_INVOICE_TITLE_UPDATED"),
        @JsonSubTypes.Type(value = TenantOrderAppliedEvent.class, name = "TENANT_ORDER_APPLIED"),
        @JsonSubTypes.Type(value = TenantPlanUpdatedEvent.class, name = "TENANT_PLAN_UPDATED"),
        @JsonSubTypes.Type(value = TenantSubdomainReadyStatusUpdatedEvent.class, name = "TENANT_SUBDOMAIN_READY_STATUS_UPDATED"),
        @JsonSubTypes.Type(value = TenantResourceUsageUpdatedEvent.class, name = "TENANT_RESOURCE_USAGE_UPDATED"),
        @JsonSubTypes.Type(value = QrCustomIdUpdatedEvent.class, name = "QR_CUSTOM_ID_UPDATED"),
        @JsonSubTypes.Type(value = QrUnMarkedAsTemplateEvent.class, name = "QR_UNMARKED_AS_TEMPLATE"),
        @JsonSubTypes.Type(value = QrAttributesUpdatedEvent.class, name = "QR_ATTRIBUTES_UPDATED"),
        @JsonSubTypes.Type(value = QrDescriptionUpdatedEvent.class, name = "QR_DESCRIPTION_UPDATED"),
        @JsonSubTypes.Type(value = QrGeolocationUpdatedEvent.class, name = "QR_GEOLOCATION_UPDATED"),
        @JsonSubTypes.Type(value = QrHeaderImageUpdatedEvent.class, name = "QR_HEADER_IMAGE_UPDATED"),
        @JsonSubTypes.Type(value = QrActivatedEvent.class, name = "QR_ACTIVATED"),
        @JsonSubTypes.Type(value = QrDeactivatedEvent.class, name = "QR_DEACTIVATED"),
        @JsonSubTypes.Type(value = QrCirculationStatusChangedEvent.class, name = "QR_CIRCULATION_STATUS_CHANGED"),
        @JsonSubTypes.Type(value = OrderCreatedEvent.class, name = "ORDER_CREATED"),
        @JsonSubTypes.Type(value = OrderBankTransferUpdatedEvent.class, name = "ORDER_BANK_TRANSFER_UPDATED"),
        @JsonSubTypes.Type(value = OrderWxTransferUpdatedEvent.class, name = "ORDER_WX_TRANSFER_UPDATED"),
        @JsonSubTypes.Type(value = OrderDeliveryUpdatedEvent.class, name = "ORDER_DELIVERY_UPDATED"),
        @JsonSubTypes.Type(value = OrderInvoiceIssuedEvent.class, name = "ORDER_INVOICE_ISSUED"),
        @JsonSubTypes.Type(value = OrderInvoiceRequestedEvent.class, name = "ORDER_INVOICE_REQUESTED"),
        @JsonSubTypes.Type(value = OrderRefundUpdatedEvent.class, name = "ORDER_REFUND_UPDATED"),
        @JsonSubTypes.Type(value = OrderWxPayUpdatedEvent.class, name = "ORDER_WX_PAY_UPDATED"),
        @JsonSubTypes.Type(value = AssignmentPlanDeletedEvent.class, name = "ASSIGNMENT_PLAN_DELETED"),
        @JsonSubTypes.Type(value = DepartmentCreatedEvent.class, name = "DEPARTMENT_CREATED"),
        @JsonSubTypes.Type(value = DepartmentDeletedEvent.class, name = "DEPARTMENT_DELETED"),
        @JsonSubTypes.Type(value = DepartmentManagersChangedEvent.class, name = "DEPARTMENT_MANAGERS_CHANGED"),
        @JsonSubTypes.Type(value = DepartmentRenamedEvent.class, name = "DEPARTMENT_RENAMED"),
        @JsonSubTypes.Type(value = MemberAddedToDepartmentEvent.class, name = "MEMBER_ADDED_TO_DEPARTMENT"),
        @JsonSubTypes.Type(value = MemberRemovedFromDepartmentEvent.class, name = "MEMBER_REMOVED_FROM_DEPARTMENT"),
        @JsonSubTypes.Type(value = GroupSyncEnabledEvent.class, name = "GROUP_SYNC_ENABLED"),
        @JsonSubTypes.Type(value = DepartmentHierarchyChangedEvent.class, name = "DEPARTMENT_HIERARCHY_CHANGED"),
        @JsonSubTypes.Type(value = AssignmentCreatedEvent.class, name = "ASSIGNMENT_CREATED"),
})

//DomainEvent既要保证能支持MongoDB的序列化/反序列化，有要能够通过Jackson序列化/反序列化（因为要发送到Redis）
@Getter
@Document(EVENT_COLLECTION)
@NoArgsConstructor(access = PROTECTED)
public abstract class DomainEvent {
    private String id;//事件ID，不能为空
    private String arTenantId;//事件对应的租户ID，不能为空
    private String arId;//事件对应的聚合根ID，不能为空
    private DomainEventType type;//事件类型
    private DomainEventStatus status;//状态
    private int publishedCount;//已经发布的次数，无论成功与否
    private int consumedCount;//已经被消费的次数，无论成功与否
    private String raisedBy;//引发该事件的memberId
    private Instant raisedAt;//事件产生时间

    protected DomainEvent(DomainEventType type, User user) {
        requireNonNull(type, "Domain event type must not be null.");
        requireNonNull(user, "User must not be null.");

        this.id = newEventId();
        this.type = type;
        this.status = DomainEventStatus.CREATED;
        this.publishedCount = 0;
        this.consumedCount = 0;
        this.raisedBy = user.getMemberId();
        this.raisedAt = now();
    }

    public String newEventId() {
        return "EVT" + newSnowflakeId();
    }

    public void setArInfo(AggregateRoot ar) {
        this.arTenantId = ar.getTenantId();
        this.arId = ar.getId();
    }

    public boolean isConsumedBefore() {
        return this.consumedCount > 0;
    }

    public boolean isNotConsumedBefore() {
        return !isConsumedBefore();
    }

    public boolean isRaisedByHuman() {
        return isNotBlank(raisedBy);
    }

}
