package org.zenframework.easyservices.serialize;

import org.zenframework.easyservices.descriptor.ValueDescriptor;

public abstract class AbstractSerializer<S> implements Serializer<S> {

    protected final SerializerFactory<S> factory;

    protected AbstractSerializer(SerializerFactory<S> factory) {
        this.factory = factory;
    }

    @Override
    public <T> T deserialize(S structure, Class<T> objType, ValueDescriptor valueDescriptor) throws SerializationException {
        if (structure == null)
            return null;
        SerializerAdapter<S, T> adapter = getSerializerAdapter(objType, valueDescriptor);
        if (adapter != null)
            return deserialize(structure, adapter, valueDescriptor != null ? valueDescriptor.getTypeParameters() : new Class<?>[0]);
        return deserialize(structure, objType);
    }

    @Override
    public S serialize(Object object, ValueDescriptor valueDescriptor) {
        if (object == null)
            return null;
        return serialize(object, getSerializerAdapter(object.getClass(), valueDescriptor));
    }

    @SuppressWarnings("unchecked")
    private <T> SerializerAdapter<S, T> getSerializerAdapter(Class<T> objType, ValueDescriptor valueDescriptor) {
        SerializerAdapter<S, T> adapter = null;
        if (valueDescriptor != null) {
            adapter = (SerializerAdapter<S, T>) valueDescriptor.getSerializerAdapter();
            if (adapter == null)
                adapter = factory.getAdapter(objType);
        }
        return adapter;
    }

    private <T> T deserialize(S structure, SerializerAdapter<S, T> adapter, Class<?>... typeParameters) throws SerializationException {
        if (structure == null)
            return null;
        return adapter.deserialize(this, structure, typeParameters);
    }

    private S serialize(Object object, SerializerAdapter<S, ?> adapter) {
        if (object == null)
            return null;
        if (adapter != null)
            return adapter.serialize(this, object);
        return serialize(object);
    }

}
