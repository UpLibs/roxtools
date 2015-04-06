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

import roxtools.BigLinkedIntListPool;
import roxtools.BigLinkedIntListPool.BigLinkedIntList;
import roxtools.IntTable;

final public class FileKeysTable implements Iterable<Entry<String,int[]>>{
	
	final private VDSector sector ;
	
	final private BigLinkedIntListPool listPool = new BigLinkedIntListPool(1, 2000) ;
	
	public FileKeysTable(VDSector sector) {
		this.sector = sector;
		
		loadAllMetaDataKeys();
	}

	public VDSector getSector() {
		return sector;
	}
	
	static final class KeyGroup {
		private final int group ;
		
		public KeyGroup(String key) {
			this.group = calcKeyGroup(key) ;
		}
		
		static private int calcKeyGroup(String key) {
			return key.hashCode() % 100 ;
		}

		@Override
		public int hashCode() {
			return group ;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			
			if (getClass() != obj.getClass()) return false;
			
			KeyGroup other = (KeyGroup) obj;
			
			if (group != other.group) return false;
			return true;
		}
		
		public boolean isKeyFromSameGroup(String key) {
			return this.group == calcKeyGroup(key) ;
		}
	}
	
	final private HashMap<KeyGroup, SoftReference<KeysTable>> keysTables = new HashMap<KeyGroup, SoftReference<KeysTable>>() ;
	final private HashMap<KeyGroup, IntTable> keysTablesHascodes = new HashMap<KeyGroup, IntTable>() ;
	
	public void clearKeysTables() {
		
		synchronized (keysTables) {
			
			for (Entry<KeyGroup, SoftReference<KeysTable>> entry : keysTables.entrySet()) {
				SoftReference<KeysTable> tableRef = entry.getValue();
				
				KeysTable table = tableRef.get() ;
				if (table == null) {
					table = instantiateTable( entry.getKey() ) ;
				}
				
				table.clear();
			}
			
			keysTables.clear();
			keysTablesHascodes.clear();
			
		}
		
	}
	
	public KeysTable getKeysTable(KeyGroup keyGroup) {
		synchronized (keysTables) {
			SoftReference<KeysTable> tableRef = keysTables.get(keyGroup) ;
			
			KeysTable table ;
			
			if (tableRef != null) {
				table = tableRef.get() ;
				if (table == null) {
					table = instantiateTable(keyGroup) ;
				}
				
				if (table != null) return table ;
			}
			
			table = new KeysTable() ;
			
			keysTables.put(keyGroup, new SoftReference<KeysTable>(table)) ;
			
			return table ;
		}
	}
	
