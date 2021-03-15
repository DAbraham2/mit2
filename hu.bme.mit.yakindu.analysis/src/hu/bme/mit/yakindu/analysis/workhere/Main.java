package hu.bme.mit.yakindu.analysis.workhere;

import java.util.ListIterator;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.junit.Test;
import org.yakindu.sct.model.sgraph.State;
import org.yakindu.sct.model.sgraph.Statechart;
import org.yakindu.sct.model.sgraph.Transition;

import hu.bme.mit.model2gml.Model2GML;
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
		}
		
		// Transforming the model into a graph representation
		String content = model2gml.transform(root);
		// and saving it
		manager.saveFile("model_output/graph.gml", content);
	}
	
	private static void SearchEmptyName(TreeIterator<EObject> iterator) {
		int counter = 0;
		while(iterator.hasNext()) {
			EObject content = iterator.next();
			if(content instanceof State) {
				counter++;
				
				State state = (State)content;
				
				if(state.getName() == "" || state.getName() == "<name>") {
					state.setName("State " + counter);
				}
			}
		}
	}
}
