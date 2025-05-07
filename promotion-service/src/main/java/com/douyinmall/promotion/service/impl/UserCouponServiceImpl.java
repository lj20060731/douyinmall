package com.douyinmall.promotion.service.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.douyinmall.common.Holder.TokenHolder;
import com.douyinmall.common.exceptions.BadRequestException;
import com.douyinmall.common.result.Result;
import com.douyinmall.common.utils.BeanUtils;
import com.douyinmall.common.utils.CollUtils;
import com.douyinmall.common.utils.StringUtils;
import com.douyinmall.promotion.discount.Discount;
import com.douyinmall.promotion.discount.DiscountStrategy;
import com.douyinmall.promotion.domain.dto.CouponDiscountDTO;
import com.douyinmall.promotion.domain.dto.OrderProductDTO;
import com.douyinmall.promotion.domain.dto.UsedCouponDTO;
import com.douyinmall.promotion.domain.dto.UserCouponDTO;
import com.douyinmall.promotion.domain.po.Coupon;
import com.douyinmall.promotion.domain.po.CouponScope;
import com.douyinmall.promotion.domain.po.ExchangeCode;
import com.douyinmall.promotion.domain.po.UserCoupon;
import com.douyinmall.promotion.domain.vo.UserCouponVO;
import com.douyinmall.promotion.enums.*;
import com.douyinmall.promotion.mapper.CouponMapper;
import com.douyinmall.promotion.mapper.UserCouponMapper;
import com.douyinmall.promotion.mq.RabbitMqHelper;
import com.douyinmall.promotion.service.ICouponScopeService;
import com.douyinmall.promotion.service.IExchangeCodeService;
import com.douyinmall.promotion.service.IUserCouponService;
import com.douyinmall.promotion.utils.CodeUtil;
import com.douyinmall.promotion.utils.MyLock;
import com.douyinmall.promotion.utils.PermuteUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.douyinmall.promotion.constants.PromotionConstants.COUPON_CACHE_KEY_PREFIX;
import static com.douyinmall.promotion.constants.PromotionConstants.USER_COUPON_CACHE_KEY_PREFIX;
import static com.douyinmall.promotion.enums.UserCouponStatus.UNUSED;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-03-29
 */
