package hu.bme.mit.yakindu.analysis.workhere;

import java.io.IOException;
import java.util.Scanner;

import hu.bme.mit.yakindu.analysis.RuntimeService;
import hu.bme.mit.yakindu.analysis.TimerService;
import hu.bme.mit.yakindu.analysis.example.ExampleStatemachine;
import hu.bme.mit.yakindu.analysis.example.IExampleStatemachine;



public class RunStatechart {
	
	public static void main(String[] args) throws IOException {
		ExampleStatemachine s = new ExampleStatemachine();
		s.setTimer(new TimerService());
		RuntimeService.getInstance().registerStatemachine(s, 200);
		s.init();
		s.enter();
		s.runCycle();
		/*print(s);
		s.raiseStart();
		s.runCycle();
		System.in.read();
		s.raiseWhite();
		s.runCycle();
		print(s);
		System.exit(0); */
		Scanner scanner = new Scanner(System.in);
		while(true) {
			System.out.println("Waiting for input...");
			
			String input = scanner.nextLine();
			
			switch(input) {
				case "start": s.raiseStart();
					break;
				case "white": s.raiseWhite();
					break;
				case "black": s.raiseBlack();
					break;
				case "exit":
					scanner.close();
					System.exit(0);
				default:
					System.out.println("Unkown command");
					break;
			}
			s.runCycle();
			
			print(s);
		}
	}

	public static void print(IExampleStatemachine s) {
		System.out.println("W = " + s.getSCInterface().getWhiteTime());
		System.out.println("B = " + s.getSCInterface().getBlackTime());
	}
}
