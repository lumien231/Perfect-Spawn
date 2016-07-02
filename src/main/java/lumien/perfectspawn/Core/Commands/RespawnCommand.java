package lumien.perfectspawn.core.Commands;

import lumien.perfectspawn.PerfectSpawn;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class RespawnCommand extends CommandBase
{
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		if (PerfectSpawn.instance.respawnUser)
		{
			return true;
		}
		return super.checkPermission(server, sender);
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 4;
	}

	@Override
	public String getCommandName()
	{
		return "respawn";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "/respawn <player>";
	}

	@Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
		EntityPlayerMP player = null;
		if (sender instanceof EntityPlayerMP)
		{
			player = (EntityPlayerMP) sender;
		}
		if (args.length == 1 && sender.canCommandSenderUseCommand(4, "respawn"))
		{
			String playerName = args[0];
			player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(playerName);
		}

		if (player != null)
		{
			player.playerNetServerHandler.playerEntity = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().recreatePlayerEntity((EntityPlayerMP) player, player.dimension, false);
		}
	}

}
