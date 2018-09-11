/**
 * chenxitech.cn Inc. Copyright (c) 2017-2018 All Rights Reserved.
 */
package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author tangyue
 * @version $Id: DateUtil.java, v 0.1 2018-07-23 16:36 tangyue Exp $$
 */
public final class DateUtil {

    private DateUtil(){

    }

    /**
     * 当前时间格式化
     * @param pattern 时间格式
     * @return
     */
    public static String formatDate(String pattern){

        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }
}
