package com.mryqr.core.member.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.common.event.DomainEventType.MEMBER_DEPARTMENTS_CHANGED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("MEMBER_DEPARTMENTS_CHANGED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class MemberDepartmentsChangedEvent extends DomainEvent {
    private String memberId;
    private Set<String> removedDepartmentIds;
    private Set<String> addedDepartmentIds;

    public MemberDepartmentsChangedEvent(String memberId,
                                         Set<String> removedDepartmentIds,
                                         Set<String> addedDepartmentIds,
                                         User user) {
        super(MEMBER_DEPARTMENTS_CHANGED, user);
        this.memberId = memberId;
        this.removedDepartmentIds = removedDepartmentIds;
        this.addedDepartmentIds = addedDepartmentIds;
    }
}
