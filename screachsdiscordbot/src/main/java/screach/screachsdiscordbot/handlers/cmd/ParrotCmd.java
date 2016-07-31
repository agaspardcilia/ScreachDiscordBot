package screach.screachsdiscordbot.handlers.cmd;

import screach.screachsdiscordbot.handlers.MessageHandler;
import screach.screachsdiscordbot.util.MessageUtils;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class ParrotCmd implements MessageHandler {

	public String getName() {
		return "Parrot";
	}

	public String getDescription() {
		return "Simple command that repeats what is in argument.";
	}

	public String getUsage() {
		return "say <Message to repeat>";
	}

	public String getCommand() {
		return "say";
	}

	public void handleMessage(MessageReceivedEvent event, String[] args){
		String result = "";
		
		
		for (int i = 1; i < args.length; i++) {
			result += args[i] + " ";
		}
		
		
		try {
			MessageUtils.sendMessage(event.getMessage().getChannel(), result);
		} catch (MissingPermissionsException e) {
			e.printStackTrace();
		}
	}

}
