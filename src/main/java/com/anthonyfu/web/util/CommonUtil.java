package com.anthonyfu.web.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.anthonyfu.web.constant.CommonConstant;

public class CommonUtil {

	// 將字串轉成 Long。
	public static Long parseLong(String value) {
		String normalizedValue = toNull(value);
		if (normalizedValue == null) {
			return null;
		}
		return Long.valueOf(normalizedValue);
	}

	// 將字串轉成 BigDecimal。
	public static BigDecimal parseBigDecimal(String value) {
		String normalizedValue = toNull(value);
		if (normalizedValue == null) {
			return null;
		}
		return new BigDecimal(normalizedValue);
	}
	
	// 將字串轉成 LocalDateTime。
	public static LocalDateTime parseDateTime(String value) {
		String normalizedValue = toNull(value);
		if (normalizedValue == null) {
			return null;
		}
		return LocalDateTime.parse(normalizedValue, CommonConstant.DATE_TIME_FORMATTER);
	}
	
	// 將空字串與 NULL 轉成 Java null。
	public static String toNull(String value) {
		if (value == null) {
			return null;
		}
		String normalizedValue = value.trim();
		if (normalizedValue.isEmpty()) {
			return null;
		}
		if ("NULL".equalsIgnoreCase(normalizedValue)) {
			return null;
		}
		return normalizedValue;
	}
}
