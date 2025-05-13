package com.douyinmall.item.service.Impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.douyinmall.common.entity.RedisData;
import com.douyinmall.common.result.PageResult;
import com.douyinmall.item.domain.DTO.ProductPageQuery;
import com.douyinmall.item.domain.VO.Product;
import com.douyinmall.item.mapper.ShopMapper;
import com.douyinmall.item.service.ShopService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.data.redis.core.StringRedisTemplate;



import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import static com.douyinmall.common.result.PageResult.EmptyPageResult;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Product> implements ShopService {
        private final ShopMapper shopMapper;
        private final StringRedisTemplate stringRedisTemplate;
        private final ObjectMapper objectMapper;
        private final static String CACHE_Product_KEY="shop:";
        private static final ExecutorService CACHE_REBUILD_EXECUTOR = new ThreadPoolExecutor(
                5, 10, 30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                Executors.defaultThreadFactory(),
                (r, executor) -> log.error("缓存重建任务被拒绝，当前队列已满")
        );
        public PageResult getProductList(ProductPageQuery query) {
                // 1. 生成缓存键（根据分页参数和查询条件）
                String cacheKey = generateCacheKey(query);
                String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
                if ("".equals(cacheValue)) {
                        return EmptyPageResult;
                }
                // 2. 缓存命中：直接反序列化返回
                if (StrUtil.isNotBlank(cacheValue)) {
                        try {
                                return objectMapper.readValue(cacheValue, PageResult.class);
                        } catch (JsonProcessingException e) {
                                // 缓存数据损坏，降级查数据库（可记录警告日志）
                                PageHelper.startPage(query.getPage(), query.getPageSize());
                                Page<Product> pageQuery = shopMapper.pageQuery(query);
                                PageResult result = new PageResult(pageQuery.getTotal(), pageQuery.getResult());
                                log.error("缓存数据损坏，降级查数据库", e);
                                stringRedisTemplate.keys("product_list:*").forEach(stringRedisTemplate::delete);
                                return result;
                        }
                }
                // 3. 缓存未命中：查数据库并更新缓存（带互斥锁防击穿）
                String lockKey = "lock:" + cacheKey;
                boolean lockAcquired = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);
                try {
                        if (lockAcquired) {
                                // 3.1 执行数据库查询
                                PageHelper.startPage(query.getPage(), query.getPageSize());
                                Page<Product> pageQuery = shopMapper.pageQuery(query);
                                if (pageQuery.getResult().isEmpty()) {
                                        stringRedisTemplate.opsForValue().set(lockKey, "", 2, TimeUnit.SECONDS);
                                        return null;
                                }
                                PageResult result = new PageResult(pageQuery.getTotal(), pageQuery.getResult());
                                // 3.2 序列化并写入Redis（设置过期时间）
                                String json = objectMapper.writeValueAsString(result);
                                stringRedisTemplate.opsForValue().set(cacheKey, json, 86400, TimeUnit.SECONDS);  // 缓存1天
                                return result;
                        } else {
                                // 未获取锁：等待50ms后重试（避免缓存击穿时的雪崩）
                                Thread.sleep(50);
                                return getProductList(query);  // 递归重试（可设置最大重试次数）
                        }
                } catch (Exception e) {
                        // 数据库查询异常，抛业务异常
                        throw new RuntimeException("查询商品列表失败", e);
                } finally {
                        if (lockAcquired) {
                                stringRedisTemplate.delete(lockKey);  // 释放锁
                        }
                }
        }

        private String generateCacheKey(ProductPageQuery query) {
                String paramsHash = String.valueOf(query.hashCode());
                return String.format("product_list:page_%d_size_%d_params_%s",
                        query.getPage(), query.getPageSize(), paramsHash);
        }


        @Override
        public Product getProductById(Long productId) {
                return queryWithLogicalExpire(productId);
        }

        @Override
        public Integer getProductsNum() {
                return shopMapper.getProductsNum();
        }

        @Override
        public void reduce(Map<Long, Integer> map) {
                Set<Long> productIds = map.keySet();
                List<Product> products = this.listByIds(productIds).stream().map(
                                product -> {
                                        Integer reduceNumber = map.getOrDefault(product.getId(), 0);
                                        product.setStock(product.getStock() - reduceNumber); // 更新库存
                                        return product;
                                })
                        .toList();
                updateBatchById(products);
                stringRedisTemplate.keys("product_list:*").forEach(stringRedisTemplate::delete);
        }

        @Override
        public void add(Map<Long, Integer> map) {
                Set<Long> productIds = map.keySet();
                List<Product> products = this.listByIds(productIds).stream().map(
                                product -> {
                                        Integer reduceNumber = map.getOrDefault(product.getId(), 0);
                                        product.setStock(product.getStock() + reduceNumber); // 更新库存
                                        return product;
                                })
                        .toList();
                updateBatchById(products);
                stringRedisTemplate.keys("product_list:*").forEach(stringRedisTemplate::delete);
        }

        @Override
        public List<Product> searchProduct(String description) throws IOException {
                RestHighLevelClient client = new RestHighLevelClient(
                        RestClient.builder(
                                HttpHost.create("http://192.168.163.129:9200")
                        )
                );

                List<Product> result = new ArrayList<>();

                // 1. 创建搜索请求
                SearchRequest request = new SearchRequest("product_index");

                // 2. 构建查询条件：同时搜索 name 和 description 字段（分词匹配）
                MultiMatchQueryBuilder query = QueryBuilders.multiMatchQuery(description, "name", "description")
                        .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)  // 匹配最佳字段（默认策略）
                         .minimumShouldMatch("35%");  // 最少匹配 35% 的分词
                // 3. 配置搜索源（可添加分页、排序等，此处简化）
                request.source().query(query);

                // 4. 执行搜索
                SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
                SearchHits hits = searchResponse.getHits();

                // 5. 处理搜索结果
                for (SearchHit hit : hits) {
                        String sourceJson = hit.getSourceAsString();
                        Product product = JSONUtil.toBean(sourceJson, Product.class);
                        result.add(product);
                }

                        client.close();


                return result;
        }


        //获取互斥锁
        private boolean tryLock(String key) {
                Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 20, TimeUnit.SECONDS);
                return BooleanUtil.isTrue(flag);
        }

        //释放互斥锁
        private void unLock(String key) {
                stringRedisTemplate.delete(key);
        }

        // 缓存击穿使用逻辑过期
        public Product queryWithLogicalExpire(Long id) {
                String ProductJson = stringRedisTemplate.opsForValue().get(CACHE_Product_KEY + id);
                if ("".equals(ProductJson)) {//不需要查询数据库，写入redis
                        return null;
                }
                LocalDateTime expireTime = null;
                Product Product = null;
                if (ProductJson != null) {
                        RedisData redisData = JSONUtil.toBean(ProductJson, RedisData.class);
                        JSONObject data = (JSONObject) redisData.getData();
                        Product = JSONUtil.toBean(data, Product.class);
                        expireTime = redisData.getExpireTime();
                        if (expireTime.isAfter(LocalDateTime.now())) {//未过期
                                return Product;
                        }
                }
                String lockKey = "shop:lock:" + id;
                boolean isLock = tryLock(lockKey);
                if (isLock) {
                        if (expireTime == null || expireTime.isBefore(LocalDateTime.now())) {//再次检查是否过期，避免并发重建
                                // 开启独立线程，实现缓存重建
                                CACHE_REBUILD_EXECUTOR.submit(() -> {
                                        try {
                                                //重建缓存
                                                this.saveProductToRedis(id, 60L);
                                                //释放锁
                                        } catch (Exception e) {
                                                throw new RuntimeException(e);
                                        } finally {
                                                unLock(lockKey);
                                        }
                                });
                        }
                }
                return Product;
        }

        public void saveProductToRedis(Long id, Long expireSeconds) throws JsonProcessingException {
                Product Product = getById(id);
                if (Product == null) {
                        stringRedisTemplate.opsForValue().set(CACHE_Product_KEY + id, "",30, TimeUnit.SECONDS);
                }
                RedisData redisData = new RedisData();
                redisData.setData(Product);
                redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
                // 写入redis,永久有效，采用逻辑过期
                String json = objectMapper.writeValueAsString(redisData);
                stringRedisTemplate.opsForValue().set(CACHE_Product_KEY + id, json);
        }
}