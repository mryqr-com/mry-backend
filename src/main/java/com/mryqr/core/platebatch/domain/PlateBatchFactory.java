package com.mryqr.core.platebatch.domain;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.plate.domain.Plate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.core.common.exception.ErrorCode.PLATE_BATCH_WITH_NAME_ALREADY_EXISTS;
import static com.mryqr.core.common.utils.MapUtils.mapOf;

@Component
@RequiredArgsConstructor
public class PlateBatchFactory {
    private final PlateBatchRepository plateBatchRepository;

    public CreatePlateBatchResult create(String name, int total, App app, User user) {
        if (plateBatchRepository.existsByName(name, app.getId())) {
            throw new MryException(PLATE_BATCH_WITH_NAME_ALREADY_EXISTS, "创建码牌批次失败，名称已被占用。", mapOf("name", name));
        }

        PlateBatch plateBatch = new PlateBatch(name, total, app, user);
        List<Plate> plates = plateBatch.createPlates(user);

        return CreatePlateBatchResult.builder().plateBatch(plateBatch).plates(plates).build();
    }
}
