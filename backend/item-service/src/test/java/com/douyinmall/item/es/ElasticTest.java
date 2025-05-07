//package com.douyinmall.item.es;
//
//import org.apache.http.HttpHost;
//
//import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.client.indices.CreateIndexRequest;
//import org.elasticsearch.client.indices.GetIndexRequest;
//import org.elasticsearch.common.xcontent.XContentType;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//
//public class ElasticTest {
//        private RestHighLevelClient client;
//
//        private static final String MAPPING = "{\n" +
//                "  \"mappings\": {\n" +
//                "    \"properties\": {\n" +
//                "      \"id\": {\n" +
//                "        \"type\": \"keyword\"\n" +
//                "      },\n" +
//                "      \"name\":{\n" +
//                "        \"type\": \"text\",\n" +
//                "        \"analyzer\": \"ik_smart\"\n" +
//                "      },\n" +
//                "      \"price\":{\n" +
//                "        \"type\": \"float\"\n" +
//                "      },\n" +
//                "      \"stock\":{\n" +
//                "        \"type\": \"integer\"\n" +
//                "      },\n" +
//                "      \"image\":{\n" +
//                "        \"type\": \"keyword\",\n" +
//                "        \"index\": false\n" +
//                "      },\n" +
//                "      \"description\":{\n" +
//                "        \"type\": \"keyword\"\n" +
//                "      }\n" +
//                "    }\n" +
//                "  }\n" +
//                "}";
//
//        @Test
//        void testCreateIndex() throws IOException {
//                CreateIndexRequest request = new CreateIndexRequest("product");
//                request.source(MAPPING, XContentType.JSON);
//                client.indices().create(request, RequestOptions.DEFAULT);
//        }
//
//        @Test
//        void testDeleteIndex() throws IOException {
//                DeleteIndexRequest request = new DeleteIndexRequest("product");
//                client.indices().delete(request, RequestOptions.DEFAULT);
//        }
//
//        @Test
//        void testGetIndex() throws IOException {
//                GetIndexRequest request = new GetIndexRequest("product");
//                client.indices().exists(request, RequestOptions.DEFAULT);
//        }
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