package lumien.perfectspawn.asm;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.POP;

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
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class PSClassTransformer implements IClassTransformer
{
	Logger logger = LogManager.getLogger("PerfectSpawnCore");

	@Override
	public byte[] transform(String name, String transformedName, byte[] data)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(data);
		classReader.accept(classNode, 0);

		if (classNode.superName.equals("net/minecraft/world/WorldProvider") || classNode.superName.equals("net/minecraft/world/WorldProviderHell") || classNode.superName.equals("net/minecraft/world/WorldProviderSurface") || classNode.superName.equals("net/minecraft/world/apa") || transformedName.equals("net.minecraft.world.WorldProvider"))
		{
			return patchWorldProvider(data);
		}
		else if (transformedName.equals("net.minecraft.block.BlockBed"))
		{
			return patchBed(data);
		}
		else if (transformedName.equals("net.minecraft.server.dedicated.DedicatedServer"))
		{
			return patchDedicatedServer(data);
		}
		else if (transformedName.equals("net.minecraft.entity.player.EntityPlayer"))
		{
			return patchEntityPlayer(data);
		}
		return data;
	}

	private byte[] patchEntityPlayer(byte[] data)
	{
		logger.log(Level.DEBUG, "Patching EntityPlayer Class");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(data);
		classReader.accept(classNode, 0);

		String onUpdateName = MCPNames.method("func_70071_h_");
		MethodNode onUpdate = null;

		for (MethodNode mn : classNode.methods)
		{
			if (mn.name.equals(onUpdateName))
			{
				onUpdate = mn;
				break;
			}
		}

		if (onUpdate != null)
		{
			String isDayTime = MCPNames.method("func_72935_r");

			for (int i = 0; i < onUpdate.instructions.size(); i++)
			{
				AbstractInsnNode ain = onUpdate.instructions.get(i);
				if (ain instanceof MethodInsnNode)
				{
					MethodInsnNode min = (MethodInsnNode) ain;

					if (min.name.equals(isDayTime))
					{
						AbstractInsnNode nextNode = onUpdate.instructions.get(i + 1);
						if (nextNode != null && nextNode instanceof JumpInsnNode)
						{
							logger.log(Level.DEBUG, "- Patched Staying in Bed Check");
							JumpInsnNode jin = (JumpInsnNode) nextNode;

							InsnList toInsert = new InsnList();
							toInsert.add(new VarInsnNode(ALOAD, 0));
							toInsert.add(new MethodInsnNode(INVOKESTATIC, "lumien/perfectspawn/core/CoreHandler", "canWakeUp", "(Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
							toInsert.add(new JumpInsnNode(Opcodes.IFEQ, new LabelNode(jin.label.getLabel())));

							onUpdate.instructions.insert(jin, toInsert);
						}
					}
				}
			}

		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);

		return writer.toByteArray();
	}

	private byte[] patchDedicatedServer(byte[] data)
	{
		logger.log(Level.DEBUG, "Patching DedicatedServer Class");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(data);
		classReader.accept(classNode, 0);

		String isBlockProtectedName = MCPNames.method("func_175579_a");
		MethodNode isBlockProtected = null;

		for (MethodNode mn : classNode.methods)
		{
			if (mn.name.equals(isBlockProtectedName))
			{
				isBlockProtected = mn;
				break;
			}
		}

		if (isBlockProtected != null)
		{
			for (int i = 0; i < isBlockProtected.instructions.size(); i++)
			{
				AbstractInsnNode ain = isBlockProtected.instructions.get(i);

				if (ain instanceof MethodInsnNode)
				{
					MethodInsnNode min = (MethodInsnNode) ain;
					if (min.getOpcode() == INVOKEVIRTUAL)
					{
						if (min.name.equals("getDimension"))
						{
							logger.log(Level.DEBUG, "- Patched Spawn Protection Control");

							isBlockProtected.instructions.insert(min, new MethodInsnNode(INVOKESTATIC, "lumien/perfectspawn/core/CoreHandler", "isBlockNotProtectedByDimension", "(I)Z", false));
							break;
						}
					}
				}
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);

		return writer.toByteArray();
	}

	private byte[] patchBed(byte[] data)
	{
		logger.log(Level.DEBUG, "Patching Bed Class");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(data);
		classReader.accept(classNode, 0);

		String onBlockActivatedName = MCPNames.method("func_180639_a");
		MethodNode onBlockActivated = null;

		for (MethodNode mn : classNode.methods)
		{
			if (mn.name.equals(onBlockActivatedName))
			{
				onBlockActivated = mn;
				break;
			}
		}

		if (onBlockActivated != null)
		{
			for (int i = 0; i < onBlockActivated.instructions.size(); i++)
			{
				AbstractInsnNode ain = onBlockActivated.instructions.get(i);

				if (ain instanceof FieldInsnNode)
				{
					FieldInsnNode fin = (FieldInsnNode) ain;
					if (fin.getOpcode() == GETSTATIC)
					{
						String biomegenbase = "Lnet/minecraft/world/biome/Biome;";
						if (fin.desc.equals(biomegenbase))
						{
							logger.log(Level.DEBUG, "- Patched Bed Biome Restriction");
							onBlockActivated.instructions.insert(ain, new InsnNode(ACONST_NULL));
							onBlockActivated.instructions.remove(ain);
							break;
						}
					}
				}
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);

		return writer.toByteArray();
	}

	private byte[] patchWorldProvider(byte[] data)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(data);
		classReader.accept(classNode, 0);

		String canRespawnHereName = MCPNames.method("func_76567_e");
		String getRespawnDimensionName = "getRespawnDimension";
		String isSurfaceWorldName = MCPNames.method("func_76569_d");
		String getRandomizedSpawnPointName = "getRandomizedSpawnPoint";

		logger.log(Level.DEBUG, "Patching " + classNode.name);

		MethodNode canRespawnHere = null;
		MethodNode getRespawnDimension = null;
		MethodNode isSurfaceWorld = null;
		MethodNode getRandomizedSpawnPoint = null;

		for (MethodNode mn : classNode.methods)
		{
			if (mn.name.equals(getRespawnDimensionName))
			{
				getRespawnDimension = mn;
			}
			else if (mn.name.equals(canRespawnHereName) && mn.desc.equals("()Z"))
			{
				canRespawnHere = mn;
			}
			else if (mn.name.equals(isSurfaceWorldName) && mn.desc.equals("()Z"))
			{
				isSurfaceWorld = mn;
			}
			else if (mn.name.equals(getRandomizedSpawnPointName))
			{
				getRandomizedSpawnPoint = mn;
			}
		}
		String worldProviderName = "net/minecraft/world/WorldProvider";
		String blockPosName = "net/minecraft/util/math/BlockPos";

		if (canRespawnHere != null)
		{
			logger.log(Level.DEBUG, "- Patched canRespawnHere");
			LabelNode l0 = new LabelNode(new Label());
			LabelNode l1 = new LabelNode(new Label());
			LabelNode l2 = new LabelNode(new Label());

			canRespawnHere.instructions.insert(new InsnNode(POP));
			canRespawnHere.instructions.insert(l2);
			canRespawnHere.instructions.insert(new InsnNode(IRETURN));
			canRespawnHere.instructions.insert(l1);
			canRespawnHere.instructions.insert(new JumpInsnNode(IFLT, l2));
			canRespawnHere.instructions.insert(new InsnNode(DUP));
			canRespawnHere.instructions.insert(new MethodInsnNode(INVOKESTATIC, "lumien/perfectspawn/core/CoreHandler", "canRespawnHere", "(L" + worldProviderName + ";)I", false));
			canRespawnHere.instructions.insert(new VarInsnNode(ALOAD, 0));
			canRespawnHere.instructions.insert(l0);
		}

		if (getRespawnDimension != null)
		{
			logger.log(Level.DEBUG, "- Patched getRespawnDimension");
			String entityPlayerMPName = "net/minecraft/entity/player/EntityPlayerMP";
			LabelNode l0 = new LabelNode(new Label());
			LabelNode l1 = new LabelNode(new Label());
			LabelNode l2 = new LabelNode(new Label());

			getRespawnDimension.instructions.insert(l2);
			getRespawnDimension.instructions.insert(new InsnNode(IRETURN));
			getRespawnDimension.instructions.insert(l1);
			getRespawnDimension.instructions.insert(new JumpInsnNode(IF_ICMPEQ, l2));
			getRespawnDimension.instructions.insert(new IntInsnNode(BIPUSH, -126));
			getRespawnDimension.instructions.insert(new InsnNode(DUP));
			getRespawnDimension.instructions.insert(new MethodInsnNode(INVOKESTATIC, "lumien/perfectspawn/core/CoreHandler", "getRespawnDimension", "(L" + worldProviderName + ";L" + entityPlayerMPName + ";)I", false));
			getRespawnDimension.instructions.insert(new VarInsnNode(ALOAD, 1));
			getRespawnDimension.instructions.insert(new VarInsnNode(ALOAD, 0));
			getRespawnDimension.instructions.insert(l0);
		}

		if (getRandomizedSpawnPoint != null)
		{
			logger.log(Level.DEBUG, "- Patched getRandomizedSpawnPoint");

			LabelNode l0 = new LabelNode(new Label());
			LabelNode l1 = new LabelNode(new Label());
			LabelNode l2 = new LabelNode(new Label());

			getRandomizedSpawnPoint.instructions.insert(new InsnNode(POP));
			getRandomizedSpawnPoint.instructions.insert(l2);
			getRandomizedSpawnPoint.instructions.insert(new InsnNode(ARETURN));
			getRandomizedSpawnPoint.instructions.insert(l1);
			getRandomizedSpawnPoint.instructions.insert(new JumpInsnNode(IFNULL, l2));
			getRandomizedSpawnPoint.instructions.insert(new InsnNode(DUP));
			getRandomizedSpawnPoint.instructions.insert(new MethodInsnNode(INVOKESTATIC, "lumien/perfectspawn/core/CoreHandler", "getRandomizedSpawnPoint", "(L" + worldProviderName + ";)L" + blockPosName + ";", false));
			getRandomizedSpawnPoint.instructions.insert(new VarInsnNode(ALOAD, 0));
			getRandomizedSpawnPoint.instructions.insert(l0);
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);

		return writer.toByteArray();
	}
}
