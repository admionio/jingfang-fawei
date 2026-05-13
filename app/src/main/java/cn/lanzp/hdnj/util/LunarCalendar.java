package cn.lanzp.hdnj.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 农历、黄帝纪年和时辰计算工具
 */
public class LunarCalendar {

    private static final int[] LUNAR_INFO = {
            0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2, //1900-1909
            0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, //1910-1919
            0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, //1920-1929
            0x06566, 0x0d4a0, 0x0ea50, 0x16a95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, //1930-1939
            0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, //1940-1949
            0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0, //1950-1959
            0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, //1960-1969
            0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6, //1970-1979
            0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, //1980-1989
            0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x05ac0, 0x0ab60, 0x096d5, 0x092e0, //1990-1999
            0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, //2000-2009
            0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, //2010-2019
            0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, //2020-2029
            0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, //2030-2039
            0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0, //2040-2049
            0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06aa0, 0x1a6c4, 0x0aae0, //2050-2059
            0x092e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4, //2060-2069
            0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0, //2070-2079
            0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160, //2080-2089
            0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a4d0, 0x0d150, 0x0f252, //2090-2099
            0x0d520 //2100
    };

    private static final String[] TIAN_GAN = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
    private static final String[] DI_ZHI = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
    private static final String[] SHENG_XIAO = {"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
    private static final String[] LUNAR_MONTH_NAMES = {"正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"};
    private static final String[] SHICHEN_NAMES = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
    private static final String[] SHICHEN_RANGES = {
            "23:00-00:59", "01:00-02:59", "03:00-04:59", "05:00-06:59",
            "07:00-08:59", "09:00-10:59", "11:00-12:59", "13:00-14:59",
            "15:00-16:59", "17:00-18:59", "19:00-20:59", "21:00-22:59"
    };

    private static final String[] LUNAR_DAY_NAMES = {
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };

    /**
     * 农历日期信息
     */
    public static class LunarDate {
        public int year;         // 农历年
        public int month;        // 农历月 (1-12)
        public int day;          // 农历日 (1-30)
        public boolean isLeap;   // 是否闰月
        public String monthName;  // 月份中文名
        public String dayName;    // 日期中文名
        public String ganzhiYear; // 干支纪年
        public String shengxiao;  // 生肖
    }

    /**
     * 时辰信息
     */
    public static class ShichenInfo {
        public String name;       // 时辰名（如"子"）
        public String range;      // 时间范围（如"23:00-00:59"）
    }

    /**
     * 获取指定公历日期的农历信息
     */
    public static LunarDate getLunarDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return solarToLunar(year, month, day);
    }

    /**
     * 获取当前时辰
     */
    public static ShichenInfo getShichen(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        int index;
        if (hour == 23 || hour == 0) {
            index = 0;  // 子时
        } else {
            index = (hour + 1) / 2;
        }

        ShichenInfo info = new ShichenInfo();
        info.name = SHICHEN_NAMES[index];
        info.range = SHICHEN_RANGES[index];
        return info;
    }

