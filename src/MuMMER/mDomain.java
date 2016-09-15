package MuMMER; /**
 * Created by elveleg on 9/9/2016.
 */
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.mdp.singleagent.model.statemodel.SampleStateModel;
import burlap.shell.visual.VisualExplorer;
import burlap.visualizer.StatePainter;
import burlap.visualizer.StateRenderLayer;
import burlap.visualizer.Visualizer;

import javax.management.Attribute;
import java.util.Random;

public class mDomain implements DomainGenerator{
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

    //Actions
    public static final String TASKCONSUME = "taskConsume";
    public static final String GREET = "Greet";
    public static final String AGOODBYE = "Goodbye";
    public static final String CHAT = "Chat";
    public static final String GIVEDIR = "giveDirections";
    public static final String REQNAME = "requestName";
    public static final String WAIT = "wait";
    public static final String CONFIRM = "confirm";
    public static final String REQTASK = "requestTask";


    @Override
    public SADomain generateDomain() {
        SADomain domain = new SADomain();
        domain.addActionTypes(
                //new UniversalActionType(TASKCONSUME),
                new UniversalActionType(GREET),
                new UniversalActionType(AGOODBYE),
                new UniversalActionType(CHAT),
                //new UniversalActionType(GIVEDIR),
                new UniversalActionType(WAIT),
                new UniversalActionType(CONFIRM),
                new UniversalActionType(REQTASK));

        WorldStateModel model = new WorldStateModel();
        RewardFunc rf = new RewardFunc();
        TerminalFunc tf = new TerminalFunc();

        domain.setModel(new FactoredModel(model, rf, tf));
        return domain;
    }

    protected class WorldStateModel implements SampleStateModel{
        //1 = taskConsume, 2 = greet, 3 = goodbye, 4 = chat, 5 = giveDirections, 6 = wait, 7 = confirm, 8 = reg_task
        public WorldStateModel(){
        }

        protected int getActionID(Action a){
            int id;

            switch (a.actionName()){
                case TASKCONSUME: id = 1;
                    break;
                case GREET: id = 2;
                    break;
                case AGOODBYE:  id = 3;
                    break;
                case CHAT:  id = 4;
                    break;
                case GIVEDIR:   id = 5;
                    break;
                case WAIT:  id = 6;
                    break;
                case CONFIRM:   id = 7;
                    break;
                case REQTASK:   id = 8;
                    break;
                default:    id = -1;
                    break;
            }

            return id;
        }
        @Override
        public State sample(State state, Action action) {
            String[] uTask;

            //TODO: inquiry: make sure the action selection is already been done before reaching this point.
            state = state.copy();

            //Copy the mutable state before alteration
            mState s = (mState)state;

            //Get the action ID of the selected action
            int actionID = getActionID(action);

            //If it was the agent's turn, check user's response
            if (!s.turnTaking){
                if(actionID == 2 && s.prevAct != 0){
                    s.usrEngaged = false;
                    s.usrTermination = true;
                }

                if((actionID == 3 && !s.bye) || (actionID != 3 && s.bye)){
                    s.usrEngaged = false;
                    s.usrTermination = true;
                }

                /* If agent takes taskConsume OR giveDir action while taskfilled is true, set the task as completed
                and reset task attributes. Also switch the mode to task-based */
                if (actionID == 1 || actionID == 5){

                    //If agent is confident on the task given, set task completed and flip the attributes
                    if(!s.lowConf){
                        s.tskCompleted = true;
                        s.tskFilled = false;
                        s.ctxTask = "";
                    } else {
                        s.usrTermination = true;
                        s.usrEngaged = false;
                    }
                }

                //If agents does not consume task while task slot is filled (and there is a high confidence score), punish
                if (s.tskFilled && (actionID != 1 || actionID != 5)){
                    if (!s.lowConf){
                        s.usrTermination = true;
                        s.usrEngaged = false;
                    }
                }

//                if (actionID == 4)
//                    s.mode = true;
//                else
//                    s.mode = false;

                if (actionID == 7 && s.lowConf)
                    s.lowConf = false;

                //Save selected action ID to prevAction attribute
                s.prevAct = actionID;
            } else { //If it was the user's turn (agent in wait state) pick a task at random
                //TODO: distance control
//                /*  If on previous turn the user was walking away and the agents didn't take Chat or
//                reqTask action then continue leaving (hard exit)  */
//                if ((agent.getIntValForAttribute(exWorld.DISTANCE) == 2) && (agent.getBooleanValForAttribute(exWorld.TIMEOUT)) &&
//                        (agent.getIntValForAttribute(exWorld.PREVACT) != 4 || agent.getIntValForAttribute(exWorld.PREVACT) != 8)){
//                    agent.setValue(exWorld.USRTERMINATION, true);
//                    agent.setValue(exWorld.USRENGAGED, false);
//                } else {
//
//                    //Set a random acceptable user distance from the sensor (close - medium)
//                    usrTurnDist = Math.round(getRandomDoubleInRange(usrDistMin, usrDistMax) * 100.0) / 100.0;
//                    if (usrTurnDist > usrDistMin && usrTurnDist <= 1.0)
//                        agent.setValue(exWorld.DISTANCE, 0);
//                    else if (usrTurnDist > 1.0 && usrTurnDist <= 1.8)
//                        agent.setValue(exWorld.DISTANCE, 1);
//                }
                //If user have said goodbye on his previous turn, then make him leave.
                if (s.bye)
                    s.usrEngaged = false;

                //Reset the TaskCompleted and timeout attributes
                s.tskCompleted = false;
                s.timeout = false;

                uTask = userTask(s);

                switch (uTask[0]) {
                    case "uGoodbye":
                        s.bye = true;
                        break;
                    case "uSilent":
                        s.timeout = true;
                        break;
                    case "uWalkAway":
                        s.distance = Integer.valueOf(uTask[1]); //TODO: distance wibbly-wobbly
                        s.timeout = true;
                        break;
                    case "uChat":
                        s.usrEngChat = true;
                        s.tskFilled = false;
                        s.ctxTask = "";
                        break;
                    case "uReq_Task":
                        s.tskFilled = true;
                        s.ctxTask = uTask[1];
                        break;
                    case "uReq_Dir":
                        s.tskFilled = true;
                        s.ctxTask = uTask[1];
                        break;
                    case "uConfirm":
                        s.lowConf = false;
                    default:
                        break;
                }

                //If user if not engaging chat, reset the userEngChat attribute.
                if (!uTask[0].equals("uChat"))
                    s.usrEngChat = false;

                //Add a 10% chance the ASR will fail to understand user's requested task
                if (uTask[0].equals("uReq_Task") || uTask[0].equals("uReq_Dir")){
                    double r = Math.random();
                    if (r < .1)
                        s.lowConf = true;
                }
            }

            //Flip whose turn this is
            s.turnTaking = !s.turnTaking;

            return s;
        }

