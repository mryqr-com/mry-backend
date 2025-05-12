package com.mryqr.core.verification.infrastructure;

import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.verification.domain.VerificationCode;
import com.mryqr.core.verification.domain.VerificationCodeRepository;
import com.mryqr.core.verification.domain.VerificationCodeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
@RequiredArgsConstructor
public class MongoVerificationCodeRepository extends MongoBaseRepository<VerificationCode> implements VerificationCodeRepository {

    @Override
    public Optional<VerificationCode> findValidOptional(String mobileOrEmail, String code, VerificationCodeType type) {
        requireNonBlank(mobileOrEmail, "Mobile or email must not be blank.");
        requireNonBlank(code, "Code must not be blank.");
        requireNonNull(type, "Type must not be null.");

        Criteria criteria = where("code").is(code)
                .and("mobileOrEmail").is(mobileOrEmail)
                .and("type").is(type.name())
                .and("usedCount").lt(3)
                .and("createdAt").gte(now().minus(10, MINUTES));
        Query query = Query.query(criteria).with(by("createdAt").descending());
        return Optional.ofNullable(mongoTemplate.findOne(query, VerificationCode.class));
    }

    @Override
    public boolean existsWithinOneMinutes(String mobileOrEmail, VerificationCodeType type) {
        requireNonBlank(mobileOrEmail, "Mobile or email must not be blank.");
        requireNonNull(type, "Type must not be null.");

        Query query = Query.query(where("mobileOrEmail").is(mobileOrEmail)
                .and("type").is(type.name())
                .and("createdAt").gte(now().minus(1, MINUTES)));
        return mongoTemplate.exists(query, VerificationCode.class);
    }

    @Override
    public long totalCodeCountOfTodayFor(String mobileOrEmail) {
        requireNonBlank(mobileOrEmail, "Mobile or email must not be blank.");

        Instant beginOfToday = LocalDate.now().atStartOfDay().atZone(systemDefault()).toInstant();
        Query query = Query.query(where("mobileOrEmail").is(mobileOrEmail).and("createdAt").gte(beginOfToday));
        return mongoTemplate.count(query, VerificationCode.class);
    }

    @Override
    public void save(VerificationCode it) {
        super.save(it);
    }

    @Override
    public VerificationCode byId(String id) {
        return super.byId(id);
    }

    @Override
    public boolean exists(String arId) {
        return super.exists(arId);
    }
}
