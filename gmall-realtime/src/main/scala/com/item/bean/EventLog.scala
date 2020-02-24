package com.item.bean

case class EventLog(mid:String,
                     uid:String,
                     appid:String,
                     area:String,
                     os:String,
                     `type`:String,
                   //发生行为的id
                     evid:String,
                     pgid:String,
                     npgid:String,
                     itemid:String,
                     var logDate:String,
                     var logHour:String,
                     var ts:Long)
