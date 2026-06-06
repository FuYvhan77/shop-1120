package com.hxy.service;

import com.hxy.enums.SendCodeEnum;
import com.hxy.utils.JsonData;

public interface NotifyService {
    JsonData sendCode(SendCodeEnum sendCodeEnum, String to);
}
