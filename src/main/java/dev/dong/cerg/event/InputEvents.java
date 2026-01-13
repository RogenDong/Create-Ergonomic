package dev.dong.cerg.event;

import dev.dong.cerg.CErgClient;
import dev.dong.cerg.CErgKeys;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;


public class InputEvents {

    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;

        KeyMapping key = event.getKeyMapping();

        boolean isUse = key == mc.options.keyUse;
        if (isUse || key == mc.options.keyAttack) {
            if (CErgKeys.CHAIN_ENCASE.isDown()) {
                if (CErgClient.CLIPBOARD_HANDLER.onMouseInput())
                    event.setCanceled(true);
            } else if (mc.player.isShiftKeyDown() && isUse) {
//                if (CErgClient.CLIPBOARD_HANDLER.sneakClickWhenSelecting())
//                    event.setCanceled(true);
            }
        }
    }
}
