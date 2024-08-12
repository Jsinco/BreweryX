
# Creating new recipe

Creating new recipe is somewhat easy with BreweryX. All you need is to follow this guide!

## Name*s* of a drink: `name`

!!! warning ""
    Required at all time

The name of the drink. Can contain three variations, each for different quality of a drink. It'll look like that:

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
```

Name supports ampersand and HEX color codes:

```yaml
ColorfulBrew:
    name: '&8Bad brew/Brew/&#ffb424Good brew'
```

## Ingredients: `ingredients`

!!! warning ""
    Required at all time

Ingredients needed for the brew. Possible values:

- Minecraft item name

- BreweryX brew name

- BreweryX custom item name

- ItemsAdder id of item - only with [IAOraxen Plugin](https://www.spigotmc.org/resources/iaoraxenaddon-breweryx-addon.114778/)

- Oraxen id of item - only with [IAOraxen Plugin](https://www.spigotmc.org/resources/iaoraxenaddon-breweryx-addon.114778/)

Format of item is `Item/Amount`

With Minecraft items this'll look like this:

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    ingredients:
        - Apple/10 # You need 10 apples
        - Brewery:ColorfulBrew/2 # You'll need 2 ColorfulBrew brews from BreweryX
        - blue_flowers/2 # You'll need 2 items defined in brewery's CustomItems
```

## Time to brew: `cookingtime`

How much in minutes player need to brew. Can be checked with right clicking Clock on Cauldron.

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    cookingtime: 12 # 12 minutes
```

## How much distilling: `distillruns`

!!! warning ""
    Not required, unless `distilltime` present

How much times player need to distill brew

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    distillruns: 4
```

## Distill time: `distilltime`

!!! warning ""
    Not required, unless `distillruns` present

How much it will take to distill brew one time

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    distilltime: 10
```

## Type of barrel wood: `wood`

!!! warning ""
    Not required, default value - `0`

Type of barrel wood needed to age brew properly. Posible values:

- 0 - any wood 

- 1 - Birch 

- 2 - Oak 

- 3 - Jungle 

- 4 - Spruce 

- 5 - Acacia 

- 6 - Dark Oak 

- 7 - Crimson 

- 8 - Warped 

- 9 - Mangrove 

- 10 - Cherry 

- 11 - Bamboo

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    wood: 4 # Spruce barrel
```

## Aging time: `age`

!!! warning ""
    Not required

How many MC days brew have to be in barrel for best quality.

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    age: 12 # 12 minecraft days
```

## Color of a drink: `color`

!!! warning ""
    Not required, default value - `WHITE`

Color of a potion. Can be HEX color or `DARK_RED`, `RED`, `BRIGHT_RED`, `ORANGE`, `YELLOW`, `PINK`, `PURPLE`, `BLUE`, `CYAN`, `WATER`, `TEAL`, `OLIVE`, `GREEN`, `LIME`, `BLACK`, `GREY`, `BRIGHT_GREY`, `WHITE`

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    color: RED
    # Or...
    color: '99FF33'
```

## Difficulty of brewing: `difficulty`

!!! warning ""
    Required at all time

Accuracy needed to get good quality, from `1` to `10`, where `1` is very unaccurate and `10` is very precise.

Lower number - easier drink and vica versa.

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    difficulty: 5
```

## Alcohol level: `alcohol`

!!! warning ""
    Not required, default value - `0`

How much alcohol "points" player will get after drinking, from `1` to `100`, where `1` is almost nothing and `100` player will most likely faint

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    alcohol: 45
```

## Lore of brew: `lore`

!!! warning ""
    Not required

Lore of a potion. It may vary depending on the quality of the drink.

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    lore:
    - "This text will always be present"
    - + "This text will be present if brew has bad quality"
    - ++ "This text will be present if brew has normal quality"
    - ++ "This text will be present if brew has good quality"
```

## Executing server commands on drink: `servercommands`

!!! warning ""
    Not required

Commands that will execute as server. They can be executed depending on the quality of the drink.

Adding `\<number>s` at the end of a command will add delay to execution.

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    servercommands:
    - say This will execute no matter what!
    - say This message will be delayed by 5 seconds! \5s
    - + kill %player% # This will execute if brew quality is bad
    - ++ heal %player% # This will execute if brew quality is normal
    - +++ op %player% # This will execute if brew quality is good. Also don't give OP to players =)
```

## Executing commands as player: `playercommands`

!!! warning ""
    Not required

Commands that will execute as player. They can be executed depending on the quality of the drink.

Adding `\<number>s` at the end of a command will add delay to execution.

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    playercommands:
    - me This will execute no matter what!
    - me This message will be delayed by 5 seconds! \5s
    - + suicide # This will execute if brew quality is bad
    - ++ home # This will execute if brew quality is normal
    - +++ give %player% diamond 9999 # This will execute if brew quality is good. 
```

## Message after drinking: `drinkmessage`

!!! warning ""
    Not required

Message that will be sent to a player after drinking brew.

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    drinkmessage: "UR DRUNK!"
```

## Title after drinking: `drinktitle`

!!! warning ""
    Not required

Title message that will be sent to a player after drinking brew.

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    drinktitle: "UR DRUNK AND THIS IS TITLE!"
```

## Glint effect: `glint`

!!! warning ""
    Not required, default value `false`

Whether brew will have glint effect (as it were enchanted)

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    glint: true
```

## CMD of item: `customModelData`

!!! warning ""
    Not required

Custom model data of a brew, can vary based on quality of a brew.

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    customModelData: 1337/1338/1339
```

## Potion effects: `effects`

!!! warning ""
    Not required

Effects that will be given to a player after drinking.

Possible Effects: <https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html>

Level or Duration ranges may be specified with a "-", ex. 'SPEED/1-2/30-40' = lvl 1 and 30 sec at worst and lvl 2 and 40 sec at best

Ranges also work high-low, ex. 'POISON/3-1/20-5' for weaker effects at good quality.

Highest possible Duration: 1638 sec. Instant Effects dont need any duration specified.

```yaml
TestBrew:
    name: Bad Brew/Brew/Good Brew
    effects:
      - FIRE_RESISTANCE/20 # Will be given no matter what
      - WEAKNESS/3-1/50-10 # Better the brew - less Weakness player will have
      - REGENERATION/1-3/10-50 # Vica versa
```

## Examples

```yaml
  g_vodka:
    name: 'Rancid Vodka/&6Golden Vodka/&6Shimmering Golden Vodka'
    ingredients:
      - Potato/10
      - Gold_Nugget/2
    cookingtime: 18
    distillruns: 3
    age: 0
    color: ORANGE
    difficulty: 6
    alcohol: 20
    effects:
      - WEAKNESS/28
      - POISON/4

  fire_whiskey:
    name: Powdery Whiskey/Burning Whiskey/Blazing Whiskey
    ingredients:
      - Wheat/10
      - Blaze_Powder/2
    cookingtime: 12
    distillruns: 3
    distilltime: 55
    wood: 4
    age: 18
    color: ORANGE
    difficulty: 7
    alcohol: 28
    drinkmessage: 'You get a burning feeling in your mouth'

  hot_choc:
    name: Hot Chocolate
    ingredients:
      - cookie/3
    cookingtime: 2
    color: DARK_RED
    difficulty: 2
    effects:
      - FAST_DIGGING/40
```