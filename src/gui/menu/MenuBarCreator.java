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





package gui.menu;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.io.File;
import java.io.IOException;

import java.lang.reflect.*;
import java.util.jar.*;

import automata.fsa.FSATransition;
import grammar.Grammar;
import grammar.TuringChecker;
import gui.environment.Environment;
import gui.environment.AutomatonEnvironment;
import gui.environment.EnvironmentFrame;
import gui.environment.Universe;
import gui.action.*;
import automata.Automaton;
import automata.graph.LayoutAlgorithmFactory;
import automata.graph.layout.VertexMover;

/**
 * The <CODE>MenuBarCreator</CODE> is a creator of the menu bars for the FLAP
 * application.
 * 
 * @author Thomas Finley
 */

public class MenuBarCreator {
	/**
	 * Instantiates the menu bar.
	 * 
	 * @param frame
	 *            the environment frame that holds the environment and object
	 * @return the menu bar appropriate for the environment
	 */
	public static JMenuBar getMenuBar(EnvironmentFrame frame) {
		JMenuBar bar = new JMenuBar();
		JMenu menu;

		menu = getFileMenu(frame);
		if (menu.getItemCount() > 0)
			bar.add(menu);

		menu = getInputMenu(frame);
		if (menu.getItemCount() > 0)
			bar.add(menu);

		menu = getTestMenu(frame);
		if (menu.getItemCount() > 0)
			bar.add(menu);
		
		menu = getViewMenu(frame);
		if (menu.getItemCount() > 0)
			bar.add(menu);

		menu = getConvertMenu(frame);
		if (menu.getItemCount() > 0)
			bar.add(menu);

		menu = getHelpMenu(frame);
		if (menu.getItemCount() > 0)
			bar.add(menu);

        CloseButton dismiss = new CloseButton(frame.getEnvironment());
        bar.add(Box.createGlue());
        bar.add(dismiss);
        
		return bar;
	}
	
	/**
	 * Special case to deal with turing converted grammar
	 * @param frame
	 * @param isTuring
	 * @return
	 */
	public static JMenuBar getMenuBar(EnvironmentFrame frame, int isTuring) {
		JMenuBar bar = new JMenuBar();
		JMenu menu;

		menu = getFileMenu(frame);
		if (menu.getItemCount() > 0)
			bar.add(menu);

		menu = getInputMenu(frame, isTuring);
		if (menu.getItemCount() > 0)
			bar.add(menu);

		menu = getTestMenu(frame);
		if (menu.getItemCount() > 0)
			bar.add(menu);
		
		menu = getViewMenu(frame);
		if (menu.getItemCount() > 0)
			bar.add(menu);

		menu = getConvertMenu(frame, isTuring);
		if (menu.getItemCount() > 0)
			bar.add(menu);

		menu = getHelpMenu(frame);
		if (menu.getItemCount() > 0)
			bar.add(menu);

        CloseButton dismiss = new CloseButton(frame.getEnvironment());
        bar.add(Box.createGlue());
        bar.add(dismiss);
        
		return bar;
	}

	/**
	 * Adds an action to a menu with the accelerator key set.
	 * 
	 * @param menu
	 *            the menu to add the action to
	 * @param a
	 *            the action to create the menu item for
	 */
	public static void addItem(JMenu menu, Action a) {
		JMenuItem item = new JMenuItem(a);
		item.setAccelerator((KeyStroke) a.getValue(Action.ACCELERATOR_KEY));
		menu.add(item);
	}

