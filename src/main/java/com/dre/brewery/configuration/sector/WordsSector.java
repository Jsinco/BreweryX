package com.dre.brewery.configuration.sector;

import com.dre.brewery.configuration.sector.capsule.ConfigWordAlter;

public class WordsSector extends AbstractOkaeriConfigSector<ConfigWordAlter> {

    // Doesn't matter what the fields are named in this sector

    ConfigWordAlter a = ConfigWordAlter.builder()
            .replace("s")
            .to("sh")
            .percentage(90)
            .alcohol(30)
            .build();

    ConfigWordAlter b = ConfigWordAlter.builder()
            .replace("ch")
            .to("sh")
            .pre("u,s,o,a")
            .match(false)
            .alcohol(10)
            .percentage(70)
            .build();

    ConfigWordAlter c = ConfigWordAlter.builder()
            .replace("h")
            .to("hh")
            .pre("sch,h,t")
            .match(false)
            .percentage(60)
            .alcohol(20)
            .build();
}
