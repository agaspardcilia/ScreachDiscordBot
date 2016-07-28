package screach.screachsdiscordbot.handlers;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

public interface MessageHandler {
	
	public String getName();
	
	public String getDescription();
	
	public String getUsage();
	
	public String getCommand();
	
	public void handleMessage(MessageReceivedEvent event, String[] args);
	
	
}
