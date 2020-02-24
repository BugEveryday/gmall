package com.item.app

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import scala.util.control.Breaks._
import com.alibaba.fastjson.JSON
import com.item.bean.{CouponAlertInfo, EventLog}
import com.item.constants.GmallConstants
import com.item.utils.MyKafkaUtil
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object AlterApp {
  def main(args: Array[String]): Unit = {

    //

    val conf: SparkConf = new SparkConf().setAppName("Alter").setMaster("local[*]")

    val ssc = new StreamingContext(conf, Seconds(3))

    val kafkaDStream: InputDStream[(String, String)] = MyKafkaUtil.getKafkaStream(ssc, Set(GmallConstants.KAFKA_TOPIC_EVENT))

    //    val unit: DStream[(String, EventLog)] = kafkaDStream.mapValues(value=>JSON.parseObject(value,classOf[EventLog]))

    val format = new SimpleDateFormat("yyyy-MM-dd HH")

    //将Kafka数据转换为eventlog样例类
    val eventLogDStream: DStream[EventLog] = kafkaDStream.map {
      case (_, value) => {
        val eventLog: EventLog = JSON.parseObject(value, classOf[EventLog])

        //补全logdate和loghour
        val ts: Long = eventLog.ts
        val dateStr: String = format.format(new Date(ts))

        eventLog.logDate = dateStr.split(" ")(0)
        eventLog.logHour = dateStr.split(" ")(1)

        eventLog
      }
    }

//    eventLogDStream.print()

    //业务处理
    //根据条件产生预警日志：同一设备，30秒内三次及以上用不同账号登录并领取优惠劵，并且在登录到领劵过程中没有浏览商品。
    val eventLogDStreamWindow: DStream[EventLog] = eventLogDStream.window(Seconds(30))

    val midToEventLogDStream: DStream[(String, Iterable[EventLog])] = eventLogDStreamWindow.map(eventLog => (eventLog.mid, eventLog))
      .groupByKey()

//    midToEventLogDStream.print()

    //要返回的是符合预警条件的日志
    val booleanToAlertInfoDStream: DStream[(Boolean, CouponAlertInfo)] = midToEventLogDStream.map {
      case (mid, eventLog) =>
        /*要返回的日志，要返回全量数据，无论是否是预警的
         * 同一个mid
         * 有多个uid（set）
         * 多种行为（list）
         * 多个商品（set）
         */
        val uidSet = new util.HashSet[String]()
        val itemSet = new util.HashSet[String]()
        //预警日志中 用户行为
        val eventList = new util.ArrayList[String]()

        //判断是否浏览商品的标志位
        var notClick: Boolean = true

        breakable {
          eventLog.foreach {
            iter => {
              eventList.add(iter.evid)

              if ("clickItem".equals(iter.evid)) {
                notClick = false
                break()
              } else if ("coupon".equals(iter.evid)) {
                uidSet.add(iter.uid)
                itemSet.add(iter.itemid)
              }

            }
          }
        }

        (uidSet.size() >= 3 && notClick, CouponAlertInfo(mid, uidSet, itemSet, eventList, System.currentTimeMillis()))

    }

    booleanToAlertInfoDStream.print()
    booleanToAlertInfoDStream.filter(_._1)
      .map(_._2)
      .print()

    ssc.start()
    ssc.awaitTermination()

  }

}
