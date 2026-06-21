package com.suhail.learning.handler.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static com.suhail.learning.Main.updateTickCounter;

public class ServerEvents {
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        updateTickCounter();
    }
}
