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

import automata.State;
import automata.Transition;
import automata.fsa.FSATransition;
import automata.Automaton;
import gui.environment.Environment;

/**
 * This action removes some of the unnecessary states of an automaton
 *
 * @author Elijah Cirioli
 */
public class RemoveUnnecessaryStatesAction extends AutomatonAction {
    /**
     * The automaton for which the states will be removed
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
     *     the automaton for which the states will be removed
     * @param e
     *     the environment this automaton is in.
     */
    public RemoveUnnecessaryStatesAction(String s, Automaton a, Environment e) {
        super(s, null);
        automaton = a;
        environment = e;
    }

    /**
     * Remove states that only exist to lambda to one other state
     */
    public void actionPerformed(ActionEvent e) {
        State[] states = automaton.getStates();
        for (State s : states) {
            Transition[] outTransitions = automaton.getTransitionsFromState(s);
            Transition[] inTransitions = automaton.getTransitionsToState(s);
            if (outTransitions.length == 0 && inTransitions.length == 0) {
                /* this state is floating */
                automaton.removeState(s);
            } else if (outTransitions.length == 1 && !automaton.isFinalState(s) && !automaton.isInitialState(s)) {
                FSATransition out = (FSATransition) outTransitions[0];
                if (out.getLabel().equals("")) {
                    /* the state only has one place to go and it's a lambda */
                    State destination = out.getToState();

                    /* if it has no in-transitions we'll let it live */
                    if (inTransitions.length == 0) {
                        continue;
                    }
                    /* add transitions to skip over s */
                    for (Transition in : inTransitions) {
                        Transition bypassTransition = new FSATransition(in.getFromState(), destination, ((FSATransition) in).getLabel());
                        automaton.addTransition(bypassTransition);
                    }
                    /* remove s */
                    automaton.removeState(s);
                }
            }
        }
    }
}
