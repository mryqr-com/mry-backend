package com.mryqr.core.common.user;

import com.mryqr.BaseUnitTest;
import com.mryqr.common.domain.user.Role;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import org.junit.jupiter.api.Test;

import static com.mryqr.common.domain.user.Role.*;
import static com.mryqr.common.domain.user.User.*;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.core.member.domain.Member.newMemberId;
import static com.mryqr.core.tenant.domain.Tenant.newTenantId;
import static org.junit.jupiter.api.Assertions.*;

class UserTest extends BaseUnitTest {

    @Test
    public void should_create_human_user() {
        String memberId = newMemberId();
        String tenantId = newTenantId();
        Role role = TENANT_ADMIN;
        User user = humanUser(memberId, "Alex", tenantId, role);
        assertEquals(user.getMemberId(), memberId);
        assertEquals(user.getTenantId(), tenantId);
        assertEquals(user.getRole(), role);
    }

    @Test
    public void should_fail_to_create_human_user_with_incomplete_info() {
        assertThrows(IllegalArgumentException.class, () -> humanUser(null, "Alex", newTenantId(), TENANT_ADMIN));
        assertThrows(IllegalArgumentException.class, () -> humanUser(newMemberId(), "Alex", null, TENANT_ADMIN));
        assertThrows(NullPointerException.class, () -> humanUser(newMemberId(), "Alex", newTenantId(), null));
    }

    @Test
    public void should_fail_to_create_robot_user_with_incomplete_info() {
        assertThrows(IllegalArgumentException.class, () -> robotUser(null));
    }

    @Test
    public void should_create_tenant_admin() {
        String memberId = newMemberId();
        String tenantId = newTenantId();
        User user = humanUser(memberId, "Alex", tenantId, TENANT_ADMIN);
        assertTrue(user.isLoggedIn());
        user.checkIsLoggedIn();
        assertTrue(user.isLoggedInFor(tenantId));
        assertFalse(user.isLoggedInFor(newTenantId()));
        user.checkIsLoggedInFor(tenantId);

    }


    @Test
    public void should_create_robot_user() {
        String tenantId = newTenantId();
        User user = robotUser(tenantId);
        assertNull(user.getMemberId());
        assertEquals(user.getTenantId(), tenantId);
        assertEquals(user.getRole(), ROBOT);
    }

    @Test
    public void should_create_anonymous_user() {
        User user = ANONYMOUS_USER;
        assertNull(user.getMemberId());
        assertNull(user.getTenantId());
        assertNull(user.getRole());
    }

    @Test
    public void should_logged_in_as_tenant_admin() {
        String memberId = newMemberId();
        String tenantId = newTenantId();
        User user = humanUser(memberId, "Alex", tenantId, TENANT_ADMIN);

        assertTrue(user.isLoggedIn());
        user.checkIsLoggedIn();

        assertTrue(user.isLoggedInFor(tenantId));
        assertFalse(user.isLoggedInFor(newTenantId()));

        user.checkIsLoggedInFor(tenantId);
        assertEquals(WRONG_TENANT, assertThrows(MryException.class, () -> user.checkIsLoggedInFor(newTenantId())).getCode());

        assertTrue(user.isTenantAdmin());
        user.checkIsTenantAdmin();

        assertTrue(user.isTenantAdminFor(tenantId));
        assertFalse(user.isTenantAdminFor(newTenantId()));

        user.checkIsTenantAdminFor(tenantId);
        assertEquals(WRONG_TENANT, assertThrows(MryException.class, () -> user.checkIsTenantAdminFor(newTenantId())).getCode());

        assertTrue(user.isTenantRoot());
        user.checkIsTenantRoot();

        assertTrue(user.isTenantRootFor(tenantId));
        assertFalse(user.isTenantRootFor(newTenantId()));

        user.checkIsTenantRootFor(tenantId);
        assertEquals(WRONG_TENANT, assertThrows(MryException.class, () -> user.checkIsTenantRootFor(newTenantId())).getCode());

        assertTrue(user.isHumanUser());
        user.checkIsHumanUser();

        assertTrue(user.isHumanUserFor(tenantId));
        assertEquals(WRONG_TENANT, assertThrows(MryException.class, () -> user.checkIsHumanUserFor(newTenantId())).getCode());
    }


