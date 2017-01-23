package org.zenframework.easyservices;

import org.zenframework.easyservices.serialize.Serializer;

public interface ServiceInvoker {

    Object getServiceInfo(Object service);
    
    Object invoke(RequestContext context, Object service, Serializer<?> serializer) throws ServiceException;

}
