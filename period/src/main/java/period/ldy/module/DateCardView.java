package period.ldy.module;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by ludeyuan on 16/7/27.
 * 日历中具体一天的界面
 */
public class DateCardView extends FrameLayout{
    private TextView mDateNumText;      //当月的几号
    private TextView mTodayText;        //今天
    private ImageView mIconImage;       //标志开始和结束的图标
    private LinearLayout mRootView;     //根视图
    private LinearLayout mStateView;    //显示状态的图片

    public DateCardView(Context context){
        super(context);
        initView();
    }

    private void initView(){
        //添加根视图
        mRootView = new LinearLayout(getContext());
        mRootView.setOrientation(LinearLayout.VERTICAL);
        mRootView.setBackgroundResource(R.drawable.period_safe_date_view_bg);
        LayoutParams rootParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        rootParams.setMargins(5,5,0,0);
        mRootView.setLayoutParams(rootParams);
        mRootView.setPadding(1,1,1,1);
        addView(mRootView);

        //添加状态的图片
        mStateView = new LinearLayout(getContext());
        mStateView.setOrientation(LinearLayout.VERTICAL);
        mStateView.setBackgroundResource(R.drawable.period_safe_date_view_bg);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mStateView.setLayoutParams(lp);
        mRootView.addView(mStateView, lp);

        //显示日期
        mDateNumText = new TextView(getContext());
        mDateNumText.setTextSize(14);
        mDateNumText.setTextColor(getResources().getColor(R.color.period_card_text_state_safe));
        mDateNumText.setPadding(5, 0, 0, 0);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mStateView.addView(mDateNumText, params);

        //添加底部的开始和今天的父布局
        LinearLayout bottomLayout = new LinearLayout(getContext());
        bottomLayout.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams bottomParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        bottomLayout.setPadding(5, 0, 0, 0);
        bottomLayout.setGravity(Gravity.CENTER_VERTICAL);
        bottomLayout.setLayoutParams(bottomParams);
        mStateView.addView(bottomLayout);

        //显示姨妈来了、走了图标
        mIconImage = new ImageView(getContext());
        LayoutParams ivParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mIconImage.setLayoutParams(ivParams);
        mIconImage.setVisibility(View.GONE);
        bottomLayout.addView(mIconImage);

        //显示今天
        mTodayText = new TextView(getContext());
        LayoutParams tvParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mTodayText.setLayoutParams(tvParams);
        mTodayText.setTextColor(getResources().getColor(R.color.period_card_text_state_safe));
        mTodayText.setTextSize(12);
        mTodayText.setText("今天");
        mTodayText.setVisibility(View.GONE);
        mTodayText.setGravity(Gravity.RIGHT);
        bottomLayout.addView(mTodayText);
    }

    /**
     * 初始化日期
     * @param dateCardModule
     */
    public void initData(DateCardModule dateCardModule){
        mDateNumText.setText(dateCardModule.date+"");//几号

        setToMonth(dateCardModule.istoMonth);
        setToDay(dateCardModule.isToday,dateCardModule.type);
        setStart(dateCardModule.isStart);
        setType(dateCardModule.type);
    }

    /**
     * 是否显示内容（是否是这个月的日期）
     * @param dayInMonth
     */
    private void setToMonth(boolean dayInMonth){
        if (dayInMonth) {
            mIconImage.setVisibility(View.VISIBLE);
            mDateNumText.setVisibility(View.VISIBLE);
            mTodayText.setVisibility(View.VISIBLE);
        } else {
            mIconImage.setVisibility(View.GONE);
            mDateNumText.setVisibility(View.GONE);
            mTodayText.setVisibility(View.GONE);
        }
    }

    /**
     * 今天
     * @param isToday
     * @param type 不同的状态
     */
    private void setToDay(boolean isToday,PeriodType type){
        if (isToday) {
            mTodayText.setVisibility(View.VISIBLE);
        } else {
            mTodayText.setVisibility(View.GONE);
        }
        if (type == PeriodType.TYPE_MENSTRUATION || type == PeriodType.TYPE_CALCULATE) {
            mTodayText.setTextColor(Color.WHITE);
        } else {
            mTodayText.setTextColor(getResources().getColor(R.color.period_card_text_state_safe));
        }
    }

    /**
     * @param dayType 1表示开始 2表示结束
     */
    private void setStart(int dayType){
        if (dayType == 1) {
            mIconImage.setVisibility(View.VISIBLE);
            mIconImage.setImageResource(R.drawable.period_begin);
        } else if (dayType == 2) {
            mIconImage.setVisibility(View.VISIBLE);
            mIconImage.setImageResource(R.drawable.period_end);
        } else {
            mIconImage.setVisibility(View.GONE);
        }
    }

    private void setType(PeriodType type){
        if(type==PeriodType.TYPE_MENSTRUATION){
            //经期
            mDateNumText.setTextColor(getResources().getColor(R.color.period_card_text_state_menstruation));
            mStateView.setBackgroundResource(R.drawable.period_menstruation_date_view_bg);
        }else if(type==PeriodType.TYPE_CALCULATE){
            //计算后处于经期
            mDateNumText.setTextColor(getResources().getColor(R.color.period_card_text_state_calculate));
            mStateView.setBackgroundResource(R.drawable.period_calculate_date_view_bg);
        }else if(type==PeriodType.TYPE_SAFE){
            //计算后处于安全期
            mDateNumText.setTextColor(getResources().getColor(R.color.period_card_text_state_safe));
            mStateView.setBackgroundResource(R.drawable.period_safe_date_view_bg);
        }else if(type == PeriodType.TYPE_DANGEROUS){
            //计算后处于排卵期
            mDateNumText.setTextColor(getResources().getColor(R.color.period_card_text_state_dangerous));
            mStateView.setBackgroundResource(R.drawable.period_dangerous_date_view_bg);
        }
    }
}
