package lumien.perfectspawn.Network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import lumien.perfectspawn.PerfectSpawn;

public class MessageHandler
{
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(PerfectSpawn.MOD_ID.toLowerCase());

	public static void init()
    {
		INSTANCE.registerMessage(PerfectSpawnSettingsMessage.class, PerfectSpawnSettingsMessage.class, 0, Side.CLIENT);
    }
}
