package com.item.app

import java.text.SimpleDateFormat
import java.util.Date

import com.alibaba.fastjson.JSON
import com.item.bean.StartUpLog
import com.item.constants.GmallConstants
import com.item.handler.DAUHandler
import com.item.utils.MyKafkaUtil
import org.apache.hadoop.conf.Configuration
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.phoenix.spark._

object DAUApp {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setAppName("DAU").setMaster("local[*]")

    val ssc = new StreamingContext(conf, Seconds(5))
    //DAU（日活）是启动日志里的
    val kafkaDStream: InputDStream[(String, String)] = MyKafkaUtil.getKafkaStream(ssc, Set(GmallConstants.KAFKA_TOPIC_STARTUP))

    //定义格式化对象
    val format = new SimpleDateFormat("yyyy-MM-dd HH")

    val startupLogDStream: DStream[StartUpLog] = kafkaDStream.map {
      case (_, value) => {
        val startUpLog: StartUpLog = JSON.parseObject(value, classOf[StartUpLog])

        //将样例类的属性 var logDate:String,var logHour:String,加上
        //取出时间戳
        val ts: Long = startUpLog.ts
        //将时间戳转为相应的格式
        val time: String = format.format(new Date(ts))

        val timeArr: Array[String] = time.split(" ")

        startUpLog.logDate = timeArr(0)
        startUpLog.logHour = timeArr(1)

        startUpLog
      }
    }
    /*
     *计算日活，很重要的就是去重。
     * 对于DStream，是分批次的
     * 所以本批次的数据需要去重，
     * 还需要本批次和之前批次的数据进行去重
     * 最好是去重过的数据保存起来，方便本批次和之前的数据去重之后再进行去重
     * 最后将所有去重后的数据（明细）保存到HBase（Phoenix），做实时计算
     */
    // 1 跨批次去重。将之前已经出现的数据先过滤掉
    val filterByRedisDStream: DStream[StartUpLog] = DAUHandler.filterByRedis(startupLogDStream,ssc)

    // 2 本批次去重。过滤一次之后，本批次还可能会有重复数据
    //第一次的时候，不存在之前的批次，本批次去重之后就是DAU
    val filterBySelfDStream: DStream[StartUpLog] = DAUHandler.filterBySelf(filterByRedisDStream)

    // 3 将两次去重之后的数据保存到Redis，为下一次跨批次去重做准备
    //初始，
    DAUHandler.saveToRedis(filterBySelfDStream)

    filterByRedisDStream.count().print()
    println("------------------------------")
    filterBySelfDStream.count().print()

    // 4 将去重后的明细数据保存到HBase，为实时做准备
    filterBySelfDStream.foreachRDD(rdd=>
      rdd.saveToPhoenix("GMALL1_DAU",Seq("MID", "UID", "APPID", "AREA", "OS", "CH", "TYPE", "VS", "LOGDATE", "LOGHOUR", "TS") ,new Configuration,Some("hadoop222,hadoop223,hadoop224:2181"))
    )


    ssc.start()
    ssc.awaitTermination()
  }

}
