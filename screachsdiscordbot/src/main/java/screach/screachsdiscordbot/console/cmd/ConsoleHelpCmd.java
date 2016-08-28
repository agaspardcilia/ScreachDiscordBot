package screach.screachsdiscordbot.console.cmd;

import screach.screachsdiscordbot.console.ConsoleCommand;
import screach.screachsdiscordbot.console.MainConsole;

public class ConsoleHelpCmd implements ConsoleCommand {
	private MainConsole mainConsole;
	
	public ConsoleHelpCmd(MainConsole mainConsole) {
		this.mainConsole = mainConsole;
	}
	
	
	public String getName() {
		return "Help";
	}

	public String getDescription() {
		return "Displays commands usage.";
	}

	public String getUsage() {
		return "help";
	}

	public String getCommand() {
		return "help";
	}



	@Override
	public void exec(String[] args) {
		String result = "Help\n";
		result += "<argument> : required, [argument] : optionnal\n\n";
		
		for (ConsoleCommand consoleCommand : mainConsole.getCommands()) {
			result += consoleCommand.getUsage() + " : " + consoleCommand.getDescription() + "\n";
		}
		
		
			System.out.println(result);
	}

}
