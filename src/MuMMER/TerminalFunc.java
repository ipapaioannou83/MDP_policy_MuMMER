package MuMMER;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;

/**
 * Created by elveleg on 15/9/2016.
 */
public class TerminalFunc implements TerminalFunction {

    @Override
    public boolean isTerminal(State state) {
        mState s = (mState) state;
        if (!s.usrEngaged) {
            return true;
        }
        return false;
    }
}
