package screach.screachsdiscordbot.handlers.presencechangehandler;

import java.util.ArrayList;
import java.util.List;

import screach.screachsdiscordbot.handlers.PresenceUpdateHandler;
import screach.screachsdiscordbot.util.Debug;
import screach.screachsdiscordbot.util.Settings;
import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class RoleManagerHandler implements PresenceUpdateHandler {
	private final static String DEFAULT_ROLE_NAME =  "dftrole";
	private final static int TRIES = 5;

	private ArrayList<IGuild> activeGuilds;
	private ArrayList<IGuild> inactiveGuilds;

	public RoleManagerHandler() {
		activeGuilds = new ArrayList<>();
		inactiveGuilds = new ArrayList<>();
	}

	@Override
	public void handle(PresenceUpdateEvent event) {
		@SuppressWarnings("deprecation")
		IGuild currentGuild = event.getGuild();
		IUser user;
		List<IRole> roles;
		IRole[] dftRoles;
		IRole dftRole;
		int i;
		if (isGuildActive(currentGuild)) {
			user = event.getUser();
			roles = user.getRolesForGuild(currentGuild);

			Debug.println("Checking roles");
			
			if (roles.size() <= 1) {
				
				Debug.println("No roles.");
				
				try {
					dftRole = currentGuild.getRolesByName(Settings.crtInstance.getValue(DEFAULT_ROLE_NAME)).get(0);
				} catch (ArrayIndexOutOfBoundsException e) {
					setGuildToInactive(currentGuild);
					Debug.println("RoleManager error : guild " + currentGuild.getName() + "is set to inactive for missing default role.");
					return;
				}

				dftRoles = new IRole[1];
				dftRoles[0] = dftRole;

				i = 0;
				while (i < TRIES){
					try {
						currentGuild.editUserRoles(user, dftRoles);
					} catch (RateLimitException | DiscordException e) {
						i++;
						continue;
					} catch(MissingPermissionsException e) {
						setGuildToInactive(currentGuild);
						Debug.println("RoleManager error : guild " + currentGuild.getName() + "is set to inactive for missing default role.");
						return;
					}
					break;
				}
			}
		}


	}

	private boolean isGuildActive(IGuild guild) {
		if (activeGuilds.contains(guild)) {
			return true;
		} else if (inactiveGuilds.contains(guild)) {
			return false;
		} else {
			activeGuilds.add(guild);
			return true;
		}
	}

	private void setGuildToInactive(IGuild guild) {
		activeGuilds.remove(guild);
		if (!inactiveGuilds.contains(guild))
			inactiveGuilds.add(guild);
	}




}
