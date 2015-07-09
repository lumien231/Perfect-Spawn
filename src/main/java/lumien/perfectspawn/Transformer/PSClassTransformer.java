package lumien.perfectspawn.Transformer;

import java.util.Iterator;

import lumien.perfectspawn.Core.CoreHandler;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import static org.objectweb.asm.Opcodes.*;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

public class PSClassTransformer implements IClassTransformer
{
	String OBF_SERVER_CONFIGURATION_MANAGER = "ld";
	String OBF_WORLD_PROVIDER = "apa";
	String BED_OBFUSCATED = "aht";

	Logger logger = LogManager.getLogger("PerfectSpawnCore");

	@Override
	public byte[] transform(String name, String transformedName, byte[] data)
	{
		if (data == null)
			return null;

		logger.log(Level.DEBUG, "Transforming "+name);
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
		return data;
	}

	private byte[] patchDedicatedServer(byte[] data)
	{
		logger.log(Level.INFO, "Patching DedicatedServer Class");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(data);
		classReader.accept(classNode, 0);
		
		String isBlockProtectedName = MCPNames.method("func_96290_a");
		MethodNode isBlockProtected = null;

		for (MethodNode mn : classNode.methods)
		{
			if (mn.name.equals(isBlockProtectedName))
			{
				isBlockProtected = mn;
				break;
			}
		}
		
		if (isBlockProtected!=null)
		{
			for (int i = 0; i < isBlockProtected.instructions.size(); i++)
			{
				AbstractInsnNode ain = isBlockProtected.instructions.get(i);

				if (ain instanceof FieldInsnNode)
				{
					FieldInsnNode fin = (FieldInsnNode) ain;
					if (fin.getOpcode() == GETFIELD)
					{
						if (fin.name.equals(MCPNames.field("field_76574_g")))
						{
							logger.log(Level.INFO, "- Patched Spawn Protection Control");

							isBlockProtected.instructions.insert(fin,new MethodInsnNode(INVOKESTATIC, "lumien/perfectspawn/Core/CoreHandler", "isBlockNotProtectedByDimension", "(I)Z"));
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
		logger.log(Level.INFO, "Patching Bed Class");
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(data);
		classReader.accept(classNode, 0);

		String onBlockActivatedName = MCPNames.method("func_149727_a");
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
						String biomegenbase = "Lnet/minecraft/world/biome/BiomeGenBase;";
						if (fin.desc.equals(biomegenbase))
						{
							logger.log(Level.INFO, "- Patched Bed Biome Restriction");
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

		logger.log(Level.INFO, "Patching " + classNode.name);

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
		String chunkCoordinatesName = "net/minecraft/util/ChunkCoordinates";

		if (canRespawnHere != null)
		{
			logger.log(Level.INFO, "- Patched canRespawnHere");
			LabelNode l0 = new LabelNode(new Label());
			LabelNode l1 = new LabelNode(new Label());
			LabelNode l2 = new LabelNode(new Label());

			canRespawnHere.instructions.insert(new InsnNode(POP));
			canRespawnHere.instructions.insert(l2);
			canRespawnHere.instructions.insert(new InsnNode(IRETURN));
			canRespawnHere.instructions.insert(l1);
			canRespawnHere.instructions.insert(new JumpInsnNode(IFLT, l2));
			canRespawnHere.instructions.insert(new InsnNode(DUP));
			canRespawnHere.instructions.insert(new MethodInsnNode(INVOKESTATIC, "lumien/perfectspawn/Core/CoreHandler", "canRespawnHere", "(L" + worldProviderName + ";)I"));
			canRespawnHere.instructions.insert(new VarInsnNode(ALOAD, 0));
			canRespawnHere.instructions.insert(l0);
		}

		if (getRespawnDimension != null)
		{
			logger.log(Level.INFO, "- Patched getRespawnDimension");
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
			getRespawnDimension.instructions.insert(new MethodInsnNode(INVOKESTATIC, "lumien/perfectspawn/Core/CoreHandler", "getRespawnDimension", "(L" + worldProviderName + ";L" + entityPlayerMPName + ";)I"));
			getRespawnDimension.instructions.insert(new VarInsnNode(ALOAD, 1));
			getRespawnDimension.instructions.insert(new VarInsnNode(ALOAD, 0));
			getRespawnDimension.instructions.insert(l0);
		}

		if (isSurfaceWorld != null)
		{
			logger.log(Level.INFO, "- Patched isSurfaceWorld");
			LabelNode l0 = new LabelNode(new Label());
			LabelNode l1 = new LabelNode(new Label());
			LabelNode l2 = new LabelNode(new Label());

			isSurfaceWorld.instructions.insert(l2);
			isSurfaceWorld.instructions.insert(new InsnNode(IRETURN));
			isSurfaceWorld.instructions.insert(l1);
			isSurfaceWorld.instructions.insert(new JumpInsnNode(IF_ICMPEQ, l2));
			isSurfaceWorld.instructions.insert(new IntInsnNode(BIPUSH, -126));
			isSurfaceWorld.instructions.insert(new InsnNode(DUP));
			isSurfaceWorld.instructions.insert(new MethodInsnNode(INVOKESTATIC, "lumien/perfectspawn/Core/CoreHandler", "isSurfaceWorld", "(L" + worldProviderName + ";)I"));
			isSurfaceWorld.instructions.insert(new VarInsnNode(ALOAD, 0));
			isSurfaceWorld.instructions.insert(l0);
		}
		
		if (getRandomizedSpawnPoint!=null)
		{
			logger.log(Level.INFO, "- Patched getRandomizedSpawnPoint");
			
			LabelNode l0 = new LabelNode(new Label());
			LabelNode l1 = new LabelNode(new Label());
			LabelNode l2 = new LabelNode(new Label());

			getRandomizedSpawnPoint.instructions.insert(new InsnNode(POP));
			getRandomizedSpawnPoint.instructions.insert(l2);
			getRandomizedSpawnPoint.instructions.insert(new InsnNode(ARETURN));
			getRandomizedSpawnPoint.instructions.insert(l1);
			getRandomizedSpawnPoint.instructions.insert(new JumpInsnNode(IFNULL, l2));
			getRandomizedSpawnPoint.instructions.insert(new InsnNode(DUP));
			getRandomizedSpawnPoint.instructions.insert(new MethodInsnNode(INVOKESTATIC, "lumien/perfectspawn/Core/CoreHandler", "getRandomizedSpawnPoint", "(L" + worldProviderName + ";)L"+chunkCoordinatesName+";"));
			getRandomizedSpawnPoint.instructions.insert(new VarInsnNode(ALOAD, 0));
			getRandomizedSpawnPoint.instructions.insert(l0);
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);

		return writer.toByteArray();
	}
}