	/**
	 * Instantiates the file menu.
	 * 
	 * @param frame
	 *            the environment frame that holds the environment and object
	 * @return a file menu
	 */
	private static JMenu getFileMenu(EnvironmentFrame frame) {
		Environment environment = frame.getEnvironment();
		JMenu menu = new JMenu("File");
		addItem(menu, new NewAction());
		SecurityManager sm = System.getSecurityManager();
		if (Universe.CHOOSER != null) {
			// Can't open and save files.
			addItem(menu, new OpenAction());
			addItem(menu, new SaveAction(environment));
			addItem(menu, new SaveAsAction(environment));
			JMenu saveImageMenu;
			saveImageMenu = new JMenu("Save Image As...");
			saveImageMenu.add(new SaveGraphJPGAction(environment, menu));
			saveImageMenu.add(new SaveGraphPNGAction(environment, menu));
			saveImageMenu.add(new SaveGraphGIFAction(environment, menu));
			saveImageMenu.add(new SaveGraphBMPAction(environment, menu));
            if (environment instanceof AutomatonEnvironment){ //this is strictly for non-Grammar
                JarFile jar = null;
                try{
                    if (new File("JFLAP.jar").exists()) jar = new JarFile("JFLAP.jar");
                    else if (new File("JFLAP_With_Source.jar").exists()) jar = new JarFile("JFLAP_With_Source.jar");
                }
                catch (IOException ioe){
                    ioe.printStackTrace();
                }

                if (new File("svg.jar").exists() || (jar != null && jar.getJarEntry("org/foo.txt") != null)){
                    //                saveImageMenu.add(new ExportAction(environment));
                    try{
                        RestrictedAction ra = (RestrictedAction) Class.forName("gui.action.ExportAction").getConstructor(new Class[]{Environment.class}).newInstance(environment);
                        saveImageMenu.add(ra);
                    }catch(Exception e){
                        e.printStackTrace();
                        System.err.println("Cannot make menu");
                    }
                }
            }

			menu.add(saveImageMenu);
		}
		else{
			addItem(menu, new OpenURLAction());
		}
		addItem(menu, new CloseAction(environment));
		addItem(menu, new CloseWindowAction(frame));
		try {
			if (sm != null)
				sm.checkPrintJobAccess();
			addItem(menu, new PrintAction(environment));
		} catch (SecurityException e) {
			// Damn. Can't print!
		}
		try {
			if (sm != null)
				sm.checkExit(0);
			addItem(menu, new QuitAction());
		} catch (SecurityException e) {
			// Well, can't exit anyway.
		}

//        if (environment instanceof AutomatonEnvironment){
//            addItem(menu, new SetUndoAmountAction());
//        }


		return menu;
	}

	/**
	 * Instantiates the menu that holds input related menu events.
	 * 
	 * @param frame
	 *            the environment frame that holds the environment and object
	 * @return an input menu
	 */
	private static JMenu getInputMenu(EnvironmentFrame frame) {
		Environment environment = frame.getEnvironment();
		JMenu menu = new JMenu("Input");
		Serializable object = environment.getObject();
		if (SimulateAction.isApplicable(object))
			addItem(menu, new SimulateAction((Automaton) object, environment));
		if (BuildingBlockSimulateAction.isApplicable(object))
			addItem(menu, new BuildingBlockSimulateAction((Automaton) object,
					environment));
		if (SimulateNoClosureAction.isApplicable(object))
			addItem(menu, new SimulateNoClosureAction((Automaton) object,
					environment));
		if (NoInteractionSimulateAction.isApplicable(object))
			addItem(menu, new NoInteractionSimulateAction((Automaton) object,
					environment));
		if (MultipleSimulateAction.isApplicable(object))
			addItem(menu, new MultipleSimulateAction((Automaton) object,
					environment));
		if (MultipleOutputSimulateAction.isApplicable(object))
			addItem(menu, new MultipleOutputSimulateAction((Automaton) object,
					environment));
		/*
		 * if (GrammarOutputAction.isApplicable(object)) addItem(menu, new
		 * GrammarOutputAction ((gui.environment.GrammarEnvironment)
		 * environment));
		 */

		boolean isTuring=TuringChecker.check(object);
		if (isTuring)
			return getInputMenu(frame, 0);
		
		// Grammar-y actions.
		if (LLParseTableAction.isApplicable(object))
			addItem(menu, new LLParseTableAction(
					(gui.environment.GrammarEnvironment) environment));
		if (LRParseTableAction.isApplicable(object))
			addItem(menu, new LRParseTableAction(
					(gui.environment.GrammarEnvironment) environment));
		if (BruteParseAction.isApplicable(object))
			addItem(menu, new BruteParseAction(
					(gui.environment.GrammarEnvironment) environment));
		if (MultipleBruteParseAction.isApplicable(object))
			addItem(menu, new MultipleBruteParseAction(
					(gui.environment.GrammarEnvironment) environment));
		if (UserControlParseAction.isApplicable(object))
			addItem(menu, new UserControlParseAction(
					(gui.environment.GrammarEnvironment) environment));
		if (CYKParseAction.isApplicable(object))
			addItem(menu, new CYKParseAction(
					(gui.environment.GrammarEnvironment) environment));
		if (MultipleCYKParseAction.isApplicable(object))
			addItem(menu, new MultipleCYKParseAction(
					(gui.environment.GrammarEnvironment) environment));
//		if (TuringBruteParseAction.isApplicable(object))
//			addItem(menu, new TuringBruteParseAction(
//					(gui.environment.GrammarEnvironment) environment));
		
		// LSystem-y actions.

		if (LSystemDisplay.isApplicable(object))
			addItem(menu, new LSystemDisplay(
					(gui.environment.LSystemEnvironment) environment));

		return menu;
	}

