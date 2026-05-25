package com.mryqr.core.printing;

import com.mryqr.BaseApiTest;
import com.mryqr.core.printing.query.QPrintingProduct;
import io.restassured.common.mapper.TypeRef;

import java.util.List;

public class PrintingProductApi {

    public static List<QPrintingProduct> listPrintingProducts() {
        return BaseApiTest.given()
                .when()
                .get("/printing-products")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

}
