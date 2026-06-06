package com.hxy.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AddressVO {
    private Long id;
    private Long userId;
    @JsonProperty("default_status")
    private Integer defaultStatus;
    @JsonProperty("receive_name")
    private String receiveName;
    private String phone;
    private String province;
    private String city;
    private String region;
    @JsonProperty("detail_address")
    private String detailAddress;
}