package lumien.perfectspawn.config.internal;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

public class Rule
{
	Object[] appliesTo = new Object[0];

	Bool canRespawnHere = Bool.NOTSET;
	Bool canSleepHere = Bool.NOTSET;

	BlockPos spawnPoint = null;
	Integer respawnDimension = null;
	
	public void setAppliesTo(Object[] appliesTo)
	{
		this.appliesTo = appliesTo;
	}

	public void setCanRespawnHere(Bool canRespawnHere)
	{
		this.canRespawnHere = canRespawnHere;
	}

	public void setCanSleepHere(Bool canSleepHere)
	{
		this.canSleepHere = canSleepHere;
	}

	public void setSpawnPoint(BlockPos spawnPoint)
	{
		this.spawnPoint = spawnPoint;
	}

	public void setRespawnDimension(Integer respawnDimension)
	{
		this.respawnDimension = respawnDimension;
	}

	public boolean appliesTo(WorldProvider provider)
	{
		int dimensionID = provider.getDimension();
		String dimensionType = provider.getDimensionType().getName();
		String providerName = provider.getClass().getName();

		for (Object o : appliesTo)
		{
			if (o instanceof Integer)
			{
				if (((Integer) o) == dimensionID)
				{
					return true;
				}
			}
			else if (o instanceof String)
			{
				String s = (String) o;

				if (s.equals("*"))
				{
					return true;
				}

				if (s.equals(dimensionType) || s.equals(providerName))
				{
					return true;
				}
			}
		}

		return false;
	}

	public Bool getCanRespawnHere()
	{
		return canRespawnHere;
	}

	public Bool getForceBed()
	{
		return canSleepHere;
	}

	public BlockPos getSpawnPoint()
	{
		return spawnPoint;
	}

	public Integer getRespawnDimension()
	{
		return respawnDimension;
	}
}
