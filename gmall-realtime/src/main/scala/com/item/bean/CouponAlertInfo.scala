package com.item.bean

case class CouponAlertInfo(mid:String,
                           uids:java.util.HashSet[String],
                          //商品id
                           itemIds:java.util.HashSet[String],
                          //发生过的行为
                           events:java.util.List[String],
                           ts:Long)

