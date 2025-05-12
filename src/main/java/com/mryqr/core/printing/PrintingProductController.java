package com.mryqr.core.printing;

import com.mryqr.core.printing.query.PrintingProductQueryService;
import com.mryqr.core.printing.query.QPrintingProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/printing-products")
public class PrintingProductController {
    private final PrintingProductQueryService printingProductQueryService;

    @GetMapping
    public List<QPrintingProduct> listPrintingProducts() {
        return printingProductQueryService.listPrintingProducts();
    }

}
