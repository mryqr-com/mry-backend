package com.mryqr.core.order.domain.task;

import com.mryqr.common.domain.permission.Permission;
import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.*;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.order.domain.Order;
import com.mryqr.core.order.domain.OrderRepository;
import com.mryqr.core.order.domain.invoice.Invoice;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.qr.domain.PlatedQr;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrFactory;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionFactory;
import com.mryqr.core.submission.domain.SubmissionRepository;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.dropdown.DropdownAnswer;
import com.mryqr.core.submission.domain.answer.email.EmailAnswer;
import com.mryqr.core.submission.domain.answer.fileupload.FileUploadAnswer;
import com.mryqr.core.submission.domain.answer.identifier.IdentifierAnswer;
import com.mryqr.core.submission.domain.answer.itemstatus.ItemStatusAnswer;
import com.mryqr.core.submission.domain.answer.mobilenumber.MobileNumberAnswer;
import com.mryqr.core.submission.domain.answer.multilinetext.MultiLineTextAnswer;
import com.mryqr.core.submission.domain.answer.numberinput.NumberInputAnswer;
import com.mryqr.core.submission.domain.answer.personname.PersonNameAnswer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.mryqr.common.domain.user.User.NOUSER;
import static com.mryqr.common.utils.MryConstants.MRY_DATE_TIME_FORMATTER;
import static com.mryqr.core.order.domain.PaymentType.BANK_TRANSFER;
import static com.mryqr.core.order.domain.PaymentType.WX_NATIVE;
import static com.mryqr.core.order.domain.detail.OrderDetailType.PLATE_PRINTING;
import static com.mryqr.management.order.MryOrderManageApp.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncOrderToManagedQrTask implements RetryableTask {
    private final OrderRepository orderRepository;
    private final TenantRepository tenantRepository;
    private final QrRepository qrRepository;
    private final QrFactory qrFactory;
    private final AppRepository appRepository;
    private final PlateRepository plateRepository;
    private final SubmissionFactory submissionFactory;
    private final SubmissionRepository submissionRepository;
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;

    public void sync(String orderId) {
        orderRepository.byIdOptional(orderId).ifPresent(order -> {
            Tenant tenant = tenantRepository.cachedById(order.getTenantId());

            if (!qrRepository.existsByCustomId(orderId, ORDER_APP_ID)) {
                createOrderQr(tenant.getName(), order);
            }

            qrRepository.byCustomIdOptional(ORDER_APP_ID, orderId).ifPresent(qr -> {
                if (!Objects.equals(qr.getName(), tenant.getName())) {
                    qr.rename(tenant.getName(), NOUSER);
                    qrRepository.save(qr);
                }

                App app = appRepository.cachedById(ORDER_APP_ID);
                Page page = app.pageById(ORDER_SYNC_PAGE_ID);
                Set<Answer> answers = buildAnswers(order, page);
                Submission submission = submissionFactory.createOrUpdateSubmission(answers,
                        qr,
                        page,
                        app,
                        Set.of(Permission.values()),
                        null,
                        NOUSER
                );
                submissionRepository.houseKeepSave(submission, app);
                log.info("Synced order[{}] to managed QR.", orderId);
                syncAttributeValuesForQrTask.run(qr.getId());//及时计算属性值，不过兜底机制也会计算，只是有时延
            });
        });
    }

    private void createOrderQr(String name, Order order) {
        App app = appRepository.cachedById(ORDER_APP_ID);
        Group group = groupRepository.cachedById(ORDER_GROUP_ID);

        PlatedQr platedQr = qrFactory.createPlatedQr(name, group, app, order.getId(), NOUSER);
        QR qr = platedQr.getQr();
        Plate plate = platedQr.getPlate();
        qrRepository.save(qr);
        plateRepository.save(plate);
    }

    private Set<Answer> buildAnswers(Order order, Page page) {
        Map<String, Control> allControls = page.getControls().stream().collect(toImmutableMap(Control::getId, identity()));

        FIdentifierControl orderIdControl = (FIdentifierControl) allControls.get(ORDER_ID_CONTROL_ID);
        IdentifierAnswer orderIdAnswer = IdentifierAnswer.answerBuilder(requireNonNull(orderIdControl))
                .content(order.getId())
                .build();

        FDropdownControl orderTypeControl = (FDropdownControl) allControls.get(ORDER_TYPE_CONTROL_ID);
        String optionId = switch (order.getDetail().getType()) {
            case PLAN -> ORDER_TYPE_PACKAGE_OPTION_ID;
            case EXTRA_MEMBER -> ORDER_TYPE_EXTRA_MEMBER_OPTION_ID;
            case EXTRA_SMS -> ORDER_TYPE_EXTRA_SMS_OPTION_ID;
            case EXTRA_STORAGE -> ORDER_TYPE_EXTRA_STORAGE_OPTION_ID;
            case EXTRA_VIDEO_TRAFFIC -> ORDER_TYPE_EXTRA_VIDEO_OPTION_ID;
            case PLATE_PRINTING -> ORDER_TYPE_PLATE_OPTION_ID;
        };

        DropdownAnswer orderTypeAnswer = DropdownAnswer.answerBuilder(requireNonNull(orderTypeControl))
                .optionIds(List.of(optionId))
                .build();

        FItemStatusControl orderStatusControl = (FItemStatusControl) allControls.get(ORDER_STATUS_CONTROL_ID);
        String statusId = switch (order.getStatus()) {
            case CREATED -> ORDER_STATUS_CREATED_OPTION_ID;
            case PAID -> ORDER_STATUS_PAID_OPTION_ID;
            case REFUNDED -> ORDER_STATUS_REFUND_OPTION_ID;
        };
        ItemStatusAnswer orderStatusAnswer = ItemStatusAnswer.answerBuilder(requireNonNull(orderStatusControl))
                .optionId(statusId)
                .build();

        FSingleLineTextControl orderDetailControl = (FSingleLineTextControl) allControls.get(ORDER_DESCRIPTION_CONTROL_ID);
        SingleLineTextAnswer orderDetailAnswer = SingleLineTextAnswer.answerBuilder(requireNonNull(orderDetailControl))
                .content(order.description())
                .build();

        FNumberInputControl orderPriceControl = (FNumberInputControl) allControls.get(ORDER_PRICE_CONTROL_ID);
        NumberInputAnswer orderPriceAnswer = NumberInputAnswer.answerBuilder(requireNonNull(orderPriceControl))
                .number(Double.valueOf(order.getPrice().getDiscountedTotalPrice()))
                .build();

        Member member = memberRepository.byIdOptional(order.getCreatedBy()).orElse(null);

        FPersonNameControl orderSubmitterControl = (FPersonNameControl) allControls.get(ORDER_SUBMITTER_CONTROL_ID);
        PersonNameAnswer orderSubmitterAnswer = PersonNameAnswer.answerBuilder(requireNonNull(orderSubmitterControl))
                .name(member != null ? member.getName() : null)
                .build();

        FMobileNumberControl submitterMobileControl = (FMobileNumberControl) allControls.get(ORDER_SUBMITTER_MOBILE_CONTROL_ID);
        MobileNumberAnswer submitterMobileAnswer = MobileNumberAnswer.answerBuilder(requireNonNull(submitterMobileControl))
                .mobileNumber(member != null ? member.getMobile() : null)
                .build();

        FEmailControl submitterEmailControl = (FEmailControl) allControls.get(ORDER_SUBMITTER_EMAIL_CONTROL_ID);
        EmailAnswer submitterEmailAnswer = EmailAnswer.answerBuilder(requireNonNull(submitterEmailControl))
                .email(member != null ? member.getEmail() : null)
                .build();

        FDropdownControl orderChannelControl = (FDropdownControl) allControls.get(ORDER_CHANNEL_CONTROL_ID);
        String channelOptionId = switch (order.getPaymentType()) {
            case WX_NATIVE -> ORDER_CHANNEL_WX_PAY_OPTION_ID;
            case WX_TRANSFER -> ORDER_CHANNEL_WX_TRANSFER_OPTION_ID;
            case BANK_TRANSFER -> ORDER_CHANNEL_BANK_TRANSFER_OPTION_ID;
        };
        DropdownAnswer orderChannelAnswer = DropdownAnswer.answerBuilder(requireNonNull(orderChannelControl))
                .optionIds(List.of(channelOptionId))
                .build();

        FIdentifierControl wxTxnIdControl = (FIdentifierControl) allControls.get(ORDER_WX_TXN_CONTROL_ID);
        IdentifierAnswer wxTxnIdAnswer = IdentifierAnswer.answerBuilder(requireNonNull(wxTxnIdControl))
                .content(order.getPaymentType() == WX_NATIVE ? order.getWxTxnId() : "不适用")
                .build();

        FSingleLineTextControl bankAccountControl = (FSingleLineTextControl) allControls.get(ORDER_BANK_ACCOUNT_CONTROL_ID);
        SingleLineTextAnswer bankAccountAnswer = SingleLineTextAnswer.answerBuilder(requireNonNull(bankAccountControl))
                .content(order.getPaymentType() == BANK_TRANSFER ?
                        (isNotBlank(order.getBankName()) ? order.getBankName() + order.getBankTransferAccountId() : null) :
                        "不适用")
                .build();

        FItemStatusControl deliveryStatusControl = (FItemStatusControl) allControls.get(ORDER_DELIVERY_STATUS_CONTROL_ID);
        String statusOptionId;
        if (order.getDetail().getType() != PLATE_PRINTING) {
            statusOptionId = ORDER_DELIVERY_NONE_OPTION_ID;
        } else if (order.getDelivery() == null) {
            statusOptionId = ORDER_DELIVERY_NOT_YET_OPTION_ID;
        } else {
            statusOptionId = ORDER_DELIVERY_ALREADY_OPTION_ID;
        }
        ItemStatusAnswer deliveryStatusAnswer = ItemStatusAnswer.answerBuilder(requireNonNull(deliveryStatusControl))
                .optionId(statusOptionId)
                .build();

        FDropdownControl deliverControl = (FDropdownControl) allControls.get(ORDER_DELIVER_CONTROL_ID);
        String deliverOptionId = null;
        if (order.getDelivery() != null) {
            deliverOptionId = switch (order.getDelivery().getCarrier()) {
                case EMS -> ORDER_DELIVER_EMS_OPTION_ID;
                case SHUN_FENG -> ORDER_DELIVER_SHUNFENG_OPTION_ID;
                case YUAN_TONG -> ORDER_DELIVER_YUANTONG_OPTION_ID;
                case ZHONG_TONG -> ORDER_DELIVER_ZHONGTONG_OPTION_ID;
                case ZHONG_TONG_56 -> ORDER_DELIVER_ZHONGTONG_EXPRESS_OPTION_ID;
                case SHEN_TONG -> ORDER_DELIVER_SHENTONG_OPTION_ID;
                case YUN_DA -> ORDER_DELIVER_YUNDA_OPTION_ID;
                case YUN_DA_56 -> ORDER_DELIVER_YUNDA_EXPRESS_OPTION_ID;
            };
        }
        DropdownAnswer deliverAnswer = DropdownAnswer.answerBuilder(requireNonNull(deliverControl))
                .optionIds(isBlank(deliverOptionId) ? List.of() : List.of(deliverOptionId))
                .build();

        FIdentifierControl deliveryIdControl = (FIdentifierControl) allControls.get(ORDER_DELIVERY_ID_CONTROL_ID);
        IdentifierAnswer deliveryIdAnswer = IdentifierAnswer.answerBuilder(requireNonNull(deliveryIdControl))
                .content(order.getDelivery() != null ? order.getDelivery().getDeliveryOrderId() : null)
                .build();

        FItemStatusControl invoiceStatusControl = (FItemStatusControl) allControls.get(ORDER_INVOICE_STATUS_CONTROL_ID);
        String invoiceStatusOptionId;
        if (order.getInvoice() == null) {
            invoiceStatusOptionId = ORDER_INVOICE_STATUS_NONE_OPTION_ID;
        } else if (order.getInvoice().isIssued()) {
            invoiceStatusOptionId = ORDER_INVOICE_STATUS_DONE_OPTION_ID;
        } else {
            invoiceStatusOptionId = ORDER_INVOICE_STATUS_WAITING_OPTION_ID;
        }
        ItemStatusAnswer invoiceStatusAnswer = ItemStatusAnswer.answerBuilder(requireNonNull(invoiceStatusControl))
                .optionId(invoiceStatusOptionId)
                .build();

        FMultiLineTextControl invoiceInfoControl = (FMultiLineTextControl) allControls.get(ORDER_INVOICE_INFO_CONTROL_ID);
        String content = null;
        Invoice invoice = order.getInvoice();
        if (invoice != null) {
            String invoiceType = switch (invoice.getType()) {
                case PERSONAL -> "个人发票";
                case VAT_NORMAL -> "增值税普通发票";
                case VAT_SPECIAL -> "增值税专用发票";
            };

            content = "发票类型：" + invoiceType + "\n" +
                      "接收邮箱：" + invoice.getEmail() + "\n" +
                      "申请时间：" + ((invoice.getRequestedAt() != null) ? MRY_DATE_TIME_FORMATTER.format(invoice.getRequestedAt()) : "") + "\n" +
                      "开具时间：" + ((invoice.getIssuedAt() != null) ? MRY_DATE_TIME_FORMATTER.format(invoice.getIssuedAt()) : "") + "\n" +
                      "发票抬头：" + invoice.getTitle().getTitle() + "\n" +
                      "信用代码：" + invoice.getTitle().getUnifiedCode() + "\n" +
                      "开户银行：" + invoice.getTitle().getBankName() + "\n" +
                      "银行账号：" + invoice.getTitle().getBankAccount() + "\n" +
                      "发票地址：" + invoice.getTitle().getAddress() + "\n" +
                      "对方电话：" + invoice.getTitle().getPhone() + "\n";
        }

        MultiLineTextAnswer invoiceInfoAnswer = MultiLineTextAnswer.answerBuilder(requireNonNull(invoiceInfoControl))
                .content(content)
                .build();

        FFileUploadControl invoiceFileControl = (FFileUploadControl) allControls.get(ORDER_INVOICE_FILE_CONTROL_ID);
        FileUploadAnswer invoiceFileAnswer = FileUploadAnswer.answerBuilder(requireNonNull(invoiceFileControl))
                .files(order.getInvoice() != null ? emptyIfNull(order.getInvoice().getFiles()) : List.of())
                .build();

        FFileUploadControl screenShotsControl = (FFileUploadControl) allControls.get(ORDER_SCREENSHOTS_CONTROL_ID);
        FileUploadAnswer screenShotsAnswer = FileUploadAnswer.answerBuilder(requireNonNull(screenShotsControl))
                .files(emptyIfNull(order.getScreenShots()))
                .build();

        FIdentifierControl tenantIdControl = (FIdentifierControl) allControls.get(ORDER_TENANT_CONTROL_ID);
        IdentifierAnswer tenantIdAnswer = IdentifierAnswer.answerBuilder(requireNonNull(tenantIdControl))
                .content(order.getTenantId())
                .build();

        return Set.of(orderIdAnswer, orderTypeAnswer, orderStatusAnswer, orderDetailAnswer, orderPriceAnswer, orderSubmitterAnswer,
                submitterMobileAnswer, submitterEmailAnswer, orderChannelAnswer, wxTxnIdAnswer,
                bankAccountAnswer, deliveryStatusAnswer, deliverAnswer, deliveryIdAnswer, invoiceStatusAnswer,
                invoiceInfoAnswer, invoiceFileAnswer, screenShotsAnswer, tenantIdAnswer);
    }
}
