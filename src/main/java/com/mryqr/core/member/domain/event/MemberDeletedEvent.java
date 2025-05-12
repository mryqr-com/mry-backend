package com.mryqr.core.member.domain.event;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.MEMBER_DELETED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("MEMBER_DELETED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class MemberDeletedEvent extends DomainEvent {
    private String memberId;

    public MemberDeletedEvent(String memberId, User user) {
        super(MEMBER_DELETED, user);
        this.memberId = memberId;
    }

}
