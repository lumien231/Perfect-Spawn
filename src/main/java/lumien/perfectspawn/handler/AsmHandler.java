package lumien.perfectspawn.handler;

import lumien.perfectspawn.PerfectSpawn;
import lumien.perfectspawn.config.PSConfig;
import lumien.perfectspawn.config.internal.Bool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;

public class AsmHandler
{
	// PlayerList
	public static int overrideInitialDimension(int original)
	{
		PSConfig config = PerfectSpawn.INSTANCE.getConfigHandler().getActiveConfig();

		if (config != null)
		{
			return config.getInitialSpawnDimension();
		}

		return 0;
	}

	// WorldProvider
	public static boolean overrideCanRespawn(WorldProvider provider, boolean original)
	{
		PSConfig config = PerfectSpawn.INSTANCE.getConfigHandler().getActiveConfig();

		if (config != null)
		{
			Bool value = config.canRespawnHere(provider);

			if (value != Bool.NOTSET)
			{
				return value == Bool.TRUE ? true : false;
			}
		}

		return original;
	}
	
	// WorldProvider
	public static int overrideGetRespawnDimension(WorldProvider provider, int original)
	{
		PSConfig config = PerfectSpawn.INSTANCE.getConfigHandler().getActiveConfig();

		if (config != null)
		{
			Integer value = config.getRespawnDimension(provider);
			
			if (value != null)
			{
				return value;
			}
		}

		return original;
	}

	// WorldProvider
	public static BlockPos overrideGetSpawnPoint(WorldProvider provider, BlockPos original)
	{
		PSConfig config = PerfectSpawn.INSTANCE.getConfigHandler().getActiveConfig();

		if (config != null)
		{
			BlockPos value = config.getSpawnPoint(provider);
			if (value != null)
			{
				return value;
			}
		}

		return original;
	}

	// WorldProvider
	public static BlockPos overrideGetRandomizedSpawnPoint(WorldProvider provider, BlockPos original)
	{
		if (true) // Exact Spawn
		{
			return provider.getSpawnPoint();
		}

		return original;
	}

	// EntityPlayer
	public static int overrideDefaultPlayerSpawnDimension(EntityPlayer player, int original)
	{
		PSConfig config = PerfectSpawn.INSTANCE.getConfigHandler().getActiveConfig();

		if (config != null)
		{
			return config.getInitialSpawnDimension();
		}

		return original;
	}

	// BlockBed null -> Ignore Hell
	public static Biome changeHellComparison(World world)
	{
		PSConfig config = PerfectSpawn.INSTANCE.getConfigHandler().getActiveConfig();

		if (config != null)
		{
			Bool value = config.canSleepHere(world.provider);
			
			if (value == Bool.TRUE)
			{
				return null;
			}
		}

		return Biomes.HELL;
	}
	
	// false -> no
	public static boolean unloadWorld(int dimension)
	{
		PSConfig config = PerfectSpawn.INSTANCE.getConfigHandler().getActiveConfig();

		if (config != null)
		{
			return config.canUnload(dimension);
		}
		
		return true;
	}

	public static boolean keepInBedFix(EntityPlayer player, boolean isDayTime)
	{
		PSConfig config = PerfectSpawn.INSTANCE.getConfigHandler().getActiveConfig();

		if (config != null)
		{
			Bool value = config.canSleepHere(player.world.provider);
			
			if (value == Bool.TRUE)
			{
				return isDayTime && player.world.provider.isSurfaceWorld();
			}
		}

		return isDayTime;
	}

	// TODO: Add Option to unload spawn chunks (Controlled by canRespawnHere()),
	// add sleep logic, add spawn protection logic
}
