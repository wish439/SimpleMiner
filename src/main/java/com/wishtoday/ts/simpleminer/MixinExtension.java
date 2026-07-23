package com.wishtoday.ts.simpleminer;

import com.wishtoday.simpleservices.services.ServiceFieldType;
import com.wishtoday.simpleservices.services.annotation.ServiceClass;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.Bytecode;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MixinExtension implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass
            , String mixinClassName, IMixinInfo mixinInfo) {
        this.inject(mixinInfo.getClassNode(0), targetClass);
    }

    private void inject(ClassNode classNode, ClassNode targetClassNode) {
        //System.out.println("triggered inject1" + classNode.name);
        if (classNode.visibleAnnotations == null || classNode.visibleAnnotations.isEmpty()) {
            return;
        }
        boolean hasAnnotation = false;
        ServiceFieldType type = ServiceFieldType.CLASS;
        for (AnnotationNode annotation : classNode.visibleAnnotations) {
            String descriptor = Type.getDescriptor(ServiceClass.class);
            if (annotation.desc.equals(descriptor)) {
                hasAnnotation = true;
                ServiceFieldType value = getValue(annotation, "value", ServiceFieldType.class);
                if (value != null) type = value;
            }
        }
        if (!hasAnnotation) {
            return;
        }
        //System.out.println("triggered inject" + classNode.name);


        if (type == ServiceFieldType.CLASS) {
            for (MethodNode method : targetClassNode.methods) {
                if (!method.name.equals("<init>")) continue;
                AbstractInsnNode superNode = this.searchSuper(method, targetClassNode);
                if (superNode == null) {
                    return;
                }
                InsnList insnList = new InsnList();

                insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));//this
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/wishtoday/simpleservices/services/ServiceInjector", "inject", "(Ljava/lang/Object;)V", false));
                //method.instructions.insertBefore(returnNode, insnList);
                method.instructions.insert(superNode, insnList);

                //method.maxStack++;
                //method.maxLocals++;

                /*for (AbstractInsnNode insn : method.instructions) {
                    if (insn instanceof FrameNode) {
                        method.instructions.remove(insn);
                    }
                }
                method.maxStack = 0;
                method.maxLocals = 0;*/

                return;
            }
            return;
        }


        Optional<MethodNode> first = targetClassNode.methods.stream().filter(method -> method.name.equals("<clinit>")).findFirst();
        MethodNode mn;
        if (first.isPresent()) {
            mn = first.get();
        } else {
            mn = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            targetClassNode.methods.add(mn);
        }
        InsnList insnList = new InsnList();
        insnList.add(new LdcInsnNode(Type.getObjectType(targetClassNode.name)));
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/wishtoday/simpleservices/services/ServiceInjector", "injectStatic", "(Ljava/lang/Class;)V", false));
        if (mn.instructions.size() == 0) {
            insnList.add(new InsnNode(Opcodes.RETURN));
            mn.instructions.insert(insnList);
            return;
        }
        mn.instructions.insert(mn.instructions.getFirst(), insnList);
    }

    private AbstractInsnNode searchReturn(MethodNode method) {
        AbstractInsnNode lastReturn = null;
        for (AbstractInsnNode node : method.instructions) {
            if (node.getOpcode() == Opcodes.RETURN) {
                lastReturn = node;
            }
        }
        return lastReturn;
    }

    private AbstractInsnNode searchSuper(MethodNode method, ClassNode classNode) {
        Bytecode.DelegateInitialiser init = Bytecode.findDelegateInit(method, classNode.superName != null ? classNode.superName : "java/lang/Object", classNode.name);
        if (init.isPresent) {
            return init.insn;
        }
        return null;
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    private static <T extends Enum<T>> T getValue(AnnotationNode annotation, String key, Class<T> enumClass) {
        boolean getNextValue = false;

        if (annotation == null || annotation.values == null) {
            return null;
        }

        for (Object value : annotation.values) {
            if (getNextValue) {
                String[] strings = (String[]) value;
                String s;
                if (strings[0].equals(Type.getDescriptor(enumClass))) {
                    s = strings[1];
                } else {
                    getNextValue = false;
                    continue;
                }
                return Enum.valueOf(enumClass, s);
            }
            if (value.equals(key)) {
                getNextValue = true;
            }
        }

        return null;
    }
}
