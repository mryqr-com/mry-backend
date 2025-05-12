package com.mryqr.core.common.domain;

import com.mryqr.core.common.utils.Identified;
import com.mryqr.core.common.validation.id.shoruuid.ShortUuid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.mryqr.core.common.utils.MryConstants.MAX_URL_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class UploadedFile implements Identified {
    @NotBlank
    @ShortUuid
    private final String id;//id，用于前端loop时作为key

    @Size(max = 200)
    private String name;//文件名称

    @NotBlank
    @Size(max = 500)
    private final String type;//文件类型

    @NotBlank
    @Size(max = MAX_URL_LENGTH)
    private final String fileUrl;//文件url

    @Size(max = 500)
    private final String ossKey;//阿里云的文件key

    @Min(0)
    private final int size;//文件大小

    @Override
    public String getIdentifier() {
        return id;
    }

    public void correct() {
        if (isBlank(name)) {
            this.name = "未命名";
        }
    }
}
