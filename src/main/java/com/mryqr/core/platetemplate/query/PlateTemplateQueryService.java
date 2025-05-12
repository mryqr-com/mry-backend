package com.mryqr.core.platetemplate.query;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.platetemplate.domain.PlateTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Component
@RequiredArgsConstructor
public class PlateTemplateQueryService {
    private final MryRateLimiter mryRateLimiter;
    private final MongoTemplate mongoTemplate;

    public List<QListPlateTemplate> listAllPlateTemplates() {
        mryRateLimiter.applyFor("PlateTemplate:List", 20);

        Query query = new Query();
        query.with(Sort.by(DESC, "order", "createdAt")).limit(100);

        List<PlateTemplate> templates = mongoTemplate.find(query, PlateTemplate.class);

        return templates.stream().map(template -> QListPlateTemplate.builder()
                        .id(template.getId())
                        .plateSetting(template.getPlateSetting())
                        .image(template.getImage())
                        .order(template.getOrder())
                        .build())
                .collect(toImmutableList());
    }
}
