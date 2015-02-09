package roxtools;

/**
 * 
 * This is an optimized binary search, by Graciliano Monteiro Passos.
 * 
 * <p>It generally finds the key in a sorted set from 20% to 50% steps of a classic binary search.
 * 
 * <p>Each steps is more complex than classic binary search, but is
 * generally faster, and shouldn't be slower:
 * 
 * <p>Speed: 42% faster than classical binary search (30% less time).
 * 
 * <p>The main idea of this algorithm is to do a search in steps,
 * and for each step changing the current area to look, defining a pivot
 * to divide this area, and check if the key will be in the lower or
 * higher area, then continue to the next step changing the look area
 * based on that, until find the key (or the insertion point).
 * This principle is the same of a classical binary search, but here
 * the pivot in each steps is not the median value, instead the low
 * and high value of the look area are used to define the scale of
 * the look area, and based on that calculate the most probable
 * position of the key in the look area (pivot index). This works
 * because the values of the look area generally have a normal
 * distribution.
 * 
 * @author Graciliano Monteiro Passos (gracilianomp@gmail.com)
 *
 */
final public class BinaryScaledSearch {

	////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Searches the specified array for the specified key using the
     * binary scaled search algorithm (by G.M.P.).
     * 
     * The array must be sorted prior to making this call. If it
     * is not sorted, the results are undefined.
     * 
     * If the array contains multiple elements with the specified value,
     * there is no guarantee which one will be found.
     *
     * @param a the array to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element greater than the key, or <tt>a.length</tt> if all
     *         elements in the array are less than the specified key. Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     */
	public static int search(int[] a, int key) {
		return search(a, 0, a.length, key) ;
	}
	

	public static int search(int[] a, int fromIndex, int toIndex, int key) {
		int low = fromIndex;
		int high = toIndex - 1;

		int lowVal = a[low] ;
		if (key < lowVal) return -1 ;
		
		int highVal = a[high] ;
		if (key > highVal) return -(high+2) ;
		
		int area = (high-low) ;
		int scale = highVal-lowVal ;
		
		int pivotScaledIdx = low + ( ( area * (key-lowVal) ) / scale ) ;
		int pivotScaledVal = a[pivotScaledIdx] ;
		
		if ( pivotScaledVal < key ) {
			int stepSz = (high - pivotScaledIdx) / 100 ;
			if (stepSz < 2) stepSz = 2 ;
			
			do {
				int pivotScaledNextIdx = pivotScaledIdx + stepSz ;
				if (pivotScaledNextIdx > high) break ;
				
				int pivotScaledNextVal = a[pivotScaledNextIdx] ;
				
				if (pivotScaledNextVal < key) {
					pivotScaledIdx = pivotScaledNextIdx ; 
				}
				else if (pivotScaledNextVal > key) {
					return binarySearchOriginal(a, pivotScaledIdx+1 , pivotScaledNextIdx, key) ;
				}
				else {
					return pivotScaledNextIdx ;
				}
			}
			while (true) ;
			
			return binarySearchOriginal(a, pivotScaledIdx+1 , high+1, key) ;
		}
		else if ( pivotScaledVal > key ) {
			int stepSz = (pivotScaledIdx-low) / 100 ;
			if (stepSz < 2) stepSz = 2 ;
			
			do {
				int pivotScaledNextIdx = pivotScaledIdx - stepSz ;
				if (pivotScaledNextIdx < 0) break ;
				
				int pivotScaledNextVal = a[pivotScaledNextIdx] ;
				
				if (pivotScaledNextVal > key) {
					pivotScaledIdx = pivotScaledNextIdx ; 
				}
				else if (pivotScaledNextVal < key) {
					return binarySearchOriginal(a, low , pivotScaledNextIdx, key) ;
				}
				else {
					return pivotScaledNextIdx ;
				}
			}
			while (true) ;
			
			
			return binarySearchOriginal(a, low, pivotScaledIdx, key) ;
		}
		else {
			return pivotScaledIdx ;
		}
		
	}
	
