package org.evomaster.client.java.instrumentation.coverage;

import org.evomaster.client.java.instrumentation.shared.ClassName;
import org.evomaster.client.java.instrumentation.Constants;
import org.evomaster.client.java.instrumentation.shared.ObjectiveNaming;
import org.evomaster.client.java.instrumentation.coverage.methodreplacement.MethodReplacementClass;
import org.evomaster.client.java.instrumentation.coverage.methodreplacement.Replacement;
import org.evomaster.client.java.instrumentation.coverage.methodreplacement.ReplacementList;
import org.evomaster.client.java.instrumentation.staticstate.ObjectiveRecorder;
import org.evomaster.client.java.utils.SimpleLogger;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MethodReplacementMethodVisitor extends MethodVisitor {

    private final String className;
    private final String methodName;

    private int currentLine;
    private int currentIndex;

    public MethodReplacementMethodVisitor(MethodVisitor mv,
                                          String className,
                                          String methodName,
                                          String descriptor) {
        super(Constants.ASM, mv);

        this.className = className;
        this.methodName = methodName;
        currentLine = 0;
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);

        currentLine = line;
        currentIndex = 0; //reset it for current line
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
                                String desc, boolean itf) {

        //don't instrument static initializers
        if (methodName.equals(Constants.CLASS_INIT_METHOD)) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }

        /*
            Loading class here could have side-effects.
            However, method replacements is only done for JDK APIs, which
            anyway should be loaded with boot-classloader.
            so, loading them here "hopefully" should be OK...

            TODO: in future, might also target methods in Kotlin API
         */
        if (!owner.startsWith("java/")) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }

        Class<?> klass = null;
        try {
            klass = this.getClass().getClassLoader().loadClass(ClassName.get(owner).getFullNameWithDots());
        } catch (ClassNotFoundException e) {
            //shouldn't really happen
            SimpleLogger.error(e.toString());
            throw new RuntimeException(e);
        }

        List<MethodReplacementClass> candidateClasses = ReplacementList.getReplacements(klass);

        if (candidateClasses.isEmpty()) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }

        Optional<Method> r = candidateClasses.stream()
                .flatMap(i -> Stream.of(i.getClass().getDeclaredMethods()))
                .filter(m -> m.getDeclaredAnnotation(Replacement.class) != null)
                .filter(m -> m.getName().equals(name))
                .filter(m -> {
                    Replacement br = m.getAnnotation(Replacement.class);
                    return (br.replacingStatic() && desc.equals(getDescriptorSkippingLast(m, 0))) ||
                            (!br.replacingStatic() && desc.equals(getDescriptorSkippingLast(m, 1)));
                })
                .findAny();

        if (!r.isPresent()) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            return;
        }

        Method m = r.get();
        replaceMethod(m);
    }

    private void replaceMethod(Method m) {

        Replacement br = m.getAnnotation(Replacement.class);

                     /*
                    In the case of replacing a non-static method a.foo(x,y),
                    we will need a replacement bar(a,x,y,id)

                    So, the stack
                    a
                    x
                    y
                    foo # non-static

                    will be replaced by
                    a
                    x
                    y
                    id
                    bar # static

                    This means we do not need to handle "a", but still need to create
                    "id" and replace "foo" with "bar".
                 */
        String idTemplate = ObjectiveNaming.methodReplacementObjectiveNameTemplate(
                className, currentLine, currentIndex
        );

        currentIndex++;

        this.visitLdcInsn(idTemplate);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(m.getDeclaringClass()),
                m.getName(),
                Type.getMethodDescriptor(m),
                false);

        String idTrue = ObjectiveNaming.methodReplacementObjectiveName(idTemplate, true, br.type());
        String idFalse = ObjectiveNaming.methodReplacementObjectiveName(idTemplate, false, br.type());
        ObjectiveRecorder.registerTarget(idTrue);
        ObjectiveRecorder.registerTarget(idFalse);
    }


    private static String getDescriptorSkippingLast(Method m, int skipFirsts) {
        Class<?>[] parameters = m.getParameterTypes();
        StringBuilder buf = new StringBuilder();
        buf.append('(');

        //skipping first parameter(s)
        int start = skipFirsts;

        /*
            we might skip the first (if replacing non-static), and
            always skipping the last (id template)
         */
        for (int i = start; i < parameters.length - 1; ++i) {
            buf.append(Type.getDescriptor(parameters[i]));
        }
        buf.append(')');
        buf.append(Type.getDescriptor(m.getReturnType()));

        return buf.toString();
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        /*
            We pushed 1 value on stack before a method call,
            so we need to increase maxStack by at least 1
         */
        super.visitMaxs(maxStack + 1, maxLocals);
    }
}
