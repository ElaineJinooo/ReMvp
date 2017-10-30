package com.remvp.library.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 时间格式工具类
 */
public class DateTimeUtil {
    private final static String DATE_FORMAT = "yyyy-MM-dd";

    public final static String DATE_FORMAT_CN = "yyyy年MM月dd日";

    public final static String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public final static String TIME_FORMAT_CN = "yyyy年MM月dd日 HH:mm:ss";

    private final static String MONTH_FORMAT = "yyyy-MM";

    public final static String DAY_FORMAT = "yyyyMMddHHmmss";
    public final static String YMD = "yyyyMMdd";

    private final static String ONLY_DAY_FORMAT = "dd";
    public final static String ONLY_TIME_FORMAT = "HH:mm:ss";
    public final static String NO_SEC_TIME_FORMAT = "yyyy/MM/dd HH:mm";
    public final static String DOT_Y_M_D = "yyyy.MM.dd";
    public final static String BAR_Y_M_D_H_M = "yyyy-MM-dd  HH:mm";
    public final static String H_M_FORMAT = "HH:mm";
    public final static String SLA_Y_M_D = "yyyy/MM/dd";

    /**
     * 取得当前系统时间，返回java.util.Date类型
     *
     * @return java.util.Date 返回服务器当前系统时间
     * @see java.util.Date
     */
    public static java.util.Date getCurrDate() {
        return new java.util.Date();
    }

    public static String getOnlyDayFormat(Date date) {
        return (String) android.text.format.DateFormat.format(ONLY_DAY_FORMAT, date); //20
    }

