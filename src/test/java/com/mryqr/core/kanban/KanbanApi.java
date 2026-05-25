package com.mryqr.core.kanban;

import com.mryqr.BaseApiTest;
import com.mryqr.core.kanban.query.FetchKanbanQuery;
import com.mryqr.core.kanban.query.QAttributeKanban;
import io.restassured.response.Response;

public class KanbanApi {
    public static QAttributeKanban fetchKanban(String jwt, FetchKanbanQuery command) {
        return fetchKanbanRaw(jwt, command)
                .then()
                .statusCode(200)
                .extract()
                .as(QAttributeKanban.class);
    }

    public static Response fetchKanbanRaw(String jwt, FetchKanbanQuery command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/kanban");
    }
}
