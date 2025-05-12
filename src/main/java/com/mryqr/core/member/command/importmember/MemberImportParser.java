package com.mryqr.core.member.command.importmember;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelCommonException;
import com.mryqr.common.exception.MryException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.core.member.command.importmember.MemberImportRecord.*;
import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberImportParser {
    private static final Validator VALIDATOR = buildDefaultValidatorFactory().getValidator();
    private static final int MAX_READ_COUNT = 500;

    public List<MemberImportRecord> parse(InputStream inputStream, String tenantId, int limit) {
        List<MemberImportRecord> records = new ArrayList<>();

        try {
            EasyExcel.read(inputStream, MemberImportRecord.class, new AnalysisEventListener<MemberImportRecord>() {
                @Override
                public void invokeHeadMap(Map<Integer, String> headers, AnalysisContext context) {
                    if (!headers.containsValue(NAME_FILED_NAME)) {
                        throw new MryException(INVALID_MEMBER_EXCEL,
                                "上传失败，文件中缺少姓名字段，请严格按照模板进行上传。",
                                mapOf("tenantId", tenantId));
                    }

                    if (!headers.containsValue(MOBILE_FIELD_NAME)) {
                        throw new MryException(INVALID_MEMBER_EXCEL,
                                "上传失败，文件中缺少手机号字段，请严格按照模板进行上传。",
                                mapOf("tenantId", tenantId));
                    }

                    if (!headers.containsValue(EMAIL_FIELD_NAME)) {
                        throw new MryException(INVALID_MEMBER_EXCEL,
                                "上传失败，文件中缺少邮箱字段，请严格按照模板进行上传。",
                                mapOf("tenantId", tenantId));
                    }

                    if (!headers.containsValue(PASSWORD_FIELD_NAME)) {
                        throw new MryException(INVALID_MEMBER_EXCEL,
                                "上传失败，文件中缺少初始密码字段，请严格按照模板进行上传。",
                                mapOf("tenantId", tenantId));
                    }
                }

                @Override
                public void invoke(MemberImportRecord record, AnalysisContext context) {
                    record.setRowIndex(context.readRowHolder().getRowIndex() + 1);
                    Set<ConstraintViolation<MemberImportRecord>> violations = VALIDATOR.validate(record);

                    if (isNotEmpty(violations)) {
                        violations.forEach(violation -> record.addError(violation.getMessage()));
                    }

                    records.add(record);

                    if (records.size() > MAX_READ_COUNT) {
                        throw new MryException(MEMBER_IMPORT_OVERFLOW,
                                "上传失败，单次最多只允许上传" + MAX_READ_COUNT + "条记录。",
                                mapOf("tenantId", tenantId));
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
            log.error("Error while uploading member file.", e);
            throw new MryException(INVALID_MEMBER_EXCEL,
                    "上传失败，请保证上传文件为xlsx格式，并且严格按照模板进行上传。"
                    , mapOf("tenantId", tenantId));
        }

        if (isEmpty(records)) {
            throw new MryException(NO_RECORDS_FOR_MEMBER_IMPORT, "上传失败，文件中无数据。", "tenantId", tenantId);
        }

        return records;
    }

}
