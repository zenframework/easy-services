package org.zenframework.easyservices.util.cls;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zenframework.easyservices.util.StringUtil;
import org.zenframework.easyservices.util.compare.ClassArrayComparator;
import org.zenframework.easyservices.util.compare.ComparableListComparator;

public class ClassInfo implements Comparable<ClassInfo>, Cloneable {

    private static final ComparableListComparator<ClassRef> PARAM_TYPES_COMPARATOR = new ComparableListComparator<ClassRef>();

    private final Class<?> describedClass;
    private final String name;
    private final boolean typeSimple;
    private final boolean typeEnum;
    private final Map<String, FieldInfo> fields = new HashMap<String, FieldInfo>();
    private final Set<MethodInfo> methods = new HashSet<MethodInfo>();
    private final Set<String> enumValues = new HashSet<String>();

    /*private ClassInfo(String name, Map<String, FieldInfo> fields, Set<MethodInfo> methods) {
        this.name = name.startsWith(JAVA_LANG_PREFIX) ? name.substring(JAVA_LANG_PREFIX.length()) : name;
        this.fields = new HashMap<String, FieldInfo>(fields);
        this.methods = new HashSet<MethodInfo>(methods);
    }*/

    private ClassInfo(Class<?> describedClass, String name, boolean typeSimple, boolean typeEnum) {
        this.describedClass = describedClass;
        this.name = name;
        this.typeSimple = typeSimple;
        this.typeEnum = typeEnum;
    }

    private void addFieldInfo(FieldInfo field) {
        fields.put(field.getName(), field);
    }

    private void addMethodInfo(MethodInfo method) {
        methods.add(method);
    }

    private void addEnumValue(String enumValue) {
        enumValues.add(enumValue);
    }

    public FieldInfo getFieldInfo(String name) {
        return fields.get(name);
    }

    public MethodInfo getMethodInfo(String name, Class<?>... argTypes) {
        for (MethodInfo method : methods)
            if (method.getName().equals(name) && PARAM_TYPES_COMPARATOR.compare(method.getParameterTypes(), getClassRefs(argTypes)) == 0)
                return method;
        return null;
    }

    public Class<?> getDescribedClass() {
        return describedClass;
    }

    public String getName() {
        return name;
    }

    public boolean isTypeSimple() {
        return typeSimple;
    }

    public boolean isTypeEnum() {
        return typeEnum;
    }

    public Map<String, FieldInfo> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    public Set<MethodInfo> getMethods() {
        return Collections.unmodifiableSet(methods);
    }

    public Set<ClassInfo> getDependencies(boolean recursive) {
        Set<ClassInfo> dependencies = new HashSet<ClassInfo>();
        fillDependencies(dependencies, recursive);
        return dependencies;
    }

