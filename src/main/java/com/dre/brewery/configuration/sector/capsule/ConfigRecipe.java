package com.dre.brewery.configuration.sector.capsule;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ConfigRecipe extends OkaeriConfig {

    private String name;
    private List<String> ingredients;

    @CustomKey("cookingtime")
    private Integer cookingTime;
    @CustomKey("distillruns")
    private Integer distillRuns;
    @CustomKey("distilltime")
    private Integer distillTime;
    private Integer wood;
    private Integer age;
    private String color;
    private Integer difficulty;
    private Integer alcohol;
    private List<String> lore;
    @CustomKey("servercommands")
    private List<String> serverCommands;
    @CustomKey("playercommands")
    private List<String> playerCommands;
    @CustomKey("drinkmessage")
    private String drinkMessage;
    @CustomKey("drinktitle")
    private String drinkTitle;
    private Boolean glint;
    private String customModelData;
    private List<String> effects;
}