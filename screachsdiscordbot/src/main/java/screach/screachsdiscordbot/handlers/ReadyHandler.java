package screach.screachsdiscordbot.handlers;

import screach.screachsdiscordbot.handlers.cmd.HelpCmd;
import screach.screachsdiscordbot.handlers.cmd.InviteCmd;
import screach.screachsdiscordbot.handlers.cmd.ParrotCmd;
import screach.screachsdiscordbot.handlers.cmd.RollCmd;
import screach.screachsdiscordbot.handlers.cmd.jukebox.JukeBoxCmd;
import screach.screachsdiscordbot.listener.MainListener;
import screach.screachsdiscordbot.util.Settings;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.ReadyEvent;

public class ReadyHandler {
	private MainListener mListener;
	
	public ReadyHandler(MainListener listener) {
		this.mListener = listener;
	}
	
	public void setup(ReadyEvent event) {
		IDiscordClient bot = event.getClient();
		
		System.out.println("The bot is starting...");
		setupBot(bot);
		setupMessageListeners(bot);
		System.out.println("The bot is ready.");
		
	}
	
	public void setupBot(IDiscordClient bot) {
		boolean setupBot = false;
		
		setupBot = Boolean.parseBoolean(Settings.crtInstance.getValue("setupbot"));
		
		if (setupBot) {
			System.out.println("Performing bot setup...");
			setupBot(bot);
			System.out.println("Bot setup finished.");
		}
	}
	
	public void setupMessageListeners(IDiscordClient bot) {
		mListener.addMessageHandler(new ParrotCmd());
		mListener.addMessageHandler(new HelpCmd(mListener));
		mListener.addMessageHandler(new RollCmd());
		mListener.addMessageHandler(new InviteCmd(bot));
		mListener.addMessageHandler(new JukeBoxCmd());
	}
	
	
}