	public static int search4(int[] a, int fromIndex, int toIndex, int key) {
		int low = fromIndex;
		int high = toIndex - 1;

		while (low <= high) {
			int pivot1Idx = (low+high) >>> 1 ;
			int pivot1Val = a[pivot1Idx] ;
			
			if ( pivot1Val < key ) {

				int pivot2Idx = (pivot1Idx+high) >>> 1 ;
				int pivot2Val = a[pivot2Idx] ;
			
				if ( pivot2Val < key ) {
					low = pivot2Idx+1 ;
					continue ;
				}
				else if ( pivot2Val > key ) {
					//int pivotScaledIdx = pivot1Idx + ( ( (pivot2Idx-pivot1Idx) * (key-pivot1Val) ) / (pivot2Val - pivot1Val) ) ;
					int pivotScaledIdx = (pivot1Idx+pivot2Idx) >>> 1 ;
					int pivotScaledVal = a[pivotScaledIdx] ;
					
					if (pivotScaledVal < key) {
						low = pivotScaledIdx+1 ;
						high = pivot2Idx-1 ;
					}
					else if (pivotScaledVal > key) {
						low = pivot1Idx+1 ;
						high = pivotScaledIdx-1 ;
					}
					else {
						return pivotScaledIdx ;
					}
					
				}
				else {
					return pivot2Idx ;
				}
				
			}
			else if ( pivot1Val > key ) {
				
				int pivot2Idx = (low+pivot1Idx) >>> 1 ;
				int pivot2Val = a[pivot2Idx] ;
			
				if ( pivot2Val < key ) {
					//int pivotScaledIdx = pivot2Idx + ( ( (pivot1Idx-pivot2Idx) * (key-pivot2Val) ) / (pivot1Val - pivot2Val) ) ;
					int pivotScaledIdx = (pivot1Idx+pivot2Idx) >>> 1 ;
					int pivotScaledVal = a[pivotScaledIdx] ;
					
					if (pivotScaledVal < key) {
						low = pivotScaledIdx+1 ;
						high = pivot1Idx-1 ;
					}
					else if (pivotScaledVal > key) {
						low = pivot2Idx+1 ;
						high = pivotScaledIdx-1 ;
					}
					else {
						return pivotScaledIdx ;
					}
					
				}
				else if ( pivot2Val > key ) {
					high = pivot2Idx-1 ;
				}
				else {
					return pivot2Idx ;
				}
				
			}
			else {
				return pivot1Idx ;
			}
			
		}

		return -(low + 1); // key not found.
	}
	
	public static int search3(int[] a, int fromIndex, int toIndex, int key) {
		int low = fromIndex;
		int high = toIndex - 1;
		
		int area ;
		int areaScaled ;
		int pivotLeftIdx ;
		int pivotRightIdx ;
		int pivotLeftVal ;
		int pivotRightVal ;
		int pivotScaledIdx ;
		int pivotScaledVal ;
		
		while (low <= high) {
			area = high - low ;
			int area1st4thPart = area/4 ; 
					
			pivotLeftIdx = low + area1st4thPart ; 
			pivotLeftVal = a[pivotLeftIdx];
			
			if (pivotLeftVal > key) {
				high = pivotLeftIdx - 1;
				continue ;
			}
			else if (pivotLeftVal == key) {
				return pivotLeftIdx ;
			}
			
			pivotRightIdx = low + ( area - area1st4thPart ) ; 
			pivotRightVal = a[pivotRightIdx];
			
			if (pivotRightVal < key) {
				low = pivotRightIdx + 1;
				continue ;
			}
			else if (pivotRightVal == key) {
				return pivotRightIdx ;
			}
			
			//areaScaled = pivotRightIdx - pivotLeftIdx ;
			//pivotScaledIdx = pivotLeftIdx + ((int)( areaScaled * ((key-pivotLeftVal) / ((float)(pivotRightVal - pivotLeftVal))) )) ;
			//pivotScaledIdx = pivotLeftIdx + ( ( areaScaled * (key-pivotLeftVal) ) / (pivotRightVal - pivotLeftVal) ) ;
			
			pivotScaledIdx = (pivotLeftIdx+pivotRightIdx) >>> 1 ;
			
			pivotScaledVal = a[pivotScaledIdx];
			
			if (pivotScaledVal < key) {
				low = pivotScaledIdx + 1;
				high = pivotRightIdx - 1;
			}
			else if (pivotScaledVal > key) {
				high = pivotScaledIdx - 1;
				low = pivotLeftIdx + 1 ;
			}
			else {
				return pivotScaledIdx ;
			}
			
		}

		return -(low + 1); // key not found.
	}
	
