package screach.screachsdiscordbot.console.cmd;

import screach.screachsdiscordbot.console.ConsoleCommand;
import screach.screachsdiscordbot.console.MainConsole;

public class StopCmd implements ConsoleCommand {
	private MainConsole mainConsole;
	
	public StopCmd(MainConsole mainConsole) {
		this.mainConsole = mainConsole;
	}
	
	@Override
	public String getName() {
		return "Stop";
	}

	@Override
	public String getDescription() {
		return "This command stops the bot.";
	}

	@Override
	public String getUsage() {
		return "stop";
	}

	@Override
	public String getCommand() {
		return "stop";
	}

	@Override
	public void exec(String[] args) {
		System.out.println("Stopping...");
		mainConsole.setInactive();
	}

}
