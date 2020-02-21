package com.item.gmallpublisher.controller;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.item.gmallpublisher.service.PublisherService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
public class PublisherController {

    @Autowired
    PublisherService service;

    @GetMapping("realtime-total")
    public String getDAUTotal(@RequestParam("date") String date) {
        /*
         *最终要求返回的数据格式是json数组
         * 需要查询得到是日活数
         */
        Long dauTotal = service.selectDAUTotal(date);

        ArrayList<Map> list = new ArrayList<>();

        HashMap<String, Object> dauMap = new HashMap<>();
        dauMap.put("id","dau");
        dauMap.put("name","新增日活");
        dauMap.put("value",dauTotal);

        HashMap<String, Object> newMidMap = new HashMap<>();
        newMidMap.put("id","new_mid");
        newMidMap.put("name","新增设备");
        newMidMap.put("value","222");

        list.add(dauMap);
        list.add(newMidMap);

        System.out.println(JSON.toJSONString(list));

        return JSON.toJSONString(list);
    }

    @GetMapping("realtime-hours")
    public String selectDAUTotalHourMap(@RequestParam("id") String id,@RequestParam("date") String date){

        //需要得到今天和昨天的数据。date是今天

        Map todayMap = service.selectDAUTotalHourMap(date);

        String yesterday = DateUtil.yesterday().toString("yyyy-MM-dd");
        Map yesterdayMap = service.selectDAUTotalHourMap(yesterday);

        //最后要求的返回格式是map套map
        HashMap<String, Map> resultMap = new HashMap<>();

        resultMap.put("yesterday",yesterdayMap);
        resultMap.put("today",todayMap);

        System.out.println(JSON.toJSONString(resultMap));

        return JSON.toJSONString(resultMap);
    }
}
