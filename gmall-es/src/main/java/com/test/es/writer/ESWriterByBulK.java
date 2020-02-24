package com.test.es.writer;

import com.test.es.bean.Test;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class ESWriterByBulK {

    public static void main(String[] args) {
        //建立连接
        JestClientFactory jestClientFactory = new JestClientFactory();

        HttpClientConfig httpClientConfig = new HttpClientConfig.Builder("http://hadoop222:9200").build();

        jestClientFactory.setHttpClientConfig(httpClientConfig);

        JestClient jestClient = jestClientFactory.getObject();

        //批量
        Test test1 = new Test("lisi1", "female");
        Test test2 = new Test("lisi2", "male");
        Test test3 = new Test("lisi3", "female");
        ArrayList<Test> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        tests.add(test3);

        ArrayList<Index> indexs = new ArrayList<>();
        int n=0;
        for (Test test : tests) {
            n++;
            Index build = new Index.Builder(test).index("test").type("_doc").id((1003 + n) + "").build();
            indexs.add(build);
        }

        Bulk.Builder builder = new Bulk.Builder();

        for (Index index : indexs) {
            builder.addAction(index);
        }

        Bulk bulk = builder.build();

        //执行
        try {
            jestClient.execute(bulk);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //关闭连接
        jestClient.shutdownClient();
    }
}