    public void fillDependencies(Set<ClassInfo> dependencies, boolean recursive) {
        for (FieldInfo fieldInfo : fields.values()) {
            ClassInfo fieldClassInfo = fieldInfo.getType().getClassInfo();
            if (!fieldClassInfo.isTypeSimple() && !dependencies.contains(fieldClassInfo)) {
                dependencies.add(fieldClassInfo);
                if (recursive)
                    fieldClassInfo.fillDependencies(dependencies, true);
            }
        }
        for (MethodInfo methodInfo : methods) {
            for (ClassRef paramType : methodInfo.getParameterTypes()) {
                ClassInfo paramClassInfo = paramType.getClassInfo();
                if (!paramClassInfo.isTypeSimple() && !dependencies.contains(paramClassInfo)) {
                    dependencies.add(paramClassInfo);
                    if (recursive)
                        paramClassInfo.fillDependencies(dependencies, true);
                }
            }
            ClassInfo returnClassInfo = methodInfo.getReturnType().getClassInfo();
            if (!returnClassInfo.isTypeSimple() && !dependencies.contains(returnClassInfo)) {
                dependencies.add(returnClassInfo);
                if (recursive)
                    returnClassInfo.fillDependencies(dependencies, true);
            }
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ClassInfo))
            return false;
        return name.equals(((ClassInfo) obj).getName());
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        List<FieldInfo> fields = new ArrayList<FieldInfo>(this.fields.values());
        List<MethodInfo> methods = new ArrayList<MethodInfo>(this.methods);
        Collections.sort(fields);
        Collections.sort(methods);
        StringBuilder str = new StringBuilder();
        str.append(name).append(" {");
        for (FieldInfo field : fields)
            StringUtil.indent(str, indent + 1, true).append(field);
        for (MethodInfo method : methods)
            StringUtil.indent(str, indent + 1, true).append(method);
        if (!fields.isEmpty() || !methods.isEmpty())
            StringUtil.indent(str, indent, true);
        return str.append('}').toString();
    }

    @Override
    public int compareTo(ClassInfo o) {
        return o == null ? 1 : name.compareTo(o.getName());
    }

    @Override
    public ClassInfo clone() {
        try {
            return (ClassInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    // Static

    private static final Method[] SKIP_METHODS = Object.class.getMethods();
    private static final Map<Class<?>, ClassInfo> CLASS_INFO_CASH = Collections.synchronizedMap(getBasicClassInfos());

    public static List<ClassRef> getClassRefs(Class<?>... classes) {
        List<ClassRef> classRefs = new ArrayList<ClassRef>(classes.length);
        for (Class<?> cls : classes)
            classRefs.add(getClassRef(cls));
        return classRefs;
    }

    public static Collection<ClassRef> getClassRefs(Collection<Class<?>> classes) {
        Collection<ClassRef> classRefs = new ArrayList<ClassRef>(classes.size());
        for (Class<?> cls : classes)
            classRefs.add(getClassRef(cls));
        return classRefs;
    }

    public static ClassInfo getClassInfo(Class<?> cls) {
        return getClassRef(cls).getClassInfo();
    }

    public static List<ClassInfo> getClassInfos(Class<?>... classes) {
        List<ClassInfo> classRefs = new ArrayList<ClassInfo>(classes.length);
        for (Class<?> cls : classes)
            classRefs.add(getClassInfo(cls));
        return classRefs;
    }

    public static Collection<ClassInfo> getClassInfos(Collection<Class<?>> classes) {
        List<ClassInfo> classRefs = new ArrayList<ClassInfo>(classes.size());
        for (Class<?> cls : classes)
            classRefs.add(getClassInfo(cls));
        return classRefs;
    }

    /*public static Set<Class<?>> getDependencies(Class<?>... classes) {
        Set<Class<?>> dependencies = new HashSet<Class<?>>();
        for (Class<?> cls : classes)
            dependencies.addAll(getDependencies(cls));
        return dependencies;
    }
    
    public static Set<Class<?>> getDependencies(Collection<Class<?>> classes) {
        Set<Class<?>> dependencies = new HashSet<Class<?>>();
        for (Class<?> cls : classes)
            dependencies.addAll(getDependencies(cls));
        return dependencies;
    }*/

    @SuppressWarnings("unchecked")
    public static ClassRef getClassRef(Class<?> cls) {
        synchronized (CLASS_INFO_CASH) {
            // Extract array component type
            int arrayDimension = 0;
            for (; cls.isArray(); cls = cls.getComponentType())
                arrayDimension++;
            ClassInfo classInfo = CLASS_INFO_CASH.get(cls);
            if (classInfo == null) {
                classInfo = new ClassInfo(cls, cls.getSimpleName(), false, cls.isEnum());
                CLASS_INFO_CASH.put(cls, classInfo);
                if (cls.isEnum()) {
                    Enum<?>[] values = ((Class<Enum<?>>) cls).getEnumConstants();
                    for (Enum<?> value : values)
                        classInfo.addEnumValue(value.name());
                } else {
                    // Inspect fields
                    for (Field field : cls.getFields())
                        if (!Modifier.isStatic(field.getModifiers())) {
                            ClassRef fieldClassRef = getClassRef(field.getType());
                            classInfo.addFieldInfo(new FieldInfo(field.getName(), fieldClassRef, true, true));
                        }
                    // Inspect methods
                    for (Method method : cls.getMethods()) {
                        if (!skipMethod(method)) {
                            FieldInfo fieldInfo1 = extractFieldFromGetterOrSetter(method);
                            if (fieldInfo1 != null) {
                                FieldInfo fieldInfo2 = classInfo.getFieldInfo(fieldInfo1.getName());
                                if (fieldInfo2 == null) {
                                    classInfo.addFieldInfo(fieldInfo1);
                                } else if (!fieldInfo2.isReadable()) {
                                    // fieldInfo1 is getter, fieldInfo2 is setter
                                    // TODO compare types
                                    classInfo.addFieldInfo(new FieldInfo(fieldInfo1.getName(), fieldInfo1.getType(), true, true));
                                } else if (!fieldInfo2.isWritable()) {
                                    // fieldInfo1 is setter, fieldInfo2 is getter
                                    // TODO compare types
                                    classInfo.addFieldInfo(new FieldInfo(fieldInfo1.getName(), fieldInfo1.getType(), true, true));
                                }
                            } else {
                                Class<?>[] paramTypes = method.getParameterTypes();
                                List<ClassRef> paramClassRefs = new ArrayList<ClassRef>(paramTypes.length);
                                for (Class<?> paramType : paramTypes)
                                    paramClassRefs.add(getClassRef(paramType));
                                ClassRef returnClassRef = getClassRef(method.getReturnType());
                                classInfo.addMethodInfo(new MethodInfo(method.getName(), paramClassRefs, returnClassRef));
                            }
                        }
                    }
                }
            }
            return new ClassRef(classInfo, arrayDimension);
        }
    }

    /**
     * Checks if method is getter or setter
     * @param cls
     * @param method
     * @return FieldInfo
     */
    private static FieldInfo extractFieldFromGetterOrSetter(Method method) {
        String name = method.getName();
        if (name.length() < 4)
            return null;
        String firstLetter = name.substring(3, 4);
        // check if the 4th letter is in upper case
        if (!firstLetter.toUpperCase().equals(firstLetter))
            return null;
        FieldInfo field = null;
        if (method.getParameterTypes().length == 0 && method.getReturnType() != void.class && name.startsWith("get")) {
            // if method name begins with 'get', has no args and returns non-void, this is getter
            field = new FieldInfo(firstLetter.toLowerCase() + name.substring(4), getClassRef(method.getReturnType()), true, false);
            // if method name begins with 'set', has one arg and returns void, this is setter
        } else if (method.getParameterTypes().length == 1 && method.getReturnType() == void.class && name.startsWith("set")) {
            field = new FieldInfo(firstLetter.toLowerCase() + name.substring(4), getClassRef(method.getParameterTypes()[0]), false, true);
        }
        return field;
    }

    /*private static String getSimpleClassName(Class<?> cls) {
        if (cls.isPrimitive())
            return cls.getName();
        String mapped = SIMPLE_MAPPER.get(cls);
        if (mapped != null)
            return mapped;
        for (Entry<Class<?>, String> interfaceMapping : INTERFACE_MAPPER.entrySet())
            if (interfaceMapping.getKey().isAssignableFrom(cls))
                return interfaceMapping.getValue();
        return null;
    }*/

    private static boolean skipMethod(Method method) {
        if (Modifier.isStatic(method.getModifiers()))
            return true;
        for (Method m : SKIP_METHODS)
            if (m.getName().equals(method.getName()) && ClassArrayComparator.INSTANCE.compare(m.getParameterTypes(), method.getParameterTypes()) == 0)
                return true;
        return false;
    }

    private static Map<Class<?>, ClassInfo> getBasicClassInfos() {
        Map<Class<?>, ClassInfo> classInfos = new HashMap<Class<?>, ClassInfo>();
        classInfos.put(void.class, new ClassInfo(void.class, "void", true, false));
        classInfos.put(byte.class, new ClassInfo(Object.class, "Number", true, false));
        classInfos.put(char.class, new ClassInfo(Object.class, "String", true, false));
        classInfos.put(short.class, new ClassInfo(Object.class, "Number", true, false));
        classInfos.put(int.class, new ClassInfo(Object.class, "Number", true, false));
        classInfos.put(long.class, new ClassInfo(Object.class, "Number", true, false));
        classInfos.put(boolean.class, new ClassInfo(Object.class, "Boolean", true, false));
        classInfos.put(float.class, new ClassInfo(Object.class, "Number", true, false));
        classInfos.put(double.class, new ClassInfo(Object.class, "Number", true, false));
        classInfos.put(Byte.class, new ClassInfo(Object.class, "Number", true, false));
        classInfos.put(Character.class, new ClassInfo(Object.class, "String", true, false));
        classInfos.put(Short.class, new ClassInfo(Object.class, "Number", true, false));
        classInfos.put(Integer.class, new ClassInfo(Object.class, "Number", true, false));
        classInfos.put(Long.class, new ClassInfo(Object.class, "Number", true, false));
        classInfos.put(Boolean.class, new ClassInfo(Object.class, "Boolean", true, false));
        classInfos.put(Float.class, new ClassInfo(Object.class, "Number", true, false));
        classInfos.put(Double.class, new ClassInfo(Object.class, "Number", true, false));
        classInfos.put(Object.class, new ClassInfo(Object.class, "Object", true, false));
        classInfos.put(String.class, new ClassInfo(String.class, "String", true, false));
        classInfos.put(Map.class, new ClassInfo(Map.class, "Object", true, false));
        classInfos.put(Collection.class, new ClassInfo(Collection.class, "Array", true, false));
        return classInfos;
    }

}
