package com.hxy.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel
@Data
public class NewUserCouponRequest {
    @ApiModelProperty(value = "用户Id", example = "1")
    @JsonProperty("user_id")
    private long userId;

    @ApiModelProperty(value = "名称", example = "Anna小姐姐")
    @JsonProperty("name")
    private String name;
}