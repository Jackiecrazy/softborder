package jackiecrazy.softborder.capability;

import jackiecrazy.softborder.config.BorderConfig;
import jackiecrazy.softborder.SoftBorder;
import jackiecrazy.softborder.SyncPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.concurrent.ConcurrentHashMap;

public class BorderTracker implements IBorderTracker {
    private final PlayerEntity linked;

    private final ConcurrentHashMap<ResourceLocation, Integer> borders = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<ResourceLocation, BlockPos> emitters = new ConcurrentHashMap<>();

    public BorderTracker() {
        linked = null;
    }

    public BorderTracker(PlayerEntity bound) {
        linked = bound;
    }

    @Override
    public int getBorderFor(World w) {
        borders.putIfAbsent(w.getDimensionKey().getLocation(), BorderConfig.size);
        return Math.max(borders.get(w.getDimensionKey().getLocation()), 1);
    }

    @Override
    public void setBorderFor(World w, int radius) {
        borders.put(w.getDimensionKey().getLocation(), Math.max(1, radius));
        if (linked != null)
            SoftBorder.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) linked), new SyncPacket(write()));
    }

    @Override
    public BlockPos getPrevEmitter(World w) {
        return emitters.getOrDefault(w.getDimensionKey().getLocation(), BlockPos.ZERO);
    }

    @Override
    public void setPrevEmitter(World w, BlockPos location) {
        emitters.put(w.getDimensionKey().getLocation(), location);
    }

    @Override
    public CompoundNBT write() {
        CompoundNBT ret = new CompoundNBT();
        borders.forEach((a, v) -> {
            CompoundNBT put = new CompoundNBT();
            put.putInt("radius", v);
            if (emitters.containsKey(a)) {
                BlockPos bp = emitters.get(a);
                put.putInt("lastX", bp.getX());
                put.putInt("lastY", bp.getY());
                put.putInt("lastZ", bp.getZ());
            }
            ret.put(a.toString(), put);
        });
        return ret;
    }

    @Override
    public void read(CompoundNBT from) {
        for (String s : from.keySet()) {
            CompoundNBT nbt = from.getCompound(s);
            final ResourceLocation dim = new ResourceLocation(s);
            borders.put(dim, nbt.getInt("radius"));
            emitters.put(dim, new BlockPos(nbt.getInt("lastX"), nbt.getInt("lastY"), nbt.getInt("lastZ")));
        }
    }
}
