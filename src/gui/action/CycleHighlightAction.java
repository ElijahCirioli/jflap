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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;

import automata.State;
import automata.Transition;
import automata.Automaton;
import automata.graph.Graph;
import gui.editor.ArrowDisplayOnlyTool;
import gui.environment.Environment;
import gui.environment.Universe;
import gui.environment.tag.CriticalTag;
import gui.viewer.AutomatonPane;
import gui.viewer.SelectionDrawer;

import javax.swing.*;

/**
 * This action tells you whether an automaton is weakly connected
 *
 * @author Elijah Cirioli
 */
public class CycleHighlightAction extends AutomatonAction {
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
    public CycleHighlightAction(String s, Automaton a, Environment e) {
        super(s, null);
        automaton = a;
        environment = e;
    }

    /**
     * Highlight all cycles that exist within the graph.
     * The time complexity on this is pretty bad but it let
     * me get an exact list of all vertices in the cycle
     * without much additional effort
     */
    public void actionPerformed(ActionEvent e) {
        State[] states = automaton.getStates();
        if (states.length == 0)
            return;


        SelectionDrawer cyclicStates = new SelectionDrawer(automaton);
        for (State v : states) {
            if (pathExistsToSelf(v))
                cyclicStates.addSelected(v);
        }

        /* Put the drawer in the environment */
        CyclePane pane = new CyclePane(new AutomatonPane(cyclicStates));
        environment.add(pane, "Cycles", new CriticalTag() {
        });
        environment.setActive(pane);
    }

    /**
     * BFS to see if a path exists back to starting vertex
    */
    private boolean pathExistsToSelf(State v) {
        HashSet<State> visited = new HashSet<>();
        LinkedList<State> queue = new LinkedList<>();
        queue.add(v);
        while (queue.size() > 0) {
            State curr = queue.remove();
            if (curr.equals(v) && visited.contains(curr))
                return true;
            for (Transition t : automaton.getTransitionsFromState(curr)) {
                State u = t.getToState();
                if (!visited.contains(u)) {
                    visited.add(u);
                    queue.addLast(u);
                }
            }
        }
        return false;
    }

    /**
     * A class that exists to make integration with the help system feasible.
     */
    private class CyclePane extends JPanel {
        public CyclePane(AutomatonPane ap) {
            super(new BorderLayout());
            add(ap, BorderLayout.CENTER);
            add(new JLabel("Cyclic states are highlighted."),
                    BorderLayout.NORTH);
            ArrowDisplayOnlyTool tool = new ArrowDisplayOnlyTool(ap, ap
                    .getDrawer());
            ap.addMouseListener(tool);
        }
    }
}
