package com.dre.brewery.hazelcast;

import com.dre.brewery.BreweryPlugin;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;

public class HazelcastMemberShipListener implements MembershipListener {
    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        // do nothing with this for now, we handle init stuff in onEnable
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        BreweryHazelcast.hazelcastLog("Hazelcast node &6"  + membershipEvent.getMember().getUuid() + "&d has disconnected! Balancing Objects now...");
        HazelcastCacheManager.balanceAll();
    }
}
