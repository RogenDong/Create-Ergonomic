package dev.dong.cerg.mixin.tools;

import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DepotBehaviour.class)
public interface DepotBehaviourAccessor {
    @Accessor("allowMerge")
    void cerg_setAllowMerge(boolean value);
}
