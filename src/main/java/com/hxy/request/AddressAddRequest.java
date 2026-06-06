package com.hxy.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "地址对象", description = "新增收货地址对象")
public class AddressAddRequest {

    @ApiModelProperty(value = "是否默认收货地址，0->否；1->是", example = "0")
    @JsonProperty("default_status")
    private Integer defaultStatus;

    @ApiModelProperty(value = "收发货人姓名", example = "隔壁老王")
    @JsonProperty("receive_name")
    private String receiveName;

    @ApiModelProperty(value = "收货人电话", example = "12321312321")
    private String phone;

    @ApiModelProperty(value = "省/直辖市", example = "广东省")
    private String province;

    @ApiModelProperty(value = "城市", example = "广州市")
    private String city;

    @ApiModelProperty(value = "区", example = "天河区")
    private String region;

    @ApiModelProperty(value = "详细地址", example = "运营中心-老王隔壁1号")
    @JsonProperty("detail_address")
    private String detailAddress;
}