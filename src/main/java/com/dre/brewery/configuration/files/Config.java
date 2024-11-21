package com.dre.brewery.configuration.files;

import com.dre.brewery.configuration.AbstractOkaeriConfigFile;
import com.dre.brewery.configuration.annotation.Footer;
import com.dre.brewery.configuration.annotation.OkaeriConfigFileOptions;
import com.dre.brewery.configuration.annotation.LocalizedComment;
import com.dre.brewery.configuration.sector.WordsSector;
import com.dre.brewery.configuration.sector.capsule.ConfigCauldronIngredient;
import com.dre.brewery.configuration.sector.capsule.ConfigCustomItem;
import com.dre.brewery.configuration.sector.capsule.ConfigRecipe;
import com.dre.brewery.configuration.sector.capsule.ConfigDistortWord;
import com.dre.brewery.storage.DataManagerType;
import com.dre.brewery.storage.records.ConfiguredDataManager;
import eu.okaeri.configs.annotation.Header;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


@OkaeriConfigFileOptions("new-config.yml") // TODO: changeme
@Header({"Our proper config guide can be found at: https://brewery.lumamc.net/en/guide/edit_config/",
		"Still have questions? Join our Discord: https://discord.gg/ZTGCzeKg45"})
@Footer("Yep, that's it! The end of config.yml! I had so much fun! And you?...")
@Getter @Setter
public class Config extends AbstractOkaeriConfigFile {

	// This doesn't need to be an enumerator, we're reading this value back to an enum from TranslationManager which doesn't rely on this class.
    @LocalizedComment("config.language")
    private String language = "en";

	@LocalizedComment("config.updateCheck")
    private boolean updateCheck = true;

    @LocalizedComment("config.autosave")
    private int autosave = 10;

    @LocalizedComment("config.pluginPrefix")
    private String pluginPrefix = "&2[BreweryX]&f ";

    @LocalizedComment("config.debug")
    private boolean debug = false;


    @LocalizedComment("config.storage.header")
    private ConfiguredDataManager storage = ConfiguredDataManager.builder()
			.type(DataManagerType.FLATFILE)
			.database("brewery-data")
			.tablePrefix("brewery_")
			.address("localhost")
			.username("root")
			.password("password")
			.build();


    @LocalizedComment("config.enableHome")
    private boolean enableHome = true;

    @LocalizedComment("config.homeType")
    private String homeType = "cmd: home";

    @LocalizedComment("config.enableWake")
    private boolean enableWake = true;

	@LocalizedComment("config.enableLoginDisallow")
	private boolean enableLoginDisallow = true;

	@LocalizedComment("config.enableKickOnOverdrink")
	private boolean enableKickOnOverdrink = false;

	@LocalizedComment("config.enablePuke")
	private boolean enablePuke = true;

	@LocalizedComment("config.pukeItem")
	private List<Material> pukeItem = List.of(Material.SOUL_SAND);

	@LocalizedComment("config.pukeDespawnTime")
	private int pukeDespawnTime = 60;

	@LocalizedComment("config.stumblePercent")
	private int stumblePercent = 100;

	@LocalizedComment("config.showStatusOnDrink")
	private boolean showStatusOnDrink = true;

	@LocalizedComment("config.drainItem")
	private List<String> drainItem = List.of("Bread/4", "Milk_Bucket/2");

	@LocalizedComment("config.enableCauldronParticles")
	private boolean enableCauldronParticles = true;

	@LocalizedComment("config.minimalParticles")
	private boolean minimalParticles = false;

	@LocalizedComment("config.craft-enableSealingTable")
	private boolean craftSealingTable = true;
	private boolean enableSealingTable = true;

	@LocalizedComment("config.sealingTableBlock")
	private Material sealingTableBlock = Material.SMOKER;

	@LocalizedComment("config.alwaysShowQuality")
	private boolean alwaysShowQuality = true;

	@LocalizedComment("config.alwaysShowAlc")
	private boolean alwaysShowAlc = false;

	@LocalizedComment("config.showBrewer")
	private boolean showBrewer = false;

	@LocalizedComment("config.requireKeywordOnSigns")
	private boolean requireKeywordOnSigns = true;

	@LocalizedComment("config.ageInMCBarrels")
	private boolean ageInMCBarrels = true;
	private int maxBrewsInMCBarrels = 6;

	@LocalizedComment("config.agingYearDuration")
	private int agingYearDuration = 20;

	@LocalizedComment("config.enableEncode")
	private boolean enableEncode = false;
	private long encodeKey = new Random().nextLong(); // Generate a random key


	@LocalizedComment("config.useOtherPlugins")
	private boolean useWorldGuard = true;
	private boolean useLWC = true;
	private boolean useGriefPrevention = true;
	private boolean useTowny = true;
	private boolean useBlockLocker = true;
	private boolean useGMInventories = true;

	@LocalizedComment("config.useVirtualChestPerms")
	private boolean useVirtualChestPerms = false;

	@LocalizedComment("config.useLogBlock")
	private boolean useLogBlock = true;

	@LocalizedComment("config.useOffhandForCauldron")
	private boolean useOffhandForCauldron = false;

	@LocalizedComment("config.loadDataAsync") // Unused, see configlangs/en.yml#config.loadDataAsync comment
	private boolean loadDataAsync = true;

	@LocalizedComment("config.hangoverDays")
	private int hangoverDays = 7;

	@LocalizedComment("config.colorInBarrels-Brewer")
	private boolean colorInBarrels = true;
	private boolean colorInBrewer = true;

	@LocalizedComment("config.openLargeBarrelEverywhere")
	private boolean openLargeBarrelEverywhere = true;

	@LocalizedComment("config.brewHopperDump")
	private boolean brewHopperDump = true;

	@LocalizedComment("config.enableChatDistortion")
	private boolean enableChatDistortion = true;

	@LocalizedComment("config.logRealChat")
	private boolean logRealChat = false;

	@LocalizedComment("config.distortCommands")
	private List<String> distortCommands = List.of("/gl", "/global", "/fl", "/s", "/letter", "/g", "/l", "/lokal",
		"/local", "/mail send", "/m", "/msg", "/w", "/whisper", "/reply", "/r", "/t", "/tell");

	@LocalizedComment("config.distortSignText")
	private boolean distortSignText = false;

	@LocalizedComment("config.distortBypass")
	private List<String> distortBypass = List.of("*,*", "[,]");


	@LocalizedComment("config.words")
	private List<ConfigDistortWord> words = new WordsSector().getCapsules().values().stream().toList();


	@LocalizedComment("config.useOtherFiles")
	private Map<String, ConfigCustomItem> customItems = Map.of("useOtherFiles", new ConfigCustomItem());
	private Map<String, ConfigCauldronIngredient> cauldron = new HashMap<>();
	private Map<String, ConfigRecipe> recipes = Map.of();

}
