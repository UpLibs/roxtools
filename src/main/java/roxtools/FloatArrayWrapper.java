package roxtools;

import java.util.Arrays;

final public class FloatArrayWrapper {
	
	private float[] values ;

	public FloatArrayWrapper(float[] values) {
		this.values = values;
	}

	public float[] getValues() {
		return values;
	}
	
	private int hashcode = 0 ;
	@Override
	public int hashCode() {
		if (hashcode == 0) {
			hashcode = Arrays.hashCode(values);
		}
		return hashcode ;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		FloatArrayWrapper other = (FloatArrayWrapper) obj;
		
		return Arrays.equals(values, other.values) ;
	}
	
	@Override
	public String toString() {
		return "FloatArrayWrapper"+ Arrays.toString(values) ;
	}
	
	public String toValuesString(String delimiter) {
		StringBuilder str = new StringBuilder() ;
		
		for (int i = 0; i < values.length; i++) {
			float f = values[i];
			
			if (i > 0) str.append(delimiter) ;
			
			if (f == 0) str.append("0") ;
			else if (f == 1) str.append("1") ;
			else str.append(f) ;
		}
		
		return str.toString() ;
	}
	
}