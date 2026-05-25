package com.mryqr.common.administrative;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.administrative.Administrative;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.mryqr.common.utils.MryConstants.CHINESE_COLLATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AdministrativeControllerApiTest extends BaseApiTest {

    @Test
    public void generate_china_administratives() throws IOException {
        ClassPathResource resource = new ClassPathResource("testdata/administrative/raw-tianditu-district.json");
        TiandutuResponse tiandutuResponse = objectMapper.readValue(resource.getInputStream(), TiandutuResponse.class);
        Administrative china = tiandutuResponse.getData();

        Set<String> tobePolishProvinces = Set.of("北京市", "上海市", "天津市", "重庆市", "香港");
        Set<String> gangAoTai = Set.of("香港", "澳门", "台湾省");

        List<Administrative> polishedProvinces = china.getChild().stream().map(administrative -> {
            if (tobePolishProvinces.contains(administrative.getName())) {
                return Administrative.builder()
                        .name(administrative.getName())
                        .child(List.of(administrative))
                        .build();
            }
            return administrative;
        }).toList();

        Stream<Administrative> majorProvinces = polishedProvinces.stream().filter(administrative1 -> !gangAoTai.contains(administrative1.getName()))
                .sorted((o1, o2) -> CHINESE_COLLATOR.compare(o1.getName(), o2.getName()));
        Stream<Administrative> specialProvinces = polishedProvinces.stream().filter(administrative -> gangAoTai.contains(administrative.getName()));
        List<Administrative> finalResults = Stream.concat(majorProvinces, specialProvinces).toList();

        assertEquals(34, finalResults.size());
        Administrative chongQing = finalResults.stream().filter(administrative -> administrative.getName().equals("重庆市")).findFirst().get();
        assertEquals("重庆市", chongQing.getChild().get(0).getName());//直辖市的省和市应该是一样的

        Administrative hangKong = finalResults.stream().filter(administrative -> administrative.getName().equals("香港")).findFirst().get();
        assertEquals("香港", hangKong.getChild().get(0).getName());//香港的省和市应该是一样的

        Administrative finalChina = Administrative.builder().name(china.getName()).child(finalResults).build();
        System.out.println(new ObjectMapper().writeValueAsString(finalChina));
    }

    static class TiandutuResponse {
        Administrative data;

        public Administrative getData() {
            return data;
        }

        public void setData(Administrative data) {
            this.data = data;
        }
    }
}