package org.zenframework.easyservices.jndi;

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
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.Reference;
import javax.naming.spi.NamingManager;

public class TreeContext implements Context {

    private static final NameParser NAME_PARSER = new NameParser() {

        @Override
        public Name parse(String name) throws NamingException {
            return new NameImpl(name);
        }

    };

    protected final Name name;
    protected final Hashtable<String, Binding> bindings;
    protected final Hashtable<String, Object> environment = new Hashtable<String, Object>();
    protected final boolean strictMode;

    public TreeContext() {
        this(false);
    }

    public TreeContext(boolean strictMode) {
        this(new NameImpl(), strictMode);
    }

    private TreeContext(Name name, boolean strictMode) {
        this(name, new Hashtable<String, Binding>(), strictMode);
    }

    private TreeContext(Name name, Hashtable<String, Binding> bindings, boolean strictMode) {
        this.name = name;
        this.bindings = bindings;
        this.strictMode = strictMode;
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        if (name.isEmpty())
            return new TreeContext(name, bindings, strictMode);
        Binding binding = getParentBindings(name, true).get(name.get(name.size() - 1));
        if (binding == null)
            throw new NameNotFoundException(name.toString());
        return binding.getObject();
    }

    @Override
    public Object lookup(String name) throws NamingException {
        return lookup(NAME_PARSER.parse(name));
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        if (name.isEmpty())
            throw new NameAlreadyBoundException("<empty>");
        Map<String, Binding> bindings = getParentBindings(name, strictMode);
        String localName = name.get(name.size() - 1);
        if (bindings.get(localName) != null)
            throw new NameAlreadyBoundException(name.toString());
        bindings.put(localName, new Binding(localName, obj));
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        bind(NAME_PARSER.parse(name), obj);
    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        if (name.isEmpty())
            throw new NameAlreadyBoundException("<empty>");
        Map<String, Binding> bindings = getParentBindings(name, strictMode);
        String localName = name.get(name.size() - 1);
        Binding binding = bindings.get(localName);
        if (binding == null) {
            bindings.put(localName, new Binding(localName, obj));
        } else {
            binding.setObject(obj);
        }
    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
        rebind(NAME_PARSER.parse(name), obj);
    }

    @Override
    public void unbind(Name name) throws NamingException {
        if (name.isEmpty())
            throw new OperationNotSupportedException("Can't unbind initial context");
        Map<String, Binding> bindings = null;
        try {
            bindings = getParentBindings(name, true);
        } catch (NamingException e) {}
        if (bindings != null && bindings.remove(name.get(name.size() - 1)) == null)
            throw new NameNotFoundException(name.toString());
    }

    @Override
    public void unbind(String name) throws NamingException {
        unbind(NAME_PARSER.parse(name));
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        bind(newName, lookup(oldName));
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        rename(NAME_PARSER.parse(oldName), NAME_PARSER.parse(newName));
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        final Iterator<Map.Entry<String, Binding>> it = getParentBindings(name, true).entrySet().iterator();
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

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return list(NAME_PARSER.parse(name));
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        final Iterator<Binding> it = getParentBindings(name, true).values().iterator();
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

    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return listBindings(NAME_PARSER.parse(name));
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException {
        if (name.isEmpty())
            throw new OperationNotSupportedException("<empty>");
        Map<String, Binding> bindings = null;
        try {
            bindings = getParentBindings(name, true);
        } catch (NamingException e) {}
        if (bindings != null) {
            Binding binding = bindings.remove(name.get(name.size() - 1));
            if (binding == null)
                throw new NameNotFoundException(name.toString());
            if (!(binding.getObject() instanceof TreeContext))
                throw new NotContextException(name.toString());
            ((TreeContext) binding.getObject()).bindings.clear();
        }
    }

    @Override
    public void destroySubcontext(String name) throws NamingException {
        destroySubcontext(NAME_PARSER.parse(name));
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        Map<String, Binding> bindings = getParentBindings(name, strictMode);
        TreeContext ctx = new TreeContext(name, strictMode);
        String localName = name.get(name.size() - 1);
        bindings.put(localName, new Binding(localName, ctx));
        return ctx;
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(NAME_PARSER.parse(name));
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
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
    public Object lookupLink(String name) throws NamingException {
        return lookupLink(NAME_PARSER.parse(name));
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
        return name.toString();
    }

    protected Map<String, Binding> getParentBindings(Name name, boolean strictMode) throws NamingException {
        Map<String, Binding> bindings = this.bindings;
        for (int i = 0; i < name.size() - 1; i++) {
            String localName = name.get(i);
            Binding binding = bindings.get(localName);
            if (binding == null) {
                if (strictMode)
                    throw new NameNotFoundException(name.getPrefix(i + 1).toString());
                binding = new Binding(localName, new TreeContext(name.getPrefix(i + 1), strictMode));
                bindings.put(localName, binding);
            } else if (!(binding.getObject() instanceof TreeContext)) {
                throw new NotContextException(name.getPrefix(i + 1).toString());
            }
            bindings = ((TreeContext) binding.getObject()).bindings;
        }
        return bindings;
    }

}
