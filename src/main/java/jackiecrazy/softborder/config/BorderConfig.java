package jackiecrazy.softborder.config;

import jackiecrazy.softborder.SoftBorder;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mod.EventBusSubscriber(modid = SoftBorder.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BorderConfig {
    public static final BorderConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    private static final String[] FX = {
            //effect, length, potency, start, (optional) end
            "minecraft:unluck, 80, 0, -0.1",
            "minecraft:slowness, 80, 0, 0.2, 0.5",
            "softborder:disintegration, 160, 1, 0.5"
    };
    public static int size, anchorTime;
    public static boolean beacon, dispel;
    public static double perc, max, warperc;
    public static CopyOnWriteArrayList<BorderEffect> effects = new CopyOnWriteArrayList<>();

    static {
        final Pair<BorderConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(BorderConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    private final ForgeConfigSpec.IntValue _defaultSize, _anchorTime;
    private final ForgeConfigSpec.BooleanValue _beacon, _remove;
    private final ForgeConfigSpec.DoubleValue _perc, _max, _warpPerc;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> _effects;

    public BorderConfig(ForgeConfigSpec.Builder b) {
        //feature toggle, resource, defense, compat, stealth, lists
        _defaultSize = b.translation("softborder.config.default").defineInRange("default border size", 5000, 0, Integer.MAX_VALUE);
        _beacon = b.translation("softborder.config.beacon").comment("whether beacons act as border emitters.").define("beacon sanctuary", true);
        _remove = b.translation("softborder.config.remove").comment("border anchor applied with dimension change is not dispellable with milk, but modded methods are all fair game.").define("border anchor triggers when dispelled", true);
        _perc = b.translation("softborder.config.percent").comment("for every 1% of your border you exceed, incoming damage will be increased by this number, and outgoing damage and healing will be decreased by this number.").defineInRange("misery percent", 0.02, 0, 1);
        _max = b.translation("softborder.config.max").comment("damage increases and healing nerfs outside the border will be capped at this value.").defineInRange("misery max", 1d, 0, 1);
        _warpPerc = b.translation("softborder.config.warpPercent").comment("when changing dimensions, you will be afflicted with border anchor, which warps you back to the last known safe zone, if you land this percent away from any safe zone. Percentage starts from the edge. Set below -1 to disable this feature.").defineInRange("safety warp percentage", 0d, -2, 100);
        _anchorTime = b.translation("softborder.config.anchorTime").comment("also determines how fast the fog surrounds you when afflicted with border anchor.").defineInRange("anchor time after warp", 100, 1, Integer.MAX_VALUE);
        _effects = b.translation("softborder.config.effects").comment("effects to be applied when the player is a certain distance (defined by percent of their border size) away from their border. Format is name, length, potency, start percentage, (optional) end percentage. \nPercentage starts from the edge, so values between -1 and 0 will take effect within the border.\nThe exception to this is border anchor. Due to its unique behavior, border anchor will not be reapplied as long as it is active.").defineList("border effects", Arrays.asList(FX), String.class::isInstance);
    }

    private static void bake() {
        effects.clear();
        size = CONFIG._defaultSize.get();
        beacon = CONFIG._beacon.get();
        perc = CONFIG._perc.get();
        max = CONFIG._max.get();
        warperc = CONFIG._warpPerc.get();
        anchorTime = CONFIG._anchorTime.get();
        dispel = CONFIG._remove.get();
        for (String s : CONFIG._effects.get()) {
            effects.add(new BorderEffect(s));
        }
        effects.sort(Comparator.comparingDouble(a -> a.start));
    }

    @SubscribeEvent
    public static void loadConfig(ModConfig.ModConfigEvent e) {
        if (e.getConfig().getSpec() == CONFIG_SPEC) {
            SoftBorder.LOGGER.debug("loading border config!");
            bake();
        }
    }

    public static class BorderEffect {
        public final Effect e;
        public final int length, potency;
        public final float start, end;

        protected BorderEffect(String parse) {
            String[] split = parse.split(",");
            Effect te;
            int tlength, tpotency;
            float tstart, tend;
            try {
                te = ForgeRegistries.POTIONS.getValue(new ResourceLocation(split[0].trim()));
                tlength = Integer.parseInt(split[1].trim());
                tpotency = Integer.parseInt(split[2].trim());
                tstart = Float.parseFloat(split[3].trim());
                if (split.length > 4)
                    tend = Float.parseFloat(split[4].trim());
                else tend = Float.MAX_VALUE;
            } catch (Exception ignored) {
                SoftBorder.LOGGER.warn(parse + " is improperly formatted, replacing with default values.");
                te = Effects.UNLUCK;
                tlength = 10;
                tpotency = 0;
                tstart = 0;
                tend = Float.MAX_VALUE;
            }
            e = te;
            length = tlength;
            potency = tpotency;
            start = tstart;
            end = tend;
        }
    }
}
