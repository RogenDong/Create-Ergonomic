package dev.dong.cerg.event;

import dev.dong.cerg.CErgClient;
import dev.dong.cerg.CErgKeys;
import dev.dong.cerg.CErgPackets;
import dev.dong.cerg.content.KeyPressStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent.ClientTickEvent;

@OnlyIn(Dist.CLIENT)
public class ClientEvents {
    private static int preSyncTick = 0;

    public static void onTick(ClientTickEvent event) {
        // 监听【连锁套壳】按键状态，同步到服务端
        if (++preSyncTick >= 4) {
            preSyncTick = 0;
            CErgPackets.sendToServer(new KeyPressStatePacket(CErgKeys.CHAIN_ENCASE, CErgKeys.CHAIN_ENCASE.isDown()));
        }

        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        CErgClient.CLIPBOARD_HANDLER.tick();
    }
}
