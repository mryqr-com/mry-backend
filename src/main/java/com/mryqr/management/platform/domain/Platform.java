package com.mryqr.management.platform.domain;

import com.mryqr.common.domain.AggregateRoot;
import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.mryqr.common.utils.MryConstants.PLATFORM_COLLECTION;
import static lombok.AccessLevel.PRIVATE;

@Getter
@FieldNameConstants
@Document(PLATFORM_COLLECTION)
@TypeAlias(PLATFORM_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class Platform extends AggregateRoot { // Used to store information of the whole platform
    private long mobileAccessCount;
    private long nonMobileAccessCount;

    public final static String PLATFORM_ID = "PLF000000000000000001";

    public Platform(User user) {
        super(PLATFORM_ID, user);
    }

    public long mobileAccessRatio() {
        long total = this.mobileAccessCount + this.nonMobileAccessCount;
        return total == 0 ? 0 : this.mobileAccessCount * 100 / total;
    }

}
