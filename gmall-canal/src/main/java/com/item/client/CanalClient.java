package com.item.client;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import com.item.constants.GmallConstants;
import com.item.utils.KafkaSenderUtil;

import java.net.InetSocketAddress;
import java.util.List;

public class CanalClient {
    public static void main(String[] args) {
        CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress("hadoop222", 11111), "example", "", "");
/*
 *      canalConnector -get->message
 * message:一个message包含多个sql(event)[entry]
 *      message -getEntries->entries -foreach-getStoreValue->storeValue
 * entry:一个sql可能会对多行记录造成影响[storeValue]
 *      RowChange.parseFrom(storeValue)->rowchange
 * rowchange:把entry中的storeValue反序列化[rowdatalist/eventTyep]
 *      rowchange -get->rowdata
 * rowdata:出现变化的数据行信息[afterColumnList]
 *      rowdata -afterColumnList->column
 * column:一个RowData里包含了多个column，每个column包含了 name和 value
 *      column -get->name,value
 */
        //不断地获取数据向kafka发送
        while (true) {
            // 2 获取连接
            canalConnector.connect();
            // 3 指定订阅的数据库
            canalConnector.subscribe("gmall.*");

            // 4 获取数据
            // 4.1 message:一个message包含多个sql(event)[entry]
            Message message = canalConnector.get(100);

            // 4.2 entry:一个sql可能会对多行记录造成影响[storeValue]
            List<CanalEntry.Entry> entries = message.getEntries();

//            System.out.println(entries.size());

            // 4.3 先对entry进行判空，为空就等等
            if (entries.size() <= 0) {
                System.out.println("空的，等30秒");
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                // 4.4 有数据，遍历取出entry
                for (CanalEntry.Entry entry : entries) {
                    /*
                     *CanalEntry.EntryType有多种类型，包括
                     *  TRANSACTIONBEGIN(0, 1),
                     *  ROWDATA(1, 2),
                     *  TRANSACTIONEND(2, 3),
                     *  HEARTBEAT(3, 4),
                     *  GTIDLOG(4, 5);
                     *需要的只是ROWDATA
                     */
                    // 4.5 过滤出ROWDATA类型的
//                    System.out.println(entry.getHeader().getTableName());
                    if (CanalEntry.EntryType.ROWDATA.equals(entry.getEntryType())) {
                        try {
                            // 4.6 反序列数据
                            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                            //获取表名，方便对表的操作
                            String tableName = entry.getHeader().getTableName();
                            //获取事件类型
                            CanalEntry.EventType eventType = rowChange.getEventType();

                            //处理数据为JSON格式并发送
                            transToJsonAndSend(tableName, eventType, rowChange);

//                            System.out.println(jsonString);
//                            //发送到Kafka。生成JSON和发送分开写的时候，控制台打印的JSON中间有空行
//                            KafkaSenderUtil.send(GmallConstants.KAFKA_TOPIC_ORDER_INFO,jsonString);

                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }


    }

    private static void transToJsonAndSend(String tableName, CanalEntry.EventType eventType, CanalEntry.RowChange rowChange) {
        String jsonString = "";
        //求GMV需要对订单表，而且需要的是insert类型的
        if ("order_info".equals(tableName) && CanalEntry.EventType.INSERT.equals(eventType)) {

            //rowChange相当于整张表（不止包括表数据），rowDataList是表数据，rowData是某一行数据
            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                //将符合要求的数据，封装到JSON
                JSONObject jsonObject = new JSONObject();
                //rowData包含了某一行数据的变化前和变化后
                for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
                    jsonObject.put(column.getName(), column.getValue());
                }
                jsonString += jsonObject.toString();

                System.out.println(jsonString);

                //发送到Kafka
                KafkaSenderUtil.send(GmallConstants.KAFKA_TOPIC_ORDER_INFO,jsonString);
            }
        }

//        return jsonString;
    }
}
