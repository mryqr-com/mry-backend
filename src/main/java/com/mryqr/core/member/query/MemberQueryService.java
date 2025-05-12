package com.mryqr.core.member.query;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.utils.Pagination;
import com.mryqr.core.department.domain.DepartmentRepository;
import com.mryqr.core.department.domain.TenantCachedDepartment;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchyRepository;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberReference;
import com.mryqr.core.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.mryqr.common.utils.CommonUtils.*;
import static com.mryqr.common.utils.MongoCriteriaUtils.regexSearch;
import static com.mryqr.common.utils.MryConstants.CHINESE_COLLATOR;
import static com.mryqr.common.utils.MryConstants.MEMBER_COLLECTION;
import static com.mryqr.common.utils.Pagination.pagination;
import static com.mryqr.common.validation.id.member.MemberIdValidator.isMemberId;
import static java.util.regex.Pattern.matches;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberQueryService {
    private final static Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "active");

    private final MemberRepository memberRepository;
    private final DepartmentHierarchyRepository departmentHierarchyRepository;
    private final DepartmentRepository departmentRepository;
    private final MongoTemplate mongoTemplate;
    private final MryRateLimiter mryRateLimiter;

    public QMemberInfo fetchMyMemberInfo(User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Member:FetchMyMemberInfo", 10);

        String memberId = user.getMemberId();
        Member member = memberRepository.byId(memberId);
        List<String> departmentIds = member.getDepartmentIds();
        List<String> departmentNames = List.of();

        if (isNotEmpty(departmentIds)) {
            DepartmentHierarchy departmentHierarchy = departmentHierarchyRepository.cachedByTenantId(user.getTenantId());
            List<TenantCachedDepartment> cachedDepartments = departmentRepository.cachedTenantAllDepartments(departmentHierarchy.getTenantId());
            Map<String, String> allDepartmentNames = cachedDepartments.stream()
                    .collect(toImmutableMap(TenantCachedDepartment::getId, TenantCachedDepartment::getName));
            Map<String, String> departmentFullNames = departmentHierarchy.departmentFullNames(allDepartmentNames);
            departmentNames = departmentIds.stream().map(departmentFullNames::get).filter(Objects::nonNull).collect(toImmutableList());
        }

        return QMemberInfo.builder()
                .memberId(member.getId())
                .tenantId(member.getTenantId())
                .name(member.getName())
                .email(member.getEmail())
                .mobile(member.getMobile())
                .wxNickName(member.getWxNickName())
                .wxBound(member.isWxBound())
                .role(member.getRole())
                .departments(departmentNames)
                .build();
    }

    public QMemberBaseSetting fetchMyBaseSetting(User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Member:FetchMyBaseSetting", 10);

        String memberId = user.getMemberId();
        Member member = memberRepository.byId(memberId);
        return QMemberBaseSetting.builder().id(memberId).name(member.getName()).build();
    }

    public List<QMemberReference> listMemberReferences(String tenantId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Member:FetchAllMemberReferencesTenant", 100);

        user.checkIsLoggedInFor(tenantId);
        return doListMemberReferences(user.getTenantId());
    }

    public List<QMemberReference> listMemberReferences(User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Member:FetchAllMemberReferences", 100);

        return doListMemberReferences(user.getTenantId());
    }

    private List<QMemberReference> doListMemberReferences(String tenantId) {
        List<MemberReference> memberReferences = memberRepository.cachedAllMemberReferences(tenantId);
        return memberReferences.stream()
                .sorted((o1, o2) -> CHINESE_COLLATOR.compare(o1.getName(), o2.getName()))
                .map(member -> {
                    String suffix = isNotBlank(member.getMobile()) ? "（" + maskMobile(member.getMobile()) + "）" : "";

                    return QMemberReference.builder()
                            .id(member.getId())
                            .showName(member.getName() + suffix)
                            .build();
                })
                .collect(toImmutableList());
    }

    public PagedList<QListMember> listMyManagedMembers(ListMyManagedMembersQuery queryCommand, User user) {
        String tenantId = user.getTenantId();
        mryRateLimiter.applyFor(tenantId, "Member:List", 10);

        Pagination pagination = pagination(queryCommand.getPageIndex(), queryCommand.getPageSize());
        String departmentId = queryCommand.getDepartmentId();
        String search = queryCommand.getSearch();

        Query query = new Query(buildMemberQueryCriteria(tenantId, departmentId, search));

        long count = mongoTemplate.count(query, Member.class);
        if (count == 0) {
            return pagedList(pagination, 0, List.of());
        }

        query.skip(pagination.skip()).limit(pagination.limit()).with(sort(queryCommand));
        query.fields().include("name").include("avatar").include("role").include("mobile")
                .include("wxUnionId").include("wxNickName").include("email")
                .include("active").include("createdAt").include("departmentIds");
        List<QListMember> members = mongoTemplate.find(query, QListMember.class, MEMBER_COLLECTION);
        return pagedList(pagination, (int) count, members);
    }

    private PagedList<QListMember> pagedList(Pagination pagination, int count, List<QListMember> members) {
        return PagedList.<QListMember>builder()
                .totalNumber(count)
                .pageSize(pagination.getPageSize())
                .pageIndex(pagination.getPageIndex())
                .data(members)
                .build();
    }

    private Criteria buildMemberQueryCriteria(String tenantId,
                                              String departmentId,
                                              String search) {
        Criteria criteria = where("tenantId").is(tenantId);

        if (isNotBlank(departmentId)) {
            criteria.and("departmentIds").is(departmentId);
        }

        //1. search为空时返回
        if (isBlank(search)) {
            return criteria;
        }

        //2. 直接根据id搜索
        if (isMemberId(search)) {
            return criteria.and("_id").is(search);
        }

        //3. search为手机号时，精确手机号查询
        if (isMobileNumber(search)) {
            return criteria.and("mobile").is(search);
        }

        //4. search为邮箱时，精确邮箱查询
        if (isEmail(search)) {
            return criteria.and("email").is(search);
        }

        //5. 当为部分手机号时，用正则搜索MOBILE
        if (matches("^[0-9]{4,11}$", search)) {
            return criteria.and("mobile").regex(search);
        }

        //6. 其他情况下，用正则搜索name或email或customId
        return criteria.orOperator(where("customId").is(search), regexSearch("name", search), where("email").regex(search));
    }

    private Sort sort(ListMyManagedMembersQuery queryCommand) {
        String sortedBy = queryCommand.getSortedBy();

        if (isBlank(sortedBy) || !ALLOWED_SORT_FIELDS.contains(sortedBy)) {
            return by(DESC, "createdAt");
        }

        Sort.Direction direction = queryCommand.isAscSort() ? ASC : DESC;
        if (Objects.equals(sortedBy, "createdAt")) {
            return by(direction, "createdAt");
        }

        return by(direction, sortedBy).and(by(DESC, "createdAt"));
    }

}
