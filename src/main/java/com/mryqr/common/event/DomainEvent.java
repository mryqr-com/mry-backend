package com.mryqr.common.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mryqr.common.domain.AggregateRoot;
import com.mryqr.common.domain.AggregateRootType;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.domain.user.UserChannel;
import com.mryqr.core.app.domain.event.*;
import com.mryqr.core.assignment.event.AssignmentCreatedEvent;
import com.mryqr.core.assignment.event.AssignmentFailedEvent;
import com.mryqr.core.assignment.event.AssignmentFinishedEvent;
import com.mryqr.core.assignment.event.AssignmentNearExpiredEvent;
import com.mryqr.core.assignmentplan.domain.event.AssignmentPlanDeletedEvent;
import com.mryqr.core.department.domain.event.DepartmentCreatedEvent;
import com.mryqr.core.department.domain.event.DepartmentDeletedEvent;
import com.mryqr.core.department.domain.event.DepartmentManagersChangedEvent;
import com.mryqr.core.department.domain.event.DepartmentRenamedEvent;
import com.mryqr.core.departmenthierarchy.domain.event.DepartmentHierarchyChangedEvent;
import com.mryqr.core.group.domain.event.*;
import com.mryqr.core.member.domain.event.*;
import com.mryqr.core.order.domain.event.*;
import com.mryqr.core.plate.domain.event.PlateBoundEvent;
import com.mryqr.core.plate.domain.event.PlateUnboundEvent;
import com.mryqr.core.platebatch.domain.event.PlateBatchCreatedEvent;
import com.mryqr.core.platebatch.domain.event.PlateBatchDeletedEvent;
import com.mryqr.core.qr.domain.QrCreatedEvent;
import com.mryqr.core.qr.domain.event.*;
import com.mryqr.core.submission.domain.event.SubmissionApprovedEvent;
import com.mryqr.core.submission.domain.event.SubmissionCreatedEvent;
import com.mryqr.core.submission.domain.event.SubmissionDeletedEvent;
import com.mryqr.core.submission.domain.event.SubmissionUpdatedEvent;
import com.mryqr.core.tenant.domain.event.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

