package com.mryqr.common.domain.administrative;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class Administrative {
    private final String name;

    @JsonInclude(NON_NULL)
    private final List<Administrative> child;

    public Optional<Administrative> subAdministrativeByName(String name) {
        return child.stream()
                .filter(aChild -> aChild.getName().equals(name))
                .findFirst();
    }
}
