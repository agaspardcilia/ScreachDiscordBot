package screach.screachsdiscordbot.handlers.cmd;

import java.util.HashMap;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

import screach.screachsdiscordbot.handlers.MessageHandler;
import screach.screachsdiscordbot.util.MessageUtils;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.MissingPermissionsException;

public class ChatterBotCmd implements MessageHandler {
	private HashMap<IChannel, ChatterBotSession> sessions;
	
	public ChatterBotCmd() {
		sessions = new HashMap<>();
	}
	
	public String getName() {
		return "ChatterBot";
	}

	public String getDescription() {
		return "Feeling lonely? Just chat with a robot with this command.";
	}

	public String getUsage() {
		return "say <Your message>";
	}

	public String getCommand() {
		return "say";
	}

	public void handleMessage(MessageReceivedEvent event, String[] args){
		String result = "";
		String message = "";
		
		for (int i = 1; i < args.length; i++) {
			message += args[i] + " ";
		}

		try {
			result += getSession(event.getMessage().getChannel()).think(message);
		} catch (Exception e) {
			result += "Chatterbot error : " + e.getMessage();
		}
		
		
		try {
			MessageUtils.sendMessage(event.getMessage().getChannel(), result);
		} catch (MissingPermissionsException e) {
			e.printStackTrace();
		}
	}

	private ChatterBotSession getSession(IChannel channel) throws Exception {
		ChatterBotSession ret;
		
		for (IChannel c : sessions.keySet()) {
			if (c.equals(channel))
				return sessions.get(c);
		}
		
		ChatterBotFactory f = new ChatterBotFactory();
		ChatterBot bot = f.create(ChatterBotType.CLEVERBOT);
		ret = bot.createSession();
		
		
		return ret;
	}
	
	public static void main(String[] args) {
		ChatterBotFactory factory = new ChatterBotFactory();
		try {
			ChatterBot bot = factory.create(ChatterBotType.CLEVERBOT);
			ChatterBotSession session = bot.createSession();

			System.out.println(session.think("Salut"));
			System.out.println(session.think("Ca va ?"));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
