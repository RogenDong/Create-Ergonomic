package dev.dong.cerg.event;

import dev.dong.cerg.CErgClient;
import dev.dong.cerg.CErgKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered;


public class InputEvents {

    public static void onClickInput(InteractionKeyMappingTriggered event) {
        var mc = Minecraft.getInstance();
        if (mc.screen != null) return;

        var key = event.getKeyMapping();

        boolean isAtk = key == mc.options.keyAttack;
        if ((isAtk || key == mc.options.keyUse)
                && CErgKeys.CHAIN_ENCASE.isDown()
                && event.getHand() == InteractionHand.MAIN_HAND) {
            event.setCanceled(true);
            CErgClient.CLIPBOARD_HANDLER.onMouseInput(isAtk);
        }
    }

}
