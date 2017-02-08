package org.zenframework.easyservices.serialize.json.gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.zenframework.easyservices.serialize.SerializationException;

import com.google.gson.reflect.TypeToken;

public class GsonUtil {

    private GsonUtil() {}

    public static Type getParameterizedType(Class<?> rawType, Class<?>... typeParameters) throws SerializationException {
        return getParameterizedType(rawType, new ArrayList<Class<?>>(Arrays.asList(typeParameters)));
    }

    public static Type getParameterizedType(Class<?> rawType, List<Class<?>> typeParameters) throws SerializationException {
        int rawTypeParametersCount = rawType.getTypeParameters().length;
        if (rawTypeParametersCount == 0)
            return rawType;
        Type[] types = new Type[rawTypeParametersCount];
        for (int i = 0; i < rawTypeParametersCount; i++) {
            if (typeParameters.size() == 0)
                throw new SerializationException("Incorrect type parameters");
            types[i] = getParameterizedType(typeParameters.remove(0), typeParameters);
        }
        return TypeToken.getParameterized(rawType, types).getType();
    }

}
