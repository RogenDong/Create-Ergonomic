package dev.dong.cerg;

import com.mojang.logging.LogUtils;
import dev.dong.cerg.content.PlayerInteract;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        var gameEventBus = MinecraftForge.EVENT_BUS;
        // Register ourselves for server and other game events we are interested in
        gameEventBus.addListener(this::onServerStarting);
        gameEventBus.addListener(PlayerInteract::rightClick);
    }

    // Register the commonSetup method for modloading
    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    // Register the item to a creative tab
    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
    }
}
