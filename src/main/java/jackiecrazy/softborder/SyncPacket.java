package jackiecrazy.softborder;

import jackiecrazy.softborder.capability.BorderData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SyncPacket {
    CompoundNBT icc;

    public SyncPacket(CompoundNBT c) {
        icc = c;
    }

    public static class SpawnSyncEncoder implements BiConsumer<SyncPacket, PacketBuffer> {

        @Override
        public void accept(SyncPacket SyncPacket, PacketBuffer packetBuffer) {
            packetBuffer.writeCompoundTag(SyncPacket.icc);
        }
    }

    public static class SpawnSyncDecoder implements Function<PacketBuffer, SyncPacket> {

        @Override
        public SyncPacket apply(PacketBuffer packetBuffer) {
            return new SyncPacket(packetBuffer.readCompoundTag());
        }
    }

    public static class SpawnSyncHandler implements BiConsumer<SyncPacket, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(SyncPacket SyncPacket, Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Handle.handleClient(SyncPacket.icc)));
            contextSupplier.get().setPacketHandled(true);
        }
    }

    public static class Handle {
        //SIDES!!!
        public static DistExecutor.SafeRunnable handleClient(CompoundNBT icc) {
            return new DistExecutor.SafeRunnable() {
                @Override
                public void run() {
                    PlayerEntity player = (PlayerEntity) Minecraft.getInstance().player;
                    if (player == null) return;
                    BorderData.getCap(Minecraft.getInstance().player).ifPresent((a)->a.read(icc));
                }
            };
        }
    }
}
