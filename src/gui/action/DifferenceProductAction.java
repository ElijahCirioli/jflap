package gui.action;

import gui.environment.AutomatonEnvironment;

public class DifferenceProductAction extends CartesianProductAction {
    public DifferenceProductAction(AutomatonEnvironment environment) {
        super(environment, "Difference");
    }

    @Override
    protected boolean isFinalState(boolean final1, boolean final2) {
        return final1 && !final2;
    }
}
