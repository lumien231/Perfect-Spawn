package lumien.perfectspawn.core;

import lumien.perfectspawn.PerfectSpawn;
import lumien.perfectspawn.core.PerfectSpawnSettings.SettingEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class CoreHandler
{
	static boolean testingRespawnDimension = false;

	public static int getRespawnDimension(WorldProvider provider, EntityPlayerMP player)
	{
		if (!testingRespawnDimension)
		{
			SettingEntry se = null;
			if (player.worldObj.isRemote)
			{
				se = PerfectSpawnClientHandler.currentServerSettings;
			}
			else
			{
				se = PerfectSpawn.settings.getValidSettingEntry();
			}

			if (se != null)
			{
				testingRespawnDimension = true;
				int normalRespawn = provider.getRespawnDimension(player);
				testingRespawnDimension = false;
				if (normalRespawn == 0)
				{
					return se.spawnDimension;
				}
			}
		}
		return -126;
	}

	public static boolean canWakeUp(EntityPlayer player)
	{
		SettingEntry se = null;
		if (player.worldObj.isRemote)
		{
			se = PerfectSpawnClientHandler.currentServerSettings;
		}
		else
		{
			se = PerfectSpawn.settings.getValidSettingEntry();
		}

		if (se != null)
		{
			if (se.forceBed && player.worldObj.provider.getDimension() == se.spawnDimension && !player.worldObj.provider.isSurfaceWorld())
			{
				return false;
			}
		}
		return true;
	}

	public static boolean isBlockNotProtectedByDimension(int dimension)
	{
		return !isBlockProtectedByDimension(dimension);
	}

	public static boolean isBlockProtectedByDimension(int dimension)
	{
		World w = DimensionManager.getWorld(dimension);
		SettingEntry se = null;
		if (w.isRemote)
		{
			se = PerfectSpawnClientHandler.currentServerSettings;
		}
		else
		{
			se = PerfectSpawn.settings.getValidSettingEntry();
		}
		
		if (se == null || !se.spawnProtection)
		{
			return dimension == 0;
		}
		else
		{
			return se.spawnDimension == dimension;
		}
	}

	public static BlockPos getRandomizedSpawnPoint(WorldProvider provider)
	{
		boolean isRemote = FMLCommonHandler.instance().getEffectiveSide().isClient();
		SettingEntry se = null;
		if (isRemote)
		{
			se = PerfectSpawnClientHandler.currentServerSettings;
		}
		else
		{
			se = PerfectSpawn.settings.getValidSettingEntry();
		}

		if (se != null && se.exactSpawn && se.spawnDimension == provider.getDimension())
		{
			return provider.getSpawnPoint();
		}
		return null;
	}

	public static int canRespawnHere(WorldProvider provider)
	{
		boolean isRemote = FMLCommonHandler.instance().getEffectiveSide().isClient();
		SettingEntry se = null;
		if (isRemote)
		{
			se = PerfectSpawnClientHandler.currentServerSettings;
		}
		else
		{
			se = PerfectSpawn.settings.getValidSettingEntry();
		}

		if (se != null)
		{
			if (provider.getDimension() == se.spawnDimension)
			{
				return 1;
			}
			
			if (provider.getDimension() == 0)
			{
				return 0;
			}
		}

		return -1;
	}

	public static int isSurfaceWorld(WorldProvider provider) // Unused
	{
		return -126;
	}
}
