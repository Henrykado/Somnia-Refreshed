package com.kingrunes.somnia.asm;

import com.google.common.collect.Lists;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;
import java.util.List;

public class SClassTransformer implements IClassTransformer
{
	private static final List<String> transformedClasses = Lists.newArrayList("net.minecraft.client.renderer.EntityRenderer",
			"net.minecraft.world.WorldServer",
			"net.minecraft.world.chunk.Chunk",
			"net.minecraft.server.MinecraftServer",
			"net.minecraft.entity.player.EntityPlayer");
	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		int index  = transformedClasses.indexOf(transformedName);
		boolean obf = !name.equals(transformedName);
		String[] split =  transformedName.split("\\.");
		return index > -1 ? transform(index, bytes, obf, split[split.length - 1]) : bytes;
	}

	private byte[] transform(int index, byte[] bytes, boolean obf, String className) {
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		System.out.println("[Somnia Core] Patching class "+className);
		switch (index) {
			case 0:
				patchEntityRenderer(classNode, obf);
				break;
			case 1:
				patchWorldServer(classNode, obf);
				break;
			case 2:
				patchChunk(classNode, obf);
				break;
			case 3:
				patchMinecraftServer(classNode);
				break;
			case 4:
				patchEntityPlayer(classNode, obf);
				break;
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);
		System.out.println("[Somnia Core] Successfully patched class "+className);
		return cw.toByteArray();
	}

	private void patchEntityPlayer(ClassNode classNode, boolean obf) {
		String methodName = obf ? "a" : "wakeUpPlayer";

		for (MethodNode m : classNode.methods) {
			if (m.name.equals(methodName) && m.desc.equals("(ZZZ)V")) {
				InsnList insnList = new InsnList();

				LabelNode label9 = new LabelNode();
				insnList.add(label9);
				insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
				insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/kingrunes/somnia/Somnia", "shouldResetSpawn", "(Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
				insnList.add(new VarInsnNode(Opcodes.ISTORE, 3));
				m.instructions.insertBefore(m.instructions.get(0), insnList);

				break;
			}
		}
	}

	private void patchEntityRenderer(ClassNode classNode, boolean obf)
	{
		String methodName = obf ? "a" : "updateCameraAndRender";
		String methodName2 = obf ? "b" : "renderWorld";

		for (MethodNode m : classNode.methods) {
			if (m.name.equals(methodName) && m.desc.equals("(FJ)V")) {
				AbstractInsnNode ain;
				MethodInsnNode min;
				VarInsnNode vin;
				Iterator<AbstractInsnNode> iter = m.instructions.iterator();
				while (iter.hasNext()) {
					ain = iter.next();
					if (ain instanceof MethodInsnNode) {
						min = (MethodInsnNode) ain;
						if (min.name.equals(methodName2) && min.desc.equalsIgnoreCase("(FJ)V") && min.getOpcode() == Opcodes.INVOKEVIRTUAL) {
							min.setOpcode(Opcodes.INVOKESTATIC);
							min.name = "renderWorld";
							min.owner = "com/kingrunes/somnia/Somnia";

							vin = (VarInsnNode) m.instructions.get(m.instructions.indexOf(min) - 5);
							m.instructions.remove(vin);
						}
					}
				}
				break;
			}
		}
	}


	private void patchWorldServer(ClassNode classNode, boolean obf)
	{
		String 	methodTick = obf ? "d" : "tick",
				methodGetGameRule = obf ? "b" : "getBoolean";

		Iterator<MethodNode> methods = classNode.methods.iterator();
		AbstractInsnNode ain;
		while(methods.hasNext())
		{
			MethodNode m = methods.next();
			if (m.name.equals(methodTick) && m.desc.equals("()V"))
			{
				Iterator<AbstractInsnNode> iter = m.instructions.iterator();
				MethodInsnNode min;
				while (iter.hasNext())
				{
					ain = iter.next();
					if (ain instanceof MethodInsnNode)
					{
						min = (MethodInsnNode)ain;
						if (min.name.equals(methodGetGameRule) && min.desc.equals("(Ljava/lang/String;)Z"))
						{
							int index = m.instructions.indexOf(min);

							LdcInsnNode lin = (LdcInsnNode)m.instructions.get(index-1);
							if (lin.cst.equals("doMobSpawning"))
							{
								min.setOpcode(Opcodes.INVOKESTATIC);
								min.desc = "(Lnet/minecraft/world/WorldServer;)Z";
								min.name = "doMobSpawning";
								min.owner = "com/kingrunes/somnia/Somnia";

								m.instructions.remove(lin);
								m.instructions.remove(m.instructions.get(index-2));
								break;
							}
						}
					}
				}
				break;
			}
		}
	}

	private void patchChunk(ClassNode classNode, boolean obf)
	{
		String methodName = obf ? "b" : "onTick";
		String methodName2 = obf ? "o" : "checkLight";

		Iterator<MethodNode> methods = classNode.methods.iterator();
		AbstractInsnNode ain;
		while(methods.hasNext())
		{
			MethodNode m = methods.next();
			if (m.name.equals(methodName) && m.desc.equals("(Z)V"))
			{
				Iterator<AbstractInsnNode> iter = m.instructions.iterator();
				while (iter.hasNext())
				{
					ain = iter.next();
					if (ain instanceof MethodInsnNode)
					{
						MethodInsnNode min = (MethodInsnNode)ain;
						if (min.name.equals(methodName2))
						{
							min.setOpcode(Opcodes.INVOKESTATIC);
							min.desc = "(Lnet/minecraft/world/chunk/Chunk;)V";
							min.name = "chunkLightCheck";
							min.owner = "com/kingrunes/somnia/Somnia";
						}
					}
				}
				break;
			}
		}
	}

	private void patchMinecraftServer(ClassNode classNode)
	{
		Iterator<MethodNode> methods = classNode.methods.iterator();
		AbstractInsnNode ain;
		while(methods.hasNext())
		{
			MethodNode m = methods.next();
			if ((m.name.equals("C") || m.name.equals("tick")) && m.desc.equals("()V"))
			{
				AbstractInsnNode lrin = null;
				Iterator<AbstractInsnNode> iter = m.instructions.iterator();
				while (iter.hasNext())
				{
					ain = iter.next();
					if (ain instanceof InsnNode && (ain).getOpcode() == Opcodes.RETURN)
						lrin = ain;
				}

				if (lrin != null)
				{
					InsnList toInject = new InsnList();
					toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/kingrunes/somnia/Somnia", "tick", "()V", false));

					m.instructions.insertBefore(lrin, toInject);
				}
				break;
			}
		}
	}
}