//package com.douyinmall.item.es;
//
//
//import cn.hutool.json.JSONUtil;
//import com.douyinmall.item.domain.VO.Product;
//import com.douyinmall.item.service.ShopService;
//import org.apache.http.HttpHost;
//import org.elasticsearch.action.index.IndexRequest;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.common.xcontent.XContentType;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.io.IOException;
//
//@SpringBootTest
//public class ElasticDocumentTest {
//        private RestHighLevelClient client;
//        @Autowired
//        private ShopService shopService;
//
//
//        @Test
//        void testIndexDocument() throws IOException {
//                for (int i = 4; i <=10 ; i++) {
//                        Product product = shopService.getProductById((long) i);
//                        IndexRequest request = new IndexRequest("product").id(product.getId().toString());
//                        request.source(JSONUtil.toJsonStr(product), XContentType.JSON);
//                        client.index(request, RequestOptions.DEFAULT);
//                }
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
