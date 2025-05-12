package com.mryqr.core.platetemplate.infrastructure;

import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.platetemplate.domain.PlateTemplate;
import com.mryqr.core.platetemplate.domain.PlateTemplateRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MongoPlateTemplateRepository extends MongoBaseRepository<PlateTemplate> implements PlateTemplateRepository {
    @Override
    public void save(PlateTemplate it) {
        super.save(it);
    }

    @Override
    public void delete(PlateTemplate it) {
        super.delete(it);
    }

    @Override
    public PlateTemplate byId(String id) {
        return super.byId(id);
    }

    @Override
    public Optional<PlateTemplate> byIdOptional(String id) {
        return super.byIdOptional(id);
    }

    @Override
    public PlateTemplate byIdAndCheckTenantShip(String id, User user) {
        return super.byIdAndCheckTenantShip(id, user);
    }

}
