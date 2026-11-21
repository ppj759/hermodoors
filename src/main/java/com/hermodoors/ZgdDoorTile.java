package com.hermodoors;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class ZgdDoorTile extends TileEntity implements ITickableTileEntity {
    public static final float STEP = 1.0F / 30.0F; // animation takes 30 ticks (1.5 s)
    public float progress; // 0 = closed, 1 = open
    public float prev;
    private boolean init;

    public ZgdDoorTile() {
        super(HermoDoors.ZGD_TILE.get());
    }

    @Override
    public void tick() {
        if (this.world == null || !this.world.isRemote) return;
        float target = getBlockState().get(ZgdDoorBlock.OPEN) ? 1.0F : 0.0F;
        if (!init) {
            progress = prev = target;
            init = true;
            return;
        }
        prev = progress;
        if (progress < target) progress = Math.min(target, progress + STEP);
        else if (progress > target) progress = Math.max(target, progress - STEP);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.add(-1, 0, -1), pos.add(2, 2, 2));
    }
}
