package com.mryqr.core.qr.command.importqr;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelCommonException;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.qr.domain.attribute.DoubleAttributeValue;
import com.mryqr.core.qr.domain.attribute.TextAttributeValue;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_FIRST;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.app.domain.attribute.AttributeType.DIRECT_INPUT;
import static com.mryqr.core.common.domain.ValueType.DOUBLE_VALUE;
import static com.mryqr.core.common.exception.ErrorCode.INVALID_QR_EXCEL;
import static com.mryqr.core.common.exception.ErrorCode.NO_RECORDS_FOR_QR_IMPORT;
import static com.mryqr.core.common.exception.ErrorCode.QR_IMPORT_DUPLICATED_CUSTOM_ID;
import static com.mryqr.core.common.exception.ErrorCode.QR_IMPORT_OVERFLOW;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static com.mryqr.core.common.utils.MryConstants.MAX_CUSTOM_ID_LENGTH;
import static com.mryqr.core.common.utils.MryConstants.MAX_DIRECT_ATTRIBUTE_VALUE_LENGTH;
import static com.mryqr.core.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static com.mryqr.core.common.utils.MryConstants.QR_COLLECTION;
import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static java.lang.Double.parseDouble;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrImportParser {
    private static final Validator VALIDATOR = buildDefaultValidatorFactory().getValidator();
    private static final int MAX_READ_COUNT = 1000;
    private final MongoTemplate mongoTemplate;

    public List<QrImportRecord> parse(InputStream inputStream, String groupId, App app, int limit) {
        RawResult rawResult = parseToRawResult(inputStream, groupId, app, limit);
        return buildImportedRecords(rawResult, app);
    }

    private RawResult parseToRawResult(InputStream inputStream, String groupId, App app, int limit) {
        Map<String, Integer> headers = new HashMap<>();
        List<RawRecord> records = new ArrayList<>();
        Set<String> customIds = new HashSet<>();

        try {
            EasyExcel.read(inputStream, new AnalysisEventListener<Map<Integer, String>>() {
                @Override
                public void invokeHeadMap(Map<Integer, String> headerMap, AnalysisContext context) {
                    Map<String, Integer> mappedHeaders = headerMap.entrySet().stream()
                            .collect(toMap(entry -> entry.getValue().trim(), Map.Entry::getKey, (oldValue, newValue) -> oldValue));

                    String nameFieldName = app.qrImportNameFieldName();
                    if (!mappedHeaders.containsKey(nameFieldName)) {
                        throw new MryException(INVALID_QR_EXCEL,
                                "上传失败，文件中缺少[" + nameFieldName + "]字段，请严格按照模板进行上传。",
                                mapOf("groupId", groupId));
                    }

                    String customIdFieldName = app.qrImportCustomIdFieldName();
                    if (!mappedHeaders.containsKey(customIdFieldName)) {
                        throw new MryException(INVALID_QR_EXCEL,
                                "上传失败，文件中缺少[" + customIdFieldName + "]字段，请严格按照模板进行上传。",
                                mapOf("groupId", groupId));
                    }

                    headers.putAll(mappedHeaders);
                }

                @Override
                public void invoke(Map<Integer, String> data, AnalysisContext context) {
                    Integer customIdIndex = headers.get(app.qrImportCustomIdFieldName());
                    String customId = data.get(customIdIndex);
                    if (isNotBlank(customId)) {
                        if (customIds.contains(customId)) {
                            throw new MryException(QR_IMPORT_DUPLICATED_CUSTOM_ID,
                                    "上传失败，" + app.customIdDesignation() + "重复：[" + customId + "]，请更正后重新上传。",
                                    mapOf("groupId", groupId));
                        }
                        customIds.add(customId);
                    }

                    Integer rowIndex = context.readRowHolder().getRowIndex();
                    RawRecord record = RawRecord.builder().rowIndex(rowIndex + 1).data(data).build();
                    records.add(record);

                    if (records.size() > MAX_READ_COUNT) {
                        throw new MryException(QR_IMPORT_OVERFLOW,
                                "上传失败，单次最多只允许上传" + MAX_READ_COUNT + "条记录。",
                                mapOf("groupId", groupId));
                    }
                }

                @Override
                public boolean hasNext(AnalysisContext context) {
                    return records.size() < limit;
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {

                }
            }).sheet().doRead();
        } catch (ExcelCommonException e) {
            log.error("Error while importing qr file.", e);
            throw new MryException(INVALID_QR_EXCEL, "上传失败，请保证上传文件为xlsx格式，并且严格按照模板进行上传。",
                    "appId", app.getId(), "groupId", groupId);
        }

        if (isEmpty(records)) {
            throw new MryException(NO_RECORDS_FOR_QR_IMPORT, "上传失败，文件中无数据。", "groupId", groupId);
        }

        return RawResult.builder().headers(headers).records(records).build();
    }

    private List<QrImportRecord> buildImportedRecords(RawResult rawResult, App app) {
        Map<String, Integer> headers = rawResult.getHeaders();
        List<RawRecord> records = rawResult.getRecords();

        Integer nameIndex = headers.get(app.qrImportNameFieldName());
        Integer customIdIndex = headers.get(app.qrImportCustomIdFieldName());

        List<Attribute> attributes = app.qrImportableAttributes();
        Map<String, Control> controlMap = app.allControls().stream().collect(toImmutableMap(Control::getId, identity()));

        Set<String> existingCustomIds = existingCustomIds(rawResult, app);
        return records.stream()
                .map(record -> {
                    QrImportRecord qrImportRecord = new QrImportRecord();
                    Map<Integer, String> recordData = record.getData();
                    qrImportRecord.setRowIndex(record.getRowIndex());

                    String name = recordData.get(nameIndex);
                    if (isBlank(name)) {
                        qrImportRecord.addError("名称不能为空");
                    } else {
                        String trimName = name.trim();
                        qrImportRecord.setName(trimName);

                        if (trimName.length() > MAX_GENERIC_NAME_LENGTH) {
                            qrImportRecord.addError("名称最多只能包含" + MAX_GENERIC_NAME_LENGTH + "个字符");
                        }
                    }

                    String customId = recordData.get(customIdIndex);
                    if (isBlank(customId)) {
                        qrImportRecord.addError(app.customIdDesignation() + "不能为空");
                    } else {
                        String trimCustomId = customId.trim();
                        qrImportRecord.setCustomId(trimCustomId);

                        if (trimCustomId.length() > MAX_CUSTOM_ID_LENGTH) {
                            qrImportRecord.addError(app.customIdDesignation() + "最多只能包含" + MAX_CUSTOM_ID_LENGTH + "个字符");
                        } else if (existingCustomIds.contains(customId)) {
                            qrImportRecord.addError(app.customIdDesignation() + "已经存在");
                        }
                    }

                    attributes.forEach(attribute -> {
                        Integer index = headers.get(attribute.getName());
                        String value = recordData.get(index);

                        if (isNotBlank(value)) {
                            String trimmedValue = value.trim();
                            if (attribute.getType() == DIRECT_INPUT) {
                                if (attribute.getValueType() == DOUBLE_VALUE) {
                                    try {
                                        double number = parseDouble(trimmedValue);
                                        qrImportRecord.addAttributeValue(new DoubleAttributeValue(attribute, attribute.format(number)));
                                    } catch (NumberFormatException numberFormatException) {
                                        qrImportRecord.addError(attribute.getName() + "必须是数字");
                                    }
                                } else {
                                    if (trimmedValue.length() <= MAX_DIRECT_ATTRIBUTE_VALUE_LENGTH) {
                                        qrImportRecord.addAttributeValue(new TextAttributeValue(attribute, trimmedValue));
                                    } else {
                                        qrImportRecord.addError(attribute.getName() + "长度不能超过" + MAX_DIRECT_ATTRIBUTE_VALUE_LENGTH + "个字符");
                                    }
                                }
                            } else if (attribute.getType() == CONTROL_LAST || attribute.getType() == CONTROL_FIRST) {
                                Control control = controlMap.get(attribute.getControlId());
                                if (control != null) {
                                    Answer answer = control.createAnswerFrom(trimmedValue);
                                    if (answer != null) {
                                        Set<ConstraintViolation<Answer>> violations = VALIDATOR.validate(answer);
                                        if (isNotEmpty(violations)) {
                                            qrImportRecord.addError(attribute.getName() + "格式错误");
                                        } else {
                                            qrImportRecord.addAnswer(attribute.getPageId(), answer);
                                        }
                                    }
                                }
                            }
                        }
                    });
                    return qrImportRecord;
                })
                .collect(toImmutableList());
    }

    private Set<String> existingCustomIds(RawResult rawResult, App app) {
        List<RawRecord> records = rawResult.getRecords();
        Map<String, Integer> headers = rawResult.getHeaders();
        Integer customIdIndex = headers.get(app.qrImportCustomIdFieldName());

        Set<String> allCustomIds = records.stream()
                .map(record -> record.getData().get(customIdIndex))
                .filter(Objects::nonNull)
                .collect(toImmutableSet());

        Query query = Query.query(where("appId").is(app.getId()).and("customId").in(allCustomIds));
        query.fields().include("customId");
        List<ExistingQr> existingQrs = mongoTemplate.find(query, ExistingQr.class, QR_COLLECTION);
        return existingQrs.stream().map(ExistingQr::getCustomId).collect(toImmutableSet());
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    private static class RawResult {
        private final Map<String, Integer> headers;
        private final List<RawRecord> records;
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    private static class RawRecord {
        private final int rowIndex;
        private final Map<Integer, String> data;
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    private static class ExistingQr {
        private String id;
        private String customId;
    }
}
