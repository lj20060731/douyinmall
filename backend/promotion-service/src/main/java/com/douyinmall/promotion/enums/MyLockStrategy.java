package com.douyinmall.promotion.enums;

import com.douyinmall.common.exceptions.BizIllegalException;
import com.douyinmall.promotion.utils.MyLock;
import org.redisson.api.RLock;

public enum MyLockStrategy {
        // 尝试立即获取锁，如果锁不可用则立即返回 false，不会等待。
        SKIP_FAST() {
                @Override
                public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException {
                        return lock.tryLock(0, prop.leaseTime(), prop.unit());
                }
        },
        // 尝试立即获取锁，如果锁不可用则抛出 BizIllegalException 异常。
        FAIL_FAST() {
                @Override
                public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException {
                        boolean isLock = lock.tryLock(0, prop.leaseTime(), prop.unit());
                        if (!isLock) {
                                throw new BizIllegalException("请求太频繁");
                        }
                        return true;
                }
        },
        //持续尝试获取锁，直到获取到锁为止。
        KEEP_TRYING() {
                @Override
                public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException {
                        lock.lock(prop.leaseTime(), prop.unit());
                        return true;
                }
        },
        //在指定的等待时间内尝试获取锁，如果在等待时间内未能获取到锁，则返回 false。
        SKIP_AFTER_RETRY_TIMEOUT() {
                @Override
                public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException {
                        return lock.tryLock(prop.waitTime(), prop.leaseTime(), prop.unit());
                }
        },
        //在指定的等待时间内尝试获取锁，如果在等待时间内未能获取到锁，则抛出 BizIllegalException 异常。
        FAIL_AFTER_RETRY_TIMEOUT() {
                @Override
                public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException {
                        boolean isLock = lock.tryLock(prop.waitTime(), prop.leaseTime(), prop.unit());
                        if (!isLock) {
                                throw new BizIllegalException("请求太频繁");
                        }
                        return true;
                }
        },
        ;

        public abstract boolean tryLock(RLock lock, MyLock prop) throws InterruptedException;
}