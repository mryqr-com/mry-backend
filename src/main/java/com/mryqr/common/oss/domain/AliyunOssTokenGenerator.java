package com.mryqr.common.oss.domain;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse.Credentials;
import com.mryqr.core.common.properties.AliyunProperties;
import com.mryqr.core.common.properties.CommonProperties;
import com.mryqr.core.common.utils.MryObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static com.aliyuncs.http.MethodType.POST;
import static java.time.Instant.parse;

@Component
@RequiredArgsConstructor
public class AliyunOssTokenGenerator {
    private static final String ENDPOINT = "sts.aliyuncs.com";
    private final MryObjectMapper objectMapper;
    private final AliyunProperties aliyunProperties;
    private final CommonProperties commonProperties;
    private IAcsClient acsClient;

    public QOssToken generateOssToken(String folderPath) {
        try {
            AssumeRoleRequest request = createRequest(folderPath);
            Credentials credentials = getAcsClient().getAcsResponse(request).getCredentials();
            return QOssToken.builder()
                    .accessKeyId(credentials.getAccessKeyId())
                    .accessKeySecret(credentials.getAccessKeySecret())
                    .securityToken(credentials.getSecurityToken())
                    .folder(folderPath + "/" + LocalDate.now())
                    .bucket(aliyunProperties.getOssBucket())
                    .endpoint(aliyunProperties.getOssEndpoint())
                    .secure(commonProperties.isHttpsEnabled())
                    .expiration(parse(credentials.getExpiration()))
                    .build();
        } catch (Throwable t) {
            throw new RuntimeException("Failed to create STS token from Aliyun for folder[" + folderPath + "].", t);
        }
    }

    private IAcsClient getAcsClient() {
        if (acsClient == null) {
            IClientProfile profile = DefaultProfile.getProfile("", aliyunProperties.getAk(), aliyunProperties.getAks());
            acsClient = new DefaultAcsClient(profile);
        }

        return acsClient;
    }


    private AssumeRoleRequest createRequest(String folder) {
        List<String> resource = List.of("acs:oss:*:*:" + aliyunProperties.getOssBucket() + "/" + folder + "/*");

        Statement putStatement = Statement.builder()
                .Action("oss:PutObject")
                .Effect("Allow")
                .Resource(resource)
                .build();

        //将deleteStatement加入到armPolicy中表示可以删除文件
        Statement deleteStatement = Statement.builder()
                .Action("oss:DeleteObject")
                .Effect("Allow")
                .Resource(resource)
                .build();

        AliyunArmPolicy armPolicy = AliyunArmPolicy.builder()
                .Version("1")
                .Statement(List.of(putStatement, deleteStatement))
                .build();

        AssumeRoleRequest request = new AssumeRoleRequest();
        request.setSysEndpoint(ENDPOINT);
        request.setDurationSeconds(900L);
        request.setSysMethod(POST);
        request.setRoleArn(aliyunProperties.getRole());
        request.setRoleSessionName("sts-session");
        request.setPolicy(objectMapper.writeValueAsString(armPolicy));
        return request;
    }
}
