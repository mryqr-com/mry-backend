package com.mryqr.core.qr.command.importqr;

import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.*;

@Getter
@EqualsAndHashCode
public class QrImportRecord {
    private int rowIndex;
    private String name;
    private String customId;
    private Map<String, Set<Answer>> answers;//pageId -> answers
    private Map<String, AttributeValue> attributeValues;//attributeId -> attributeValue
    private List<String> errors;

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }

    public void addAnswer(String pageId, Answer answer) {
        if (this.answers == null) {
            this.answers = new HashMap<>();
        }

        Set<Answer> pageAnswers = this.answers.get(pageId);
        if (pageAnswers == null) {
            pageAnswers = new HashSet<>();
            pageAnswers.add(answer);
            this.answers.put(pageId, pageAnswers);
        } else {
            pageAnswers.add(answer);
        }
    }

    public void addAttributeValue(AttributeValue value) {
        if (this.attributeValues == null) {
            this.attributeValues = new HashMap<>();
        }

        this.attributeValues.put(value.getAttributeId(), value);
    }

    public void addError(String errorMessage) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(errorMessage);
    }
}
