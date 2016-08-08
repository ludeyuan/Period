package period.ldy.module;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by ludeyuan on 16/7/27.
 * 日历(显示安全、预测、安全和排卵期)
 */
public class DatePeriodView extends GridLayout {

    private final int COLUM_NUM = 7;//一共有7列
    private final int TOTAL_NUM = 7 * 6;  //GridView中的最大数量
    private final long DAY_COUNT = 24 * 60 * 60 * 1000;//一天的时间

    private Calendar mCalendar;     //计算器
    private Date mTodayDate;        //今天
    private Date mCurDate;          //当前的日期

    private int[] mDateNum = new int[TOTAL_NUM]; // 日历显示数字
    private DateCardView[] mDateCard = new DateCardView[TOTAL_NUM];
    private DateCardModule[] mDateCardList = new DateCardModule[TOTAL_NUM];
    private int mLastMonthDays, mCurMonthDays;   //上个月、这个月的天数
    private MenstruationCalculate mMenstruationCalculate;
    private DateClickListener mDateClickListener;

    private boolean mNeedInitCards = true;//需要初始化日期
    private MenstruationModel mInitMenstruationModel;

    public DatePeriodView(Context context) {
        this(context, null);
    }

    public DatePeriodView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DatePeriodView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    /**
     * 进行常规的设置
     */
    private void initView() {
        setColumnCount(COLUM_NUM);//一共显示7列
        mCalendar = Calendar.getInstance();
        mTodayDate = new Date();
        mCurDate = new Date();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ;
        if (mNeedInitCards) {
            mNeedInitCards = false;
            int cardSize = w / COLUM_NUM;
            addCards(cardSize);
        }
    }

    /**
     * 添加日期
     *
     * @param cardSize 日期布局的大小（正方形）
     */
    private void addCards(int cardSize) {
        DateCardView dateCardView;
        int dayCount = 0;
        for (int index = 0; index < TOTAL_NUM / COLUM_NUM; index++) {
            for (int cloum = 0; cloum < COLUM_NUM; cloum++) {
                dateCardView = new DateCardView(getContext());
                dateCardView.initData(mDateCardList[dayCount]);
                addView(dateCardView, cardSize, cardSize);
                mDateCard[dayCount] = dateCardView;
                dayCount++;
            }
        }
        setListener();
    }

    public void initPeriodData(MenstruationModel menstruationModel,DateClickListener dateClickListener) {
        mDateClickListener=dateClickListener;
        mInitMenstruationModel = menstruationModel;
        calculateDate();
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        long nowDate = DateChange.dateTimeStamp(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-1","yyyy-MM-dd");
        long nextDate = DateChange.dateTimeStamp(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+2)+"-1","yyyy-MM-dd");

        mMenstruationCalculate = new MenstruationCalculate();
        ArrayList<MenstruationModel> list = mMenstruationCalculate.calculateMt(menstruationModel,nowDate,nextDate);
        list.add(menstruationModel);
        caculateType(list);
    }

