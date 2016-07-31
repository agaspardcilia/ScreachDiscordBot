package screach.screachsdiscordbot.handlers.cmd;

import java.text.ParseException;
import java.util.Random;

import screach.screachsdiscordbot.handlers.MessageHandler;
import screach.screachsdiscordbot.util.MessageUtils;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class RollCmd implements MessageHandler {
	private final static int DEFAULT_MAX = 100;

	private Random rng;

	public RollCmd() {
		rng = new Random();
	}

	public String getName() {
		return "Roll";
	}

	public String getDescription() {
		return "Generates a random number between 1 and " + DEFAULT_MAX + " by default. The maximum value is " + Integer.MAX_VALUE + ".";
	}

	public String getUsage() {
		return "roll [max value]";
	}

	public String getCommand() {
		return "roll";
	}

	public void handleMessage(MessageReceivedEvent event, String[] args) {
		String result = "";
		int max = DEFAULT_MAX;

		if (args.length > 1) {
			try {
				max = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				result += "Bad argument \"" + args[1] + "\". Setting max to default value (" + DEFAULT_MAX + ").\n";
			} catch (IllegalArgumentException e) {
				result += "Bad argument \"" + args[1] + "\". Setting max to default value (" + DEFAULT_MAX + ").\n";
			}

		}

		result += rng.nextInt(max + 1);

		try {
			MessageUtils.sendMessage(event.getMessage().getChannel(), result);
		} catch (MissingPermissionsException e) {
			e.printStackTrace();
		}
	}


}
