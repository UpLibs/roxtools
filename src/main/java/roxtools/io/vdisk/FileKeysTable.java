package roxtools.io.vdisk;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import roxtools.ArrayUtils;
import roxtools.BigLinkedIntListPool;
import roxtools.BigLinkedIntListPool.BigLinkedIntList;
import roxtools.IntTable;

final public class FileKeysTable implements Iterable<Entry<String,int[]>>{
	
	final private VDSector sector ;
	
	private BigLinkedIntListPool listPool ;
	
	public FileKeysTable(VDSector sector) {
		this.sector = sector;
		
		instantiateListPool();
		
		loadAllMetaDataKeys();
	}
	
	private void instantiateListPool() {
		if (this.listPool != null) {
			this.listPool.clearUnreferencedLists() ;
		}
		
		this.listPool = new BigLinkedIntListPool(1, sector.getTotalBlocks()) ;
	}

	public VDSector getSector() {
		return sector;
	}

	private static final int MASK_REMOVE_NEGATIVE_SIGN = 0x7FFFFFFF;
	
	static final private int TOTAL_KEYS_GROUPS = 100 ;
	
	static private int calcKeyGroup(int keyHashcpde) {
		return (keyHashcpde & MASK_REMOVE_NEGATIVE_SIGN) % TOTAL_KEYS_GROUPS ;
	}
	
	static private boolean isKeyFromSameGroup(int keyGroup, int keyHashcode) {
		return keyGroup == calcKeyGroup(keyHashcode) ;
	}
	
	@SuppressWarnings("unchecked")
	final private SoftReference<KeysTable>[] keysTables = new SoftReference[TOTAL_KEYS_GROUPS] ;
	final private IntTable[] keysTablesHascodes = new IntTable[TOTAL_KEYS_GROUPS] ;
	
	public void clearKeysTables() {
		
		synchronized (keysTables) {
			
			int countLostInstances = 0 ;
			
			for (SoftReference<KeysTable> tableRef : keysTables) {
				if (tableRef == null) continue ;
			
				KeysTable table = tableRef.get() ;
				
				if (table != null) {
					table.clear();
				}
				else {
					countLostInstances++ ;
				}
			}
			
			if (countLostInstances > 0) {
				instantiateListPool();
			}
			
			ArrayUtils.clear(keysTables);
			ArrayUtils.clear(keysTablesHascodes);
		}
		
	}
	
	public KeysTable getKeysTable(int keyGroup) {
		synchronized (keysTables) {
			SoftReference<KeysTable> tableRef = keysTables[keyGroup] ;
			
			KeysTable table ;
			
			if (tableRef != null) {
				table = tableRef.get() ;
				if (table == null) {
					table = instantiateTable(keyGroup) ;
				}
				
				if (table != null) return table ;
			}
			
			table = new KeysTable() ;
			
			keysTables[keyGroup] = new SoftReference<KeysTable>(table) ;
			
			return table ;
		}
	}
	
	public KeysTable getKeysTableCleared(int keyGroup) {
		synchronized (keysTables) {
			SoftReference<KeysTable> tableRef = keysTables[keyGroup] ;
			
			KeysTable table ;
			
			if (tableRef != null) {
				table = tableRef.get() ;
				if (table != null) {
					table.clear();
					return table ;
				}
			}
			
			table = new KeysTable() ;
			
			keysTables[keyGroup] = new SoftReference<KeysTable>(table) ;
			
			return table ;
		}
	}
	
	public KeysTable getKeysTableIfExists(int keyGroup) {
		synchronized (keysTables) {
			SoftReference<KeysTable> tableRef = keysTables[keyGroup] ;
			
			KeysTable table ;
			
			if (tableRef != null) {
				table = tableRef.get() ;
				if (table == null) {
					table = instantiateTable(keyGroup) ;
				}
				
				if (table != null) return table ;
			}
			
			return null ;
		}
	}

	public IntTable getKeysTableHashcode(int keyGroup) {
		synchronized (keysTables) {
			IntTable intTable = keysTablesHascodes[keyGroup] ;
			if (intTable != null) return intTable ;
			
			intTable = new IntTable(10, 0.75) ;
			
			keysTablesHascodes[keyGroup] = intTable ;
			
			return intTable ;
		}
	}
	
