package com.mryqr.core.platebatch.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.PLATE_BATCH_DELETED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("PLATE_BATCH_DELETED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class PlateBatchDeletedEvent extends DomainEvent {
    private String batchId;

    public PlateBatchDeletedEvent(String batchId, User user) {
        super(PLATE_BATCH_DELETED, user);
        this.batchId = batchId;
    }
}
