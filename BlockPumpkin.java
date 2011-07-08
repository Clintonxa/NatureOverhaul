// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package net.minecraft.src;

//========
// BEGIN AUTOFOREST
//========
import java.util.Random;
import modoptionsapi.ModOptionsAPI;
import modoptionsapi.ModBooleanOption;
import modoptionsapi.ModOptions;
import modoptionsapi.ModMappedMultiOption;
//========
// END AUTOFOREST
//========

public class BlockPumpkin extends BlockMortal
{

    protected BlockPumpkin(int i, int j, boolean flag)
    {
        super(i, Material.pumpkin);
        blockIndexInTexture = j;
        setTickOnLoad(true);
        blockType = flag;
    }
	
	//========
	// BEGIN AUTOFOREST
	//========
    protected String growthModifierType = "PumpkinSpawn";
	
    public void updateTick(World world, int i, int j, int k, Random random) {
    	if(!world.multiplayerWorld){
    		ModOptions pumpkins = mod_AutoForest.pumpkins;
			boolean grow = ((ModBooleanOption) pumpkins.getOption("PumpkinsGrow")).getValue();
			if(grow){
				double growthRate = 1D /(((ModMappedMultiOption) pumpkins
						.getOption("PumpkinGrowthRate")).getValue());
				attemptGrowth(world, i, j, k, growthRate);
			}
			
			// ATTEMPT DEATH
			boolean death = mod_AutoForest.pumpkinDeath.getValue();
			double deathProb = 1D / (1.5D * (((ModMappedMultiOption) pumpkins
						.getOption("PumpkinDeathRate")).getValue()));
			if(death && hasDied(world, i, j, k, deathProb)) {
				death(world, i, j, k);
			}
		}
	}
	//========
	// END AUTOFOREST
	//========
	
    public int getBlockTextureFromSideAndMetadata(int i, int j)
    {
        if(i == 1)
        {
            return blockIndexInTexture;
        }
        if(i == 0)
        {
            return blockIndexInTexture;
        }
        int k = blockIndexInTexture + 1 + 16;
        if(blockType)
        {
            k++;
        }
        if(j == 2 && i == 2)
        {
            return k;
        }
        if(j == 3 && i == 5)
        {
            return k;
        }
        if(j == 0 && i == 3)
        {
            return k;
        }
        if(j == 1 && i == 4)
        {
            return k;
        } else
        {
            return blockIndexInTexture + 16;
        }
    }

    public int getBlockTextureFromSide(int i)
    {
        if(i == 1)
        {
            return blockIndexInTexture;
        }
        if(i == 0)
        {
            return blockIndexInTexture;
        }
        if(i == 3)
        {
            return blockIndexInTexture + 1 + 16;
        } else
        {
            return blockIndexInTexture + 16;
        }
    }

    public void onBlockAdded(World world, int i, int j, int k)
    {
        super.onBlockAdded(world, i, j, k);
    }

    public boolean canPlaceBlockAt(World world, int i, int j, int k)
    {
        int l = world.getBlockId(i, j, k);
        return (l == 0 || Block.blocksList[l].blockMaterial.getIsGroundCover()) && world.func_28100_h(i, j - 1, k);
    }

    public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving)
    {
        int l = MathHelper.floor_double((double)((entityliving.rotationYaw * 4F) / 360F) + 2.5D) & 3;
        world.setBlockMetadataWithNotify(i, j, k, l);
    }

    private boolean blockType;
}