	public IntTable getKeysTableHashcodeIfExists(int keyGroup) {
		synchronized (keysTables) {
			IntTable intTable = keysTablesHascodes[keyGroup] ;
			return intTable ;
		}
	}
	
	/////////////////////////////////////
	
	private boolean containsKeysHashcodeInMemory(int keyGroup, int keyHashcode) {
		IntTable keysTableHashcode = getKeysTableHashcodeIfExists(keyGroup) ;
		
		if (keysTableHashcode != null) {
			if ( !keysTableHashcode.contains(keyHashcode) ) return false ;
		}
		
		return true ;
	}
	
	public int[] getFileIdent(String key) {
		int keyHashcode = key.hashCode() ;
		int keyGroup = calcKeyGroup(keyHashcode) ;
		
		if ( !containsKeysHashcodeInMemory(keyGroup, keyHashcode) ) return null ;
		
		KeysTable table = getKeysTableIfExists(keyGroup) ;
		if (table == null) return null ;
		
		return table.get(key) ;
	}
	
	public boolean containsFileIdent(String key) {
		int keyHashcode = key.hashCode() ;
		int keyGroup = calcKeyGroup(keyHashcode) ;
		
		if ( !containsKeysHashcodeInMemory(keyGroup, keyHashcode) ) return false ;
		
		KeysTable table = getKeysTableIfExists(keyGroup) ;
		if (table == null) return false ;
		
		return table.containsKey(key) ;
	}
	
	public int[] setFileIdent(String key, int[] ident) {
		int keyHashcode = key.hashCode();
		int keyGroup = calcKeyGroup(keyHashcode) ;
		
		KeysTable table = getKeysTable(keyGroup) ;
		
		int[] prev = table.put(key, ident) ;
		
		IntTable keysTableHashcode = getKeysTableHashcode(keyGroup) ;
		keysTableHashcode.put(keyHashcode);
		
		return prev ;
	}
	
	public int[] removeFileIdent(String key) {
		int keyHashcode = key.hashCode();
		int keyGroup = calcKeyGroup(keyHashcode) ;
		
		KeysTable table = getKeysTableIfExists(keyGroup) ;
		if (table == null) return null ;
		
		int[] prevIdent = table.remove(key) ;
		
		IntTable keysTableHashcode = getKeysTableHashcodeIfExists(keyGroup) ;
		if ( keysTableHashcode != null ) keysTableHashcode.remove( keyHashcode ) ;
		
		return prevIdent ;
	}
	
	/////////////////////////////////////
	
	private class MyIterator {
		
		private int keyGroupCursor = -1 ;
		private Iterator<Entry<String, BigLinkedIntList>> currentTableIterator ;
		
		private boolean prepareNext() {
			while (true) {
				if (currentTableIterator == null) {
					keyGroupCursor++ ;
					
					if ( keyGroupCursor < TOTAL_KEYS_GROUPS ) {
						SoftReference<KeysTable> tableRef = keysTables[keyGroupCursor] ;
						if (tableRef == null) continue ;
						
						KeysTable table = tableRef.get() ;
						
						if (table == null) {
							table = instantiateTable(keyGroupCursor) ;
							if (table == null) continue ;
						}
						
						currentTableIterator = table.entrySet().iterator() ;
					}
					else {
						return false ;
					}
					
				}
				
				if ( currentTableIterator.hasNext() ) {
					return true ;	
				}
				else {
					currentTableIterator = null ;
				}
			}
		}
		
		final public boolean hasNext() {
			return prepareNext() ;
		}

		public Entry<String, BigLinkedIntList> nextImplem() {
			return currentTableIterator.next() ;
		}

		final public void remove() {
			throw new UnsupportedOperationException() ;
		}
		
	}

	final private class MyEntry implements Map.Entry<String, int[]>{
		final private Entry<String, BigLinkedIntList> entry ;
		
		public MyEntry(Entry<String, BigLinkedIntList> entry) {
			this.entry = entry;
		}

		@Override
		public String getKey() {
			return entry.getKey() ;
		}

		@Override
		public int[] getValue() {
			return entry.getValue().toIntArray() ;
		}

		@Override
		public int[] setValue(int[] value) {
			BigLinkedIntList list = listPool.createLinkedList() ;
			list.addAll(value);
			
			BigLinkedIntList prev = entry.setValue(list) ;
			
			if (prev == null) return null ;
			
			int[] data = prev.toIntArray() ;
			prev.clear();
			return data ;
		}
		