	/**
	 * Get input menu for turing converted grammar
	 * @param frame
	 * @param specialCaseForTuringConverted
	 * @return
	 */
	private static JMenu getInputMenu(EnvironmentFrame frame, int specialCaseForTuringConverted) {
		Environment environment = frame.getEnvironment();
		JMenu menu = new JMenu("Input");
		Serializable object = environment.getObject();
		if (UserControlParseAction.isApplicable(object))
			addItem(menu, new UserControlParseAction(
					(gui.environment.GrammarEnvironment) environment));
		if (TuringBruteParseAction.isApplicable(object))
			addItem(menu, new TuringBruteParseAction(
					(gui.environment.GrammarEnvironment) environment));
		

		return menu;
	}

	
	/**
	 * Instantiates the menu holding events concerning the manipulation of object
	 * positions in the window.
	 * 
	 * @param frame
	 *            the environment frame that holds the environment and object
	 * @return a view menu
	 */
	private static JMenu getViewMenu(EnvironmentFrame frame) {
		Environment environment = frame.getEnvironment();
		JMenu menu = new JMenu("View");
		Serializable object = environment.getObject();
		if (AutomatonAction.isApplicable(object)) {
			Automaton automaton = (Automaton) object;
			LayoutStorageAction store = new LayoutStorageAction("Save Current Graph Layout", 
					"Restore Saved Graph Layout", automaton);			
			menu.add(store);			
			menu.add(store.getRestoreAction());
			
			
			JMenu viewMenu, subMenu;
			viewMenu = new JMenu("Vertex Color");
			viewMenu.add(new VertexColorAction("Yellow", automaton, new Color(255, 255, 150), new Color(252, 170, 76)));
			viewMenu.add(new VertexColorAction("Blue", automaton, new Color(115, 180, 255), new Color(89, 238, 255)));
			viewMenu.add(new VertexColorAction("Green",  automaton, new Color(107, 255, 144), new Color(79, 255, 238)));
			viewMenu.add(new VertexColorAction("Red",  automaton, new Color(255, 105, 82), new Color(255, 169, 64)));
			viewMenu.add(new VertexColorAction("Black",  automaton, new Color(43, 43, 43), new Color(90, 100, 112), new Color(196, 196, 196)));
			menu.add(viewMenu);

			viewMenu = new JMenu("Move Vertices");
			subMenu = new JMenu("Reflect Across Line...");
			subMenu.add(new LayoutAlgorithmAction("Horizontal Line Through Center", automaton, 
					environment, VertexMover.HORIZONTAL_CENTER));
			subMenu.add(new LayoutAlgorithmAction("Vertical Line Through Center", automaton, 
					environment, VertexMover.VERTICAL_CENTER));
			subMenu.add(new LayoutAlgorithmAction("Diagonal From Upper-Left To Lower-Right", automaton, 
					environment, VertexMover.POSITIVE_SLOPE_DIAGONAL));
			subMenu.add(new LayoutAlgorithmAction("Diagonal From Lower-Left To Upper-Right", automaton, 
					environment, VertexMover.NEGATIVE_SLOPE_DIAGONAL));
			viewMenu.add(subMenu);
			viewMenu.add(new LayoutAlgorithmAction("Rotate The Graph", automaton, 
					environment, VertexMover.ROTATE));
			viewMenu.add(new LayoutAlgorithmAction("Fill Screen With Graph", automaton, 
					environment, VertexMover.FILL));
			menu.add(viewMenu);
			
			menu.add(new LayoutAlgorithmAction("Apply A Random Layout Algorithm", automaton, 
					environment, LayoutAlgorithmFactory.RANDOM_CHOICE));
			viewMenu = new JMenu("Apply A Specific Layout Algorithm");
			viewMenu.add(new LayoutAlgorithmAction("Circle", automaton, 
					environment, LayoutAlgorithmFactory.CIRCLE));
			viewMenu.add(new LayoutAlgorithmAction("GEM", automaton, 
					environment, LayoutAlgorithmFactory.GEM));
			viewMenu.add(new LayoutAlgorithmAction("Random", automaton, 
					environment, LayoutAlgorithmFactory.RANDOM));
			viewMenu.add(new LayoutAlgorithmAction("Spiral", automaton, 
					environment, LayoutAlgorithmFactory.SPIRAL));
			subMenu = new JMenu("Tree");
			subMenu.add(new LayoutAlgorithmAction("Degree", automaton,
					environment, LayoutAlgorithmFactory.TREE_DEGREE));
			subMenu.add(new LayoutAlgorithmAction("Hierarchy", automaton,
					environment, LayoutAlgorithmFactory.TREE_HIERARCHY));
			viewMenu.add(subMenu);
			viewMenu.add(new LayoutAlgorithmAction("Two Circle", automaton, 
					environment, LayoutAlgorithmFactory.TWO_CIRCLE));
			viewMenu.add(new LayoutAlgorithmAction("Force-Directed", automaton,
					environment, LayoutAlgorithmFactory.FORCE_DIRECTED));
			menu.add(viewMenu);
//			menu.add(new StateColorSelector(automaton,environment,menu));
		}
		return menu;
	}
	
