package com.item.gmallpublisher.mapper;

import java.util.List;
import java.util.Map;

public interface GMVMapper {

    //每日交易总额
    double selectOrderAmountTotal(String date);

    //分时统计交易额
    List<Map> selectOrderAmountHourMap(String date);
}
