package com.mryqr.core.order.infrastructure;

import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.order.domain.Order;
import com.mryqr.core.order.domain.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MongoOrderRepository extends MongoBaseRepository<Order> implements OrderRepository {
    @Override
    public void save(Order it) {
        super.save(it);
    }

    @Override
    public Order byId(String id) {
        return super.byId(id);
    }

    @Override
    public Optional<Order> byIdOptional(String id) {
        return super.byIdOptional(id);
    }

    @Override
    public Order byIdAndCheckTenantShip(String id, User user) {
        return super.byIdAndCheckTenantShip(id, user);
    }

    @Override
    public void delete(Order it) {
        super.delete(it);
    }

}
