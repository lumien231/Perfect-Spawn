package lumien.perfectspawn.Core;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.helpers.Loader;

import lumien.perfectspawn.PerfectSpawn;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;

public class PerfectSpawnCommand extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "ps";
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender cs, String[] strings)
    {
		if (strings.length==1)
		{
			return getListOfStringsMatchingLastWord(strings,"reload");
		}
        return null;
    }

	@Override
	public String getCommandUsage(ICommandSender var1)
	{
		return "/ps reload";
	}

	@Override
	public void processCommand(ICommandSender commandUser, String[] arguments)
	{
		if (arguments.length==0)
		{
			return;
		}
		else
		{
			String subCommand = arguments[0];
			if (subCommand.equals("reload"))
			{
				commandUser.addChatMessage(new ChatComponentText("Reloading PerfectSpawn settings"));
				PerfectSpawn.settings.reload();
			}
		}
	}
}
