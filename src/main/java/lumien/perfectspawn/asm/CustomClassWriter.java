package lumien.perfectspawn.asm;

import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassWriter;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public class CustomClassWriter extends ClassWriter
{
	static HashMap<Pair<String, String>, String> resultCache = new HashMap<Pair<String, String>, String>();

	static
	{
		resultCache.put(Pair.of("net/minecraft/entity/EntityLivingBase", "net/minecraft/entity/Entity"), "net/minecraft/entity/Entity");
		resultCache.put(Pair.of("net/minecraft/nbt/NBTTagCompound", "java/util/Iterator"), "java/lang/Object");
		resultCache.put(Pair.of("java/lang/Object", "java/util/Iterator"), "java/lang/Object");
		resultCache.put(Pair.of("java/lang/Object", "net/minecraft/nbt/NBTTagCompound"), "java/lang/Object");
		resultCache.put(Pair.of("net/minecraft/server/management/DemoPlayerInteractionManager", "net/minecraft/server/management/PlayerInteractionManager"), "net/minecraft/server/management/PlayerInteractionManager");
	}

	public CustomClassWriter(int flags)
	{
		super(flags);
	}

	@Override
	protected String getCommonSuperClass(String type1, String type2)
	{
		Pair<String, String> pair1 = Pair.of(type1, type2);
		Pair<String, String> pair2 = Pair.of(type2, type1);

		if (resultCache.containsKey(pair1))
		{
			return resultCache.get(pair1);
		}

		if (resultCache.containsKey(pair2))
		{
			return resultCache.get(pair2);
		}

		ClassTransformer.logger.log(Level.DEBUG, "Dangerous: " + type1 + " | " + type2);

		String superResult = super.getCommonSuperClass(type1, type2);

		ClassTransformer.logger.log(Level.DEBUG, "- " + superResult);

		return superResult;
	}
}
