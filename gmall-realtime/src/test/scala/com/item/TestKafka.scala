package com.item

import com.item.constants.GmallConstants
import com.item.utils.MyKafkaUtil
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.InputDStream

object TestKafka {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setAppName("DAU").setMaster("local[*]")

    val ssc = new StreamingContext(conf,Seconds(3))

    val kafkaDStream: InputDStream[(String, String)] = MyKafkaUtil.getKafkaStream(ssc, Set(GmallConstants.KAFKA_TOPIC_STARTUP))

    kafkaDStream.map(_._2).print()

    ssc.start()
    ssc.awaitTermination()
  }

}
