package com.douyinmall.promotion.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductType implements BaseEnum {
        DIGIT(1, "数码型"),
        FOOD(2, "食品型"),
        CLOTHING(3,"服装型")

        ;
        @EnumValue
        @JsonValue
        private final int value;
        private final String desc;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static ProductType of(Integer value) {
                if (value == null) {
                        return null;
                }
                for (ProductType status : values()) {
                        if (status.value == value) {
                                return status;
                        }
                }
                return null;
        }

}
