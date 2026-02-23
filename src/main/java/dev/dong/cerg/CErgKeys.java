package dev.dong.cerg;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import java.util.Optional;

public enum CErgKeys {
    /**
     * 按住连锁
     */
    CHAIN_ENCASE("chain_encase", 96);

    private final String name;
    private final int defaultKey;
    private final String description;
    private KeyMapping keybind;

    CErgKeys(String desc, int defaultKey) {
        this.name = CErg.ID + ".key." + desc;
        this.defaultKey = defaultKey;
        this.description = desc;
    }

    public static void register(RegisterKeyMappingsEvent event) {
        for (var key : CErgKeys.values()) {
            key.keybind = new KeyMapping(
                    CErg.ID + ".key." + key.description,
                    key.defaultKey,
                    CErg.ID + ".name");
            event.register(key.keybind);
        }
    }

    public static CErgKeys indexOf(int i) {
        CErgKeys[] keys = values();
        if (i < 0 || i >= keys.length) return null;
        return keys[i];
    }

    public static Optional<CErgKeys> getByCode(int code) {
        for (var k : values())
            if (k.keybind.getKey().getValue() == code)
                return Optional.of(k);
        return Optional.empty();
    }

    public boolean isDown() {
        if (keybind.getKey().equals(InputConstants.UNKNOWN)) return false;
        return keybind.isDown();
    }

    public String getName() {
        return this.name;
    }

    public static boolean ctrlDown() {
        return Screen.hasControlDown();
    }

    public static boolean shiftDown() {
        return Screen.hasShiftDown();
    }

    public static boolean altDown() {
        return Screen.hasAltDown();
    }
}
