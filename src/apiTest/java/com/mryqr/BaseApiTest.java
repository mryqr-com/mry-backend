package com.mryqr;

import com.mryqr.common.event.DomainEvent;
import com.mryqr.common.event.DomainEventType;
import com.mryqr.common.event.consume.ConsumingDomainEventDao;
import com.mryqr.common.event.publish.PublishingDomainEvent;
import com.mryqr.common.event.publish.PublishingDomainEventDao;
import com.mryqr.common.exception.Error;
import com.mryqr.common.exception.ErrorCode;
import com.mryqr.common.exception.QErrorResponse;
import com.mryqr.common.password.MryPasswordEncoder;
import com.mryqr.common.properties.CommonProperties;
import com.mryqr.common.security.jwt.JwtService;
import com.mryqr.common.utils.MryObjectMapper;
import com.mryqr.core.app.domain.AppFactory;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.appmanual.domain.AppManualRepository;
import com.mryqr.core.assignment.domain.AssignmentRepository;
import com.mryqr.core.assignmentplan.domain.AssignmentPlanRepository;
import com.mryqr.core.department.domain.DepartmentRepository;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchyRepository;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.order.domain.OrderRepository;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.platebatch.domain.PlateBatchRepository;
import com.mryqr.core.platetemplate.domain.PlateTemplateRepository;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.submission.domain.SubmissionRepository;
import com.mryqr.core.tenant.domain.TenantRepository;
import com.mryqr.core.verification.domain.VerificationCodeRepository;
import com.mryqr.utils.SetupApi;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.function.Supplier;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.mryqr.common.utils.MryConstants.AUTHORIZATION;
import static com.mryqr.common.utils.MryConstants.AUTH_COOKIE_NAME;
import static io.restassured.config.RestAssuredConfig.config;
import static io.restassured.http.ContentType.JSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@SuppressWarnings({"unchecked"})
@ActiveProfiles("ci")
@Execution(CONCURRENT)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public abstract class BaseApiTest {

    @Autowired
    protected CommonProperties commonProperties;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected MryObjectMapper objectMapper;

    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    @Autowired
    protected SetupApi setupApi;

    @Autowired
    protected PublishingDomainEventDao publishingDomainEventDao;
    @Autowired
    protected ConsumingDomainEventDao<DomainEvent> consumingDomainEventDao;

    @Autowired
    protected GroupRepository groupRepository;

    @Autowired
    protected AppRepository appRepository;

    @Autowired
    protected TenantRepository tenantRepository;

    @Autowired
    protected SubmissionRepository submissionRepository;

    @Autowired
    protected QrRepository qrRepository;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected VerificationCodeRepository verificationCodeRepository;

    @Autowired
    protected PlateRepository plateRepository;

    @Autowired
    protected PlateBatchRepository plateBatchRepository;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected PlateTemplateRepository plateTemplateRepository;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected MryPasswordEncoder mryPasswordEncoder;

    @Autowired
    protected AppManualRepository appManualRepository;

    @Autowired
    protected AssignmentPlanRepository assignmentPlanRepository;

    @Autowired
    protected AssignmentRepository assignmentRepository;

    @Autowired
    protected DepartmentRepository departmentRepository;

    @Autowired
    protected GroupHierarchyRepository groupHierarchyRepository;

    @Autowired
    protected AppFactory appFactory;

    @Autowired
    protected DepartmentHierarchyRepository departmentHierarchyRepository;

    @LocalServerPort
    protected int port;

    public static RequestSpecification given() {
        return RestAssured.given()
                .config(config()
                        .objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                                (type, s) -> new MryObjectMapper()))
                        .encoderConfig(new EncoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false))
                        .logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails()));
    }

    public static RequestSpecification given(String jwt) {
        if (isNotBlank(jwt)) {
            return given().cookie(AUTH_COOKIE_NAME, jwt);
        }

        return given();
    }

    public static RequestSpecification givenBearer(String jwt) {
        if (isNotBlank(jwt)) {
            return given().header(AUTHORIZATION, String.format("Bearer %s", jwt));
        }

        return given();
    }

    public static RequestSpecification givenBasic(String username, String password) {
        return given().auth().preemptive().basic(username, password);
    }

    @BeforeEach
    public void setUp() {
        objectMapper.enable(INDENT_OUTPUT);
        RestAssured.port = port;
//        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType(JSON)
                .setAccept(JSON)
                .build();
    }

    @AfterEach
    public void cleanUp() {
    }

    public static void assertError(Supplier<Response> apiCall, ErrorCode expectedCode) {
        Error error = apiCall.get().then().statusCode(expectedCode.getStatus()).extract().as(QErrorResponse.class).getError();
        assertEquals(expectedCode, error.getCode());
    }

    protected <T extends DomainEvent> T latestEventFor(String arId, DomainEventType type, Class<T> eventClass) {
        Query query = query(where(PublishingDomainEvent.Fields.event + "." + DomainEvent.Fields.arId).is(arId)
                .and(PublishingDomainEvent.Fields.event + "." + DomainEvent.Fields.type).is(type))
                .with(by(DESC, PublishingDomainEvent.Fields.raisedAt));
        PublishingDomainEvent publishingDomainEvent = mongoTemplate.findOne(query, PublishingDomainEvent.class);
        if (publishingDomainEvent == null) {
            return null;
        }
        return (T) publishingDomainEvent.getEvent();
    }
}
