package period.ldy.module;

import android.content.Context;
import android.util.AttributeSet;
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
    private MenstruationModel mInitMenstruationModel;//一开始计算的日期

    //推算的部分
    private DateCardModule mCurrentDateModule = null;//当前点击的卡片
    private DateCardModule mBenginData = null; //经期开始的日期
    private DateCardModule mEndDateData = null;    //经期结束的日期

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

    public void initPeriodData(MenstruationModel menstruationModel, DateClickListener dateClickListener) {
        mDateClickListener = dateClickListener;
        mInitMenstruationModel = menstruationModel;
        calculateDate();
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        long nowDate = DateChange.dateTimeStamp(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-1", "yyyy-MM-dd");
        long nextDate = DateChange.dateTimeStamp(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 2) + "-1", "yyyy-MM-dd");

        mMenstruationCalculate = new MenstruationCalculate();
        ArrayList<MenstruationModel> list = mMenstruationCalculate.calculateMt(menstruationModel, nowDate, nextDate);
        list.add(menstruationModel);
        caculateType(list);
    }

    /**
     * 点击开始
     *
     * @param startOpen true:设置成开始日期  false:当前日期关闭
     * @return true:操作成功 false操作失败
     */
    public boolean clickStart(boolean startOpen) {
        if (null == mCurrentDateModule) {
            //当前没有选中日期
            return false;
        }

        /*
        * 1、没有经期列表
        * 1-1、没有经期结束时间，直接把当前时间设置成经期开始时间；
        * 1-2、有经期结束时间
        * 1-2-1、开始时间小于结束时间，且经期持续时间小于经期长度，设置开始时间并进行推算；
        * 1-2-2、开时间等于结束时间，把结束时间设置成空，并设置开始时间；
        * 1-2-3、开始时间大于结束时间，不可以；
        * 1-2-4、经期的持续时间大于周期长度，不可以
        * 2、有经期列表:遍历集合，取出一组或者两组数据（规则：如果当前的时间包含在其中一组内，直接返回；
        * 否则返回前后的两次时间；如果当前的时间在最前面或最后面，也只返回一次）
        * 2-1、只有一组
        * 2-1-1、如果时间在区间内（不包含区间），重新计算，并把经期开始和结束设置成空；
        * 2-1-2、在区间的开始或结束、大于结束日期、结束时间减去开始时间大于周期长度，把开始的时间设置成当前日期；
        * 2-1-3、计算周期；
        * 2-2、有两组数据（第一组的时间小于第二组）
        * 2-2-1、距离第二组的开始时间小于7，且距离第二组的结束时间小于周期长度，重新计算
        * 2-2-2、其他情况，直接设置开始时间，清空经期列表
        * */

        List<MenstruationModel> menstruationModels = DatePeriodHelper.getSideList(
                getMenstruationModleLists(),getNowDate(mCurrentDateModule.date));
        if (null == menstruationModels || menstruationModels.size() == 0) {
            if (mEndDateData == null) {
                mBenginData = mCurrentDateModule;
                refreshUI();
                return true;
            } else {
                //获取经期开始和结束的时间间隔
                int distanceDay = DateChange.getDistanceDay(mCurrentDateModule, mEndDateData);
                if (distanceDay > 0 && distanceDay < mInitMenstruationModel.getCycle()) {
                    reCalculatePeriod(DateChange.dateTimeStamp(mCurrentDateModule),
                            DateChange.dateTimeStamp(mEndDateData));
                    return true;
                } else {
                    mBenginData = mCurrentDateModule;
                    mEndDateData = null;
                    refreshUI();
                    return true;
                }
            }
        } else {
            MenstruationModel saveMenstruationModel = null;
            if (menstruationModels.size() == 1) {
                saveMenstruationModel = menstruationModels.get(0);
            } else {
                saveMenstruationModel = menstruationModels.get(1);
            }

            long curdate = DateChange.dateTimeStamp(mCurrentDateModule);
            if (curdate > saveMenstruationModel.getBeginTime() && curdate < saveMenstruationModel.getEndTime()) {
                reCalculatePeriod(curdate, saveMenstruationModel.getEndTime());
                return true;
            } else if (curdate == saveMenstruationModel.getBeginTime()) {
                mEndDateData = DateChange.time2DateCardModule(saveMenstruationModel.getEndTime(), false);
                mBenginData = null;
                refreshUI();
                return false;
            } else if (curdate < saveMenstruationModel.getBeginTime() &&
                    (saveMenstruationModel.getEndTime() - curdate) <= DateConstants.WEEK_TIME) {
                reCalculatePeriod(curdate, saveMenstruationModel.getEndTime());
                return true;
            } else {
                mBenginData = mCurrentDateModule;
                mEndDateData = null;
                refreshUI();
                return true;
            }

        }

    }

    /**
     * 点击结束
     *
     * @param endOpen true:设置成结束日期  false：当前日期关闭
     * @return true:操作成功 false操作失败
     */
    public boolean clickEnd(boolean endOpen) {
        if (null == mCurrentDateModule) {
            //当前没有选中日期
            return false;
        }

        /*
        * 1、没有经期列表
        * 1-1、没有经期开始时间，直接把当前时间设置成经期结束时间；
        * 1-2、有经期开始时间
        * 1-2-1、结束时间小于开始时间，且经期持续时间小于经期长度，设置结束时间并进行推算；
        * 1-2-2、结束时间等于当前时间，把开始时间设置成空，并设置结束时间；
        * 1-2-3、开始时间大于结束时间，不可以；
        * 1-2-4、经期的持续时间大于周期长度，不可以
        * 2、有经期列表:遍历集合，取出一组或者两组数据（规则：如果当前的时间包含在其中一组内，直接返回；
        * 否则返回前后的两次时间；如果当前的时间在最前面或最后面，也只返回一次）
        * 2-1、只有一组
        * 2-1-1、如果时间在区间内（不包含区间），重新计算，并把经期开始和结束设置成空；
        * 2-1-2、在区间的开始或结束、大于结束日期、结束时间减去开始时间大于周期长度，把结束时间设置成当前日期；
        * 2-1-3、计算周期；
        * 2-2、有两组数据（第一组的时间小于第二组）
        * 2-2-1、距离第一组的结束时间小于8且距离第一组的时间小于周期长度，重新计算；
        * 2-2-2、其他情况，直接设置开始时间，清空经期列表
        * */

        List<MenstruationModel> menstruationModels = DatePeriodHelper.getSideList(
                getMenstruationModleLists(),getNowDate(mCurrentDateModule.date));
        if(null == menstruationModels || menstruationModels.size()==0){
            if (mBenginData == null) {
                mEndDateData = mCurrentDateModule;
                refreshUI();
                return true;
            } else {
                //获取经期开始和结束的时间间隔
                int distanceDay = DateChange.getDistanceDay(mBenginData,mCurrentDateModule);
                if (distanceDay > 0 && distanceDay < mInitMenstruationModel.getCycle()) {
                    reCalculatePeriod(DateChange.dateTimeStamp(mBenginData),DateChange.dateTimeStamp(mCurrentDateModule));
                    return true;
                } else {
                    mEndDateData = mCurrentDateModule;
                    mBenginData = null;
                    refreshUI();
                    return true;
                }
            }
        }else {
            MenstruationModel saveMenstruationModel = null;
            if (menstruationModels.size() == 1) {
                saveMenstruationModel = menstruationModels.get(0);
            } else {
                saveMenstruationModel = menstruationModels.get(0);
            }

            long curdate = DateChange.dateTimeStamp(mCurrentDateModule);
            if (curdate > saveMenstruationModel.getBeginTime() && curdate < saveMenstruationModel.getEndTime()) {
                reCalculatePeriod(saveMenstruationModel.getBeginTime(),curdate);
                return true;
            } else if (curdate == saveMenstruationModel.getEndTime()) {
                mBenginData = DateChange.time2DateCardModule(saveMenstruationModel.getBeginTime(), false);
                mEndDateData = null;
                refreshUI();
                return false;
            } else if (curdate > saveMenstruationModel.getEndTime() &&
                    (curdate - saveMenstruationModel.getBeginTime()) <= DateConstants.WEEK_TIME) {
                reCalculatePeriod(saveMenstruationModel.getBeginTime(),curdate);
                return true;
            } else {
                mEndDateData = mCurrentDateModule;
                mBenginData = null;
                refreshUI();
                return true;
            }

        }

    }

    private void setListener() {
        //设置点击事件，上个月和下个月不能显示
        for (int i = 0; i <= mLastMonthDays; i++) {
            mDateCard[i].setOnClickListener(null);
        }
        for (int i = mLastMonthDays + 1; i <= mLastMonthDays + mCurMonthDays; i++) {
            final int position = i;
            mDateCard[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int j = mLastMonthDays + 1; j <= mLastMonthDays + mCurMonthDays; j++) {
                        //把选中的日期的背景设置成带红色的框，没有变成白色，并把当前的日期返回
                        if (position == j) {
                            //点击的日期是当前的日期
                            mDateCardList[j].mIsClick = true;
                            dealClickDateCard(mDateCardList[position]);
                        } else {
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
    private void dealClickDateCard(DateCardModule dateCardModule) {
        mCurrentDateModule = dateCardModule;
        long clickDay = getNowDate(dateCardModule.date);
        //如果选中的日期超过了当前的日期，
        if (null != mDateClickListener) {
            long today = DateChange.getDate();
            //小于今天
            if (today < clickDay) {
                mDateClickListener.clickDayAfterToday(dateCardModule.isStart == 1, dateCardModule.isStart == 2);
            } else if (today == clickDay) {
                mDateClickListener.clickDayEqualsToday(dateCardModule.isStart == 1, dateCardModule.isStart == 2);
            } else {
                mDateClickListener.clickDayBeforeToday(dateCardModule.isStart == 1, dateCardModule.isStart == 2);
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
            mDateCardList[monthStart] = new DateCardModule(1, mCalendar.get(Calendar.MONTH) + 1, mCalendar.get(Calendar.YEAR), true, PeriodType.TYPE_SAFE, 0, true);
        } else {
            mDateCardList[monthStart] = new DateCardModule(1, mCalendar.get(Calendar.MONTH) + 1, mCalendar.get(Calendar.YEAR), false, PeriodType.TYPE_SAFE, 0, true);
        }
        mDateNum[monthStart] = 1;

        //上个月(当前月份，显示上个月)
        if (monthStart > 0) {
            mCalendar.set(Calendar.DAY_OF_MONTH, 0);
            int dayInMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
            mLastMonthDays = monthStart - 1;

            for (int i = monthStart - 1; i >= 0; i--) {
                mDateCardList[i] = new DateCardModule(dayInMonth, mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.YEAR), false, PeriodType.TYPE_SAFE, 0, false);
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
                mDateCardList[monthStart + i] = new DateCardModule(i + 1, mCalendar.get(Calendar.MONTH) + 1, mCalendar.get(Calendar.YEAR), true, PeriodType.TYPE_SAFE, 0, true);
            } else {
                mDateCardList[monthStart + i] = new DateCardModule(i + 1, mCalendar.get(Calendar.MONTH) + 1, mCalendar.get(Calendar.YEAR), false, PeriodType.TYPE_SAFE, 0, true);
            }
        }

        //下个月
        for (int i = monthStart + monthDay; i < TOTAL_NUM; i++) {
            mDateCardList[i] = new DateCardModule(i - (monthStart + monthDay) + 2, mCalendar.get(Calendar.MONTH) + 1, mCalendar.get(Calendar.YEAR), false, PeriodType.TYPE_SAFE, 0, false);
            mDateNum[i] = i - (monthStart + monthDay) + 1;
        }
    }

    private void caculateType(List<MenstruationModel> mtmList) {
        if (mtmList == null) {
            for (int i = mLastMonthDays + 1; i <= mLastMonthDays + mCurMonthDays; i++) {
                final DateCardModule nowDateCardModule =  mDateCardList[i];
                if(null!=mBenginData){
                    if(nowDateCardModule.year== mBenginData.year && nowDateCardModule.month==mBenginData.month
                            && nowDateCardModule.date == mBenginData.date){
                        nowDateCardModule.type= PeriodType.TYPE_MENSTRUATION;
                        nowDateCardModule.isStart = 1;
                    }else{
                        nowDateCardModule.type= PeriodType.TYPE_SAFE;
                        nowDateCardModule.isStart= 0;
                    }
                }else if(null!=mEndDateData){
                    if(nowDateCardModule.year== mEndDateData.year && nowDateCardModule.month==mEndDateData.month
                            && nowDateCardModule.date == mEndDateData.date){
                        nowDateCardModule.type= PeriodType.TYPE_MENSTRUATION;
                        nowDateCardModule.isStart= 2;
                    }else{
                        nowDateCardModule.type= PeriodType.TYPE_SAFE;
                        nowDateCardModule.isStart= 0;
                    }
                }else{
                    nowDateCardModule.type= PeriodType.TYPE_SAFE;
                    nowDateCardModule.isStart= 0;
                }
            }
            return;
        }
        for (int i = mLastMonthDays + 1; i <= mLastMonthDays + mCurMonthDays; i++) {
            if (isMenOrCal(mtmList, i) != -1) {
                //处于月经期或者安全期
                int position = isMenOrCal(mtmList, i);
                if (mtmList.get(position).getBeginTime() > DateChange.dateTimeStamp(getYMD("yyyy-MM-dd"), "yyyy-MM-dd")) {
                    mDateCardList[i].type = PeriodType.TYPE_CALCULATE;//预测期
                } else if (mtmList.get(position).getEndTime() <= DateChange.dateTimeStamp(getYMD("yyyy-MM-dd"), "yyyy-MM-dd")) {
                    mDateCardList[i].type = PeriodType.TYPE_MENSTRUATION;//经期
                    if (isStart(mtmList.get(position), i)) {
                        mDateCardList[i].isStart = 1;
                    } else if (isEnd(mtmList.get(position), i)) {
                        mDateCardList[i].isStart = 2;
                    } else {
                        mDateCardList[i].isStart = 3;
                    }
                } else if (getNowDate(mDateCardList[i].date) == mtmList.get(position).getBeginTime()) {
                    if (mtmList.get(position).isCon()) {
                        mDateCardList[i].type = PeriodType.TYPE_MENSTRUATION;
                        if (isStart(mtmList.get(position), i)) {
                            mDateCardList[i].isStart = 1;
                        } else if (isEnd(mtmList.get(position), i)) {
                            mDateCardList[i].isStart = 2;
                        } else {
                            mDateCardList[i].isStart = 3;
                        }
                    } else {
                        mDateCardList[i].type = PeriodType.TYPE_MENSTRUATION;
                    }
                } else {
                    if (getNowDate(mDateCardList[i].date) > mtmList.get(position).getBeginTime() &&
                            getNowDate(mDateCardList[i].date) <= DateChange.dateTimeStamp(getYMD("yyyy-MM-dd"), "yyyy-MM-dd")) {
                        mDateCardList[i].type = PeriodType.TYPE_MENSTRUATION;
                        if (isStart(mtmList.get(position), i)) {
                            mDateCardList[i].isStart = 1;
                        } else if (isEnd(mtmList.get(position), i)) {
                            mDateCardList[i].isStart = 2;
                        } else {
                            mDateCardList[i].isStart = 3;
                        }
                    } else {
                        mDateCardList[i].type = PeriodType.TYPE_CALCULATE;
                    }
                }
            } else if (isSafeDay(mtmList, i)) {
                mDateCardList[i].type = PeriodType.TYPE_SAFE;
            } else {
                mDateCardList[i].type = PeriodType.TYPE_DANGEROUS;
            }
        }
    }

    /**
     * 重新计算经期
     *
     * @param beginTime 开始的时间
     * @param endTime   结束的时间
     */
    private void reCalculatePeriod(long beginTime, long endTime) {
        mBenginData = null;
        mEndDateData = null;

        MenstruationModel menstruationModel = mInitMenstruationModel;
        menstruationModel.setBeginTime(beginTime);
        menstruationModel.setEndTime(endTime);
        menstruationModel.setDurationDay((int)((endTime-beginTime)/DateConstants.DAY_TIME)+1);
        mInitMenstruationModel = menstruationModel;
        refreshUI();
//        initPeriodData(menstruationModel,mDateClickListener);
    }

    /**
     * 刷新当前的日历
     */
    public void refreshUI() {
        mCalendar.setTime(mCurDate);
        mCalendar.add(Calendar.MONTH, 0);
        mCurDate = mCalendar.getTime();

        mCalendar.setTime(mTodayDate);
        mCalendar.add(Calendar.MONTH, 0);
        mTodayDate = mCalendar.getTime();
        caculateType(getMenstruationModleLists());
        for (int i = 0; i < TOTAL_NUM; i++) {
            mDateCard[i].initData(mDateCardList[i]);
        }
        setListener();
    }

    public String clickToady() {
        mCalendar.setTime(mTodayDate);
        mCalendar.add(Calendar.MONTH, getNowTime("yyyy") * 12 + getNowTime("MM") - (mCalendar.get(Calendar.MONTH) + 1) - mCalendar.get(Calendar.YEAR) * 12);
        mCurDate = mCalendar.getTime();
        return recurToday(getMenstruationModleLists());
    }

    /**
     * 跳转到上个月
     *
     * @return
     */
    public String clickLastMonth() {
        mCalendar.setTime(mCurDate);
        mCalendar.add(Calendar.MONTH, -1);
        mCurDate = mCalendar.getTime();
        return clickLeftMonth(getMenstruationModleLists());
    }

    public String clickNextMonth() {
        mCalendar.setTime(mCurDate);
        mCalendar.add(Calendar.MONTH, +1);
        mCurDate = mCalendar.getTime();

        return clickRightMonth(getMenstruationModleLists());
    }

    private List<MenstruationModel> getMenstruationModleLists() {
        if (mBenginData != null || mEndDateData != null) {
            //经期的开始或者结束时间没有，无法计算经期
            return null;
        }

        long nowDate = DateChange.dateTimeStamp(mCalendar.get(Calendar.YEAR) + "-" + (mCalendar.get(Calendar.MONTH) + 1) + "-1", "yyyy-MM-dd");
        long nextDate = DateChange.dateTimeStamp(mCalendar.get(Calendar.YEAR) + "-" + (mCalendar.get(Calendar.MONTH) + 2) + "-1", "yyyy-MM-dd");
        List<MenstruationModel> lists = mMenstruationCalculate.calculateMt(mInitMenstruationModel, nowDate, nextDate);
        return lists;
    }

    /**
     * 向上一月
     *
     * @return
     */
    private String clickLeftMonth(List<MenstruationModel> mtmList) {
        mCalendar.setTime(mTodayDate);
        mCalendar.add(Calendar.MONTH, -1);
        mTodayDate = mCalendar.getTime();
        calculateDate();
        caculateType(mtmList);
        for (int i = 0; i < TOTAL_NUM; i++) {
            mDateCard[i].initData(mDateCardList[i]);
        }
        setListener();
        return getYearAndmonth();
    }

    /**
     * 向下一月
     *
     * @return
     */
    private String clickRightMonth(List<MenstruationModel> mtmList) {
        mCalendar.setTime(mTodayDate);
        mCalendar.add(Calendar.MONTH, 1);
        mTodayDate = mCalendar.getTime();
        calculateDate();
        caculateType(mtmList);
        for (int i = 0; i < TOTAL_NUM; i++) {
            mDateCard[i].initData(mDateCardList[i]);
        }
        setListener();
        return getYearAndmonth();
    }

    /**
     * 回今天
     *
     * @return
     */
    public String recurToday(List<MenstruationModel> mtmList) {
        mCalendar.setTime(mTodayDate);
        mCalendar.add(Calendar.MONTH, getNowTime("yyyy") * 12 + getNowTime("MM") - (mCalendar.get(Calendar.MONTH) + 1) - mCalendar.get(Calendar.YEAR) * 12);
        mTodayDate = mCalendar.getTime();
        calculateDate();
        caculateType(mtmList);
        int position = 1;
        for (int i = 0; i < TOTAL_NUM; i++) {
            mDateCard[i].initData(mDateCardList[i]);
        }
        setListener();
        return getYearAndmonth();
    }

    /**
     * 获得当前应该显示的年月
     *
     * @return
     */
    public String getYearAndmonth() {
        mCalendar.setTime(mTodayDate);
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH) + 1;
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
        for (int i = 0; i < mtmList.size(); i++) {
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
        for (int i = 0; i < mtmList.size(); i++) {
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
        if (getNowDate(mDateCardList[position].date) == menstruationModel.getEndTime()) {
            return true;
        }
        return false;
    }
}
