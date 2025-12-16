package dev.dong.cerg.mixin.tools;

import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DepotBehaviour.class, remap = false)
public abstract class DepotBehaviourMixin {
    @Shadow
    boolean allowMerge;

    @Inject(method = "write", at = @At("HEAD"))
    private void writeAllowMerge(CompoundTag compound, boolean clientPacket, CallbackInfo callbackInfo) {
        compound.putBoolean("allowMerge", allowMerge);
    }

    @Inject(method = "read", at = @At("HEAD"))
    private void readAllowMerge(CompoundTag compound, boolean clientPacket, CallbackInfo callbackInfo) {
        allowMerge = compound.getBoolean("allowMerge");
    }
}
