package com.dat.erp.dto.response;

import lombok.Data;

@Data
public class UserInformationResponse {

    private String code;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private String avatarUrl;
}
