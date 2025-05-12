package com.mryqr.core.plate.domain.event;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.PLATE_UNBOUND;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("PLATE_UNBOUND_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class PlateUnboundEvent extends DomainEvent {
    private String plateId;
    private String qrId;

    public PlateUnboundEvent(String plateId, String qrId, User user) {
        super(PLATE_UNBOUND, user);
        this.plateId = plateId;
        this.qrId = qrId;
    }
}
