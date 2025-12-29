package dev.dong.cerg;

import com.mojang.logging.LogUtils;
import dev.dong.cerg.event.InputEvents;
import dev.dong.cerg.event.PlayerInteract;
import dev.dong.cerg.event.PlayerLogged;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CErg.ID)
public class CErg {
    public static final String ID = "create_ergonomic";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final CErgConfig CONFIG = AutoConfig.register(CErgConfig.class, Toml4jConfigSerializer::new).getConfig();

    public CErg() {
        CONFIG.register();
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        var gameEventBus = MinecraftForge.EVENT_BUS;
        modEventBus(modEventBus);
        gameEventBus(gameEventBus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> onClient(modEventBus, gameEventBus));
    }

    private void modEventBus(IEventBus modEventBus) {
        modEventBus.addListener(this::addCreative);
        CErgPackets.registerPackets();
    }

    private void gameEventBus(IEventBus gameEventBus) {
        gameEventBus.addListener(PlayerInteract::rightClick);
        gameEventBus.addListener(PlayerLogged::playerLoggedOut);
    }

    private static void onClient(IEventBus modEventBus, IEventBus gameEventBus) {
        modEventBus.addListener(CErgKeys::register);
        gameEventBus.addListener(InputEvents::listenerKeyChainEncase);
    }

    // Register the item to a creative tab
    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(ID, path);
    }
}
