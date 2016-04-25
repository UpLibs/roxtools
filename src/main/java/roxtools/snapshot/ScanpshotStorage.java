package roxtools.snapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

abstract public class ScanpshotStorage<I extends SnapshotID, S extends Snapshot<I>> {
	
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
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends ScanpshotStorage<I,S>> ScanpshotStorage<I, S> getRegisteredStorage(Class<S> snapshotType) {
		
		synchronized (registeredScanpshotStorage) {
			ScanpshotStorage<?, ?> prevReg = registeredScanpshotStorage.get(snapshotType) ;
			return (ScanpshotStorage<I, S>) prevReg ;
		}
		
	}
	
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends ScanpshotStorage<I,S>> ScanpshotStorage<I, S> registerStorage(String scanpshotStorageClass) {
		return registerStorage(scanpshotStorageClass, true) ;
	}
	
	@SuppressWarnings("unchecked")
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends ScanpshotStorage<I,S>> ScanpshotStorage<I, S> registerStorage(String scanpshotStorageClass, boolean overwrite) {
		Class<T> clazz;
		try {
			clazz = (Class<T>) Class.forName(scanpshotStorageClass, true, ScanpshotStorage.class.getClassLoader());
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException("Can't find storage class to register: "+ scanpshotStorageClass, e) ;
		}
		
		T storage ;
		try {
			storage = clazz.newInstance() ;
		}
		catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("Can't create instance for storage class: "+ scanpshotStorageClass) ;
		}
		
		return registerStorage(storage, overwrite) ;
	}
	
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends ScanpshotStorage<I,S>> ScanpshotStorage<I, S> registerStorage(T scanpshotStorage) {
		return registerStorage(scanpshotStorage, true) ;
	}
	
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends ScanpshotStorage<I,S>> ScanpshotStorage<I, S> registerStorage(T scanpshotStorage, boolean overwrite) {
		return registerStorage(scanpshotStorage, scanpshotStorage.getSnapshotType(), overwrite);
	}
	
	static private final HashMap<Class<?>, ScanpshotStorage<?,?>> registeredScanpshotStorage = new HashMap<>() ; 
	
	static public void clearRegisteredStorages() {
		
		synchronized (registeredScanpshotStorage) {
			registeredScanpshotStorage.clear();
		}
		
	}
	
	static public List<ScanpshotStorage<?, ?>> getRegisteredStorages() {

		synchronized (registeredScanpshotStorage) {
			ArrayList< ScanpshotStorage<?, ?> > list = new ArrayList<>() ;
			
			for (ScanpshotStorage<?, ?> storage : registeredScanpshotStorage.values()) {
				list.add(storage) ;
			}
			
			return list ;
		}
		
	}
	
	static public List<Class<?>> getRegisteredTypes() {
		
		synchronized (registeredScanpshotStorage) {
			ArrayList<Class<?>> list = new ArrayList<>() ;
			
			for (Class<?> clazz : registeredScanpshotStorage.keySet()) {
				list.add(clazz) ;
			}
			
			return list ;
		}
		
	}
	
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends ScanpshotStorage<I,S>> ScanpshotStorage<I, S> registerStorage(T scanpshotStorage, Class<S> snapshotType) {
		return registerStorage(scanpshotStorage, snapshotType, true) ;
	}
	
	@SuppressWarnings("unchecked")
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends ScanpshotStorage<I,S>> ScanpshotStorage<I, S> registerStorage(T scanpshotStorage, Class<S> snapshotType, boolean overwrite) {
		
		synchronized (registeredScanpshotStorage) {
			ScanpshotStorage<?, ?> prevReg = registeredScanpshotStorage.get(snapshotType) ;
			
			registeredScanpshotStorage.put(snapshotType, scanpshotStorage) ;
			
			return (ScanpshotStorage<I, S>) prevReg ;
		}
		
	}
	
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends ScanpshotStorage<I,S>> ScanpshotStorage<I, S> unregisterStorage(T scanpshotStorage) {
		return unregisterStorage(scanpshotStorage, scanpshotStorage.getSnapshotType()) ;
	}
	
	@SuppressWarnings("unchecked")
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends ScanpshotStorage<I,S>> ScanpshotStorage<I, S> unregisterStorage(T scanpshotStorage, Class<S> snapshotType) {
		
		synchronized (registeredScanpshotStorage) {
			return (ScanpshotStorage<I, S>) registeredScanpshotStorage.remove(snapshotType) ;
		}
		
	}
	
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends ScanpshotStorage<I,S>> boolean isRegisteredStorage(T scanpshotStorage) {
		return isRegisteredStorage(scanpshotStorage, scanpshotStorage.getSnapshotType()) ;
	}
	
	static public <I extends SnapshotID, S extends Snapshot<I>, T extends ScanpshotStorage<I,S>> boolean isRegisteredStorage(T scanpshotStorage, Class<S> snapshotType) {
		
		synchronized (registeredScanpshotStorage) {
			ScanpshotStorage<?, ?> prevReg = registeredScanpshotStorage.get(snapshotType) ;
			
			return prevReg == scanpshotStorage ;
		}
		
	}

	
	/////////////////////////////////////////////////////////////////////////////////////////////

	abstract public Class<S> getSnapshotType() ;
	
	abstract public I storeSnapshot( S snapshot ) ;
	
	abstract public S loadSnapshot( I snapshotID ) ;
	
	abstract public byte[] readSnapshotData( I snapshotID ) ;
	
	abstract public boolean containsSnapshot( I snapshotID ) ;
	
}

