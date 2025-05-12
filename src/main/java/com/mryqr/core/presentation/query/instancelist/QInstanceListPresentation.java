package com.mryqr.core.presentation.query.instancelist;

import com.mryqr.core.presentation.query.QControlPresentation;
import lombok.*;

import java.time.Instant;
import java.util.List;

import static com.mryqr.core.app.domain.page.control.ControlType.INSTANCE_LIST;
import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class QInstanceListPresentation extends QControlPresentation {
    private List<InstanceItem> instances;

    public QInstanceListPresentation(List<InstanceItem> instances) {
        super(INSTANCE_LIST);
        this.instances = instances;
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class InstanceItem {
        private String plateId;
        private String name;
        private String creator;
        private Instant createdAt;
    }
}