@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements IUserCouponService {

        private final CouponMapper couponMapper;
        private final IExchangeCodeService exchangeCodeService;
        private final StringRedisTemplate stringRedisTemplate;
        private final RabbitMqHelper rabbitMqHelper;
        private final ICouponScopeService couponScopeService;
        private final TokenHolder tokenHolder;
        private final UserCouponMapper userCouponMapper;

        @Resource(name = "applicationTaskExecutor")
        private Executor calculateSolutionExecutor;

        @Override
        @MyLock(name = "lock:coupon:uid:#{userId}")
        public Result<String> receiveCoupon(Long id) {
                String key = COUPON_CACHE_KEY_PREFIX + id;
                Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
                Coupon coupon = BeanUtils.mapToBean(entries, Coupon.class, false, CopyOptions.create());
                if (coupon == null) {
                        return Result.error("优惠券不存在");
                }
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(coupon.getIssueBeginTime()) || now.isAfter(coupon.getIssueEndTime())) {
                        return Result.error("优惠券发放时间不在有效期内");
                }
                if (coupon.getTotalNum() <= 0) {
                        return Result.error("优惠券发放数量不足");
                }
                Integer userLimit = coupon.getUserLimit();
                String userKey = USER_COUPON_CACHE_KEY_PREFIX + id;
                Long increment = stringRedisTemplate.opsForHash().increment(userKey, tokenHolder.getUserId().toString(), 1);
                if (increment > userLimit) {
                        return Result.error("优惠券已达领取上限");
                }
                String couponKey = COUPON_CACHE_KEY_PREFIX + id;
                stringRedisTemplate.opsForHash().increment(couponKey, "totalNum", -1);
                UserCouponDTO userCouponDTO = new UserCouponDTO();
                userCouponDTO.setUserId(tokenHolder.getUserId());
                userCouponDTO.setCouponId(id);
                rabbitMqHelper.send(
                        "promotion.topic",
                        "coupon.receive",
                        userCouponDTO
                );
//                couponMapper.updateIssueNum(id);
//                saveUserCoupon(UserContext.getUser(), coupon);
                return Result.success("领取成功");
        }

        @Override
        public Result<String> exchangeCoupon(String code) {
                if (StringUtils.isBlank(code)) {
                        return Result.error("兑换码不能为空");
                }
                long serialNum;
                //获得序列号
                try {
                        serialNum = CodeUtil.parseCode(code);
                } catch (BadRequestException e) {
                        return Result.error("无效兑换码");
                }

                //判断兑换码是否被使用
                boolean result = exchangeCodeService.updateExchangeCodeMark(serialNum, true);
                if (result) {
                        return Result.error("兑换码已被使用");
                }
                try {
                        //校验兑换码
                        ExchangeCode exchangeCode = exchangeCodeService.getById(serialNum);
                        if (exchangeCode == null) {
                                return Result.error("兑换码不存在");
                        }
                        LocalDateTime now = LocalDateTime.now();
                        if (now.isAfter(exchangeCode.getExpiredTime())) {
                                return Result.error("兑换码已过期");
                        }
                        Long userId = tokenHolder.getUserId();
                        Long couponId = exchangeCode.getExchangeTargetId();
                        //校验优惠券
                        Coupon coupon = couponMapper.selectById(couponId);
                        if (coupon == null) {
                                return Result.error("优惠券不存在");
                        }
                        //校验并更新
                        UserCouponServiceImpl userCouponService = (UserCouponServiceImpl) AopContext.currentProxy();
                        userCouponService.checkAndCreateUserCoupon(userId, coupon, serialNum);

                } catch (Exception e) {
                        //兑换失败，恢复redis兑换码状态
                        exchangeCodeService.updateExchangeCodeMark(serialNum, false);
                        return Result.error("兑换失败");
                }
                return Result.success("兑换成功");
        }

        @Override
        public void checkAndCreateUserCouponNew(UserCouponDTO dto) {
                Coupon coupon = couponMapper.selectById(dto.getCouponId());
                if (coupon == null) {
                        return;
                }
                couponMapper.updateIssueNum(coupon.getId());
                saveUserCoupon(dto.getUserId(), coupon);
        }

        @Override
        public void useCoupon(UsedCouponDTO usedCouponDTO) {
                List<Long> couponIds = usedCouponDTO.getCouponIds();
                Long orderId = usedCouponDTO.getOrderId();
                List<UserCoupon> userCoupons = new ArrayList<>();
                for (Long couponId : couponIds) {
                        UserCoupon uc = userCouponMapper.selectOne(
                                new LambdaQueryWrapper<UserCoupon>()
                                        .eq(UserCoupon::getUserId, tokenHolder.getUserId())
                                        .eq(UserCoupon::getCouponId, couponId)
                                        .eq(UserCoupon::getUserCouponStatus, UserCouponStatus.UNUSED) // 假设 UNUSED 是枚举或常量
                        );
                        if (uc == null || uc.getUserCouponStatus() != UNUSED || uc.getTermEndTime().isBefore(LocalDateTime.now()) || uc.getTermBeginTime().isAfter(LocalDateTime.now())) {
                                throw new BadRequestException("优惠券已失效");
                        }
                        UserCoupon userCoupon = new UserCoupon();
                        userCoupon.setUsedTime(LocalDateTime.now());
                        userCoupon.setUserCouponStatus(UserCouponStatus.USED);
                        userCoupon.setOrderId(orderId);
                        userCoupon.setId(uc.getId());
                        userCoupons.add(userCoupon);
                }
                UserCouponServiceImpl userCouponService = (UserCouponServiceImpl) AopContext.currentProxy();
                userCouponService.updateBatchById(userCoupons);
        }

        @Override
        public Result<Map<Long, List<UserCouponVO>>> getUserCouponByOrderId(List<Long> orderIds) {
                Map<Long, List<UserCouponVO>> result = new HashMap<>();
                for (Long orderId : orderIds) {
                        List<UserCoupon> userCoupons = lambdaQuery().eq(UserCoupon::getOrderId, orderId).eq(UserCoupon::getUserId, tokenHolder.getUserId()).list();
                        List<Long> collect = userCoupons.stream().map(UserCoupon::getCouponId).collect(Collectors.toList());
                        if (CollUtils.isEmpty(collect)) {
                                result.put(orderId, CollUtils.emptyList());
                                continue;
                        }
                        List<Coupon> coupons = couponMapper.selectBatchIds(collect);
                        List<UserCouponVO> userCouponVOS = BeanUtils.copyList(coupons, UserCouponVO.class);
                        result.put(orderId, userCouponVOS);
                }
                return Result.success(result);
        }

        @Override
        public List<CouponDiscountDTO> queryAvailableCoupon(List<OrderProductDTO> products) {
                //查询用户可用优惠券
                List<Coupon> coupons = getBaseMapper().queryMyCoupon(tokenHolder.getUserId());
                if (CollUtils.isEmpty(coupons)) {
                        return CollUtils.emptyList();
                }
                //初次筛选可用优惠券，考虑金额
                int totalAmount = products.stream().mapToInt(OrderProductDTO::getPrice).sum();
                List<Coupon> availableCoupons = coupons.stream()
                        .filter(coupon -> DiscountStrategy.getDiscount(coupon.getDiscountType()).canUse(totalAmount, coupon))
                        .collect(Collectors.toList());
                if (CollUtils.isEmpty(availableCoupons)) {
                        return CollUtils.emptyList();
                }
                //再次筛选可用优惠券，考虑适用商品
                Map<Coupon, List<OrderProductDTO>> avaMap = findAvailableCoupons(availableCoupons, products);
                if (CollUtils.isEmpty(avaMap)) {
                        return CollUtils.emptyList();
                }
                availableCoupons = new ArrayList<>(avaMap.keySet());
                //排列组合，计算可用优惠券
                List<List<Coupon>> solutions = PermuteUtil.permute(availableCoupons);
                //存放单张优惠券
                for (Coupon coupon : availableCoupons) {
                        solutions.add(List.of(coupon));
                }
                List<CouponDiscountDTO> result = Collections.synchronizedList(new ArrayList<>(solutions.size()));
                CountDownLatch latch = new CountDownLatch(solutions.size());
                //计算每一个方案的优惠金额
                for (List<Coupon> solution : solutions) {
                        CompletableFuture.supplyAsync(
                                () -> calculateSolutionDiscount(avaMap, solution, products)
                                , calculateSolutionExecutor).thenAccept(couponDiscountDTO -> {
                                result.add(couponDiscountDTO);
                                latch.countDown();
                        });
                }
                try {
                        latch.await(2, TimeUnit.SECONDS);//主线程最多等待2秒
                } catch (InterruptedException e) {
                        log.error("计算优惠金额异常", e);
                }

                return findBestSolution(result);
        }

        @Override
        public Result<List<UserCouponVO>> queryMyCoupon() {
                Long userId = tokenHolder.getUserId();
                List<UserCoupon> list = lambdaQuery().eq(UserCoupon::getUserId, userId).list();
                List<UserCouponVO> userCouponVOS = new ArrayList<>();
                for (UserCoupon userCoupon : list) {
                        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
                        UserCouponVO userCouponVO = BeanUtils.copyBean(coupon, UserCouponVO.class);
                        userCouponVO.setCouponId(coupon.getId());
                        userCouponVO.setUserCouponStatus(userCoupon.getUserCouponStatus());
                        List<CouponScope> couponScopes = couponScopeService.lambdaQuery().eq(CouponScope::getCouponId, userCouponVO.getCouponId()).list();
                        List<ProductType> productTypes = couponScopes.stream().map(CouponScope::getType).toList();
                        userCouponVO.setScopes(productTypes);
                        userCouponVOS.add(userCouponVO);
                }
                return Result.success(userCouponVOS);
        }


        //计算每一个方案的优惠金额
        private CouponDiscountDTO calculateSolutionDiscount(Map<Coupon, List<OrderProductDTO>> avaMap, List<Coupon> solution, List<OrderProductDTO> courses) {
                CouponDiscountDTO dto = new CouponDiscountDTO();
                //存每一个商品的优惠金额，初始化为id->0
                Map<Long, Integer> detailMap = courses.stream().collect(Collectors.toMap(OrderProductDTO::getId, OrderProductDTO -> 0));
                //遍历方案每一张优惠券
                for (Coupon coupon : solution) {
                        //获取该优惠券适用商品
                        List<OrderProductDTO> avaCourses = avaMap.get(coupon);
                        //计算使用商品的总金额
                        int totalAmount = avaCourses.stream().mapToInt(
                                value -> value.getPrice() - detailMap.get(value.getId())
                        ).sum();
                        //判断是否满足优惠条件，根据优惠券类型
                        Discount discount = DiscountStrategy.getDiscount(coupon.getDiscountType());
                        //如果不满足，跳过
                        if (!discount.canUse(totalAmount, coupon)) {
                                continue;
                        }
                        //计算当前优惠券和适用的商品可以优惠的金额，不能超过该优惠券限定的最大优惠金额
                        int calDiscount = discount.calculateDiscount(totalAmount, coupon);
                        //计算每一个商品的优惠金额，按比例分配
                        calculateDetailDiscount(detailMap, avaCourses, totalAmount, calDiscount);
                        //添加使用的优惠券id,规则,总优惠金额
                        dto.getIds().add(coupon.getId());
                        dto.getRules().add(discount.getRule(coupon));
                        dto.setDiscountAmount(dto.getDiscountAmount() + calDiscount);
                }
                dto.setDiscountDetail(detailMap);
                return dto;
        }

        //计算每一个商品的优惠金额
        private void calculateDetailDiscount(Map<Long, Integer> detailMap, List<OrderProductDTO> avaCourses, int totalAmount, int calDiscount) {
                int times = 0;
                int remainingDiscount = calDiscount;
                for (OrderProductDTO course : avaCourses) {
                        times++;
                        int discount;
                        if (times == avaCourses.size()) {
                                //如果是最后一个，直接使用剩余优惠金额
                                discount = remainingDiscount;
                        } else {
                                //如果不是最后一个，按照比例计算优惠金额
                                discount = course.getPrice() * calDiscount / totalAmount;
                                remainingDiscount -= discount;
                        }
                        //累加优惠金额
                        detailMap.put(course.getId(), discount + detailMap.get(course.getId()));
                }
        }

        private Map<Coupon, List<OrderProductDTO>> findAvailableCoupons(List<Coupon> coupons, List<OrderProductDTO> orderProducts) {
                Map<Coupon, List<OrderProductDTO>> map = new HashMap<>();
                for (Coupon coupon : coupons) {
                        List<OrderProductDTO> available = orderProducts;
                        if (coupon.getSpecific()) {
                                //查询优惠券适用商品类型
                                List<CouponScope> list = couponScopeService.lambdaQuery().eq(CouponScope::getCouponId, coupon.getId()).list();
                                List<ProductType> scopeIds = list.stream().map(CouponScope::getType).toList();
                                available = orderProducts.stream().filter(
                                                OrderProductDTO -> scopeIds.contains(OrderProductDTO.getProductType()))
                                        .collect(Collectors.toList());
                        }
                        int totalAmount = available.stream().mapToInt(OrderProductDTO::getPrice).sum();
                        boolean canUse = DiscountStrategy.getDiscount(coupon.getDiscountType()).canUse(totalAmount, coupon);
                        if (canUse) {
                                map.put(coupon, available);
                        }
                }
                return map;
        }

        //找出最优解
        private List<CouponDiscountDTO> findBestSolution(List<CouponDiscountDTO> result) {
                Map<String, CouponDiscountDTO> moreDiscountMap = new HashMap<>();
                Map<Integer, CouponDiscountDTO> lessCouponMap = new HashMap<>();
                for (CouponDiscountDTO solution : result) {
                        String ids = solution.getIds().stream().sorted(Comparator.comparing(Long::longValue)).map(String::valueOf).collect(Collectors.joining(","));
                        CouponDiscountDTO old = moreDiscountMap.get(ids);
                        if (old != null && old.getDiscountAmount() >= solution.getDiscountAmount()) {
                                continue;
                        }
                        old = lessCouponMap.get(solution.getDiscountAmount());
                        if (old != null && old.getIds().size() <= solution.getIds().size()) {
                                continue;
                        }
                        moreDiscountMap.put(ids, solution);
                        lessCouponMap.put(solution.getDiscountAmount(), solution);
                }
                Collection<CouponDiscountDTO> bestSolution = CollUtils.intersection(moreDiscountMap.values(), lessCouponMap.values());
                return bestSolution.stream().sorted(Comparator.comparingInt(CouponDiscountDTO::getDiscountAmount).reversed()).collect(Collectors.toList());
        }

        @Transactional
        @MyLock(name = "lock:coupon:uid:#{userId}", lockType = MyLockType.RE_ENTRANT_LOCK, lockStrategy = MyLockStrategy.FAIL_AFTER_RETRY_TIMEOUT)
        public void checkAndCreateUserCoupon(Long userId, Coupon coupon, Long serialNum) {
                int count = Math.toIntExact(this.lambdaQuery()
                        .eq(UserCoupon::getCouponId, coupon.getId())
                        .eq(UserCoupon::getUserId, userId)
                        .count());
                if (count >= coupon.getUserLimit()) {
                        throw new RuntimeException("优惠券已达领取上限");
                }
                //把优惠券发放数量+1
                couponMapper.updateIssueNum(coupon.getId());
                // 保存用户优惠券
                saveUserCoupon(userId, coupon);
                if (serialNum != null) {
                        //更新兑换码状态
                        exchangeCodeService.lambdaUpdate()
                                .set(ExchangeCode::getUserId, userId)
                                .set(ExchangeCode::getStatus, ExchangeCodeStatus.USED)
                                .eq(ExchangeCode::getId, serialNum)
                                .update();
                }
        }

        private void saveUserCoupon(Long user, Coupon coupon) {
                LocalDateTime termBeginTime = coupon.getTermBeginTime();
                LocalDateTime termEndTime = coupon.getTermEndTime();
                if (termBeginTime == null && termEndTime == null) {
                        termBeginTime = LocalDateTime.now();
                        termEndTime = termBeginTime.plusDays(coupon.getTermDays());
                }
                this.save(new UserCoupon()
                        .setUserId(user)
                        .setCouponId(coupon.getId())
                        .setCreateTime(LocalDateTime.now())
                        .setTermBeginTime(termBeginTime)
                        .setTermEndTime(termEndTime));
        }
}
