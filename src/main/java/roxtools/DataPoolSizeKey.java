package roxtools;

import java.util.Arrays;

abstract public class DataPoolSizeKey {
	
	@Override
	abstract public boolean equals(Object obj) ;
	@Override
	abstract public int hashCode() ;
	
	

	final static public class DataPoolSizeKeyInt extends DataPoolSizeKey {
		final int size ;

		public DataPoolSizeKeyInt(int size) {
			this.size = size;
		}
		
		public int getSize() {
			return size;
		}

		@Override
		public int hashCode() {
			return size ;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			
			if (getClass() != obj.getClass()) return false;
			
			DataPoolSizeKeyInt other = (DataPoolSizeKeyInt) obj;
			if (size != other.size) return false;
			return true;
		}
	}
	
	final static public class DataPoolSizeKeyIntPair extends DataPoolSizeKey {
		final int size1 ;
		final int size2 ;

		public DataPoolSizeKeyIntPair(int size1, int size2) {
			this.size1 = size1;
			this.size2 = size2;
		}

		public int getSize1() {
			return size1;
		}
		
		public int getSize2() {
			return size2;
		}
		
		@Override
		public int hashCode() {
			return size1 ^ size2 ;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			
			if (getClass() != obj.getClass()) return false;
			
			DataPoolSizeKeyIntPair other = (DataPoolSizeKeyIntPair) obj;
			if (size1 != other.size1) return false;
			if (size2 != other.size2) return false;
			
			return true;
		}
	}
	
	final static public class DataPoolSizeKeyIntArray extends DataPoolSizeKey {
		final int[] sizes ;

		public DataPoolSizeKeyIntArray(int... sizes) {
			this.sizes = sizes;
		}

		public int[] getSizes() {
			return sizes;
		}
		
		private int hashcode = 0 ;
		
		@Override
		public int hashCode() {
			if (hashcode == 0) {
				hashcode = Arrays.hashCode(sizes) ;
			}
			return hashcode ;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			
			if (getClass() != obj.getClass()) return false;
			
			DataPoolSizeKeyIntArray other = (DataPoolSizeKeyIntArray) obj;
			
			if (hashCode() != other.hashCode()) return false;
			
			return Arrays.equals(this.sizes, other.sizes);
		}
	}
	
}