		@Override
		public String toString() {
			return getKey()+"="+getValue() ;
		}
	}
	
	final private class MyIteratorEntry extends MyIterator implements Iterator<Entry<String, int[]>> {
		@Override
		public Entry<String, int[]> next() {
			return new MyEntry( nextImplem() ) ;
		}
	}
	
	final private class MyIteratorKey extends MyIterator implements Iterator<String> {
		@Override
		public String next() {
			return nextImplem().getKey() ;
		}
	}
	
	final private class MyIteratorValue extends MyIterator implements Iterator<int[]> {
		@Override
		public int[] next() {
			return nextImplem().getValue().toIntArray() ;
		}
	}
	
	private KeysTable instantiateTable(int keyGroup) {
		Object[] ret = loadAllKeyGroupMetaDataKeys(keyGroup);
		return (KeysTable) ret[0] ;
	}
	
	public int getAllKeysSize() {
		int total = 0 ;
		
		for (int keyGroup = 0; keyGroup < TOTAL_KEYS_GROUPS; keyGroup++) {
			SoftReference<KeysTable> tableRef = keysTables[keyGroup];
			if (tableRef == null) continue ;
			
			KeysTable table = tableRef.get() ;
			
			if (table == null) {
				table = instantiateTable(keyGroup) ;
				if (table == null) continue ;
			}
			
			total += table.size() ;
		}
	
		return total ;
	}
	
	public List<String> getAllKeys() {
		ArrayList<String> keys = new ArrayList<String>() ;
		
		for (int keyGroup = 0; keyGroup < TOTAL_KEYS_GROUPS; keyGroup++) {
			SoftReference<KeysTable> tableRef = keysTables[keyGroup];
			if (tableRef == null) continue ;
			
			KeysTable table = tableRef.get() ;
			
			if (table == null) {
				table = instantiateTable(keyGroup) ;
				if (table == null) continue ;
			}
			
			keys.ensureCapacity( keys.size() + table.size() );
			
			for (String k : table.keySet()) {
				keys.add(k) ;
			}
		}
		
		return keys ;
	}
	
	@Override
	public Iterator<Entry<String, int[]>> iterator() {
		return new MyIteratorEntry() ;
	}
	
