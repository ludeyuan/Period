package period.ldy.module;

/**
 * Created by ludeyuan on 16/7/27.
 * 日期的数据
 */
public class DateCardModule {
    public int date;            //日期（一月中的第几天）
    public int month;           //月份
    public int year;            //年份
    public boolean isToday;
    public PeriodType type; //显示状态：1为月经期，2为预测期，3为安全期，4为易孕期, 0为其他
    public int isStart;//1表示开始，2表示结束，0表示其他
    public boolean istoMonth;//是否为当月的日期
    public boolean mIsClick = false;    //是否被点击了
    public DateCardModule(int date,int month,int year, boolean isToday, PeriodType type, int isStart, boolean istoMonth) {
        this.date = date;
        this.month = month;
        this.year = year;
        this.isToday = isToday;
        this.type = type;
        this.isStart = isStart;
        this.istoMonth = istoMonth;
    }
}
