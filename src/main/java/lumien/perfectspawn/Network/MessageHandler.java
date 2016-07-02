package lumien.perfectspawn.network;

import lumien.perfectspawn.PerfectSpawn;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class MessageHandler
{
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(PerfectSpawn.MOD_ID.toLowerCase());

	public static void init()
    {
		INSTANCE.registerMessage(PerfectSpawnSettingsMessage.class, PerfectSpawnSettingsMessage.class, 0, Side.CLIENT);
    }
}
