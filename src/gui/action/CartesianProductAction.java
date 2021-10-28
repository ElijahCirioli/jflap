/*
 *  JFLAP - Formal Languages and Automata Package
 *
 *
 *  Susan H. Rodger
 *  Computer Science Department
 *  Duke University
 *  August 27, 2009

 *  Copyright (c) 2002-2009
 *  All rights reserved.

 *  JFLAP is open source software. Please see the LICENSE for terms.
 *
 */


package gui.action;

import automata.*;
import automata.fsa.FSATransition;
import automata.fsa.FiniteStateAutomaton;
import gui.environment.*;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;

/**
 * This creates a new automaton as the cartesian product of two automata
 *
 * @author Elijah Cirioli
 */

public class CartesianProductAction extends AutomatonAction {
    /**
     * Instantiates a new action to create the cartesian product
     *
     * @param environment the automaton environment
     *                    name
     *                    the action name
     */
    public CartesianProductAction(AutomatonEnvironment environment, String name) {
        super(name, null);
        this.environment = environment;
    }

    /**
     * triggers the creation of the new automaton
     *
     * @param e the action event
     */
    public void actionPerformed(ActionEvent e) {
        JComboBox combo = new JComboBox();
        /* find all other FSA */
        for (EnvironmentFrame frame : Universe.frames()) {
            Environment env = frame.getEnvironment();
            if (env == environment
                    || !(env instanceof AutomatonEnvironment)
                    || environment.getObject().getClass() != env.getObject()
                    .getClass())
                continue;
            if (environment.getObject() instanceof FiniteStateAutomaton) {
                combo.addItem(frame);
            }
        }
        /* no other FSA exist */
        if (combo.getItemCount() == 0) {
            JOptionPane.showMessageDialog(Universe
                            .frameForEnvironment(environment),
                    "No other finite state automata around.");
            return;
        }
        /* make the user confirm */
        int result = JOptionPane.showOptionDialog(Universe
                        .frameForEnvironment(environment), combo, "Create product automaton",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null);
        if (result != JOptionPane.YES_OPTION && result != JOptionPane.OK_OPTION)
            return;

        FiniteStateAutomaton self = (FiniteStateAutomaton) environment.getAutomaton().clone();
        FiniteStateAutomaton other = (FiniteStateAutomaton) ((EnvironmentFrame) combo
                .getSelectedItem()).getEnvironment().getObject();

        FiniteStateAutomaton product = cartesianProduct(self, other);

        FrameFactory.createFrame(product);
    }

    /**
     * Creates a new automaton out of the product of two FSA
     *
     * @param a1, a2
     *            the two automata to create the product of
     */
    private FiniteStateAutomaton cartesianProduct(FiniteStateAutomaton a1, FiniteStateAutomaton a2) {
        FiniteStateAutomaton product = new FiniteStateAutomaton();
        HashMap<String, State> combinedStates = new HashMap<>();

        State[] states1 = a1.getStates();
        State[] states2 = a2.getStates();

        /* define a decent scale for placing states in the viewport */
        int scale = Math.min(Math.round(400 /states1.length ), Math.round(300 / states2.length));

        /* create all the product states */
        for (int i = 0; i < states1.length; i++) {
            for (int j = 0; j < states2.length; j++) {
                String productName = states1[i].getName() + ", " + states2[j].getName();
                State productState = product.createState(new Point(i * scale + 50, j * scale + 40));
                productState.setName(productName);
                if (isFinalState(a1.isFinalState(states1[i]), a2.isFinalState(states2[j]))) {
                    product.addFinalState(productState);
                }
                combinedStates.put(getProductId(states1[i], states2[j]), productState);
            }
        }

        boolean warningNeeded = false;

        /* create all the transitions */
        for (State s1 : states1) {
            HashSet<String> allLabels1 = new HashSet<>();
            for (State s2 : states2) {
                HashSet<String> allLabels2 = new HashSet<>();
                /* this combination of states represents one state in the product FSA */
                String fromId = getProductId(s1, s2);

                /* take note of all the possible labels in a2 from this state */
                for (Transition t2 : a2.getTransitionsFromState(s2)) {
                    String label2 = ((FSATransition) t2).getLabel();
                    allLabels2.add(label2);
                }

                /* product transitions */
                for (Transition t1 : a1.getTransitionsFromState(s1)) {
                    String label1 = ((FSATransition) t1).getLabel();
                    /* take note of all the possible labels in a1 from this state */
                    allLabels1.add(label1);

                    for (Transition t2 : a2.getTransitionsFromState(s2)) {
                        /* this combination of transitions represents one transition in the product FSA */
                        String label2 = ((FSATransition) t2).getLabel();
                        /* make sure these transitions have the same label or one is lambda*/
                        if (label1.equals(label2) || label1.equals("") || label2.equals("")) {
                            String label = label1;
                            String toId = getProductId(t1.getToState(), t2.getToState());
                            /* if it's a lambda transition one of the sub-states stays the same */
                            if (!label1.equals(label2)) {
                                label = "";
                                if (label1.equals("")) {
                                    toId = getProductId(t1.getToState(), s2);
                                } else {
                                    toId = getProductId(s1, t2.getToState());
                                }
                            }
                            /* create the actual transition */
                            Transition productTransition = new FSATransition(combinedStates.get(fromId), combinedStates.get(toId), label);
                            product.addTransition(productTransition);
                        }
                    }
                }

                /* add all one dimensional states/transitions that are needed */
                warningNeeded = true;

                /* automaton 1 x null */
                for (Transition t : a1.getTransitionsFromState(s1)) {
                    String label = ((FSATransition) t).getLabel();
                    State s = t.getToState();
                    if (!allLabels2.contains(label) || label.equals("")) {
                        createOneDimensionalAutomaton(s, true, product, a1, combinedStates, fromId, label);
                    }
                }

                /* null x automaton 2 */
                for (Transition t : a2.getTransitionsFromState(s2)) {
                    String label = ((FSATransition) t).getLabel();
                    State s = t.getToState();
                    if (!allLabels1.contains(label) || label.equals("")) {
                        createOneDimensionalAutomaton(s, false, product, a2, combinedStates, fromId, label);
                    }
                }
            }
        }

        /* warn them if 1D states had to be added */
        if (warningNeeded) {
            JOptionPane.showMessageDialog(Universe
                            .frameForEnvironment(environment),
                    "The automata are missing equivalent transitions from all states.");
        }

        /* set initial state */
        if (a1.getInitialState() == null || a2.getInitialState() == null) {
            JOptionPane.showMessageDialog(Universe
                            .frameForEnvironment(environment),
                    "All automata must have an initial state.");
        } else {
            String initialId = getProductId(a1.getInitialState(), a2.getInitialState());
            product.setInitialState(combinedStates.get(initialId));
        }


        return product;
    }

