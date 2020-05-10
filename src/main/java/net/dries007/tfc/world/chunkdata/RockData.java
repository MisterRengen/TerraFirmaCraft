/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.world.chunkdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import net.dries007.tfc.api.Rock;
import net.dries007.tfc.objects.blocks.soil.SandBlockType;
import net.dries007.tfc.objects.blocks.soil.SoilBlockType;
import net.dries007.tfc.objects.types.RockManager;
import net.dries007.tfc.util.Helpers;

public class RockData implements INBTSerializable<CompoundNBT>
{
    private final Rock[] bottomLayer;
    private final Rock[] topLayer;
    private final SoilBlockType.Variant[] soilLayer;
    private final SandBlockType[] sandLayer;
    private final int[] rockLayerHeight;

    public RockData()
    {
        this(new Rock[256], new Rock[256], new SoilBlockType.Variant[256], new SandBlockType[256], new int[256]);
    }

    public RockData(Rock[] bottomLayer, Rock[] topLayer, SoilBlockType.Variant[] soilLayer, SandBlockType[] sandLayer, int[] rockLayerHeight)
    {
        this.bottomLayer = bottomLayer;
        this.topLayer = topLayer;
        this.soilLayer = soilLayer;
        this.sandLayer = sandLayer;
        this.rockLayerHeight = rockLayerHeight;
    }

    public Rock getRock(int x, int y, int z)
    {
        if (y > getRockHeight(x, z))
        {
            return getTopRock(x, z);
        }
        else
        {
            return getBottomRock(x, z);
        }
    }

    public int getRockHeight(int x, int z)
    {
        return rockLayerHeight[(x & 15) + 16 * (z & 15)];
    }

    public Rock getTopRock(int x, int z)
    {
        return topLayer[(x & 15) + 16 * (z & 15)];
    }

    public Rock getBottomRock(int x, int z)
    {
        return bottomLayer[(x & 15) + 16 * (z & 15)];
    }

    public SoilBlockType.Variant getSoil(int x, int z)
    {
        return soilLayer[(x & 15) + 16 * (z & 15)];
    }

    public SandBlockType getSand(int x, int z)
    {
        return sandLayer[(x & 15) + 16 * (z & 15)];
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        CompoundNBT nbt = new CompoundNBT();

        // Record a map from bytes -> rocks (pallet, similar to vanilla world save format)
        List<Rock> uniqueRocks = Stream.of(bottomLayer, topLayer).flatMap(Arrays::stream).distinct().collect(Collectors.toList());
        ListNBT pallet = new ListNBT();
        byte index = 0;
        for (Rock rock : uniqueRocks)
        {
            pallet.add(index, StringNBT.valueOf(rock.getId().toString()));
        }
        nbt.put("pallet", pallet);

        // Record the raw byte values
        nbt.putByteArray("bottomLayer", Helpers.createByteArray(topLayer, r -> (byte) uniqueRocks.indexOf(r)));
        nbt.putByteArray("topLayer", Helpers.createByteArray(bottomLayer, r -> (byte) uniqueRocks.indexOf(r)));

        // Record byte values for soil and sand
        nbt.putByteArray("soilLayer", Helpers.createByteArray(soilLayer, e -> (byte) e.ordinal()));
        nbt.putByteArray("sandLayer", Helpers.createByteArray(sandLayer, e -> (byte) e.ordinal()));

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        if (nbt != null)
        {
            // Build pallet
            ListNBT pallet = nbt.getList("pallet", Constants.NBT.TAG_STRING);
            List<Rock> uniqueRocks = new ArrayList<>(pallet.size());
            for (int i = 0; i < pallet.size(); i++)
            {
                uniqueRocks.add(RockManager.INSTANCE.get(new ResourceLocation(pallet.getString(i))));
            }

            Helpers.createArrayFromBytes(nbt.getByteArray("bottomLayer"), bottomLayer, uniqueRocks::get);
            Helpers.createArrayFromBytes(nbt.getByteArray("topLayer"), topLayer, uniqueRocks::get);

            Helpers.createArrayFromBytes(nbt.getByteArray("soilLayer"), soilLayer, SoilBlockType.Variant::valueOf);
            Helpers.createArrayFromBytes(nbt.getByteArray("sandLayer"), sandLayer, SandBlockType::valueOf);
        }
    }

}