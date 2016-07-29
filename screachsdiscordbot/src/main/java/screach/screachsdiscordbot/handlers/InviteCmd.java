package screach.screachsdiscordbot.handlers;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.BotInviteBuilder;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

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
			event.getMessage().getChannel().sendMessage(result);
		} catch (RateLimitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MissingPermissionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DiscordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
