package org.zenframework.easyservices.jndi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;
import javax.naming.OperationNotSupportedException;

public class SharedContext extends FilterContext {

    private final Map<String, Set<String>> referrers = new HashMap<String, Set<String>>();
    private final Name privatePath;
    private final Name sharedPath;

    public SharedContext(Context context, String privatePath, String sharedPath) throws NamingException {
        super(context);
        this.privatePath = parser.parse(privatePath);
        this.sharedPath = parser.parse(sharedPath);
    }

    public SharedContext(Context context, Name privatePath, Name sharedPath) throws NamingException {
        super(context);
        this.privatePath = privatePath;
        this.sharedPath = sharedPath;
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        if (name.startsWith(sharedPath))
            throw new NoPermissionException(sharedPath.toString());
        if (name.startsWith(privatePath)) {
            Name uniqueName = getUniqueObjectName(obj);
            context.bind(name, new LinkRef(uniqueName));
            try {
                try {
                    Object uniqueObject = context.lookup(uniqueName);
                    if (uniqueObject != obj)
                        throw new IllegalStateException("Unique object " + uniqueObject + " at " + uniqueName + " != already bound object " + obj);
                } catch (NamingException e) {
                    context.bind(uniqueName, obj);
                }
                addReferrer(uniqueName.toString(), name.toString());
            } catch (NamingException e) {
                context.unbind(name);
                throw e;
            }
        } else {
            context.bind(name, obj);
        }
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        bind(parser.parse(name), obj);
    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        if (name.startsWith(sharedPath))
            throw new NoPermissionException(sharedPath.toString());
        if (name.startsWith(privatePath)) {
            LinkRef link = null;
            try {
                link = (LinkRef) context.lookup(name);
            } catch (NamingException e) {}
            if (link != null) {
                String uniqueName = link.getLinkName();
                Object uniqueObj = context.lookup(uniqueName);
                removeReferrer(uniqueName, name.toString());
                if (removeReferrer(uniqueName, name.toString()) > 0)
                    throw new IllegalStateException("Unique object " + uniqueObj + " at " + uniqueName + " has referrers");
                context.unbind(uniqueName);
            }
            Name uniqueName = getUniqueObjectName(obj);
            context.rebind(name, new LinkRef(uniqueName));
            try {
                try {
                    Object uniqueObj = context.lookup(uniqueName);
                    if (uniqueObj != obj)
                        throw new IllegalStateException("Unique object " + uniqueObj + " at " + uniqueName + " != bound object " + obj);
                } catch (NamingException e) {
                    context.bind(uniqueName, obj);
                }
                addReferrer(uniqueName.toString(), name.toString());
            } catch (NamingException e) {
                context.unbind(name);
                throw e;
            }
        } else {
            context.rebind(name, obj);
        }

    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
        rebind(parser.parse(name), obj);
    }

    @Override
    public void unbind(Name name) throws NamingException {
        if (name.startsWith(sharedPath))
            throw new NoPermissionException(sharedPath.toString());
        if (name.startsWith(privatePath)) {
            LinkRef link = (LinkRef) context.lookup(name);
            int count = removeReferrer(link.getLinkName(), name.toString());
            if (count == 0)
                context.unbind(link.getLinkName());
        }
        context.unbind(name);
    }

    @Override
    public void unbind(String name) throws NamingException {
        unbind(parser.parse(name));
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        boolean oldChecked = oldName.startsWith(privatePath);
        boolean newChecked = newName.startsWith(privatePath);
        if (oldName.startsWith(sharedPath) || newName.startsWith(sharedPath))
            throw new NoPermissionException(sharedPath.toString());
        if (oldChecked && !newChecked || !oldChecked && newChecked)
            throw new OperationNotSupportedException("Rename " + oldName + " to " + newName);
        context.rename(oldName, newName);
        if (oldChecked && newChecked) {
            LinkRef link = (LinkRef) context.lookup(newName);
            try {
                removeReferrer(link.getLinkName(), oldName.toString());
                addReferrer(link.getLinkName(), newName.toString());
            } catch (NamingException e) {}
        }
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        rename(parser.parse(oldName), parser.parse(newName));
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        boolean checked = privatePath.startsWith(name);
        boolean unique = sharedPath.startsWith(name);
        if (checked && unique)
            return new SharedContext(context.createSubcontext(name), privatePath.getSuffix(name.size()),
                    sharedPath.getSuffix(name.size()));
        if (!checked && !unique)
            return context.createSubcontext(name);
        throw new OperationNotSupportedException(name.toString());
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(parser.parse(name));
    }

    protected Name getUniqueObjectName(Object object) throws InvalidNameException {
        return sharedPath.add(object.getClass().getCanonicalName() + '@' + System.identityHashCode(object));
    }

    private void addReferrer(String referred, String referrer) {
        synchronized (referrers) {
            Set<String> refs = referrers.get(referred);
            if (refs == null) {
                refs = new HashSet<String>();
                referrers.put(referred, refs);
            }
            refs.add(referrer);
        }
    }

    private int removeReferrer(String referred, String referrer) {
        synchronized (referrers) {
            Set<String> refs = referrers.get(referred);
            if (refs != null) {
                refs.remove(referrer);
                return refs.size();
            }
            return 0;
        }
    }

}