    private void setListener(){
        //设置点击事件，上个月和下个月不能显示
        for(int i=0;i<=mLastMonthDays;i++){
            mDateCard[i].setOnClickListener(null);
        }
        for(int i=mLastMonthDays+1;i<=mLastMonthDays+mCurMonthDays;i++){
            final int position = i;
            mDateCard[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    for(int j=mLastMonthDays+1;j<=mLastMonthDays+mCurMonthDays;j++){
                        //把选中的日期的背景设置成带红色的框，没有变成白色，并把当前的日期返回
                        if(position==j){
                            //点击的日期是当前的日期
                            mDateCardList[j].mIsClick = true;
                            dealClickDateCard(mDateCardList[position]);
                        }else{
                            mDateCardList[j].mIsClick = false;
                        }
                        mDateCard[j].onClick(mDateCardList[j].mIsClick);
                    }
                }
            });
        }
    }

    /**
     * 处理点击后的日期
     */
    private void dealClickDateCard(DateCardModule dateCardModule){
        long clickDay = getNowDate(dateCardModule.date);
        //如果选中的日期超过了当前的日期，
        if(null!=mDateClickListener){
            long today = DateChange.getDate();
            //小于今天
            if(today<clickDay){
                mDateClickListener.clickDayAfterToday(dateCardModule.isStart==1,dateCardModule.isStart==2);
            }else if(today == clickDay){
                mDateClickListener.clickDayEqualsToday(dateCardModule.isStart==1,dateCardModule.isStart==2);
            }else{
                mDateClickListener.clickDayBeforeToday(dateCardModule.isStart==1,dateCardModule.isStart==2);
            }
        }
    }

    /**
     * 计算日期
     */
    private void calculateDate() {
        mCalendar.setTime(mTodayDate);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int dayInWeek = mCalendar.get(Calendar.DAY_OF_WEEK);//
        int monthStart = dayInWeek;
        if (monthStart == 1) {
            monthStart = 8;
        }
        monthStart -= 1;  //以日为开头-1，以星期一为开头-2

        //月初1号
        if (getNowTime("yyyy") == mCalendar.get(Calendar.YEAR) &&
                getNowTime("MM") == mCalendar.get(Calendar.MONTH) + 1 &&
                getNowTime("dd") == 1) {
            mDateCardList[monthStart] = new DateCardModule(1, true, PeriodType.TYPE_SAFE, 0, true);
        } else {
            mDateCardList[monthStart] = new DateCardModule(1, false, PeriodType.TYPE_SAFE, 0, true);
        }
        mDateNum[monthStart] = 1;

        //上个月(当前月份，显示上个月)
        if (monthStart > 0) {
            mCalendar.set(Calendar.DAY_OF_MONTH, 0);
            int dayInMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
            mLastMonthDays = monthStart - 1;

            for (int i = monthStart - 1; i >= 0; i--) {
                mDateCardList[i] = new DateCardModule(dayInMonth, false, PeriodType.TYPE_SAFE, 0, false);
                mDateNum[i] = dayInMonth;
                dayInMonth--;
            }
            mCalendar.set(Calendar.DAY_OF_MONTH, mDateNum[0]);
        }

        //本月
        mCalendar.setTime(mTodayDate);
        mCalendar.add(Calendar.MONTH, 1);
        mCalendar.set(Calendar.DAY_OF_MONTH, 0);
        int monthDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mCurMonthDays = monthDay;
        for (int i = 1; i < mCurMonthDays; i++) {
            mDateNum[i] = i + 1;
            mCalendar.setTime(mTodayDate);
            if (getNowTime("yyyy") == mCalendar.get(Calendar.YEAR) &&
                    getNowTime("MM") == mCalendar.get(Calendar.MONTH) + 1 &&
                    getNowTime("dd") == (i + 1)) {
                //日期为今天
                mDateCardList[monthStart + i] = new DateCardModule(i + 1, true, PeriodType.TYPE_SAFE, 0, true);
            } else {
                mDateCardList[monthStart + i] = new DateCardModule(i + 1, false, PeriodType.TYPE_SAFE, 0, true);
            }
        }

        //下个月
        for (int i = monthStart + monthDay; i < TOTAL_NUM; i++) {
            mDateCardList[i] = new DateCardModule(i - (monthStart + monthDay) + 1, false, PeriodType.TYPE_SAFE, 0, false);
            mDateNum[i] = i - (monthStart + monthDay) + 1;
        }
    }

    private void caculateType(List<MenstruationModel> mtmList) {
        if(mtmList==null){
            return;
        }
        for (int i = mLastMonthDays + 1; i <= mLastMonthDays + mCurMonthDays; i++) {
            if (isMenOrCal(mtmList, i)!=-1) {
                //处于月经期或者安全期
                int position = isMenOrCal(mtmList, i);
                if (mtmList.get(position).getBeginTime() > DateChange.dateTimeStamp(getYMD("yyyy-MM-dd"), "yyyy-MM-dd")) {
                    mDateCardList[i].type = PeriodType.TYPE_CALCULATE;//预测期
                } else if (mtmList.get(position).getEndTime() <= DateChange.dateTimeStamp(getYMD("yyyy-MM-dd"), "yyyy-MM-dd")) {
                    mDateCardList[i].type = PeriodType.TYPE_MENSTRUATION;//经期
                    if(isStart(mtmList.get(position),i)){
                        mDateCardList[i].isStart = 1;
                    }else if(isEnd(mtmList.get(position),i)){
                        mDateCardList[i].isStart = 2;
                    }else{
                        mDateCardList[i].isStart = 3;
                    }
                }else if(getNowDate(mDateCardList[i].date) == mtmList.get(position).getBeginTime()){
                    if(mtmList.get(position).isCon()){
                        mDateCardList[i].type = PeriodType.TYPE_MENSTRUATION;
                        if(isStart(mtmList.get(position), i)){
                            mDateCardList[i].isStart = 1;
                        }else if(isEnd(mtmList.get(position), i)) {
                            mDateCardList[i].isStart = 2;
                        }else {
                            mDateCardList[i].isStart = 3;
                        }
                    }else {
                        mDateCardList[i].type = PeriodType.TYPE_MENSTRUATION;
                    }
                }else {
                    if(getNowDate(mDateCardList[i].date) > mtmList.get(position).getBeginTime() &&
                            getNowDate(mDateCardList[i].date)<=DateChange.dateTimeStamp(getYMD("yyyy-MM-dd"), "yyyy-MM-dd")){
                        mDateCardList[i].type = PeriodType.TYPE_MENSTRUATION;
                        if(isStart(mtmList.get(position), i)){
                            mDateCardList[i].isStart = 1;
                        }else if(isEnd(mtmList.get(position), i)) {
                            mDateCardList[i].isStart = 2;
                        }else {
                            mDateCardList[i].isStart = 3;
                        }
                    }else {
                        mDateCardList[i].type = PeriodType.TYPE_CALCULATE;
                    }
                }
            }else if(isSafeDay(mtmList, i)){
                mDateCardList[i].type = PeriodType.TYPE_SAFE;
            }else {
                mDateCardList[i].type = PeriodType.TYPE_DANGEROUS;
            }
        }
    }

    public String clickToady(){
        mCalendar.setTime(mTodayDate);
        mCalendar.add(Calendar.MONTH, getNowTime("yyyy")*12 + getNowTime("MM") - (mCalendar.get(Calendar.MONTH)+1)-mCalendar.get(Calendar.YEAR)*12);
        mCurDate = mCalendar.getTime();
        long nowDate = DateChange.dateTimeStamp(mCalendar.get(Calendar.YEAR)+"-"+(mCalendar.get(Calendar.MONTH)+1)+"-1","yyyy-MM-dd");
        long nextDate = DateChange.dateTimeStamp(mCalendar.get(Calendar.YEAR)+"-"+(mCalendar.get(Calendar.MONTH)+2)+"-1","yyyy-MM-dd");
        List<MenstruationModel> lists = mMenstruationCalculate.calculateMt(mInitMenstruationModel,nowDate,nextDate);
        return recurToday(lists);
    }

    /**
     * 跳转到上个月
     * @return
     */
    public String clickLastMonth(){
        mCalendar.setTime(mCurDate);
        mCalendar.add(Calendar.MONTH,-1);
        mCurDate = mCalendar.getTime();
        long nowDate = DateChange.dateTimeStamp(mCalendar.get(Calendar.YEAR)+"-"+(mCalendar.get(Calendar.MONTH)+1)+"-1","yyyy-MM-dd");
        long nextDate = DateChange.dateTimeStamp(mCalendar.get(Calendar.YEAR)+"-"+(mCalendar.get(Calendar.MONTH)+2)+"-1","yyyy-MM-dd");
        List<MenstruationModel> lists = mMenstruationCalculate.calculateMt(mInitMenstruationModel,nowDate,nextDate);
        return clickLeftMonth(lists);
    }

    public String clickNextMonth(){
        mCalendar.setTime(mCurDate);
        mCalendar.add(Calendar.MONTH,+1);
        mCurDate = mCalendar.getTime();
        long nowDate = DateChange.dateTimeStamp(mCalendar.get(Calendar.YEAR)+"-"+(mCalendar.get(Calendar.MONTH)+1)+"-1","yyyy-MM-dd");
        long nextDate = DateChange.dateTimeStamp(mCalendar.get(Calendar.YEAR)+"-"+(mCalendar.get(Calendar.MONTH)+2)+"-1","yyyy-MM-dd");
        List<MenstruationModel> lists = mMenstruationCalculate.calculateMt(mInitMenstruationModel,nowDate,nextDate);
        return clickRightMonth(lists);
    }

    /**
     * 向上一月
     * @return
     */
    private String clickLeftMonth(List<MenstruationModel> mtmList){
        mCalendar.setTime(mTodayDate);
        mCalendar.add(Calendar.MONTH, -1);
        mTodayDate = mCalendar.getTime();
        calculateDate();
        caculateType(mtmList);
        for(int i=0; i<TOTAL_NUM; i++){
            mDateCard[i].initData(mDateCardList[i]);
        }
        setListener();
        return getYearAndmonth();
    }

    /**
     * 向下一月
     * @return
     */
    private String clickRightMonth(List<MenstruationModel> mtmList){
        mCalendar.setTime(mTodayDate);
        mCalendar.add(Calendar.MONTH, 1);
        mTodayDate = mCalendar.getTime();
        calculateDate();
        caculateType(mtmList);
        for(int i=0; i<TOTAL_NUM; i++){
            mDateCard[i].initData(mDateCardList[i]);
        }
        setListener();
        return getYearAndmonth();
    }

    /**
     * 回今天
     * @return
     */
    public String recurToday(List<MenstruationModel> mtmList){
        mCalendar.setTime(mTodayDate);
        mCalendar.add(Calendar.MONTH, getNowTime("yyyy")*12 + getNowTime("MM") - (mCalendar.get(Calendar.MONTH)+1)-mCalendar.get(Calendar.YEAR)*12);
        mTodayDate = mCalendar.getTime();
        calculateDate();
        caculateType(mtmList);
        int position = 1;
        for(int i=0; i<TOTAL_NUM; i++){
            mDateCard[i].initData(mDateCardList[i]);
        }
        setListener();
        return getYearAndmonth();
    }

    /**
     *  获得当前应该显示的年月
     * @return
     */
    public String getYearAndmonth() {
        mCalendar.setTime(mTodayDate);
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH)+1;
        return year + "年" + month + "月";
    }

    /**
     * 获取当天日期
     *
     * @param format
     * @return
     */
    public int getNowTime(String format) {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);// 可以方便地修改日期格式
        String nowTimeInt = dateFormat.format(now);
        return Integer.parseInt(nowTimeInt);
    }

    private long getNowDate(int d) {
        mCalendar.setTime(mTodayDate);
        String date = mCalendar.get(Calendar.YEAR) + "-" + (mCalendar.get(Calendar.MONTH) + 1) + "-" + d;
        long dealTime = DateChange.dateTimeStamp(date, "yyyy-MM-dd");
        return dealTime;
    }

    /**
     * 获取当天日期
     *
     * @param format
     * @return
     */
    public String getYMD(String format) {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);// 可以方便地修改日期格式
        String hehe = dateFormat.format(now);
        return hehe;
    }

    /**
     * 处于经期或者排卵期
     *
     * @return
     */
    private int isMenOrCal(List<MenstruationModel> mtmList, int position) {
        for(int i=0;i<mtmList.size();i++){
            if (getNowDate(mDateCardList[position].date) >= mtmList.get(i).getBeginTime() &&
                    getNowDate(mDateCardList[position].date) <= mtmList.get(i).getEndTime()) {
                return i;
            }
        }
        return -1;

    }

    /**
     * @return true处于安全期，false处于危险期
     */
    private boolean isSafeDay(List<MenstruationModel> mtmList, int position) {
        for(int i=0;i<mtmList.size();i++){
            long curTime = getNowDate(mDateCardList[position].date);
            if ((curTime < mtmList.get(i).getBeginTime() && curTime >= mtmList.get(i).getBeginTime() - 7 * DAY_COUNT)
                    || (curTime > mtmList.get(i).getEndTime() && curTime <= mtmList.get(i).getEndTime() + 8 * DAY_COUNT)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStart(MenstruationModel menstruationModel, int position) {
        if (getNowDate(mDateCardList[position].date) == menstruationModel.getBeginTime()) {
            return true;
        }
        return false;
    }

    private boolean isEnd(MenstruationModel menstruationModel, int position) {
        Log.i("DatePeriod", "结束的时间＝"+DateChange.timeStamp2Date(menstruationModel.getEndTime()+"",null));
        if (getNowDate(mDateCardList[position].date) == menstruationModel.getEndTime()) {
            return true;
        }
        return false;
    }
}
