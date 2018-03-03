package lumien.perfectspawn;

import java.util.Random;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class PSCommand extends CommandBase
{

	@Override
	public String getName()
	{
		return "ps";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/ps reload";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length == 1 && args[0].equals("reload"))
		{
			PerfectSpawn.INSTANCE.configHandler.reloadConfig();
		}
	}

}
