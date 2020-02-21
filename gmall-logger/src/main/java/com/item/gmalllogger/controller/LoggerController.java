package com.item.gmalllogger.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.item.constants.GmallConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

//@Controller
@RestController//如果返回值只是字符串可以使用这个。而且想省略掉ResponseBody
// RestController=Controller+ResponseBody
@Slf4j//会自动注入一个Logger对象log
public class LoggerController {
    //注入KafkaTemplate
    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;
//
//    @GetMapping("test1")
//    public String test1(@RequestParam("a") String aa){
//        return aa+"--
//    @GetMapping("test" )
////    @ResponseBody
//    public String test(){
//        System.out.println("test success");
//        return "success";
//    }test1--success";
//    }

//   生成日志信息
    @PostMapping("log")
    public String doLog(@RequestParam("logString") String logString){
        //0 给log日志补充个时间戳。log日志要json格式
        JSONObject jsonObject = JSON.parseObject(logString);
        jsonObject.put("ts",System.currentTimeMillis());

        //1 将日志信息打印到控制台
        String jsonString = jsonObject.toString();
        log.info(jsonString);

        //2 连接到Kafka，将消息发送到相应主题
        if("startup".equals(jsonObject.getString("type"))){
            kafkaTemplate.send(GmallConstants.KAFKA_TOPIC_STARTUP,jsonString);
        }else{
            kafkaTemplate.send(GmallConstants.KAFKA_TOPIC_EVENT,jsonString);
        }
        return "success";
    }
}
