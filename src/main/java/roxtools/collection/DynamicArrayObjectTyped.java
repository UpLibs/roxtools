package roxtools.collection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

abstract public class DynamicArrayObjectTyped<O> extends DynamicArrayObject<O> {

	public DynamicArrayObjectTyped(int minBlockSize, int maxBlockSize) {
		super(null, minBlockSize, maxBlockSize);
	}

	public DynamicArrayObjectTyped(Class<O> objectType) {
		super(null);
	}
	
	static private Class<?> getDynamicArrayObjectSubclass(Class<?> clazz) {
        Class<?> superclass = clazz.getSuperclass();
        if (superclass.equals(DynamicArrayObjectTyped.class)) {
            return clazz;
        } else if (superclass.equals(Object.class)) {
            return null;
        } else {
            return (getDynamicArrayObjectSubclass(superclass));
        }
    }

	@SuppressWarnings("unchecked")
	static private <T> Class<T> getTypeParameter(Class<?> annotationLiteralSuperclass) {
        Type type = annotationLiteralSuperclass.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getActualTypeArguments().length == 1) {
                return (Class<T>) parameterizedType.getActualTypeArguments()[0];
            }
        }
        return null;
    }
	
	private transient Class<O> objectType;

    protected Class<O> objectType() {
        if (objectType == null) {
            Class<?> annotationLiteralSubclass = getDynamicArrayObjectSubclass(this.getClass());
            if (annotationLiteralSubclass == null) {
                throw new RuntimeException(getClass() + " is not a subclass of DynamicArrayObjectTyped");
            }
            objectType = getTypeParameter(annotationLiteralSubclass);
            if (objectType == null) {
                throw new RuntimeException(getClass() + " does not specify the type parameter O of DynamicArrayObjectTyped<O>");
            }
        }
        return objectType;
    }
	

}
