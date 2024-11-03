---
description: How to edit config.yml in BreweryX
---

# Configuring the config

The Brewery<span class="neon">X</span> config has a lot of its own jokes, which we will now analyze.yze.

***

## Language: `language`

The BreweryX message language. It can be any of the `BreweryX/languages/` folder.

Current languages: `en`, `ru`, `de`, `es`, `fr`, `it`, `tw` and `zh`

***

## Data storage: `storage`

How to store every BreweryX's data.

`type` - type of data storage, could be:

- `FlatFile` - like in OG Brewery - just an `.yml` file.

- `SQLite` - simple database within a `.db` file

- `MySQL` - big database

Next parameters will work only if `MySQL` is choosen:

- `database` - da name of database

- `tablePrefix` - prefix of tables

- `address` - ip address of mysql databse

- `username` - database access login

- `password` - database access password

***

## Plugin prefix: `pluginPrefix`

This is what will be written before the plugin message. 

Default value: `pluginPrefix: '&2[Brewery]&f "`

***

## Home type: `homeType`

This is the command that will be registered on behalf of the player when he gets drunk and logs back on the server.

Default value: `homeType: 'cmd: home"`

***

## Consequences of drinking: `enableWake`

`true` - the player will wake up at random points that were set by the `/brew Wakeup add` command by the admin after a hard drinking session and some time offline

`false` - disables this feature

Default value: `enableWake: true`

***

## Difficulty logging in: `enableLoginDisallow`

`true` - the player will need to log in to the server several times if he is extremely drunk

`false` - disables this feature

***

## Consequences of Drinking 2.0: `enableKickOnOverdrink`

`true` - kicks the player if he ** got too** fast

`false` - disables this feature

***

## Vomit: `enablePuke`

`true` - if the player is very drunk, he will "vomit" items defined in `pukeItem`

`false` - disables this feature

***

## Type of vomit: `pukeItem`

A comma-separated list of items that the player will burp with if `enablePuke` is enabled

Default value: `pukeItem: [Soul_Sand]`

***

## Vomit disappearing time: `pukeDespawnTime'

How many seconds will vomit items disappear.

Default value: `pukeDespawntime: 60`

***

## How much is a year for BreweryX: `agingYearDuration`

ow many days the Brewery will count for an year.

Default value: 20

***


***

## Difficulty walking: `stumblePercent`

The number of how difficult it is for a player to walk when heavily intoxicated, from `0` to `1000`

Default value: `stumblePercent: 100`

***

## Alcohol level when drinking: `showStatusOnDrink`

`true` - to show the player how drunk he is when drinking

`false` - do not show

***

## Withdrawal of intoxication: `drainItems`

A list of vanilla items that remove intoxication in the format `Item Name/How many intoxication points does it take off`

Default values:

```yaml
drainItems:
  - Bread/4
  - Milk_Bucket/2
```

***

## Cauldron Effects: `enableCauldronParticles`

`true` - the Cauldronhas effects

`false` - no

***

## Number of Cauldron particles: `minimalParticles`

`true` - reduce the number of Cauldron particles to a minimum

`false` - leave everything as it is

***

## Craft Sealing Table: `craftSealingTable`

`true` - sealing table crafting is enabled

`false` - disabled

***

## Is the Sealing table enabled: `enableSealingTable`

`true` - the sealing table can be used

`false` - not allowed

***

## Show the quality of the drink: `alwaysShowQuality`

`true` - always show the quality of the drink in the lore of the drink

`false` - only during fermentation/distillation

***

## Show the alcohol level of the drink: `alwaysShowAlc`

`true` - always show the alcohol level of the drink in the lore of the drink

`false` - only during distillation

***

## Show who brewed the drink: `showBrewer`

`true` - show who brewed the drink in the lore of the drink

`false` - do not show

***

## Is it possible to ferment in Minecraft barrels: `ageInMCBarrels`

`true` - drinks can be fermented in a regular barrel

`false` - not allowed

***

## How many slots are in a regular barrel: `maxBrewsInMCBarrels`

The number is how many slots are available in a regular barrel.

Default value: `maxBrewsInMCBarrels: 6`

***

## Encoding of drinks: `enableEncode`

This setting (if set to `true`) allows you to encode the components (NBT) of the drink so that cheaters cannot find out what kind of drink it is, who brewed it and what its exact recipe is.

**Important**! With the setting enabled, drinks can only be stored on one server. All encoded drinks will not be able to be transmitted through worlds, their downloads, schematics, etc.

***

## Encoding type: `encodeKey`

Like a minecraft seed, but for `enableEncode`.

If you want to use same drinks on multiple server connected via Bungee or Velocity, this parameter have to be the same on every server.

***

## Checking for updates: `updateCheck`

Whether to check for BreweryX updates

***

## Autosave: `autosave`

The number in minutes, once in how many minutes to save BreweryX data

***

## Debug: `debug`

Whether to enable the plugin's debug

***

## Config version: `version`

<span class="red bold">NEVER TOUCH.</span>

***

## Confusing chat: `words`

This setting is located at the very end of the config, it allows you to change chat messages from drunk players.

An example of the setup looks like this:

```yaml
words:
- replace: e # Replace all letters "e"
to: EEE # with "EEE"
percentage: 90 # Chance that the letter "e" will be replaced
alcohol: 30 # What alcohol level will this setting work on?

- replace: y # Replace all letters "y"
   to: YY # On "YY"
   pre: y # If there was a letter "y" before it
   match: false # Should the letter "y" be before "y" for this setting to work
   alcohol: 10 # What alcohol level will this setting work on?
   percentage: 70 # Chance that the letter "y" will be replaced
```

***

## To be continued...