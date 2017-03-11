package org.zenframework.easyservices.jndi;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.naming.Name;

public class ReferredBinding {

    private final Set<Name> referrers = Collections.synchronizedSet(new HashSet<Name>());
    private final Object object;

    public ReferredBinding(Object object) {
        this.object = object;
    }

    public Set<Name> getReferrers() {
        return referrers;
    }

    public Object getObject() {
        return object;
    }

    public void addReferrer(Name name) {
        referrers.add(name);
    }

    public void removeReferrer(Name name) {
        referrers.remove(name);
    }

    public int referrersCount() {
        return referrers.size();
    }

}
