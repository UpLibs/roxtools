package roxtools.snapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

abstract public class SnapshotStorage<I extends SnapshotID, S extends Snapshot<I>> {
	
	static final public String PROPERTY_REGISTER_STORAGE_PREFIX = "roxtools.snapshot.storage."; 
	
	static {
		registerStoragesFromProperties();
	}
	
	static public void registerStoragesFromProperties() {
		Properties properties = System.getProperties() ;
		
		for (Object obj : properties.keySet()) {
			String key = obj.toString() ;
			
			if (key.startsWith(PROPERTY_REGISTER_STORAGE_PREFIX)) {
				Object objVal = properties.get(obj) ;
				String val = objVal != null ? objVal.toString().trim() : null ;
				
				if ( val != null && !val.isEmpty() ) {
					try {
						registerStorage(val, false) ;
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends SnapshotStorage<I,S>> SnapshotStorage<I, S> getRegisteredStorage(Class<S> snapshotType) {
		
		synchronized (registeredSnapshotStorage) {
			SnapshotStorage<?, ?> prevReg = registeredSnapshotStorage.get(snapshotType) ;
			return (SnapshotStorage<I, S>) prevReg ;
		}
		
	}
	
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends SnapshotStorage<I,S>> SnapshotStorage<I, S> registerStorage(String snapshotStorageClass) {
		return registerStorage(snapshotStorageClass, true) ;
	}
	
	@SuppressWarnings("unchecked")
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends SnapshotStorage<I,S>> SnapshotStorage<I, S> registerStorage(String snapshotStorageClass, boolean overwrite) {
		Class<T> clazz;
		try {
			clazz = (Class<T>) Class.forName(snapshotStorageClass, true, SnapshotStorage.class.getClassLoader());
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException("Can't find storage class to register: "+ snapshotStorageClass, e) ;
		}
		
		T storage ;
		try {
			storage = clazz.newInstance() ;
		}
		catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("Can't create instance for storage class: "+ snapshotStorageClass) ;
		}
		
		return registerStorage(storage, overwrite) ;
	}
	
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends SnapshotStorage<I,S>> SnapshotStorage<I, S> registerStorage(T snapshotStorage) {
		return registerStorage(snapshotStorage, true) ;
	}
	
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends SnapshotStorage<I,S>> SnapshotStorage<I, S> registerStorage(T snapshotStorage, boolean overwrite) {
		return registerStorage(snapshotStorage, snapshotStorage.getSnapshotType(), overwrite);
	}
	
	static private final HashMap<Class<?>, SnapshotStorage<?,?>> registeredSnapshotStorage = new HashMap<>() ; 
	
	static public void clearRegisteredStorages() {
		
		synchronized (registeredSnapshotStorage) {
			registeredSnapshotStorage.clear();
		}
		
	}
	
	static public List<SnapshotStorage<?, ?>> getRegisteredStorages() {

		synchronized (registeredSnapshotStorage) {
			ArrayList< SnapshotStorage<?, ?> > list = new ArrayList<>() ;
			
			for (SnapshotStorage<?, ?> storage : registeredSnapshotStorage.values()) {
				list.add(storage) ;
			}
			
			return list ;
		}
		
	}
	
	static public List<Class<?>> getRegisteredTypes() {
		
		synchronized (registeredSnapshotStorage) {
			ArrayList<Class<?>> list = new ArrayList<>() ;
			
			for (Class<?> clazz : registeredSnapshotStorage.keySet()) {
				list.add(clazz) ;
			}
			
			return list ;
		}
		
	}
	
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends SnapshotStorage<I,S>> SnapshotStorage<I, S> registerStorage(T snapshotStorage, Class<S> snapshotType) {
		return registerStorage(snapshotStorage, snapshotType, true) ;
	}
	
	@SuppressWarnings("unchecked")
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends SnapshotStorage<I,S>> SnapshotStorage<I, S> registerStorage(T snapshotStorage, Class<S> snapshotType, boolean overwrite) {
		
		synchronized (registeredSnapshotStorage) {
			SnapshotStorage<?, ?> prevReg = registeredSnapshotStorage.get(snapshotType) ;
			
			registeredSnapshotStorage.put(snapshotType, snapshotStorage) ;
			
			return (SnapshotStorage<I, S>) prevReg ;
		}
		
	}
	
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends SnapshotStorage<I,S>> SnapshotStorage<I, S> unregisterStorage(T snapshotStorage) {
		return unregisterStorage(snapshotStorage, snapshotStorage.getSnapshotType()) ;
	}
	
	@SuppressWarnings("unchecked")
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends SnapshotStorage<I,S>> SnapshotStorage<I, S> unregisterStorage(T snapshotStorage, Class<S> snapshotType) {
		
		synchronized (registeredSnapshotStorage) {
			return (SnapshotStorage<I, S>) registeredSnapshotStorage.remove(snapshotType) ;
		}
		
	}
	
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends SnapshotStorage<I,S>> boolean isRegisteredStorage(T snapshotStorage) {
		return isRegisteredStorage(snapshotStorage, snapshotStorage.getSnapshotType()) ;
	}
	
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends SnapshotStorage<I,S>> boolean isRegisteredStorage(T snapshotStorage, Class<S> snapshotType) {
		
		synchronized (registeredSnapshotStorage) {
			SnapshotStorage<?, ?> prevReg = registeredSnapshotStorage.get(snapshotType) ;
			
			return prevReg == snapshotStorage ;
		}
		
	}

	
	/////////////////////////////////////////////////////////////////////////////////////////////

	abstract public Class<S> getSnapshotType() ;
	
	abstract public I storeSnapshot( S snapshot ) ;
	
	abstract public S loadSnapshot( I snapshotID , boolean ignoreTimeAndGetLatest ) ;
	
	abstract public byte[] readSnapshotData( I snapshotID , boolean ignoreTimeAndGetLatest ) ;
	
	abstract public boolean containsSnapshot( I snapshotID , boolean ignoreTimeAndGetLatest ) ;
	
}

