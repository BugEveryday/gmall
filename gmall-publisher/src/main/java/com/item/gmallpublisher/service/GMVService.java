package com.item.gmallpublisher.service;

import java.util.Map;

public interface GMVService {
    //每日交易总额
    double selectOrderAmountTotal(String date);

    //分时统计交易额
    Map selectOrderAmountHourMap(String date);
}
