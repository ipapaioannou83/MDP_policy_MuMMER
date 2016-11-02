/**
 * Created by elveleg on 9/9/2016.
 */
package MuMMER;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.UnknownKeyException;
//import com.sun.org.apache.xpath.internal.operations.String;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

//Actions

//State attributes
import static MuMMER.mDomain.*;


public class mState implements MutableState{
    public boolean mode;
    public boolean bye;
    public boolean tskCompleted;
    public boolean tskFilled;
    public boolean timeout;
    public String ctxTask;
    public int distance;
    public int prevAct;
    public boolean usrTermination;
    public boolean usrEngaged;
    public boolean usrEngChat;
    public boolean lowConf;
    public boolean turnTaking;


    public mState(){
    }

    public mState(boolean mode, boolean bye, boolean tskCompleted, boolean tskFilled, boolean timeout, String ctxTask, int distance, int prevAct,
                  boolean usrEngChat, boolean usrEngaged, boolean usrTermination, boolean lowConf, boolean turnTaking){
        this.mode = mode;
        this.bye = bye;
        this.tskCompleted = tskCompleted;
        this.tskFilled = tskFilled;
        this.timeout = timeout;
        this.ctxTask = ctxTask;
        this.distance = distance;
        this.prevAct = prevAct;
        this.usrEngChat = usrEngChat;
        this.usrEngaged = usrEngaged;
        this.usrTermination = usrTermination;
        this.lowConf = lowConf;
        this.turnTaking = turnTaking;
    }

    private final static List<Object> keys = Arrays.<Object>asList(MODE, SGOODBYE, TASKCOMPLETED, TASKFILLED, TIMEOUT, CTXTASK, DISTANCE, PREVACT,
            USRENGAGED, USRENGCHAT, USRTERMINATION, LOWCONF, TURNTAKING);

    @Override
    public MutableState set(Object variableKey, Object value) {
        if (variableKey.equals(MODE)){
            this.mode = StateUtilities.stringOrBoolean(value);
        }
        else if (variableKey.equals(SGOODBYE)){
            this.bye = StateUtilities.stringOrBoolean(value);
        }
        else if (variableKey.equals(TASKCOMPLETED)){
            this.tskCompleted = StateUtilities.stringOrBoolean(value);
        }
        else if (variableKey.equals(TASKFILLED)){
            this.tskFilled = StateUtilities.stringOrBoolean(value);
        }
        else if (variableKey.equals(TIMEOUT)){
            this.timeout = StateUtilities.stringOrBoolean(value);
        }
        else if (variableKey.equals(CTXTASK)){
            this.ctxTask = StateUtilities.stringOrBoolean(value).toString();
        }
        else if (variableKey.equals(DISTANCE)){
            this.distance = StateUtilities.stringOrNumber(value).intValue();
        }
        else if (variableKey.equals(PREVACT)){
            this.prevAct = StateUtilities.stringOrNumber(value).intValue();
        }
        else if (variableKey.equals(USRENGAGED)){
            this.usrEngaged = StateUtilities.stringOrBoolean(value);
        }
        else if (variableKey.equals(USRENGCHAT)){
            this.usrEngChat = StateUtilities.stringOrBoolean(value);
        }
        else if (variableKey.equals(USRTERMINATION)){
            this.usrTermination = StateUtilities.stringOrBoolean(value);
        }
        else if (variableKey.equals(LOWCONF)){
            this.lowConf = StateUtilities.stringOrBoolean(value);
        }
        else if (variableKey.equals(TURNTAKING)){
            this.turnTaking = StateUtilities.stringOrBoolean(value);
        }
        else{
            throw new UnknownKeyException(variableKey);
        }
        return this;
    }

    //TODO: add the List<> enumeration
    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if (variableKey.equals(MODE)){
            return mode;
        }
        else if (variableKey.equals(SGOODBYE)){
            return bye;
        }
        else if (variableKey.equals(TASKCOMPLETED)){
            return tskCompleted;
        }
        else if (variableKey.equals(TASKFILLED)){
            return tskFilled;
        }
        else if (variableKey.equals(TIMEOUT)){
            return timeout;
        }
        else if (variableKey.equals(CTXTASK)){
            return ctxTask;
        }
        else if (variableKey.equals(DISTANCE)){
            return distance;
        }
        else if (variableKey.equals(PREVACT)){
            return prevAct;
        }
        else if (variableKey.equals(USRENGAGED)){
            return usrEngaged;
        }
        else if (variableKey.equals(USRENGCHAT)){
            return usrEngChat;
        }
        else if (variableKey.equals(USRTERMINATION)){
            return usrTermination;
        }
        else if (variableKey.equals(LOWCONF)){
            return lowConf;
        }
        else if (variableKey.equals(TURNTAKING)){
            return turnTaking;
        }
        else{
            throw new UnknownKeyException(variableKey);
        }
    }

    @Override
    public State copy() {
        return new mState(mode, bye, tskCompleted, tskFilled, timeout, ctxTask, distance, prevAct,
                usrEngChat, usrEngaged, usrTermination, lowConf, turnTaking);
    }

    @Override
    public String toString() {
        return StateUtilities.stateToString(this);
    }

    public static State getInitialState(){
        mState s = new mState();
        Random random = new Random();

        s.distance = random.nextInt(3);
        s.mode = false;
        s.usrEngaged = true;
        s.usrTermination = false;
        s.usrEngChat = false;
        s.lowConf = false;
        s.tskCompleted = false;
        s.ctxTask = "";
        s.timeout = false;
        s.tskFilled = false;
        s.turnTaking = false;
        s.prevAct = 0;
        s.bye = false;

        return s;
    }
}
