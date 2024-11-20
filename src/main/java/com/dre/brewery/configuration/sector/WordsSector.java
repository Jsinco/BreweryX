package com.dre.brewery.configuration.sector;

import com.dre.brewery.configuration.sector.capsule.ConfigDistortWord;

public class WordsSector extends AbstractOkaeriConfigSector<ConfigDistortWord> {

    // TODO: add defaults
    // Doesn't matter what the fields are named in this sector

    ConfigDistortWord a = ConfigDistortWord.builder()
            .replace("s")
            .to("sh")
            .percentage(90)
            .alcohol(30)
            .build();

    ConfigDistortWord b = ConfigDistortWord.builder()
            .replace("ch")
            .to("sh")
            .pre("u,s,o,a")
            .match(false)
            .alcohol(10)
            .percentage(70)
            .build();

    ConfigDistortWord c = ConfigDistortWord.builder()
            .replace("h")
            .to("hh")
            .pre("sch,h,t")
            .match(false)
            .percentage(60)
            .alcohol(20)
            .build();
}