	public static int search2(int[] a, int fromIndex, int toIndex, int key) {
		int low = fromIndex ;
		int high = toIndex - 1;
		
		if (low > high) throw new IllegalArgumentException("fromIndex > toIndex: "+ fromIndex +" > "+ toIndex) ;
		
		int lowVal ;
		int highVal ;
		int pivotIdx ;
		int pivotVal ;
		
		// loop until find key, or return inverted insertion index at end:
		do {
			lowVal = a[low] ;
			highVal = a[high] ;

			if (key < lowVal) return -(low + 1);
			if (key > highVal) return -(high + 1 + 1);
			
			pivotIdx = low + ((int)( (high - low) * ((key-lowVal) / ((float)(highVal - lowVal))) )) ;
			pivotVal = a[pivotIdx];
			
			//System.out.println( low +" .. "+ pivotIdx +" .. "+ high +" > "+ lowVal +" .. "+ pivotVal +" <=> "+ key +" .. "+ highVal );
			
			if (pivotVal == key) {
				return pivotIdx ;
			}
			else {
				if (pivotVal < key) {
					low = pivotIdx + 1;
				}
				else {
					high = pivotIdx - 1;
				}
			}


		}
		while (true) ;

	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
     * Searches the specified array for the specified key using the
     * binary scaled search algorithm (by G.M.P.).
     * 
     * The array must be sorted prior to making this call. If it
     * is not sorted, the results are undefined.
     * 
     * If the array contains multiple elements with the specified value,
     * there is no guarantee which one will be found.
     *
     * @param a the array to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the array: the index of the first
     *         element greater than the key, or <tt>a.length</tt> if all
     *         elements in the array are less than the specified key. Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     */
	public static int search(float[] a, float key) {
		return search(a, 0, a.length, key) ;
	}
	
	public static int search(float[] a, int fromIndex, int toIndex, float key) {
		int low = fromIndex;
		int high = toIndex - 1;
		
		if (low > high) throw new IllegalArgumentException("fromIndex > toIndex: "+ fromIndex +" > "+ toIndex) ;
		
		float lowVal = a[low] ;
		float highVal = a[high] ;
		
		// Check key range, if out, return inverted insert index:
		if (key < lowVal) return -(low + 1);
		if (key > highVal) return -(high + 1 + 1);
		
		// loop until find key, or return inverted insertion index at end:
		do {
			float keyInScale = (key-lowVal) / (highVal - lowVal) ;
			int idxsScale = high - low ;
			
			// pivot index based in scale distribution of current area (low .. high):
			int pivotIdx = (int) (low + (idxsScale * keyInScale)) ;
			float pivotVal = a[pivotIdx];
			
			if (pivotVal < key) {
				
				if (keyInScale < 0.25f) {
					// check if high index should be changed to a lower position:
					int pivotLimitIdx = (low + high) >>> 1;
					
					if (a[pivotLimitIdx] > key) {
						high = pivotLimitIdx-1 ;
						highVal = a[high] ;
						
						// check range:
						if (key > highVal) return -(high + 1 + 1);
					}
				}
				
				// change low index and value, and also check range:
				low = pivotIdx + 1;
				lowVal = a[low] ;
				
				if (key < lowVal) {
					return -(low + 1);
				}
				
				continue ;
			}
			else if (pivotVal > key) {
				
				if (keyInScale > 0.75f) {
					// check if low index should be changed to a higher position:
					int pivotLimitIdx = (low + high) >>> 1;
					
					if (a[pivotLimitIdx] < key) {
						low = pivotLimitIdx+1 ;
						lowVal = a[low] ;
						
						// check range:
						if (key < lowVal) return -(low + 1);
					}
				}

				// change high index and value, and also check range:
				high = pivotIdx - 1;
				highVal = a[high] ;
				
				if (key > highVal) {
					return -(high + 1 + 1);
				}
				
				continue ;
			}
			else {
				// check if really found key, or NaN:
				
				int pivotBits = Float.floatToIntBits(pivotVal);
                int keyBits = Float.floatToIntBits(key);
                
                if (pivotBits == keyBits)     // Values are equal
                    return pivotIdx;             // Key found
                else if (pivotBits < keyBits) // (-0.0, 0.0) or (!NaN, NaN)
                    low = pivotIdx + 1;
                else                        // (0.0, -0.0) or (NaN, !NaN)
                    high = pivotIdx - 1;
				
			}

		}
		while (low <= high) ;
			
		// can't find key, return inverted insert index
		return -(low + 1);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////

	public static int binarySearchOriginal(int[] a, int key) {
		return binarySearchOriginal(a, 0, a.length, key) ;
	}

	public static int binarySearchOriginal(int[] a, int fromIndex, int toIndex, int key) {
		int low = fromIndex;
		int high = toIndex - 1;
		
		while (low <= high) {
			int pivotIdx = (low + high) >>> 1;
			int pivotVal = a[pivotIdx];
			
			if (pivotVal < key) {
				low = pivotIdx + 1;
			}
			else if (pivotVal > key) {
				high = pivotIdx - 1;
			}
			else {
				return pivotIdx; // key found
			}
		}

		return -(low + 1); // key not found.
	}

	public static int binarySearchOriginal(float[] a, float key) {
		return binarySearchOriginal(a, 0, a.length, key) ;
	}

	public static int binarySearchOriginal(float[] a, int fromIndex, int toIndex, float key) {
		int low = fromIndex;
		int high = toIndex - 1;
		
		while (low <= high) {
			int pivotIdx = (low + high) >>> 1;
			float pivotVal = a[pivotIdx];
			
			if (pivotVal < key) {
				low = pivotIdx + 1;
			}
			else if (pivotVal > key) {
				high = pivotIdx - 1;
			}
			else {
				// check if really found key, or NaN:
				
				int pivotBits = Float.floatToIntBits(pivotVal);
                int keyBits = Float.floatToIntBits(key);
                
                if (pivotBits == keyBits)     // Values are equal
                    return pivotIdx;             // Key found
                else if (pivotBits < keyBits) // (-0.0, 0.0) or (!NaN, NaN)
                    low = pivotIdx + 1;
                else                        // (0.0, -0.0) or (NaN, !NaN)
                    high = pivotIdx - 1;
				
			}
		}

		return -(low + 1); // key not found.
	}

}
