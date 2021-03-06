package hu.bme.mit.yakindu.analysis.workhere;

import java.io.IOException;
import java.util.ListIterator;
import java.util.Scanner;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.junit.Test;
import org.yakindu.base.types.Event;
import org.yakindu.base.types.Property;
import org.yakindu.sct.model.sgraph.Scope;
import org.yakindu.sct.model.sgraph.State;
import org.yakindu.sct.model.sgraph.Statechart;
import org.yakindu.sct.model.sgraph.Transition;
import org.yakindu.sct.model.stext.stext.VariableDefinition;

import hu.bme.mit.model2gml.Model2GML;
import hu.bme.mit.yakindu.analysis.RuntimeService;
import hu.bme.mit.yakindu.analysis.TimerService;
import hu.bme.mit.yakindu.analysis.example.ExampleStatemachine;
import hu.bme.mit.yakindu.analysis.example.IExampleStatemachine;
import hu.bme.mit.yakindu.analysis.modelmanager.ModelManager;

public class Main {
	@Test
	public void test() {
		main(new String[0]);
	}
	
	public static void main(String[] args) {
		ModelManager manager = new ModelManager();
		Model2GML model2gml = new Model2GML();

		// Loading model
		EObject root = manager.loadModel("model_input/example.sct");
		
		// Reading model
		Statechart s = (Statechart) root;
		TreeIterator<EObject> iterator = s.eAllContents();
		
		
		/*
		SearchEmptyName(s.eAllContents());
		
		while (iterator.hasNext()) {
			EObject content = iterator.next();
			if(content instanceof State) {
				State state = (State) content;
				
				System.out.println(state.getName());
				EList<Transition> vertexes = state.getOutgoingTransitions();
				
				if(vertexes.isEmpty()) {
					System.out.println(state.getName() + " is a trap state");
				}
				
				ListIterator<Transition> iter = vertexes.listIterator();
				
				while(iter.hasNext()) {
					Transition nextState = iter.next();
					System.out.print(state.getName()); System.out.print(" -> "); System.out.println(nextState.getTarget().getName());
				}
			}
		}*/
		
		CodeGen(s);
		
		// Transforming the model into a graph representation
		String content = model2gml.transform(root);
		// and saving it
		manager.saveFile("model_output/graph.gml", content);
	}
	
	private static void Bejar(Statechart s) {
		EList<Scope> scopes = s.getScopes();
		
		ListIterator<Scope> iterator = scopes.listIterator();
		
		System.out.println("public static void print(IExampleStatemachine s) {");
		while(iterator.hasNext()) {
			Scope scope = iterator.next();
			
			scope.getVariables().forEach(x -> System.out.println("\tSystem.out.println(\""+x.getName()+" = \" + s.getSCInterface().get" + toCamelCase(x.getName()) +"());"));
			
			System.out.println("//Events:");
			scope.getEvents().forEach(event -> System.out.println("\t//"+event.getName()));
		}
		System.out.println("}");
	}

	private static String toCamelCase(String s) {
		char kezdo = s.toUpperCase().charAt(0);
		
		return kezdo + s.substring(1);
	}
	
	private static void SearchEmptyName(TreeIterator<EObject> iterator) {
		int counter = 0;
		while(iterator.hasNext()) {
			EObject content = iterator.next();
			
			if(content instanceof State) {
				counter++;
				
				State state = (State)content;
				
				state.getIncomingTransitions();
				
				if(state.getName() == "" || state.getName() == "<name>") {
					state.setName("State " + counter);
				}
			}
		}
	}
	
	private static void CodeGen(Statechart s) {
		String main = "public static void main(String[] args) throws IOException {\n"
				+ "ExampleStatemachine s = new ExampleStatemachine();\n"
				+ "s.setTimer(new TimerService());\n"
				+ "RuntimeService.getInstance().registerStatemachine(s, 200);\n"
				+ "s.init();\n"
				+ "s.enter();\n"
				+ "s.runCycle();\n\n"
				+ "Scanner scanner = new Scanner(System.in);\n"
				+ "while(true) {\n"
				+ "System.out.println(\"Waiting for input...\");\n"
				+ "String input = scanner.nextLine();\n"
				+ "switch(input) {\n";
		
		String pCode = "public static void print(IExampleStatemachine s) {\n";
		
		EList<Scope> scopes = s.getScopes();
		
		ListIterator<Scope> iterator = scopes.listIterator();
		
		while(iterator.hasNext()) {
			Scope scope = iterator.next();
			
			for(Property p : scope.getVariables()) {
				pCode = pCode + String.format("\tSystem.out.println(\"%s = \" + s.getSCInterface().get%s());\n", 
						p.getName(), toCamelCase(p.getName()));
			}
			
			
			for(Event e : scope.getEvents()) {
				main = main + String.format("case \"%s\":\n\ts.raise%s();\nbreak;\n", e.getName(), toCamelCase(e.getName()));
			}
		}
		
		pCode = pCode + "}\n";
		
		main = main + "case \"exit\": \nscanner.close();\nSystem.exit(0);\ndefault:\nSystem.out.println(\"Unknown event!\");\n}\n" + 
				"s.runCycle();\r\n" + 
				"\n" + 
				"print(s);\n" + 
				"}\n" + 
				"}\n";
		
		System.out.println(String.format("public class GeneratedCode {\n"
				+ "%s\n%s\n}", main, pCode));
	}
}
