package com.mryqr.core.presentation.query.instancelist;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PInstanceListControl;
import com.mryqr.core.presentation.query.ControlPresentationer;
import com.mryqr.core.presentation.query.QControlPresentation;
import com.mryqr.core.presentation.query.instancelist.QInstanceListPresentation.InstanceItem;
import com.mryqr.core.qr.domain.QR;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.common.utils.MryConstants.QR_COLLECTION;
import static com.mryqr.core.app.domain.page.control.ControlType.INSTANCE_LIST;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class InstanceListPresentationer implements ControlPresentationer {
    private final MongoTemplate mongoTemplate;

    @Override
    public boolean canHandle(Control control) {
        return control.getType() == INSTANCE_LIST;
    }

    @Override
    public QControlPresentation present(QR qr, Control control, App app) {
        PInstanceListControl theControl = (PInstanceListControl) control;

        Query query = query(where("appId").is(app.getId()));
        query.with(by(DESC, "createdAt"));
        query.limit(theControl.getMax());
        query.fields().include("plateId", "name", "creator", "createdAt");

        List<InstanceItem> instances = mongoTemplate.find(query, InstanceItem.class, QR_COLLECTION);
        return new QInstanceListPresentation(instances);
    }
}
