package jackiecrazy.softborder.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class BorderStorage implements Capability.IStorage<IBorderTracker> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<IBorderTracker> capability, IBorderTracker IBorderTracker, Direction direction) {
        return IBorderTracker.write();
    }

    @Override
    public void readNBT(Capability<IBorderTracker> capability, IBorderTracker IBorderTracker, Direction direction, INBT inbt) {
        if(inbt instanceof CompoundNBT) {
            IBorderTracker.read((CompoundNBT) inbt);
        }
    }
}
