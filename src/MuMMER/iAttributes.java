package MuMMER;

public interface iAttributes {
    String MODE = "mode"; //0 = task, 1 = chat
    String DISTANCE = "distance";
    String USRENGCHAT = "user engaged chat";
    String TIMEOUT= "timeout";
    String TASKFILLED= "task filled";
    String TASKCOMPLETED= "task completed";
    String USRENGAGED= "user engaged";
    String CTXTASK= "task context";
    String SGOODBYE = "goodbye";
    String USRTERMINATION= "forced user termination";
    String TURNTAKING= "turn taking"; //0 = agent, 1 = user
    String LOWCONF= "low confidence";
    String PREVACT= "previous action";
}