	/**
	 * This is the fun test menu for those that wish to run tests.
	 * 
	 * @param frame
	 *            the environment frame that holds the tests
	 * @return a test menu
	 */
	private static JMenu getTestMenu(EnvironmentFrame frame) {
		Environment environment = frame.getEnvironment();
		JMenu menu = new JMenu("Test");
		Serializable object = environment.getObject();

		if (DFAEqualityAction.isApplicable(object))
			addItem(menu, new DFAEqualityAction(
					(automata.fsa.FiniteStateAutomaton) object, environment));

		/*
		 * if (MinimizeAction.isApplicable(object)) addItem(menu, new
		 * MinimizeAction ((automata.fsa.FiniteStateAutomaton) object,
		 * environment));
		 */
		if (NondeterminismAction.isApplicable(object))
			addItem(menu, new NondeterminismAction((automata.Automaton) object,
					environment));
		/*
		 * if (UnnecessaryAction.isApplicable(object)) addItem(menu, new
		 * UnnecessaryAction ((automata.Automaton) object, environment));
		 */
		if (LambdaHighlightAction.isApplicable(object))
			addItem(menu, new LambdaHighlightAction(
					(automata.Automaton) object, environment));

		/*
		  if (GrammarTestAction.isApplicable(object)) addItem(menu, new
		  GrammarTestAction ((gui.environment.GrammarEnvironment)
		  environment));
		 */

		if (GrammarTypeTestAction.isApplicable(object))
			addItem(menu, new GrammarTypeTestAction ((gui.environment.GrammarEnvironment) environment));

		if (AutomatonAction.isApplicable(object)) {
			Automaton automaton = (Automaton) object;
			addItem(menu,new CycleHighlightAction("Highlight Cycles", automaton, environment));

			JMenu subMenu = new JMenu("Test Connectivity");
			addItem(subMenu, new TestWeakConnectivityAction("Weakly Connected", automaton, environment));
			addItem(subMenu, new TestStrongConnectivityAction("Strongly Connected", automaton, environment));
			menu.add(subMenu);
	}

		return menu;
	}