    /**
     * Adds states to the combined automaton that come from only one source automaton
     *
     * @param firstState
     *              the root from where the cloning should start
     *        firstDim
     *              whether this comes from the first automaton or not
     *        combinedAutomaton
     *              the work in progress product automaton
     *        subAutomaton
     *              the automaton to clone the states from
     *        allStates
     *              all of the states in the product automaton and their ids
     *        fromId
     *              the id of the state that this will splinter off from
     *        fromLabel
     *              the label for the transition that connects this subsection to the main product automaton
     */
    private void createOneDimensionalAutomaton(State firstState, boolean firstDim, FiniteStateAutomaton combinedAutomaton, FiniteStateAutomaton subAutomaton,
                                               HashMap<String, State> allStates, String fromId, String fromLabel) {
        /* create states with BFS */
        LinkedList<State> queue = new LinkedList<>();
        queue.add(firstState);
        while (queue.size() > 0) {
            State curr = queue.remove();
            String stateId = firstDim ? curr.getID() + "x" : "x" + curr.getID();
            /* this acts as a visited check but it also shows us if this state already exists */
            if (!allStates.containsKey(stateId)) {
                /* create the new 1D state */
                String stateName = firstDim ? curr.getName() + ", Ø" : "Ø, " + curr.getName();
                State state = combinedAutomaton.createState(randomPoint());
                state.setName(stateName);
                if (firstDim && isFinalState(subAutomaton.isFinalState(curr), false)) {
                    combinedAutomaton.addFinalState(state);
                } else if (!firstDim && isFinalState(false, subAutomaton.isFinalState(curr))) {
                    combinedAutomaton.addFinalState(state);
                }
                allStates.put(stateId, state);
                /* crawl to all of its neighbors */
                for (Transition t : subAutomaton.getTransitionsFromState(curr)) {
                    queue.addLast(t.getToState());
                }
            }
        }

        /* create transition linking this to the main product automaton */
        String firstId = firstDim ? firstState.getID() + "x" : "x" + firstState.getID();
        combinedAutomaton.addTransition(new FSATransition(allStates.get(fromId), allStates.get(firstId), fromLabel));

        /* create transitions with BFS again*/
        HashSet<State> visited = new HashSet<>();
        queue.add(firstState);
        while (queue.size() > 0) {
            State curr = queue.remove();
            String stateId = firstDim ? firstState.getID() + "x" : "x" + firstState.getID();
            State fromState = allStates.get(stateId);

            for (Transition t : subAutomaton.getTransitionsFromState(curr)) {
                State subDest = t.getToState();
                if (!visited.contains(subDest)) {
                    queue.addLast(subDest);
                    visited.add(subDest);
                    String destId = firstDim ? subDest.getID() + "x" : "x" + subDest.getID();
                    State dest = allStates.get(destId);
                    String label = ((FSATransition) t).getLabel();
                    /* it's possible that a transition like this already exists from a previous
                    call to this function but it's a hash map so it's fine */
                    combinedAutomaton.addTransition(new FSATransition(fromState, dest, label));
                }
            }
        }
    }

    private Point randomPoint() {
        Random r = new Random();
        return new Point(r.nextInt(300) + 50, r.nextInt(200) + 40);
    }

    private String getProductId(State s1, State s2) {
        return s1.getID() + "x" + s2.getID();
    }

    protected boolean isFinalState(boolean final1, boolean final2) {
        return false;
    }

    private AutomatonEnvironment environment;
}
