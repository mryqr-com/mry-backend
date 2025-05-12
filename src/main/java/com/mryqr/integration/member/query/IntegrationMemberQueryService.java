package com.mryqr.integration.member.query;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.common.utils.MryConstants.MEMBER_COLLECTION;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class IntegrationMemberQueryService {
    private final MemberRepository memberRepository;
    private final MryRateLimiter mryRateLimiter;
    private final MongoTemplate mongoTemplate;

    public QIntegrationMember fetchMember(String memberId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Member:Fetch", 10);

        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        return transform(member);
    }

    public QIntegrationMember fetchMemberByCustomId(String customId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Member:Custom:Fetch", 10);

        Member member = memberRepository.byCustomIdAndCheckTenantShip(user.getTenantId(), customId, user);
        return transform(member);
    }

    private QIntegrationMember transform(Member member) {
        return QIntegrationMember.builder()
                .id(member.getId())
                .name(member.getName())
                .role(member.getRole())
                .mobile(member.getMobile())
                .email(member.getEmail())
                .avatar(member.getAvatar())
                .customId(member.getCustomId())
                .active(member.isActive())
                .departmentIds(member.getDepartmentIds())
                .createdAt(member.getCreatedAt())
                .createdBy(member.getCreatedBy())
                .build();
    }

    public List<QIntegrationListMember> listMembers(User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Member:List", 10);

        Query query = Query.query(where("tenantId").is(user.getTenantId())).with(by(DESC, "createdAt"));
        query.fields().include("name", "role", "mobile", "email", "active", "customId", "departmentIds");

        return mongoTemplate.find(query, QIntegrationListMember.class, MEMBER_COLLECTION);
    }
}
