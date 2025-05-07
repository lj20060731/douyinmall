package com.douyinmall.promotion.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.douyinmall.common.Holder.TokenHolder;
import com.douyinmall.common.exceptions.BadRequestException;
import com.douyinmall.common.exceptions.BizIllegalException;
import com.douyinmall.common.utils.BeanUtils;
import com.douyinmall.common.utils.CollUtils;
import com.douyinmall.promotion.constants.PromotionConstants;
import com.douyinmall.promotion.domain.dto.CouponFormDTO;
import com.douyinmall.promotion.domain.dto.CouponIssueFormDTO;
import com.douyinmall.promotion.domain.dto.PageDTO;
import com.douyinmall.promotion.domain.po.Coupon;
import com.douyinmall.promotion.domain.po.CouponScope;
import com.douyinmall.promotion.domain.po.UserCoupon;
import com.douyinmall.promotion.domain.query.CouponQuery;
import com.douyinmall.promotion.domain.vo.CouponPageVO;
import com.douyinmall.promotion.domain.vo.CouponVO;
import com.douyinmall.promotion.enums.CouponStatus;
import com.douyinmall.promotion.enums.ObtainType;
import com.douyinmall.promotion.enums.ProductType;
import com.douyinmall.promotion.enums.UserCouponStatus;
import com.douyinmall.promotion.mapper.CouponMapper;
import com.douyinmall.promotion.service.ICouponScopeService;
import com.douyinmall.promotion.service.ICouponService;
import com.douyinmall.promotion.service.IExchangeCodeService;
import com.douyinmall.promotion.service.IUserCouponService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.douyinmall.promotion.constants.PromotionConstants.COUPON_CACHE_KEY_PREFIX;
import static com.douyinmall.promotion.enums.CouponStatus.*;


/**
 * <p>
 * 优惠券的规则信息 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-03-28
 */