    /**
     * 获取黄帝纪年
     * 黄帝纪元起始于公元前2697年（甲子年）
     */
    public static int getYellowEmperorYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int gregorianYear = cal.get(Calendar.YEAR);
        return gregorianYear + 2697;
    }

    /**
     * 获取黄帝纪年的干支年
     */
    public static String getYellowEmperorGanzhi(int yellowEmperorYear) {
        int ganIndex = (yellowEmperorYear - 1) % 10;
        int zhiIndex = (yellowEmperorYear - 1) % 12;
        return TIAN_GAN[ganIndex] + DI_ZHI[zhiIndex];
    }

    /**
     * 获取格式化的日期信息字符串（用于首页展示）
     */
    public static String getFormattedDateInfo(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        String solarDate = dateFormat.format(date);

        LunarDate lunar = getLunarDate(date);
        String lunarDate = "农历" + lunar.monthName + "月" + lunar.dayName;

        int yellowEmperorYear = getYellowEmperorYear(date);
        String yellowEmperor = "黄帝纪元" + yellowEmperorYear + "年";

        return solarDate + "  " + lunarDate + "  " + yellowEmperor;
    }

    /**
     * 公历转农历
     */
    private static LunarDate solarToLunar(int year, int month, int day) {
        LunarDate result = new LunarDate();

        // 基准日期：1900年1月31日 = 农历庚子年正月初一
        Calendar baseCal = Calendar.getInstance();
        baseCal.set(1900, Calendar.JANUARY, 31);

        Calendar targetCal = Calendar.getInstance();
        targetCal.set(year, month - 1, day);

        long offsetDays = (targetCal.getTimeInMillis() - baseCal.getTimeInMillis()) / (1000 * 60 * 60 * 24);

        // 计算目标年份
        int lunarYear;
        int yearDays;
        int leapMonth;
        int leapMonthDays;

        for (lunarYear = 1900; lunarYear < 2101 && offsetDays > 0; lunarYear++) {
            yearDays = getLunarYearDays(lunarYear);
            if (offsetDays < yearDays) break;
            offsetDays -= yearDays;
        }

        if (lunarYear > 2100) {
            lunarYear = 2100;
        }

        result.year = lunarYear;

        // 计算干支和生肖
        int ganIndex = (lunarYear - 4) % 10;
        int zhiIndex = (lunarYear - 4) % 12;
        if (ganIndex < 0) ganIndex += 10;
        if (zhiIndex < 0) zhiIndex += 12;
        result.ganzhiYear = TIAN_GAN[ganIndex] + DI_ZHI[zhiIndex];
        result.shengxiao = SHENG_XIAO[zhiIndex];

        // 获取该年的闰月信息
        leapMonth = getLeapMonth(lunarYear);
        result.isLeap = false;

        // 计算月份和日期
        int currentMonth;
        for (currentMonth = 1; currentMonth <= 12 && offsetDays >= 0; currentMonth++) {
            int monthDays;

            if (leapMonth > 0 && currentMonth == leapMonth + 1) {
                // 先处理闰月
                if (result.isLeap) {
                    // 已经处理过闰月，走正常月份
                    monthDays = getLunarMonthDays(lunarYear, currentMonth - 1);
                } else {
                    monthDays = getLunarMonthDays(lunarYear, currentMonth - 1, true);
                    if (offsetDays < monthDays) {
                        result.isLeap = true;
                        result.month = currentMonth - 1;
                        result.day = (int) offsetDays + 1;
                        break;
                    }
                    offsetDays -= monthDays;
                    // 然后再处理正常月份
                    monthDays = getLunarMonthDays(lunarYear, currentMonth - 1);
                    if (offsetDays < monthDays) {
                        result.month = currentMonth - 1;
                        result.day = (int) offsetDays + 1;
                        break;
                    }
                    offsetDays -= monthDays;
                    continue;
                }
            } else {
                monthDays = getLunarMonthDays(lunarYear, currentMonth - 1);
            }

            if (offsetDays < monthDays) {
                result.month = currentMonth;
                result.day = (int) offsetDays + 1;
                break;
            }
            offsetDays -= monthDays;
        }

        // 设置中文名称
        result.monthName = LUNAR_MONTH_NAMES[result.month - 1];
        result.dayName = LUNAR_DAY_NAMES[result.day - 1];

        return result;
    }

    /**
     * 获取农历年总天数
     */
    private static int getLunarYearDays(int year) {
        int sum = 348; // 12个月 × 29天
        int info = LUNAR_INFO[year - 1900];

        for (int i = 0x8000; i > 0x8; i >>= 1) {
            sum += ((info & i) != 0) ? 1 : 0;
        }
        sum += getLeapMonthDays(year);
        return sum;
    }

    /**
     * 获取闰月天数
     */
    private static int getLeapMonthDays(int year) {
        if (getLeapMonth(year) == 0) return 0;
        int info = LUNAR_INFO[year - 1900];
        return (info & 0x10000) != 0 ? 30 : 29;
    }

    /**
     * 获取闰月月份（1-12, 0表示无闰月）
     */
    private static int getLeapMonth(int year) {
        int info = LUNAR_INFO[year - 1900];
        return info & 0xf;
    }

    /**
     * 获取农历某月天数（不考虑闰月）
     */
    private static int getLunarMonthDays(int year, int month) {
        int info = LUNAR_INFO[year - 1900];
        return (info & (0x10000 >> month)) != 0 ? 30 : 29;
    }

    /**
     * 获取农历某月天数（指定是否为闰月）
     */
    private static int getLunarMonthDays(int year, int month, boolean isLeap) {
        if (isLeap) {
            return getLeapMonthDays(year);
        }
        return getLunarMonthDays(year, month);
    }
}
