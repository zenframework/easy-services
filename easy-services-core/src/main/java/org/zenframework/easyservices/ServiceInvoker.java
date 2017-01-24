package org.zenframework.easyservices;

import org.zenframework.easyservices.descriptor.ServiceDescriptor;
import org.zenframework.easyservices.serialize.Serializer;

public interface ServiceInvoker {

    String getServiceInfo(Object service, Serializer<?> serializer, ServiceDescriptor descriptor);

    String invoke(RequestContext context, Object service, Serializer<?> serializer, ServiceDescriptor descriptor) throws ServiceException;

}
