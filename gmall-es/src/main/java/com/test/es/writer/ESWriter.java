package com.test.es.writer;

import com.test.es.bean.Test;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;

import java.io.IOException;

public class ESWriter {
    public static void main(String[] args) {
        //创建ES连接
        JestClientFactory jestClientFactory = new JestClientFactory();

        HttpClientConfig httpClientConfig = new HttpClientConfig.Builder("http://hadoop222:9200").build();

        jestClientFactory.setHttpClientConfig(httpClientConfig);

        JestClient jestClient = jestClientFactory.getObject();

        //操作前的准备
        //es的插入数据操作
//        PUT test/_doc/1001
//        {
//            "name":"zhangsan",
//                "gender":"male"
//        }
//        Index index = new Index.Builder("{\"name\":\"listi\",\n" +
//                "                   \"gender\":\"male\"}")
//                .index("test")//index库名
//                .type("_doc")//type表名
//                .id("1002")//id行号
//                .build();

        //要插入的数据还可以直接使用bean的形式
        Test test = new Test("wangwu", "female");

        Index index = new Index.Builder(test)
                .index("test")
                .type("_doc")
                .id("1003")
                .build();

        //执行操作
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //关闭连接。close()有点问题，还是shutdownClient()
        jestClient.shutdownClient();

    }
}
