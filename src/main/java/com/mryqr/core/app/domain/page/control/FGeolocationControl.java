package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.ButtonStyle;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.geolocation.GeolocationAnswer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("GEOLOCATION_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FGeolocationControl extends Control {
    private boolean allowRandomPosition;//允许定位任意位置

    private boolean offsetRestrictionEnabled;//限制提交定位与qr定位的偏离半径

    @Min(50)
    @Max(100000)
    private int offsetRestrictionRadius;//最大偏离半径(米)

    @Valid
    @NotNull
    private ButtonStyle buttonStyle;//按钮样式

    @Override
    protected void doCorrect(AppSettingContext context) {
        if (!context.isGeolocationEnabled()) {
            this.offsetRestrictionEnabled = false;
        }

        if (!this.offsetRestrictionEnabled) {
            this.offsetRestrictionRadius = 500;
        }
    }

    @Override
    protected void doValidate(AppSettingContext context) {
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

    public GeolocationAnswer check(GeolocationAnswer answer) {
        return answer;
    }

}
