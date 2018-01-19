package lumien.perfectspawn;

import lumien.perfectspawn.lib.Reference;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "[1.12,1.13)")
public class PerfectSpawn
{
	@Instance
	public static PerfectSpawn INSTANCE;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{

	}
}
