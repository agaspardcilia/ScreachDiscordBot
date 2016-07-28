package screach.screachsdiscordbot.handlers;

import screach.screachsdiscordbot.listener.MainListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class HelpCmd implements MessageHandler {
	private MainListener ml;
	
	public HelpCmd(MainListener ml) {
		this.ml = ml;
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

	public void handleMessage(MessageReceivedEvent event, String[] args) {
		String result = "Help\n---------------\n";
		
		for (MessageHandler mh : ml.getMsgHandlers()) {
			result += mh.getUsage() + " : " + mh.getDescription() + "\n";
		}
		
		result += "---------------\n";
		
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
