package com.mryqr.core.member.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.MEMBER_DELETED;
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
