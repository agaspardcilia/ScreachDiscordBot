package screach.screachsdiscordbot;

import screach.screachsdiscordbot.handlers.HelpCmd;
import screach.screachsdiscordbot.handlers.ParrotCmd;
import screach.screachsdiscordbot.listener.MainListener;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

public class App {
	private final static String token = "MjA4MzE2OTY2MTY1ODA3MTA1.CnwH0A.7P4N6Nk7aYhZ-uMwJbKTLeP2SsQ";


	public static void main(String[] args) {
		IDiscordClient client;
		
		MainListener mListener;
		
		
		try {
			client = getClient(token);
			mListener = new MainListener();

			mListener.addMessageHandler(new ParrotCmd());
			mListener.addMessageHandler(new HelpCmd(mListener));
			
			client.getDispatcher().registerListener(mListener);
			

		} catch (DiscordException e) {
			e.printStackTrace();
		}




	}


	public static IDiscordClient getClient(String token) throws DiscordException {
		return new ClientBuilder().withToken(token).login();
	}    


}
