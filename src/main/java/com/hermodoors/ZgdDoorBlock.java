package com.hermodoors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class ZgdDoorBlock extends Block {
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public ZgdDoorBlock(Properties props) {
        super(props);
        // change OPEN to true here if you want the door to be placed open
        this.setDefaultState(this.stateContainer.getBaseState()
                .with(FACING, Direction.NORTH).with(OPEN, false).with(LOCKED, false).with(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> b) {
        b.add(FACING, OPEN, LOCKED, HALF);
    }

    // ---------- shapes (model has the leaf on the north edge, hinge on the east side) ----------
    private static double[] tr(Direction d, double x, double z) {
        switch (d) {
            case EAST:  return new double[]{-z, x};
            case SOUTH: return new double[]{-x, -z};
            case WEST:  return new double[]{z, -x};
            default:    return new double[]{x, z};
        }
    }

    private static VoxelShape rot(Direction d, double x1, double z1, double x2, double z2) {
        double[] a = tr(d, x1 - 8, z1 - 8);
        double[] b = tr(d, x2 - 8, z2 - 8);
        return Block.makeCuboidShape(Math.min(a[0], b[0]) + 8, 0, Math.min(a[1], b[1]) + 8,
                Math.max(a[0], b[0]) + 8, 16, Math.max(a[1], b[1]) + 8);
    }

    @Override
    public VoxelShape getShape(BlockState s, IBlockReader w, BlockPos p, ISelectionContext c) {
        Direction d = s.get(FACING);
        return s.get(OPEN) ? rot(d, 13, 0, 16, 16) : rot(d, 0, 0, 16, 3);
    }

    // ---------- rendering is done by the tile entity renderer ----------
    @Override
    public BlockRenderType getRenderType(BlockState s) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean hasTileEntity(BlockState s) {
        return s.get(HALF) == DoubleBlockHalf.LOWER;
    }

    @Override
    public TileEntity createTileEntity(BlockState s, IBlockReader w) {
        return new ZgdDoorTile();
    }

    // ---------- placing / breaking (2 blocks tall) ----------
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext c) {
        BlockPos pos = c.getPos();
        if (pos.getY() < 255 && c.getWorld().getBlockState(pos.up()).isReplaceable(c)) {
            return this.getDefaultState().with(FACING, c.getPlacementHorizontalFacing())
                    .with(OPEN, false).with(LOCKED, false).with(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Override
    public void onBlockPlacedBy(World w, BlockPos pos, BlockState s, LivingEntity e, ItemStack i) {
        w.setBlockState(pos.up(), s.with(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public BlockState updatePostPlacement(BlockState s, Direction f, BlockState fs, IWorld w, BlockPos p, BlockPos fp) {
        DoubleBlockHalf half = s.get(HALF);
        if (f.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (f == Direction.UP)) {
            return fs.isIn(this) && fs.get(HALF) != half ? s : Blocks.AIR.getDefaultState();
        }
        return s;
    }

    @Override
    public void onBlockHarvested(World w, BlockPos pos, BlockState s, PlayerEntity player) {
        if (!w.isRemote && s.get(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos low = pos.down();
            BlockState ls = w.getBlockState(low);
            if (ls.isIn(this)) {
                if (!player.isCreative()) {
                    Block.spawnDrops(ls, w, low, null, player, player.getHeldItemMainhand());
                }
                w.setBlockState(low, Blocks.AIR.getDefaultState(), 35);
            }
        }
        super.onBlockHarvested(w, pos, s, player);
    }

    // RMB = open / close (blocked while locked). Shift+RMB = lock / unlock (lock only when closed).
    @Override
    public ActionResultType onBlockActivated(BlockState s, World w, BlockPos pos, PlayerEntity pl, Hand h, BlockRayTraceResult r) {
        BlockPos low = s.get(HALF) == DoubleBlockHalf.LOWER ? pos : pos.down();
        BlockState ls = w.getBlockState(low);
        if (!ls.isIn(this)) return ActionResultType.PASS;
        BlockState us = w.getBlockState(low.up());

        if (pl.isSecondaryUseActive()) {
            if (ls.get(OPEN)) return ActionResultType.PASS; // cannot lock an open door
            boolean lock = !ls.get(LOCKED);
            if (!w.isRemote) {
                w.setBlockState(low, ls.with(LOCKED, lock), 10);
                if (us.isIn(this)) w.setBlockState(low.up(), us.with(LOCKED, lock), 10);
                w.playSound(null, low, lock ? SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE : SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN,
                        SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
            return ActionResultType.SUCCESS;
        }

        if (ls.get(LOCKED)) return ActionResultType.SUCCESS; // locked: nothing happens
        boolean open = !ls.get(OPEN);
        if (!w.isRemote) {
            w.setBlockState(low, ls.with(OPEN, open), 10);
            if (us.isIn(this)) w.setBlockState(low.up(), us.with(OPEN, open), 10);
            w.playSound(null, low, open ? SoundEvents.BLOCK_IRON_DOOR_OPEN : SoundEvents.BLOCK_IRON_DOOR_CLOSE,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        return ActionResultType.SUCCESS;
    }
}
