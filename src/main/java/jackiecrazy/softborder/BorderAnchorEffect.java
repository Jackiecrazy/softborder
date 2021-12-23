package jackiecrazy.softborder;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class BorderAnchorEffect extends Effect {
    protected BorderAnchorEffect() {
        super(EffectType.BENEFICIAL, 0x358ba1);
    }

    @Override
    public void performEffect(LivingEntity entityLivingBaseIn, int amplifier) {
        if (entityLivingBaseIn instanceof ServerPlayerEntity) {
            BorderUtils.procBorderAnchor((ServerPlayerEntity) entityLivingBaseIn);
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration == 1;
    }
}
