package com.mryqr.core.report;

import com.mryqr.BaseApiTest;
import com.mryqr.core.report.query.chart.ChartReportQuery;
import com.mryqr.core.report.query.chart.QChartReport;
import com.mryqr.core.report.query.number.NumberReportQuery;
import com.mryqr.core.report.query.number.QNumberReport;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

public class ReportApi {
    public static QNumberReport fetchNumberReport(String jwt, NumberReportQuery query) {
        return fetchNumberReportRaw(jwt, query)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static Response fetchNumberReportRaw(String jwt, NumberReportQuery query) {
        return BaseApiTest.given(jwt)
                .body(query)
                .when()
                .post("/reports/number");
    }

    public static QChartReport fetchChartReport(String jwt, ChartReportQuery query) {
        return fetchChartReportRaw(jwt, query)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static Response fetchChartReportRaw(String jwt, ChartReportQuery query) {
        return BaseApiTest.given(jwt)
                .body(query)
                .when()
                .post("/reports/chart");
    }


}
