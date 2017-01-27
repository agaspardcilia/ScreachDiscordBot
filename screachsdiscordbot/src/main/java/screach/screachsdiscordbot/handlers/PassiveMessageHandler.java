package screach.screachsdiscordbot.handlers;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

public interface PassiveMessageHandler {
	
	public String getName();
	
	public String getDescription();
	
	public void handleMessage(MessageReceivedEvent event);
	
	
}
