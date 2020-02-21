package com.item.handler

import java.util

import cn.hutool.core.date.{DateTime, DateUtil}
import com.item.bean.StartUpLog
import com.item.utils.RedisUtil
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import redis.clients.jedis.Jedis

object DAUHandler {
  /**
    * 跨批次去重
    * 将Redis中的数据取出，进行过滤
    * @param startupLogDStream 日志数据
    * @return
    */
  def filterByRedis(startupLogDStream: DStream[StartUpLog],ssc:StreamingContext): DStream[StartUpLog] = {
    //将Redis中的数据取出，广播，然后进行过滤
    startupLogDStream.transform{rdd=>{
      //因为某个批次可能存在跨天的情况，所以要分开处理

      //获取连接
      val client: Jedis = RedisUtil.getJedisClient

      //使用了hutool的日期时间插件，方便操作
      val today: String = DateUtil.today()
      val todayRowKey = s"DAU_$today"
      val yesterday: DateTime = DateUtil.yesterday()
      val yesterdayRowKey = s"DAU_$yesterday"

      val todayRedis: util.Set[String] = client.smembers(todayRowKey)
      val yesterdayRedis: util.Set[String] = client.smembers(yesterdayRowKey)

      //释放连接
      client.close()

      val todayBc: Broadcast[util.Set[String]] = ssc.sparkContext.broadcast(todayRedis)
      val yesterdayBc: Broadcast[util.Set[String]] = ssc.sparkContext.broadcast(yesterdayRedis)

      rdd.filter(starupLog=>{
        var flag = true
        val date: String = starupLog.logDate
        if(date==today){
          flag = !todayBc.value.contains(starupLog.mid)
        }
        if(date==yesterday){
          flag = !yesterdayBc.value.contains(starupLog.mid)
        }
        flag
      })

    }}


    startupLogDStream
  }

  /**
    * 将去重过的数据保存到Redis，为下批次数据过滤做准备
    * 只保存mid即可
    *
    * @param filterBySelfDStream 两次去重之后的数据
    */
  def saveToRedis(filterBySelfDStream: DStream[StartUpLog]) = {
    filterBySelfDStream.foreachRDD(rdd => {

      rdd.foreachPartition(iter=>{
        //1获取连接
        val client: Jedis = RedisUtil.getJedisClient
        //2写库
        iter.foreach(startupLog=>{
          val rowKey = s"DAU_${startupLog.logDate}"
          client.sadd(rowKey,startupLog.mid)
        })
        //3释放连接
        client.close()
      })
    })
  }

  /**
    * 本批次内去重
    *
    * @param filterByRedisDStream 跨批次过滤之后的数据
    * @return 还是原来的数据类型
    */
  def filterBySelf(filterByRedisDStream: DStream[StartUpLog]): DStream[StartUpLog] = {

    val distinctedDStream: DStream[((String, String), Iterable[StartUpLog])] = filterByRedisDStream.map(startUpLog => {
      //因为可能存在同一批次跨天的情况，所以不能只以mid作为key进行gruopby，而是date和mid
      ((startUpLog.logDate, startUpLog.mid), startUpLog)
    }).groupByKey()

    //只需要其中最早的一个就可以了
    val value: DStream[StartUpLog] = distinctedDStream.flatMap {
      case ((date, mid), startupLog) =>
        startupLog.toList.sortWith(_.ts < _.ts).take(1)
    }
    value
  }

}
