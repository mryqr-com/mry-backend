package com.mryqr.core.submission.domain.answer.multilevelselection;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static com.mryqr.core.app.domain.page.control.FMultiLevelSelectionControl.MAX_OPTION_NAME_LENGTH;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MultiLevelSelection {
    private static final String JOINNER = "/";

    @Size(max = MAX_OPTION_NAME_LENGTH)
    private final String level1;

    @Size(max = MAX_OPTION_NAME_LENGTH)
    private final String level2;

    @Size(max = MAX_OPTION_NAME_LENGTH)
    private final String level3;

    public boolean isFilled() {
        return isNotBlank(level1) ||
                isNotBlank(level2) ||
                isNotBlank(level3);
    }

    public Set<String> indexedValues() {
        if (!isFilled()) {
            return null;
        }

        if (isBlank(level1)) {
            return null;
        }

        Set<String> results = new HashSet<>();
        results.add(level1);
        if (isNotBlank(level2)) {
            results.add(joinLevels(level1, level2));
        }

        if (isNotBlank(level2) && isNotBlank(level3)) {
            results.add(joinLevels(level1, level2, level3));
        }
        return results;
    }

    public String displayValue() {
        return joinLevels(level1, level2, level3);
    }

    public static String joinLevels(String... levels) {
        return Stream.of(levels).filter(StringUtils::isNotBlank).collect(joining(JOINNER));
    }

    public String toText() {
        return joinLevels(level1, level2, level3);
    }

}
