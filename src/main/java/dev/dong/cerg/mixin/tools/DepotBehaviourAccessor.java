package dev.dong.cerg.mixin.tools;

import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = DepotBehaviour.class, remap = false)
public interface DepotBehaviourAccessor {
    @Accessor
    void setAllowMerge(boolean value);
}
