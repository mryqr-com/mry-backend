package com.mryqr.core.order.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.ORDER_BANK_TRANSFER_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("ORDER_BANK_TRANSFER_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class OrderBankTransferUpdatedEvent extends OrderUpdatedEvent {

    public OrderBankTransferUpdatedEvent(String orderId, User user) {
        super(ORDER_BANK_TRANSFER_UPDATED, orderId, user);
    }
}
