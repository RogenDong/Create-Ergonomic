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

        if (key == mc.options.keyUse || key == mc.options.keyAttack) {
            if (CErgKeys.CHAIN_ENCASE.isDown() && CErgClient.CLIPBOARD_HANDLER.onMouseInput())
                event.setCanceled(true);
        }
    }
}
