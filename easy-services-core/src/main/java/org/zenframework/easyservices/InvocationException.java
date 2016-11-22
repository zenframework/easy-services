package org.zenframework.easyservices;

import org.zenframework.commons.bean.PrettyStringBuilder;

public class InvocationException extends ServiceException {

    private static final long serialVersionUID = 7323392674987691317L;

    public InvocationException(RequestContext context, Object args[], Throwable cause) {
        super(getCallString(context, args) + ": " + cause.getMessage(), cause);
    }

    private static String getCallString(RequestContext context, Object args[]) {
        return context.getServiceName() + '.' + context.getMethodName() + new PrettyStringBuilder().toString(args);
    }

}
