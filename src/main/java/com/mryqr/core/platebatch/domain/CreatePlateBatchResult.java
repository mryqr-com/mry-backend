package com.mryqr.core.platebatch.domain;

import com.mryqr.core.plate.domain.Plate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CreatePlateBatchResult {
    private final PlateBatch plateBatch;
    private final List<Plate> plates;
}
