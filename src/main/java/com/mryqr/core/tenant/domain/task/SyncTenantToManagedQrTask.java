package com.mryqr.core.tenant.domain.task;

import com.mryqr.common.domain.invoice.InvoiceTitle;
import com.mryqr.common.domain.permission.Permission;
import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.common.properties.PropertyService;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.TenantCachedApp;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.*;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.member.domain.TenantCachedMember;
import com.mryqr.core.plan.domain.Plan;
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
import com.mryqr.core.submission.domain.answer.date.DateAnswer;
import com.mryqr.core.submission.domain.answer.dropdown.DropdownAnswer;
import com.mryqr.core.submission.domain.answer.identifier.IdentifierAnswer;
import com.mryqr.core.submission.domain.answer.itemstatus.ItemStatusAnswer;
import com.mryqr.core.submission.domain.answer.multilinetext.MultiLineTextAnswer;
import com.mryqr.core.submission.domain.answer.numberinput.NumberInputAnswer;
import com.mryqr.core.tenant.domain.Packages;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.mryqr.common.domain.user.User.NOUSER;
import static com.mryqr.common.utils.MryConstants.MRY_DATE_FORMATTER;
import static com.mryqr.common.utils.MryConstants.MRY_DATE_TIME_FORMATTER;
import static com.mryqr.core.app.domain.page.control.FNumberInputControl.MAX_NUMBER;
import static com.mryqr.management.common.PlanTypeControl.PLAN_TO_OPTION_MAP;
import static com.mryqr.management.crm.MryTenantManageApp.*;
import static java.lang.Long.compare;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;
import static java.time.ZoneId.systemDefault;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncTenantToManagedQrTask implements RetryableTask {
    private final TenantRepository tenantRepository;
    private final AppRepository appRepository;
    private final QrRepository qrRepository;
    private final QrFactory qrFactory;
    private final PlateRepository plateRepository;
    private final SubmissionFactory submissionFactory;
    private final SubmissionRepository submissionRepository;
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final PropertyService propertyService;

    @Transactional
    public void sync(String tenantId) {
        tenantRepository.byIdOptional(tenantId).ifPresent(tenant -> {
            if (!qrRepository.existsByCustomId(tenantId, MRY_TENANT_MANAGE_APP_ID)) {
                createTenantQr(tenant);
            }

            qrRepository.byCustomIdOptional(MRY_TENANT_MANAGE_APP_ID, tenantId).ifPresent(qr -> {
                qr.rename(tenant.getName(), NOUSER);
                qr.updateHeaderImage(tenant.getLogo(), NOUSER);
                qrRepository.save(qr);

                App app = appRepository.cachedById(MRY_TENANT_MANAGE_APP_ID);
                Page page = app.pageById(TENANT_SYNC_PAGE_ID);
                Set<Answer> answers = buildAnswers(tenant, page);

                Submission submission = submissionFactory.createOrUpdateSubmission(answers,
                        qr,
                        page,
                        app,
                        Set.of(Permission.values()),
                        null,
                        NOUSER
                );
                submissionRepository.houseKeepSave(submission, app);
                log.debug("Synced tenant[{}] to managed QR.", tenantId);
                syncAttributeValuesForQrTask.run(qr.getId());//及时计算属性值，不过兜底机制也会计算，只是有时延
            });
        });
    }

    private void createTenantQr(Tenant tenant) {
        App app = appRepository.cachedById(MRY_TENANT_MANAGE_APP_ID);
        Group group = groupRepository.cachedById(MRY_TENANT_MANAGE_GROUP_ID);

        PlatedQr platedQr = qrFactory.createPlatedQr(tenant.getName(), group, app, tenant.getId(), NOUSER);
        QR qr = platedQr.getQr();
        Plate plate = platedQr.getPlate();
        qrRepository.save(qr);
        plateRepository.save(plate);
    }

    private Set<Answer> buildAnswers(Tenant tenant, Page page) {
        Map<String, Control> allControls = page.getControls().stream().collect(toImmutableMap(Control::getId, identity()));

        FDropdownControl currentPackageControl = (FDropdownControl) allControls.get(CURRENT_PACKAGE_CONTROL_ID);
        String planOptionId = PLAN_TO_OPTION_MAP.get(tenant.currentPlan().getType());
        DropdownAnswer currentPackageAnswer = DropdownAnswer.answerBuilder(requireNonNull(currentPackageControl))
                .optionIds(List.of(planOptionId))
                .build();

        FDateControl packageExpireDateControl = (FDateControl) allControls.get(EXPIRE_DATE_CONTROL_ID);
        LocalDate expireDate = LocalDate.ofInstant(tenant.packagesExpiredAt(), systemDefault());
        DateAnswer expireDateAnswer = DateAnswer.answerBuilder(requireNonNull(packageExpireDateControl))
                .date(expireDate.toString())
                .build();

        FItemStatusControl packageStatusControl = (FItemStatusControl) allControls.get(PACKAGES_STATUS_CONTROL_ID);
        String optionId = PACKAGES_STATUS_NORMAL_OPTION_ID;
        if (expireDate.isBefore(LocalDate.now())) {
            optionId = PACKAGES_STATUS_EXPIRED_OPTION_ID;
        } else if (expireDate.isBefore(LocalDate.now().plusDays(30))) {
            optionId = PACKAGES_STATUS_EXPIRING_OPTION_ID;
        }
        ItemStatusAnswer packageStatusAnswer = ItemStatusAnswer.answerBuilder(requireNonNull(packageStatusControl))
                .optionId(optionId)
                .build();

        FDateControl registerDateControl = (FDateControl) allControls.get(REGISTER_DATE_CONTROL_ID);
        LocalDate registerDate = LocalDate.ofInstant(tenant.getCreatedAt(), systemDefault());
        DateAnswer registerDateAnswer = DateAnswer.answerBuilder(requireNonNull(registerDateControl))
                .date(registerDate.toString())
                .build();

        FDateControl recentActiveDateControl = (FDateControl) allControls.get(RECENT_ACTIVE_DATE_CONTROL_ID);
        Instant defaultRecentActiveInstant = LocalDate.of(2000, 1, 1).atStartOfDay(systemDefault()).toInstant();
        Instant recentTenantActiveTime = Optional.ofNullable(tenant.getRecentAccessedAt()).orElse(defaultRecentActiveInstant);
        String recentActiveDate = MRY_DATE_FORMATTER.format(recentTenantActiveTime);
        DateAnswer recentActiveDateAnswer = DateAnswer.answerBuilder(requireNonNull(recentActiveDateControl))
                .date(recentActiveDate)
                .build();

        FNumberInputControl appUsageControl = (FNumberInputControl) allControls.get(APP_USAGE_CONTROL_ID);
        long appCount = appRepository.countApp(tenant.getId());
        double appUsage = Math.min(appCount, MAX_NUMBER);
        NumberInputAnswer appUsageAnswer = NumberInputAnswer.answerBuilder(requireNonNull(appUsageControl))
                .number(appUsage)
                .build();

        FNumberInputControl qrUsageControl = (FNumberInputControl) allControls.get(QR_USAGE_CONTROL_ID);
        long qrCount = qrRepository.count(tenant.getId());
        double qrUsage = Math.min(qrCount, MAX_NUMBER);
        NumberInputAnswer qrUsageAnswer = NumberInputAnswer.answerBuilder(requireNonNull(qrUsageControl))
                .number(qrUsage)
                .build();

        FNumberInputControl submissionUsageControl = (FNumberInputControl) allControls.get(SUBMISSION_USAGE_CONTROL_ID);
        long submissionCount = submissionRepository.count(tenant.getId());
        double submissionUsage = Math.min(submissionCount, MAX_NUMBER);
        NumberInputAnswer submissionUsageAnswer = NumberInputAnswer.answerBuilder(requireNonNull(submissionUsageControl))
                .number(submissionUsage)
                .build();

        FNumberInputControl memberUsageControl = (FNumberInputControl) allControls.get(MEMBER_USAGE_CONTROL_ID);
        long memberCount = memberRepository.countMembersUnderTenant(tenant.getId());
        double memberUsage = Math.min(memberCount, MAX_NUMBER);
        NumberInputAnswer memberUsageAnswer = NumberInputAnswer.answerBuilder(requireNonNull(memberUsageControl))
                .number(memberUsage)
                .build();

        FNumberInputControl storageUsageControl = (FNumberInputControl) allControls.get(STORAGE_USAGE_CONTROL_ID);
        double storage = valueOf(tenant.getResourceUsage().getStorage()).setScale(2, HALF_UP).doubleValue();
        NumberInputAnswer storageUsageAnswer = NumberInputAnswer.answerBuilder(requireNonNull(storageUsageControl))
                .number(storage)
                .build();

        FNumberInputControl smsUsageControl = (FNumberInputControl) allControls.get(SMS_USAGE_CONTROL_ID);
        NumberInputAnswer smsUsageAnswer = NumberInputAnswer.answerBuilder(requireNonNull(smsUsageControl))
                .number((double) tenant.getResourceUsage().getSmsSentCountForCurrentMonth())
                .build();

        FItemStatusControl statusControl = (FItemStatusControl) allControls.get(ACTIVE_STATUS_CONTROL_ID);
        String statusOptionId = tenant.isActive() ? ACTIVE_STATUS_YES_OPTION_ID : ACTIVE_STATUS_NO_OPTION_ID;
        ItemStatusAnswer statusAnswer = ItemStatusAnswer.answerBuilder(requireNonNull(statusControl))
                .optionId(statusOptionId)
                .build();

        FIdentifierControl subdomainControl = (FIdentifierControl) allControls.get(SUBDOMAIN_CONTROL_ID);
        IdentifierAnswer subdomainAnswer = IdentifierAnswer.answerBuilder(requireNonNull(subdomainControl))
                .content(isNotBlank(tenant.getSubdomainPrefix()) ? propertyService.subdomainFor(tenant.getSubdomainPrefix()) : null)
                .build();

        FMultiLineTextControl invoiceTitleControl = (FMultiLineTextControl) allControls.get(INVOICE_TITLE_CONTROL_ID);
        String invoiceTitleContent = null;
        InvoiceTitle invoiceTitle = tenant.getInvoiceTitle();
        if (invoiceTitle != null) {
            invoiceTitleContent = "发票抬头：" + invoiceTitle.getTitle() + "\n" +
                                  "信用代码：" + invoiceTitle.getUnifiedCode() + "\n" +
                                  "开户银行：" + invoiceTitle.getBankName() + "\n" +
                                  "银行账号：" + invoiceTitle.getBankAccount() + "\n" +
                                  "注册地址：" + invoiceTitle.getAddress() + "\n" +
                                  "注册电话：" + invoiceTitle.getPhone() + "\n";
        }
        MultiLineTextAnswer invoiceTitleAnswer = MultiLineTextAnswer.answerBuilder(requireNonNull(invoiceTitleControl))
                .content(invoiceTitleContent)
                .build();

        List<TenantCachedApp> apps = this.appRepository.cachedTenantAllApps(tenant.getId());
        String appStrings = apps.stream().map(app -> app.getName() + "：" + app.getId()).collect(joining("\n"));

        FMultiLineTextControl packageDetailControl = (FMultiLineTextControl) allControls.get(PACKAGE_DESCRIPTION_CONTROL_ID);
        Packages packages = tenant.getPackages();
        Plan currentPlan = packages.currentPlan();
        String packageDetailContent = "租户ID：" + tenant.getId() + "\n" +
//                                      "套餐名称：" + currentPlan.getType().getName() + "\n" +
//                                      "过期时间：" + MRY_DATE_TIME_FORMATTER.format(packages.expireAt()) + "\n" +
                                      "应用数量：" + currentPlan.getMaxAppCount() + " 个\n" +
                                      "提交总量：" + currentPlan.getMaxSubmissionCount() + "份\n" +
                                      "实例总量：" + currentPlan.getMaxQrCount() + "个\n" +
                                      "部门总量：" + currentPlan.getMaxDepartmentCount() + "个\n" +
                                      "单应用分组数量：" + currentPlan.getMaxGroupCountPerApp() + "个\n" +
                                      "成员数量：" + currentPlan.getMaxMemberCount() + " 名\n" +
                                      "存储容量：" + currentPlan.getMaxStorage() + " G\n" +
                                      "每月短信量：" + currentPlan.getMaxSmsCountPerMonth() + " 条\n" +
                                      "最近活跃时间：" + MRY_DATE_TIME_FORMATTER.format(recentTenantActiveTime) + "\n" +
//                                      "增购成员数量：" + packages.getExtraMemberCount() + " 名\n" +
//                                      "增购存储空间：" + packages.getExtraStorage() + " G\n" +
//                                      "剩余增购短信量：" + packages.getExtraRemainSmsCount() + " 条\n" +
//                                      "允许自定义子域名：" + currentPlan.isCustomSubdomainAllowed() + "\n" +
//                                      "域名是否就绪：" + tenant.isSubdomainReady() + "\n" +
//                                      "是否允许自定义logo：" + currentPlan.isCustomLogoAllowed() + "\n" +
//                                      "去除页面底部码如云标识：" + currentPlan.isHideBottomMryLogo() + "\n" +
//                                      "去除广告：" + currentPlan.isHideAds() + "\n" +
//                                      "可上传音视频：" + currentPlan.isVideoAudioAllowed() + "\n" +
//                                      "开发功能：" + currentPlan.isDeveloperAllowed() + "\n" +
//                                      "报表功能：" + currentPlan.isReportingAllowed() + "\n" +
//                                      "提交提醒功能：" + currentPlan.isSubmissionNotifyAllowed() + "\n" +
//                                      "批量导入实例数据：" + currentPlan.isBatchImportQrAllowed() + "\n" +
//                                      "批量导入成员数据：" + currentPlan.isBatchImportMemberAllowed() + "\n" +
//                                      "提交审批：" + currentPlan.isSubmissionApprovalAllowed() + "\n" +
                                      "应用列表：\n" +
                                      appStrings + "\n";

        MultiLineTextAnswer packageDetailAnswer = MultiLineTextAnswer.answerBuilder(requireNonNull(packageDetailControl))
                .content(packageDetailContent)
                .build();

        FMultiLineTextControl adminsControl = (FMultiLineTextControl) allControls.get(ADMINS_CONTROL_ID);
        List<TenantCachedMember> admins = memberRepository.cachedAllActiveTenantAdmins(tenant.getId());

        String adminsContent = admins.stream().map(member -> {
            String name = member.getName();
            String mobile = isNotBlank(member.getMobile()) ? member.getMobile() : "";
            String email = isNotBlank(member.getEmail()) ? member.getEmail() : "";
            return name + "：" + mobile + "，" + email;
        }).collect(joining("\n"));

        String emails = admins.stream().map(TenantCachedMember::getEmail).filter(Objects::nonNull).collect(joining(","));

        MultiLineTextAnswer adminsAnswer = MultiLineTextAnswer.answerBuilder(requireNonNull(adminsControl))
                .content(adminsContent +
                         "\n\n----------------------\n所有邮箱：" + emails +
                         "\n注册人：" + tenant.getCreator())
                .build();

        FMultiLineTextControl opslogControl = (FMultiLineTextControl) allControls.get(OPS_LOG_CONTROL_ID);
        String opsContent = tenant.getOpsLogs().stream()
                .sorted((o1, o2) -> compare(o2.getOperatedAt().getEpochSecond(), o1.getOperatedAt().getEpochSecond()))
                .map(opsLog -> {
                    String time = MRY_DATE_TIME_FORMATTER.format(opsLog.getOperatedAt());
                    String operatedByName = opsLog.getOperatedByName() != null ? opsLog.getOperatedByName() : "";
                    return time + "：" + operatedByName + "：" + opsLog.getNote();
                })
                .collect(joining("\n"));

        MultiLineTextAnswer opsLogAnswer = MultiLineTextAnswer.answerBuilder(requireNonNull(opslogControl))
                .content(opsContent)
                .build();

        return Set.of(currentPackageAnswer, expireDateAnswer, packageStatusAnswer, registerDateAnswer, appUsageAnswer,
                memberUsageAnswer, storageUsageAnswer, smsUsageAnswer, statusAnswer, subdomainAnswer, invoiceTitleAnswer,
                packageDetailAnswer, adminsAnswer, opsLogAnswer, qrUsageAnswer, submissionUsageAnswer, recentActiveDateAnswer);
    }
}
