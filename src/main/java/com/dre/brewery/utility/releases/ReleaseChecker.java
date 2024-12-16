/*
 * BreweryX Bukkit-Plugin for an alternate brewing process
 * Copyright (C) 2024 The Brewery Team
 *
 * This file is part of BreweryX.
 *
 * BreweryX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BreweryX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BreweryX. If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 */

package com.dre.brewery.utility.releases;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.configuration.ConfigManager;
import com.dre.brewery.configuration.files.Config;
import com.dre.brewery.configuration.files.Lang;
import com.dre.brewery.utility.releases.impl.GitHubReleaseChecker;
import com.dre.brewery.utility.releases.impl.NoImplReleaseChecker;
import com.dre.brewery.utility.releases.impl.SpigotReleaseChecker;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.concurrent.CompletableFuture;

@Getter
public abstract class ReleaseChecker {

    protected static final String CONST_UNRESOLVED = "Unresolved";
    private static ReleaseChecker instance;

    protected String resolvedLatestVersion = null; // Latest version of BX resolved from the source


    public abstract CompletableFuture<String> resolveLatest();

    public abstract CompletableFuture<Boolean> checkForUpdate();
    public CompletableFuture<Boolean> checkForUpdate(boolean overwriteCache) {
        if (overwriteCache) {
            this.resolvedLatestVersion = null;
        }
        return checkForUpdate();
    }


    public void notify(CommandSender receiver) {
        if (receiver.hasPermission("brewery.update") && isUpdateAvailable()) {
            ConfigManager.getConfig(Lang.class)
                    .sendEntry(receiver, "Etc_UpdateAvailable", "v" + localVersion(), "v" + resolvedLatestVersion);
        }
    }


    public boolean isUpdateAvailable() {
        if (resolvedLatestVersion.equals(CONST_UNRESOLVED)) {
            return false;
        }
        int local = parseVersion(localVersion());
        int resolved = parseVersion(resolvedLatestVersion);
        return resolved > local;
    }


    // Singleton

    public static ReleaseChecker getInstance() {
        if (instance != null) {
            return instance;
        }
        Config config = ConfigManager.getConfig(Config.class);
        switch (config.getResolveUpdatesFrom()) {
            case GITHUB -> instance = new GitHubReleaseChecker("BreweryTeam", "BreweryX");
            case SPIGOT -> instance = new SpigotReleaseChecker(114777);
            case NONE -> instance = new NoImplReleaseChecker();
        }
        return instance;
    }


    // Util
    public String localVersion() {
        String versionString = BreweryPlugin.getInstance().getDescription().getVersion();
        if (versionString.contains(";")) {
            // I don't care about the branch
            return versionString.split(";")[0];
        }
        return versionString;
    }

    public int parseVersion(String version) {
        StringBuilder sb = new StringBuilder();
        for (char c : version.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
        return Integer.parseInt(sb.toString());
    }


    public enum ReleaseCheckerType {
        GITHUB,
        SPIGOT,
        NONE
    }
}
