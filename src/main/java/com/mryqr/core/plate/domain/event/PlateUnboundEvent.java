package com.mryqr.core.plate.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.PLATE_UNBOUND;
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
