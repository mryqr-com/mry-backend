package com.mryqr.core.plate.domain;

import com.mryqr.core.common.domain.AggregateRoot;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.plate.domain.event.PlateBoundEvent;
import com.mryqr.core.plate.domain.event.PlateUnboundEvent;
import com.mryqr.core.platebatch.domain.PlateBatch;
import com.mryqr.core.qr.domain.QR;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

import static com.mryqr.core.common.exception.ErrorCode.PLATE_ALREADY_BOUND;
import static com.mryqr.core.common.exception.ErrorCode.PLATE_QR_NOT_MATCH;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static com.mryqr.core.common.utils.MryConstants.PLATE_COLLECTION;
import static com.mryqr.core.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Document(PLATE_COLLECTION)
@TypeAlias(PLATE_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class Plate extends AggregateRoot {
    private String qrId;//对应的QR的ID
    private String groupId;//对应QR所在group的ID，用于在删除group时同时通过EDA解绑该group下所有的QR
    private String appId;//对应的APP的ID
    private String batchId;//如果码牌属于某个批次，则batchId为对应PlateBatch的ID

    public Plate(QR qr, User user) {
        super(qr.getPlateId(), qr.getTenantId(), user);
        this.appId = qr.getAppId();
        this.bind(qr, user);
        addOpsLog("从[" + qr.getId() + "]新建", user);
    }

    public Plate(PlateBatch plateBatch, User user) {
        super(newPlateId(), plateBatch.getTenantId(), user);
        this.appId = plateBatch.getAppId();
        this.batchId = plateBatch.getId();
        addOpsLog("从批次[" + plateBatch.getId() + "]新建", user);
    }

    public static String newPlateId() {
        return "MRY" + newSnowflakeId();
    }

    public void bind(QR qr, User user) {
        checkBindStatus();
        this.qrId = qr.getId();
        this.groupId = qr.getGroupId();
        raiseEvent(new PlateBoundEvent(this.getId(), this.qrId, user));
        addOpsLog("绑定[" + qr.getId() + "]", user);
    }

    public void checkBindStatus() {
        if (this.isBound()) {
            throw new MryException(PLATE_ALREADY_BOUND, "码牌已经被占用，无法完成绑定。", mapOf("plateId", this.getId()));
        }
    }

    public void unBind(User user) {
        if (!isBound()) {
            return;
        }

        String oldQrId = this.qrId;
        this.qrId = null;
        this.groupId = null;
        raiseEvent(new PlateUnboundEvent(this.getId(), this.qrId, user));
        addOpsLog("从[" + oldQrId + "]解绑", user);
    }

    public boolean isBound() {
        return isNotBlank(this.qrId);
    }

    public boolean isBatched() {
        return isNotBlank(this.batchId);
    }

    public void syncGroupFromQr(QR qr, User user) {
        if (!Objects.equals(this.qrId, qr.getId())) {
            throw new MryException(PLATE_QR_NOT_MATCH, "码牌和QR不匹配。",
                    mapOf("plateId", this.getId(), "qrId", qr.getId()));
        }

        this.groupId = qr.getGroupId();
        addOpsLog("移动分组:" + this.groupId, user);
    }
}
