package com.dre.brewery.commands;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.commands.subcommands.CopyCommand;
import com.dre.brewery.commands.subcommands.CreateCommand;
import com.dre.brewery.commands.subcommands.DataManagerCommand;
import com.dre.brewery.commands.subcommands.DebugInfoCommand;
import com.dre.brewery.commands.subcommands.DeleteCommand;
import com.dre.brewery.commands.subcommands.DrinkCommand;
import com.dre.brewery.commands.subcommands.HelpCommand;
import com.dre.brewery.commands.subcommands.InfoCommand;
import com.dre.brewery.commands.subcommands.ItemName;
import com.dre.brewery.commands.subcommands.PukeCommand;
import com.dre.brewery.commands.subcommands.ReloadAddonsCommand;
import com.dre.brewery.commands.subcommands.ReloadCommand;
import com.dre.brewery.commands.subcommands.SealCommand;
import com.dre.brewery.commands.subcommands.SetCommand;
import com.dre.brewery.commands.subcommands.ShowStatsCommand;
import com.dre.brewery.commands.subcommands.StaticCommand;
import com.dre.brewery.commands.subcommands.UnLabelCommand;
import com.dre.brewery.commands.subcommands.VersionCommand;
import com.dre.brewery.commands.subcommands.WakeupCommand;
import com.dre.brewery.configuration.ConfigManager;
import com.dre.brewery.configuration.files.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager implements TabExecutor {

    private static final BreweryPlugin plugin = BreweryPlugin.getInstance();
    private static final Lang lang = ConfigManager.getConfig(Lang.class);

    private static final Map<String, SubCommand> subCommands = new HashMap<>();

    public CommandManager() {
        subCommands.put("help" , new HelpCommand());
        subCommands.put("reload", new ReloadCommand());
        subCommands.put("wakeup", new WakeupCommand());
        subCommands.put("itemName", new ItemName());
        subCommands.put("create", new CreateCommand());
        subCommands.put("info", new InfoCommand());
        subCommands.put("seal", new SealCommand());
        subCommands.put("copy", new CopyCommand());
        subCommands.put("delete", new DeleteCommand());
        subCommands.put("static", new StaticCommand());
		subCommands.put("set", new SetCommand());
        subCommands.put("unLabel", new UnLabelCommand());
        subCommands.put("debuginfo", new DebugInfoCommand());
        subCommands.put("showstats", new ShowStatsCommand());
        subCommands.put("puke", new PukeCommand());
        subCommands.put("drink", new DrinkCommand());
		subCommands.put("reloadaddons", new ReloadAddonsCommand());
        subCommands.put("version", new VersionCommand());
        subCommands.put("data", new DataManagerCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length < 1) {
            CommandUtil.cmdHelp(sender, args);
            return true;
        }

        SubCommand subCommand = subCommands.get(args[0]);
        if (subCommand == null) {
            CommandUtil.cmdHelp(sender, args);
            return true;
        }
        boolean playerOnly = subCommand.playerOnly();
        String permission = subCommand.permission();

        if (playerOnly && !(sender instanceof Player)) {
            lang.sendEntry(sender, "Error_NotPlayer");
            return true;
        } else if (permission != null && !sender.hasPermission(permission)) {
            lang.sendEntry(sender, "Error_NoPermission");
            return true;
        }

        subCommand.execute(plugin, lang, sender, s, args);
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            List<String> commands = new ArrayList<>();
            for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
                String perm = entry.getValue().permission();
                if (perm != null && commandSender.hasPermission(perm)) {
                    commands.add(entry.getKey());
                }
            }
            return commands;
        }

        SubCommand subCommand = subCommands.get(strings[0].toLowerCase());
        if (subCommand != null) {
            return subCommand.tabComplete(plugin, commandSender, s, strings);
        }
        return null;
    }

	public static void addSubCommand(String name, SubCommand subCommand) {
		subCommands.put(name, subCommand);
	}

	public static void removeSubCommand(String name) {
		subCommands.remove(name);
	}
}
