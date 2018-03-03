package lumien.perfectspawn.handler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lumien.perfectspawn.asm.MCPNames;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

public class ReflectionHandler
{
	private static Method spawnShoulderEntities;
	private static Method setSize;
	private static Method setRenderOffsetForSleep;
	
	private static Field sleeping;
	private static Field sleepTimer;
	

	static
	{
		try
		{
			spawnShoulderEntities = EntityPlayer.class.getDeclaredMethod(MCPNames.method("func_192030_dh"));
			setSize = Entity.class.getDeclaredMethod(MCPNames.method("func_70105_a"), float.class, float.class);
			setRenderOffsetForSleep = EntityPlayer.class.getDeclaredMethod(MCPNames.method("func_175139_a"), EnumFacing.class);
			
			spawnShoulderEntities.setAccessible(true);
			setSize.setAccessible(true);
			setRenderOffsetForSleep.setAccessible(true);
			
			
			sleeping = EntityPlayer.class.getDeclaredField(MCPNames.field("field_71083_bS"));
			sleepTimer = EntityPlayer.class.getDeclaredField(MCPNames.field("field_71076_b"));
			
			sleeping.setAccessible(true);
			sleepTimer.setAccessible(true);
			
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}
	
	public static void setSleepTimer(EntityPlayer player, int value)
	{
		try
		{
			sleepTimer.set(player, value);
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void setSleeping(EntityPlayer player, boolean value)
	{
		try
		{
			sleeping.set(player, value);
		}
		catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void setRenderOffsetForSleep(EntityPlayer player, EnumFacing enumfacing)
	{
		try
		{
			setRenderOffsetForSleep.invoke(player, enumfacing);
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}

	// Entity.setSize
	public static void setSize(Entity entity, float width, float height)
	{
		try
		{
			setSize.invoke(entity, width, height);
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}


	// EntityPlayer.spawnShoulderEntities
	public static void spawnShoulderEntities(EntityPlayer player)
	{
		try
		{
			spawnShoulderEntities.invoke(player);
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}
}
