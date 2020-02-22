package com.item.gmallpublisher.service.impl;

import com.item.gmallpublisher.mapper.GMVMapper;
import com.item.gmallpublisher.service.GMVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GMVServiceImpl implements GMVService {

    @Autowired
    GMVMapper gmvMapper;

    //每日gmv
    @Override
    public double selectOrderAmountTotal(String date) {
        return gmvMapper.selectOrderAmountTotal(date);
    }

    //分时gmv
    @Override
    public Map selectOrderAmountHourMap(String date) {
        List<Map> maps = gmvMapper.selectOrderAmountHourMap(date);

        HashMap<String, Object> map = new HashMap<>();

        for (Map ele : maps) {
            map.put((String) ele.get("CREATE_HOUR"), ele.get("SUM_AMOUNT"));
        }

        return map;
    }
}
