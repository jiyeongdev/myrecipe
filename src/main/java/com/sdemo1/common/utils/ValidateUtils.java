package com.sdemo1.common.utils;

public class ValidateUtils {

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static boolean isValidParam(String param) {
        return param != null && !param.trim().isEmpty();
    }


}
