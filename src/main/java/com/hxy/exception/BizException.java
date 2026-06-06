package com.hxy.exception;

import com.hxy.utils.BizCodeEnum;
import lombok.Data;

/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName BizException
 * @date 2026-05-13 14:11
 */

@Data
public class BizException extends RuntimeException{
    private Integer code;
    private String msg;

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
        this.msg = message;
    }

    public BizException(BizCodeEnum bizCodeEnum) {
        super(bizCodeEnum.getMessage());
        this.code = bizCodeEnum.getCode();
        this.msg = bizCodeEnum.getMessage();
    }
}
