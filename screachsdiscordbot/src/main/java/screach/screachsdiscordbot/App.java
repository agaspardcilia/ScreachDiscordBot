package screach.screachsdiscordbot;


import screach.screachsdiscordbot.listener.MainListener;
import screach.screachsdiscordbot.util.Debug;
import screach.screachsdiscordbot.util.FailedToLoadSettingsException;
import screach.screachsdiscordbot.util.Settings;
import screach.screachsdiscordbot.util.ThreadPool;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

public class App {

	public static void main(String[] args) {
		IDiscordClient bot;
		MainListener mListener;
		String token;
		
		
		
		try {
			Settings.init();
			ThreadPool.init();
			Debug.init();
			System.out.println("Settings loaded.");
		} catch (FailedToLoadSettingsException e1) {
			System.err.println("Failed to load settings : " + e1.getNotice());
			System.err.println("Stopping program...");
			return;
		}
		
		try {
			token = Settings.crtInstance.getValue("token");
			bot = getClient(token);
			
			mListener = new MainListener();
			bot.getDispatcher().registerListener(mListener);
			

		} catch (DiscordException e) {
			e.printStackTrace();
		}




	}


	public static IDiscordClient getClient(String token) throws DiscordException {
		return new ClientBuilder().withToken(token).login();
	}    

	

}
