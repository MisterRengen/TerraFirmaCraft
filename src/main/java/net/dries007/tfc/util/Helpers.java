package net.dries007.tfc.util;

import net.dries007.tfc.objects.Type;
import net.dries007.tfc.objects.blocks.BlockPeat;
import net.dries007.tfc.objects.blocks.BlockRockVariant;
import net.dries007.tfc.objects.blocks.BlocksTFC;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public final class Helpers
{
    private Helpers() {}

    public static void spreadGrass(World world, BlockPos pos, IBlockState us, Random rand)
    {
        if (world.getLightFromNeighbors(pos.up()) < 4 && world.getBlockState(pos.up()).getLightOpacity(world, pos.up()) > 2)
        {
            if (us.getBlock() instanceof BlockPeat)
            {
                //noinspection ConstantConditions
                world.setBlockState(pos, BlocksTFC.PEAT.getDefaultState());
            }
            else if (us.getBlock() instanceof BlockRockVariant)
            {
                BlockRockVariant block = ((BlockRockVariant) us.getBlock());
                world.setBlockState(pos, block.getVariant(block.type.getNonGrassVersion()).getDefaultState());
            }
        }
        else
        {
            if (world.getLightFromNeighbors(pos.up()) < 9) return;

            for (int i = 0; i < 4; ++i)
            {
                BlockPos target = pos.add(rand.nextInt(3) - 1, rand.nextInt(5) - 3, rand.nextInt(3) - 1);
                if (world.isOutsideBuildHeight(target) || !world.isBlockLoaded(target)) return;
                BlockPos up = target.add(0, 1, 0);

                IBlockState current = world.getBlockState(target);
                if (!BlocksTFC.isSoil(current) || BlocksTFC.isGrass(current)) continue;
                if (world.getLightFromNeighbors(up) < 4 || world.getBlockState(up).getLightOpacity(world, up) > 3) continue;

                if (current.getBlock() instanceof BlockPeat)
                {
                    //noinspection ConstantConditions
                    world.setBlockState(target, BlocksTFC.PEAT_GRASS.getDefaultState());
                }
                else if (current.getBlock() instanceof BlockRockVariant)
                {
                    Type spreader = Type.GRASS;
                    if ((us.getBlock() instanceof BlockRockVariant) && ((BlockRockVariant) us.getBlock()).type == Type.DRY_GRASS)
                        spreader = Type.DRY_GRASS;

                    BlockRockVariant block = ((BlockRockVariant) current.getBlock());
                    world.setBlockState(target, block.getVariant(block.type.getGrassVersion(spreader)).getDefaultState());
                }
            }
        }
    }
}
