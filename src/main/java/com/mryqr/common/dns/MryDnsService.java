package com.mryqr.common.dns;

public interface MryDnsService {
    String addCname(String subdomainPrefix);

    String updateCname(String recordId, String subdomainPrefix);

    void deleteCname(String recordId);
}
