package com.mryqr.common.domain;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.tenant.domain.Tenant;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static com.mryqr.common.domain.user.Role.TENANT_ADMIN;
import static com.mryqr.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static org.junit.jupiter.api.Assertions.*;

class AggregateRootTest {
    private static final User TEST_USER = User.humanUser(Member.newMemberId(), "memberName", Tenant.newTenantId(), TENANT_ADMIN);

    @Test
    public void should_create() {
        String id = "Test" + newSnowflakeId();

        TestAggregate aggregate = new TestAggregate(id);
        assertEquals(id, aggregate.getId());
        assertEquals(TEST_USER.getTenantId(), aggregate.getTenantId());

        assertEquals(TEST_USER.getMemberId(), aggregate.getCreatedBy());
        assertNotNull(aggregate.getCreatedAt());

        assertNull(aggregate.getEvents());
        assertNull(aggregate.getOpsLogs());

        assertEquals(id, aggregate.getIdentifier());
    }

    @Test
    public void should_raise_event() {
        String id = "Test" + newSnowflakeId();
        TestAggregate aggregate = new TestAggregate(id);
        DomainEvent event = new DomainEvent() {
        };

        aggregate.raiseEvent(event);
        assertSame(event, aggregate.getEvents().get(0));
    }

    @Test
    public void should_add_ops_log() {
        String id = "Test" + newSnowflakeId();
        TestAggregate aggregate = new TestAggregate(id);
        String opsLog = "Hello ops logs";
        aggregate.addOpsLog(opsLog, TEST_USER);
        assertEquals(opsLog, aggregate.getOpsLogs().get(0).getNote());
    }

    @Test
    public void should_slice_ops_logs_if_too_much() {
        String id = "Test" + newSnowflakeId();
        TestAggregate aggregate = new TestAggregate(id);
        String opsLog = "Hello ops logs";
        IntStream.range(0, 21).forEach(i -> aggregate.addOpsLog(opsLog, TEST_USER));
        String lastLog = "last ops log";
        aggregate.addOpsLog(lastLog, TEST_USER);
        assertEquals(20, aggregate.getOpsLogs().size());
        assertEquals(lastLog, aggregate.getOpsLogs().get(19).getNote());
    }

    static class TestAggregate extends AggregateRoot {

        public TestAggregate(String id) {
            super(id, TEST_USER);
        }
    }

}