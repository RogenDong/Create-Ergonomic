package dev.dong.cerg.event;

import dev.dong.cerg.content.PlayerKeyStates;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerLogged {

    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerKeyStates.onPlayerLogout(event.getEntity());
    }
}