	/**
	 * This is the menu for doing conversions.
	 * 
	 * @param frame
	 *            the environment frame that holds the conversion items
	 * @return the conversion menu
	 */
	private static JMenu getConvertMenu(EnvironmentFrame frame) {
		Environment environment = frame.getEnvironment();
		JMenu menu = new JMenu("Convert");
		Serializable object = environment.getObject();

		boolean isTuring=TuringChecker.check(object);
		if (isTuring)
			return getConvertMenu(frame, 0);
		
		if (NFAToDFAAction.isApplicable(object))
			addItem(menu, new NFAToDFAAction(
					(automata.fsa.FiniteStateAutomaton) object, environment));
		if (MinimizeTreeAction.isApplicable(object))
			addItem(menu, new MinimizeTreeAction(
					(automata.fsa.FiniteStateAutomaton) object, environment));

		if (ConvertFSAToGrammarAction.isApplicable(object))
			addItem(menu, new ConvertFSAToGrammarAction(
					(gui.environment.AutomatonEnvironment) environment));
		if (ConvertPDAToGrammarAction.isApplicable(object))
			addItem(menu, new ConvertPDAToGrammarAction(
					(gui.environment.AutomatonEnvironment) environment));

		if (ConvertFSAToREAction.isApplicable(object))
			addItem(menu, new ConvertFSAToREAction(
					(gui.environment.AutomatonEnvironment) environment));
		
		if (ConvertCFGLL.isApplicable(object))
			addItem(menu, new ConvertCFGLL(
					(gui.environment.GrammarEnvironment) environment));
		if (ConvertCFGLR.isApplicable(object))
			addItem(menu, new ConvertCFGLR(
					(gui.environment.GrammarEnvironment) environment));
		if (ConvertRegularGrammarToFSA.isApplicable(object))
			addItem(menu, new ConvertRegularGrammarToFSA(
					(gui.environment.GrammarEnvironment) environment));
		if (GrammarTransformAction.isApplicable(object))
			addItem(menu, new GrammarTransformAction(
					(gui.environment.GrammarEnvironment) environment));

		if (REToFSAAction.isApplicable(object))
			addItem(menu, new REToFSAAction(
					(gui.environment.RegularEnvironment) environment));

		if (CombineAutomaton.isApplicable(object))
			addItem(menu, new CombineAutomaton(
					(gui.environment.AutomatonEnvironment) environment));

		if (FSAAction.isApplicable(object)) {
			JMenu subMenu = new JMenu("Cross Product");
			addItem(subMenu, new UnionProductAction((gui.environment.AutomatonEnvironment) environment));
			addItem(subMenu, new IntersectionProductAction((gui.environment.AutomatonEnvironment) environment));
			addItem(subMenu, new DifferenceProductAction((gui.environment.AutomatonEnvironment) environment));
			menu.add(subMenu);
		}


		if (TuringToUnrestrictGrammarAction.isApplicable(object))
			addItem(menu, new TuringToUnrestrictGrammarAction(
					(gui.environment.AutomatonEnvironment) environment));


		if (AddTrapStateToDFAAction.isApplicable(object))
			addItem(menu, new AddTrapStateToDFAAction(
					(gui.environment.AutomatonEnvironment) environment));

		if (FSAAction.isApplicable(object)) {
			Automaton automaton = (Automaton) object;
			addItem(menu, new RemoveUnnecessaryStatesAction("Remove Unnecessary States", automaton, environment));
		}

		return menu;
	}
	
	/**
	 * Special convert menu for grammar converted from TM
	 * @param frame
	 * @param specialCaseForTuringConverted
	 * @return
	 */
	private static JMenu getConvertMenu(EnvironmentFrame frame, int specialCaseForTuringConverted) {
		Environment environment = frame.getEnvironment();
		JMenu menu = new JMenu("Convert");
		Serializable object = environment.getObject();
		
		return menu;
	}

	/**
	 * This is the menu for help.
	 * 
	 * @param frame
	 *            the environment frame
	 * @return the help menu
	 */
	private static JMenu getHelpMenu(EnvironmentFrame frame) {
		Environment environment = frame.getEnvironment();
		JMenu menu = new JMenu("Help");
		Serializable object = environment.getObject();

		//Currently commented out, but can be restored if the help menus are fixed.
		//addItem(menu, new EnvironmentHelpAction(environment));
		
		//Temporary help action.
		addItem(menu, new AbstractAction("Help...") {
			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(null, "For help, feel free to access the JFLAP tutorial at\n" +
						"                          www.jflap.org.", "Help", JOptionPane.PLAIN_MESSAGE);
			}
		});
		
		addItem(menu, new AboutAction());

		return menu;
	}
}
