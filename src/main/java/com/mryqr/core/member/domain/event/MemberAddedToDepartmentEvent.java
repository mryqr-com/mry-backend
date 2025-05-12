package com.mryqr.core.member.domain.event;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.MEMBER_ADDED_TO_DEPARTMENT;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("MEMBER_ADDED_TO_DEPARTMENT_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class MemberAddedToDepartmentEvent extends DomainEvent {
    private String memberId;
    private String departmentId;

    public MemberAddedToDepartmentEvent(String memberId, String departmentId, User user) {
        super(MEMBER_ADDED_TO_DEPARTMENT, user);
        this.memberId = memberId;
        this.departmentId = departmentId;
    }
}
