package com.ws.common.util;

/**
 * 字符串工具类
 */
public class TextUtils {

    public static boolean isEmpty(String str) {
        if (str == null || str.trim().length() <= 0) {
            return true;
        }
        return false;
    }
}
