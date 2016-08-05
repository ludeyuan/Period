package period.ldy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import period.ldy.module.DateChange;
import period.ldy.module.DatePeriodView;
import period.ldy.module.MenstruationModel;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DatePeriodView mDateCardView;
    private TextView mTimeText;
    private Button mLastButton;
    private Button mNextButton;
    private Button mTodayButton;

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

        int num = 5;
        int cycle = 30;
        String etLast = "2016-6-28";

        MenstruationModel mtm = new MenstruationModel();
        mtm.setBeginTime(DateChange.dateTimeStamp(etLast, "yyyy-MM-dd"));
        mtm.setEndTime(DateChange.dateTimeStamp(etLast, "yyyy-MM-dd") + 86400000l * (num - 1));
        mtm.setCycle(cycle);
        mtm.setDurationDay(num);
        mtm.setDate(DateChange.dateTimeStamp(etLast, "yyyy-MM-dd"));

        mDateCardView.initPeriodData(mtm);
        mTimeText.setText(mDateCardView.getYearAndmonth());
    }

    @Override
    public void onClick(View v) {
        if(v==mLastButton){
            mTimeText.setText(mDateCardView.clickLastMonth());
        }else if(v==mNextButton){
            mTimeText.setText(mDateCardView.clickNextMonth());
        }else if(v==mTodayButton){
            mTimeText.setText(mDateCardView.clickToady());
        }
    }
}
