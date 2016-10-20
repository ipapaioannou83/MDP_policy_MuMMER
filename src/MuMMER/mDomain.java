package MuMMER; /**
 * Created by elveleg on 9/9/2016.
 */

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.statemodel.SampleStateModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class mDomain implements DomainGenerator, iAttributes, iActions{





    @Override
    public SADomain generateDomain() {
        SADomain domain = new SADomain();

        domain.addActionTypes(
//                new UniversalActionType(TASKCONSUME),
//                new UniversalActionType(GREET),
//                new UniversalActionType(AGOODBYE),
//                new UniversalActionType(CHAT),
//                new UniversalActionType(GIVEDIR),
//                new UniversalActionType(WAIT),
//                new UniversalActionType(CONFIRM),
//                new UniversalActionType(REQTASK));

                new PepperAction(TASKCONSUME),
                new PepperAction(GREET),
                new PepperAction(AGOODBYE),
                new PepperAction(CHAT),
                new PepperAction(GIVEDIR),
                new PepperAction(WAIT),
                new PepperAction(CONFIRM),
                new PepperAction(REQTASK));


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

            state = state.copy();

            //Copy the mutable state before alteration
            mState s = (mState)state;

            //Get the action ID of the selected action
            int actionID = getActionID(action);

            //If it was the agent's turn, check user's response
            if (!s.turnTaking){

                if(actionID == 6){
                    s.usrEngaged = false;
                    s.usrTermination = true;
                }

                if((actionID == 2 && s.prevAct != 0) || (actionID != 2 && s.prevAct == 0)){
                    s.usrEngaged = false;
                    s.usrTermination = true;
                }

                if((actionID == 3 && !s.bye) || (actionID != 3 && s.bye)){
                    s.usrEngaged = false;
                    s.usrTermination = true;
                }

                /* If agent takes taskConsume OR giveDir action while taskfilled is true, set the task as completed
                and reset task attributes. Also switch the mode to task-based */
                if (actionID == 1){

                    //If took the tskConsume without having a task -> punish
                    if(!s.tskFilled || s.ctxTask.equals("directions")){
                        s.usrTermination = true;
                        s.usrEngaged = false;
                    }

                    //If agent is confident on the task given, set task completed and flip the attributes
                    if(!s.lowConf && s.tskFilled){
                        s.tskCompleted = true;
                        s.tskFilled = false;
                        s.ctxTask = "";
                    } else if(s.lowConf){
                        s.usrTermination = true;
                        s.usrEngaged = false;
                    }
                }

                if (actionID == 5){

                    //If took the tskConsume without having a task -> punish
                    if(!s.ctxTask.equals("directions")){
                        s.usrTermination = true;
                        s.usrEngaged = false;
                    }

                    //If agent is confident on the task given, set task completed and flip the attributes
                    if(!s.lowConf && s.ctxTask.equals("directions")){
                        s.tskCompleted = true;
                        s.tskFilled = false;
                        s.ctxTask = "";
                    } else {
                        s.usrTermination = true;
                        s.usrEngaged = false;
                    }
                }

//                //If agents does not consume task while task slot is filled (and there is a high confidence score), punish
//                if ((actionID != 1 && s.tskFilled) || (actionID != 5 && s.tskFilled)){
//                    if (!s.lowConf){
//                        s.usrTermination = true;
//                        s.usrEngaged = false;
//                    }
//                }

                if (s.tskFilled && actionID != 1){
                    s.usrEngaged = false;
                    s.usrTermination = true;
                }

                if (s.tskFilled && actionID != 5){
                    s.usrEngaged = false;
                    s.usrTermination = true;
                }

                if (actionID == 4)
                    s.mode = true;
                else
                    s.mode = false;

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

                // Only the wait action should be applicable during this turn
                if (actionID != 6){
                    s.usrTermination = true;
                    s.usrEngaged = false;
                }


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
                        s.distance = 1;
                        s.tskFilled = true;
                        s.ctxTask = uTask[1];
                        break;
                    case "uReq_Dir":
                        s.distance = 1;
                        s.tskFilled = true;
                        s.ctxTask = uTask[1];
                        break;
                    case "uConfirm":
                        s.lowConf = false;
                    case "uLeave":
                        s.usrTermination = true;
                        s.usrEngaged = false;
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

            index = randInt(0, task.length - 1);

//            if (s.prevAct == 1)
//                if (!s.tskFilled || s.lowConf){
//                    result[0] = "uHarshLeave";
//                    result[1] = "";
//
//                    return result;
//                }
//
//            if (s.prevAct == 5)
//                if ((!s.tskFilled && !s.ctxTask.equals("directions"))|| s.lowConf){
//                    result[0] = "uHarshLeave";
//                    result[1] = "";
//
//                    return result;
//                }


            //If last agent's action was to request for task, user have 80% to provide a random TASK or DIRECTIONS.
            if (s.prevAct == 8){
                double r = Math.random();
                if (r < .6){
                    result[0] = task[randInt(0, 1)];
                    if (result[0].equals("uReq_Task"))
                        result[1] = ctx[randInt(0, ctx.length - 1)];
                    if (result[0].equals("uReq_Dir"))
                        result[1] = "directions";

                    return result;
                } else {
                    index = randInt(0, task.length - 2); // select from all actions except uChat
                }
            }

            /* If agent already in chat mode, user have 50% probability to continue the chat
            and 50% to take some other action */
            if (s.usrEngChat) {
                double r = Math.random();
                    if (r < .5){
                        result[0] = task[task.length - 1];
                        result[1] = "";

                        return result;
                    } else {
                        index = randInt(0, task.length - 1);
                    }

            }

            //Else continue with uniform distribution action selection.
            result[0] = task[index];
            result[1] = "";
            if (task[index].equals("uReq_Task")){
                iCtx = randInt(0, ctx.length - 1);
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
            return min + (max - min) * r.nextDouble();
            //TODO make this round
        }
    }

    public static int randInt(int min, int max) {
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }


}