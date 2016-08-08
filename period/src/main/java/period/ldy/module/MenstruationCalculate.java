package period.ldy.module;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ludeyuan on 16/7/27.
 * 计算当月的经期
 */
public class MenstruationCalculate {

    private final long DAY_LENGTH = 24 * 60 * 60 * 1000;    //一天的时长
    private long newData;       //当前的月份
    private long nextData;      //下个月份
    private Date mCurDate=null;      //当前的日期

    /**
     * 获取到当前月份的经期
     *
     * @param nowDate  当月时间
     * @param nextDate 下月时间
     * @return
     */
    public ArrayList<MenstruationModel> calculateMt(MenstruationModel model, long nowDate, long nextDate) {
        ArrayList<MenstruationModel> menstruationModelList = new ArrayList<>();
        if (nowDate < model.getDate() - model.getCycle() * DAY_LENGTH) {
            //现在时间小于基础时间，不用计算
            menstruationModelList.add(model);
            return menstruationModelList;
        }

        //计算在本月内的经期(开始的时间往回推一个月)
        //步骤1、获取之前一个周期的值；2一直循环到本月结束；3、循环的间隔为周期
        long startTime = model.getBeginTime() - model.getCycle() * DAY_LENGTH;
        long endTime = model.getBeginTime()+ model.getDurationDay()*DAY_LENGTH + model.getCycle()*DAY_LENGTH;

        while (startTime>nowDate){
            startTime = startTime - model.getCycle() * DAY_LENGTH;
            endTime = endTime -model.getCycle() *DAY_LENGTH;
        }
        while (endTime<nextDate){
            startTime = startTime + model.getCycle() *DAY_LENGTH;
            endTime =endTime +model.getCycle() *DAY_LENGTH;
        }

        long durationDay = model.getDurationDay() * DAY_LENGTH;   //月经的持续时间
        long cyclerDay = model.getCycle()*DAY_LENGTH;
        while (startTime + durationDay <= endTime) {
            if (startTime + durationDay >= nowDate) {
                //经期出现在当前月份内，添加经期
                if(startTime>=nextDate){//下个月
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(nowDate);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    int dayInMonth = calendar.get(Calendar.DAY_OF_MONTH);
                    long lastMonth = nowDate + dayInMonth * dayInMonth;
                    menstruationModelList.add(getMenstruationModel(startTime,
                            startTime + (model.getDurationDay()-1) * DAY_LENGTH, lastMonth, model));
                }else if (startTime >= nowDate) {
                    //当前月份
                    menstruationModelList.add(getMenstruationModel(startTime,
                            startTime + (model.getDurationDay()-1) * DAY_LENGTH, nowDate, model));
                } else {
                    //上个月
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(nowDate);
                    calendar.set(Calendar.DAY_OF_MONTH, 0);
                    int dayInMonth = calendar.get(Calendar.DAY_OF_MONTH);
                    long lastMonth = nowDate - dayInMonth * dayInMonth;
                    menstruationModelList.add(getMenstruationModel(startTime,
                            startTime + (model.getDurationDay()-1) * DAY_LENGTH, lastMonth, model));
                }
            }
            startTime = startTime + cyclerDay;
        }
        return menstruationModelList;
    }


    /**
     * 获取到出现在当前月份的经期
     *
     * @param beginTime
     * @param endTime
     * @param month
     * @param model
     * @return
     */
    private static MenstruationModel getMenstruationModel(long beginTime, long endTime, long month, MenstruationModel model) {
        MenstruationModel menstruationModel = new MenstruationModel();
        menstruationModel.setCycle(model.getCycle());
        menstruationModel.setDurationDay(model.getDurationDay());
        menstruationModel.setDate(month);
        menstruationModel.setBeginTime(beginTime);
        menstruationModel.setEndTime(endTime);
        return menstruationModel;
    }


}
