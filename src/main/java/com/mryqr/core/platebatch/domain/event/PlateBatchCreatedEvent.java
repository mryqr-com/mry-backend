package com.mryqr.core.platebatch.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.PLATE_BATCH_CREATED;
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
