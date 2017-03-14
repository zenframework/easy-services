package org.zenframework.easyservices.jndi;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.NamingManager;

@Deprecated
public class PlainContext implements Context {

    private static final NameParser NAME_PARSER = new NameParser() {

        @Override
        public Name parse(String name) throws NamingException {
            return new NameImpl(name);
        }

    };

    private final String name;
    private final String prefix;
    private final Map<String, Binding> context;
    private final Hashtable<String, Object> environment = new Hashtable<String, Object>();

    public PlainContext() {
        this("", new HashMap<String, Binding>());
    }

    private PlainContext(String name, Map<String, Binding> context) {
        this.name = name;
        this.prefix = name.isEmpty() ? "" : name + NameImpl.DELIMETER;
        this.context = context;
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }

    @Override
    public Object lookup(String name) throws NamingException {
        name = prefix + name;
        synchronized (context) {
            Binding binding = context.get(name);
            if (binding == null)
                throw new NameNotFoundException("Object '" + name + "' not bound");
            return binding.getObject();
        }
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        bind(name.toString(), obj);
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        name = prefix + name;
        synchronized (context) {
            if (context.containsKey(name))
                throw new NameAlreadyBoundException("Object '" + name + "' already bound");
            context.put(name, new Binding(name, obj));
        }
    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        rebind(name.toString(), obj);
    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
        name = prefix + name;
        synchronized (context) {
            context.put(name, new Binding(name, obj));
        }
    }

    @Override
    public void unbind(Name name) throws NamingException {
        unbind(name.toString());
    }

    @Override
    public void unbind(String name) throws NamingException {
        name = prefix + name;
        synchronized (context) {
            if (!context.containsKey(name))
                throw new NameNotFoundException("Object '" + name + "' not bound");
            context.remove(name);
        }
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        rename(oldName.toString(), newName.toString());
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        oldName = prefix + oldName;
        newName = prefix + newName;
        synchronized (context) {
            if (!context.containsKey(oldName))
                throw new NameNotFoundException("Object '" + oldName + "' not bound");
            if (context.containsKey(newName))
                throw new NameAlreadyBoundException("Object '" + newName + "' already bound");
            Binding binding = context.remove(oldName);
            binding.setName(newName);
            context.put(newName, binding);
        }
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return list(name.toString());
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        name = prefix + name;
        synchronized (context) {
            final Iterator<Map.Entry<String, Binding>> it = getSubContext(name).entrySet().iterator();
            return new NamingEnumeration<NameClassPair>() {

                @Override
                public boolean hasMoreElements() {
                    return it.hasNext();
                }

                @Override
                public NameClassPair nextElement() {
                    Map.Entry<String, Binding> entry = it.next();
                    Object obj = entry.getValue().getObject();
                    return new NameClassPair(entry.getKey(), obj == null ? null : obj.getClass().getName());
                }

                @Override
                public NameClassPair next() throws NamingException {
                    return nextElement();
                }

                @Override
                public boolean hasMore() throws NamingException {
                    return hasMoreElements();
                }

                @Override
                public void close() throws NamingException {}

            };
        }
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return listBindings(name.toString());
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        name = prefix + name;
        synchronized (context) {
            final Iterator<Binding> it = getSubContext(name).values().iterator();
            return new NamingEnumeration<Binding>() {

                @Override
                public boolean hasMoreElements() {
                    return it.hasNext();
                }

                @Override
                public Binding nextElement() {
                    return it.next();
                }

                @Override
                public Binding next() throws NamingException {
                    return nextElement();
                }

                @Override
                public boolean hasMore() throws NamingException {
                    return hasMoreElements();
                }

                @Override
                public void close() throws NamingException {}

            };
        }
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException {
        destroySubcontext(name.toString());
    }

    @Override
    public void destroySubcontext(String name) throws NamingException {
        name = prefix + name;
        synchronized (context) {
            Collection<String> keys = new HashSet<String>(context.keySet());
            String path = name.isEmpty() ? "" : name + NameImpl.DELIMETER;
            for (String key : keys)
                if (key.equals(name) || key.startsWith(path))
                    context.remove(key);
        }
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        return createSubcontext(name.toString());
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        return new PlainContext(name, context);
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        return lookupLink(name.toString());
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        Object result = lookup(name);
        if (result instanceof LinkRef) {
            result = lookup(((LinkRef) result).getLinkName());
        } else if (result instanceof Reference) {
            try {
                result = NamingManager.getObjectInstance(result, null, null, this.environment);
            } catch (NamingException e) {
                throw e;
            } catch (Exception e) {
                throw (NamingException) new NamingException("Could not look up : " + name).initCause(e);
            }
        }
        return result;
    }

    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        return NAME_PARSER;
    }

    @Override
    public NameParser getNameParser(String name) throws NamingException {
        return NAME_PARSER;
    }

    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        return prefix.addAll(name);
    }

    @Override
    public String composeName(String name, String prefix) throws NamingException {
        StringBuilder str = new StringBuilder(name.length() + prefix.length() + NameImpl.DELIMETER.length());
        str.append(prefix);
        if (!name.isEmpty() && !prefix.isEmpty())
            str.append(NameImpl.DELIMETER);
        str.append(name);
        return str.toString();
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return environment.put(propName, propVal);
    }

    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        return environment.remove(propName);
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return environment;
    }

    @Override
    public void close() throws NamingException {}

    @Override
    public String getNameInNamespace() throws NamingException {
        return name;
    }

    private Map<String, Binding> getSubContext(String name) throws NamingException {
        Map<String, Binding> subContext = new HashMap<String, Binding>();
        String path = name.isEmpty() ? "" : name + NameImpl.DELIMETER;
        for (Map.Entry<String, Binding> entry : context.entrySet())
            if (entry.getKey().equals(name) || entry.getKey().startsWith(path))
                subContext.put(entry.getKey(), entry.getValue());
        //if (subContext.isEmpty())
        //    throw new NamingException("Subcontext '" + name + "' is empty");
        return subContext;
    }

}
