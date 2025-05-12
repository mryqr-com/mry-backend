package com.mryqr.core.tenant.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.common.properties.AliyunProperties;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

import static com.mryqr.core.common.domain.user.User.NOUSER;
import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static java.lang.Float.parseFloat;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountStorageForTenantTask implements RepeatableTask {
    private static final String OUTPUT_PREFIX = "total du size(GB):";
    private final TenantRepository tenantRepository;
    private final AliyunProperties aliyunProperties;

    public void run(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(aliyunProperties.getOssUtilCommand(),
                    "du",
                    "oss://" + aliyunProperties.getOssBucket() + "/" + tenantId,
                    "--block-size",
                    "GB",
                    "--config-file",
                    aliyunProperties.getOssUtilConfigFile());
            Process process = processBuilder.start();
            String output = IOUtils.toString(process.getInputStream(), UTF_8);

            String count = Arrays.stream(output.split(System.lineSeparator()))
                    .filter(StringUtils::isNotBlank)
                    .filter(it -> it.contains(OUTPUT_PREFIX))
                    .map(it -> it.replace(OUTPUT_PREFIX, "").trim())
                    .findFirst()
                    .orElse(null);

            if (count != null) {
                Tenant tenant = tenantRepository.byId(tenantId);
                tenant.setStorage(parseFloat(count), NOUSER);
                tenantRepository.save(tenant);
                log.info("Counted {}G storage for tenant[{}].", count, tenantId);
            } else {
                log.error("Failed to count storage for tenant[{}]: {}.", tenantId, output);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to count storage fot tenant[" + tenantId + "]", e);
        }
    }
}
