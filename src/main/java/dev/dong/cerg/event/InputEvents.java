package dev.dong.cerg.event;

import dev.dong.cerg.CErgKeys;
import dev.dong.cerg.CErgPackets;
import dev.dong.cerg.content.KeyPressStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;


public class InputEvents {
    private static int preSyncTick = 0;

    public static void onKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().screen != null)
            return;
        CErgKeys.getByCode(event.getKey()).ifPresent(k -> CErgPackets.sendToServer(
                new KeyPressStatePacket(k, event.getAction() > 0)));
    }

    /**
     * 监听【连锁套壳】按键状态，同步到服务端
     */
    public static void listenerKeyChainEncase(TickEvent.ClientTickEvent event) {
        if (++preSyncTick < 4) return;
        preSyncTick = 0;

        CErgPackets.sendToServer(new KeyPressStatePacket(CErgKeys.CHAIN_ENCASE, CErgKeys.CHAIN_ENCASE.isDown()));
    }
}
