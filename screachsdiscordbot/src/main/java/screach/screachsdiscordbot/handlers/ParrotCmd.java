package screach.screachsdiscordbot.handlers;

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
			event.getMessage().getChannel().sendMessage(result);
		} catch (RateLimitException e) {
			e.printStackTrace();
		} catch (MissingPermissionsException e) {
			e.printStackTrace();
		} catch (DiscordException e) {
			e.printStackTrace();
		}
	}

}