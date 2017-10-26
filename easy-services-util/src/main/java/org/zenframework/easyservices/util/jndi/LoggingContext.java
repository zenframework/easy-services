package org.zenframework.easyservices.util.jndi;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingContext implements Context {

    private final Context context;
    private final Logger log;

    public LoggingContext(Context context) {
        this.context = context;
        this.log = LoggerFactory.getLogger(context.getClass());
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        try {
            Object object = context.lookup(name);
            log.debug("CONTEXT LOOKUP " + name + ": " + object);
            return object;
        } catch (NamingException e) {
            log.debug("CONTEXT LOOKUP " + name + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public Object lookup(String name) throws NamingException {
        try {
            Object object = context.lookup(name);
            log.debug("CONTEXT LOOKUP " + name + ": " + object);
            return object;
        } catch (NamingException e) {
            log.debug("CONTEXT LOOKUP " + name + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        try {
            context.bind(name, obj);
            log.debug("CONTEXT BIND " + name + " = " + obj);
        } catch (NamingException e) {
            log.debug("CONTEXT BIND '" + name + " = " + obj + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        try {
            context.bind(name, obj);
            log.debug("CONTEXT BIND " + name + " = " + obj);
        } catch (NamingException e) {
            log.debug("CONTEXT BIND '" + name + " = " + obj + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        try {
            context.rebind(name, obj);
            log.debug("CONTEXT REBIND " + name + " = " + obj);
        } catch (NamingException e) {
            log.debug("CONTEXT REBIND '" + name + " = " + obj + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
        try {
            context.rebind(name, obj);
            log.debug("CONTEXT REBIND " + name + " = " + obj);
        } catch (NamingException e) {
            log.debug("CONTEXT REBIND '" + name + " = " + obj + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public void unbind(Name name) throws NamingException {
        try {
            context.unbind(name);
            log.debug("CONTEXT UNBIND " + name);
        } catch (NamingException e) {
            log.debug("CONTEXT UNBIND '" + name + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public void unbind(String name) throws NamingException {
        try {
            context.unbind(name);
            log.debug("CONTEXT UNBIND " + name);
        } catch (NamingException e) {
            log.debug("CONTEXT UNBIND '" + name + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        try {
            context.rename(oldName, newName);
            log.debug("CONTEXT RENAME " + oldName + " TO " + newName);
        } catch (NamingException e) {
            log.debug("CONTEXT RENAME " + oldName + " TO " + newName + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        try {
            context.rename(oldName, newName);
            log.debug("CONTEXT RENAME " + oldName + " TO " + newName);
        } catch (NamingException e) {
            log.debug("CONTEXT RENAME " + oldName + " TO " + newName + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        try {
            NamingEnumeration<NameClassPair> list = context.list(name);
            log.debug("CONTEXT LIST " + name + ": " + list);
            return list;
        } catch (NamingException e) {
            log.debug("CONTEXT LIST " + name + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        try {
            NamingEnumeration<NameClassPair> list = context.list(name);
            log.debug("CONTEXT LIST " + name + ": " + list);
            return list;
        } catch (NamingException e) {
            log.debug("CONTEXT LIST " + name + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        try {
            NamingEnumeration<Binding> list = context.listBindings(name);
            log.debug("CONTEXT LIST BINDINGS " + name + ": " + list);
            return list;
        } catch (NamingException e) {
            log.debug("CONTEXT LIST BINDINGS " + name + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        try {
            NamingEnumeration<Binding> list = context.listBindings(name);
            log.debug("CONTEXT LIST BINDINGS " + name + ": " + list);
            return list;
        } catch (NamingException e) {
            log.debug("CONTEXT LIST BINDINGS " + name + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException {
        try {
            context.destroySubcontext(name);
            log.debug("CONTEXT DESTROY " + name);
        } catch (NamingException e) {
            log.debug("CONTEXT DESTROY '" + name + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public void destroySubcontext(String name) throws NamingException {
        try {
            context.destroySubcontext(name);
            log.debug("CONTEXT DESTROY " + name);
        } catch (NamingException e) {
            log.debug("CONTEXT DESTROY '" + name + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        try {
            Context con = context.createSubcontext(name);
            log.debug("CONTEXT CREATE " + name + ": " + con);
            return con;
        } catch (NamingException e) {
            log.debug("CONTEXT CREATE " + name + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        try {
            Context con = context.createSubcontext(name);
            log.debug("CONTEXT CREATE " + name + ": " + con);
            return con;
        } catch (NamingException e) {
            log.debug("CONTEXT CREATE " + name + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        try {
            Object object = context.lookupLink(name);
            log.debug("CONTEXT LOOKUP LINK " + name + ": " + object);
            return object;
        } catch (NamingException e) {
            log.debug("CONTEXT LOOKUP LINK " + name + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        try {
            Object object = context.lookupLink(name);
            log.debug("CONTEXT LOOKUP LINK " + name + ": " + object);
            return object;
        } catch (NamingException e) {
            log.debug("CONTEXT LOOKUP LINK " + name + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        try {
            NameParser parser = context.getNameParser(name);
            log.debug("CONTEXT GET NAME PARSER " + name + ": " + parser);
            return parser;
        } catch (NamingException e) {
            log.debug("CONTEXT GET NAME PARSER " + name + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public NameParser getNameParser(String name) throws NamingException {
        try {
            NameParser parser = context.getNameParser(name);
            log.debug("CONTEXT GET NAME PARSER " + name + ": " + parser);
            return parser;
        } catch (NamingException e) {
            log.debug("CONTEXT GET NAME PARSER " + name + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        try {
            Name newName = context.composeName(name, prefix);
            log.debug("CONTEXT COMPOSE " + name + ", " + prefix + ": " + newName);
            return newName;
        } catch (NamingException e) {
            log.debug("CONTEXT COMPOSE " + name + ", " + prefix + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public String composeName(String name, String prefix) throws NamingException {
        try {
            String newName = context.composeName(name, prefix);
            log.debug("CONTEXT COMPOSE " + name + ", " + prefix + ": " + newName);
            return newName;
        } catch (NamingException e) {
            log.debug("CONTEXT COMPOSE " + name + ", " + prefix + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        try {
            Object object = context.addToEnvironment(propName, propVal);
            log.debug("CONTEXT ADD TO ENV " + propName + " = " + propVal);
            return object;
        } catch (NamingException e) {
            log.debug("CONTEXT ADD TO ENV " + propName + " = " + propVal + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        try {
            Object object = context.removeFromEnvironment(propName);
            log.debug("CONTEXT REMOVE FROM ENV " + propName);
            return object;
        } catch (NamingException e) {
            log.debug("CONTEXT REMOVE FROM ENV " + propName + ": FAILED", e);
            throw e;
        }
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        try {
            Hashtable<?, ?> env = context.getEnvironment();
            log.debug("CONTEXT GET ENV: " + env);
            return env;
        } catch (NamingException e) {
            log.debug("CONTEXT GET ENV: FAILED", e);
            throw e;
        }
    }

    @Override
    public void close() throws NamingException {
        try {
            context.close();
            log.debug("CONTEXT CLOSE");
        } catch (NamingException e) {
            log.debug("CONTEXT CLOSE: FAILED", e);
            throw e;
        }
    }

    @Override
    public String getNameInNamespace() throws NamingException {
        try {
            String name = context.getNameInNamespace();
            log.debug("CONTEXT GET NAME: " + name);
            return name;
        } catch (NamingException e) {
            log.debug("CONTEXT GET NAME: FAILED", e);
            throw e;
        }
    }

}
