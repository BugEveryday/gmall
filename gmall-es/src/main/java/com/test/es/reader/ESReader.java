package com.test.es.reader;

import com.alibaba.fastjson.JSONObject;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.MinAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.min.MinAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValueType;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ESReader {
    public static void main(String[] args) {
        JestClientFactory jestClientFactory = new JestClientFactory();

        HttpClientConfig httpClientConfig = new HttpClientConfig.Builder("http://hadoop222:9200").build();

        jestClientFactory.setHttpClientConfig(httpClientConfig);
        JestClient jestClient = jestClientFactory.getObject();

//        String query = "{\n" +
//                "  \"query\": {\n" +
//                "    \"bool\": {\n" +
//                "      \"filter\": {\n" +
//                "        \"term\": {\n" +
//                "          \"gender\": \"male\"\n" +
//                "        }\n" +
//                "      },\n" +
//                "      \"must\": [\n" +
//                "        {\"match\": {\n" +
//                "          \"favo\": \"dance\"\n" +
//                "        }}\n" +
//                "      ]\n" +
//                "    }\n" +
//                "  },\n" +
//                "  \"aggs\": {\n" +
//                "    \"min_age\": {\n" +
//                "      \"min\": {\n" +
//                "        \"field\": \"age\"\n" +
//                "      }\n" +
//                "    },\n" +
//                "    \"countby_class\":{\n" +
//                "      \"terms\": {\n" +
//                "        \"field\": \"classid\",\n" +
//                "        \"size\": 4\n" +
//                "      }\n" +
//                "    }\n" +
//                "  }\n" +
//                "}";
        //使用对象，写查询语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //query
        // bool
        //  filter
        //    term
        //      filed和vaule
        //  must
        //    match
        //      filed和value
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(new TermQueryBuilder("gender","male"));
        boolQueryBuilder.must(new MatchQueryBuilder("favo","dance"));
        searchSourceBuilder.query(boolQueryBuilder);
        //aggs
        // 名称
        //   方法名
        //    filed和value
        //    size
        MinAggregationBuilder min_age = new MinAggregationBuilder("min_age");
        min_age.field("age");
        searchSourceBuilder.aggregation(min_age);

        TermsAggregationBuilder countby_class = new TermsAggregationBuilder("countby_class", ValueType.LONG);
        countby_class.field("classid");
        countby_class.size(4);
        searchSourceBuilder.aggregation(countby_class);
        //分页。分页与query和aggs同级
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(2);

//        Search search = new Search.Builder(query).build();
        Search search = new Search.Builder(searchSourceBuilder.toString()).build();

        SearchResult result=null;
        //执行
        try {
            result = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //查看结果
        //total-->总共命中的个数
        System.out.println("总共命中的个数-->"+result.getTotal());
        //hit命中的结果集--->query的结果
        List<SearchResult.Hit<Map, Void>> hits = result.getHits(Map.class);
        for (SearchResult.Hit<Map, Void> hit : hits) {
            System.out.println("index-->"+hit.index);
            System.out.println("type-->"+hit.type);

            JSONObject jsonObject = new JSONObject();
            Map source = hit.source;
            for (Object o : source.keySet()) {
                jsonObject.put((String)o,source.get(o));
            }
            jsonObject.put("id",hit.id);
            System.out.println(jsonObject.toString());
        }
        //aggres的结果集--->聚合组的结果
        MetricAggregation aggregations = result.getAggregations();

        MinAggregation min_age1 = aggregations.getMinAggregation("min_age");
        System.out.println("min_age-->"+min_age1.getMin());

        TermsAggregation countby_class1 = aggregations.getTermsAggregation("countby_class");
        List<TermsAggregation.Entry> buckets = countby_class1.getBuckets();
        for (TermsAggregation.Entry bucket : buckets) {
            System.out.println("key-->"+bucket.getKey());
            System.out.println("count-->"+bucket.getCount());
        }

        //关闭连接
        jestClient.shutdownClient();
    }
}
