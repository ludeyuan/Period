package period.ldy.module;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2015/5/21.
 */
public class DateChange {
    /**
     * 时间戳转换成日期格式字符
     *
     * @param seconds 精确到秒的字符串
     */
    public static String timeStamp2Date(String seconds,String format) {
        if(seconds == null || seconds.isEmpty() || seconds.equals("null")){
            return "";
        }

        if(format == null || format.isEmpty()) format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds)));
    }

    /**
     * 日期格式字符串转换成时间
     * @param format 如：yyyy-MM-dd HH:mm:ss
     */
    public static String date2TimeStamp(String date_str,String format){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(date_str).getTime()/1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    /**
     * 日期格式字符串转换成时间
     * @param format 如：yyyy-MM-dd HH:mm:ss
     */
    public static long dateTimeStamp(String date_str,String format){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(date_str).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取当天日期时间
     * @return
     */
    public static Long getDate(){
    	int y,m,d;    
    	Calendar cal=Calendar.getInstance();    
    	y=cal.get(Calendar.YEAR);    
    	m=cal.get(Calendar.MONTH);    
    	d=cal.get(Calendar.DATE);    
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	long l = 0;
		try {
			l = sdf.parse(y+"-"+(m+1)+"-"+d).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
        return l;
    }
    
    /**
     * 取得当前时间戳（精确到秒
     */
    public static String timeStamp(){
        long time = System.currentTimeMillis();
        String t = String.valueOf(time/1000);
        return t;
    }
    /**
     * 计算两个日期相差天数
     * @return **周**天
     */
    public static String getTime(long endTime, long startTime){
    	int M = (int) ((endTime-startTime)/86400000l/7);
    	int D = (int) ((endTime-startTime)/86400000l%7);
    	if(M != 0 && D != 0){
    		return M+"周"+D+"天";
    	}else if (M == 0 && D != 0) {
    		return D+"天";
		}else if (M != 0 && D == 0) {
			return M+"周";
		}
    	return "1天";
    }
    //  输出结果
    //  timeStamp=1417792627
    //  date=2014-12-05 23:17:07
    //  1417792627
    public static void main(String[] args) {
        String timeStamp = timeStamp();
        System.out.println("timeStamp="+timeStamp);

        String date = timeStamp2Date(timeStamp, "yyyy-MM-dd HH:mm:ss");
        System.out.println("date="+date);

        String timeStamp2 = date2TimeStamp(date, "yyyy-MM-dd HH:mm:ss");
        System.out.println(timeStamp2);
    }
}