    /**
     * 得到系统当前日期的前或者后几天
     *
     * @param iDate 如果要获得前几天日期，该参数为负数； 如果要获得后几天日期，该参数为正数
     * @return Date 返回系统当前日期的前或者后几天
     * @see java.util.Calendar#add(int, int)
     */
    public static Date getDateBeforeOrAfter(int iDate) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, iDate);
        return cal.getTime();
    }

    /**
     * 得到日期的前或者后几天
     *
     * @param iDate 如果要获得前几天日期，该参数为负数； 如果要获得后几天日期，该参数为正数
     * @return Date 返回参数<code>curDate</code>定义日期的前或者后几天
     * @see java.util.Calendar#add(int, int)
     */
    public static Date getDateBeforeOrAfter(Date curDate, int iDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(curDate);
        cal.add(Calendar.DAY_OF_MONTH, iDate);
        return cal.getTime();
    }

    /**
     * @param first
     * @param second
     * @return 获取两个Date之间的天数的列表
     * @author lenghao
     * @createTime 2008-8-5 下午01:57:09
     */
    public static List<Date> getDaysListBetweenDates(Date first, Date second) {
        List<Date> dateList = new ArrayList<Date>();
        Date d1 = getFormatDateTime(getFormatDate(first), DATE_FORMAT);
        Date d2 = getFormatDateTime(getFormatDate(second), DATE_FORMAT);
        if (d1.compareTo(d2) > 0) {
            return dateList;
        }
        do {
            dateList.add(d1);
            d1 = getDateBeforeOrAfter(d1, 1);
        } while (d1.compareTo(d2) <= 0);
        return dateList;
    }

    /**
     * 得到格式化后的日期，格式为yyyy-MM-dd，如2006-02-15
     *
     * @param currDate 要格式化的日期
     * @return String 返回格式化后的日期，默认格式为为yyyy-MM-dd，如2006-02-15
     * @see #getFormatDate(java.util.Date, String)
     */
    public static String getFormatDate(java.util.Date currDate) {
        return getFormatDate(currDate, DATE_FORMAT);
    }

    /**
     * 根据格式得到格式化后的时间
     *
     * @param currDate 要格式化的时间
     * @param format   时间格式，如yyyy-MM-dd HH:mm:ss
     * @return Date 返回格式化后的时间，格式由参数<code>format</code>定义，如yyyy-MM-dd
     * HH:mm:ss
     * @see java.text.SimpleDateFormat#parse(java.lang.String)
     */
    public static Date getFormatDateTime(String currDate, String format) {
        if (currDate == null) {
            return null;
        }
        SimpleDateFormat dtFormatdB = null;
        try {
            dtFormatdB = new SimpleDateFormat(format);
            return dtFormatdB.parse(currDate);
        } catch (Exception e) {
            dtFormatdB = new SimpleDateFormat(TIME_FORMAT);
            try {
                return dtFormatdB.parse(currDate);
            } catch (Exception ex) {
            }
        }
        return null;
    }

    public static String getFormatOnlyTime(String stamp) {
        if (stamp == null) {
            return null;
        }
        SimpleDateFormat dtFormatDB = new SimpleDateFormat(ONLY_TIME_FORMAT);
        SimpleDateFormat format1 = new SimpleDateFormat(TIME_FORMAT);
        Date date = null;
        try {
            date = format1.parse(stamp);
            return dtFormatDB.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 根据格式得到格式化后的日期
     *
     * @param currDate 要格式化的日期
     * @param format   日期格式，如yyyy-MM-dd
     * @return String 返回格式化后的日期，格式由参数<code>format</code>
     * 定义，如yyyy-MM-dd，如2006-02-15
     * @see java.text.SimpleDateFormat#format(java.util.Date)
     */
    public static String getFormatDate(java.util.Date currDate, String format) {
        if (currDate == null) {
            return "";
        }
        SimpleDateFormat dtFormatdB = null;
        try {
            dtFormatdB = new SimpleDateFormat(format);
            return dtFormatdB.format(currDate);
        } catch (Exception e) {
            dtFormatdB = new SimpleDateFormat(DATE_FORMAT);
            try {
                return dtFormatdB.format(currDate);
            } catch (Exception ex) {
            }
        }
        return null;
    }

    /**
     * 根据日期取得星期几
     *
     * @param date
     * @return "日","一","二","三","四","五","六"
     */
    public static String getWeek(Date date) {
        String[] weeks = {"星期天", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (week_index < 0) {
            week_index = 0;
        }
        return weeks[week_index];
    }

    /**
     * 获取今天是星期几
     *
     * @return
     */
    public static String getCurrWeek() {
        return getWeek(getCurrDate());
    }

    /**
     * 日期格式字符串转换成时间戳 yyyy-MM-dd HH:mm:ss
     *
     * @param date_str 字符串日期
     * @return
     */
    public static String date2TimeStamp(String date_str) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
            return String.valueOf(sdf.parse(date_str).getTime() / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 日期转换成时间戳
     *
     * @param date
     * @return
     */
    public static String date2TimeStamp(Date date) {
        return String.valueOf(date.getTime() / 1000);
    }

    /**
     * 得到格式化后的当月第一天，格式为yyyy-MM-dd，如2006-02-01
     *
     * @return String 返回格式化后的当月第一天，格式为yyyy-MM-dd，如2006-02-01
     * @see java.util.Calendar#getMinimum(int)
     * @see #getFormatDate(java.util.Date, String)
     */
    public static String getFirstDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        int firstDay = cal.getMinimum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, firstDay);
        return getFormatDate(cal.getTime(), DATE_FORMAT);
    }

    /**
     * PHP时间转换
     *
     * @param str
     * @return
     */
    public static String toTimePhpToJava(String str) {
        str = str + "000";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String sd = sdf.format(new Date(Long.parseLong(str)));
        return sd;
    }

    /**
     * @return
     */
    public static String getTimeYMDHMS() {
        SimpleDateFormat sdf = new SimpleDateFormat(DAY_FORMAT);
        String sd = sdf.format(new Date(System.currentTimeMillis()));
        return sd;
    }

    /**
     * PHP时间转换
     *
     * @param str
     * @param format
     * @return
     */
    public static String getTimeFormat(long str, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String sd = sdf.format(new Date(str));
        return sd;
    }

    /**
     * PHP时间转换
     *
     * @param str
     * @param format
     * @return
     */
    public static String toTimePhpToJava(String str, String format) {
        str = str + "000";
        if (format == null || format.length() == 0) {
            return str;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String sd = sdf.format(new Date(Long.parseLong(str)));
        return sd;
    }

    /**
     * 判断两个时间戳是否在同一天
     *
     * @param time1
     * @param time2
     * @return
     */
    public static boolean isTheSameDate(long time1, long time2) {
        if (time1 != 0 && time2 != 0) {
            Calendar c1 = Calendar.getInstance();
            c1.setTimeInMillis(time1);
            int y1 = c1.get(Calendar.YEAR);
            int m1 = c1.get(Calendar.MONTH);
            int d1 = c1.get(Calendar.DATE);
            Calendar c2 = Calendar.getInstance();
            c2.setTimeInMillis(time2);
            int y2 = c2.get(Calendar.YEAR);
            int m2 = c2.get(Calendar.MONTH);
            int d2 = c2.get(Calendar.DATE);
            if (y1 == y2 && m1 == m2 && d1 == d2) {
                return true;
            }
        } else {
            if (time1 == 0 && time2 == 0) {
                return true;
            }
        }
        return false;
    }

}