@RequiredArgsConstructor
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements ICouponService {

        private final ICouponScopeService couponScopeService;
        private final IExchangeCodeService exchangeCodeService;
        private final IUserCouponService userCouponService;
        private final StringRedisTemplate stringRedisTemplate;
        private final CouponMapper couponMapper;
        private final TokenHolder tokenHolder;

        @Override
        @Transactional
        public void saveCoupon(CouponFormDTO dto) {
                Coupon coupon = BeanUtil.copyProperties(dto, Coupon.class);
                this.save(coupon);
                if (!dto.getSpecific()) {
                        return;
                }
                List<Integer> scopes = dto.getScopes();
                if (CollUtils.isEmpty(scopes)) {
                        throw new BadRequestException("优惠券的范围不能为空");
                }
//                List<CouponScope> couponScopes = new ArrayList<>();
//                for (Long scope : scopes) {
//                        CouponScope couponScope = new CouponScope();
//                        couponScope.setBizId(scope);
//                        couponScope.setCouponId(coupon.getId());
//                        couponScope.setType(1);
//                        couponScopes.add(couponScope);
//                }
//                List<CouponScope> collect = scopes.stream().map(new Function<Long, CouponScope>() {
//                        @Override
//                        public CouponScope apply(Long scope) {
//                                CouponScope couponScope = new CouponScope();
//                                couponScope.setBizId(scope);
//                                couponScope.setCouponId(coupon.getId());
//                                couponScope.setType(1);
//                                return couponScope;
//                        }
//                }).collect(Collectors.toList());

                List<CouponScope> collect = scopes.stream().map(scope ->
                        new CouponScope().setType(ProductType.of(scope)).setCouponId(coupon.getId())
                ).collect(Collectors.toList());

                couponScopeService.saveBatch(collect);
        }

        @Override
        public PageDTO<CouponPageVO> queryCouponPage(CouponQuery query) {
                Page<Coupon> page = this.lambdaQuery()
                        .eq(query.getType() != null, Coupon::getDiscountType, query.getType())
                        .eq(query.getStatus() != null, Coupon::getStatus, query.getStatus())
                        .like(StringUtils.isNotBlank(query.getName()), Coupon::getName, query.getName())
                        .page(query.toMpPageDefaultSortByCreateTimeDesc());
                List<Coupon> records = page.getRecords();
                if (CollUtils.isEmpty(records)) {
                        return PageDTO.empty(page);
                }
                List<CouponPageVO> couponPageVOS = BeanUtils.copyList(records, CouponPageVO.class);
                return PageDTO.of(page, couponPageVOS);

        }

        @Override
        public void issueCoupon(CouponIssueFormDTO dto, Long id) {
                if (id == null || !id.equals(dto.getId())) {
                        throw new BadRequestException("优惠券的id不匹配");
                }
                Coupon coupon = this.getById(id);
                if (coupon == null) {
                        throw new BadRequestException("优惠券不存在");
                }
                if (coupon.getStatus() != CouponStatus.DRAFT && coupon.getStatus() != PAUSE) {
                        throw new BizIllegalException("优惠券的状态不匹配");
                }
                LocalDateTime now = LocalDateTime.now();
                boolean isBeginIssue = dto.getIssueBeginTime() == null || !dto.getIssueBeginTime().isAfter(now);
                if (isBeginIssue) {
                        coupon.setIssueBeginTime(dto.getIssueBeginTime() == null ? now : dto.getIssueBeginTime());
                        coupon.setStatus(ISSUING);
                        String key = COUPON_CACHE_KEY_PREFIX + coupon.getId();
                        stringRedisTemplate.opsForHash().put(key, "issueBeginTime", now.toString());
                        stringRedisTemplate.opsForHash().put(key, "issueEndTime", dto.getIssueEndTime().toString());
                        stringRedisTemplate.opsForHash().put(key, "totalNum", String.valueOf(coupon.getTotalNum()));
                        stringRedisTemplate.opsForHash().put(key, "userLimit", String.valueOf(coupon.getUserLimit()));
                } else {
                        coupon.setIssueBeginTime(dto.getIssueBeginTime());
                        coupon.setStatus(UN_ISSUE);
                }
                coupon.setIssueEndTime(dto.getIssueEndTime());
                coupon.setTermDays(dto.getTermDays());
                coupon.setTermBeginTime(dto.getTermBeginTime());
                coupon.setTermEndTime(dto.getTermEndTime());
                this.updateById(coupon);
                if (coupon.getObtainWay() == ObtainType.ISSUE && coupon.getStatus() == ISSUING) {
                        exchangeCodeService.asyncGenerateExchangeCode(coupon);
                }
        }

        @Override
        public List<CouponVO> queryCouponList() {
                List<Coupon> couponList = this.lambdaQuery()
                        .eq(Coupon::getStatus, ISSUING)
                        .eq(Coupon::getObtainWay, ObtainType.PUBLIC)
                        .list();
                if (CollUtils.isEmpty(couponList)) {
                        return null;
                }
                List<Long> couponIds = couponList.stream().map(Coupon::getId).collect(Collectors.toList());
                List<UserCoupon> list = userCouponService.lambdaQuery()
                        .eq(UserCoupon::getUserId, tokenHolder.getUserId())
                        .in(UserCoupon::getCouponId, couponIds)
                        .list();
//                if (CollUtils.isEmpty(list)) {
//                        return null;
//                }
                // 统计优惠券的领取次数
                Map<Long, Long> issueMap = list.stream().collect(Collectors.groupingBy(UserCoupon::getCouponId, Collectors.counting()));
                // 统计已领优惠券的未使用次数
                Map<Long, Long> unUseMap = list.stream()
                        .filter(c -> c.getUserCouponStatus() == UserCouponStatus.UNUSED)
                        .collect(Collectors.groupingBy(UserCoupon::getCouponId, Collectors.counting()));
                List<CouponVO> couponVOS = new java.util.ArrayList<>(couponList.size());
                for (Coupon coupon : couponList) {
                        CouponVO vo = BeanUtil.copyProperties(coupon, CouponVO.class);
                        List<CouponScope> couponScopes = couponScopeService.lambdaQuery().eq(CouponScope::getCouponId, coupon.getId()).list();
                        List<ProductType> productTypes = couponScopes.stream().map(CouponScope::getType).toList();
                        vo.setAvailable(coupon.getIssueNum() < coupon.getTotalNum() && issueMap.getOrDefault(coupon.getId(), 0L) < coupon.getUserLimit());
                        vo.setScopes(productTypes);
                        couponVOS.add(vo);
                }
                return couponVOS;
        }

        @Override
        @Transactional
        public void pauseIssue(Long id) {
                // 1.查询旧优惠券
                Coupon coupon = getById(id);
                if (coupon == null) {
                        throw new BadRequestException("优惠券不存在");
                }

                // 2.当前券状态必须是未开始或进行中
                CouponStatus status = coupon.getStatus();
                if (status != UN_ISSUE && status != ISSUING) {
                        // 状态错误，直接结束
                        return;
                }

                // 3.更新状态
                boolean success = lambdaUpdate()
                        .set(Coupon::getStatus, PAUSE)
                        .eq(Coupon::getId, id)
                        .in(Coupon::getStatus, UN_ISSUE, ISSUING)
                        .update();
                if (!success) {
                        // 可能是重复更新，结束
                        log.error("重复暂停优惠券");
                }

                // 4.删除缓存
                stringRedisTemplate.delete(PromotionConstants.COUPON_CACHE_KEY_PREFIX + id);
        }
}
