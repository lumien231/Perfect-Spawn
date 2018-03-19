package lumien.perfectspawn.asm;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

import net.minecraft.launchwrapper.IClassTransformer;

public class ClassTransformer implements IClassTransformer
{
	public static Logger logger = LogManager.getLogger("PerfectSpawnCore");

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
		else if (transformedName.equals("net.minecraft.block.BlockBed"))
		{
			return patchBlockBed(basicClass);
		}
		else if (transformedName.equals("net.minecraft.world.gen.ChunkProviderServer"))
		{
			return patchChunkProviderServer(basicClass);
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

	private byte[] patchChunkProviderServer(byte[] basicClass)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		logger.log(Level.DEBUG, "Found ChunkProviderServer Class: " + classNode.name);

		MethodNode tick = null;

		for (MethodNode mn : classNode.methods)
		{
			if (mn.name.equals(MCPNames.method("func_73156_b")))
			{
				tick = mn;
				break;
			}
		}

		if (tick != null)
		{
			logger.log(Level.DEBUG, " - Patching tick");

			for (int i = 0; i < tick.instructions.size(); i++)
			{
				AbstractInsnNode ain = tick.instructions.get(i);

				if (ain instanceof MethodInsnNode)
				{
					MethodInsnNode min = (MethodInsnNode) ain;

					if (min.name.equals("unloadWorld"))
					{
						LabelNode skipNode = new LabelNode(new Label());
						
						InsnList toInsert = new InsnList();
						toInsert.add(new MethodInsnNode(INVOKESTATIC, asmHandler, "unloadWorld", "(I)Z", false));
						toInsert.add(new JumpInsnNode(IFEQ, skipNode));
						toInsert.add(new VarInsnNode(ALOAD, 0));
						toInsert.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/gen/ChunkProviderServer", MCPNames.field("field_73251_h"), "Lnet/minecraft/world/WorldServer;"));
						toInsert.add(new FieldInsnNode(GETFIELD,  "net/minecraft/world/WorldServer", MCPNames.field("field_73011_w"), "Lnet/minecraft/world/WorldProvider;"));
						toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/WorldProvider", "getDimension", "()I", false));
						
						tick.instructions.insertBefore(min, toInsert);
						tick.instructions.insert(min, skipNode);
						
						break;
					}
				}
			}
		}

		ClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);

		return writer.toByteArray();
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
		MethodNode getRespawnDimension = null;

		for (MethodNode mn : classNode.methods)
		{
			if (mn.name.equals(MCPNames.method("func_76567_e")))
			{
				canRespawnHere = mn;
			}
			else if (mn.name.equals("getSpawnPoint"))
			{
				getSpawnPoint = mn;
			}
			else if (mn.name.equals("getRandomizedSpawnPoint"))
			{
				getRandomizedSpawnPoint = mn;
			}
			else if (mn.name.equals("getRespawnDimension"))
			{
				getRespawnDimension = mn;
			}
		}
		
		if (getRespawnDimension != null)
		{
			logger.log(Level.DEBUG, " - Patching getRespawnDimension");

			for (int i = 0; i < getRespawnDimension.instructions.size(); i++)
			{
				AbstractInsnNode ain = getRespawnDimension.instructions.get(i);

				if (ain.getOpcode() == IRETURN)
				{
					// Found return
					InsnList toInsert = new InsnList();
					toInsert.add(new VarInsnNode(ALOAD, 0));
					toInsert.add(new InsnNode(SWAP));
					toInsert.add(new MethodInsnNode(INVOKESTATIC, asmHandler, "overrideGetRespawnDimension", "(Lnet/minecraft/world/WorldProvider;I)I", false));
					getRespawnDimension.instructions.insertBefore(ain, toInsert);

					i += 3;
				}
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

		ClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
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

		ClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
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
		MethodNode onUpdate = null;

		for (MethodNode mn : classNode.methods)
		{
			if (mn.name.equals("getSpawnDimension"))
			{
				getSpawnDimension = mn;
			}
			else if (mn.name.equals(MCPNames.method("func_70071_h_")))
			{
				onUpdate = mn;
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

		if (onUpdate != null)
		{
			logger.log(Level.DEBUG, " - Found onUpdate");

			String getDayTimeName = MCPNames.method("func_72935_r");

			for (int i = 0; i < onUpdate.instructions.size(); i++)
			{
				AbstractInsnNode ain = onUpdate.instructions.get(i);

				if (ain instanceof MethodInsnNode)
				{
					MethodInsnNode min = (MethodInsnNode) ain;

					if (min.name.equals(getDayTimeName))
					{
						InsnList toInsert = new InsnList();

						toInsert.add(new VarInsnNode(ALOAD, 0));
						toInsert.add(new InsnNode(SWAP));
						toInsert.add(new MethodInsnNode(INVOKESTATIC, asmHandler, "keepInBedFix", "(Lnet/minecraft/entity/player/EntityPlayer;Z)Z", false));

						onUpdate.instructions.insert(min, toInsert);

						i += 3;
					}
				}
			}
		}

		ClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);

		return writer.toByteArray();
	}

	private byte[] patchBlockBed(byte[] basicClass)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		logger.log(Level.DEBUG, "Found BlockBed Class: " + classNode.name);

		MethodNode onBlockActivated = null;

		for (MethodNode mn : classNode.methods)
		{
			if (mn.name.equals(MCPNames.method("func_180639_a")))
			{
				onBlockActivated = mn;
				break;
			}
		}


		if (onBlockActivated != null)
		{
			logger.log(Level.DEBUG, " - Found onBlockActivated");

			for (int i = 0; i < onBlockActivated.instructions.size(); i++)
			{
				AbstractInsnNode ain = onBlockActivated.instructions.get(i);

				if (ain instanceof FieldInsnNode)
				{
					FieldInsnNode fin = (FieldInsnNode) ain;

					if (fin.name.equals(MCPNames.field("field_76778_j")))
					{
						// Found HELL comparison
						logger.log(Level.DEBUG, " - Found HELL comparison");

						InsnList toInsert = new InsnList();
						toInsert.add(new InsnNode(POP));
						toInsert.add(new VarInsnNode(ALOAD, 1));
						toInsert.add(new MethodInsnNode(INVOKESTATIC, asmHandler, "changeHellComparison", "(Lnet/minecraft/world/World;)Lnet/minecraft/world/biome/Biome;", false));


						onBlockActivated.instructions.insert(fin, toInsert);
					}
				}
			}
		}

		ClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);

		return writer.toByteArray();
	}

	private byte[] patchDummyClass(byte[] basicClass)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		logger.log(Level.DEBUG, "Found Dummy Class: " + classNode.name);

		CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);

		return writer.toByteArray();
	}
}
