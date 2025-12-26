package dev.dong.cerg.content;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import dev.dong.cerg.CErg;
import dev.dong.cerg.CErgKeys;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent.Context;

public class KeyPressStatePacket extends SimplePacketBase {
    private static final ResourceLocation ID = CErg.asResource("key_state_sync");
    private final boolean isPressed;
    private final int keybind;

    public KeyPressStatePacket(FriendlyByteBuf buf) {
        this.isPressed = buf.readBoolean();
        this.keybind = buf.readInt();
    }

    // 自定义构造器
    public KeyPressStatePacket(CErgKeys keybind, boolean isPressed) {
        this.isPressed = isPressed;
        this.keybind = keybind.ordinal();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(isPressed);
        buffer.writeInt(keybind);
    }

    @Override
    public boolean handle(Context context) {
        context.enqueueWork(() -> setKeyState(context));
        return true;
    }

    private void setKeyState(Context context) {
        var player = context.getSender();
        if (player == null) return;

        var key = CErgKeys.indexOf(this.keybind);
        if (key == null) return;

        PlayerKeyStates.setKeyState(player, key, isPressed);
    }
}
