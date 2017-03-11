package org.zenframework.easyservices.cls;

import java.util.Collection;

import org.zenframework.easyservices.cls.ClassInfo;
import org.zenframework.easyservices.cls.ClassRef;

import junit.framework.TestCase;

public class ClassInfoTest extends TestCase {

    public void testClassInfo() throws Exception {
        ClassRef classRef = ClassInfo.getClassRef(E.class);
        assertEquals(E.class.getSimpleName(), classRef.getName());
        System.out.println(classRef.getClassInfo());
        Collection<ClassInfo> dependencies = classRef.getClassInfo().getDependencies(true);
        for (ClassInfo dep : dependencies)
            System.out.println(dep);
    }

}
