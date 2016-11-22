package org.zenframework.easyservices.jndi.impl;

import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;

public class NameParserImpl implements NameParser {

    private final String prefix;
    
    public NameParserImpl(String prefix) {
        this.prefix = prefix == null || prefix.isEmpty() ? "" : prefix + NameImpl.DELIMETER;
    }
    
    @Override
    public Name parse(String name) throws NamingException {
        return new NameImpl(prefix + name);
    }

}
