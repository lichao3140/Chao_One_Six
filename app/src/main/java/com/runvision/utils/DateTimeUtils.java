package com.runvision.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {
    public final static String DATETIME_FORMAT_ONE = "yyyy-MM-dd HH:mm:ss";
	public final static String DATETIME_FORMAT_TWO = "yyyyMMddHHmmss";
    
	public static String parseDataTimeToFormatString(long datetime) {
    	SimpleDateFormat format = new SimpleDateFormat(DATETIME_FORMAT_ONE, Locale.getDefault());
    	return format.format(new Date(datetime));
    }
	
	public static String parseDataTimeToFormatString(Date datetime) {
    	SimpleDateFormat format = new SimpleDateFormat(DATETIME_FORMAT_ONE, Locale.getDefault());
    	return format.format(datetime);
    }


	public static String parseDataTimeToFormatString(Date datetime, int type) {
		SimpleDateFormat format = new SimpleDateFormat(DATETIME_FORMAT_TWO, Locale.getDefault());
		return format.format(datetime);
	}
	public static long getTime() {
		String time = System.currentTimeMillis() + "";
		long mTime = Long.parseLong(time.substring(0, time.length() - 3));
		return mTime;
	}

	public static long getLongTime() {
		Date date = new Date();
		long dateTime = date.getTime();
		return dateTime;
	}

	/**
	 * "yyyy-MM-dd HH:mm:ss"
	 * 把long 转换成 日期 再转换成String类型
	 * @param dateFormat
	 * @param millSec
	 * @return
	 */
	public static String transferLongToDate(String dateFormat, Long millSec) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Date date = new Date(millSec);
		return sdf.format(date);
	}
}

