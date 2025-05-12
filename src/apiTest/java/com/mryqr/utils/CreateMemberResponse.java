package com.mryqr.utils;

public class CreateMemberResponse {
    private final String memberId;
    private final String name;
    private final String mobile;
    private final String password;
    private final String jwt;

    public CreateMemberResponse(String memberId, String name, String mobile, String password, String jwt) {
        this.memberId = memberId;
        this.name = name;
        this.mobile = mobile;
        this.password = password;
        this.jwt = jwt;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public String getPassword() {
        return password;
    }

    public String getJwt() {
        return jwt;
    }
}
