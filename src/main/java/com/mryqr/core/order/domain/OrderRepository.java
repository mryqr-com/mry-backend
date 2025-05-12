package com.mryqr.core.order.domain;

import com.mryqr.core.common.domain.user.User;

import java.util.Optional;

public interface OrderRepository {
    void save(Order it);

    Order byId(String id);

    Optional<Order> byIdOptional(String id);

    Order byIdAndCheckTenantShip(String id, User user);

    void delete(Order it);
}
