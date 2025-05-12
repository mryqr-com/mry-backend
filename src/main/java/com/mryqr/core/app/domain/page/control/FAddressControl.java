package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.common.domain.Address;
import com.mryqr.core.common.domain.administrative.Administrative;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.address.AddressAnswer;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Optional;

import static com.mryqr.core.common.domain.administrative.AdministrativeProvider.CHINA;
import static com.mryqr.core.common.exception.ErrorCode.CITY_NOT_PROVIDED;
import static com.mryqr.core.common.exception.ErrorCode.DETAIL_ADDRESS_NOT_PROVIDED;
import static com.mryqr.core.common.exception.ErrorCode.DISTRICT_NOT_PROVIDED;
import static com.mryqr.core.common.exception.ErrorCode.PROVINCE_NOT_PROVIDED;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@SuperBuilder
@TypeAlias("ADDRESS_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FAddressControl extends Control {
    public static final int MIN_ADDRESS_PRECISION = 1;
    public static final int MAX_ADDRESS_PRECISION = 4;

    private boolean positionable;//是否可定位

    @Min(value = MIN_ADDRESS_PRECISION)
    @Max(value = MAX_ADDRESS_PRECISION)
    private int precision;//精度，1：省份；2：城市；3：区县；4：详细地址

    @Override
    protected void doCorrect(AppSettingContext context) {
    }

    @Override
    protected void doValidate(AppSettingContext context) {
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

    public AddressAnswer check(AddressAnswer answer) {
        Address address = answer.getAddress();

        switch (precision) {
            case 1 -> {
                checkProvince(address);
                return AddressAnswer.builder()
                        .controlId(answer.getControlId())
                        .controlType(answer.getControlType())
                        .address(Address.builder()
                                .province(address.getProvince())
                                .build())
                        .build();
            }

            case 2 -> {
                checkProvince(address);
                checkCity(address);

                return AddressAnswer.builder()
                        .controlId(answer.getControlId())
                        .controlType(answer.getControlType())
                        .address(Address.builder()
                                .province(address.getProvince())
                                .city(address.getCity())
                                .build())
                        .build();
            }

            case 3 -> {
                checkProvince(address);
                checkCity(address);
                checkDistrict(address);

                return AddressAnswer.builder()
                        .controlId(answer.getControlId())
                        .controlType(answer.getControlType())
                        .address(Address.builder()
                                .province(address.getProvince())
                                .city(address.getCity())
                                .district(address.getDistrict())
                                .build())
                        .build();
            }

            case 4 -> {
                checkProvince(address);
                checkCity(address);
                checkDistrict(address);
                checkAddress(address);
                return answer;
            }
            default -> {
                throw new RuntimeException("Address precision:[" + this.getId() + "] not supported。");
            }
        }
    }

    private void checkProvince(Address address) {
        if (isBlank(address.getProvince())) {
            failAnswerValidation(PROVINCE_NOT_PROVIDED, "未填写省份信息。");
        }
    }

    private void checkCity(Address address) {
        Optional<Administrative> provinceOptional = CHINA.subAdministrativeByName(address.getProvince());

        //有些省份下本来就没有城市，则不检查
        if (provinceOptional.isEmpty() || isEmpty(provinceOptional.get().getChild())) {
            return;
        }

        if (isBlank(address.getCity())) {
            failAnswerValidation(CITY_NOT_PROVIDED, "未填写城市信息。");
        }
    }

    private void checkDistrict(Address address) {
        Optional<Administrative> provinceOptional = CHINA.subAdministrativeByName(address.getProvince());
        //有些省份下本来就没有城市，则不检查
        if (provinceOptional.isEmpty() || isEmpty(provinceOptional.get().getChild())) {
            return;
        }

        Optional<Administrative> cityOptional = provinceOptional.get().subAdministrativeByName(address.getCity());
        //有些城市下本来就没有区县，则不检查
        if (cityOptional.isEmpty() || isEmpty(cityOptional.get().getChild())) {
            return;
        }

        if (isBlank(address.getDistrict())) {
            failAnswerValidation(DISTRICT_NOT_PROVIDED, "未填写区县信息。");
        }
    }

    private void checkAddress(Address address) {
        if (isBlank(address.getAddress())) {
            failAnswerValidation(DETAIL_ADDRESS_NOT_PROVIDED, "未填写地址详情。");
        }
    }

}