import static com.mryqr.common.domain.user.UserChannel.HUMAN;
import static com.mryqr.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static java.time.Instant.now;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PROTECTED;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = AppAttributesCreatedEvent.class, name = "APP_ATTRIBUTES_CREATED"),
        @JsonSubTypes.Type(value = AppAttributesDeletedEvent.class, name = "APP_ATTRIBUTES_DELETED"),
        @JsonSubTypes.Type(value = AppControlOptionsDeletedEvent.class, name = "APP_CONTROL_OPTIONS_DELETED"),
        @JsonSubTypes.Type(value = AppControlsDeletedEvent.class, name = "APP_CONTROLS_DELETED"),
        @JsonSubTypes.Type(value = AppCreatedEvent.class, name = "APP_CREATED"),
        @JsonSubTypes.Type(value = AppCreatedFromTemplateEvent.class, name = "APP_CREATED_FROM_TEMPLATE"),
        @JsonSubTypes.Type(value = AppDeletedEvent.class, name = "APP_DELETED"),
        @JsonSubTypes.Type(value = AppPagesDeletedEvent.class, name = "APP_PAGES_DELETED"),
        @JsonSubTypes.Type(value = GroupCreatedEvent.class, name = "GROUP_CREATED"),
        @JsonSubTypes.Type(value = GroupDeactivatedEvent.class, name = "GROUP_DEACTIVATED"),
        @JsonSubTypes.Type(value = GroupActivatedEvent.class, name = "GROUP_ACTIVATED"),
        @JsonSubTypes.Type(value = GroupDeletedEvent.class, name = "GROUP_DELETED"),
        @JsonSubTypes.Type(value = GroupManagersChangedEvent.class, name = "GROUP_MANAGERS_CHANGED"),
        @JsonSubTypes.Type(value = MemberCreatedEvent.class, name = "MEMBER_CREATED"),
        @JsonSubTypes.Type(value = MemberDeletedEvent.class, name = "MEMBER_DELETED"),
        @JsonSubTypes.Type(value = MemberNameChangedEvent.class, name = "MEMBER_NAME_CHANGED"),
        @JsonSubTypes.Type(value = MemberDepartmentsChangedEvent.class, name = "MEMBER_DEPARTMENTS_CHANGED"),
        @JsonSubTypes.Type(value = AppPageChangedToSubmitPerInstanceEvent.class, name = "APP_PAGE_CHANGED_TO_SUBMIT_PER_INSTANCE"),
        @JsonSubTypes.Type(value = AppPageChangedToSubmitPerMemberEvent.class, name = "APP_PAGE_CHANGED_TO_SUBMIT_PER_MEMBER"),
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
        @JsonSubTypes.Type(value = TenantActivatedEvent.class, name = "TENANT_ACTIVATED"),
        @JsonSubTypes.Type(value = TenantDeactivatedEvent.class, name = "TENANT_DEACTIVATED"),
        @JsonSubTypes.Type(value = TenantBaseSettingUpdatedEvent.class, name = "TENANT_BASE_SETTING_UPDATED"),
        @JsonSubTypes.Type(value = TenantInvoiceTitleUpdatedEvent.class, name = "TENANT_INVOICE_TITLE_UPDATED"),
        @JsonSubTypes.Type(value = TenantOrderAppliedEvent.class, name = "TENANT_ORDER_APPLIED"),
        @JsonSubTypes.Type(value = TenantPlanUpdatedEvent.class, name = "TENANT_PLAN_UPDATED"),
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
        @JsonSubTypes.Type(value = AppGroupSyncEnabledEvent.class, name = "APP_GROUP_SYNC_ENABLED"),
        @JsonSubTypes.Type(value = DepartmentHierarchyChangedEvent.class, name = "DEPARTMENT_HIERARCHY_CHANGED"),
        @JsonSubTypes.Type(value = AssignmentCreatedEvent.class, name = "ASSIGNMENT_CREATED"),
        @JsonSubTypes.Type(value = AssignmentFinishedEvent.class, name = "ASSIGNMENT_FINISHED"),
        @JsonSubTypes.Type(value = AssignmentFailedEvent.class, name = "ASSIGNMENT_FAILED"),
        @JsonSubTypes.Type(value = AssignmentNearExpiredEvent.class, name = "ASSIGNMENT_NEAR_EXPIRED"),
})

//DomainEvent既要保证能支持MongoDB的序列化/反序列化，有要能够通过Jackson序列化/反序列化（因为要发送到Redis）
@Getter
@FieldNameConstants
@NoArgsConstructor(access = PROTECTED)
public abstract class DomainEvent {
    private String id;//事件ID，不能为空
    private String arTenantId;//事件对应的租户ID，不能为空
    private String arId;//事件对应的聚合根ID，不能为空
    private DomainEventType type;//事件类型
    private AggregateRootType arType; //事件来自哪种聚合根
    private String raisedBy;//引发该事件的memberId
    private Instant raisedAt;//事件产生时间
    private UserChannel userChannel;//引发该事件的渠道

    protected DomainEvent(DomainEventType type, User user) {
        requireNonNull(type, "Domain event type must not be null.");
        requireNonNull(user, "User must not be null.");

        this.id = newEventId();
        this.type = type;
        this.arType = type.getArType();
        this.raisedBy = user.getMemberId();
        this.raisedAt = now();
        this.userChannel = user.getChannel();
    }

    public String newEventId() {
        return "EVT" + newSnowflakeId();
    }

    public void setArInfo(AggregateRoot ar) {
        this.arTenantId = ar.getTenantId();
        this.arId = ar.getId();
    }

    public boolean isFromHumanChannel() {
        return this.userChannel == HUMAN;
    }

}
