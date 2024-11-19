package com.dre.brewery.configuration.files;

import com.dre.brewery.configuration.AbstractOkaeriConfigFile;
import com.dre.brewery.configuration.configurer.BreweryXConfigurer;
import com.dre.brewery.configuration.configurer.TranslationManager;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Exclude;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Lang extends AbstractOkaeriConfigFile {

    @Getter @Exclude
    private static final Lang instance = createConfig(Lang.class,
            TranslationManager.getInstance().getActiveTranslation().getFilename(),
            new BreweryXConfigurer());

    // I shouldn't have to set any declarations here since they'll all be pulled from the translation files.

    @Comment("Brew")
    @CustomKey("Brew_-times")
    private String brewTimes;
    @CustomKey("Brew_BadPotion")
    private String brewBadPotion;
    @CustomKey("Brew_BarrelRiped")
    private String brewBarrelRiped;
    @CustomKey("Brew_DistillUndefined")
    private String brewDistillUndefined;
    @CustomKey("Brew_Distilled")
    private String brewDistilled;
    @CustomKey("Brew_LessDistilled")
    private String brewLessDistilled;
    @CustomKey("Brew_HundredsOfYears")
    private String brewHundredsOfYears;
    @CustomKey("Brew_Ingredients")
    private String brewIngredients;
    @CustomKey("Brew_MinutePluralPostfix")
    private String brewMinutePluralPostfix;
    @CustomKey("Brew_OneYear")
    private String brewOneYear;
    @CustomKey("Brew_ThickBrew")
    private String brewThickBrew;
    @CustomKey("Brew_Undefined")
    private String brewUndefined;
    @CustomKey("Brew_Woodtype")
    private String brewWoodtype;
    @CustomKey("Brew_Years")
    private String brewYears;
    @CustomKey("Brew_fermented")
    private String brewFermented;
    @CustomKey("Brew_minute")
    private String brewMinute;
    @CustomKey("Brew_Alc")
    private String brewAlc;
    @CustomKey("Brew_Brewer")
    private String brewBrewer;


    @Comment("CMD")
    @CustomKey("CMD_Copy_Error")
    private String cmdCopyError;
    @CustomKey("CMD_Info_Drunk")
    private String cmdInfoDrunk;
    @CustomKey("CMD_Info_NotDrunk")
    private String cmdInfoNotDrunk;
    @CustomKey("CMD_NonStatic")
    private String cmdNonStatic;
    @CustomKey("CMD_Player")
    private String cmdPlayer;
    @CustomKey("CMD_Player_Error")
    private String cmdPlayerError;
    @CustomKey("CMD_Reload")
    private String cmdReload;
    @CustomKey("CMD_Created")
    private String cmdCreated;
    @CustomKey("CMD_Configname")
    private String cmdConfigname;
    @CustomKey("CMD_Configname_Error")
    private String cmdConfignameError;
    @CustomKey("CMD_Static")
    private String cmdStatic;
    @CustomKey("CMD_UnLabel")
    private String cmdUnLabel;
    @CustomKey("CMD_Drink")
    private String cmdDrink;
    @CustomKey("CMD_DrinkOther")
    private String cmdDrinkOther;
    @CustomKey("CMD_Set")
    private String cmdSet;


    @Comment("Error")
    @CustomKey("Error_ConfigUpdate")
    private String errorConfigUpdate;
    @CustomKey("Error_ItemNotPotion")
    private String errorItemNotPotion;
    @CustomKey("Error_SealingTableDisabled")
    private String errorSealingTableDisabled;
    @CustomKey("Error_SealedAlwaysStatic")
    private String errorSealedAlwaysStatic;
    @CustomKey("Error_AlreadyUnlabeled")
    private String errorAlreadyUnlabeled;
    @CustomKey("Error_NoBarrelAccess")
    private String errorNoBarrelAccess;
    @CustomKey("Error_NoBrewName")
    private String errorNoBrewName;
    @CustomKey("Error_NoPermissions")
    private String errorNoPermissions;
    @CustomKey("Error_PlayerCommand")
    private String errorPlayerCommand;
    @CustomKey("Error_Recipeload")
    private String errorRecipeload;
    @CustomKey("Error_ShowHelp")
    private String errorShowHelp;
    @CustomKey("Error_UnknownCommand")
    private String errorUnknownCommand;
    @CustomKey("Error_YmlRead")
    private String errorYmlRead;
    @CustomKey("Error_NoPlayer")
    private String errorNoPlayer;


    @Comment("Etc")
    @CustomKey("Etc_Barrel")
    private String etcBarrel;
    @CustomKey("Etc_Page")
    private String etcPage;
    @CustomKey("Etc_Usage")
    private String etcUsage;
    @CustomKey("Etc_SealingTable")
    private String etcSealingTable;
    @CustomKey("Etc_UpdateAvailable")
    private String etcUpdateAvailable;


    @Comment("Help")
    @CustomKey("Help_Copy")
    private String helpCopy;
    @CustomKey("Help_Create")
    private String helpCreate;
    @CustomKey("Help_Give")
    private String helpGive;
    @CustomKey("Help_Delete")
    private String helpDelete;
    @CustomKey("Help_Help")
    private String helpHelp;
    @CustomKey("Help_Info")
    private String helpInfo;
    @CustomKey("Help_InfoOther")
    private String helpInfoOther;
    @CustomKey("Help_Reload")
    private String helpReload;
    @CustomKey("Help_Configname")
    private String helpConfigname;
    @CustomKey("Help_Static")
    private String helpStatic;
    @CustomKey("Help_Set")
    private String helpSet;
    @CustomKey("Help_UnLabel")
    private String helpUnLabel;
    @CustomKey("Help_Seal")
    private String helpSeal;
    @CustomKey("Help_Wakeup")
    private String helpWakeup;
    @CustomKey("Help_WakeupAdd")
    private String helpWakeupAdd;
    @CustomKey("Help_WakeupCheck")
    private String helpWakeupCheck;
    @CustomKey("Help_WakeupCheckSpecific")
    private String helpWakeupCheckSpecific;
    @CustomKey("Help_WakeupList")
    private String helpWakeupList;
    @CustomKey("Help_WakeupRemove")
    private String helpWakeupRemove;
    @CustomKey("Help_Puke")
    private String helpPuke;
    @CustomKey("Help_Drink")
    private String helpDrink;


    @Comment("Perms")
    @CustomKey("Perms_NoBarrelCreate")
    private String permsNoBarrelCreate;
    @CustomKey("Perms_NoBigBarrelCreate")
    private String permsNoBigBarrelCreate;
    @CustomKey("Perms_NoCauldronFill")
    private String permsNoCauldronFill;
    @CustomKey("Perms_NoCauldronInsert")
    private String permsNoCauldronInsert;
    @CustomKey("Perms_NoSmallBarrelCreate")
    private String permsNoSmallBarrelCreate;


    @Comment("Player")
    @CustomKey("Player_BarrelCreated")
    private String playerBarrelCreated;
    @CustomKey("Player_BarrelFull")
    private String playerBarrelFull;
    @CustomKey("Player_CantDrink")
    private String playerCantDrink;
    @CustomKey("Player_CauldronInfo1")
    private String playerCauldronInfo1;
    @CustomKey("Player_CauldronInfo2")
    private String playerCauldronInfo2;
    @CustomKey("Player_DrunkPassOut")
    private String playerDrunkPassOut;
    @CustomKey("Player_LoginDeny")
    private String playerLoginDeny;
    @CustomKey("Player_LoginDenyLong")
    private String playerLoginDenyLong;
    @CustomKey("Player_TriedToSay")
    private String playerTriedToSay;
    @CustomKey("Player_Wake")
    private String playerWake;
    @CustomKey("Player_WakeAlreadyDeleted")
    private String playerWakeAlreadyDeleted;
    @CustomKey("Player_WakeCancel")
    private String playerWakeCancel;
    @CustomKey("Player_WakeCreated")
    private String playerWakeCreated;
    @CustomKey("Player_WakeDeleted")
    private String playerWakeDeleted;
    @CustomKey("Player_WakeFilled")
    private String playerWakeFilled;
    @CustomKey("Player_WakeHint1")
    private String playerWakeHint1;
    @CustomKey("Player_WakeHint2")
    private String playerWakeHint2;
    @CustomKey("Player_WakeLast")
    private String playerWakeLast;
    @CustomKey("Player_WakeNoCheck")
    private String playerWakeNoCheck;
    @CustomKey("Player_WakeNoPoints")
    private String playerWakeNoPoints;
    @CustomKey("Player_WakeNotExist")
    private String playerWakeNotExist;
    @CustomKey("Player_WakeTeleport")
    private String playerWakeTeleport;
    @CustomKey("Player_ShopSealBrew")
    private String playerShopSealBrew;
    @CustomKey("Player_Drunkeness")
    private String playerDrunkeness;
    @CustomKey("Player_Hangover")
    private String playerHangover;

}
