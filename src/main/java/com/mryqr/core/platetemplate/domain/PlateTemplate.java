package com.mryqr.core.platetemplate.domain;

import com.mryqr.core.app.domain.plate.PlateSetting;
import com.mryqr.core.common.domain.AggregateRoot;
import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.mryqr.core.common.exception.ErrorCode.PLATE_SETTING_HAS_ATTRIBUTES;
import static com.mryqr.core.common.exception.ErrorCode.PLATE_SETTING_NOT_COMPLETE;
import static com.mryqr.core.common.utils.MryConstants.PLATE_TEMPLATE_COLLECTION;
import static com.mryqr.core.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Document(PLATE_TEMPLATE_COLLECTION)
@TypeAlias(PLATE_TEMPLATE_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class PlateTemplate extends AggregateRoot {
    private PlateSetting plateSetting;
    private UploadedFile image;
    private int order;

    public PlateTemplate(PlateSetting plateSetting, User user) {
        super(newPlateTemplateId(), user);

        if (!plateSetting.isComplete()) {
            throw new MryException(PLATE_SETTING_NOT_COMPLETE, "模板不完整，无法生成模板库。");
        }

        if (plateSetting.isAttributeReferenced()) {
            throw new MryException(PLATE_SETTING_HAS_ATTRIBUTES, "模板存在自定义属性引用，无法生成模板库。");
        }

        this.plateSetting = plateSetting;
        addOpsLog("新建", user);
    }

    public static String newPlateTemplateId() {
        return "PT" + newSnowflakeId();
    }

    public void update(UploadedFile image, int order, User user) {
        this.image = image;
        this.order = order;
        addOpsLog("更新设置", user);
    }

}
