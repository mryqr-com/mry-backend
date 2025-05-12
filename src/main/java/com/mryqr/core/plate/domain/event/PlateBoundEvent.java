package com.mryqr.core.plate.domain.event;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.PLATE_BOUND;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("PLATE_BOUND_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class PlateBoundEvent extends DomainEvent {
    private String plateId;
    private String qrId;

    public PlateBoundEvent(String plateId, String qrId, User user) {
        super(PLATE_BOUND, user);
        this.plateId = plateId;
        this.qrId = qrId;
    }
}
