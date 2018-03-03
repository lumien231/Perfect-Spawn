package lumien.perfectspawn;

import org.apache.logging.log4j.Logger;

import lumien.perfectspawn.config.PSConfigHandler;
import lumien.perfectspawn.handler.PSEventHandler;
import lumien.perfectspawn.lib.Reference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = "[1.12,1.13)")
public class PerfectSpawn
{
	@Instance
	public static PerfectSpawn INSTANCE;
	
	public Logger logger;

	PSEventHandler eventHandler;
	
	PSConfigHandler configHandler;
	
	public PerfectSpawn()
	{
		configHandler = new PSConfigHandler();
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(eventHandler = new PSEventHandler());
		
		logger = event.getModLog();
	}
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		configHandler.serverStarting(event);
		
		event.registerServerCommand(new PSCommand());
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{

	}
	
	public static void warn(String string)
	{
		INSTANCE.logger.warn(string);
	}
	
	public static void debug(String string)
	{
		INSTANCE.logger.debug(string);
	}

	public PSConfigHandler getConfigHandler()
	{
		return configHandler;
	}
}
