package lumien.perfectspawn.asm;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

import net.minecraft.launchwrapper.IClassTransformer;

public class ClassTransformer implements IClassTransformer
{
	Logger logger = LogManager.getLogger("PerfectSpawnCore");

	static final String asmHandler = "lumien/perfectspawn/handler/AsmHandler";

	public ClassTransformer()
	{

	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (basicClass == null)
		{
			return null;
		}

		if (transformedName.equals("net.minecraft.server.management.PlayerList"))
		{
			// Patches the dimension the server initially spawns a new player in
			return patchPlayerList(basicClass);
		}
		else if (transformedName.equals("net.minecraft.entity.player.EntityPlayer"))
		{
			return patchEntityPlayer(basicClass);
		}
		else
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(basicClass);
			classReader.accept(classNode, 0);

			if (classNode.superName.equals("net/minecraft/world/WorldProvider") || classNode.superName.equals("net/minecraft/world/WorldProviderHell") || classNode.superName.equals("net/minecraft/world/WorldProviderSurface") || transformedName.equals("net.minecraft.world.WorldProvider"))
			{
				return patchWorldProvider(basicClass);
			}
		}

		return basicClass;
	}

	private byte[] patchWorldProvider(byte[] basicClass)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		logger.log(Level.DEBUG, "Found a WorldProvider Class: " + classNode.name);

		MethodNode canRespawnHere = null;
		MethodNode getSpawnPoint = null;
		MethodNode getRandomizedSpawnPoint = null;

		for (MethodNode mn : classNode.methods)
		{
			if (mn.name.equals(MCPNames.method("func_76567_e")))
			{
				canRespawnHere = mn;
			}
			else if (mn.name.equals(MCPNames.method("func_175694_M")))
			{
				getSpawnPoint = mn;
			}
			else if (mn.name.equals("getRandomizedSpawnPoint"))
			{
				getRandomizedSpawnPoint = mn;
			}
		}

		if (canRespawnHere != null)
		{
			logger.log(Level.DEBUG, " - Patching canRespawnHere");

			for (int i = 0; i < canRespawnHere.instructions.size(); i++)
			{
				AbstractInsnNode ain = canRespawnHere.instructions.get(i);

				if (ain.getOpcode() == IRETURN)
				{
					// Found return
					InsnList toInsert = new InsnList();
					toInsert.add(new VarInsnNode(ALOAD, 0));
					toInsert.add(new InsnNode(SWAP));
					toInsert.add(new MethodInsnNode(INVOKESTATIC, asmHandler, "overrideCanRespawn", "(Lnet/minecraft/world/WorldProvider;Z)Z", false));
					canRespawnHere.instructions.insertBefore(ain, toInsert);

					i += 3;
				}
			}
		}

		if (getSpawnPoint != null)
		{
			logger.log(Level.DEBUG, " - Patching getSpawnPoint");

			for (int i = 0; i < getSpawnPoint.instructions.size(); i++)
			{
				AbstractInsnNode ain = getSpawnPoint.instructions.get(i);

				if (ain.getOpcode() == ARETURN)
				{
					// Found return
					InsnList toInsert = new InsnList();
					toInsert.add(new VarInsnNode(ALOAD, 0));
					toInsert.add(new InsnNode(SWAP));
					toInsert.add(new MethodInsnNode(INVOKESTATIC, asmHandler, "overrideGetSpawnPoint", "(Lnet/minecraft/world/WorldProvider;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;", false));
					getSpawnPoint.instructions.insertBefore(ain, toInsert);

					i += 3;
				}
			}
		}

		if (getRandomizedSpawnPoint != null)
		{
			logger.log(Level.DEBUG, " - Patching getRandomizedSpawnPoint");

			for (int i = 0; i < getRandomizedSpawnPoint.instructions.size(); i++)
			{
				AbstractInsnNode ain = getRandomizedSpawnPoint.instructions.get(i);

				if (ain.getOpcode() == ARETURN)
				{
					// Found return
					InsnList toInsert = new InsnList();
					toInsert.add(new VarInsnNode(ALOAD, 0));
					toInsert.add(new InsnNode(SWAP));
					toInsert.add(new MethodInsnNode(INVOKESTATIC, asmHandler, "overrideGetRandomizedSpawnPoint", "(Lnet/minecraft/world/WorldProvider;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;", false));
					getRandomizedSpawnPoint.instructions.insertBefore(ain, toInsert);

					i += 3;
				}
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);

		return writer.toByteArray();
	}

	private byte[] patchPlayerList(byte[] basicClass)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		logger.log(Level.DEBUG, "Found PlayerList Class: " + classNode.name);

		MethodNode createPlayerForUser = null;

		for (MethodNode mn : classNode.methods)
		{
			if (mn.name.equals(MCPNames.method("func_148545_a")))
			{
				createPlayerForUser = mn;
				break;
			}
		}

		if (createPlayerForUser != null)
		{
			logger.log(Level.DEBUG, " - Found createPlayerForUser");

			int counter = 0;
			for (int i = 0; i < createPlayerForUser.instructions.size(); i++)
			{
				AbstractInsnNode ain = createPlayerForUser.instructions.get(i);

				if (ain instanceof MethodInsnNode)
				{
					MethodInsnNode min = (MethodInsnNode) ain;

					if (min.name.equals(MCPNames.method("func_71218_a")))
					{
						InsnList toInsert = new InsnList();

						toInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, asmHandler, "overrideInitialDimension", "(I)I", false));

						createPlayerForUser.instructions.insertBefore(min, toInsert);

						i++;
						logger.log(Level.DEBUG, " - Patched getWorld (" + (++counter) + ")");
					}
				}
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);

		return writer.toByteArray();
	}

	private byte[] patchEntityPlayer(byte[] basicClass)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		logger.log(Level.DEBUG, "Found EntityPlayer Class: " + classNode.name);

		MethodNode getSpawnDimension = null;

		for (MethodNode mn : classNode.methods)
		{
			if (mn.name.equals("getSpawnDimension"))
			{
				getSpawnDimension = mn;
			}
		}

		if (getSpawnDimension != null)
		{
			logger.log(Level.DEBUG, " - Found getSpawnDimension");

			for (int i = 0; i < getSpawnDimension.instructions.size(); i++)
			{
				AbstractInsnNode ain = getSpawnDimension.instructions.get(i);

				if (ain.getOpcode() == ICONST_0)
				{
					InsnList toInsert = new InsnList();
					toInsert.add(new VarInsnNode(ALOAD, 0));
					toInsert.add(new InsnNode(SWAP));
					toInsert.add(new MethodInsnNode(INVOKESTATIC, asmHandler, "overrideDefaultPlayerSpawnDimension", "(Lnet/minecraft/entity/player/EntityPlayer;I)I", false));

					getSpawnDimension.instructions.insert(ain, toInsert);
					break;
				}
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);

		return writer.toByteArray();
	}

	private byte[] patchDummyClass(byte[] basicClass)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		logger.log(Level.INFO, "Found Dummy Class: " + classNode.name);

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);

		return writer.toByteArray();
	}
}