    @Test
    public void should_logged_in_as_tenant_member() {
        String memberId = newMemberId();
        String tenantId = newTenantId();
        User user = humanUser(memberId, "Alex", tenantId, TENANT_MEMBER);

        assertTrue(user.isLoggedIn());
        user.checkIsLoggedIn();

        assertTrue(user.isLoggedInFor(tenantId));
        assertFalse(user.isLoggedInFor(newTenantId()));

        user.checkIsLoggedInFor(tenantId);
        assertEquals(WRONG_TENANT, assertThrows(MryException.class, () -> user.checkIsLoggedInFor(newTenantId())).getCode());

        assertFalse(user.isTenantAdmin());
        assertEquals(ACCESS_DENIED, assertThrows(MryException.class, user::checkIsTenantAdmin).getCode());

        assertFalse(user.isTenantAdminFor(tenantId));
        assertFalse(user.isTenantAdminFor(newTenantId()));

        assertEquals(ACCESS_DENIED, assertThrows(MryException.class, () -> user.checkIsTenantAdminFor(tenantId)).getCode());
        assertEquals(WRONG_TENANT, assertThrows(MryException.class, () -> user.checkIsTenantAdminFor(newTenantId())).getCode());

        assertFalse(user.isTenantRoot());
        assertEquals(ACCESS_DENIED, assertThrows(MryException.class, user::checkIsTenantRoot).getCode());

        assertFalse(user.isTenantRootFor(tenantId));
        assertFalse(user.isTenantRootFor(newTenantId()));

        assertEquals(ACCESS_DENIED, assertThrows(MryException.class, () -> user.checkIsTenantRootFor(tenantId)).getCode());
        assertEquals(WRONG_TENANT, assertThrows(MryException.class, () -> user.checkIsTenantRootFor(newTenantId())).getCode());

        assertTrue(user.isHumanUser());
        user.checkIsHumanUser();

        assertTrue(user.isHumanUserFor(tenantId));
        assertEquals(WRONG_TENANT, assertThrows(MryException.class, () -> user.checkIsHumanUserFor(newTenantId())).getCode());
    }

    @Test
    public void should_logged_in_as_robot() {
        String tenantId = newTenantId();
        User user = robotUser(tenantId);

        assertTrue(user.isLoggedIn());
        user.checkIsLoggedIn();

        assertTrue(user.isLoggedInFor(tenantId));
        assertFalse(user.isLoggedInFor(newTenantId()));

        user.checkIsLoggedInFor(tenantId);
        assertEquals(WRONG_TENANT, assertThrows(MryException.class, () -> user.checkIsLoggedInFor(newTenantId())).getCode());

        assertFalse(user.isTenantAdmin());
        assertEquals(ACCESS_DENIED, assertThrows(MryException.class, user::checkIsTenantAdmin).getCode());

        assertFalse(user.isTenantAdminFor(tenantId));
        assertFalse(user.isTenantAdminFor(newTenantId()));

        assertEquals(ACCESS_DENIED, assertThrows(MryException.class, () -> user.checkIsTenantAdminFor(tenantId)).getCode());
        assertEquals(WRONG_TENANT, assertThrows(MryException.class, () -> user.checkIsTenantAdminFor(newTenantId())).getCode());

        assertTrue(user.isTenantRoot());
        user.checkIsTenantRoot();

        assertTrue(user.isTenantRootFor(tenantId));
        assertFalse(user.isTenantRootFor(newTenantId()));

        user.checkIsTenantRootFor(tenantId);
        assertEquals(WRONG_TENANT, assertThrows(MryException.class, () -> user.checkIsTenantRootFor(newTenantId())).getCode());

        assertFalse(user.isHumanUser());
        assertEquals(ACCESS_DENIED, assertThrows(MryException.class, user::checkIsHumanUser).getCode());

        assertFalse(user.isHumanUserFor(tenantId));
        assertFalse(user.isHumanUserFor(newTenantId()));
        assertEquals(ACCESS_DENIED, assertThrows(MryException.class, () -> user.checkIsHumanUserFor(tenantId)).getCode());
        assertEquals(WRONG_TENANT, assertThrows(MryException.class, () -> user.checkIsHumanUserFor(newTenantId())).getCode());
    }


    @Test
    public void should_logged_in_as_anonymous() {
        User user = ANONYMOUS_USER;

        assertFalse(user.isLoggedIn());
        assertEquals(AUTHENTICATION_FAILED, assertThrows(MryException.class, user::checkIsLoggedIn).getCode());

        assertFalse(user.isLoggedInFor(newTenantId()));
        assertEquals(AUTHENTICATION_FAILED, assertThrows(MryException.class, () -> user.checkIsLoggedInFor(newTenantId())).getCode());

        assertFalse(user.isTenantAdmin());
        assertEquals(AUTHENTICATION_FAILED, assertThrows(MryException.class, user::checkIsTenantAdmin).getCode());

        assertFalse(user.isTenantAdminFor(newTenantId()));
        assertEquals(AUTHENTICATION_FAILED, assertThrows(MryException.class, () -> user.checkIsTenantAdminFor(newTenantId())).getCode());

        assertFalse(user.isTenantRoot());
        assertEquals(AUTHENTICATION_FAILED, assertThrows(MryException.class, user::checkIsTenantRoot).getCode());


        assertFalse(user.isTenantRootFor(newTenantId()));
        assertEquals(AUTHENTICATION_FAILED, assertThrows(MryException.class, () -> user.checkIsTenantRootFor(newTenantId())).getCode());

        assertFalse(user.isHumanUser());
        assertEquals(AUTHENTICATION_FAILED, assertThrows(MryException.class, user::checkIsHumanUser).getCode());

        assertFalse(user.isHumanUserFor(newTenantId()));
        assertEquals(AUTHENTICATION_FAILED, assertThrows(MryException.class, () -> user.checkIsHumanUserFor(newTenantId())).getCode());
    }

}