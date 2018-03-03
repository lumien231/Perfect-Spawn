package lumien.perfectspawn.config;

import static lumien.perfectspawn.config.internal.Bool.*;

import org.objectweb.asm.tree.IincInsnNode;

import lumien.perfectspawn.config.internal.Bool;
import lumien.perfectspawn.config.internal.Rule;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

public class PSConfig
{
	Rule[] rules = new Rule[0];

	// General Values
	int initialSpawnDimension = 0;

	public void setRules(Rule[] rules)
	{
		this.rules = rules;
	}

	public void setInitialSpawnDimension(int initialSpawnDimension)
	{
		this.initialSpawnDimension = initialSpawnDimension;
	}

	public Bool canRespawnHere(WorldProvider provider)
	{
		Bool result = NOTSET;
		for (Rule r : rules)
		{
			if (r.appliesTo(provider))
			{
				Bool ruleValue = r.getCanRespawnHere();

				if (ruleValue != NOTSET)
				{
					result = ruleValue;
				}
			}
		}

		return result;
	}

	public Bool canSleepHere(WorldProvider provider)
	{
		Bool result = NOTSET;
		for (Rule r : rules)
		{
			if (r.appliesTo(provider))
			{
				Bool ruleValue = r.getForceBed();

				if (ruleValue != NOTSET)
				{
					result = ruleValue;
				}
			}
		}

		return result;
	}

	public Integer getRespawnDimension(WorldProvider provider)
	{
		Integer result = null;
		for (Rule r : rules)
		{
			if (r.appliesTo(provider))
			{
				Integer ruleValue = r.getRespawnDimension();

				if (ruleValue != null)
				{
					result = ruleValue;
				}
			}
		}

		return result;
	}
	
	public BlockPos getSpawnPoint(WorldProvider provider)
	{
		BlockPos result = null;
		for (Rule r : rules)
		{
			if (r.appliesTo(provider))
			{
				BlockPos ruleValue = r.getSpawnPoint();

				if (ruleValue != null)
				{
					result = ruleValue;
				}
			}
		}

		return result;
	}

	public int getInitialSpawnDimension()
	{
		return initialSpawnDimension;
	}

	public boolean canUnload(int dimension)
	{
		if (dimension == initialSpawnDimension)
		{
			return false;
		}
		
		for (Rule r : rules)
		{
			Integer respawn = r.getRespawnDimension();
			
			if (respawn != null && respawn == dimension)
			{
				return false;
			}
		}
		
		return true;
	}
}
