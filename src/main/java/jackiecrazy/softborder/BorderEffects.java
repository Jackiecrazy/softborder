package jackiecrazy.softborder;

import com.google.common.collect.Lists;
import jackiecrazy.softborder.capability.BorderData;
import jackiecrazy.softborder.config.BorderConfig;
import net.minecraft.client.renderer.model.BlockPart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = SoftBorder.MODID)
public class BorderEffects {

    static ConcurrentHashMap<ServerPlayerEntity, Boolean> tempPos = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void effects(TickEvent.PlayerTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            double perc = BorderUtils.getOverreachPercentage(e.player);
            for (BorderConfig.BorderEffect be : BorderConfig.effects) {
                if (be.start > perc) return;
                if (e.player.isPotionActive(be.e) && (be.e == SoftBorder.BORDER_ANCHOR.get() || ((e.player.getActivePotionEffect(be.e).getAmplifier() >= be.potency || e.player.getActivePotionEffect(be.e).getDuration() >= be.length / 2))))
                    continue;
                EffectInstance ei = new EffectInstance(be.e, be.length, be.potency, true, false);
                e.player.addPotionEffect(ei);
            }
        }
    }

    @SubscribeEvent
    public static void weaken(LivingHurtEvent e) {
        if (e.getSource().getTrueSource() instanceof PlayerEntity) {
            PlayerEntity p = (PlayerEntity) e.getSource().getTrueSource();
            e.setAmount((float) (e.getAmount() * (1 - BorderUtils.getClampedOverreachPercentage(p))));
        }
        if (e.getEntityLiving() instanceof PlayerEntity) {
            e.setAmount((float) (e.getAmount() * (1 + BorderUtils.getClampedOverreachPercentage((PlayerEntity) e.getEntityLiving()))));
        }
    }

    @SubscribeEvent
    public static void noHeal(LivingHealEvent e) {
        if (e.getEntityLiving() instanceof PlayerEntity) {
            e.setAmount((float) (e.getAmount() * (1 - BorderUtils.getClampedOverreachPercentage((PlayerEntity) e.getEntityLiving()))));
        }
    }

    @SubscribeEvent
    public static void capability(final AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            event.addCapability(new ResourceLocation("softborder:tracker"), new BorderData((PlayerEntity) event.getObject()));
        }
    }

    @SubscribeEvent
    public static void sync(final PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getPlayer() instanceof ServerPlayerEntity)
            BorderData.getCap(e.getPlayer()).ifPresent((a) -> SoftBorder.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) e.getPlayer()), new SyncPacket(a.write())));
    }

    @SubscribeEvent
    public static void sync(final PlayerEvent.PlayerRespawnEvent e) {
        if (e.getPlayer() instanceof ServerPlayerEntity)
            BorderData.getCap(e.getPlayer()).ifPresent((a) -> SoftBorder.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) e.getPlayer()), new SyncPacket(a.write())));
    }

    @SubscribeEvent
    public static void preSync(final EntityTravelToDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayerEntity) {
            tempPos.put((ServerPlayerEntity) e.getEntity(), BorderUtils.getOverreachPercentage((ServerPlayerEntity) e.getEntity()) < 0);
        }
    }

    @SubscribeEvent
    public static void sync(final PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getPlayer() instanceof ServerPlayerEntity) {
            BorderData.getCap(e.getPlayer()).ifPresent((a) -> {
                SoftBorder.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) e.getPlayer()), new SyncPacket(a.write()));
                Boolean b = tempPos.get((ServerPlayerEntity) e.getPlayer());
                if (b != null && b) {
                    final double overreachPercentage = BorderUtils.getOverreachPercentage(e.getPlayer());
                    if (BorderConfig.warperc > -1 && overreachPercentage > BorderConfig.warperc) {
                        e.getPlayer().sendStatusMessage(new TranslationTextComponent("softborder.dimension"), false);
                        final EffectInstance effect = new EffectInstance(SoftBorder.BORDER_ANCHOR.get(), BorderConfig.anchorTime);
                        effect.setCurativeItems(new ArrayList<>());
                        e.getPlayer().addPotionEffect(effect);
                    }
                }

            });
        }
    }

    @SubscribeEvent
    public static void sync(final PlayerEvent.Clone e) {
        BorderData.getCap(e.getPlayer()).ifPresent((b) -> BorderData.getCap(e.getOriginal()).ifPresent((a) -> b.read(a.write())));
    }

    @SubscribeEvent
    public static void removeAnchor(final PotionEvent.PotionRemoveEvent e) {
        if (BorderConfig.dispel && e.getEntityLiving() instanceof ServerPlayerEntity && e.getPotion() == SoftBorder.BORDER_ANCHOR.get()) {
            BorderUtils.procBorderAnchor((PlayerEntity) e.getEntityLiving());
        }
    }
}
