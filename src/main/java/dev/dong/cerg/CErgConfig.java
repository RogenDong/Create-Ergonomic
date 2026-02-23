package dev.dong.cerg;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.TransitiveObject;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;

@Config(name = CErg.ID)
public class CErgConfig implements ConfigData {
    /**
     * 常规设置
     */
    @Category("general")
    @TransitiveObject
    public final General general = new General();

    /**
     * 设置连锁范围
     */
    @Category("finiteChain")
    @TransitiveObject
    public final FiniteChain finiteChain = new FiniteChain();

    public void register(@NotNull ModContainer container) {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        IConfigScreenFactory factory = (modContainer, screen) -> AutoConfig.getConfigScreen(CErgConfig.class, screen).get();
        container.registerExtensionPoint(IConfigScreenFactory.class, factory);
    }

    public static class General {
        /**
         * 允许切换置物台物品合并开关
         */
        @Tooltip
        public boolean enableDepotMerge = true;
        /**
         * 允许连锁套壳/拆壳
         */
        @Tooltip
        public boolean enableChainEncase = true;
        /**
         * 允许连锁切换管道or玻璃管道
         */
        @Tooltip
        public boolean enableChainTogglePipes = true;
    }

    public static class FiniteChain {
        /**
         * 轴向连锁上限
         */
        @Tooltip
        @BoundedDiscrete(min = 8, max = 128)
        public int axialDistance = 64;

        /**
         * 管道连锁上限
         */
        @Tooltip
        @BoundedDiscrete(min = 8, max = 256)
        public int pipeMaxDistance = 64;
    }
}
