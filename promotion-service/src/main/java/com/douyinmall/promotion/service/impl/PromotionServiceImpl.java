package com.douyinmall.promotion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.douyinmall.promotion.domain.po.Promotion;
import com.douyinmall.promotion.mapper.PromotionMapper;
import com.douyinmall.promotion.service.IPromotionService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 促销活动，形式多种多样，例如：优惠券 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-03-28
 */
@Service
public class PromotionServiceImpl extends ServiceImpl<PromotionMapper, Promotion> implements IPromotionService {

}
