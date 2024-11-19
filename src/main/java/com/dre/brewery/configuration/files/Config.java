package com.dre.brewery.configuration.files;

import com.dre.brewery.configuration.AbstractOkaeriConfigFile;
import com.dre.brewery.configuration.configurer.BreweryXConfigurer;
import com.dre.brewery.configuration.configurer.LocalizedComment;
import com.dre.brewery.configuration.configurer.Translation;
import com.dre.brewery.configuration.sector.CauldronSector;
import com.dre.brewery.configuration.sector.CustomItemsSector;
import com.dre.brewery.configuration.sector.RecipesSector;
import com.dre.brewery.storage.DataManagerType;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.annotation.Header;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

import java.util.List;

@Header({"Our proper config guide can be found at: https://brewery.lumamc.net/en/guide/edit_config/",
		"Still have questions? Join our Discord: https://discord.gg/ZTGCzeKg45"})
@Getter @Setter
public class Config extends AbstractOkaeriConfigFile {

	// How do we do translations on this? - Jsinco
	@Exclude
	public static final String FILE_NAME = "new-config.yml"; // TODO: Change this eventually
    @Getter @Exclude
    private static final Config instance = createConfig(Config.class, FILE_NAME, new BreweryXConfigurer());


    @LocalizedComment("config.language")
    private Translation language = Translation.EN;

	@LocalizedComment("config.updateCheck")
    private boolean updateCheck = true;

    @LocalizedComment("config.autosave")
    private int autosave = 10;

    @LocalizedComment("config.pluginPrefix")
    private String pluginPrefix = "&2[BreweryX]&f ";

    @LocalizedComment("config.debug")
    private boolean debug = false;


    @LocalizedComment("config.storage.header")
    private Storage storage = new Storage();
    @Getter @Setter
    public static class Storage extends OkaeriConfig {
        @LocalizedComment("config.storage.type")
        private DataManagerType type = DataManagerType.FLATFILE;
        @LocalizedComment("config.storage.database")
        private String database = "brewery-data";
        private String tablePrefix = "brewery_";
        private String address = "localhost";
        private String username = "root";
        private String password = "";
    }


    // Maybe condense more of this into configuration sections

    @LocalizedComment("config.enableHome")
    private boolean enableHome = true;

    @LocalizedComment("config.homeType")
    private String homeType = "cmd: home";

    @LocalizedComment("config.enableWake")
    private boolean enableWake = true;

	@LocalizedComment("config.enableLoginDisallow")
	private boolean enableLoginDisallow = true;

	@Comment("If the Player faints shortly (gets kicked from the server) if he drinks the max amount of alcohol possible [false]")
	private boolean enableKickOnOverdrink = false;

	@Comment({
		"If the Player vomits on high drunkenness (drops item defined below) [true]",
		"The item can not be collected and stays on the ground until it despawns."})
	private boolean enablePuke = true;

	@Comment({
		"Items that is dropped multiple times uncollectable when puking [Soul_Sand]",
		"Can be list of items such as [Sould_sand, Slime_block, Dirt]"})
	private List<String> pukeItem = List.of("Soul_Sand");

	@Comment({
		"Time in seconds until the pukeitems despawn, (mc default is 300 = 5 min) [60]",
		"If the item despawn time was changed in the spigot.yml, the pukeDespawntime changes as well."})
	private int pukeDespawnTime = 60;

	@Comment("How much the Player stumbles depending on the amount of alcohol he drank. Can be set to 0 and higher than 100 [100]")
	private int stumblePercent = 100;

	@Comment("Display his drunkenness to the player when he drinks a brew or eats a drainItem [true]")
	private boolean showStatusOnDrink = true;

	@Comment("Consumable Item/strength. Decreases the alcohol level by <strength> when consumed. (list)")
	private List<String> drainItem = List.of("Bread/4", "Milk_Bucket/2");

	@Comment({
		"Show Particles over Cauldrons when they have ingredients and a heat source. [true]",
		"The changing color of the particles can help with timing some recipes"})
	private boolean enableCauldronParticles = true;

	@Comment("If Cauldron Particles should be reduced to the bare minimum [false]")
	private boolean minimalParticles = false;

	@Comment("If crafting and using of the Brew Sealing Table is enabled (2 Bottles over 4 Planks) [true, true]")
	private boolean craftSealingTable = true;
	private boolean enableSealingTable = true;

	@Comment({
		"By default, Brewery uses Smoker as a Sealing Table, this option allows you to change it",
		"IMPORTANT: It needs to be a container - meaning a block that can store items (e.g., SMOKER, CHEST, BLAST_FURNACE)."})
	private Material sealingTableBlock = Material.SMOKER;

	@Comment("Always show the 1-5 stars on the item depending on the quality. If false, they will only appear when brewing [true]")
	private boolean alwaysShowQuality = true;

