package com.item.utils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class KafkaSenderUtil {

    private static KafkaProducer kafkaProducer = null;

    public static KafkaProducer createKafkaProducer(){

        Properties prop = new Properties();
        prop.put("bootstrap.servers","hadoop222:9092,hadopp223:9092,hadoop224:9092");
        prop.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        prop.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String, String> producer = null;
        try {
            producer = new KafkaProducer<>(prop);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return producer;
    }

    public static void send(String topic,String message){
        if(kafkaProducer==null){
            kafkaProducer=createKafkaProducer();
        }
        kafkaProducer.send(new ProducerRecord(topic,message));
    }

}
