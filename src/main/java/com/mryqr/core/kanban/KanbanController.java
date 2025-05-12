package com.mryqr.core.kanban;

import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.kanban.query.FetchKanbanQuery;
import com.mryqr.core.kanban.query.KanbanQueryService;
import com.mryqr.core.kanban.query.QAttributeKanban;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@Validated
@RestController
@RequestMapping(value = "/kanban")
@RequiredArgsConstructor
public class KanbanController {
    private final KanbanQueryService kanbanQueryService;

    @PostMapping
    public QAttributeKanban fetchKanban(@RequestBody @Valid FetchKanbanQuery queryCommand,
                                        @AuthenticationPrincipal User user) {
        return kanbanQueryService.fetchKanban(queryCommand, user);
    }

}
