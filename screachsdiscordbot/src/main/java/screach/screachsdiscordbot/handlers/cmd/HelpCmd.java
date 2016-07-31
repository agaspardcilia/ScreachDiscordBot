package screach.screachsdiscordbot.handlers.cmd;

import screach.screachsdiscordbot.handlers.MessageHandler;
import screach.screachsdiscordbot.listener.MainListener;
import screach.screachsdiscordbot.util.MessageUtils;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.MissingPermissionsException;

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
		String result = "__**Help**__\n";
		result += "<argument> : required, [argument] : optionnal\n\n";
		
		for (MessageHandler mh : ml.getMsgHandlers()) {
			result += "**" + mh.getUsage() + "** : " + mh.getDescription() + "\n";
		}
		
		
		try {
			MessageUtils.sendMessage(event.getMessage().getChannel(), result);
		} catch (MissingPermissionsException e) {
			e.printStackTrace();
		}
	}

}
