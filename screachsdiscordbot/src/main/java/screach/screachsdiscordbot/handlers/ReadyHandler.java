package screach.screachsdiscordbot.handlers;

import screach.screachsdiscordbot.handlers.cmd.HelpCmd;
import screach.screachsdiscordbot.handlers.cmd.InviteCmd;

import org.apache.commons.io.FilenameUtils;

import screach.screachsdiscordbot.App;
import screach.screachsdiscordbot.handlers.cmd.ChatterBotCmd;
import screach.screachsdiscordbot.handlers.cmd.RollCmd;
import screach.screachsdiscordbot.handlers.cmd.jukebox.JukeBoxCmd;
import screach.screachsdiscordbot.handlers.presencechangehandler.RoleManagerHandler;
import screach.screachsdiscordbot.listener.MainListener;
import screach.screachsdiscordbot.util.Settings;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;

public class ReadyHandler {
	private MainListener mListener;
	
	public ReadyHandler(MainListener listener) {
		this.mListener = listener;
	}
	
	public void setup(ReadyEvent event) {
		Status status;
		IDiscordClient bot = event.getClient();
		
		System.out.println("The bot is starting...");
		setupBot(bot);
		setupMessageListeners(bot);
		setupPresenceListeners();
		
		status = Status.game(Settings.crtInstance.getValue("botstatus"));
		bot.changeStatus(status);

		
		System.out.println("The bot is ready.");
		
	}
	
	public void setupBot(IDiscordClient bot) {
		boolean setupBot = false;
		
		setupBot = Boolean.parseBoolean(Settings.crtInstance.getValue("setupbot"));
		
		if (setupBot) {
			System.out.println("Performing bot setup...");
			try {
				setupBotIdentity(bot);
				
				System.out.println("Bot setup finished.");
			} catch (RateLimitException e) {
				e.printStackTrace();
			} catch (DiscordException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setupMessageListeners(IDiscordClient bot) {
		mListener.addMessageHandler(new ChatterBotCmd());
		mListener.addMessageHandler(new HelpCmd(mListener));
		mListener.addMessageHandler(new RollCmd());
		mListener.addMessageHandler(new InviteCmd(bot));
		mListener.addMessageHandler(new JukeBoxCmd(bot));
	}
	
	
	public void setupPresenceListeners() {
		mListener.addPresenceUpdateHandler(new RoleManagerHandler());
	}
	
	public void setupBotIdentity(IDiscordClient bot) throws RateLimitException, DiscordException {
		String email;
		String avatar;
		Image img;
		
		
		avatar = Settings.crtInstance.getValue("botimage");
		email = Settings.crtInstance.getValue("botemail");
		img = Image.forUrl(FilenameUtils.getExtension(avatar), avatar);
		
		bot.changeEmail(email);
	    bot.changeAvatar(img);
		
		
	}
	
}
