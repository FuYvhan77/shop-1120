package com.hxy.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String name;

    @JsonProperty("head_img")
    private String headImg;

    private String slogan;
    private Integer sex;       // 0表示女，1表示男
    private Integer points;
    private String mail;
}