package gui.action;

import gui.environment.AutomatonEnvironment;

public class IntersectionProductAction extends CartesianProductAction {
    public IntersectionProductAction(AutomatonEnvironment environment) {
        super(environment, "Intersection");
    }

    @Override
    protected boolean isFinalState(boolean final1, boolean final2) {
        return final1 && final2;
    }
}
