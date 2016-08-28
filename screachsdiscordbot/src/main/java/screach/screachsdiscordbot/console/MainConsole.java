package screach.screachsdiscordbot.console;

import java.util.ArrayList;
import java.util.Scanner;

import screach.screachsdiscordbot.console.cmd.ConsoleHelpCmd;
import screach.screachsdiscordbot.console.cmd.StopCmd;

public class MainConsole implements Runnable {
	private volatile boolean isActive;
	
	private ArrayList<ConsoleCommand> commands;
	private Scanner scanner;
	
	public MainConsole() {
		commands = new ArrayList<>();
		isActive = true;
	}
	
	public void setActive() {
		isActive = true;
	}
	
	public void setInactive() {
		isActive = false;
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public void addCommand(ConsoleCommand command) {
		commands.add(command);
	}
	
	public ArrayList<ConsoleCommand> getCommands() {
		return commands;
	}
	
	@Override
	public void run() {
		String input;
		scanner = new Scanner(System.in);
		
		while(isActive) {
			System.out.print("->");
			input = scanner.nextLine();
			execCommand(input);
		}
		
		scanner.close();
		
		System.out.println("Console closed, bye.");
	}
	
	private void execCommand(String input) {
		String[] args = input.split(" ");
		String cmd;
		
		if (args.length <= 0 || input.equals("")) {
			return;
		} 
		
		cmd = args[0];
		
		for (ConsoleCommand consoleCommand : commands) {
			if (consoleCommand.getCommand().equals(cmd)) {
				consoleCommand.exec(args);
				return;
			}
				
		}
		
		System.out.println("Unknown command " + cmd + ".");
		
	}
	
	
	public static void main(String[] args) {
		MainConsole mc = new MainConsole();
		mc.addCommand(new StopCmd(mc));
		mc.addCommand(new ConsoleHelpCmd(mc));
		
		Thread t  = new Thread(mc);
		t.start();
	}

}
