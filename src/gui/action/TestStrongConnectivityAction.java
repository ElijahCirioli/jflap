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

import java.awt.event.ActionEvent;
import java.util.*;

import automata.State;
import automata.Transition;
import automata.Automaton;
import automata.graph.Graph;
import gui.environment.Environment;

import javax.swing.*;

/**
 * This action tells you whether an automaton is strongly connected
 *
 * @author Elijah Cirioli
 */
public class TestStrongConnectivityAction extends AutomatonAction {
    /**
     * The automaton we will be testing
     */
    private Automaton automaton;
    /**
     * The environment in which the automaton is placed.
     */
    private Environment environment;

    /**
     * Constructor.
     *
     * @param s
     *     the title of this action.
     * @param a
     *     the automaton this action will be testing
     * @param e
     *     the environment this automaton is in.
     */
    public TestStrongConnectivityAction(String s, Automaton a, Environment e) {
        super(s, null);
        automaton = a;
        environment = e;
    }

    /**
     * Check if a path exists between every pair of states
     */
    public void actionPerformed(ActionEvent e) {
        State[] states = automaton.getStates();
        if (states.length < 2) {
            JOptionPane.showMessageDialog(environment,
                    "Not enough states are present.", "Testing Connectivity",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        /* start at every state and see if it can get to every other state */
        for (State u : states) {
            HashSet<State> visited = new HashSet<>();
            LinkedList<State> queue = new LinkedList<>();
            visited.add(u);
            queue.add(u);
            while (queue.size() > 0) {
                State curr = queue.remove();
                ArrayList<State> neighborhood = new ArrayList<>();
                for (Transition t : automaton.getTransitionsFromState(curr))
                    neighborhood.add(t.getToState());
                for (State v : neighborhood) {
                    if (!visited.contains(v)) {
                        visited.add(v);
                        queue.addLast(v);
                    }
                }
            }

            if (visited.size() != states.length) {
                JOptionPane.showMessageDialog(environment,
                        "The graph is not strongly connected", "Testing Connectivity",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        /* we haven't found any place without a connection so we must be strongly connected */
        JOptionPane.showMessageDialog(environment,
                "The graph is strongly connected", "Testing Connectivity",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
