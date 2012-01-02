// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package net.minecraft.src;

import java.util.Random;
//====================
// BEGIN NATURE OVERHAUL
//====================
import moapi.ModOptionsAPI;
import moapi.ModOptions;
import moapi.ModMappedMultiOption;
import moapi.ModBooleanOption;

public class BlockSapling extends BlockFlower
{

    protected BlockSapling(int i, int j)
    {
        super(i, j);
        float f = 0.4F;
        setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
    }

    public void updateTick(World world, int i, int j, int k, Random random)
    {
        if(world.multiplayerWorld)
        {
            return;
        }
		ModOptions options = ModOptionsAPI.getModOptions(mod_AutoForest.MENU_NAME).getSubOption(mod_AutoForest.TREE_MENU_NAME);
		ModMappedMultiOption mo = (ModMappedMultiOption) options.getOption("TreeGrowthRate");
		
		int bound = mo.getValue();
        super.updateTick(world, i, j, k, random);
		// bound is *3 because metadata is now 3 times smaller
		// due to type addition
        if((world.getBlockLightValue(i, j + 1, k) >= 9) && 
			((bound == 0) || (random.nextInt(bound * 6) == 0))) { 
			int l = world.getBlockMetadata(i, j, k);
			// Added bound > 0 to ensure INSTANT is instant
			// Add 4 each time to avoid breaking the sapling
			// specific growth
			//System.out.println(l);
			if((((l & 8) == 0)) && (bound > 0)) {
				world.setBlockMetadataWithNotify(i, j, k, l | 8);
			} else {
				growTree(world, i, j, k, random, false);
			}
		}
    }

    public int getBlockTextureFromSideAndMetadata(int i, int j)
    {
        j &= 3;
        if(j == 1)
        {
            return 63;
        }
        if(j == 2)
        {
            return 79;
        } else
        {
            return super.getBlockTextureFromSideAndMetadata(i, j);
        }
    }
	
	/**
	* Grow a tree ignoring death
	*/
	public void growTree(World world, int i, int j, int k, Random random) {
		growTree(world, i, j, k, random, true);
	}
	
	/**
	* Attempt to Grow a tree
	*
	* @param 	ignoreDeath		True if we should force grow the sapling
	*/
    private void growTree(World world, int i, int j, int k, Random random, boolean ignoreDeath) {
		// Check options
		ModOptions mo = ModOptionsAPI.getModOptions(mod_AutoForest.MENU_NAME);
		ModOptions saps = mo.getSubOption(mod_AutoForest.SAPLING_MENU_NAME);
		ModBooleanOption sapDeathOp = (ModBooleanOption) saps.getOption("SaplingDeath");
		
		// Rate of big tree growth:
		int bigTreeRate = mod_AutoForest.getBiomeModifier(mod_AutoForest.getBiomeName(i,k), 
							"BigTree");
		
		// Choose a generator
		Object obj = null;
		
		//%3 as the meta data is on a %3 basis, where the 0th, 1st and 2nd index 
		// are for type, the rest is for timing tree growth
		int type = world.getBlockMetadata(i,j,k) & 3;
		if(type == 1) {
			if(random.nextInt(3) == 0) {
				obj = new WorldGenTaiga1();
			} else {
				obj = new WorldGenTaiga2(true);
			}
		} else if(type == 2) {
			 obj = new WorldGenForest(true);
		} else if(random.nextInt(100) < bigTreeRate) {
			obj = new WorldGenBigTree(true);
		} else {
			if(mod_AutoForest.getBiomeAt(i, k) == Biome.SWAMPLAND) {
				obj = new WorldGenSwamp();
			} else {
				obj = new WorldGenTrees(true);
			}
		}
		
		world.setBlockWithNotify(i, j, k, 0); 
		
		// Ignore death of saplings
		if(ignoreDeath) {
			if(!((WorldGenerator) (obj)).generate(world, random, i, j, k)) {
				world.setBlockAndMetadata(i, j, k, blockID, type);
			}
		// Sapling has a random chance of dying instead of growing
		} else {
			boolean starved = starvedSapling(world, i, j, k);
			boolean died    = randomDeath(world, i, j, k, random);
			boolean canDie  = sapDeathOp.getValue();
			boolean grew 	 = false;
			
			if(!starved && (!died || !canDie) && 
				(grew = !((WorldGenerator) (obj)).generate(world, random, i, j, k))) {
				world.setBlockAndMetadata(i, j, k, blockID, type);
			}
		}
    }

    protected int damageDropped(int i)
    {
        return i & 3;
    }

	
	/**
	* Check if a sapling is starved by it's neighbours
	*
	* @return	true is starved
	*/
	private boolean starvedSapling(World world, int i, int j, int k) {
		// Check options
		ModOptions mo = ModOptionsAPI.getModOptions(mod_AutoForest.MENU_NAME);
		ModOptions saps = mo.getSubOption(mod_AutoForest.SAPLING_MENU_NAME);
		boolean sapDeathOp = ((ModBooleanOption) saps.getOption("SaplingDeath")).getValue();
		
		if(sapDeathOp) {
			// Min dist between trees
			int dist = mod_AutoForest.getBiomeModifier(mod_AutoForest.getBiomeName(i,k),
							"TreeGap");
			
			for(int x = i - dist; x <= dist + i; x++) {
				for(int z = k - dist; z <= dist + k; z++) {
					if(world.getBlockId(x, j, z) == Block.wood.blockID) {
						//System.out.println("STARVED SAPLING IN biome: " + mod_AutoForest.getBiomeName(i,k) + ". DIST: " + dist);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	* Check if a sapling has died due to natural causes
	*
	* @return	True if a sapling has died
	*/
	private boolean randomDeath(World world, int i, int j, int k, Random random) {
		// Check options
		ModOptions mo = ModOptionsAPI.getModOptions(mod_AutoForest.MENU_NAME);
		ModOptions saps = mo.getSubOption(mod_AutoForest.SAPLING_MENU_NAME);
		boolean sapDeathOp = ((ModBooleanOption) saps.getOption("SaplingDeath")).getValue();
		String biomeName = mod_AutoForest.getBiomeName(i,k);
		int deathRate = mod_AutoForest.getBiomeModifier(biomeName, "SaplingDeath");
		
		// This means that a sapling with a death rate of X will die x% of the time
		if((sapDeathOp) && (random.nextInt(100) >= 100 - deathRate)) {
			//System.out.println("SAPLING RANDOM DEATH IN biome: "+ biomeName + " AT RATE " + deathRate);
			return true;
		}
		
		return false;
	}
}	
//====================
// END NATURE OVERHAUL
//====================