	public KeysTable getKeysTableIfExists(KeyGroup keyGroup) {
		synchronized (keysTables) {
			SoftReference<KeysTable> tableRef = keysTables.get(keyGroup) ;
			
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

	public IntTable getKeysTableHashcode(KeyGroup keyGroup) {
		synchronized (keysTables) {
			IntTable intTable = keysTablesHascodes.get(keyGroup) ;
			if (intTable != null) return intTable ;
			
			intTable = new IntTable() ;
			
			keysTablesHascodes.put(keyGroup, intTable) ;
			
			return intTable ;
		}
	}
	
	public IntTable getKeysTableHashcodeIfExists(KeyGroup keyGroup) {
		synchronized (keysTables) {
			IntTable intTable = keysTablesHascodes.get(keyGroup) ;
			return intTable ;
		}
	}
	
	/////////////////////////////////////
	
	private boolean containsKeysHashcodeInMemory(KeyGroup keyGroup, String key) {
		IntTable keysTableHashcode = getKeysTableHashcodeIfExists(keyGroup) ;
		
		if (keysTableHashcode != null) {
			if ( !keysTableHashcode.contains(key.hashCode()) ) return false ;
		}
		
		return true ;
	}
	
	public int[] getFileIdent(String key) {
		KeyGroup keyGroup = new KeyGroup(key) ;
		
		if ( !containsKeysHashcodeInMemory(keyGroup, key) ) return null ;
		
		KeysTable table = getKeysTableIfExists(keyGroup) ;
		if (table == null) return null ;
		
		return table.get(key) ;
	}
	
	public boolean containsFileIdent(String key) {
		KeyGroup keyGroup = new KeyGroup(key) ;
		
		if ( !containsKeysHashcodeInMemory(keyGroup, key) ) return false ;
		
		KeysTable table = getKeysTableIfExists(keyGroup) ;
		if (table == null) return false ;
		
		return table.containsKey(key) ;
	}
	
	public int[] setFileIdent(String key, int[] ident) {
		KeyGroup keyGroup = new KeyGroup(key) ;
		
		KeysTable table = getKeysTable(keyGroup) ;
		
		int[] prev = table.put(key, ident) ;
		
		IntTable keysTableHashcode = getKeysTableHashcode(keyGroup) ;
		keysTableHashcode.put(key.hashCode());
		
		return prev ;
	}
	
	public int[] removeFileIdent(String key) {
		KeyGroup keyGroup = new KeyGroup(key) ;
		
		KeysTable table = getKeysTableIfExists(keyGroup) ;
		if (table == null) return null ;
		
		int[] prevIdent = table.remove(key) ;
		
		IntTable keysTableHashcode = getKeysTableHashcodeIfExists(keyGroup) ;
		if ( keysTableHashcode != null ) keysTableHashcode.remove( key.hashCode() ) ;
		
		return prevIdent ;
	}
	
	/////////////////////////////////////
	
	private class MyIterator {
		
		final private Iterator<Entry<KeyGroup, SoftReference<KeysTable>>> tablesIterator = keysTables.entrySet().iterator() ;
		private Iterator<Entry<String, BigLinkedIntList>> currentTableIterator ;
		
		private boolean prepareNext() {
			while (true) {
				if (currentTableIterator == null) {
					
					if ( tablesIterator.hasNext() ) {
						Entry<KeyGroup, SoftReference<KeysTable>> currentEntry = tablesIterator.next() ;
						
						SoftReference<KeysTable> tableRef = currentEntry.getValue() ;
						KeysTable table = tableRef.get() ;
						
						if (table == null) {
							KeyGroup keyGroup = currentEntry.getKey() ;
							table = instantiateTable(keyGroup) ;
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
	
	private KeysTable instantiateTable(KeyGroup keyGroup) {
		Object[] ret = loadAllKeyGroupMetaDataKeys(keyGroup);
		return (KeysTable) ret[0] ;
	}
	
	public int getAllKeysSize() {
		int total = 0 ;
		
		for (Entry<KeyGroup, SoftReference<KeysTable>> entry : keysTables.entrySet()) {
			SoftReference<KeysTable> tableRef = entry.getValue() ;
			KeysTable table = tableRef.get() ;
			
			if (table == null) {
				KeyGroup keyGroup = entry.getKey() ;
				table = instantiateTable(keyGroup) ;
				if (table == null) continue ;
			}
			
			total += table.size() ;
		}
	
		return total ;
	}
	
	public List<String> getAllKeys() {
		ArrayList<String> keys = new ArrayList<String>( getAllKeysSize() ) ;
		
		for (Entry<KeyGroup, SoftReference<KeysTable>> entry : keysTables.entrySet()) {
			SoftReference<KeysTable> tableRef = entry.getValue() ;
			KeysTable table = tableRef.get() ;
			
			if (table == null) {
				KeyGroup keyGroup = entry.getKey() ;
				table = instantiateTable(keyGroup) ;
				if (table == null) continue ;
			}
			
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
					
					KeyGroup keyGroup = new KeyGroup(key) ;
					
					KeysTable keysTable = getKeysTable(keyGroup) ;
					
					int[] prevIdents = keysTable.get(key) ;
					
					if (prevIdents == null) {
						int[] ident = block.getIdent() ;
						keysTable.put(key, ident) ;
						
						IntTable keysTableHashcode = getKeysTableHashcode(keyGroup) ;
						
						keysTableHashcode.put(key.hashCode()) ;
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
	
	private Object[] loadAllKeyGroupMetaDataKeys(KeyGroup keyGroup) {
		
		VDSector sector = this.sector ;
		
		KeysTable keysTable = getKeysTable(keyGroup) ;
		IntTable keysTableHashcode = getKeysTableHashcode(keyGroup) ;
		
		keysTable.clear();
		keysTableHashcode.clear();
		
		int sz = sector.getTotalBlocks() ;
		
		for (int i = 0; i < sz; i++) {
			VDBlock block = sector.getBlock(i) ;
			
			if ( block != null && block.hasMetaData() ) {
				try {
					String key = block.getMetaDataKey() ;
					
					if ( !keyGroup.isKeyFromSameGroup(key) ) continue ; 
					
					int[] prevIdents = keysTable.get(key) ;
					
					if (prevIdents == null) {
						int[] ident = block.getIdent() ;
						keysTable.put(key, ident) ;	
						keysTableHashcode.put(key.hashCode()) ;
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
		KeyGroup keyGroup = new KeyGroup(key) ;
		
		KeysTable keysTable = getKeysTable(keyGroup) ;
		IntTable keysTableHashcode = getKeysTableHashcode(keyGroup) ;
		
		int[] prevIdents = keysTable.get(key) ;
		
		if (prevIdents == null) {
			keysTable.put(key, new int[] {blockIndex , blockSector}) ;
			keysTableHashcode.put( key.hashCode() ) ;
		}
		else {
			int[] idents2 = VDSector.joinIdents(prevIdents, blockIndex , blockSector) ;
			if (prevIdents != idents2) keysTable.put(key, idents2) ;
		}
	}
	
	synchronized protected void notifyMetaDataKeyRemove(String key, int blockIndex, int blockSector) {
		KeyGroup keyGroup = new KeyGroup(key) ;
		
		KeysTable keysTable = getKeysTableIfExists(keyGroup) ;
		IntTable keysTableHashcode = getKeysTableHashcodeIfExists(keyGroup) ;
		
		if (keysTable == null) {
			if (keysTableHashcode != null) keysTableHashcode.remove( key.hashCode() ) ;
			return ;
		}
		
		int[] prevIdents = keysTable.get(key) ;
		
		if (prevIdents != null) {
			if (keysTableHashcode != null) keysTableHashcode.remove( key.hashCode() ) ;
			
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
