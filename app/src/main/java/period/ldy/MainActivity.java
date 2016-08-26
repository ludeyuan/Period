package period.ldy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import period.ldy.module.DateChange;
import period.ldy.module.DateClickListener;
import period.ldy.module.DatePeriodView;
import period.ldy.module.MenstruationModel;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DatePeriodView mDateCardView;
    private TextView mTimeText;
    private Button mLastButton;
    private Button mNextButton;
    private Button mTodayButton;

    private View mStartContainerView;
    private View mEndContainerView;
    private ImageView mStartCheckBox;
    private ImageView mEndCheckBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDateCardView = (DatePeriodView)findViewById(R.id.main_datecardview);
        mTimeText = (TextView)findViewById(R.id.main_time);
        mLastButton = (Button)findViewById(R.id.main_button_last);
        mLastButton.setOnClickListener(this);
        mNextButton = (Button)findViewById(R.id.main_button_next);
        mNextButton.setOnClickListener(this);
        mTodayButton = (Button)findViewById(R.id.main_button_today);
        mTodayButton.setOnClickListener(this);
        mStartContainerView = (View)findViewById(R.id.period_container_layout_start);
        mEndContainerView = (View)findViewById(R.id.period_container_layout_end);
        mStartCheckBox = (ImageView)findViewById(R.id.period_container_image_start_checkbox);
        mStartCheckBox.setOnClickListener(this);
        mEndCheckBox = (ImageView)findViewById(R.id.period_container_image_end_checkbox);
        mEndCheckBox.setOnClickListener(this);

        int num = 5;
        int cycle = 30;
        String etLast = "2016-8-2";

        MenstruationModel mtm = new MenstruationModel();
        mtm.setBeginTime(DateChange.dateTimeStamp(etLast, "yyyy-MM-dd"));
        mtm.setEndTime(DateChange.dateTimeStamp(etLast, "yyyy-MM-dd") + 86400000l * (num - 1));
        mtm.setCycle(cycle);
        mtm.setDurationDay(num);
        mtm.setDate(DateChange.dateTimeStamp(etLast, "yyyy-MM-dd"));

        mDateCardView.initPeriodData(mtm,mDateClicklistener);
        mTimeText.setText(mDateCardView.getYearAndmonth());
    }

    private DateClickListener mDateClicklistener = new DateClickListener() {
        @Override
        public void clickDayBeforeToday(boolean isStart,boolean isEnd) {
            mStartContainerView.setVisibility(View.VISIBLE);
            mEndContainerView.setVisibility(View.VISIBLE);
            if(isStart){
                mStartCheckBox.setImageResource(R.drawable.period_checkbox_on);
                mEndCheckBox.setImageResource(R.drawable.period_checkbox_off);
            }else if(isEnd){
                mStartCheckBox.setImageResource(R.drawable.period_checkbox_off);
                mEndCheckBox.setImageResource(R.drawable.period_checkbox_on);
            }else{
                mStartCheckBox.setImageResource(R.drawable.period_checkbox_off);
                mEndCheckBox.setImageResource(R.drawable.period_checkbox_off);
            }
        }

        @Override
        public void clickDayEqualsToday(boolean isStart,boolean isEnd) {
            mStartContainerView.setVisibility(View.VISIBLE);
            mEndContainerView.setVisibility(View.VISIBLE);
            if(isStart){
                mStartCheckBox.setImageResource(R.drawable.period_checkbox_on);
                mEndCheckBox.setImageResource(R.drawable.period_checkbox_off);
            }else if(isEnd){
                mStartCheckBox.setImageResource(R.drawable.period_checkbox_off);
                mEndCheckBox.setImageResource(R.drawable.period_checkbox_on);
            }else{
                mStartCheckBox.setImageResource(R.drawable.period_checkbox_off);
                mEndCheckBox.setImageResource(R.drawable.period_checkbox_off);
            }
        }

        @Override
        public void clickDayAfterToday(boolean isStart,boolean isEnd) {
            mStartContainerView.setVisibility(View.GONE);
            mEndContainerView.setVisibility(View.GONE);
        }
    };

    @Override
    public void onClick(View v) {

        if(v==mLastButton){
            mTimeText.setText(mDateCardView.clickLastMonth());
        }else if(v==mNextButton){
            mTimeText.setText(mDateCardView.clickNextMonth());
        }else if(v==mTodayButton){
            mTimeText.setText(mDateCardView.clickToady());
        }else if(v==mStartCheckBox){
            //开始
            mDateCardView.clickStart(true);

        }else if(v==mEndCheckBox){
            //结束
            mDateCardView.clickEnd(true);
        }
    }
}
