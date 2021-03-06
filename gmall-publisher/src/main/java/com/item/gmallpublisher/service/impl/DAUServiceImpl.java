package com.item.gmallpublisher.service.impl;

import com.item.gmallpublisher.mapper.DAUMapper;
import com.item.gmallpublisher.service.DAUService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DAUServiceImpl implements DAUService {

    @Autowired
    DAUMapper dauMapper;

    //日活总数
    @Override
    public Long selectDAUTotal(String date) {
        return dauMapper.selectDAUTotal(date);
    }

    //日活分时统计
    @Override
    public Map selectDAUTotalHourMap(String date) {
//获取到的List<Map>中map的结构是 属性名：属性值。要对其进行转换
        List<Map> maps = dauMapper.selectDAUTotalHourMap(date);

        HashMap<String, Long> map = new HashMap<>();

        for (Map ele : maps) {
            map.put((String) ele.get("LH"), (Long) ele.get("CT"));
        }

        return map;
    }

}
