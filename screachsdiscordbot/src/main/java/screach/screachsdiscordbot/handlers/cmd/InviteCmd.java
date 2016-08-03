package screach.screachsdiscordbot.handlers.cmd;

import screach.screachsdiscordbot.handlers.MessageHandler;
import screach.screachsdiscordbot.util.MessageUtils;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.BotInviteBuilder;
import sx.blah.discord.util.MissingPermissionsException;

public class InviteCmd implements MessageHandler {
	private IDiscordClient bot;
	
	public InviteCmd(IDiscordClient bot) {
		this.bot = bot;
	}
	
	public String getName() {
		return "Bot invitation";
	}

	public String getDescription() {
		return "Generates an url to invite the bot to a server.";
	}

	public String getUsage() {
		return "invite";
	}

	public String getCommand() {
		return "invite";
	}

	public void handleMessage(MessageReceivedEvent event, String[] args) {
		String result = "";
		
		result += new BotInviteBuilder(bot).build();
		
		try {
			MessageUtils.sendMessage(event.getMessage().getChannel(), result);
		} catch (MissingPermissionsException e) {
			e.printStackTrace();
		}
		
	}

}
