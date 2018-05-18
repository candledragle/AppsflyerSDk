package com.appsflyer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by maximkleb on 1/28/18.
 */

class AFDateFormat {

    static String dateFormatUTC(SimpleDateFormat dateFormat, long time) {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date(time));
    }

    static SimpleDateFormat getDataFormatter(String pattern) {
        return new SimpleDateFormat(pattern, Locale.US);
    }


}
