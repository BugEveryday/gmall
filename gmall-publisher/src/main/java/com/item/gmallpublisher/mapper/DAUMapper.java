package com.item.gmallpublisher.mapper;


import java.util.List;
import java.util.Map;

public interface DAUMapper {
//    获取日活总数
    public Long selectDAUTotal(String date);
//    获取日活分时统计
    public List<Map> selectDAUTotalHourMap(String date);
}
