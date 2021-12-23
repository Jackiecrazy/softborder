package jackiecrazy.softborder.capability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class BorderData implements ICapabilitySerializable<CompoundNBT> {
    @CapabilityInject(IBorderTracker.class)
    public static Capability<IBorderTracker> CAP = null;
    private final LazyOptional<IBorderTracker> instance;

    public BorderData(PlayerEntity p) {
        instance = LazyOptional.of(() -> new BorderTracker(p));
    }

    public static Optional<IBorderTracker> getCap(PlayerEntity le) {
        return le.getCapability(CAP).resolve();//.orElseThrow(() -> new IllegalArgumentException("attempted to find a nonexistent capability"));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CAP.orEmpty(cap, instance);
    }

    @Override
    public CompoundNBT serializeNBT() {
        return (CompoundNBT) CAP.getStorage().writeNBT(
                CAP,
                instance.orElseThrow(() ->
                        new IllegalArgumentException("LazyOptional cannot be empty!")),
                null);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        CAP.getStorage().readNBT(
                CAP,
                instance.orElseThrow(() ->
                        new IllegalArgumentException("LazyOptional cannot be empty!")),
                null, nbt);
    }
}
