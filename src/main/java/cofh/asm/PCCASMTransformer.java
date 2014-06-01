package cofh.asm;

import static org.objectweb.asm.Opcodes.*;

import cofh.asm.relauncher.Implementable;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.common.asm.transformers.deobf.FMLRemappingAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class PCCASMTransformer implements IClassTransformer {

	private String desc;
	private ArrayList<String> workingPath = new ArrayList<String>();
	private ClassNode world = null, worldServer = null;

	public PCCASMTransformer() {

		desc = Type.getDescriptor(Implementable.class);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {

		if (bytes == null) {
			return null;
		}
		ClassReader cr = new ClassReader(bytes);
		ClassNode cn = new ClassNode();
		cr.accept(cn, 0);

		workingPath.add(transformedName);

		if (this.implement(cn)) {
			System.out.println("Adding runtime interfaces to " + transformedName);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			cn.accept(cw);
			bytes = cw.toByteArray();
			cr = new ClassReader(bytes);
		}

		workingPath.remove(workingPath.size() - 1);

		if ("net.minecraft.world.WorldServer".equals(transformedName)) {
			bytes = writeWorldServer(name, transformedName, bytes, cr);
		} else if ("net.minecraft.world.World".equals(transformedName)) {
			bytes = writeWorld(name, transformedName, bytes, cr);
		} else if ("skyboy.core.world.WorldProxy".equals(transformedName)) {
			bytes = writeWorldProxy(name, bytes, cr);
		} else if ("skyboy.core.world.WorldServerProxy".equals(transformedName)) {
			bytes = writeWorldServerProxy(name, bytes, cr);
		}

		return bytes;
	}

	private byte[] writeWorld(String name, String transformedName, byte[] bytes, ClassReader cr) {

		String[] names = null;
		if (LoadingPlugin.runtimeDeobfEnabled) {
			names = new String[] { "field_73019_z", "field_72986_A", "field_73011_w", "field_72984_F" };
		} else {
			names = new String[] { "saveHandler", "worldInfo", "provider", "theProfiler" };
		}
		name = name.replace('.', '/');
		ClassNode cn = new ClassNode(ASM4);
		cr.accept(cn, ClassReader.EXPAND_FRAMES);
		String sig = "(Lnet/minecraft/world/storage/ISaveHandler;Ljava/lang/String;Lnet/minecraft/world/WorldProvider;Lnet/minecraft/world/WorldSettings;Lnet/minecraft/profiler/Profiler;)V";
		FMLDeobfuscatingRemapper remapper = FMLDeobfuscatingRemapper.INSTANCE;
		String sigObf = "(" + "L" + remapper.unmap("net/minecraft/world/storage/ISaveHandler") + ";" + "Ljava/lang/String;" + "L"
				+ remapper.unmap("net/minecraft/world/WorldProvider") + ";" + "L" + remapper.unmap("net/minecraft/world/WorldSettings") + ";" + "L"
				+ remapper.unmap("net/minecraft/profiler/Profiler") + ";" + ")V";

		l: {
			for (MethodNode m : cn.methods) {
				if ("<init>".equals(m.name) && (sig.equals(m.desc) || sigObf.equals(m.desc))) {
					break l;
				}
			}

			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
			cn.accept(cw);
			/*
			 * new World constructor World(ISaveHandler saveHandler, String worldName, WorldProvider provider, WorldSettings worldSettings, Profiler
			 * theProfiler)
			 */
			cw.newMethod(name, "<init>", sig, true);
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", sig, null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(DUP);
			mv.visitInsn(DUP);
			mv.visitInsn(DUP);
			mv.visitInsn(DUP);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitFieldInsn(PUTFIELD, name, names[0], "Lnet/minecraft/world/storage/ISaveHandler;");
			mv.visitTypeInsn(NEW, "net/minecraft/world/storage/WorldInfo");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKESPECIAL, "net/minecraft/world/storage/WorldInfo", "<init>", "(Lnet/minecraft/world/WorldSettings;Ljava/lang/String;)V");
			mv.visitFieldInsn(PUTFIELD, name, names[1], "Lnet/minecraft/world/storage/WorldInfo;");
			mv.visitVarInsn(ALOAD, 3);
			mv.visitFieldInsn(PUTFIELD, name, names[2], "Lnet/minecraft/world/WorldProvider;");
			mv.visitVarInsn(ALOAD, 5);
			mv.visitFieldInsn(PUTFIELD, name, names[3], "Lnet/minecraft/profiler/Profiler;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(11, 10);
			mv.visitEnd();
			cw.visitEnd();
			bytes = cw.toByteArray();
		}
		{
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			RemappingClassAdapter remapAdapter = new FMLRemappingAdapter(classWriter);
			cr = new ClassReader(bytes);
			cr.accept(remapAdapter, ClassReader.EXPAND_FRAMES);
			cn = new ClassNode(ASM4);
			cr = new ClassReader(classWriter.toByteArray());
			cr.accept(cn, ClassReader.EXPAND_FRAMES);
			world = cn;
		}
		return bytes;
	}

	private byte[] writeWorldServer(String name, String transformedName, byte[] bytes, ClassReader cr) {

		String[] names = null;
		if (LoadingPlugin.runtimeDeobfEnabled) {
			names = new String[] { "field_73061_a", "field_73062_L", "field_73063_M", "field_85177_Q" };
		} else {
			names = new String[] { "mcServer", "theEntityTracker", "thePlayerManager", "worldTeleporter" };
		}
		name = name.replace('.', '/');
		ClassNode cn = new ClassNode(ASM4);
		cr.accept(cn, ClassReader.EXPAND_FRAMES);
		String sig = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/world/storage/ISaveHandler;Ljava/lang/String;Lnet/minecraft/world/WorldProvider;Lnet/minecraft/world/WorldSettings;Lnet/minecraft/profiler/Profiler;)V";
		FMLDeobfuscatingRemapper remapper = FMLDeobfuscatingRemapper.INSTANCE;
		String sigObf = "(" + "L" + remapper.unmap("net/minecraft/server/MinecraftServer") + ";" + "L"
				+ remapper.unmap("net/minecraft/world/storage/ISaveHandler") + ";" + "Ljava/lang/String;" + "L"
				+ remapper.unmap("net/minecraft/world/WorldProvider") + ";" + "L" + remapper.unmap("net/minecraft/world/WorldSettings") + ";" + "L"
				+ remapper.unmap("net/minecraft/profiler/Profiler") + ";" + ")V";

		l: {
			for (MethodNode m : cn.methods) {
				if ("<init>".equals(m.name) && (sig.equals(m.desc) || sigObf.equals(m.desc))) {
					break l;
				}
			}

			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
			cn.accept(cw);
			/*
			 * new WorldServer constructor WorldServer(MinecraftServer minecraftServer, ISaveHandler saveHandler, String worldName, WorldProvider provider,
			 * WorldSettings worldSettings, Profiler theProfiler)
			 */
			cw.newMethod(name, "<init>", sig, true);
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", sig, null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(DUP);
			mv.visitInsn(DUP);
			mv.visitInsn(DUP);
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitVarInsn(ALOAD, 5);
			mv.visitVarInsn(ALOAD, 6);
			// [World] super(saveHandler, worldName, provider, worldSettings, theProfiler);
			mv.visitMethodInsn(
					INVOKESPECIAL,
					"net/minecraft/world/World",
					"<init>",
					"(Lnet/minecraft/world/storage/ISaveHandler;Ljava/lang/String;Lnet/minecraft/world/WorldProvider;Lnet/minecraft/world/WorldSettings;Lnet/minecraft/profiler/Profiler;)V");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitFieldInsn(PUTFIELD, name, names[0], "Lnet/minecraft/server/MinecraftServer;");
			mv.visitInsn(ACONST_NULL);
			mv.visitFieldInsn(PUTFIELD, name, names[1], "Lnet/minecraft/entity/EntityTracker;");
			mv.visitInsn(ACONST_NULL);
			mv.visitFieldInsn(PUTFIELD, name, names[2], "Lnet/minecraft/server/management/PlayerManager;");
			mv.visitInsn(ACONST_NULL);
			mv.visitFieldInsn(PUTFIELD, name, names[3], "Lnet/minecraft/world/Teleporter;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(11, 10);
			mv.visitEnd();
			cw.visitEnd();
			bytes = cw.toByteArray();
		}
		{
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			RemappingClassAdapter remapAdapter = new FMLRemappingAdapter(classWriter);
			cr = new ClassReader(bytes);
			cr.accept(remapAdapter, ClassReader.EXPAND_FRAMES);
			cn = new ClassNode(ASM4);
			cr = new ClassReader(classWriter.toByteArray());
			cr.accept(cn, ClassReader.EXPAND_FRAMES);
			worldServer = cn;
		}
		return bytes;
	}

	private byte[] writeWorldProxy(String name, byte[] bytes, ClassReader cr) {

		if (world == null) {
			try {
				Class.forName("net.minecraft.world.World");
			} catch (Throwable _) { /* Won't happen */
			}
		}

		ClassNode cn = new ClassNode(ASM4);
		cr.accept(cn, ClassReader.EXPAND_FRAMES);

		for (MethodNode m : world.methods) {
			if (m.name.indexOf('<') != 0 && (m.access & ACC_STATIC) == 0) {
				{
					Iterator<MethodNode> i = cn.methods.iterator();
					while (i.hasNext()) {
						MethodNode m2 = i.next();
						if (m2.name.equals(m.name) && m2.desc.equals(m.desc)) {
							i.remove();
						}
					}
				}
				MethodVisitor mv = cn.visitMethod(getAccess(m), m.name, m.desc, m.signature, m.exceptions.toArray(new String[0]));
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, "skyboy/core/world/WorldProxy", "proxiedWorld", "Lnet/minecraft/world/World;");
				Type[] types = Type.getArgumentTypes(m.desc);
				for (int i = 0, w = 1, e = types.length; i < e; ++i) {
					mv.visitVarInsn(types[i].getOpcode(ILOAD), w);
					w += types[i].getSize();
				}
				mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/World", m.name, m.desc);
				mv.visitInsn(Type.getReturnType(m.desc).getOpcode(IRETURN));
				mv.visitMaxs(1, 1);
				mv.visitEnd();
			}
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cn.accept(cw);
		return cw.toByteArray();
	}

	private byte[] writeWorldServerProxy(String name, byte[] bytes, ClassReader cr) {

		if (worldServer == null) {
			try {
				Class.forName("net.minecraft.world.WorldServer");
			} catch (Throwable _) { /* Won't happen */
			}
		}
		if (world == null) {
			try {
				Class.forName("net.minecraft.world.World");
			} catch (Throwable _) { /* Won't happen */
			}
		}

		ClassNode cn = new ClassNode(ASM4);
		cr.accept(cn, ClassReader.EXPAND_FRAMES);

		cn.superName = "net/minecraft/world/WorldServer";
		for (MethodNode m : cn.methods) {
			if ("<init>".equals(m.name)) {
				InsnList l = m.instructions;
				for (int i = 0, e = l.size(); i < e; ++i) {
					AbstractInsnNode n = l.get(i);
					if (n instanceof MethodInsnNode) {
						MethodInsnNode mn = (MethodInsnNode) n;
						if (mn.getOpcode() == INVOKESPECIAL) {
							mn.owner = cn.superName;
							break;
						}
					}
				}
				break;
			}
		}

		for (MethodNode m : world.methods) {
			if (m.name.indexOf('<') != 0 && (m.access & ACC_STATIC) == 0) {
				{
					Iterator<MethodNode> i = cn.methods.iterator();
					while (i.hasNext()) {
						MethodNode m2 = i.next();
						if (m2.name.equals(m.name) && m2.desc.equals(m.desc)) {
							i.remove();
						}
					}
				}
				MethodVisitor mv = cn.visitMethod(getAccess(m), m.name, m.desc, m.signature, m.exceptions.toArray(new String[0]));
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, "skyboy/core/world/WorldServerProxy", "proxiedWorld", "Lnet/minecraft/world/WorldServer;");
				Type[] types = Type.getArgumentTypes(m.desc);
				for (int i = 0, w = 1, e = types.length; i < e; ++i) {
					mv.visitVarInsn(types[i].getOpcode(ILOAD), w);
					w += types[i].getSize();
				}
				mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/World", m.name, m.desc);
				mv.visitInsn(Type.getReturnType(m.desc).getOpcode(IRETURN));
				mv.visitMaxs(1, 1);
				mv.visitEnd();
			}
		}

		for (MethodNode m : worldServer.methods) {
			if (m.name.indexOf('<') != 0 && (m.access & ACC_STATIC) == 0) {
				{
					Iterator<MethodNode> i = cn.methods.iterator();
					while (i.hasNext()) {
						MethodNode m2 = i.next();
						if (m2.name.equals(m.name) && m2.desc.equals(m.desc)) {
							i.remove();
						}
					}
				}
				MethodVisitor mv = cn.visitMethod(getAccess(m), m.name, m.desc, m.signature, m.exceptions.toArray(new String[0]));
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, "skyboy/core/world/WorldServerProxy", "proxiedWorld", "Lnet/minecraft/world/WorldServer;");
				Type[] types = Type.getArgumentTypes(m.desc);
				for (int i = 0, w = 1, e = types.length; i < e; ++i) {
					mv.visitVarInsn(types[i].getOpcode(ILOAD), w);
					w += types[i].getSize();
				}
				mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/WorldServer", m.name, m.desc);
				mv.visitInsn(Type.getReturnType(m.desc).getOpcode(IRETURN));
				mv.visitMaxs(1, 1);
				mv.visitEnd();
			}
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cn.accept(cw);
		bytes = cw.toByteArray();
		return bytes;
	}

	private boolean implement(ClassNode cn) {

		if (cn.visibleAnnotations == null) {
			return false;
		}
		boolean interfaces = false;
		for (AnnotationNode node : cn.visibleAnnotations) {
			if (node.desc.equals(desc)) {
				if (node.values != null) {
					List<Object> values = node.values;
					for (int i = 0, e = values.size(); i < e;) {
						Object k = values.get(i++);
						Object v = values.get(i++);
						if (k instanceof String && k.equals("value") && v instanceof String[]) {
							String[] value = (String[]) v;
							for (int j = 0, l = value.length; j < l; ++j) {
								String clazz = value[j].trim();
								String cz = clazz.replace('.', '/');
								if (!cn.interfaces.contains(cz)) {
									try {
										if (!workingPath.contains(clazz)) {
											Class.forName(clazz, false, this.getClass().getClassLoader());
										}
										cn.interfaces.add(cz);
										interfaces = true;
									} catch (Throwable _) {
									}
								}
							}
						}
					}
				}
			}
		}
		return interfaces;
	}

	private static int getAccess(MethodNode m) {

		int r = m.access;
		r &= ~(ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED | ACC_FINAL | ACC_BRIDGE | ACC_ABSTRACT);
		r |= ACC_PUBLIC | ACC_SYNTHETIC;
		return r;
	}

}
