package com.mryqr.feed;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.CreateAppResponse;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.config.AppConfig;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.page.control.FAddressControl;
import com.mryqr.core.app.domain.page.control.FCheckboxControl;
import com.mryqr.core.app.domain.page.control.FDateControl;
import com.mryqr.core.app.domain.page.control.FDropdownControl;
import com.mryqr.core.app.domain.page.control.FEmailControl;
import com.mryqr.core.app.domain.page.control.FFileUploadControl;
import com.mryqr.core.app.domain.page.control.FGeolocationControl;
import com.mryqr.core.app.domain.page.control.FIdentifierControl;
import com.mryqr.core.app.domain.page.control.FImageUploadControl;
import com.mryqr.core.app.domain.page.control.FItemCountControl;
import com.mryqr.core.app.domain.page.control.FItemStatusControl;
import com.mryqr.core.app.domain.page.control.FMobileNumberControl;
import com.mryqr.core.app.domain.page.control.FMultiLevelSelectionControl;
import com.mryqr.core.app.domain.page.control.FMultiLineTextControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.app.domain.page.control.FNumberRankingControl;
import com.mryqr.core.app.domain.page.control.FPersonNameControl;
import com.mryqr.core.app.domain.page.control.FPointCheckControl;
import com.mryqr.core.app.domain.page.control.FRadioControl;
import com.mryqr.core.app.domain.page.control.FRichTextInputControl;
import com.mryqr.core.app.domain.page.control.FSignatureControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.app.domain.page.control.FTimeControl;
import com.mryqr.core.app.domain.page.setting.PageSetting;
import com.mryqr.core.app.domain.page.setting.SubmitType;
import com.mryqr.core.group.GroupApi;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.plan.domain.Plan;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.utils.CreateMemberResponse;
import com.mryqr.utils.LoginResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.page.control.ControlType.MEMBER_SELECT;
import static com.mryqr.core.app.domain.page.setting.SubmitType.NEW;
import static com.mryqr.core.app.domain.page.setting.SubmitType.ONCE_PER_INSTANCE;
import static com.mryqr.core.common.domain.permission.Permission.AS_TENANT_MEMBER;
import static com.mryqr.core.plan.domain.Plan.ADVANCED_PLAN;
import static com.mryqr.core.plan.domain.Plan.BASIC_PLAN;
import static com.mryqr.core.plan.domain.Plan.FREE_PLAN;
import static com.mryqr.core.plan.domain.Plan.PROFESSIONAL_PLAN;
import static com.mryqr.core.plan.domain.PlanType.FREE;
import static com.mryqr.utils.RandomTestFixture.defaultAddressControl;
import static com.mryqr.utils.RandomTestFixture.defaultCheckboxControl;
import static com.mryqr.utils.RandomTestFixture.defaultDateControl;
import static com.mryqr.utils.RandomTestFixture.defaultDropdownControl;
import static com.mryqr.utils.RandomTestFixture.defaultEmailControl;
import static com.mryqr.utils.RandomTestFixture.defaultFileUploadControl;
import static com.mryqr.utils.RandomTestFixture.defaultGeolocationControl;
import static com.mryqr.utils.RandomTestFixture.defaultIdentifierControl;
import static com.mryqr.utils.RandomTestFixture.defaultImageUploadControl;
import static com.mryqr.utils.RandomTestFixture.defaultItemCountControl;
import static com.mryqr.utils.RandomTestFixture.defaultItemStatusControl;
import static com.mryqr.utils.RandomTestFixture.defaultMobileControl;
import static com.mryqr.utils.RandomTestFixture.defaultMultipleLineTextControl;
import static com.mryqr.utils.RandomTestFixture.defaultNumberInputControl;
import static com.mryqr.utils.RandomTestFixture.defaultNumberRankingControl;
import static com.mryqr.utils.RandomTestFixture.defaultPageBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultPersonNameControl;
import static com.mryqr.utils.RandomTestFixture.defaultPointCheckControl;
import static com.mryqr.utils.RandomTestFixture.defaultRadioControl;
import static com.mryqr.utils.RandomTestFixture.defaultRichTextInputControl;
import static com.mryqr.utils.RandomTestFixture.defaultSignatureControl;
import static com.mryqr.utils.RandomTestFixture.defaultSingleLineTextControl;
import static com.mryqr.utils.RandomTestFixture.defaultSubmissionReferenceControlBuilder;
import static com.mryqr.utils.RandomTestFixture.defaultTimeControl;
import static com.mryqr.utils.RandomTestFixture.rAnswer;
import static com.mryqr.utils.RandomTestFixture.rAttributeName;
import static com.mryqr.utils.RandomTestFixture.rInt;
import static com.mryqr.utils.RandomTestFixture.rMobile;
import static com.mryqr.utils.RandomTestFixture.rMobileOrEmail;
import static com.mryqr.utils.RandomTestFixture.rPassword;
import static com.mryqr.utils.RandomTestFixture.rRawGroupName;
import static com.mryqr.utils.RandomTestFixture.rRawMemberName;
import static com.mryqr.utils.RandomTestFixture.rRawQrName;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.RandomUtils.nextInt;

