package com.mryqr.core.app.domain;

import com.mryqr.common.domain.UploadedFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.common.utils.UuidGenerator.newShortUuid;

@Component
@RequiredArgsConstructor
public class AppHeaderImageProvider {

    public UploadedFile defaultAppHeaderImage() {
        return UploadedFile.builder()
                .id(newShortUuid())
                .name("初始页眉图")
                //如果需要更换默认页眉图，可修改以下URL
                .fileUrl("https://mry-static.oss-cn-hangzhou.aliyuncs.com/default-app-header/cooperation.jpg")
                .ossKey(null)//ossKey为null时，前端删除时不会调用阿里云删除API
                .type("image/jpeg")
                .size(100)
                .build();
    }
}