	public Iterator<String> iteratorKeys() {
		return new MyIteratorKey() ;
	}
	
	
	public Iterator<int[]> iteratorValues() {
		return new MyIteratorValue() ;
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	@SuppressWarnings("unused")
	final private class KeysTable {
		
		final private HashMap<String, BigLinkedIntList> entries = new HashMap<String, BigLinkedIntList>() ;

		synchronized public void clear() {
			
			for (BigLinkedIntList list : entries.values()) {
				list.clear();
			}
			
			entries.clear();
			
		}
		
		synchronized public int size() {
			return entries.size();
		}
		
		synchronized public boolean isEmpty() {
			return entries.isEmpty();
		}

		synchronized public int[] get(Object key) {
			BigLinkedIntList list = entries.get(key);
			return list != null ? list.toIntArray() : null ;
		}

		synchronized public boolean containsKey(Object key) {
			return entries.containsKey(key);
		}

		synchronized public int[] put(String key, int[] value) {
			BigLinkedIntList list = listPool.createLinkedList() ;
			list.addAll(value);
			BigLinkedIntList prev = entries.put(key, list);
			
			if (prev == null) return null ;
			
			int[] data = prev.toIntArray() ;
			
			prev.clear();
			
			return data ;
		}

		synchronized public int[] remove(Object key) {
			BigLinkedIntList prev = entries.remove(key);
			
			if (prev == null) return null ;
			
			int[] data = prev.toIntArray() ;
			
			prev.clear();
			
			return data ;
		}

		synchronized public Set<String> keySet() {
			return entries.keySet();
		}

		synchronized public Set<Entry<String, BigLinkedIntList>> entrySet() {
			return entries.entrySet();
		}
		
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void loadAllMetaDataKeys() {
		
		VDSector sector = this.sector ;
		
		clearKeysTables(); 
		
		int sz = sector.getTotalBlocks() ;
		
		for (int i = 0; i < sz; i++) {
			VDBlock block = sector.getBlock(i) ;
			
			if ( block != null && block.hasMetaData() ) {
				try {
					String key = block.getMetaDataKey() ;
					
					int keyHashcode = key.hashCode() ;
					int keyGroup = calcKeyGroup(keyHashcode) ;
					
					KeysTable keysTable = getKeysTable(keyGroup) ;
					
					int[] prevIdents = keysTable.get(key) ;
					
					if (prevIdents == null) {
						int[] ident = block.getIdent() ;
						keysTable.put(key, ident) ;
						
						IntTable keysTableHashcode = getKeysTableHashcode(keyGroup) ;
						
						keysTableHashcode.put(keyHashcode) ;
					}
					else {
						int[] idents2 = VDSector.joinIdents(prevIdents, block.getBlockIndex(), block.getSectorIndex()) ;
						if (prevIdents != idents2) {
							keysTable.put(key, idents2) ;
						}
					}
					
				}
				catch (IOException e) {
					e.printStackTrace() ;
				}
			}
		}
		
	}
	
	private Object[] loadAllKeyGroupMetaDataKeys(int keyGroup) {
		
		VDSector sector = this.sector ;
		
		KeysTable keysTable = getKeysTableCleared(keyGroup) ;
		IntTable keysTableHashcode = getKeysTableHashcode(keyGroup) ;
		
		keysTableHashcode.clear();
		
		int sz = sector.getTotalBlocks() ;
		
		for (int i = 0; i < sz; i++) {
			VDBlock block = sector.getBlock(i) ;
			
			if ( block != null && block.hasMetaData() ) {
				try {
					String key = block.getMetaDataKey() ;
					int keyHashcode = key.hashCode() ;
					
					if ( !isKeyFromSameGroup(keyGroup, keyHashcode) ) continue ; 
					
					int[] prevIdents = keysTable.get(key) ;
					
					if (prevIdents == null) {
						int[] ident = block.getIdent() ;
						keysTable.put(key, ident) ;	
						keysTableHashcode.put(keyHashcode) ;
					}
					else {
						int[] idents2 = VDSector.joinIdents(prevIdents, block.getBlockIndex(), block.getSectorIndex()) ;
						if (prevIdents != idents2) {
							keysTable.put(key, idents2) ;
						}
					}
					
				}
				catch (IOException e) {
					e.printStackTrace() ;
				}
			}
		}
		
		return new Object[] { keysTable , keysTableHashcode } ;
	}
	
	////////////////////////////////////////////////////////////////////////////
	

	synchronized protected void notifyMetaDataKeyChange(String key, int blockIndex, int blockSector) {
		int keyHashcode = key.hashCode() ;
		int keyGroup = calcKeyGroup(keyHashcode) ;
		
		KeysTable keysTable = getKeysTable(keyGroup) ;
		IntTable keysTableHashcode = getKeysTableHashcode(keyGroup) ;
		
		int[] prevIdents = keysTable.get(key) ;
		
		if (prevIdents == null) {
			keysTable.put(key, new int[] {blockIndex , blockSector}) ;
			keysTableHashcode.put( keyHashcode ) ;
		}
		else {
			int[] idents2 = VDSector.joinIdents(prevIdents, blockIndex , blockSector) ;
			if (prevIdents != idents2) keysTable.put(key, idents2) ;
		}
	}
	
	synchronized protected void notifyMetaDataKeyRemove(String key, int blockIndex, int blockSector) {
		int keyHashcode = key.hashCode();
		int keyGroup = calcKeyGroup(keyHashcode) ;
		
		KeysTable keysTable = getKeysTableIfExists(keyGroup) ;
		IntTable keysTableHashcode = getKeysTableHashcodeIfExists(keyGroup) ;
		
		if (keysTable == null) {
			if (keysTableHashcode != null) keysTableHashcode.remove( keyHashcode ) ;
			return ;
		}
		
		int[] prevIdents = keysTable.get(key) ;
		
		if (prevIdents != null) {
			if (keysTableHashcode != null) keysTableHashcode.remove( keyHashcode ) ;
			
			prevIdents = VDSector.removeIdent(prevIdents, blockIndex, blockSector) ;
			
			if ( prevIdents.length > 0 ) {
				keysTable.put(key, prevIdents) ;
			}
			else {
				keysTable.remove(key) ;
			}
		}
		
	}

	/////////////////////////////////////////////////////////////

	public void close() {

		clearKeysTables();
		
	}

}
