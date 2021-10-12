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

import automata.Automaton;
import automata.graph.AutomatonGraph;
import automata.graph.LayoutAlgorithmFactory;
import gui.viewer.StateDrawer;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * The <CODE>VertexColorAction</CODE> is the action to change the color of all vertices
 * @author Elijah Cirioli
 */

public class VertexColorAction extends AutomatonAction {
    private Color primaryColor;
    private Color secondaryColor;
    private Automaton automaton;

    /**
     * Instantiates a new <CODE>VertexColorAction</CODE>.
     *
     * @param string
     *            a string description
     * @param color
     *            the color to change the vertices to
     */
    public VertexColorAction(String string, Automaton a, Color c) {
        super(string, null);
        primaryColor = c;
        secondaryColor = new Color(0, 0, 0); /* default secondary color to black */
        automaton = a;
    }

    public VertexColorAction(String string, Automaton a, Color pc, Color sc) {
        super(string, null);
        primaryColor = pc;
        secondaryColor = sc;
        automaton = a;
    }

    public void actionPerformed(ActionEvent e) {
        StateDrawer.STATE_COLOR = primaryColor;
        StateDrawer.SECONDARY_STATE_COLOR = secondaryColor;
        /* a hacky workaround to force a redraw */
        AutomatonGraph graph = LayoutAlgorithmFactory.getAutomatonGraph(0, automaton);
        graph.moveAutomatonStates();
    }
}
