package dev.dong.cerg;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.TransitiveObject;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Config(name = CErg.ID)
public class CErgConfig implements ConfigData {
    /**
     * 常规设置
     */
    @Category("general")
    @TransitiveObject
    public final General general = new General();

    /**
     * 连锁套壳/拆壳设置
     */
    @Category("chainEncase")
    @TransitiveObject
    public final ChainEncase chainEncase = new ChainEncase();

    public void register() {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        var factory = new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> AutoConfig.getConfigScreen(CErgConfig.class, screen).get());
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> factory);
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

    public static class ChainEncase {
        /**
         * 轴向连锁上限
         */
        @Tooltip
        @BoundedDiscrete(min = 8, max = 128)
        public int pillarMaxDistance = 64;

        /**
         * 管道连锁上限
         */
        @Tooltip
        @BoundedDiscrete(min = 8, max = 256)
        public int pipeMaxDistance = 64;
    }
}
