package lumien.perfectspawn.core;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.logging.log4j.Level;

import lumien.perfectspawn.PerfectSpawn;
import lumien.perfectspawn.asm.MCPNames;
import lumien.perfectspawn.core.PerfectSpawnSettings.SettingEntry;
import lumien.perfectspawn.network.MessageHandler;
import lumien.perfectspawn.network.PerfectSpawnSettingsMessage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class PSEventHandler
{
	Field sleepTimer;
	Field sleeping;

	public PSEventHandler()
	{
		try
		{
			sleepTimer = EntityPlayer.class.getDeclaredField(MCPNames.field("field_71076_b"));
			sleepTimer.setAccessible(true);

			sleeping = EntityPlayer.class.getDeclaredField(MCPNames.field("field_71083_bS"));
			sleeping.setAccessible(true);
		}
		catch (NoSuchFieldException nsf)
		{
			nsf.printStackTrace();
		}
	}

	@SubscribeEvent
	public void worldLoaded(WorldEvent.Load event)
	{
		if (!event.getWorld().isRemote)
		{
			SettingEntry se = PerfectSpawn.settings.getValidSettingEntry();
			if (se != null && se.spawnDimension == event.getWorld().provider.getDimension())
			{
				setSpawnPoint(se.spawnDimension, se.spawnX, se.spawnY, se.spawnZ);
			}
		}
	}

	public static final String NBT_KEY = "perfectspawnJoined";

	@SubscribeEvent
	public void playerLogin(PlayerLoggedInEvent event)
	{
		SettingEntry se = PerfectSpawn.settings.getValidSettingEntry();

		PerfectSpawnSettingsMessage message = null;

		if (se == null)
		{
			message = new PerfectSpawnSettingsMessage();
		}
		else
		{
			message = new PerfectSpawnSettingsMessage(se);
		}

		MessageHandler.INSTANCE.sendTo(message, (EntityPlayerMP) event.player);

		if (!event.player.worldObj.isRemote)
		{
			NBTTagCompound data = event.player.getEntityData();
			NBTTagCompound persistent;

			if (!data.hasKey(EntityPlayer.PERSISTED_NBT_TAG))
			{
				data.setTag(EntityPlayer.PERSISTED_NBT_TAG, (persistent = new NBTTagCompound()));
			}
			else
			{
				persistent = data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
			}

			if (!persistent.hasKey(NBT_KEY))
			{
				persistent.setBoolean(NBT_KEY, true);
				if (se != null)
				{
					if (se.spawnDimension != event.player.dimension)
					{
						FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().transferPlayerToDimension((EntityPlayerMP) event.player, se.spawnDimension, new PerfectSpawnTeleporter(FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(se.spawnDimension)));
					}
					((EntityPlayerMP) event.player).playerNetServerHandler.setPlayerLocation(se.spawnX + 0.5, se.spawnY, se.spawnZ + 0.5, event.player.cameraYaw, event.player.cameraPitch);
					event.player.getEntityData().setBoolean("psjoined", true);
				}
			}
		}
	}

	@SubscribeEvent
	public void sleepInBed(PlayerSleepInBedEvent event)
	{
		SettingEntry se = null;
		if (event.getEntityPlayer().worldObj.isRemote)
		{
			se = PerfectSpawnClientHandler.currentServerSettings;
		}
		else
		{
			se = PerfectSpawn.settings.getValidSettingEntry();
		}

		if (se != null && se.forceBed && event.getEntityPlayer().worldObj.provider.getDimension() == se.spawnDimension)
		{
			WorldProvider provider = event.getEntityPlayer().worldObj.provider;
			EntityPlayer player = event.getEntityPlayer();
			World worldObj = player.worldObj;
			BlockPos bedLocation = event.getPos();

			if (!player.worldObj.isRemote)
			{
				if (player.isPlayerSleeping() || !player.isEntityAlive())
				{
					event.setResult(EntityPlayer.EnumStatus.OTHER_PROBLEM);
					return;
				}

				if (!player.worldObj.provider.isSurfaceWorld())
				{
					// event.setResult(EntityPlayer.EnumStatus.NOT_POSSIBLE_HERE);
					// FORCE
				}

				if (player.worldObj.provider.isSurfaceWorld() && player.worldObj.isDaytime())
				{
					event.setResult(EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW);
					return;
				}

				if (Math.abs(player.posX - (double) bedLocation.getX()) > 3.0D || Math.abs(player.posY - (double) bedLocation.getY()) > 2.0D || Math.abs(player.posZ - (double) bedLocation.getZ()) > 3.0D)
				{
					event.setResult(EntityPlayer.EnumStatus.TOO_FAR_AWAY);
					return;
				}

				double d0 = 8.0D;
				double d1 = 5.0D;
				List<EntityMob> list = player.worldObj.<EntityMob> getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB((double) bedLocation.getX() - d0, (double) bedLocation.getY() - d1, (double) bedLocation.getZ() - d0, (double) bedLocation.getX() + d0, (double) bedLocation.getY() + d1, (double) bedLocation.getZ() + d0));

				if (!list.isEmpty())
				{
					event.setResult(EntityPlayer.EnumStatus.NOT_SAFE);
					return;
				}
			}

			if (player.isRiding())
			{
				player.dismountRidingEntity();
			}

			setSize(player, 0.2F, 0.2F);

			IBlockState state = null;
			if (player.worldObj.isBlockLoaded(bedLocation))
				state = player.worldObj.getBlockState(bedLocation);
			if (state != null && state.getBlock().isBed(state, player.worldObj, bedLocation, player))
			{
				EnumFacing enumfacing = state.getBlock().getBedDirection(state, player.worldObj, bedLocation);
				float f = 0.5F;
				float f1 = 0.5F;

				switch (enumfacing)
				{
					case SOUTH:
						f1 = 0.9F;
						break;
					case NORTH:
						f1 = 0.1F;
						break;
					case WEST:
						f = 0.1F;
						break;
					case EAST:
						f = 0.9F;
				}

				setRenderOffsetForSleep(player, enumfacing);
				player.setPosition((double) ((float) bedLocation.getX() + f), (double) ((float) bedLocation.getY() + 0.6875F), (double) ((float) bedLocation.getZ() + f1));
			}
			else
			{
				player.setPosition((double) ((float) bedLocation.getX() + 0.5F), (double) ((float) bedLocation.getY() + 0.6875F), (double) ((float) bedLocation.getZ() + 0.5F));
			}

			try
			{
				sleeping.set(player, true);
				sleepTimer.set(player, 0);
			}
			catch (Exception e)
			{
				PerfectSpawn.instance.logger.log(Level.ERROR, "Error reflecting Player Sleep Data");
				e.printStackTrace();
			}

			player.playerLocation = bedLocation;
			player.motionX = player.motionZ = player.motionY = 0.0D;

			if (!player.worldObj.isRemote)
			{
				player.worldObj.updateAllPlayersSleepingFlag();
			}

			event.setResult(EntityPlayer.EnumStatus.OK);
		}
	}

	private void setRenderOffsetForSleep(EntityPlayer player, EnumFacing facing)
	{
		player.renderOffsetX = 0.0F;
		player.renderOffsetZ = 0.0F;

		switch (facing)
		{
			case SOUTH:
				player.renderOffsetZ = -1.8F;
				break;
			case NORTH:
				player.renderOffsetZ = 1.8F;
				break;
			case WEST:
				player.renderOffsetX = 1.8F;
				break;
			case EAST:
				player.renderOffsetX = -1.8F;
		}
	}

	public static void setSpawnPoint(int dimension, int spawnX, int spawnY, int spawnZ)
	{
		World dimensionWorld = DimensionManager.getWorld(dimension);
		if (dimensionWorld instanceof WorldServerMulti)
		{
			WorldServerMulti w = (WorldServerMulti) DimensionManager.getWorld(dimension);
			DerivedWorldInfo worldInfo = (DerivedWorldInfo) w.getWorldInfo();

			try
			{
				Field f = DerivedWorldInfo.class.getDeclaredField(MCPNames.field("field_76115_a"));
				f.setAccessible(true);
				WorldInfo info = (WorldInfo) f.get(worldInfo);
				info.setSpawn(new BlockPos(spawnX, spawnY, spawnZ));
			}
			catch (Exception e)
			{
				PerfectSpawn.instance.logger.log(Level.ERROR, "Couldn't set spawn position");
				e.printStackTrace();
			}
		}
		else
		{
			WorldInfo info = dimensionWorld.getWorldInfo();
			info.setSpawn(new BlockPos(spawnX, spawnY, spawnZ));
		}
	}

	private void setSize(Entity entity, float width, float height)
	{
		if (width != entity.width || height != entity.height)
		{
			float f2 = entity.width;
			entity.width = width;
			entity.height = height;
			entity.setEntityBoundingBox(new AxisAlignedBB(entity.getEntityBoundingBox().minX, entity.getEntityBoundingBox().minY, entity.getEntityBoundingBox().minZ, entity.getEntityBoundingBox().minX + (double) entity.width, entity.getEntityBoundingBox().minY + (double) entity.height, entity.getEntityBoundingBox().minZ + (double) entity.width));

			if (entity.width > f2 && !entity.worldObj.isRemote)
			{
				entity.moveEntity((double) (f2 - entity.width), 0.0D, (double) (f2 - entity.width));
			}
		}
	}
}
