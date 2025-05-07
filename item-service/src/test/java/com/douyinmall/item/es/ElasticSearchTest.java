//package com.douyinmall.item.es;
//
//
//import cn.hutool.json.JSONUtil;
//import com.douyinmall.item.domain.VO.Product;
//import org.apache.http.HttpHost;
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.SearchHits;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
//import org.elasticsearch.search.sort.SortOrder;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.util.Map;
//
//public class ElasticSearchTest {
//
//        private RestHighLevelClient client;
//
//
//        @Test
//        void testMatchAll() throws IOException {
//                SearchRequest request = new SearchRequest("product");
//                request.source().query(QueryBuilders.matchAllQuery());
//                SearchResponse search = client.search(request, RequestOptions.DEFAULT);
//                SearchHits searchHits = search.getHits();
//                // 1.获取总条数
//                long total = 0;
//                if (searchHits.getTotalHits() != null) {
//                        total = searchHits.getTotalHits().value;
//                }
//                System.out.println("共搜索到" + total + "条数据");
//                // 2.遍历结果数组
//                SearchHit[] hits = searchHits.getHits();
//                for (SearchHit hit : hits) {
//                        // 3.得到_source，也就是原始json文档
//                        String source = hit.getSourceAsString();
//                        // 4.反序列化并打印
//                        Product item = JSONUtil.toBean(source, Product.class);
//                        System.out.println(item);
//                }
//                System.out.println(search);
//        }
//
//
//        @Test
//        void testSearch() throws IOException {
//                SearchRequest request = new SearchRequest("product");
//                request.source().query(
//                        QueryBuilders.boolQuery()
//                                .must(QueryBuilders.matchQuery("name", "小米"))//全文检索
//                                .filter(QueryBuilders.rangeQuery("price").lt(6000))//过滤,不参与算分，价格低于6000
//                );
////                //分页
////                request.source().from(0);
////                request.source().size(10);
////                //排序
////                request.source().sort("price", SortOrder.DESC);
//
//                SearchResponse search = client.search(request, RequestOptions.DEFAULT);
//                SearchHits searchHits = search.getHits();
//                // 1.获取总条数
//                long total = 0;
//                if (searchHits.getTotalHits() != null) {
//                        total = searchHits.getTotalHits().value;
//                }
//                System.out.println("共搜索到" + total + "条数据");
//                // 2.遍历结果数组
//                SearchHit[] hits = searchHits.getHits();
//                for (SearchHit hit : hits) {
//                        // 3.得到_source，也就是原始json文档
//                        String source = hit.getSourceAsString();
//                        // 4.反序列化并打印
//                        Product item = JSONUtil.toBean(source, Product.class);
//                        // 5.得到高亮结果
//                        Map<String, HighlightField> hfs = hit.getHighlightFields();
//                        if (hfs!= null) {
//                                // 5.1.有高亮结果，获取name的高亮结果
//                                HighlightField hf = hfs.get("name");
//                                if (hf != null) {
//                                        // 5.2.获取第一个高亮结果片段，就是商品名称的高亮值
//                                        String hfName = hf.getFragments()[0].string();
//                                        item.setName(hfName);
//                                }
//                        }
//                        System.out.println(item);
//                }
//                System.out.println(search);
//        }
//
//        @BeforeEach
//        void setUp() {
//                client = new RestHighLevelClient(
//                        RestClient.builder(
//                                HttpHost.create("http://192.168.163.129:9200")
//                        )
//                );
//        }
//
//        @AfterEach
//        void tearDown() throws Exception {
//                if (client != null) {
//                        client.close();
//                }
//        }
//}
