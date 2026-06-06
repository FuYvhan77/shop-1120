package com.hxy.service;

import com.hxy.enums.ProductOrderPayTypeEnum;
import com.hxy.model.OrderMessage;
import com.hxy.model.ProductOrderDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hxy.request.ConfirmOrderRequest;
import com.hxy.request.RepayOrderRequest;
import com.hxy.utils.JsonData;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zyd
 * @since 2026-05-21
 */
public interface ProductOrderService extends IService<ProductOrderDO> {

    String confirmOrder(ConfirmOrderRequest request);

    String queryProductOrderState(String outTradeNo);

    boolean closeProductOrder(OrderMessage orderMessage);

    JsonData handlerOrderCallbackMsg(ProductOrderPayTypeEnum productOrderPayTypeEnum, Map<String, String> paramsMap);

    Object page1(int page, int size, String state);

    String repay(RepayOrderRequest repayRequest);
}
