package lumien.perfectspawn.Core;

import lumien.perfectspawn.PerfectSpawn;
import lumien.perfectspawn.Core.PerfectSpawnSettings.SettingEntry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.demo.DemoWorldManager;
import net.minecraftforge.common.DimensionManager;

public class CoreHandler
{
	static boolean testingRespawnDimension = false;

	public static int getRespawnDimension(WorldProvider provider, EntityPlayerMP player)
	{
		if (!testingRespawnDimension)
		{
			SettingEntry se = null;
			if (provider.worldObj.isRemote)
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

	public static ChunkCoordinates getRandomizedSpawnPoint(WorldProvider provider)
	{
		SettingEntry se = null;
		if (provider.worldObj.isRemote)
		{
			se = PerfectSpawnClientHandler.currentServerSettings;
		}
		else
		{
			se = PerfectSpawn.settings.getValidSettingEntry();
		}

		if (se != null && se.exactSpawn && se.spawnDimension == provider.dimensionId)
		{
			return provider.getSpawnPoint();
		}
		return null;
	}

	public static int canRespawnHere(WorldProvider provider)
	{
		SettingEntry se = null;
		if (provider.worldObj.isRemote)
		{
			se = PerfectSpawnClientHandler.currentServerSettings;
		}
		else
		{
			se = PerfectSpawn.settings.getValidSettingEntry();
		}

		if (se != null)
		{
			if (provider.dimensionId == 0)
			{
				return 0;
			}
			if (provider.dimensionId == se.spawnDimension)
			{
				return 1;
			}
		}

		return -1;
	}

	public static int isSurfaceWorld(WorldProvider provider) // Unused
	{
		return -126;
	}
}
