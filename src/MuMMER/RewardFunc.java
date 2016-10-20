package MuMMER;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;

/**
 * Created by elveleg on 15/9/2016.
 */
public class RewardFunc implements RewardFunction, iActions {
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
        else if (sprime.usrTermination)
            reward = -100;
//        else if (groundedAction.actionName().equals(GREET) && s.prevAct == 0)
//            reward = 100;
//        else if ((s.turnTaking && !groundedAction.actionName().equals(WAIT)) || (!s.turnTaking && groundedAction.actionName().equals(WAIT)))
//            reward = -1000;
        else
            reward = 5;

        return reward;
    }
}
