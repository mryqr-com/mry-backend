package com.mryqr.core.printing;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.UploadedFile;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.FDropdownControl;
import com.mryqr.core.app.domain.page.control.FMultiLineTextControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.login.LoginApi;
import com.mryqr.core.printing.query.QPlatePrintingType;
import com.mryqr.core.printing.query.QPrintingProduct;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.answer.dropdown.DropdownAnswer;
import com.mryqr.core.submission.domain.answer.multilinetext.MultiLineTextAnswer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.mryqr.common.domain.user.User.NOUSER;
import static com.mryqr.core.printing.domain.MaterialType.TRANSPARENT_ACRYLIC;
import static com.mryqr.management.MryManageTenant.ADMIN_INIT_MOBILE;
import static com.mryqr.management.MryManageTenant.ADMIN_INIT_PASSWORD;
import static com.mryqr.management.printingproduct.PrintingProductApp.*;
import static com.mryqr.utils.RandomTestFixture.rAnswerBuilder;
import static com.mryqr.utils.RandomTestFixture.rImageFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrintingProductControllerApiTest extends BaseApiTest {

    @Test
    public void should_list_printing_products() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        CreateQrResponse qrResponse = QrApi.createQr(jwt, PP_GROUP_ID);
        QR qr = qrRepository.byId(qrResponse.getQrId());
        UploadedFile headerImage = rImageFile();
        qr.updateHeaderImage(headerImage, NOUSER);
        qrRepository.save(qr);

        App app = appRepository.byId(PP_APP_ID);

        FDropdownControl materialTypeControl = (FDropdownControl) app.controlById(PP_MATERIAL_TYPE_CONTROL_ID);
        DropdownAnswer materialTypeAnswer = rAnswerBuilder(materialTypeControl).optionIds(List.of(PP_TRANSPARENT_ACRYLIC_OPTION_ID)).build();

        FSingleLineTextControl descriptionControl = (FSingleLineTextControl) app.controlById(PP_DESCRIPTION_CONTROL_ID);
        SingleLineTextAnswer descriptionAnswer = rAnswerBuilder(descriptionControl).content("blah").build();

        FMultiLineTextControl introductionControl = (FMultiLineTextControl) app.controlById(PP_INTRODUCTION_CONTROL_ID);
        MultiLineTextAnswer introductionAnswer = rAnswerBuilder(introductionControl).content("blah blah").build();

        SubmissionApi.newSubmission(jwt, qrResponse.getQrId(), PP_HOME_PAGE_ID, materialTypeAnswer, descriptionAnswer, introductionAnswer);

        List<QPrintingProduct> products = PrintingProductApi.listPrintingProducts();
        QPrintingProduct product = products.get(0);
        assertEquals(TRANSPARENT_ACRYLIC, product.getMaterialType());
        assertEquals(descriptionAnswer.getContent(), product.getDescription());
        assertEquals(introductionAnswer.getContent(), product.getIntroduction());
        assertEquals(TRANSPARENT_ACRYLIC.getName(), product.getName());
        assertEquals(headerImage, product.getImage());
        assertEquals(17, product.getPrintingTypes().size());
        QPlatePrintingType printingType = product.getPrintingTypes().get(0);
        assertEquals(TRANSPARENT_ACRYLIC, printingType.getMaterialType());
    }
}
