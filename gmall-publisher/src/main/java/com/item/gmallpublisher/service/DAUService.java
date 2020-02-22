package com.item.gmallpublisher.service;

import java.util.Map;

public interface DAUService {
    //新增日活总数
    Long selectDAUTotal(String date);
    //日活分时统计
    Map selectDAUTotalHourMap(String date);
}
