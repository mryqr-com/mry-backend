package com.mryqr.common.utils;

import com.mryqr.common.domain.indexedfield.IndexedField;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;

public class MongoCriteriaUtils {
    public static Criteria regexSearch(String field, String search) {
        Criteria criteria = new Criteria();
        if (isBlank(search)) {
            return criteria;
        }

        return criteria.andOperator(Arrays.stream(CommonUtils.splitSearchBySpace(search)).map(s -> where(field).regex(s)).toArray(Criteria[]::new));
    }

    public static String mongoAttributeValueFieldOf(String attributeId) {
        return "attributeValues." + attributeId;
    }

    public static String mongoAnswerFieldOf(String controlId) {
        return "answers." + controlId;
    }

    public static String mongoIndexedValueFieldOf(IndexedField field) {
        return "ivs." + field.name();
    }

    public static String mongoTextFieldOf(IndexedField field) {
        return "ivs." + field.name() + ".tv";
    }

    public static String mongoSortableFieldOf(IndexedField field) {
        return "ivs." + field.name() + ".sv";
    }

    public static String mongoReferencedFieldOf(IndexedField field) {
        return "ivs." + field.name() + ".rid";
    }

}
