package jackiecrazy.softborder;

import jackiecrazy.softborder.capability.BorderData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class DisintegrationEffect extends Effect {
    protected DisintegrationEffect() {
        super(EffectType.HARMFUL, 0xC61C39);
    }

    @Override
    public void performEffect(LivingEntity entityLivingBaseIn, int amplifier) {
        if (entityLivingBaseIn instanceof PlayerEntity && ((PlayerEntity) entityLivingBaseIn).abilities.isCreativeMode)
            return;
        if (!entityLivingBaseIn.attackEntityFrom(DisintegrationDamage.DISINTEGRATE, amplifier))
            entityLivingBaseIn.attackEntityFrom(DamageSource.OUT_OF_WORLD, amplifier);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    public static class DisintegrationDamage extends DamageSource {
        public static final DamageSource DISINTEGRATE = (new DamageSource("disintegration")).setDamageBypassesArmor();

        public DisintegrationDamage() {
            super("disintegration");
        }
    }
}
