package com.item.gmallpublisher.controller;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.item.gmallpublisher.service.GMVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.item.gmallpublisher.service.DAUService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
public class PublisherController {

    @Autowired
    DAUService dauService;
    @Autowired
    GMVService gmvService;

    @GetMapping("realtime-total")
    public String getDAUTotal(@RequestParam("date") String date) {
        /*
         *最终要求返回的数据格式是json数组
         * 需要查询得到是日活数
         */
        ArrayList<Map> list = new ArrayList<>();

        Long dauTotal = dauService.selectDAUTotal(date);
        HashMap<String, Object> dauMap = new HashMap<>();
        dauMap.put("id","dau");
        dauMap.put("name","新增日活");
        dauMap.put("value",dauTotal);

        HashMap<String, Object> newMidMap = new HashMap<>();
        newMidMap.put("id","new_mid");
        newMidMap.put("name","新增设备");
        newMidMap.put("value","222");

        double gmvTotal = gmvService.selectOrderAmountTotal(date);
        HashMap<String, Object> gmvMap = new HashMap<>();
        gmvMap.put("id","order_amount");
        gmvMap.put("name","新增交易额");
        gmvMap.put("value",gmvTotal);


        list.add(dauMap);
        list.add(newMidMap);
        list.add(gmvMap);

        System.out.println(JSON.toJSONString(list));

        return JSON.toJSONString(list);
    }

    @GetMapping("realtime-hours")
    public String selectDAUTotalHourMap(@RequestParam("id") String id,@RequestParam("date") String date){
        //yesterday
        String yesterday = DateUtil.yesterday().toString("yyyy-MM-dd");

        Map todayMap=null;
        Map yesterdayMap=null;

       if("dau".equals(id)){
           //需要得到今天和昨天的数据。date是今天
           todayMap = dauService.selectDAUTotalHourMap(date);

           yesterdayMap = dauService.selectDAUTotalHourMap(yesterday);
       }else if("order_amount".equals(id)){
           todayMap = gmvService.selectOrderAmountHourMap(date);
           yesterdayMap = gmvService.selectOrderAmountHourMap(yesterday);
       }

        //最后要求的返回格式是map套map
        HashMap<String, Map> resultMap = new HashMap<>();

        resultMap.put("yesterday",yesterdayMap);
        resultMap.put("today",todayMap);

        System.out.println(JSON.toJSONString(resultMap));

        return JSON.toJSONString(resultMap);
    }
}
