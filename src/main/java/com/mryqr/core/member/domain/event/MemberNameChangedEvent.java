package com.mryqr.core.member.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.MEMBER_NAME_CHANGED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("MEMBER_NAME_CHANGED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class MemberNameChangedEvent extends DomainEvent {
    private String memberId;
    private String newName;

    public MemberNameChangedEvent(String memberId, String newName, User user) {
        super(MEMBER_NAME_CHANGED, user);
        this.memberId = memberId;
        this.newName = newName;
    }
}
