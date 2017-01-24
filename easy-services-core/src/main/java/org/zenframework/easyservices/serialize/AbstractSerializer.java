package org.zenframework.easyservices.serialize;

import org.zenframework.easyservices.descriptor.ValueDescriptor;

public abstract class AbstractSerializer<S> implements Serializer<S> {

    protected final SerializerFactory<S> factory;

    protected AbstractSerializer(SerializerFactory<S> factory) {
        this.factory = factory;
    }

    @Override
    public Object deserialize(S structure, SerializerAdapter<S> adapter, Class<?>... typeParameters) throws SerializationException {
        if (structure == null)
            return null;
        return adapter.deserialize(this, structure, typeParameters);
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

    @Override
    public Object[] deserialize(S structure, Class<?> objTypes[]) throws SerializationException {
        if (structure == null)
            return new Object[0];
        S[] array = toArray(structure);
        Object[] result = new Object[objTypes.length];
        if (array.length != result.length)
            throw new SerializationException("Incorrect number of arguments: " + array.length + ", expected: " + result.length);
        for (int i = 0; i < result.length; i++)
            result[i] = deserialize(array[i], objTypes[i]);
        return result;
    }

    @Override
    public Object[] deserialize(S structure, SerializerAdapter<S>[] adapters, Class<?> typeParameters[][]) throws SerializationException {
        if (structure == null)
            return new Object[0];
        S[] array = toArray(structure);
        Object[] result = new Object[adapters.length];
        if (array.length != result.length)
            throw new SerializationException("Incorrect number of arguments: " + array.length + ", expected: " + result.length);
        for (int i = 0; i < result.length; i++)
            result[i] = deserialize(array[i], adapters[i], typeParameters[i]);
        return result;
    }

    @Override
    public Object[] deserialize(S structure, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws SerializationException {
        if (structure == null)
            return new Object[0];
        S[] array = toArray(structure);
        Object[] result = new Object[objTypes.length];
        if (array.length != result.length)
            throw new SerializationException("Incorrect number of arguments: " + array.length + ", expected: " + result.length);
        for (int i = 0; i < result.length; i++)
            result[i] = deserialize(array[i], objTypes[i], valueDescriptors[i]);
        return result;
    }

    @Override
    public S serialize(Object object, SerializerAdapter<S> adapter) {
        if (object == null)
            return null;
        if (adapter != null)
            return adapter.serialize(this, object);
        return serialize(object);
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public S serialize(Object object, ValueDescriptor valueDescriptor) {
        if (object == null)
            return null;
        return serialize(object, getSerializerAdapter(object.getClass(), valueDescriptor));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public S serialize(Object[] array, ValueDescriptor[] valueDescriptors) {
        if (array == null)
            return null;
        SerializerAdapter[] adapters = new SerializerAdapter[array.length];
        for (int i = 0; i < adapters.length; i++)
            adapters[i] = getSerializerAdapter(array[i].getClass(), valueDescriptors[i]);
        return serialize(array, adapters);
    }

    abstract protected S[] toArray(S object);

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

}
