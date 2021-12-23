package jackiecrazy.softborder.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBorderTracker {
    int getBorderFor(World w);
    void setBorderFor(World w, int radius);
    BlockPos getPrevEmitter(World w);
    void setPrevEmitter(World w, BlockPos location);
    CompoundNBT write();
    void read(CompoundNBT from);
}
