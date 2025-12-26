package dev.dong.cerg.event;

import dev.dong.cerg.CErgKeys;
import dev.dong.cerg.CErgPackets;
import dev.dong.cerg.content.KeyPressStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;

public class InputEvents {
    private static long preSyncTime = 0;

    public static void onKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().screen != null)
            return;
        CErgKeys.getByCode(event.getKey()).ifPresent(k -> CErgPackets.sendToServer(new KeyPressStatePacket(k, event.getAction() > 0)));
    }

    /**
     * 监听【连锁套壳】按键状态，同步到服务端
     */
    public static void listenerKeyChainEncase(TickEvent.ClientTickEvent event) {
        long now = System.currentTimeMillis();
        if (now < preSyncTime) return;
        preSyncTime = now + 200;// 4gt

        boolean pressed = CErgKeys.CHAIN_ENCASE.isDown();
        CErgPackets.sendToServer(new KeyPressStatePacket(CErgKeys.CHAIN_ENCASE, pressed));
    }

//    private static long preTipTime = 0;
//    private static final Component TIP_ENCASE = Component.literal("连锁套壳").withStyle(ChatFormatting.GREEN);
//    private static final Component TIP_DECASE = Component.literal("连锁拆壳").withStyle(ChatFormatting.GREEN);
//
//    public static void showKeyTipChainEncase(TickEvent.ServerTickEvent event) {
//        long now = System.currentTimeMillis();
//        if (now < preTipTime) return;
//        preTipTime = now + 500;
//
//        MinecraftServer server = event.getServer();
//        if (server == null) return;
//
//        List<ServerPlayer> players = server.getPlayerList().getPlayers();
//        if (players.isEmpty()) return;
//
//        for (ServerPlayer player : players) {
//            if (!PlayerKeyStates.isKeyPressed(player, CErgKeys.CHAIN_ENCASE)) continue;
//            ItemStack held = player.getMainHandItem();
//            if (held.isEmpty()) continue;
//
//            if (AllItems.WRENCH.isIn(held))
//                player.displayClientMessage(TIP_DECASE, true);
//            if (AllBlocks.ANDESITE_CASING.isIn(held) || AllBlocks.BRASS_CASING.isIn(held) || AllBlocks.COPPER_CASING.isIn(held))
//                player.displayClientMessage(TIP_ENCASE, true);
//        }
//    }
}
