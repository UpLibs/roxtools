package roxtools;

import java.util.Arrays;

final public class LongTable {
	
	private long[][] table ;
	private int[] tableSizes ;
	private int size ;
	
	private int threshold ;
	
	static private final int MAXIMUM_CAPACITY = 1 << 30;
	static private final int DEFAULT_TABLE_GROUP_SIZE = 30 ;
	static private final float DEFAULT_TABLE_LOAD_FACTOR = 0.75f ;
	
	public LongTable() {
		this(8) ;
	}
	
	public LongTable(int initialCapacity) {
		if (initialCapacity < 4) initialCapacity = 4 ;
		
		int capacity = 1;
        while (capacity < initialCapacity) capacity <<= 1;
		
		this.table = new long[capacity][DEFAULT_TABLE_GROUP_SIZE] ;
		this.tableSizes = new int[capacity] ;
		
		this.threshold = calcThreshold(capacity) ;
		
		//System.out.println("-- init threshold> "+ this.threshold);
	}
	
	static private int calcThreshold(int capacity) {
		return (int) Math.min(capacity * DEFAULT_TABLE_GROUP_SIZE*DEFAULT_TABLE_LOAD_FACTOR , MAXIMUM_CAPACITY + 1);
	}
	
	private void reHash(int newTableSize) {
		
		long[][] table2 = new long[newTableSize][DEFAULT_TABLE_GROUP_SIZE] ;
		int[] table2Sizes = new int[newTableSize] ;
		
		//int maxLoad = 0 ;
		//int minLoad = Integer.MAX_VALUE ;
		//long averageLoad = 0 ;
		
		//int totalAllocated = table.length * DEFAULT_TABLE_GROUP_SIZE ;
		
		for (int i = 0; i < table.length; i++) {
			long[] group = table[i] ;
			int groupSz = tableSizes[i] ;
			
			//if (groupSz > maxLoad) maxLoad = groupSz ;
			//if (groupSz < minLoad) minLoad = groupSz ;
			//averageLoad += groupSz ;
			
			for (int j = groupSz-1; j >= 0; j--) {
				long v = group[j] ;
				
				int hash = hash(v) ;
				int tableIdx = tableIndexFor(hash, newTableSize) ;
				
				long[] group2 = table2[tableIdx] ;
				int group2sz = table2Sizes[tableIdx] ;
				
				if (group2sz == group2.length) {
					group2 = Arrays.copyOf(group2, group2sz+DEFAULT_TABLE_GROUP_SIZE) ;
					table2[tableIdx] = group2 ;
				}
				
				group2[group2sz++] = v ;
				table2Sizes[tableIdx] = group2sz ;
			}
		}
		
		//averageLoad /= table.length ;
		
		//System.out.println("-- reHash> size: "+ size +" > table size: "+ this.table.length +" > threshold> "+ this.threshold +" >> totalAllocated: "+ totalAllocated +" > average load: "+ averageLoad +" -> "+ minLoad +" .. "+ maxLoad +" > "+ ( averageLoad * table.length ) );
		
		this.table = table2 ;
		this.tableSizes = table2Sizes ;
		
		this.threshold = calcThreshold( newTableSize ) ;
		
		
	}
	
	public int size() {
		return size;
	}
	
	public void put(int n) {
		int hash = hash(n) ;
		
		int tableIdx = tableIndexFor(hash, table.length) ;
		
		long[] group = table[tableIdx] ;
		int groupSize = tableSizes[tableIdx] ;
		
		for (int i = groupSize-1; i >= 0; i--) {
			long v = group[i] ;
			if (v == n) return ;
		}
		
		if (size >= threshold) {
			reHash( table.length * 2 ) ;
			
			tableIdx = tableIndexFor(hash, table.length) ;
			
			group = table[tableIdx] ;
			groupSize = tableSizes[tableIdx] ;
		}
		
		if (groupSize == group.length) {
			group = Arrays.copyOf(group, groupSize+DEFAULT_TABLE_GROUP_SIZE) ;
			table[tableIdx] = group ;
		}
		
		group[groupSize++] = n ;
		tableSizes[tableIdx] = groupSize ;
		
		//System.out.println("-- put> "+ tableIdx +" > "+ groupSize);
		
		this.size++ ;
	}
	
	public boolean contains(int n) {
		int hash = hash(n) ;
		
		int tableIdx = tableIndexFor(hash, table.length) ;
		
		long[] group = table[tableIdx] ;
		int groupSize = tableSizes[tableIdx] ;
		
		for (int i = groupSize-1; i >= 0; i--) {
			long v = group[i] ;
			if (v == n) return true ;
		}
		
		return false ;
	}
	
