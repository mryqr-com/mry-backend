package com.mryqr.common.domain.idnode.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static com.mryqr.common.domain.idnode.IdTree.NODE_ID_SEPARATOR;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class NodeIdValidator implements ConstraintValidator<NodeId, String> {

    @Override
    public void initialize(NodeId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String nodeId, ConstraintValidatorContext context) {
        if (isBlank(nodeId)) {
            return true;
        }

        return !nodeId.contains(NODE_ID_SEPARATOR);
    }

}
