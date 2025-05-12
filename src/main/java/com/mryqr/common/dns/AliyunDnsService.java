package com.mryqr.common.dns;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.AddDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.AddDomainRecordResponse;
import com.aliyuncs.alidns.model.v20150109.DeleteDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.mryqr.core.common.properties.AliyunProperties;
import com.mryqr.core.common.properties.CommonProperties;
import com.mryqr.core.common.properties.PropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class AliyunDnsService implements MryDnsService {
    private static final String CNAME = "CNAME";
    private static final String CN_HANGZHOU = "cn-hangzhou";//使用固定值，根据SDK文档https://help.aliyun.com/document_detail/67793.html
    private final AliyunProperties aliyunProperties;
    private final CommonProperties commonProperties;
    private final PropertyService propertyService;
    private IAcsClient acsClient;

    @Override
    public String addCname(String subdomainPrefix) {
        requireNonBlank(subdomainPrefix, "Subdomain prefix must not be blank.");

        AddDomainRecordRequest request = new AddDomainRecordRequest();
        request.setDomainName(propertyService.rootDomainName());
        request.setType(CNAME);
        request.setValue(cnameTarget());
        request.setRR(resourceRecordOf(subdomainPrefix));

        try {
            AddDomainRecordResponse response = getAcsClient().getAcsResponse(request);
            return response.getRecordId();
        } catch (ClientException e) {
            throw new RuntimeException("Failed to add subdomain prefix[" + subdomainPrefix + "].", e);
        }
    }

    @Override
    public String updateCname(String recordId, String subdomainPrefix) {
        requireNonBlank(recordId, "Record ID must not be blank.");
        requireNonBlank(subdomainPrefix, "Subdomain prefix must not be blank.");

        UpdateDomainRecordRequest request = new UpdateDomainRecordRequest();
        request.setRecordId(recordId);
        request.setType(CNAME);
        request.setValue(cnameTarget());
        request.setRR(resourceRecordOf(subdomainPrefix));

        try {
            UpdateDomainRecordResponse response = getAcsClient().getAcsResponse(request);
            return response.getRecordId();
        } catch (ClientException e) {
            if ("DomainRecordDuplicate".equals(e.getErrCode())) {
                log.warn("Domain record[{}] duplicated.", recordId);
                return recordId;
            }

            throw new RuntimeException("Failed to update subdomain prefix[" + subdomainPrefix + "] with recordId[" + recordId + "].", e);
        }
    }

    @Override
    public void deleteCname(String recordId) {
        requireNonBlank(recordId, "Record ID must not be blank.");

        DeleteDomainRecordRequest request = new DeleteDomainRecordRequest();
        request.setRecordId(recordId);

        try {
            getAcsClient().getAcsResponse(request);
        } catch (ClientException e) {
            if ("DomainRecordNotBelongToUser".equals(e.getErrCode())) {
                log.error("Domain record[{}] not exists.", recordId);
                return;
            }

            throw new RuntimeException("Failed to delete subdomain with recordId[" + recordId + "].", e);
        }
    }

    private IAcsClient getAcsClient() {
        if (acsClient == null) {
            IClientProfile profile = DefaultProfile.getProfile(CN_HANGZHOU, aliyunProperties.getAk(), aliyunProperties.getAks());
            acsClient = new DefaultAcsClient(profile);
        }

        return acsClient;
    }

    private String resourceRecordOf(String subdomainPrefix) {
        if (Objects.equals(commonProperties.getBaseDomainName(), propertyService.rootDomainName())) {
            return subdomainPrefix;
        }

        return subdomainPrefix + "." + commonProperties.getBaseDomainName().replace("." + propertyService.rootDomainName(), "");
    }

    private String cnameTarget() {
        return propertyService.subdomainFor("console");
    }
}
