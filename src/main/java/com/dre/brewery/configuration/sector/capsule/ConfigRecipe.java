package com.dre.brewery.configuration.sector.capsule;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class ConfigRecipe extends OkaeriConfig {

    private String name;
    private List<String> ingredients;

    @CustomKey("cookingtime")
    private int cookingTime;
    @CustomKey("distillruns")
    private int distillRuns;
    @CustomKey("distilltime")
    private int distillTime;
    private byte wood;
    private int age;
    private String color;
    private int difficulty;
    private int alcohol;
    private List<String> lore;
    @CustomKey("servercommands")
    private List<String> serverCommands;
    @CustomKey("playercommands")
    private List<String> playerCommands;
    @CustomKey("drinkmessage")
    private String drinkMessage;
    @CustomKey("drinktitle")
    private String drinkTitle;
    private boolean glint;
    private String customModelData;
    private List<String> effects;
}