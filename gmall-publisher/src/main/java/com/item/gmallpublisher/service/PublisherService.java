package com.item.gmallpublisher.service;

import java.util.List;
import java.util.Map;

public interface PublisherService {
    //新增日活总数
    public Long selectDAUTotal(String date);
    //日活分时统计
    public Map selectDAUTotalHourMap(String date);
}
