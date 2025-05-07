-- 参数说明：
-- KEYS[1...n]: 商品缓存键列表（如 ["shop:1", "shop:2"]）
-- ARGV[1...n]: 需要扣减的数量列表（如 [5, 3]），顺序与 KEYS 一一对应

-- 第一步：预校验所有商品库存是否足够
for i = 1, #KEYS do
    local productJson = redis.call('GET', KEYS[i])
    if not productJson then
        return -2  -- 商品不存在（缓存未命中）
    end

    -- 解码RedisData对象
    local redisData = cjson.decode(productJson)
    local product = redisData.data  -- 从data字段获取Product对象

    -- 校验Product对象是否存在
    if not product then
        return -5  -- RedisData.data为空（数据结构异常）
    end

    -- 校验stock字段是否存在且为数字
    if not product.stock or type(product.stock) ~= "number" then
        return -3  -- 商品库存字段缺失或类型错误
    end

    -- 校验扣减数量是否为有效数字
    local requiredNumber = tonumber(ARGV[i])
    if not requiredNumber or requiredNumber <= 0 then
        return -4  -- 扣减数量无效（非数字或小于等于0）
    end

    -- 校验库存是否足够
    if product.stock < requiredNumber then
        return -1  -- 库存不足
    end
end

-- 第二步：所有商品库存充足，执行批量扣减
for i = 1, #KEYS do
    local productJson = redis.call('GET', KEYS[i])
    local redisData = cjson.decode(productJson)
    local product = redisData.data  -- 获取Product对象

    -- 扣减库存
    local requiredNumber = tonumber(ARGV[i])
    product.stock = product.stock - requiredNumber

    -- 更新Redis中的RedisData对象（重新序列化）
    redisData.data = product  -- 替换data字段为更新后的Product
    redis.call('SET', KEYS[i], cjson.encode(redisData))
end

return 1  -- 所有商品扣减成功