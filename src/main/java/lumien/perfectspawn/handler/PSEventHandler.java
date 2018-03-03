package lumien.perfectspawn.handler;

import java.util.List;

import com.google.common.base.Predicate;

import lumien.perfectspawn.PerfectSpawn;
import lumien.perfectspawn.config.PSConfig;
import lumien.perfectspawn.config.internal.Bool;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PSEventHandler
{
	@SubscribeEvent
	public void rightClickBlock(RightClickBlock event)
	{
		
	}

	@SubscribeEvent
	public void sleepInBed(PlayerSleepInBedEvent event)
	{
		PSConfig config = PerfectSpawn.INSTANCE.getConfigHandler().getActiveConfig();

		if (config != null)
		{
			Bool value = config.canSleepHere(event.getEntity().world.provider);
			if (value == Bool.TRUE)
			{
				event.setResult(forceBed(event));
			}
			else if (value == Bool.FALSE)
			{
				event.setResult(SleepResult.NOT_POSSIBLE_HERE);
			}
		}
	}

	private SleepResult forceBed(PlayerSleepInBedEvent event)
	{
		EntityPlayer player = event.getEntityPlayer();

		final IBlockState state = player.world.isBlockLoaded(event.getPos()) ? player.world.getBlockState(event.getPos()) : null;
		final boolean isBed = state != null && state.getBlock().isBed(state, player.world, event.getPos(), player);
		final EnumFacing enumfacing = isBed && state.getBlock() instanceof BlockHorizontal ? (EnumFacing) state.getValue(BlockHorizontal.FACING) : null;

		if (!player.world.isRemote)
		{
			if (player.isPlayerSleeping() || !player.isEntityAlive())
			{
				return EntityPlayer.SleepResult.OTHER_PROBLEM;
			}

			if (player.world.provider.isSurfaceWorld() && player.world.isDaytime())
			{
				return EntityPlayer.SleepResult.NOT_POSSIBLE_NOW;
			}

			if (!bedInRange(player, event.getPos(), enumfacing))
			{
				return EntityPlayer.SleepResult.TOO_FAR_AWAY;
			}

			double d0 = 8.0D;
			double d1 = 5.0D;
			List<EntityMob> list = player.world.<EntityMob> getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB((double) event.getPos().getX() - 8.0D, (double) event.getPos().getY() - 5.0D, (double) event.getPos().getZ() - 8.0D, (double) event.getPos().getX() + 8.0D, (double) event.getPos().getY() + 5.0D, (double) event.getPos().getZ() + 8.0D), new Predicate<EntityMob>()
			{
				@Override
				public boolean apply(EntityMob input)
				{
					return input.isPreventingPlayerRest(player);
				}
			});

			if (!list.isEmpty())
			{
				return EntityPlayer.SleepResult.NOT_SAFE;
			}
		}

		if (player.isRiding())
		{
			player.dismountRidingEntity();
		}

		ReflectionHandler.spawnShoulderEntities(player);
		ReflectionHandler.setSize(player, 0.2F, 0.2F);

		if (enumfacing != null)
		{
			float f1 = 0.5F + (float) enumfacing.getFrontOffsetX() * 0.4F;
			float f = 0.5F + (float) enumfacing.getFrontOffsetZ() * 0.4F;

			ReflectionHandler.setRenderOffsetForSleep(player, enumfacing);
			player.setPosition((double) ((float) event.getPos().getX() + f1), (double) ((float) event.getPos().getY() + 0.6875F), (double) ((float) event.getPos().getZ() + f));
		}
		else
		{
			player.setPosition((double) ((float) event.getPos().getX() + 0.5F), (double) ((float) event.getPos().getY() + 0.6875F), (double) ((float) event.getPos().getZ() + 0.5F));
		}

		ReflectionHandler.setSleeping(player, true);

		ReflectionHandler.setSleepTimer(player, 0);
		player.bedLocation = event.getPos();
		player.motionX = 0.0D;
		player.motionY = 0.0D;
		player.motionZ = 0.0D;

		if (!player.world.isRemote)
		{
			player.world.updateAllPlayersSleepingFlag();
		}

		return EntityPlayer.SleepResult.OK;
	}

	private EntityPlayer getPlayerInBed(World worldIn, BlockPos pos)
	{
		for (EntityPlayer entityplayer : worldIn.playerEntities)
		{
			if (entityplayer.isPlayerSleeping() && entityplayer.bedLocation.equals(pos))
			{
				return entityplayer;
			}
		}

		return null;
	}

	private static boolean bedInRange(EntityPlayer player, BlockPos bedPos, EnumFacing bedFacing)
	{
		if (Math.abs(player.posX - (double) bedPos.getX()) <= 3.0D && Math.abs(player.posY - (double) bedPos.getY()) <= 2.0D && Math.abs(player.posZ - (double) bedPos.getZ()) <= 3.0D)
		{
			return true;
		}
		else if (bedFacing == null)
			return false;
		else
		{
			BlockPos blockpos = bedPos.offset(bedFacing.getOpposite());
			return Math.abs(player.posX - (double) blockpos.getX()) <= 3.0D && Math.abs(player.posY - (double) blockpos.getY()) <= 2.0D && Math.abs(player.posZ - (double) blockpos.getZ()) <= 3.0D;
		}
	}


}
