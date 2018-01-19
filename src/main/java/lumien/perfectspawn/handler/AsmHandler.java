package lumien.perfectspawn.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProvider;

public class AsmHandler
{
	// PlayerList
	public static int overrideInitialDimension(int original)
	{
		System.out.println("Overriding with -1");
		return -1;
	}
	
	// WorldProvider
	public static boolean overrideCanRespawn(WorldProvider provider, boolean original)
	{
		return provider.getDimension() == -1 ? true : original;
	}
	
	// WorldProvider
	public static BlockPos overrideGetSpawnPoint(WorldProvider provider, BlockPos original)
	{
		if (provider.getDimension() == -1)
		{
			return new BlockPos(-58, 111, 864);
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
		return -1;
	}
	
	// TODO: Add Option to unload spawn chunks (Controlled by canRespawnHere()), add sleep logic, add spawn protection logic
}
