package org.zenframework.easyservices.jndi;

import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;
import javax.naming.OperationNotSupportedException;

public class UniqueObjectsContext extends FilterContext {

    private final Name checkedPath;
    private final Name uniqueObjectsPath;

    public UniqueObjectsContext(Context context, Name checkedPath, Name uniqueObjectsPath) throws NamingException {
        super(context);
        this.checkedPath = checkedPath;
        this.uniqueObjectsPath = uniqueObjectsPath;
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        Object object = context.lookup(name);
        return object instanceof ReferredBinding ? ((ReferredBinding) object).getObject() : object;
    }

    @Override
    public Object lookup(String name) throws NamingException {
        Object object = context.lookup(name);
        return object instanceof ReferredBinding ? ((ReferredBinding) object).getObject() : object;
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        Object object = context.lookupLink(name);
        return object instanceof ReferredBinding ? ((ReferredBinding) object).getObject() : object;
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        Object object = context.lookupLink(name);
        return object instanceof ReferredBinding ? ((ReferredBinding) object).getObject() : object;
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        if (name.startsWith(uniqueObjectsPath))
            throw new NoPermissionException(uniqueObjectsPath.toString());
        if (name.startsWith(checkedPath)) {
            Name uniqueName = getUniqueObjectName(obj);
            context.bind(name, new LinkRef(uniqueName));
            try {
                ReferredBinding binding;
                try {
                    binding = (ReferredBinding) context.lookup(uniqueName);
                } catch (NamingException e) {
                    binding = new ReferredBinding(obj);
                    context.bind(uniqueName, binding);
                }
                if (binding.getObject() != obj)
                    throw new OperationNotSupportedException(
                            "Unique object " + binding.getObject() + " at " + uniqueName + " != bound object " + obj);
                binding.addReferrer(name);
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
        if (name.startsWith(uniqueObjectsPath))
            throw new NoPermissionException(uniqueObjectsPath.toString());
        if (name.startsWith(checkedPath)) {
            LinkRef link = null;
            try {
                link = (LinkRef) context.lookup(name);
            } catch (NamingException e) {}
            if (link != null) {
                String uniqueName = link.getLinkName();
                ReferredBinding binding = (ReferredBinding) context.lookup(uniqueName);
                binding.removeReferrer(name);
                if (binding.referrersCount() > 0)
                    throw new OperationNotSupportedException("Unique object " + binding.getObject() + " at " + uniqueName + " has referrers");
                context.unbind(uniqueName);
            }
            Name uniqueName = getUniqueObjectName(obj);
            context.rebind(name, new LinkRef(uniqueName));
            try {
                ReferredBinding binding;
                try {
                    binding = (ReferredBinding) context.lookup(uniqueName);
                } catch (NamingException e) {
                    binding = new ReferredBinding(obj);
                    context.bind(uniqueName, binding);
                }
                if (binding.getObject() != obj)
                    throw new OperationNotSupportedException(
                            "Unique object " + binding.getObject() + " at " + uniqueName + " != bound object " + obj);
                binding.addReferrer(name);
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
        if (name.startsWith(uniqueObjectsPath))
            throw new NoPermissionException(uniqueObjectsPath.toString());
        if (name.startsWith(checkedPath)) {
            LinkRef link = (LinkRef) context.lookup(name);
            try {
                ReferredBinding binding = (ReferredBinding) context.lookup(link.getLinkName());
                binding.removeReferrer(name);
            } catch (NamingException e) {}
        }
        context.unbind(name);
    }

    @Override
    public void unbind(String name) throws NamingException {
        unbind(parser.parse(name));
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        boolean oldChecked = oldName.startsWith(checkedPath);
        boolean newChecked = newName.startsWith(checkedPath);
        if (oldName.startsWith(uniqueObjectsPath) || newName.startsWith(uniqueObjectsPath))
            throw new NoPermissionException(uniqueObjectsPath.toString());
        if (oldChecked && !newChecked || !oldChecked && newChecked)
            throw new OperationNotSupportedException("Rename " + oldName + " to " + newName);
        context.rename(oldName, newName);
        if (oldChecked && newChecked) {
            LinkRef link = (LinkRef) context.lookup(newName);
            try {
                ReferredBinding binding = (ReferredBinding) context.lookup(link.getLinkName());
                binding.removeReferrer(oldName);
                binding.addReferrer(newName);
            } catch (NamingException e) {}
        }
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        rename(parser.parse(oldName), parser.parse(newName));
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        boolean checked = checkedPath.startsWith(name);
        boolean unique = uniqueObjectsPath.startsWith(name);
        if (checked && unique)
            return new UniqueObjectsContext(context.createSubcontext(name), checkedPath.getSuffix(name.size()),
                    uniqueObjectsPath.getSuffix(name.size()));
        if (!checked && !unique)
            return context.createSubcontext(name);
        throw new OperationNotSupportedException(name.toString());
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(parser.parse(name));
    }

    protected Name getUniqueObjectName(Object object) throws InvalidNameException {
        return uniqueObjectsPath.add(object.getClass().getCanonicalName() + '@' + System.identityHashCode(object));
    }

}
