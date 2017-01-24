package org.zenframework.easyservices.serialize;

import org.zenframework.easyservices.descriptor.ValueDescriptor;

public abstract class AbstractSerializer<S> implements Serializer<S> {

    private final SerializerFactory<S> factory;

    protected AbstractSerializer(SerializerFactory<S> factory) {
        this.factory = factory;
    }

    @Override
    public SerializerFactory<S> getFactory() {
        return factory;
    }

    @Override
    public Object deserialize(S objStruct, SerializerAdapter<S> adapter, Class<?>... typeParameters) throws SerializationException {
        if (objStruct == null)
            return null;
        return adapter.deserialize(this, objStruct, typeParameters);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object deserialize(S objStruct, Class<?> objClass, ValueDescriptor valueDescriptor) throws SerializationException {
        SerializerAdapter adapter = null;
        if (valueDescriptor != null)
            adapter = valueDescriptor.getSerializerAdapter();
        if (adapter == null)
            adapter = getFactory().getAdapter(objClass);
        if (adapter != null)
            return deserialize(objStruct, adapter, valueDescriptor != null ? valueDescriptor.getTypeParameters() : new Class<?>[0]);
        return deserialize(objStruct, objClass);
    }

    @Override
    public Object[] deserialize(S[] arrStruct, Class<?> objTypes[]) throws SerializationException {
        if (arrStruct == null)
            return new Object[0];
        Object[] result = new Object[objTypes.length];
        if (arrStruct.length != result.length)
            throw new SerializationException("Incorrect number of arguments: " + arrStruct.length + ", expected: " + result.length);
        for (int i = 0; i < result.length; i++)
            result[i] = deserialize(arrStruct[i], objTypes[i]);
        return result;
    }

    @Override
    public Object[] deserialize(S[] arrStruct, SerializerAdapter<S>[] adapters, Class<?> typeParameters[][]) throws SerializationException {
        if (arrStruct == null)
            return new Object[0];
        Object[] result = new Object[adapters.length];
        if (arrStruct.length != result.length)
            throw new SerializationException("Incorrect number of arguments: " + arrStruct.length + ", expected: " + result.length);
        for (int i = 0; i < result.length; i++)
            result[i] = deserialize(arrStruct[i], adapters[i], typeParameters[i]);
        return result;
    }

    @Override
    public Object[] deserialize(S[] arrStruct, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws SerializationException {
        if (arrStruct == null)
            return new Object[0];
        Object[] result = new Object[objTypes.length];
        if (arrStruct.length != result.length)
            throw new SerializationException("Incorrect number of arguments: " + arrStruct.length + ", expected: " + result.length);
        for (int i = 0; i < result.length; i++)
            result[i] = deserialize(arrStruct[i], objTypes[i], valueDescriptors[i]);
        return result;
    }

}
