package com.kingrunes.somnia.asm;

import com.google.common.collect.Lists;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

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
		String 	methodSleep = obf ? "a" : "trySleep",
				methodGetWorldTime = obf ? "S" : "getWorldTime",
				methodSendStatusMessage = obf ? "a" : "sendStatusMessage",
				classEntityPlayer = obf ? "aed" : "net/minecraft/entity/player/EntityPlayer",
				classWorld = obf ? "amu" : "net/minecraft/world/World",
				classTextComponentTranslation = obf ? "hp" : "net/minecraft/util/text/TextComponentTranslation",
				classSleepResult = obf ? "aed$a" : "net/minecraft/entity/player/EntityPlayer$SleepResult",
				classBlockPos = obf ? "et" : "net/minecraft/util/math/BlockPos",
				descSleep = obf ? "(Let;)Laed$a;" : "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/entity/player/EntityPlayer$SleepResult;",
				descWorld = obf ? "Lamu;" : "Lnet/minecraft/world/World;",
				descSleepResult = obf ? "Laed$a;" : "Lnet/minecraft/entity/player/EntityPlayer$SleepResult;",
				descSendStatusMessage = obf ? "(Lhh;Z)V" : "(Lnet/minecraft/util/text/ITextComponent;Z)V",
				fieldWorld = obf ? "l" : "world",
				fieldOtherProblem = obf ? "e" : "OTHER_PROBLEM";

		Iterator<MethodNode> methods = classNode.methods.iterator();
		AbstractInsnNode ain;

		while(methods.hasNext())
		{
			MethodNode m = methods.next();
			if (m.name.equals(methodSleep) && m.desc.equals(descSleep))
			{
				Iterator<AbstractInsnNode> iter = m.instructions.iterator();
				while (iter.hasNext())
				{
					ain = iter.next();
					if (ain instanceof JumpInsnNode && m.instructions.indexOf(ain) == 108) {
						LabelNode label18 = ((JumpInsnNode) ain).label;
						for (byte i = 0; i < 4; i++) m.instructions.remove(m.instructions.get(105)); //Remove sleep time check

						InsnList insnList = new InsnList(); //Change sleep time check
						insnList.add(new FieldInsnNode(GETSTATIC, "com/kingrunes/somnia/common/CommonProxy", "enterSleepPeriod", "Lcom/kingrunes/somnia/common/util/TimePeriod;"));
						insnList.add(new VarInsnNode(ALOAD, 0));
						insnList.add(new FieldInsnNode(GETFIELD, classEntityPlayer, fieldWorld, descWorld));
						insnList.add(new MethodInsnNode(INVOKEVIRTUAL, classWorld, methodGetWorldTime, "()J", false));
						insnList.add(new LdcInsnNode(24000L));
						insnList.add(new InsnNode(LREM));
						insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "com/kingrunes/somnia/common/util/TimePeriod", "isTimeWithin", "(J)Z", false));
						LabelNode label175 = new LabelNode();
						insnList.add(new JumpInsnNode(IFNE, label175));
						m.instructions.insert(m.instructions.get(104), insnList);

						InsnList insnList2 = new InsnList(); //Fatigue check
						insnList2.add(((JumpInsnNode)m.instructions.get(112)).label);
						insnList2.add(new VarInsnNode(ALOAD, 0));
						insnList2.add(new MethodInsnNode(INVOKESTATIC, "com/kingrunes/somnia/Somnia", "checkFatigue", "(Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
						insnList2.add(new JumpInsnNode(IFNE, label18));
						LabelNode label176 = new LabelNode();
						insnList2.add(label176);
						insnList2.add(new VarInsnNode(ALOAD, 0));
						insnList2.add(new TypeInsnNode(NEW, classTextComponentTranslation));
						insnList2.add(new InsnNode(DUP));
						insnList2.add(new LdcInsnNode("somnia.status.cooldown"));
						insnList2.add(new InsnNode(ICONST_0));
						insnList2.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));
						insnList2.add(new MethodInsnNode(INVOKESPECIAL, classTextComponentTranslation, "<init>", "(Ljava/lang/String;[Ljava/lang/Object;)V", false));
						insnList2.add(new InsnNode(ICONST_1));
						insnList2.add(new MethodInsnNode(INVOKEVIRTUAL, classEntityPlayer, methodSendStatusMessage, descSendStatusMessage, false));
						LabelNode label177 = new LabelNode();
						insnList2.add(label177);
						insnList2.add(new FieldInsnNode(GETSTATIC, classSleepResult, fieldOtherProblem, descSleepResult));
						insnList2.add(new InsnNode(ARETURN));
						m.instructions.insert(m.instructions.get(116), insnList2);

						InsnList insnList3 = new InsnList(); //Add an ignoremonsters check to existing if statement
						insnList3.add(new FieldInsnNode(GETSTATIC, "com/kingrunes/somnia/common/SomniaConfig", "OPTIONS", "Lcom/kingrunes/somnia/common/SomniaConfig$Options;"));
						insnList3.add(new FieldInsnNode(GETFIELD, "com/kingrunes/somnia/common/SomniaConfig$Options", "ignoreMonsters", "Z"));
						JumpInsnNode ainsnode = (JumpInsnNode) m.instructions.get(204);
						insnList3.add(new JumpInsnNode(IFNE, ainsnode.label));
						m.instructions.insert(ainsnode, insnList3);

						InsnList insnList4 = new InsnList(); //Armor check
						LabelNode label20 = (LabelNode) m.instructions.get(146);
						LabelNode label195 = new LabelNode();
						m.instructions.insert(m.instructions.get(141), new JumpInsnNode(IFNE, label195));
						m.instructions.remove(m.instructions.get(141));
						insnList4.add(label195);
						insnList4.add(new FrameNode(Opcodes.F_APPEND, 2, new Object[]{classBlockPos, classEntityPlayer}, 0, null));
						insnList4.add(new FieldInsnNode(GETSTATIC, "com/kingrunes/somnia/common/SomniaConfig", "OPTIONS", "Lcom/kingrunes/somnia/common/SomniaConfig$Options;"));
						insnList4.add(new FieldInsnNode(GETFIELD, "com/kingrunes/somnia/common/SomniaConfig$Options", "sleepWithArmor", "Z"));
						insnList4.add(new JumpInsnNode(IFNE, label20));
						insnList4.add(new VarInsnNode(ALOAD, 0));
						insnList4.add(new MethodInsnNode(INVOKESTATIC, "com/kingrunes/somnia/Somnia", "doesPlayHaveAnyArmor", "(Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
						insnList4.add(new JumpInsnNode(IFEQ, label20));
						LabelNode label148 = new LabelNode();
						insnList4.add(label148);
						insnList4.add(new VarInsnNode(ALOAD, 0)); //Send armor status to player
						insnList4.add(new TypeInsnNode(NEW, classTextComponentTranslation));
						insnList4.add(new InsnNode(DUP));
						insnList4.add(new LdcInsnNode("somnia.status.armor"));
						insnList4.add(new InsnNode(ICONST_0));
						insnList4.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));
						insnList4.add(new MethodInsnNode(INVOKESPECIAL, classTextComponentTranslation, "<init>", "(Ljava/lang/String;[Ljava/lang/Object;)V", false));
						insnList4.add(new InsnNode(ICONST_1));
						insnList4.add(new MethodInsnNode(INVOKEVIRTUAL, classEntityPlayer, methodSendStatusMessage, descSendStatusMessage, false));

						LabelNode label149 = new LabelNode(); //Return
						insnList4.add(label149);
						insnList4.add(new FieldInsnNode(GETSTATIC, classSleepResult, fieldOtherProblem, descSleepResult));
						insnList4.add(new InsnNode(ARETURN));
						m.instructions.insert(m.instructions.get(145), insnList4);

						InsnList insnList5 = new InsnList();
						insnList5.add(new VarInsnNode(ALOAD, 0));
						insnList5.add(new MethodInsnNode(INVOKESTATIC, "com/kingrunes/somnia/Somnia", "updateWakeTime", "(Lnet/minecraft/entity/player/EntityPlayer;)V", false));
						m.instructions.insert(m.instructions.get(375), insnList5);
					}
				}
				break;
			}
		}
	}

	private void patchEntityRenderer(ClassNode classNode, boolean obf)
	{
		String methodName = obf ? "a" : "updateCameraAndRender";
		String methodName2 = obf ? "b" : "renderWorld";

		boolean f = true;

		for (MethodNode m : classNode.methods) {
			if (m.name.equals(methodName) && m.desc.equals("(F)V")) {
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

							vin = (VarInsnNode) m.instructions.get(m.instructions.indexOf(min) - (f ? 9 : 3));
							m.instructions.remove(vin);

							f = false;
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
			if (m.name.equals(methodName))
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