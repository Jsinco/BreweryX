package com.dre.brewery.commands;

import com.dre.brewery.Brew;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.recipe.BRecipe;
import com.dre.brewery.utility.BUtil;
import com.dre.brewery.utility.MinecraftVersion;
import com.dre.brewery.utility.PermissionUtil;
import com.dre.brewery.utility.Tuple;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.dre.brewery.utility.PermissionUtil.BPermission.*;

public class CommandUtil {

    private static final BreweryPlugin plugin = BreweryPlugin.getInstance();
    private static final MinecraftVersion VERSION = BreweryPlugin.getMCVersion();

    // Todo: Replace with a map
    private static Set<Tuple<String, String>> mainSet;
    private static Set<Tuple<String, String>> altSet;
    private static final String[] QUALITY = {"1", "10"};


    public static void cmdHelp(CommandSender sender, String[] args) {

        int page = 1;
        if (args.length > 1) {
            page = plugin.parseInt(args[1]);
        }

        ArrayList<String> commands = getCommands(sender);

        if (page == 1) {
            plugin.msg(sender, "&6" + plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion());
        }

        BUtil.list(sender, commands, page);

    }

    @Nullable
    public static Tuple<Brew, Player> getFromCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return null;
        }

        int quality = 10;
        boolean hasQuality = false;
        String pName = null;
        if (args.length > 2) {
            quality = plugin.parseInt(args[args.length - 1]);

            if (quality <= 0 || quality > 10) {
                pName = args[args.length - 1];
                if (args.length > 3) {
                    quality = plugin.parseInt(args[args.length - 2]);
                }
            }
            if (quality > 0 && quality <= 10) {
                hasQuality = true;
            } else {
                quality = 10;
            }
        }
        Player player = null;
        if (pName != null) {
            player = plugin.getServer().getPlayer(pName);
        }

        if (!(sender instanceof Player) && player == null) {
            plugin.msg(sender, plugin.languageReader.get("Error_PlayerCommand"));
            return null;
        }

        if (player == null) {
            player = ((Player) sender);
            pName = null;
        }
        int stringLength = args.length - 1;
        if (pName != null) {
            stringLength--;
        }
        if (hasQuality) {
            stringLength--;
        }

        String name;
        if (stringLength > 1) {
            StringBuilder builder = new StringBuilder(args[1]);

            for (int i = 2; i < stringLength + 1; i++) {
                builder.append(" ").append(args[i]);
            }
            name = builder.toString();
        } else {
            name = args[1];
        }
        name = name.replaceAll("\"", "");

        BRecipe recipe = BRecipe.getMatching(name);
        if (recipe != null) {
            return new Tuple<>(recipe.createBrew(quality), player);
        } else {
            plugin.msg(sender, plugin.languageReader.get("Error_NoBrewName", name));
        }
        return null;
    }

    public static ArrayList<String> getCommands(CommandSender sender) {

        ArrayList<String> cmds = new ArrayList<>();
        cmds.add(plugin.languageReader.get("Help_Help"));
        PermissionUtil.evaluateExtendedPermissions(sender);

		/*
        if (PLAYER.checkCached(sender)) {
            cmds.add (plugin.languageReader.get("Help_Player"));
        }
        */

        if (INFO.checkCached(sender)) {
            cmds.add (plugin.languageReader.get("Help_Info"));
        }

        if (VERSION.isOrLater(MinecraftVersion.V1_13) && SEAL.checkCached(sender)) {
            cmds.add (plugin.languageReader.get("Help_Seal"));
        }

        if (UNLABEL.checkCached(sender)) {
            cmds.add (plugin.languageReader.get("Help_UnLabel"));
        }

        if (PermissionUtil.noExtendedPermissions(sender)) {
            return cmds;
        }

        if (INFO_OTHER.checkCached(sender)) {
            cmds.add (plugin.languageReader.get("Help_InfoOther"));
        }

        if (CREATE.checkCached(sender)) {
            cmds.add(plugin.languageReader.get("Help_Create"));
            cmds.add(plugin.languageReader.get("Help_Give"));
        }

        if (DRINK.checkCached(sender) || DRINK_OTHER.checkCached(sender)) {
            cmds.add(plugin.languageReader.get("Help_Drink"));
        }

        if (RELOAD.checkCached(sender)) {
            cmds.add(plugin.languageReader.get("Help_Configname"));
            cmds.add(plugin.languageReader.get("Help_Reload"));
        }

        if (PUKE.checkCached(sender) || PUKE_OTHER.checkCached(sender)) {
            cmds.add(plugin.languageReader.get("Help_Puke"));
        }

        if (WAKEUP.checkCached(sender)) {
            cmds.add(plugin.languageReader.get("Help_Wakeup"));
            cmds.add(plugin.languageReader.get("Help_WakeupList"));
            cmds.add(plugin.languageReader.get("Help_WakeupCheck"));
            cmds.add(plugin.languageReader.get("Help_WakeupCheckSpecific"));
            cmds.add(plugin.languageReader.get("Help_WakeupAdd"));
            cmds.add(plugin.languageReader.get("Help_WakeupRemove"));
        }

        if (STATIC.checkCached(sender)) {
            cmds.add(plugin.languageReader.get("Help_Static"));
        }

		if (SET.checkCached(sender)) {
			cmds.add(plugin.languageReader.get("Help_Set"));
		}

        if (COPY.checkCached(sender)) {
            cmds.add (plugin.languageReader.get("Help_Copy"));
        }

        if (DELETE.checkCached(sender)) {
            cmds.add (plugin.languageReader.get("Help_Delete"));
        }

        return cmds;
    }


    public static List<String> tabCreateAndDrink(String[] args) {
        if (args.length == 2) {

            if (mainSet == null) {
                mainSet = new HashSet<>();
                altSet = new HashSet<>();
                for (BRecipe recipe : BRecipe.getAllRecipes()) {
                    mainSet.addAll(createLookupFromName(recipe.getName(5)));

                    Set<String> altNames = new HashSet<>(3);
                    altNames.add(recipe.getName(1));
                    altNames.add(recipe.getName(10));
                    if (recipe.getId() != null) { // Leaving a null check JUST in case. But ids are never null in the current implementation
                        altNames.add(recipe.getId());
                    }

                    for (String altName : altNames) {
                        altSet.addAll(createLookupFromName(altName));
                    }

                }
            }

            final String input = args[1].toLowerCase();

            List<String> options = mainSet.stream()
                    .filter(s -> s.a().startsWith(input))
                    .map(Tuple::second)
                    .collect(Collectors.toList());
            if (options.isEmpty()) {
                options = altSet.stream()
                        .filter(s -> s.a().startsWith(input))
                        .map(Tuple::second)
                        .collect(Collectors.toList());
            }
            return options;
        } else {
            if (args[args.length - 1].matches("10|[1-9]")) {
                return null; // automatically suggests player names
            } else {
                return filterWithInput(QUALITY, args[args.length - 1]);
            }
        }

    }
    private static List<Tuple<String, String>> createLookupFromName(final String name) {
        return Arrays.stream(name.split(" "))
                .map(word -> new Tuple<>(word.toLowerCase(), name))
                .collect(Collectors.toList());
    }

    public static List<String> filterWithInput(String[] options, String input) {
        return Arrays.stream(options)
                .filter(s -> s.startsWith(input))
                .collect(Collectors.toList());
    }

    public static void reloadTabCompleter() {
        mainSet = null;
        altSet = null;
    }
}
