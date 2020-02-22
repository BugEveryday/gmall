package com.item.app

import com.alibaba.fastjson.JSON
import com.item.bean.OrderInfo
import com.item.constants.GmallConstants
import com.item.utils.MyKafkaUtil
import org.apache.hadoop.conf.Configuration
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.phoenix.spark._

object GMVApp {
  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("GMV").setMaster("local[*]")

    val ssc = new StreamingContext(conf, Seconds(5))

    val kafkaDStream: InputDStream[(String, String)] = MyKafkaUtil.getKafkaStream(ssc, Set(GmallConstants.KAFKA_TOPIC_ORDER_INFO))

    //将从kafka获取到的数据封装到样例类中，并做相应处理
    val orderInfoDStream: DStream[OrderInfo] = kafkaDStream.map {
      case (_, value) => {
        //封装到样例类
        val orderInfo: OrderInfo = JSON.parseObject(value, classOf[OrderInfo])

        //补全日期和小时
        //create_time->2020-02-21 11:11:11
        val timeArr: Array[String] = orderInfo.create_time.split(" ")

        orderInfo.create_date = timeArr(0)
        orderInfo.create_hour = timeArr(1).split(":")(0)

        //电话号脱敏
        orderInfo.consignee_tel = orderInfo.consignee_tel.splitAt(3)._1 + "********"
        //返回值
        orderInfo
      }
    }
    //保存到phoenix
    orderInfoDStream.foreachRDD(rdd=>{
      rdd.saveToPhoenix("GMALL_ORDER_INFO", Seq("ID", "PROVINCE_ID", "CONSIGNEE", "ORDER_COMMENT", "CONSIGNEE_TEL", "ORDER_STATUS", "PAYMENT_WAY", "USER_ID", "IMG_URL", "TOTAL_AMOUNT", "EXPIRE_TIME", "DELIVERY_ADDRESS", "CREATE_TIME", "OPERATE_TIME", "TRACKING_NO", "PARENT_ORDER_ID", "OUT_TRADE_NO", "TRADE_BODY", "CREATE_DATE", "CREATE_HOUR"), new Configuration(), Some("hadoop222,hadoop223,hadoop224:2181"))

    })

    ssc.start()
    ssc.awaitTermination()

  }

}
