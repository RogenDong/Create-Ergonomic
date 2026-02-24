package dev.dong.cerg;

import com.mojang.logging.LogUtils;
import dev.dong.cerg.event.ClientEvents;
import dev.dong.cerg.event.PlayerInteract;
import dev.dong.cerg.event.PlayerLogged;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(CErg.ID)
public class CErg {
    public static final String ID = "create_ergonomic";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final CErgConfig CONFIG = AutoConfig.register(CErgConfig.class, Toml4jConfigSerializer::new).getConfig();

    public CErg(ModContainer container, Dist dist) {
        CONFIG.register(container);
        var modEventBus = container.getEventBus();
        var gameEventBus = NeoForge.EVENT_BUS;
        modEventBus(modEventBus);
        gameEventBus(gameEventBus);
        if (dist == Dist.CLIENT) onClient(modEventBus, gameEventBus);
    }

    private void modEventBus(IEventBus modEventBus) {
        CErgPackets.register();
    }

    private void gameEventBus(IEventBus gameEventBus) {
        gameEventBus.addListener(PlayerInteract::rightClick);
        gameEventBus.addListener(PlayerLogged::playerLoggedOut);
    }

    private static void onClient(IEventBus modEventBus, IEventBus gameEventBus) {
        modEventBus.addListener(CErgKeys::register);
        gameEventBus.addListener(PlayerInteract::onClientClickInput);
        gameEventBus.addListener(ClientEvents::onTick);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }
}
