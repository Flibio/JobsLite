package me.Flibio.JobsLite.Listeners;

import me.Flibio.JobsLite.Main;
import me.Flibio.JobsLite.Utils.JobManager;
import me.Flibio.JobsLite.Utils.PlayerManager;
import me.Flibio.JobsLite.Utils.TextUtils;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.MessageSinkEvent;
import org.spongepowered.api.text.Texts;

import com.google.common.base.Optional;

public class PlayerChatListener {
	
	private JobManager jobManager = Main.access.jobManager;
	private PlayerManager playerManager = Main.access.playerManager;
	//Waiting on Sponge API PR #835
	@Listener
	public void onPlayerChat(MessageSinkEvent event) {
		Optional<Player> playerOptional = event.getCause().first(Player.class);
		if(!playerOptional.isPresent()) return;
		Player player = playerOptional.get();
		if(Main.optionEnabled("chatPrefixes")) {
			String uuid = player.getUniqueId().toString();
			if(playerManager.playerExists(uuid)) {
				String currentJob = playerManager.getCurrentJob(uuid).trim();
				if(!currentJob.isEmpty()) {
					String displayName = jobManager.getDisplayName(currentJob);
					if(!displayName.isEmpty()) {
						if(Main.optionEnabled("displayLevel")) {
							event.setMessage(TextUtils.chatMessage(player.getName(), displayName, playerManager.getCurrentLevel(uuid, currentJob), 
									jobManager.getColor(currentJob), Texts.toPlain(event.getOriginalMessage())));
						} else {
							event.setMessage(TextUtils.chatMessage(player.getName(), displayName, jobManager.getColor(currentJob), Texts.toPlain(event.getOriginalMessage())));
						}
					}
				}
			}
		}
	}
}
