package org.zenframework.easyservices.jndi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;

public class SecureContext extends FilterContext {

    protected static final int LOOKUP = 1;
    protected static final int BIND = 2;
    protected static final int UNBIND = 4;

    private final Set<Name> allowedDomains = new HashSet<Name>();
    private final boolean systemAccess;

    public SecureContext(Context context, boolean systemAccess, Name... allowedDomains) throws NamingException {
        super(context);
        this.systemAccess = systemAccess;
        this.allowedDomains.addAll(Arrays.asList(allowedDomains));
    }

    public SecureContext(Context context, boolean systemAccess, String... allowedDomains) throws NamingException {
        super(context);
        this.systemAccess = systemAccess;
        for (String allowedDomain : allowedDomains)
            this.allowedDomains.add(parser.parse(allowedDomain));
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        checkAccessContext(name, LOOKUP);
        return context.lookup(name);
    }

    @Override
    public Object lookup(String name) throws NamingException {
        return lookup(parser.parse(name));
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        checkAccessContext(name, BIND);
        context.bind(name, obj);
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        bind(parser.parse(name), obj);
    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        checkAccessContext(name, BIND & UNBIND);
        context.rebind(name, obj);
    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
        rebind(parser.parse(name), obj);
    }

    @Override
    public void unbind(Name name) throws NamingException {
        checkAccessContext(name, UNBIND);
        context.unbind(name);
    }

    @Override
    public void unbind(String name) throws NamingException {
        unbind(parser.parse(name));
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        checkAccessContext(oldName, UNBIND);
        checkAccessContext(newName, BIND);
        context.rename(oldName, newName);
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        rename(parser.parse(oldName), parser.parse(newName));
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        checkAccessContext(name, LOOKUP);
        return context.list(name);
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return list(parser.parse(name));
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        checkAccessContext(name, LOOKUP);
        return context.listBindings(name);
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return listBindings(parser.parse(name));
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException {
        checkAccessContext(name, UNBIND);
        context.destroySubcontext(name);
    }

    @Override
    public void destroySubcontext(String name) throws NamingException {
        destroySubcontext(parser.parse(name));
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        Set<Name> allowedSubdomains = new HashSet<Name>();
        for (Name allowedDomain : allowedDomains)
            if (allowedDomain.startsWith(name))
                allowedSubdomains.add(allowedDomain.getSuffix(name.size()));
        return new SecureContext(context.createSubcontext(name), systemAccess, allowedSubdomains.toArray(new Name[allowedSubdomains.size()]));
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(parser.parse(name));
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        checkAccessContext(name, LOOKUP);
        return context.lookupLink(name);
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        return lookupLink(parser.parse(name));
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        checkSystemAccess();
        return context.addToEnvironment(propName, propVal);
    }

    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        checkSystemAccess();
        return context.removeFromEnvironment(propName);
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        checkSystemAccess();
        return context.getEnvironment();
    }

    @Override
    public void close() throws NamingException {
        checkSystemAccess();
        context.close();
    }

    @Override
    public String getNameInNamespace() throws NamingException {
        return context.getNameInNamespace();
    }

    protected void checkAccessContext(Name name, int operation) throws NamingException {
        name = context.composeName(name, contextName);
        for (Name allowedDomain : allowedDomains)
            if (name.startsWith(allowedDomain))
                return;
        throw new NoPermissionException(name.toString());
    }

    protected void checkSystemAccess() throws NoPermissionException {
        if (!systemAccess)
            throw new NoPermissionException();
    }

}
