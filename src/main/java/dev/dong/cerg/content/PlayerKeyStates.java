package dev.dong.cerg.content;

import dev.dong.cerg.CErgKeys;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储玩家按键状态（服务端）
 */
public class PlayerKeyStates {
    private static final Map<UUID, Map<CErgKeys, Boolean>> PLAYER_KEY_STATES = new ConcurrentHashMap<>(20);

    public static void setKeyState(Player player, CErgKeys keybind, boolean isPressed) {
        PLAYER_KEY_STATES.computeIfAbsent(player.getUUID(), k -> new HashMap<>())
                .put(keybind, isPressed);
    }

    public static boolean isKeyPressed(Player player, CErgKeys keybind) {
        var tmp = PLAYER_KEY_STATES.get(player.getUUID());
        if (tmp == null) return false;
        return tmp.getOrDefault(keybind, false);
    }

    public static void onPlayerLogout(Player player) {
        PLAYER_KEY_STATES.remove(player.getUUID());
    }
}
