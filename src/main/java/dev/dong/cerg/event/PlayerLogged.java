package dev.dong.cerg.event;

import dev.dong.cerg.content.PlayerKeyStates;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;

public class PlayerLogged {

    public static void playerLoggedOut(PlayerLoggedOutEvent event) {
        PlayerKeyStates.onPlayerLogout(event.getEntity());
    }
}