public class PreExistingDataGenerator extends BaseApiTest {
    private static final Logger logger = LoggerFactory.getLogger(PreExistingDataGenerator.class);
    ForkJoinPool tenantPool = new ForkJoinPool(100);
    ForkJoinPool qrPool = new ForkJoinPool(100);

    @Disabled
    @Test
    public void generate_fake_data() throws ExecutionException, InterruptedException {
        int allTenantCount = 1000;

        tenantPool.submit(
                () -> range(0, allTenantCount).parallel().forEach(tenantIndex -> {
                    int packagePossibility = nextInt(0, 100);
                    if (packagePossibility >= 98) {
                        createTenant(PROFESSIONAL_PLAN);
                    } else if (packagePossibility >= 90) {
                        createTenant(BASIC_PLAN);
                    } else if (packagePossibility >= 80) {
                        createTenant(ADVANCED_PLAN);
                    } else {
                        createTenant(FREE_PLAN);
                    }
                })).get();
    }

    private void createTenant(Plan plan) {
        LoginResponse tenantAdmin = setupApi.registerWithLogin(rRawMemberName(), rMobileOrEmail(), rPassword());
        if (plan.getType() != FREE) {
            setupApi.updateTenantPackages(tenantAdmin.getTenantId(), plan.getType());
        }

        List<String> allMemberJwts = Stream.concat(of(tenantAdmin.getJwt()), range(0, rInt(1, plan.getMaxMemberCount() - 1)).mapToObj(value -> createMember(tenantAdmin.getJwt())).filter(Objects::nonNull)).collect(toList());
        int totalAppCount = appCountOf(plan);

        range(0, totalAppCount).forEach(appIndex -> {
            createApp(tenantAdmin.getJwt(), plan).ifPresent(response -> {
                App app = response.getLeft();

                List<String> allGroupIds = range(0, nextInt(0, plan.getMaxGroupCountPerApp() - 1))
                        .mapToObj(it -> createGroup(tenantAdmin.getJwt(), app.getId()))
                        .filter(Objects::nonNull).collect(toList());
                allGroupIds.add(response.getRight());

                try {
                    qrPool.submit(
                            () -> range(0, appQrCountOf(plan, totalAppCount)).parallel().forEach(qrIndex -> {
                                createQr(tenantAdmin.getJwt(), allGroupIds.get(nextInt(0, allGroupIds.size()))).ifPresent(qrId -> {
                                    app.allPages().stream().filter(page -> page.isFillable() && page.submitType() == ONCE_PER_INSTANCE)
                                            .forEach(page -> createSubmission(allMemberJwts.get(nextInt(0, allMemberJwts.size())), qrId, page));

                                    app.allPages().stream().filter(page -> page.isFillable() && page.submitType() == NEW)
                                            .forEach(page -> createSubmission(allMemberJwts.get(nextInt(0, allMemberJwts.size())), qrId, page));
                                });
                            })
                    ).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private String createMember(String jwt) {
        try {
            CreateMemberResponse member = MemberApi.createMemberAndLogin(jwt, rRawMemberName(), rMobile(), rPassword());
            return member.getJwt();
        } catch (Throwable t) {
            logger.warn("Failed to create member: {}.", t.getMessage());
            return null;
        }
    }

    private Optional<Pair<App, String>> createApp(String jwt, Plan plan) {
        try {
            CreateAppResponse appResponse = AppApi.createApp(jwt);
            App app = appRepository.byId(appResponse.getAppId());
            Page perInstancePage = createPage(ONCE_PER_INSTANCE, createControlsFor(plan));
            Page homePage = createPage(NEW, List.of(defaultSubmissionReferenceControlBuilder().pageId(perInstancePage.getId()).build()));
            List<Page> submissionPages = range(0, submissionPageCountOf(plan)).mapToObj(index -> createPage(NEW, createControlsFor(plan))).collect(toList());

            List<Page> allPages = Stream.concat(of(homePage, perInstancePage), submissionPages.stream()).collect(toList());
            AppSetting setting = app.getSetting();
            AppConfig config = setting.getConfig();
            ReflectionTestUtils.setField(config, "homePageId", homePage.getId());
            ReflectionTestUtils.setField(config, "allowDuplicateInstanceName", true);
            setting.getPages().clear();
            setting.getPages().addAll(allPages);

            List<Attribute> attributes = perInstancePage.getControls().stream().map(control -> {
                        if (rInt(1, 100) > 30) {
                            return Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(perInstancePage.getId()).controlId(control.getId()).range(NO_LIMIT).build();
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(toList());

            setting.getAttributes().addAll(attributes);
            AppApi.updateAppSetting(jwt, appResponse.getAppId(), app.getVersion(), setting);
            return Optional.of(Pair.of(app, appResponse.getDefaultGroupId()));
        } catch (Throwable t) {
            logger.warn("Failed to create app: {}.", t.getMessage());
            return Optional.empty();
        }
    }


    private Page createPage(SubmitType submitType, List<Control> controls) {
        return defaultPageBuilder()
                .setting(PageSetting.defaultPageSettingBuilder()
                        .submitType(submitType)
                        .permission(AS_TENANT_MEMBER)
                        .build())
                .controls(controls)
                .build();
    }

    private List<Control> createControlsFor(Plan plan) {
        List<ControlType> controlTypes = plan.getSupportedControlTypes().stream()
                .filter(controlType -> controlType.isFillable() && controlType != MEMBER_SELECT)//不要成员控件，不好提供成员数据
                .collect(toList());

        Collections.shuffle(controlTypes);
        int possibility = nextInt(0, 100);
        int count = possibility <= 50 ? rInt(1, 5) : rInt(1, controlTypes.size());
        return controlTypes.stream().limit(count).map(this::createControl).filter(Objects::nonNull).collect(toList());
    }

    private Control createControl(ControlType controlType) {
        return switch (controlType) {
            case RADIO -> defaultRadioControl();
            case CHECKBOX -> defaultCheckboxControl();
            case SINGLE_LINE_TEXT -> defaultSingleLineTextControl();
            case MULTI_LINE_TEXT -> defaultMultipleLineTextControl();
            case RICH_TEXT_INPUT -> defaultRichTextInputControl();
            case DROPDOWN -> defaultDropdownControl();
            case FILE_UPLOAD -> defaultFileUploadControl();
            case IMAGE_UPLOAD -> defaultImageUploadControl();
            case ADDRESS -> defaultAddressControl();
            case GEOLOCATION -> defaultGeolocationControl();
            case NUMBER_INPUT -> defaultNumberInputControl();
            case NUMBER_RANKING -> defaultNumberRankingControl();
            case MOBILE -> defaultMobileControl();
            case IDENTIFIER -> defaultIdentifierControl();
            case PERSON_NAME -> defaultPersonNameControl();
            case EMAIL -> defaultEmailControl();
            case DATE -> defaultDateControl();
            case TIME -> defaultTimeControl();
            case ITEM_COUNT -> defaultItemCountControl();
            case ITEM_STATUS -> defaultItemStatusControl();
            case POINT_CHECK -> defaultPointCheckControl();
            case SIGNATURE -> defaultSignatureControl();
            case MULTI_LEVEL_SELECTION -> defaultMultipleLineTextControl();
            default -> null;
        };
    }

    private String createGroup(String jwt, String appId) {
        try {
            return GroupApi.createGroup(jwt, appId, rRawGroupName());
        } catch (Throwable t) {
            logger.warn("Failed to created group:{}.", t.getMessage());
            return null;
        }
    }

    private Optional<String> createQr(String jwt, String groupId) {
        try {
            return Optional.of(QrApi.createQr(jwt, rRawQrName(), groupId).getQrId());
        } catch (Throwable t) {
            logger.warn("Failed to create QR: {}.", t.getMessage());
            return Optional.empty();
        }
    }

    private String createSubmission(String jwt, String qrId, Page page) {
        try {
            return SubmissionApi.newSubmission(jwt, qrId, page.getId(), page.allFillableControls().stream().map(this::answerOf).collect(toSet()));
        } catch (Throwable t) {
            logger.warn("Failed to create submission: {}.", t.getMessage());
            return null;
        }
    }

    private Answer answerOf(Control control) {
        return switch (control.getType()) {
            case RADIO -> rAnswer((FRadioControl) control);
            case CHECKBOX -> rAnswer((FCheckboxControl) control);
            case SINGLE_LINE_TEXT -> rAnswer((FSingleLineTextControl) control);
            case MULTI_LINE_TEXT -> rAnswer((FMultiLineTextControl) control);
            case RICH_TEXT_INPUT -> rAnswer((FRichTextInputControl) control);
            case DROPDOWN -> rAnswer((FDropdownControl) control);
            case FILE_UPLOAD -> rAnswer((FFileUploadControl) control);
            case IMAGE_UPLOAD -> rAnswer((FImageUploadControl) control);
            case ADDRESS -> rAnswer((FAddressControl) control);
            case GEOLOCATION -> rAnswer((FGeolocationControl) control);
            case NUMBER_INPUT -> rAnswer((FNumberInputControl) control);
            case NUMBER_RANKING -> rAnswer((FNumberRankingControl) control);
            case MOBILE -> rAnswer((FMobileNumberControl) control);
            case IDENTIFIER -> rAnswer((FIdentifierControl) control);
            case PERSON_NAME -> rAnswer((FPersonNameControl) control);
            case EMAIL -> rAnswer((FEmailControl) control);
            case DATE -> rAnswer((FDateControl) control);
            case TIME -> rAnswer((FTimeControl) control);
            case ITEM_COUNT -> rAnswer((FItemCountControl) control);
            case ITEM_STATUS -> rAnswer((FItemStatusControl) control);
            case POINT_CHECK -> rAnswer((FPointCheckControl) control);
            case SIGNATURE -> rAnswer((FSignatureControl) control);
            case MULTI_LEVEL_SELECTION -> rAnswer((FMultiLevelSelectionControl) control);
            default -> null;
        };
    }

    private int appCountOf(Plan plan) {
        int factor = Math.min(rInt(1, 20), rInt(1, 10));
        return Math.min(rInt(1, plan.getMaxAppCount()), factor);
    }

    private int appQrCountOf(Plan plan, int totalAppCount) {
        return Math.min(rInt(1, plan.getMaxQrCount() / totalAppCount), rInt(1, plan.getMaxQrCount() / totalAppCount));
    }

    private int submissionPageCountOf(Plan plan) {
        int count = switch (plan.getType()) {
            case FREE -> rInt(1, 3);
            case BASIC -> rInt(1, 5);
            case ADVANCED -> rInt(1, 6);
            case PROFESSIONAL -> rInt(1, 7);
            case FLAGSHIP -> rInt(1, 8);
        };

        if (count > 5) {
            return Math.min(count, rInt(1, 10));
        }

        return count;
    }
}
