package MuMMER;

/**
 * Created by elveleg on 15/9/2016.
 */
public interface iAttributes {
    //State representation
    public static final String MODE = "mode"; //0 = task, 1 = chat
    public static final String DISTANCE = "distance";
    public static final String USRENGCHAT = "user engaged chat";
    public static final String TIMEOUT= "timeout";
    public static final String TASKFILLED= "task filled";
    public static final String TASKCOMPLETED= "task completed";
    public static final String USRENGAGED= "user engaged";
    public static final String CTXTASK= "task context";
    public static final String SGOODBYE = "goodbye";
    public static final String USRTERMINATION= "forced user termination";
    public static final String TURNTAKING= "turn taking"; //0 = agent, 1 = user
    public static final String LOWCONF= "low confidence";
    public static final String PREVACT= "previous action";
}
