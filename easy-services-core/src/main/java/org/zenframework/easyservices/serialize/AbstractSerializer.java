package org.zenframework.easyservices.serialize;

import org.zenframework.easyservices.descriptor.ValueDescriptor;

public abstract class AbstractSerializer<S> implements Serializer<S> {

    protected final SerializerFactory<S> factory;

    protected AbstractSerializer(SerializerFactory<S> factory) {
        this.factory = factory;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object deserialize(S structure, Class<?> objType, ValueDescriptor valueDescriptor) throws SerializationException {
        if (structure == null)
            return null;
        SerializerAdapter adapter = getSerializerAdapter(objType, valueDescriptor);
        if (adapter != null)
            return deserialize(structure, adapter, valueDescriptor != null ? valueDescriptor.getTypeParameters() : new Class<?>[0]);
        return deserialize(structure, objType);
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public S serialize(Object object, ValueDescriptor valueDescriptor) {
        if (object == null)
            return null;
        return serialize(object, getSerializerAdapter(object.getClass(), valueDescriptor));
    }

    @SuppressWarnings({ "rawtypes" })
    private SerializerAdapter getSerializerAdapter(Class<?> objType, ValueDescriptor valueDescriptor) {
        SerializerAdapter adapter = null;
        if (valueDescriptor != null) {
            adapter = valueDescriptor.getSerializerAdapter();
            if (adapter == null)
                adapter = factory.getAdapter(objType);
        }
        return adapter;
    }

    private Object deserialize(S structure, SerializerAdapter<S> adapter, Class<?>... typeParameters) throws SerializationException {
        if (structure == null)
            return null;
        return adapter.deserialize(this, structure, typeParameters);
    }

    private S serialize(Object object, SerializerAdapter<S> adapter) {
        if (object == null)
            return null;
        if (adapter != null)
            return adapter.serialize(this, object);
        return serialize(object);
    }

}
