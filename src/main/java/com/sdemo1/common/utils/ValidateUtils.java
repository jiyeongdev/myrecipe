package com.sdemo1.common.utils;

import java.util.Map;

public class ValidateUtils {

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static boolean isValidParam(Map<String, String> params, String key) {
        return params.containsKey(key) && !isNullOrEmpty(params.get(key));
    }


}
