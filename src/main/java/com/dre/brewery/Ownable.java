package com.dre.brewery;

import java.util.UUID;

public interface Ownable {
    void setOwner(UUID owner);
    UUID getOwner();
}
