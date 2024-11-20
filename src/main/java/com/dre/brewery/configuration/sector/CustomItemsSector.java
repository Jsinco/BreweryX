package com.dre.brewery.configuration.sector;

import com.dre.brewery.configuration.sector.capsule.ConfigCustomItem;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomItemsSector extends AbstractOkaeriConfigSector<ConfigCustomItem> {

	@CustomKey("ex-item")
	ConfigCustomItem ex_item = ConfigCustomItem.builder()
			.material("Barrier")
			.name("Wall")
			.lore(List.of("&7Very well protected"))
			.build();

	@CustomKey("ex-item2")
	ConfigCustomItem ex_item2 = ConfigCustomItem.builder()
			.matchAny(true)
			.material(List.of("Acacia_Door", "Oak_Door", "Spruce_Door"))
			.name(List.of("Beechwood Door"))
			.lore(List.of("A door"))
			.build();

	ConfigCustomItem rasp = ConfigCustomItem.builder()
			.name("&cRaspberry")
			.build();

	ConfigCustomItem modelitem = ConfigCustomItem.builder()
			.material("paper")
			.customModelData(List.of(10234, 30334))
			.build();

	@CustomKey("blue-flowers")
	ConfigCustomItem blue_flowers = ConfigCustomItem.builder()
			.matchAny(true)
			.material(List.of("cornflower", "blue_orchid"))
			.build();
}
