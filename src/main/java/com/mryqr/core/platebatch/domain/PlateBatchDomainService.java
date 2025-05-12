package com.mryqr.core.platebatch.domain;

import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.mryqr.core.common.exception.ErrorCode.PLATE_BATCH_WITH_NAME_ALREADY_EXISTS;
import static com.mryqr.core.common.utils.MapUtils.mapOf;

@Component
@RequiredArgsConstructor
public class PlateBatchDomainService {
    private final PlateBatchRepository plateBatchRepository;

    public void rename(PlateBatch plateBatch, String newName, User user) {
        if (!Objects.equals(plateBatch.getName(), newName) && plateBatchRepository.existsByName(newName, plateBatch.getAppId())) {
            throw new MryException(PLATE_BATCH_WITH_NAME_ALREADY_EXISTS, "重命名码牌批次失败，名称已被占用。",
                    mapOf("id", plateBatch.getId(), "name", newName));
        }

        plateBatch.rename(newName, user);
    }
}
