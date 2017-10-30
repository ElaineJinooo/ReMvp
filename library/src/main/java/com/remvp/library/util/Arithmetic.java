package com.remvp.library.util;

import android.text.TextUtils;

import java.math.BigDecimal;

/**
 * 数学计算工具类
 */
public class Arithmetic {
    /**
     * 加法
     *
     * @param f 数
     * @param i 数
     * @return
     */
    public static String add(String f, String i) {
        BigDecimal b1 = new BigDecimal(f);
        BigDecimal b2 = new BigDecimal(i);
        return b1.add(b2)
                .setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 乘法
     *
     * @param f 数
     * @param i 数
     * @return
     */
    public static String multiply(String f, String i) {
        BigDecimal b1 = new BigDecimal(f);
        BigDecimal b2 = new BigDecimal(i);
        return b1.multiply(b2)
                .setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 减法
     *
     * @param f 数
     * @param i 数
     * @return
     */
    public static String subtract(String f, String i) {
        BigDecimal b1 = new BigDecimal(f);
        BigDecimal b2 = new BigDecimal(i);
        return b1.subtract(b2)
                .setScale(2, BigDecimal.ROUND_HALF_UP).toString();

    }

    /**
     * 价格小于等于0时则为0.01
     *
     * @param str 数
     * @return
     */
    public static String toMoney(String str) {
        BigDecimal big = new BigDecimal(str);
        if (big.compareTo(BigDecimal.ZERO) != 1) {
            return "0.01";
        }
        BigDecimal result = big.divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP);
        return result.toString();
    }

    /**
     * 字符串实际金额
     *
     * @param str 数
     * @return 为空返回0.00
     */
    public static String toCommonMoney(String str) {
        if (TextUtils.isEmpty(str)) {
            return "0.00";
        }
        BigDecimal big = new BigDecimal(str);
        BigDecimal result = big.divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP);
        return result.toString();
    }

    /**
     * 比较两个数的大小
     *
     * @param str1 第一个数
     * @param str2 第二个数
     * @return -1表示小于,0是等于,1是大于
     */
    public static int compareTo(String str1, String str2) {
        BigDecimal big1 = new BigDecimal(str1);
        BigDecimal big2 = new BigDecimal(str2);
        return big1.compareTo(big2);
    }
}
