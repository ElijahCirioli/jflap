package gui.action;

import gui.environment.AutomatonEnvironment;

public class UnionProductAction extends CartesianProductAction {
    public UnionProductAction(AutomatonEnvironment environment) {
        super(environment, "Union");
    }

    @Override
    protected boolean isFinalState(boolean final1, boolean final2) {
        return final1 || final2;
    }
}
