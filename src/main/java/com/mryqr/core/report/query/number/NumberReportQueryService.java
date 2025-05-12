package com.mryqr.core.report.query.number;

import com.mryqr.common.domain.permission.ManagePermissionChecker;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.report.number.NumberReport;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.report.query.number.reporter.NumberReporter;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
@RequiredArgsConstructor
public class NumberReportQueryService {
    private final List<NumberReporter> reporters;
    private final MryRateLimiter mryRateLimiter;
    private final AppRepository appRepository;
    private final GroupRepository groupRepository;
    private final ManagePermissionChecker managePermissionChecker;
    private final TenantRepository tenantRepository;

    public QNumberReport fetchNumberReport(NumberReportQuery queryCommand, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Report:Number", 20);

        Tenant tenant = tenantRepository.cachedByIdAndCheckTenantShip(user.getTenantId(), user);
        tenant.packagesStatus().validateReporting();

        String appId = queryCommand.getAppId();
        String groupId = queryCommand.getGroupId();
        NumberReport report = queryCommand.getReport();

        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);
        Set<String> groupIds = allEligibleGroupIds(user, groupId, app);

        if (isEmpty(groupIds)) {
            throw new IllegalStateException("Groups should not be empty.");
        }

        Double result = getReporter(report).report(report, groupIds, app);
        return QNumberReport.builder().number(result).build();
    }

    private Set<String> allEligibleGroupIds(User user, String groupId, App app) {
        //这里本来应该正常使用AppOperatePermissionChecker获取可管理的group的，
        //但是由于只要可管理一个group，则可管理其下所有的group，因此采用下面的简单方式
        if (isBlank(groupId)) {
            managePermissionChecker.checkCanManageApp(user, app);
            return groupRepository.cachedAllVisibleGroupIds(app.getId());
        } else {
            Group group = groupRepository.cachedByIdAndCheckTenantShip(groupId, user);
            managePermissionChecker.checkCanManageGroup(user, group, app);
            return groupRepository.cachedWithAllSubVisibleGroupIds(app.getId(), group.getId());
        }
    }

    private NumberReporter getReporter(NumberReport report) {
        return reporters.stream().filter(generator -> generator.supports(report)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("No number report generator found."));
    }
}
