# How to make Brewery compatible with Multipaper.

In Brewery, mutability and accessibility is extremely important. For this example, let's take a look at one of Brewery's objects and see how Brewery
uses the object by itself.

### The Barrel
- Brewery constantly ticks to age potions inside of it
- Brewery constantly ticks to check if it's been broken
- Brewery mutates frequently when players add/remove ItemStacks

Brewery relies on a single ArrayList to store all the Barrels that exist to the plugin.

For any other server software, this isn't a problem. There can only ever be one of these lists to exist and only one Brewery plugin will ever be loaded on the server at runtime.
For MultiPaper, this won't work. Brewery needs to share this list of Barrels between every instance of the plugin.

### The original solution:
The original solution to this problem is to use **Redis**. With Redis, we can have every single instance of Brewery use a single list.
Unfortunately, this solution won't work practically. Remember, Brewery constantly ticks and mutates these barrels.

**The problems that arise:**
- Multiple plugins are now ticking the same Barrel all at once
- Multiple plugins can mutate the same barrel at the same time
- Operations have to be done in `MutableOperations`. Meaning that Brewery can no longer just do `Barrel#get` and start mutating that barrel, Brewery now has to do all of its operations within that method so the changes can be pushed back to Redis. (Aka. `Barrel#get` now becomes `Barrel#mutate(MutableOperation)`)*****
- Race conditions. What happens when `Brewery A` wants to mutate one Barrel while another `Brewery B` is already doing an operation on it? `Brewery A` will get an outdated version of the Barrel and our Barrel will lose important data once `Brewery B` finishes its operation and pushes back to Redis.


### The second solution:
This idea comes from how MultiPaper handles chunks.

Give each Barrel an owner. Brewery will ONLY cache, tick, and mutate Barrels it owns. When a player interacts with a Barrel from `Brewery A`. `Brewery A` will check its cache to see if it owns the Barrel. If it doesn't `Brewery A` will publish a message on Redis requesting the Barrel if `Brewery B` is doing an operation on it. `Brewery A` will need to wait or tell the player that another player from `Brewery B` is using that Barrel, and they must wait until they're finished.
If all checks pass, `Brewery B` will give up ownership of the Barrel and `Brewery A` will take over. `Brewery A`  will handle all necessary tasks with the Barrel until another player from a different Brewery instance wants to use that Barrel.

***** Example of how Brewery was intended to use MutableOperations:
```java
    public void mutateBarrelList(MutableOperation operation) {
        try (Jedis redis = redisPool.getResource()) {
            List<Barrel> barrels = SerializableBarrel.toBarrels(
                    serializer.deserializeArray(
                            redis.lrange(BARRELS, 0, -1), SerializableBarrel.class)
                    )
                    .stream()
                    .filter(Objects::nonNull)
                    .toList();

            if (operation != null) {
                operation.go(barrels);
            }

            // save back to redis
            redis.lpush(BARRELS, serializer.serializeArray(SerializableBarrel.fromBarrels(barrels)));
        } catch (Exception e) {
            plugin.errorLog("Redis failed to get barrels!", e);
        }
    }
```