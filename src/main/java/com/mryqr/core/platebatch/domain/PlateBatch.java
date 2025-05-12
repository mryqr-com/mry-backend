package com.mryqr.core.platebatch.domain;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.common.domain.AggregateRoot;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.platebatch.domain.event.PlateBatchCreatedEvent;
import com.mryqr.core.platebatch.domain.event.PlateBatchDeletedEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Objects;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.core.common.utils.MryConstants.PLATE_BATCH_COLLECTION;
import static com.mryqr.core.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static java.util.stream.IntStream.range;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Document(PLATE_BATCH_COLLECTION)
@TypeAlias(PLATE_BATCH_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class PlateBatch extends AggregateRoot {
    private String name;//批次名称
    private String appId;//对应App的ID
    private int totalCount;//该批次下总共的码牌数量
    private int usedCount;//已用数量，即已经绑定QR的码牌数量

    public PlateBatch(String name, int totalCount, App app, User user) {
        super(newPlateBatchId(), app.getTenantId(), user);
        this.name = name;
        this.appId = app.getId();
        this.totalCount = totalCount;
        this.usedCount = 0;
        raiseEvent(new PlateBatchCreatedEvent(this.getId(), totalCount, user));
        addOpsLog("新建", user);
    }

    public static String newPlateBatchId() {
        return "BCH" + newSnowflakeId();
    }

    public List<Plate> createPlates(User user) {
        return range(0, totalCount).mapToObj(i -> new Plate(this, user)).collect(toImmutableList());
    }

    public void updateUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    public void rename(String name, User user) {
        if (Objects.equals(this.name, name)) {
            return;
        }

        this.name = name;
        addOpsLog("重命名为[" + name + "]", user);
    }

    public int getAvailableCount() {
        return this.totalCount - this.usedCount;
    }

    public void onDelete(User user) {
        this.raiseEvent(new PlateBatchDeletedEvent(this.getId(), user));
    }
}
