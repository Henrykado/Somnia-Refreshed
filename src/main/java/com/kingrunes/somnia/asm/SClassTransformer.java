package com.kingrunes.somnia.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

import static org.objectweb.asm.Opcodes.*;

public class SClassTransformer implements IClassTransformer
{
	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		if (name.equalsIgnoreCase("net.minecraft.client.renderer.EntityRenderer"))
			return patchEntityRenderer(bytes, false);
		else if (name.equalsIgnoreCase("buq"))
			return patchEntityRenderer(bytes, true);
		else if (name.equalsIgnoreCase("net.minecraft.world.WorldServer"))
			return patchWorldServer(bytes, false);
		else if (name.equalsIgnoreCase("oo"))
			return patchWorldServer(bytes, true);
		else if (name.equalsIgnoreCase("net.minecraft.world.chunk.Chunk"))
			return patchChunk(bytes, false);
		else if (name.equalsIgnoreCase("axw"))
			return patchChunk(bytes, true);
		else if (name.equalsIgnoreCase("net.minecraft.server.MinecraftServer"))
			return patchMinecraftServer(bytes);
		else if (name.equalsIgnoreCase("net.minecraft.item.ItemClock") || name.equalsIgnoreCase("ahl"))
			return patchItemClock(bytes);
		else if (name.equalsIgnoreCase("net.minecraft.entity.player.EntityPlayer"))
			return patchEntityPlayer(bytes, false);
		else if (name.equalsIgnoreCase("aed"))
			return patchEntityPlayer(bytes, true);
		else if (name.equalsIgnoreCase("net.minecraft.block.BlockBed"))
			return patchBlockBed(bytes, false);
		else if (name.equalsIgnoreCase("aou"))
			return patchBlockBed(bytes, true);
		return bytes;
	}

	private byte[] patchBlockBed(byte[] bytes, boolean obf) {
		String 	methodOnBlockActivated = obf ? "a" : "onBlockActivated",
				descOnBlockActivated = obf ? "(Lamu;Let;Lawt;Laed;Lub;Lfa;FFF)Z" : "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/EnumFacing;FFF)Z",
				classItemStack = obf ? "aip" : "net/minecraft/item/ItemStack";

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		for (MethodNode m : classNode.methods) {
			if (m.name.equals(methodOnBlockActivated) && m.desc.equals(descOnBlockActivated)) {
				//Add wake time calculation
				InsnList insnList = new InsnList();
				insnList.add(new FrameNode(Opcodes.F_APPEND, 1, new Object[]{classItemStack}, 0, null)); //TODO: Do I need this?
				insnList.add(new VarInsnNode(ALOAD, 1));
				insnList.add(new MethodInsnNode(INVOKESTATIC, "com/kingrunes/somnia/Somnia", "updateWakeTime", "(Lnet/minecraft/world/World;)V", false));
				m.instructions.insert(m.instructions.get(6), insnList);
				break;
			}
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);
		System.out.println("[Somnia Core] Successfully patched BlockBed");
		return cw.toByteArray();
	}

	private byte[] patchEntityPlayer(byte[] bytes, boolean obf) {
		String 	methodSleep = obf ? "a" : "trySleep",
				methodGetWorldTime = obf ? "S" : "getWorldTime",
				methodSendStatusMessage = obf ? "a" : "sendStatusMessage",
				classEntityPlayer = obf ? "aed" : "net/minecraft/entity/player/EntityPlayer",
				classWorld = obf ? "amu" : "net/minecraft/world/World",
				classTextComponentTranslation = obf ? "hp" : "net/minecraft/util/text/TextComponentTranslation",
				classSleepResult = obf ? "aed$a" : "net/minecraft/entity/player/EntityPlayer$SleepResult",
				classBlockPos = obf ? "et" : "net/minecraft/util/math/BlockPos",
				classEntityPlayerMP = obf ? "oq" : "net/minecraft/entity/player/EntityPlayerMP",
				descSleep = obf ? "(Let;)Laed$a;" : "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/entity/player/EntityPlayer$SleepResult;",
				descWorld = obf ? "Lamu;" : "Lnet/minecraft/world/World;",
				descSleepResult = obf ? "Laed$a;" : "Lnet/minecraft/entity/player/EntityPlayer$SleepResult;",
				descSendStatusMessage = obf ? "(Lhh;Z)V" : "(Lnet/minecraft/util/text/ITextComponent;Z)V",
				fieldWorld = obf ? "l" : "world",
				fieldIsRemote = obf ? "G" : "isRemote",
				fieldOtherProblem = obf ? "e" : "OTHER_PROBLEM";

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

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
						insnList3.add(new FieldInsnNode(GETSTATIC, "com/kingrunes/somnia/common/CommonProxy", "ignoreMonsters", "Z"));
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
						insnList4.add(new FieldInsnNode(GETSTATIC, "com/kingrunes/somnia/common/CommonProxy", "sleepWithArmor", "Z"));
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

						//Send GuiOpen packet
						InsnList insnList5 = new InsnList();
						LabelNode label425 = new LabelNode();
						JumpInsnNode jump = (JumpInsnNode)m.instructions.get(365);
						LabelNode label43 = jump.label;
						jump.label = label425;
						insnList5.add(label425);
						insnList5.add(new VarInsnNode(ALOAD, 0));
						insnList5.add(new FieldInsnNode(GETFIELD, classEntityPlayer, fieldWorld, descWorld));
						insnList5.add(new FieldInsnNode(GETFIELD, classWorld, fieldIsRemote, "Z"));
						insnList5.add(new JumpInsnNode(IFNE, label43));
						LabelNode label426 = new LabelNode();
						insnList5.add(label426);
						insnList5.add(new FieldInsnNode(GETSTATIC, "com/kingrunes/somnia/Somnia", "eventChannel", "Lnet/minecraftforge/fml/common/network/FMLEventChannel;"));
						insnList5.add(new MethodInsnNode(INVOKESTATIC, "com/kingrunes/somnia/common/PacketHandler", "buildGUIOpenPacket", "()Lnet/minecraftforge/fml/common/network/internal/FMLProxyPacket;", false));
						insnList5.add(new VarInsnNode(ALOAD, 0));
						insnList5.add(new TypeInsnNode(CHECKCAST, classEntityPlayerMP));
						insnList5.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraftforge/fml/common/network/FMLEventChannel", "sendTo", "(Lnet/minecraftforge/fml/common/network/internal/FMLProxyPacket;Lnet/minecraft/entity/player/EntityPlayerMP;)V", false));
						m.instructions.insert(m.instructions.get(370), insnList5);
					}
				}
				for (AbstractInsnNode insn : m.instructions.toArray()) System.out.println("ins: " + insn + "   " + m.instructions.indexOf(insn) + "   " + (insn instanceof LineNumberNode ? ((LineNumberNode) insn).line : ""));

				break;
			}
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);
		System.out.println("[Somnia Core] Successfully patched EntityPlayer");
		return cw.toByteArray();
	}

	private byte[] patchEntityRenderer(byte[] bytes, boolean obf)
	{
		String methodName = obf ? "a" : "updateCameraAndRender";
		String methodName2 = obf ? "b" : "renderWorld";
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

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

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);
		System.out.println("[Somnia Core] Successfully patched EntityRenderer");
		return cw.toByteArray();
	}


	private byte[] patchWorldServer(byte[] bytes, boolean obf)
	{
		String 	methodTick = obf ? "d" : "tick",
				methodGetGameRule = obf ? "b" : "getBoolean";

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

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

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);
		System.out.println("[Somnia Core] Successfully patched WorldServer");
		return cw.toByteArray();
	}

	private byte[] patchChunk(byte[] bytes, boolean obf)
	{
		String methodName = obf ? "b" : "onTick";
		String methodName2 = obf ? "o" : "checkLight";

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

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

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);
		System.out.println("[Somnia Core] Successfully patched Chunk");
		return cw.toByteArray();
	}

	private byte[] patchMinecraftServer(byte[] bytes)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

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

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);
		System.out.println("[Somnia Core] Successfully patched MinecraftServer");
		return cw.toByteArray();
	}

	private byte[] patchItemClock(byte[] bytes) {
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC, "onItemUseFirst", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;FFFLnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;", null, null);
		methodNode.visitCode();
		methodNode.visitVarInsn(ALOAD, 1);
		methodNode.visitVarInsn(ALOAD, 2);
		methodNode.visitVarInsn(ALOAD, 3);
		methodNode.visitVarInsn(ALOAD, 4);
		methodNode.visitVarInsn(FLOAD, 5);
		methodNode.visitVarInsn(FLOAD, 6);
		methodNode.visitVarInsn(FLOAD, 7);
		methodNode.visitVarInsn(ALOAD, 8);
		methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, "com/kingrunes/somnia/Somnia", "onItemUseFirst", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;FFFLnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;", false);
		methodNode.visitInsn(Opcodes.ARETURN);
		methodNode.visitEnd();

		classNode.methods.add(methodNode);

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);
		System.out.println("[Somnia Core] Successfully patched ItemClock");
		return cw.toByteArray();
	}
}