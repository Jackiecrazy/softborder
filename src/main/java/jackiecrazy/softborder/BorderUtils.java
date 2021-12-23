package jackiecrazy.softborder;

import jackiecrazy.softborder.capability.BorderData;
import jackiecrazy.softborder.config.BorderConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;

public class BorderUtils {
    static int getBorderFor(PlayerEntity p) {
        if (BorderData.getCap(p).isPresent())
            return BorderData.getCap(p).get().getBorderFor(p.world);
        return BorderConfig.size;
    }

    static double getClampedOverreachPercentage(PlayerEntity p) {
        return MathHelper.clamp(getOverreachPercentage(p) * 100 * BorderConfig.perc, 0, BorderConfig.max);
    }

    static double getOverreachPercentage(PlayerEntity p) {
        BlockPos closest = getClosestBorderEmitter(p);
        int size = getMaxBorderSize(p, closest);
        double over = Math.sqrt(p.getDistanceSq(closest.getX(), closest.getY(), closest.getZ())) - size;
        return over / size;
    }

    static BlockPos getClosestBorderEmitter(PlayerEntity p) {
        BlockPos at = BlockPos.ZERO.add(0, p.getPosY(), 0);
        double result = (at.distanceSq(p.getPosX(), p.getPosY(), p.getPosZ(), false)) / (getBorderFor(p) * getBorderFor(p));
        if (BorderConfig.beacon) {
            for (TileEntity t : p.world.loadedTileEntityList) {
                if (t instanceof BeaconTileEntity) {
                    BeaconTileEntity beacon = (BeaconTileEntity) t;
                    if (beacon.getLevels() == 0) continue;
                    int coverage = (beacon.getLevels() + 1) * 10;
                    double dist = p.getDistanceSq(Vector3d.copyCentered(beacon.getPos())) / (coverage * coverage);
                    if (dist < result) {
                        result = dist;
                        at = beacon.getPos();
                    }
                }
            }
        }
        if(result<1){
            BlockPos finalAt = at;
            BorderData.getCap(p).ifPresent((a)->a.setPrevEmitter(p.world, finalAt));
        }
        return at;
    }

    static int getMaxBorderSize(PlayerEntity p, BlockPos pos) {
        if (BorderConfig.beacon && p.world.getTileEntity(pos) instanceof BeaconTileEntity) {
            BeaconTileEntity beacon = (BeaconTileEntity) p.world.getTileEntity(pos);
            if (pos.getX() == 0 && pos.getZ() == 0)
                return Math.max(getBorderFor(p), (beacon.getLevels() + 1) * 10);
            return (beacon.getLevels() + 1) * 10;
        } else return getBorderFor(p);
    }

    static void procBorderAnchor(PlayerEntity player) {
        if (getOverreachPercentage(player) < 0) return;
        BorderData.getCap(player).ifPresent((cap) -> {
            BlockPos safeZoneCenter = cap.getPrevEmitter(player.world);
            Vector3d vec = player.getPositionVec().subtract(safeZoneCenter.getX(), safeZoneCenter.getY(), safeZoneCenter.getZ());
            double scale = getMaxBorderSize(player, safeZoneCenter);
            vec = vec.normalize().scale(scale).add(safeZoneCenter.getX(), safeZoneCenter.getY(), safeZoneCenter.getZ());
            int safeYUp = (int) vec.getY();
            int safeYDown = (int) vec.getY();
            while (safeYDown > 5) {
                final BlockPos pos = new BlockPos(vec.x, safeYDown, vec.z);
                if (player.world.getBlockState(pos).isAir() && player.world.getBlockState(pos.up()).isAir()) {
                    break;
                }
                safeYDown--;
            }
            while (safeYUp < 300) {
                final BlockPos pos = new BlockPos(vec.x, safeYUp, vec.z);
                if (player.world.getBlockState(pos).isAir() && player.world.getBlockState(pos.up()).isAir()) {
                    break;
                }
                safeYUp++;
            }
            vec = new Vector3d(vec.x, safeYUp - vec.y < safeYDown - vec.y ? safeYUp : safeYDown, vec.z);
            player.setPositionAndUpdate(vec.x, vec.y, vec.z);
            player.sendStatusMessage(new TranslationTextComponent("softborder.warped"), false);
            //textures/mob_effect/border_anchor.png
        });
    }
}
