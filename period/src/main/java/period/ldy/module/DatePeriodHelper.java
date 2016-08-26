package period.ldy.module;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ludeyuan on 16/8/9.
 */
public class DatePeriodHelper {

    /**
     * 获取到数组中和这个时间靠近的周期
     * @param lists
     * @param time 当前的时间，以他为依据，选取最近的时间段返回
     * @return 如果在一个周期内，返回一组，否则返回前后两组
     */
    public static List<MenstruationModel> getSideList(List<MenstruationModel> lists,long time){
        if(lists==null || lists.size()<=1){
            return lists;
        }
        ArrayList<MenstruationModel> returnLists = new ArrayList<>();
        MenstruationModel preModel=null,afterModel=null;
        for(MenstruationModel model:lists){
            if(time>=model.getBeginTime() && time<=model.getEndTime()){
                //在经期内，直接返回一个经期对象
                returnLists.add(model);
                return returnLists;
            }else if(time<model.getBeginTime()){
                //当前时间在这个周期的前面
                if(afterModel==null){
                    afterModel = model;
                }else{
                    //比较这个周期和前个周期，选择最靠近的
                    if(model.getBeginTime()<afterModel.getBeginTime()){
                        afterModel = model;
                    }
                }
                continue;
            }else{
                if(preModel==null){
                    preModel = model;
                }else{
                    if(model.getEndTime()>preModel.getEndTime()){
                        preModel = model;
                    }
                }
            }
        }
        if(null!=preModel)
            returnLists.add(preModel);

        if(null!=afterModel)
            returnLists.add(afterModel);

        return returnLists;
    }

}
