package org.zenframework.easyservices.util.bean;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.zenframework.easyservices.util.string.QuotedStringTokenizer;

public class BeanCmdProcessor {

    private Object executorBean;
    private Class<?> executorClass;
    private final Map<String, List<Method>> methods = new HashMap<String, List<Method>>();
    private final List<ObjectConverter> converters = new LinkedList<ObjectConverter>(
            Arrays.asList(new DefaultObjectConverter()));

    public Object getExecutorBean() {
        return executorBean;
    }

    public void setExecutorBean(Object executorBean) {
        this.executorBean = executorBean;
        methods.clear();
    }

    public Class<?> getExecutorClass() {
        return executorClass;
    }

    public void setExecutorClass(Class<?> executorClass) {
        this.executorClass = executorClass;
        methods.clear();
    }

    private Object executeCommand(String cmd, PrintStream out, PrintStream err) throws BeanCmdException {
        prepareMethods();
        QuotedStringTokenizer tokenizer = new QuotedStringTokenizer(cmd);
        if (!tokenizer.hasMoreElements()) {
            throw new BeanCmdException("Command line is empty");
        }
        String methodName = tokenizer.nextElement();
        List<Method> methodsByName = methods.get(methodName);
        if (methodsByName == null) {
            throw new BeanCmdException("Can't find method '" + methodName + "' in " + executorBean);
        }
        List<String> strArgs = new ArrayList<String>();
        while (tokenizer.hasMoreElements()) {
            strArgs.add(tokenizer.nextElement());
        }
        Object args[] = new Object[strArgs.size()];
        for (Method method : methodsByName) {
            int argsCount = method.getParameterTypes().length;
            // find method by number of arguments
            if (argsCount == args.length) {
                // try to convert arguments
                boolean convertionSuccess = false;
                try {
                    for (int i = 0; i < argsCount; i++) {
                        args[i] = convert(strArgs.get(i), method.getParameterTypes()[i]);
                    }
                    convertionSuccess = true;
                } catch (ConverterException e) {
                    //LOG.debug("Method " + method + " skipped", e);
                    err.println("Method " + method + " skipped");
                    e.printStackTrace(err);
                }
                if (convertionSuccess) {
                    try {
                        return method.invoke(executorBean, args);
                    } catch (IllegalArgumentException e) {
                        throw new BeanCmdException(e);
                    } catch (IllegalAccessException e) {
                        throw new BeanCmdException(e);
                    } catch (InvocationTargetException e) {
                        throw new BeanCmdException(e.getTargetException());
                    }
                }
            }
        }
        throw new BeanCmdException("Can't execute '" + cmd + "'");
    }

    public void execute(String cmd, PrintStream out, PrintStream err) {
        try {
            //out.println(ReflectionToStringBuilder.toString(execute(cmd), toStringStyle));
            out.println(new PrettyStringBuilder().toString(executeCommand(cmd, out, err)));
            //out.println(execute(cmd));
        } catch (Throwable e) {
            e.printStackTrace(err);
        }
    }

    private void prepareMethods() {
        if (methods.isEmpty()) {
            if (executorClass == null) {
                executorClass = executorBean.getClass();
            }
            for (Class<?> clazz = executorClass; clazz != null; clazz = clazz.getSuperclass()) {
                for (Method method : clazz.getMethods()) {
                    List<Method> methodsByName = methods.get(method.getName());
                    if (methodsByName == null) {
                        methodsByName = new LinkedList<Method>();
                        methods.put(method.getName(), methodsByName);
                    }
                    methodsByName.add(method);
                }
            }
        }
    }

    public void addConverter(ObjectConverter converter) {
        converters.add(converter);
    }

    public void setConverters(List<ObjectConverter> converters) {
        this.converters.clear();
        this.converters.addAll(converters);
    }

    private Object convert(Object value, Class<?> clazz) throws ConverterException {
        if (value == null) {
            return null;
        }
        for (ObjectConverter converter : converters) {
            try {
                return converter.toClass(value, clazz);
            } catch (ConverterException e) {/**/}
        }
        throw new ConverterException(value, clazz);
    }

}
