package com.mryqr.core.app.domain.page.control;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MultiLevelOption {
    private String name;
    private List<MultiLevelOption> options;
    private double numericalValue;

    public Optional<MultiLevelOption> childOption(String optionName) {
        return options.stream()
                .filter(option -> option.getName().equals(optionName))
                .findFirst();
    }
}
