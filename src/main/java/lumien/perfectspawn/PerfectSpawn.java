package lumien.perfectspawn;

import static lumien.perfectspawn.PerfectSpawn.MOD_ID;
import static lumien.perfectspawn.PerfectSpawn.MOD_NAME;
import static lumien.perfectspawn.PerfectSpawn.MOD_VERSION;

import java.util.Map;

import org.apache.logging.log4j.Logger;

import lumien.perfectspawn.core.PSEventHandler;
import lumien.perfectspawn.core.PerfectSpawnSettings;
import lumien.perfectspawn.core.Commands.PerfectSpawnCommand;
import lumien.perfectspawn.core.Commands.RespawnCommand;
import lumien.perfectspawn.network.MessageHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = MOD_ID, name = MOD_NAME, version = MOD_VERSION)
public class PerfectSpawn
{
	public static final String MOD_ID = "PerfectSpawn";
	public static final String MOD_NAME = "PerfectSpawn";
	public static final String MOD_VERSION = "@VERSION@";

	public Logger logger;
	public boolean respawnUser;
	
	@Instance(MOD_ID)
	public static PerfectSpawn instance;
	
	public static PerfectSpawnSettings settings;
	
	@NetworkCheckHandler
	public boolean checkRemote(Map<String,String> mods,Side remoteSide)
	{
		return true;
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
		
		Configuration c = new Configuration(event.getSuggestedConfigurationFile());
		c.load();
		
		respawnUser = c.getBoolean("RespawnCommandUser", "Settings", false, "Whether a normal user can use the /respawn command on himself");
		
		if (c.hasChanged())
		{
			c.save();
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		settings = new PerfectSpawnSettings();
		settings.init();
		
		PSEventHandler psh = new PSEventHandler();
		MinecraftForge.EVENT_BUS.register(psh);
		FMLCommonHandler.instance().bus().register(psh);
		
		MessageHandler.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{

	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new PerfectSpawnCommand());
		event.registerServerCommand(new RespawnCommand());
	}
	
	@EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{
		settings.serverStarted();
	}
}
