/*
 * This file is part of JobsLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2016 Flibio
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.flibio.jobslite.listeners;

import me.flibio.jobslite.JobsLite;
import me.flibio.jobslite.utils.JobManager;
import me.flibio.jobslite.utils.PlayerManager;
import me.flibio.jobslite.utils.TextUtils;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.message.MessageChannelEvent;

import java.util.Optional;

public class PlayerChatListener {
	
	private JobManager jobManager = JobsLite.access.jobManager;
	private PlayerManager playerManager = JobsLite.access.playerManager;
	
	@Listener
	public void onPlayerChat(MessageChannelEvent.Chat event) {
		Optional<Player> playerOptional = event.getCause().first(Player.class);
		if(!playerOptional.isPresent()) return;
		Player player = playerOptional.get();
		if(JobsLite.optionEnabled("chatPrefixes")) {
			String uuid = player.getUniqueId().toString();
			if(playerManager.playerExists(uuid)) {
				String currentJob = playerManager.getCurrentJob(uuid).trim();
				if(!currentJob.isEmpty()) {
					String displayName = jobManager.getDisplayName(currentJob);
					if(!displayName.isEmpty()) {
						if(JobsLite.optionEnabled("displayLevel")) {
							event.setMessage(TextUtils.chatMessage(player.getName(), displayName, playerManager.getCurrentLevel(uuid, currentJob), 
									jobManager.getColor(currentJob), event.getRawMessage().toPlain()));
						} else {
							event.setMessage(TextUtils.chatMessage(player.getName(), displayName, jobManager.getColor(currentJob), 
									event.getRawMessage().toPlain()));
						}
					}
				}
			}
		}
	}
}
