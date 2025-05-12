package com.mryqr.core.platebatch.domain.event;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.PLATE_BATCH_CREATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("PLATE_BATCH_CREATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class PlateBatchCreatedEvent extends DomainEvent {
    private String batchId;
    private int plateCount;

    public PlateBatchCreatedEvent(String batchId, int plateCount, User user) {
        super(PLATE_BATCH_CREATED, user);
        this.batchId = batchId;
        this.plateCount = plateCount;
    }
}