	@Comment("Always show the alcohol content on the item. If false, it will only show in the brewing stand [false]")
	private boolean alwaysShowAlc = false;

	@Comment("If we should show who brewed the drink [false]")
	private boolean showBrewer = false;

	@Comment("If barrels are only created when the sign placed contains the word \"barrel\" (or a translation when using another language) [true]")
	private boolean requireKeywordOnSigns = true;

	@Comment("If aging in -Minecraft- Barrels in enabled [true] and how many Brewery drinks can be put into them [6]")
	private boolean ageInMCBarrels = true;
	private int maxBrewsInMCBarrels = 6;

	@Comment("Duration (in minutes) of a \"year\" when aging drinks [20]")
	private int agingYearDuration = 20;

	@Comment({
		"The used Ingredients and other brewing-data is saved to all Brewery Items. To prevent",
		"hacked clients from reading what exactly was used to brew an item, the data can be encoded/scrambled.",
		"This is a fast process to stop players from hacking out recipes, once they get hold of a brew.",
		" ",
		"Only drawback: brew items can only be used on another server with the same encodeKey.",
		"When using Brews on multiple (BungeeCord) Servers, define a MYSQL database in the 'storage' settings.",
		" ",
		"So enable this if you want to make recipe cheating harder, but don't share any brews by world download, schematics, or other means. [false]"})
	private boolean enableEncode = false;
	private int encodeKey = 0;

	// Skipping customItems section.

	// Skipping cauldron section.

	// Skipping recipes section.

	@Comment({
		"Enable checking of other Plugins (if installed) for Barrel Permissions [true]",
		"Plugins 'Landlord' and 'Protection Stones' use the WorldGuard Flag. 'ClaimChunk' is natively supported."})
	private boolean useWorldGuard = true;
	private boolean useLWC = true;
	private boolean useGriefPrevention = true;
	private boolean useTowny = true;
	private boolean useBlockLocker = true;
	private boolean useGMInventories = true;

	@Comment({
		"Use a virtual chest when opening a Barrel to check with all other protection plugins",
		"This could confuse Anti-Cheat plugins, but is otherwise good to use",
		"use this for 'Residence' Plugin and any others that don't check all cases in the PlayerInteractEvent"})
	private boolean useVirtualChestPerms = false;

	@Comment("Enable the Logging of Barrel Inventories to LogBlock [true]")
	private boolean useLogBlock = true;

	@Comment("If items in Offhand should be added to the cauldron as well [false]")
	private boolean useOffhandForCauldron = false;

	@Comment("If Barrel and Cauldron data can be loaded Async/in the Background [true]")
	private boolean loadDataAsync = true;

	@Comment("Time (in days) that drunkenness-data stays in memory after a player goes offline, to apply hangover etc. [7]")
	private int hangoverDays = 7;

	@Comment("Color the Item information (lore) depending on quality while it is 1. in a barrel and/or 2. in a brewing stand [true, true]")
	private boolean colorInBarrels = true;
	private boolean colorInBrewer = true;

	@Comment("If a Large Barrel can be opened by clicking on any of its blocks, not just Spigot or Sign. This is always true for Small Barrels. [true]")
	private boolean openLargeBarrelEverywhere = true;

	@Comment("Allow emptying brews into hoppers to discard brews while keeping the glass bottle [true]")
	private boolean brewHopperDump = true;

	@Comment({
		"If written Chat is distorted when the Player is Drunk, so that it looks like drunk writing",
		"How much the chat is distorted depends on how drunk the Player is",
		"Below are settings for what and how changes in chat occur"})
	private boolean enableChatDistortion = true;

	@Comment("Log to the Serverlog what the player actually wrote, before his words were altered [false]")
	private boolean logRealChat = false;

	@Comment("Text after specified commands will be distored when drunk (list) [- /gl]")
	private List<String> distortCommands = List.of("/gl", "/global", "/fl", "/s", "/letter", "/g", "/l", "/lokal",
		"/local", "/mail send", "/m", "/msg", "/w", "/whisper", "/reply", "/r", "/t", "/tell");

	@Comment("Distort the Text written on a Sign while drunk [false]")
	private boolean distortSignText = false;

	@Comment({
		"Enclose a Chat text with these Letters to bypass Chat Distortion (Use "," as Separator) (list) [- '[,]']",
		"Chat Example: Hello i am drunk *I am testing Brewery*"})
	private List<String> distortBypass = List.of("*,*", "[,]");


	// TODO: Skipping words section

	@Comment("You may declare custom items, recipes, and cauldron ingredients here too, optionally, but using their respective files is recommended.")
	private CustomItemsSector customItems;
	private CauldronSector cauldron;
	private RecipesSector recipes;

	// TODO: Footer comment?
}
