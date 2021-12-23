package jackiecrazy.softborder;

import jackiecrazy.softborder.capability.BorderStorage;
import jackiecrazy.softborder.capability.BorderTracker;
import jackiecrazy.softborder.capability.IBorderTracker;
import jackiecrazy.softborder.config.BorderConfig;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("softborder")
public class SoftBorder {
    public static final String MODID = "softborder";
    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, MODID);
    public static final RegistryObject<Effect> BORDER_ANCHOR = EFFECTS.register("border_anchor", BorderAnchorEffect::new);
    public static final RegistryObject<Effect> DISINTEGRATION = EFFECTS.register("disintegration", DisintegrationEffect::new);
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public SoftBorder() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.addListener(this::commands);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, BorderConfig.CONFIG_SPEC);
        EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        CapabilityManager.INSTANCE.register(IBorderTracker.class, new BorderStorage(), BorderTracker::new);
        CHANNEL.registerMessage(0, SyncPacket.class, new SyncPacket.SpawnSyncEncoder(), new SyncPacket.SpawnSyncDecoder(), new SyncPacket.SpawnSyncHandler());
    }

    private void commands(final RegisterCommandsEvent event) {
        BorderCommand.register(event.getDispatcher());
    }
}
