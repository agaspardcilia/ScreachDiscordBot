package screach.screachsdiscordbot.console;

public interface ConsoleCommand {
	
	public String getName();
	
	public String getDescription();
	
	public String getUsage();
	
	public String getCommand();
	
	public void exec(String[] args);

}
