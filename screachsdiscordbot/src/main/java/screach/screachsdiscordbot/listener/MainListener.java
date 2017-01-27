package screach.screachsdiscordbot.listener;

import java.util.ArrayList;

import screach.screachsdiscordbot.handlers.ActiveMessageHandler;
import screach.screachsdiscordbot.handlers.PresenceUpdateHandler;
import screach.screachsdiscordbot.handlers.ReadyHandler;
import screach.screachsdiscordbot.server.Server;
import screach.screachsdiscordbot.util.Debug;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class MainListener implements IListener<Event> {
	private ArrayList<Server> servers = new ArrayList<>();
	private ReadyHandler readyHandler;
	
	
	public MainListener() {
		servers = new ArrayList<>();
		readyHandler = new ReadyHandler(this);
	}

	@SuppressWarnings("deprecation")
	public void handle(Event event) {
		try {
			Server server;
			if (event instanceof MessageReceivedEvent) { //Message
				MessageReceivedEvent me = (MessageReceivedEvent) event;
				
				server = getServer(me.getMessage().getGuild());
				server.handleMessageEvent(me);
			} else if (event instanceof PresenceUpdateEvent) { //Presence
				PresenceUpdateEvent pue = (PresenceUpdateEvent) event;
				//TODO make sure it won't miss important events
				server = getServer(pue.getGuild()); //supposed to be unreliable but fuck it, should do the trick.
				
				server.handlePresence(pue);
			} else if (event instanceof ReadyEvent) { //Ready
				readyHandler.setup((ReadyEvent) event);
			}
//			else
//				Debug.println("Unhandled event :" + event.toString());
				
		} catch (RateLimitException e) {
			e.printStackTrace();
		} catch (MissingPermissionsException e) {
			e.printStackTrace();
		} catch (DiscordException e) {
			e.printStackTrace();
		}
	}

	private Server getServer(IGuild guild) {
		for (Server server : servers) {
			if (server.getGuild().getID().equals(guild.getID())) {
				return server;
			}
		}
		//TODO implement new server creation.
		return null;
	}
	

	public static void answerMessageError(MessageReceivedEvent event, String notice) throws RateLimitException, MissingPermissionsException, DiscordException {
		event.getMessage().getChannel().sendMessage(notice);
	}

}
