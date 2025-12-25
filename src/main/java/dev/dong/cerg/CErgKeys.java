package dev.dong.cerg;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.jarjar.nio.util.Lazy;

import java.util.Optional;

public enum CErgKeys {
    /**
     * 按住连锁
     */
    CHAIN_ENCASE("chain_encase", 96);

    private final Lazy<KeyMapping> keybind;

    CErgKeys(String desc, int defaultKey) {
        this.keybind = Lazy.of(new KeyMapping(
                CErg.ID + ".key." + desc,
                defaultKey,
                CErg.ID + ".name"));
    }

    public static void register(RegisterKeyMappingsEvent event) {
//        key.keybind = new KeyMapping(key.description, key.key, category);
        for (var key : values()) event.register(key.keybind.get());
    }

    public static CErgKeys indexOf(int i) {
        return switch (i) {
            case 0 -> CHAIN_ENCASE;
            default -> null;
        };
    }

    public static Optional<CErgKeys> getByCode(int code) {
        for (CErgKeys k : values())
            if (k.keybind.get().getKey().getValue() == code)
                return Optional.of(k);
        return Optional.empty();
    }

    public boolean isDown() {
        KeyMapping bind = keybind.get();
        if (bind.getKey().equals(InputConstants.UNKNOWN)) return false;
        return bind.isDown();
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
