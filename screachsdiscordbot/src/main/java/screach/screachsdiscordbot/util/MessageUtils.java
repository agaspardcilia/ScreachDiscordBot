package screach.screachsdiscordbot.util;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class MessageUtils {
	public final static int DEFAULT_TRIES = 5;
	private final static long SLEEP_LENGTH = 100;
	
	public static void sendMessage(IChannel channel, String content, int tries) throws MissingPermissionsException {
		if (content.equals("")){
			System.out.println("Cannot send empty message.");
			return;
		}
		
		int i = 0;
		do {
			try {
				channel.sendMessage(content);
				break;
			} catch (RateLimitException | DiscordException e) {
				System.out.println("Failed to send message (attemp " + (i+1) + "/" + tries + ")");
				System.out.println(e.getMessage());
				try {
					Thread.sleep(SLEEP_LENGTH);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				i++;
			}
		} while (i < tries);
	}
	
	public static void sendMessage(IChannel channel, String content) throws MissingPermissionsException {
		sendMessage(channel, content, DEFAULT_TRIES);
	}

}
