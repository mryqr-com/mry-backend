package com.mryqr.core.member.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.MEMBER_CREATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("MEMBER_CREATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class MemberCreatedEvent extends DomainEvent {
    private String memberId;

    public MemberCreatedEvent(String memberId, User user) {
        super(MEMBER_CREATED, user);
        this.memberId = memberId;
    }

}
