package me.Flibio.JobsLite.Listeners;

import me.Flibio.JobsLite.Main;
import me.Flibio.JobsLite.Utils.JobManager;
import me.Flibio.JobsLite.Utils.PlayerManager;
import me.Flibio.JobsLite.Utils.TextUtils;

import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.text.Texts;

public class PlayerChatListener {
	
	private JobManager jobManager = Main.access.jobManager;
	private PlayerManager playerManager = Main.access.playerManager;
	
	@Subscribe
	public void onPlayerChat(PlayerChatEvent event) {
		if(Main.optionEnabled("chatPrefixes")) {
			String uuid = event.getUser().getUniqueId().toString();
			if(playerManager.playerExists(uuid)) {
				String currentJob = playerManager.getCurrentJob(uuid).trim();
				if(!currentJob.isEmpty()) {
					String displayName = jobManager.getDisplayName(currentJob);
					if(!displayName.isEmpty()) {
						if(Main.optionEnabled("displayLevel")) {
							event.setNewMessage(TextUtils.chatMessage(event.getUser().getName(), displayName, playerManager.getCurrentLevel(uuid, currentJob), 
									jobManager.getColor(currentJob), Texts.toPlain(event.getUnformattedMessage())));
						} else {
							event.setNewMessage(TextUtils.chatMessage(event.getUser().getName(), displayName, jobManager.getColor(currentJob), Texts.toPlain(event.getUnformattedMessage())));
						}
					}
				}
			}
		}
	}
}