	public boolean remove(int n) {
		int hash = hash(n) ;
		
		int tableIdx = tableIndexFor(hash, table.length) ;
		
		long[] group = table[tableIdx] ;
		int groupSize = tableSizes[tableIdx] ;
		
		for (int i = groupSize-1; i >= 0; i--) {
			long v = group[i] ;
			if (v == n) {
				
				System.arraycopy(group, i+1, group, i, (groupSize-(i+1))) ;
				tableSizes[tableIdx] = groupSize-1 ;
				
				return true ;
			}
		}
		
		return false ;
	}
	
	static int tableIndexFor(int h, int length) {
        return h & (length-1);
    }
	
	final int hash(long l) {
		int h = (int)( l ^ (l >>> 32) ) ;
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
	
	public void clear() {
		clear(8) ;
	}
	
	public void clear(int initialCapacity) {
		if (initialCapacity < 4) initialCapacity = 4 ;
		
		int capacity = 1;
        while (capacity < initialCapacity) capacity <<= 1;
		
		this.table = new long[capacity][DEFAULT_TABLE_GROUP_SIZE] ;
		this.tableSizes = new int[capacity] ;
		
		this.threshold = calcThreshold(capacity) ;
		
	}
	
	public long[] getValues() {
		long[] values = new long[size] ;
		int valuesSz = 0 ;
		
		for (int i = table.length-1; i >= 0 ; i--) {
			long[] group = table[i] ;
			int groupSz = tableSizes[i] ;
			
			for (int j = groupSz-1; j >= 0 ; j--) {
				long v = group[j] ;
				values[valuesSz++] = v ;
			}
		}
		
		return values ;
	}
	
	public void iterateValues(Iteration iteration) {
		
		int index = 0 ;
		
		for (int i = table.length-1; i >= 0 ; i--) {
			long[] group = table[i] ;
			int groupSz = tableSizes[i] ;
			
			for (int j = groupSz-1; j >= 0 ; j--) {
				long v = group[j] ;
				
				iteration.iterate(this, v, index++, size) ;
			}
		}
		
	}
	
	static public interface Iteration {
		public void iterate(LongTable table, long value, int index, int tableSize) ;
	}
	
	public long getAllocatedMemory() {
		
		int alloc = 4 + (table.length * 4) ;
		
		for (int i = table.length-1; i >= 0 ; i--) {
			long[] group = table[i] ;
			alloc += 4 + (group.length * 4) ;
		}
		

		alloc += 4 + (tableSizes.length * 4) ;
		
		return alloc ;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder() ;
		
		str.append( this.getClass().getName() ) ;
		
		str.append("[size: "+ size +" ; table groups: "+ table.length +" ; allocatedMem: "+ getAllocatedMemory() +"]") ;
		
		return str.toString();
	}
	
	/////////////////////////////////////////////////////////////////
	
	
	@SuppressWarnings({ "unused" })
	public static void main(String[] args) {
		
		if (false) {
			System.out.println("Bench...");
			
			LongTable intTable = new LongTable() ;
			
			int loops = 4000 ;
			
			int inserts = 10000 ;
			
			int totalInserts = loops * inserts ;
			
			long time = System.currentTimeMillis() ;
			
			for (int l = 0; l < loops ; l++) {
				int base = l*inserts ;
				
				for (int i = inserts-1 ; i >= 0 ; i--) {
					intTable.put(base+i) ;
				}
				
			}
			
			time = System.currentTimeMillis() - time ;
			
			System.out.println("TIME: "+ time);
			System.out.println("totalInserts> "+ totalInserts);
			System.out.println(intTable);
			
			try { Thread.sleep(1000000) ;} catch (InterruptedException e) {}
			
			return ;
		}
		
		LongTable intTable = new LongTable() ;
		
		System.out.println("size> "+ intTable.size());
		
		int totalToAdd = 1000 ;
		
		for (int i = 10; i < totalToAdd; i++) {
			intTable.put(i) ;
			System.out.println(i+"> size> "+ intTable.size());
		}
		
		System.out.println("--------------------------------------------------");
		
		for (int i = 0; i < totalToAdd+10; i++) {
			boolean ok = intTable.contains(i) ;
			System.out.println(i+"> "+ ok);
		}
		
		System.out.println("--------------------------------------------------");
		
		intTable.remove(100) ;
		
		long[] values = intTable.getValues() ;
		
		Arrays.sort(values) ;
		
		for (int i = 0; i < values.length; i++) {
			long v = values[i];
			System.out.println(i+"> "+ v);
		}
		
		
	}

}
