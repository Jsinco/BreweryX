
# Using custom items

You can define custom items that can be used for brewing.

Custom items will look like this:

```yaml
customItems:
  # Three Example Items
  ex-item:
    material: Barrier # One or list of materials that can be counted as "ex-item"
    name: 'Wall' # "ex-item" will be counted only if item have "Wall" name
    lore:
      - '&7Very well protected' # "ex-item" will be counted only if item have "&7Very well protected" lore
```

### `matchAny`

`true`/`false` - if you need to match only one param of custom item.

### `material`

Material or list of material that should be counted as custom item

### `name`

Custom name that item has to have to be counted as custom item

### `lore`

Custom lore that item has to have to be counted as custom item

### `customModelData`

Custom model data that item has to have to be counted as custom item

## Example of usage

```yaml
customItems:
    blue_flowers:
      matchAny: true
      material:
        - cornflower
        - blue_orchid

cauldron:
    TestBrew:
        name: Some Brew
        ingredients:
            - blue_flowers/2
```