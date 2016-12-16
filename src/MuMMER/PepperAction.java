package MuMMER;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.state.State;

import java.util.Arrays;
import java.util.List;


public class PepperAction implements ActionType, iActions, iAttributes{
    public String typeName;
    public Action action;
    protected List<Action> allActions;

    public PepperAction(String typeName) {
        this(new SimpleAction(typeName));
    }

    public PepperAction(Action action) {
        this(action.actionName(), action);
    }

    public PepperAction(String s, Action action) {
        this.typeName = s;
        this.action = action;
        this.allActions = Arrays.asList(new Action[]{this.action});
    }

    @Override
    public String typeName() {
        return this.typeName;
    }

    @Override
    public Action associatedAction(String s) {
        return this.action;
    }

    @Override
    public List<Action> allApplicableActions(State state) {
        mState s = (mState) state;

        if (this.typeName.equals(GIVEDIR) && !s.ctxTask.equals("directions"))
            return Arrays.asList(new Action[]{});
        if (this.typeName.equals(CONFIRM) && !s.lowConf)
            return Arrays.asList(new Action[]{});
        if (this.typeName.equals(REQTASK) && !s.timeout)
            return Arrays.asList(new Action[]{});
        if (this.typeName.equals(TASKCONSUME) && (s.ctxTask.equals("directions") || s.ctxTask.equals("voucher")))
            return Arrays.asList(new Action[]{});

        return this.allActions;
    }
}
