package com.anthonyfu.web.constant;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CommonConstant {

	// CSV 來源時間格式為 "yyyy-M-d HH:mm"（月、日不補零，無秒數），例如 2019-9-20 07:42
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");
	
    public static final String YEAR_15 = "15";
    public static final String YEAR_16 = "16";
    public static final String YEAR_17 = "17";
    public static final String YEAR_18 = "18";
    public static final String YEAR_19 = "19";
    public static final String YEAR_20 = "20";
    public static final String YEAR_21 = "21";
    public static final String YEAR_22 = "22";
    public static final String YEAR_23 = "23";
    public static final String YEAR_24 = "24";
    public static final String YEAR_25 = "25";
    public static final String YEAR_26 = "26";

    // 日期型欄位
    public static final List<String> YEAR_LIST = new ArrayList<String>() {{
//        add(YEAR_15);
//        add(YEAR_16);
//        add(YEAR_17);
//        add(YEAR_18);
//        add(YEAR_19);
//        add(YEAR_20);
//        add(YEAR_21);

        add(YEAR_22);
        add(YEAR_23);
        add(YEAR_24);
        add(YEAR_25);
        add(YEAR_26);
    }};
}
