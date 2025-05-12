package com.mryqr.core.submission;

import com.mryqr.BaseApiTest;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.utils.ReturnId;
import com.mryqr.core.submission.command.ApproveSubmissionCommand;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.command.UpdateSubmissionCommand;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.query.QDetailedSubmission;
import com.mryqr.core.submission.query.autocalculate.AutoCalculateQuery;
import com.mryqr.core.submission.query.autocalculate.ItemStatusAutoCalculateResponse;
import com.mryqr.core.submission.query.autocalculate.NumberInputAutoCalculateResponse;
import com.mryqr.core.submission.query.list.ListSubmissionsQuery;
import com.mryqr.core.submission.query.list.QListSubmission;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.mryqr.utils.RandomTestFixture.rSentence;

public class SubmissionApi {
    public static Response newSubmissionRaw(String jwt, NewSubmissionCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/submissions");
    }

    public static String newSubmission(String jwt, NewSubmissionCommand command) {
        return newSubmissionRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class).toString();
    }

    public static String newSubmission(String jwt, String qrId, String pageId, Set<Answer> answers) {
        NewSubmissionCommand command = NewSubmissionCommand.builder().qrId(qrId).pageId(pageId).answers(answers).build();
        return newSubmission(jwt, command);
    }

    public static String newSubmission(String jwt, String qrId, String pageId, Answer... answers) {
        return newSubmission(jwt, qrId, pageId, newHashSet(answers));
    }

    public static Response updateSubmissionRaw(String jwt, String submissionId, UpdateSubmissionCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/submissions/{submissionId}", submissionId);
    }

    public static String updateSubmission(String jwt, String submissionId, UpdateSubmissionCommand command) {
        return updateSubmissionRaw(jwt, submissionId, command)
                .then()
                .statusCode(200)
                .extract()
                .as(ReturnId.class).toString();
    }

    public static String updateSubmission(String jwt, String submissionId, Set<Answer> answers) {
        UpdateSubmissionCommand command = UpdateSubmissionCommand.builder().answers(answers).build();
        return updateSubmission(jwt, submissionId, command);
    }

    public static String updateSubmission(String jwt, String submissionId, Answer... answers) {
        return updateSubmission(jwt, submissionId, newHashSet(answers));
    }

    public static Response approveSubmissionRaw(String jwt, String submissionId, ApproveSubmissionCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/submissions/{submissionId}/approval", submissionId);
    }

    public static String approveSubmission(String jwt, String submissionId, ApproveSubmissionCommand command) {
        return approveSubmissionRaw(jwt, submissionId, command)
                .then()
                .statusCode(201)
                .extract()
                .as(ReturnId.class).toString();
    }

    public static String approveSubmission(String jwt, String submissionId, boolean passed) {
        ApproveSubmissionCommand command = ApproveSubmissionCommand.builder().passed(passed).note(rSentence(10) + "审批注释").build();
        return approveSubmission(jwt, submissionId, command);
    }


    public static Response deleteSubmissionRaw(String jwt, String submissionId) {
        return BaseApiTest.given(jwt)
                .when()
                .delete("/submissions/{submissionId}", submissionId);
    }

    public static String deleteSubmission(String jwt, String submissionId) {
        return deleteSubmissionRaw(jwt, submissionId)
                .then()
                .statusCode(200)
                .extract()
                .as(ReturnId.class).toString();
    }

    public static Response listSubmissionsRaw(String jwt, ListSubmissionsQuery command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/submissions/lists");
    }

    public static PagedList<QListSubmission> listSubmissions(String jwt, ListSubmissionsQuery command) {
        return listSubmissionsRaw(jwt, command)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static byte[] exportSubmissionsAsExcel(String jwt, ListSubmissionsQuery command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/submissions/excel")
                .then()
                .extract()
                .asByteArray();
    }

    public static Response fetchSubmissionRaw(String jwt, String submissionId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/submissions/{submissionId}", submissionId);
    }

    public static QDetailedSubmission fetchSubmission(String jwt, String submissionId) {
        return fetchSubmissionRaw(jwt, submissionId)
                .then()
                .statusCode(200)
                .extract()
                .as(QDetailedSubmission.class);
    }


    public static Response fetchListedSubmissionRaw(String jwt, String submissionId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/submissions/lists/{submissionId}", submissionId);
    }

    public static QListSubmission fetchListedSubmission(String jwt, String submissionId) {
        return fetchListedSubmissionRaw(jwt, submissionId)
                .then()
                .statusCode(200)
                .extract()
                .as(QListSubmission.class);
    }

    public static Response tryFetchInstanceLastSubmissionRaw(String jwt, String qrId, String pageId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/submissions/{qrId}/{pageId}/instance-last-submission", qrId, pageId);
    }

    public static QDetailedSubmission tryFetchInstanceLastSubmission(String jwt, String qrId, String pageId) {
        return tryFetchInstanceLastSubmissionRaw(jwt, qrId, pageId)
                .then()
                .statusCode(200)
                .extract()
                .as(QDetailedSubmission.class);
    }

    public static Response tryFetchMyLastSubmissionRaw(String jwt, String qrId, String pageId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/submissions/{qrId}/{pageId}/my-last-submission", qrId, pageId);
    }

    public static QDetailedSubmission tryFetchMyLastSubmission(String jwt, String qrId, String pageId) {
        return tryFetchMyLastSubmissionRaw(jwt, qrId, pageId)
                .then()
                .statusCode(200)
                .extract()
                .as(QDetailedSubmission.class);
    }

    public static Response tryFetchSubmissionAnswersForAutoFillRaw(String jwt, String qrId, String pageId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/submissions/{qrId}/{pageId}/auto-fill-answers", qrId, pageId);
    }

    public static Set<Answer> tryFetchSubmissionAnswersForAutoFill(String jwt, String qrId, String pageId) {
        return tryFetchSubmissionAnswersForAutoFillRaw(jwt, qrId, pageId)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static NumberInputAutoCalculateResponse autoCalculateNumberInput(String jwt, AutoCalculateQuery query) {
        return BaseApiTest.given(jwt)
                .body(query)
                .when()
                .post("/submissions/auto-calculate/number-input")
                .then()
                .statusCode(200)
                .extract()
                .as(NumberInputAutoCalculateResponse.class);
    }

    public static ItemStatusAutoCalculateResponse autoCalculateItemStatus(String jwt, AutoCalculateQuery query) {
        return BaseApiTest.given(jwt)
                .body(query)
                .when()
                .post("/submissions/auto-calculate/item-status")
                .then()
                .statusCode(200)
                .extract()
                .as(ItemStatusAutoCalculateResponse.class);
    }

}