        private String[] userTask (mState s){
            String[] result = new String[2];
            String[] task = {"uReq_Task", "uReq_Dir", "uGoodbye", "uSilent", "uWalkAway", "uChat"};
            String[] ctx = {"", "coffee", "electronics", "clothing"};
            int index, iCtx;

            Random random = new Random();
            index = random.nextInt(task.length);

            //If last agent's action was to request for task, user have 80% to provide a random TASK.
            if (s.prevAct == 8){
                double r = Math.random();
                if (r < .8){
                    result[0] = task[0];
                    random = new Random();
                    iCtx = random.nextInt(ctx.length);
                    result[1] = ctx[iCtx];

                    return result;
                } else {
                    random = new Random();
                    index = random.nextInt(task.length - 1); // select from all actions except uChat
                }
            }

            /* If user was chatting during his previous turn (meaning he got a response that the agent could not comply),
             * there is a 50% probability to give a task and 50% to say bye and leave */
            if (s.usrEngChat) {
                double r = Math.random();
                if (r < .5){
                    result[0] = task[0];
                    random = new Random();
                    iCtx = random.nextInt(ctx.length);
                    result[1] = ctx[iCtx];

                    return result;
                } else {
                    result[0] = "uGoodbye";
                    result[1] = "";

                    return result;
                }
            }

            //If agent NOT in chat mode, continue with uniform distribution action selection.
            result[0] = task[index];
            result[1] = "";
            if (task[index].equals("uReq_Task")){
                random = new Random();
                iCtx = random.nextInt(ctx.length);
                result[1] = ctx[iCtx];
            }
            if (task[index].equals("uReq_Dir")){
                result[1] = "directions";
            }
            if (task[index].equals("uWalkAway")){
                //Set the distance to "far".
                result[1]=String.valueOf(2);       }

            return result;
        }

        public double getRandomDoubleInRange(double min, double max){
            Random r = new Random();
            double randomValue = min + (max - min) * r.nextDouble();
            return randomValue;
            //TODO make this round
        }
    }

    public static class RewardFunc implements RewardFunction {
    /*
    For each turn the user is engaged: +1
    Completing a task: +10
    Greeting as appropriate: +100
    User leaves earlier: -100
    User leaves normally: +100
     */

        @Override
        public double reward(State state, Action groundedAction, State state1) {
            int reward;

            mState sprime = (mState) state1;
            mState s = (mState) state;
            if (sprime.tskCompleted)
                reward = 10;
            else if (sprime.bye)
                reward = 100;
            else if (sprime.usrTermination)
                reward = -100;
            else if (groundedAction.actionName().equals(GREET) && s.prevAct == 0){
                reward = 100;}
            else
                reward = 1;

            return reward;
        }
    }

    public static class TerminalFunc implements TerminalFunction {

        @Override
        public boolean isTerminal(State state) {
            mState s = (mState) state;
            if (!s.usrEngaged) {
                return true;
            }
            return false;
        }
    }
}
