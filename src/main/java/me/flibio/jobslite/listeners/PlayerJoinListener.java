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
import me.flibio.jobslite.data.JobData;
import me.flibio.jobslite.data.JobDataManipulatorBuilder;
import me.flibio.jobslite.data.JobInfo;
import me.flibio.jobslite.utils.HttpUtils;
import me.flibio.jobslite.utils.JobManager;
import me.flibio.jobslite.utils.JsonUtils;
import me.flibio.jobslite.utils.PlayerManager;
import me.flibio.jobslite.utils.TextUtils;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class PlayerJoinListener {
	
	private PlayerManager playerManager = JobsLite.access.playerManager;
	private JobManager jobManager = JobsLite.access.jobManager;
	private HttpUtils httpUtils = new HttpUtils();
	
	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event) {
		playerManager.addPlayer(event.getTargetEntity());
		Player player = event.getTargetEntity();
		if(!jobManager.jobExists(playerManager.getCurrentJob(player))) {
			JobDataManipulatorBuilder builder = (JobDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(JobData.class).get();
			JobData data = builder.setJobInfo(new JobInfo("",0,0)).create();
			player.offer(data);
		}
		JobsLite.access.economyService.createAccount(player.getUniqueId());
		JobsLite.access.game.getScheduler().createTaskBuilder().execute(new Runnable() {
			public void run() {
				if(player.hasPermission("jobs.admin.updates")&&JobsLite.optionEnabled("updateNotifications")) {
					JsonUtils jsonUtils = new JsonUtils();
					TextUtils textUtils = new TextUtils();
					//Get the data
					String latest = httpUtils.requestData("https://api.github.com/repos/Flibio/JobsLite/releases/latest");
					if(latest.isEmpty()) return;
					String version = jsonUtils.getVersion(latest).replace("v", "");
					String changes = httpUtils.requestData("https://flibio.github.io/JobsLite/changelogs/"+version.replaceAll("\\.", "-")+".txt");
					String[] iChanges = changes.split(";");
					String url = jsonUtils.getUrl(latest);
					boolean prerelease = jsonUtils.isPreRelease(latest);
					//Make sure the latest update is not a prerelease
					if(!prerelease) {
						//Check if the latest update is newer than the current one
						String currentVersion = JobsLite.access.version;
						if(jsonUtils.versionCompare(version, currentVersion)>0) {
							player.sendMessage(textUtils.updateAvailable(version, url));
							for(String change : iChanges) {
								if(!change.trim().isEmpty()) {
									player.sendMessage(textUtils.change(change));
								}
							}
						}
					}
				}
			}
		}).async().submit(JobsLite.access);
	}
}
