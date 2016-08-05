package period.ldy.module;


public class MenstruationModel {
    private int id;
    private long beginTime; //月经开始时间
    private long endTime; //月经结束时间
    private long date; //月份
    private int cycle; //月经周期
    private int durationDay; //月经天数
    private boolean isCon = true;//是否是确定的

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getCycle() {
        return cycle;
    }

    public void setCycle(int cycle) {
        this.cycle = cycle;
    }

    public int getDurationDay() {
        return durationDay;
    }

    public void setDurationDay(int durationDay) {
        this.durationDay = durationDay;
    }

    public boolean isCon() {
        return isCon;
    }

    public void setCon(boolean isCon) {
        this.isCon = isCon;
    }

}
