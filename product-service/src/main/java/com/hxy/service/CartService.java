package com.hxy.service;

import com.hxy.request.CartItemRequest;
import com.hxy.vo.CartItemVO;
import com.hxy.vo.CartVO;

import java.util.List;

public interface CartService {
    void addToCart(CartItemRequest cartItemRequest);

    void clear();

    CartVO getMyCart();

    void deleteItem(long productId);

    void changeItemNum(CartItemRequest cartItemRequest);

    List<CartItemVO> confirmOrderCartItem(List<Long> productIdList);
}
