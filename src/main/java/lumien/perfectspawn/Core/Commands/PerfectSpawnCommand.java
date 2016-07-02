package lumien.perfectspawn.core.Commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.gson.stream.JsonWriter;

import lumien.perfectspawn.PerfectSpawn;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class PerfectSpawnCommand extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "ps";
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
		if (args.length == 1)
		{
			return getListOfStringsMatchingLastWord(args, "reload","set");
		}
		else if (args.length == 2 && args[0].equals("set"))
		{
			return getListOfStringsMatchingLastWord(args, "main","world");
		}
		return null;
	}

	@Override
	public String getCommandUsage(ICommandSender var1)
	{
		return "/ps reload|set";
	}

	@Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
		if (args.length == 0)
		{
			return;
		}
		else
		{
			String subCommand = args[0];
			if (subCommand.equals("reload"))
			{
				sender.addChatMessage(new TextComponentString("Reloading PerfectSpawn settings"));
				PerfectSpawn.settings.reload();
			}
			else if (subCommand.equals("set"))
			{
				WrongUsageException wu = new WrongUsageException("/ps set [world|main] <x> <y> <z> <dimension> <exactSpawn> <forceBed> <spawnProtection>");
				if (args.length == 1)
				{
					throw wu;
				}

				String mode = args[1];
				if (!(mode.equals("world") || mode.equals("main")))
				{
					throw wu;
				}

				int spawnX = 0, spawnY = 0, spawnZ = 0, spawnDimension = 0;

				boolean exactSpawn = true;
				boolean forceBed = true;
				boolean spawnProtection = true;

				if (sender instanceof EntityPlayer)
				{
					EntityPlayer player = (EntityPlayer) sender;
					spawnX = (int) Math.floor(player.posX);
					spawnY = (int) Math.floor(player.posY);
					spawnZ = (int) Math.floor(player.posZ);
					spawnDimension = player.dimension;
				}

				if (args.length > 2)
				{
					if (args.length < 6)
					{
						throw wu;
					}
					if (args.length > 5)
					{
						spawnX = Integer.parseInt(args[2]);
						spawnY = Integer.parseInt(args[3]);
						spawnZ = Integer.parseInt(args[4]);
						spawnDimension = Integer.parseInt(args[5]);
					}
					
					if (args.length == 7)
					{
						exactSpawn = Boolean.parseBoolean(args[6]);
					}
					else if (args.length == 8)
					{
						exactSpawn = Boolean.parseBoolean(args[6]);
						forceBed = Boolean.parseBoolean(args[7]);
					}
					else if (args.length == 9)
					{
						exactSpawn = Boolean.parseBoolean(args[6]);
						forceBed = Boolean.parseBoolean(args[7]);
						spawnProtection = Boolean.parseBoolean(args[8]);
					}
					else if (args.length>9)
					{
						throw wu;
					}
				}

				if (mode.equals("world") && FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
				{
					throw new WrongUsageException("There are no world (save) folders on a server. Use main instead");
				}
				sender.addChatMessage(new TextComponentString("Saving Spawn Data"));

				sender.addChatMessage(new TextComponentString(" Spawn-X: "+spawnX));
				sender.addChatMessage(new TextComponentString(" Spawn-Y: "+spawnY));
				sender.addChatMessage(new TextComponentString(" Spawn-Z: "+spawnZ));
				sender.addChatMessage(new TextComponentString(" Spawn-Dimension: "+spawnDimension));
				sender.addChatMessage(new TextComponentString(" Exact-Spawn: "+exactSpawn));
				sender.addChatMessage(new TextComponentString(" Force-Bed: "+forceBed));
				sender.addChatMessage(new TextComponentString(" Spawn-Protection: "+spawnProtection));

				JsonWriter jsonWriter = null;
				FileWriter writer = null;
				File f = null;

				if (mode.equals("main"))
				{
					f = new File("PerfectSpawn.json");
				}
				else
				{
					File worldDictionary = DimensionManager.getCurrentSaveRootDirectory();
					f = new File(worldDictionary, "PerfectSpawn.json");
				}

				if (f.exists())
				{
					f.delete();
				}
				try
				{
					writer = new FileWriter(f);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				jsonWriter = new JsonWriter(writer);
				jsonWriter.setIndent("  ");
				try
				{
					jsonWriter.beginObject();
					jsonWriter.name("spawnDimension");
					jsonWriter.value(spawnDimension);

					jsonWriter.name("spawnX");
					jsonWriter.value(spawnX);

					jsonWriter.name("spawnY");
					jsonWriter.value(spawnY);

					jsonWriter.name("spawnZ");
					jsonWriter.value(spawnZ);

					jsonWriter.name("exactSpawn");
					jsonWriter.value(exactSpawn);

					jsonWriter.name("forceBed");
					jsonWriter.value(forceBed);

					jsonWriter.name("spawnProtection");
					jsonWriter.value(spawnProtection);

					jsonWriter.endObject();
				}
				catch (IOException io)
				{
					throw new CommandException("Error writing config values", (Object[]) null);
				}
				finally
				{
					try
					{
						jsonWriter.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				
				PerfectSpawn.settings.reload();
				
				sender.addChatMessage(new TextComponentString("Saved & Reloaded Spawn Data"));
			}
			else
			{
				throw new WrongUsageException("Invalid Subcommand (reload,set)", (Object[]) null);
			}
		}
	}
}
