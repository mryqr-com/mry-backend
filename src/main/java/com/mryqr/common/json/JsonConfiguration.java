package com.mryqr.common.json;


import com.mryqr.common.utils.CommonUtils;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.Instant;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import static com.fasterxml.jackson.annotation.PropertyAccessor.ALL;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.mryqr.common.utils.MryConstants.CHINA_TIME_ZONE;
import static java.time.ZoneId.of;
import static java.util.TimeZone.getTimeZone;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static tools.jackson.databind.MapperFeature.REQUIRE_SETTERS_FOR_GETTERS;
import static tools.jackson.databind.cfg.DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS;
import static tools.jackson.databind.cfg.DateTimeFeature.WRITE_DURATIONS_AS_TIMESTAMPS;

@Configuration
public class JsonConfiguration {

    private static SimpleModule instantModule() {
        return new SimpleModule()
                .addSerializer(Instant.class, new ValueSerializer<>() {
                    @Override
                    public void serialize(Instant value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
                        gen.writeNumber(value.toEpochMilli());
                    }
                })
                .addDeserializer(Instant.class, new ValueDeserializer<>() {
                    @Override
                    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
                        return Instant.ofEpochMilli(p.getValueAsLong());
                    }
                });
    }

    private static SimpleModule trimStringModule() {
        return new SimpleModule()
                .addDeserializer(String.class, new StdScalarDeserializer<>(String.class) {
                    @Override
                    public String deserialize(JsonParser jsonParser, DeserializationContext ctx) {
                        return CommonUtils.nullIfBlank(jsonParser.getValueAsString().trim());
                    }
                });
    }

    @Bean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> builder
                .changeDefaultVisibility(it -> it.withVisibility(ALL, NONE).withVisibility(FIELD, ANY))
                .changeDefaultPropertyInclusion(it -> it.withValueInclusion(ALWAYS))
                .addModule(instantModule())
                .addModule(trimStringModule())
                .defaultTimeZone(getTimeZone(of(CHINA_TIME_ZONE)))
                .enable(REQUIRE_SETTERS_FOR_GETTERS)
                .disable(FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(FAIL_ON_NULL_FOR_PRIMITIVES)
                .disable(WRITE_DATES_AS_TIMESTAMPS)
                .disable(WRITE_DURATIONS_AS_TIMESTAMPS);
    }

}
