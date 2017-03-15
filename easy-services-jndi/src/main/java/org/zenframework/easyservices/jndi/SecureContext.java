package org.zenframework.easyservices.jndi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;

public class SecureContext extends FilterContext {

    public static final int NONE = 0;
    public static final int LOOKUP = 1;
    public static final int BIND = 2;
    public static final int UNBIND = 4;
    public static final int ALL = 7;

    private final List<Rule> rules = new ArrayList<Rule>();
    private final boolean systemAccess;

    public SecureContext(Context context, boolean systemAccess, Rule... rules) throws NamingException {
        this(context, systemAccess, Arrays.asList(rules));
    }

    public SecureContext(Context context, boolean systemAccess, List<Rule> rules) throws NamingException {
        super(context);
        this.systemAccess = systemAccess;
        for (Rule rule : rules)
            this.rules.add(rule.getDomainName() != null ? rule : new Rule(parser.parse(rule.getDomainStr()), rule.getAccess()));
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
        List<Rule> rules = new ArrayList<Rule>(this.rules.size());
        for (Rule rule : this.rules)
            if (rule.getDomainName().startsWith(name))
                rules.add(new Rule(rule.getDomainName().getSuffix(name.size()), rule.getAccess()));
        return new SecureContext(context.createSubcontext(name), systemAccess, rules);
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

    protected void checkAccessContext(Name name, int access) throws NamingException {
        for (Rule rule : rules) {
            // checked name in domain
            if (name.startsWith(rule.getDomainName())) {
                // access_granted = checked_access -> allowed_access
                if (((~access | rule.getAccess()) & ALL) == ALL)
                    return;
                throw new NoPermissionException(name.toString());
            }
        }
        throw new NoPermissionException(name.toString());
    }

    protected void checkSystemAccess() throws NoPermissionException {
        if (!systemAccess)
            throw new NoPermissionException();
    }

    public static String ruleToString(Rule rule) {
        return new StringBuilder().append(rule.getDomainName() != null ? rule.getDomainName().toString() : rule.getDomainStr()).append(':')
                .append((rule.getAccess() & LOOKUP) != 0 ? 'l' : '-').append((rule.getAccess() & BIND) != 0 ? 'b' : '-')
                .append((rule.getAccess() & UNBIND) != 0 ? 'u' : '-').toString();
    }

    public static Rule parseRule(String ruleStr) {
        String[] nameAccess = ruleStr.trim().split("\\:");
        if (nameAccess.length != 2)
            throw new IllegalArgumentException(ruleStr);
        return new Rule(nameAccess[0], (nameAccess[1].charAt(0) == 'l' ? LOOKUP : NONE) | (nameAccess[1].charAt(1) == 'b' ? BIND : NONE)
                | (nameAccess[1].charAt(2) == 'u' ? UNBIND : NONE));
    }

    public static class Rule {

        private final Name domainName;
        private final String domainStr;
        private final int access;

        public Rule(Name domain, int access) {
            this.domainName = domain;
            this.domainStr = null;
            this.access = access;
        }

        public Rule(String domain, int access) {
            this.domainName = null;
            this.domainStr = domain;
            this.access = access;
        }

        public Name getDomainName() {
            return domainName;
        }

        public String getDomainStr() {
            return domainStr;
        }

        public int getAccess() {
            return access;
        }

        @Override
        public int hashCode() {
            return domainName.hashCode() ^ domainStr.hashCode() ^ access;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Rule))
                return false;
            Rule rule = (Rule) obj;
            return (domainName != null && domainName.equals(rule.getDomainName()) || domainStr.equals(rule.getDomainStr()))
                    && access == rule.getAccess();
        }

        @Override
        public String toString() {
            return SecureContext.ruleToString(this);
        }

    }

}
