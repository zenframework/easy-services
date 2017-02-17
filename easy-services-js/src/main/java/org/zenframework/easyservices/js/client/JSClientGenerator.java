package org.zenframework.easyservices.js.client;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.zenframework.easyservices.util.cls.ClassInfo;
import org.zenframework.easyservices.util.cls.ClassRef;
import org.zenframework.easyservices.util.cls.FieldInfo;
import org.zenframework.easyservices.util.cls.MethodInfo;

public class JSClientGenerator {

    public void generateJSClient(PrintStream out, Class<?>... serviceClasses) {
        Set<ClassInfo> dependencies = new HashSet<ClassInfo>();
        Collection<ClassInfo> classInfos = ClassInfo.getClassInfos(serviceClasses);
        dependencies.addAll(classInfos);
        for (ClassInfo classInfo : classInfos)
            classInfo.fillDependencies(dependencies, true);
        for (ClassInfo classInfo : dependencies)
            out.println("var " + classInfo.getName() + " = new Model({});");
        out.println("\n");
        for (ClassInfo classInfo : dependencies) {
            for (FieldInfo fieldInfo : classInfo.getFields().values())
                out.println(classInfo.getName() + ".definition." + fieldInfo.getName() + " = " + fieldInfo.getName() + ";");
            for (MethodInfo methodInfo : classInfo.getMethods()) {
                out.print(classInfo.getName() + ".definition." + methodInfo.getName() + " = Model.Function(");
                List<ClassRef> paramTypes = methodInfo.getParameterTypes();
                for (int i = 0; i < paramTypes.size(); i++) {
                    out.print(paramTypes.get(i).getName());
                    if (i < paramTypes.size() - 1)
                        out.print(", ");
                }
                out.println(").return(" + methodInfo.getReturnType().getName() + ");");
            }
        }
        /*out.println("var Proxy = require('generic/api/Proxy');");
        out.println("var UUID = require('generic/model/UUID');");
        out.println();
        out.println("var Session = new Model({");
        out.println("    id : String,");
        out.println("    loggedIn : Date,");
        out.println("    lastAccess : Date,");
        out.println("    user : User");
        out.println("});");
        out.println();
        out.println("module.exports = Session;");
        out.println();
        out.println("var Profile = new Model({");
        out.println("    name : [ String ],");
        out.println("    avatar : [ String ]");
        out.println("});");
        out.println();
        out.println("var User = new Model({");
        out.println("    id : String,");
        out.println("    active : [ Boolean ],");
        out.println("    profile : [ Profile ]");
        out.println("});");
        out.println();
        out.println("User.Array = Model.Array(User);");
        out.println("User.Profile = Profile;");
        out.println();
        out.println("module.exports = User;");
        out.println();
        out.println("var UserManager = Proxy.create({");
        out.println("    url : 'services/user',");
        out.println("    debug : false,");
        out.println("    service : new Model({");
        out.println("        register : Model.Function(User, String).return(Session),");
        out.println("        login : Model.Function(String, String, String).return(Session),");
        out.println("        logout : Model.Function(UUID).return(Boolean),");
        out.println("        getUsers : Model.Function(UUID, String).return(User.Array)");
        out.println("    })");
        out.println("});");
        out.println();
        out.println("module.exports = UserManager;");*/
    }

}
