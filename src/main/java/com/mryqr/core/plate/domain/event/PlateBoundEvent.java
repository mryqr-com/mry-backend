package com.mryqr.core.plate.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.PLATE_BOUND;
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
