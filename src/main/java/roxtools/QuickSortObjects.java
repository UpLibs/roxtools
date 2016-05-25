package roxtools;


final public class QuickSortObjects {
	
	static public interface ObjectCompareValueInt<O> {
		public int getObjectCompareValue(O obj) ;
	}

	static public interface ObjectCompareValueFloat<O> {
		public float getObjectCompareValue(O obj) ;
	}
	
	static public interface ObjectCompareValueDouble<O> {
		public double getObjectCompareValue(O obj) ;
	}
	
	////////////////////////////
	
	static public <O> void sort(O[] o, ObjectCompareValueInt<O> objCompareValue) {
		sort(o, objCompareValue, 0, o.length-1);
	}
	
	static public <O> void sort(O[] o, ObjectCompareValueInt<O> objCompareValue, int fromIndex, int toIndex) {
		int[] a = new int[toIndex+1] ;
		
		for (int i = fromIndex; i <= toIndex; i++) {
			a[i] = objCompareValue.getObjectCompareValue( o[i] ) ;
		}
		
		quickSort(a, o, fromIndex, toIndex);
	}
	
	static public <O> void sort(O[] o, ObjectCompareValueFloat<O> objCompareValue) {
		sort(o, objCompareValue, 0, o.length-1);
	}
	
	static public <O> void sort(O[] o, ObjectCompareValueFloat<O> objCompareValue, int fromIndex, int toIndex) {
		float[] a = new float[toIndex+1] ;
		
		for (int i = fromIndex; i <= toIndex; i++) {
			a[i] = objCompareValue.getObjectCompareValue( o[i] ) ;
		}
		
		quickSort(a, o, fromIndex, toIndex);
	}
	
	static public <O> void sort(O[] o, ObjectCompareValueDouble<O> objCompareValue) {
		sort(o, objCompareValue, 0, o.length-1);
	}
	
	static public <O> void sort(O[] o, ObjectCompareValueDouble<O> objCompareValue, int fromIndex, int toIndex) {
		double[] a = new double[toIndex+1] ;
		
		for (int i = fromIndex; i <= toIndex; i++) {
			a[i] = objCompareValue.getObjectCompareValue( o[i] ) ;
		}
		
		quickSort(a, o, fromIndex, toIndex);
	}

	////////////////////////////
	
	static public void sortNumbersAsInt(Number[] o) {
		sortNumbersAsInt(o, 0, o.length-1);
	}
	
	static public void sortNumbersAsFloat(Number[] o) {
		sortNumbersAsFloat(o, 0, o.length-1);
	}
	
	static public void sortNumbersAsDouble(Number[] o) {
		sortNumbersAsDouble(o, 0, o.length-1);
	}
	
	static public void sortNumbersAsInt(Number[] o, int fromIndex, int toIndex) {
		int[] a = new int[toIndex+1] ;
		
		for (int i = fromIndex; i <= toIndex; i++) {
			a[i] = o[i].intValue() ;
		}
		
		quickSort(a, o, fromIndex, toIndex);
	}
	
	static public void sortNumbersAsFloat(Number[] o, int fromIndex, int toIndex) {
		float[] a = new float[toIndex+1] ;
		
		for (int i = fromIndex; i <= toIndex; i++) {
			a[i] = o[i].floatValue() ;
		}
		
		quickSort(a, o, fromIndex, toIndex);
	}
	
	static public void sortNumbersAsDouble(Number[] o, int fromIndex, int toIndex) {
		double[] a = new double[toIndex+1] ;
		
		for (int i = fromIndex; i <= toIndex; i++) {
			a[i] = o[i].doubleValue() ;
		}
		
		quickSort(a, o, fromIndex, toIndex);
	}
	
	////////////////////////////
	
	static public void sort(int[] a, Object[] o) {
		quickSort(a, o, 0, a.length-1);
	}
	
	static public void sort(int[] a, Object[] o, int fromIndex, int toIndex) {
		quickSort(a, o, fromIndex, toIndex);
	}
	
	static public void sort(float[] a, Object[] o) {
		quickSort(a, o, 0, a.length-1);
	}
	
	static public void sort(float[] a, Object[] o, int fromIndex, int toIndex) {
		quickSort(a, o, fromIndex, toIndex);
	}
	
	static public void sort(double[] a, Object[] o){
		quickSort(a, o, 0, a.length-1);
	}
	
	static public void sort(double[] a, Object[] o, int fromIndex, int toIndex){
		quickSort(a, o, fromIndex, toIndex);
	}
	
	////////////////////////////
	
	static public void sort(float[] a, int[] o) {
		quickSort(a, o, 0, a.length-1);
	}
	
	static public void sort(float[] a, int[] o, int fromIndex, int toIndex) {
		quickSort(a, o, fromIndex, toIndex);
	}
		
	static public void sort(float[] a, float[] o) {
		quickSort(a, o, 0, a.length-1);
	}
	
	static public void sort(float[] a, float[] o, int fromIndex, int toIndex) {
		quickSort(a, o, fromIndex, toIndex);
	}
	
	static public void sort(float[] a, int[] o, float[][] o2) {
		quickSort(a, o, o2, 0, a.length-1);
	}
	
	static public void sort(float[] a, int[] o, float[][] o2, int fromIndex, int toIndex) {
		quickSort(a, o, o2, fromIndex, toIndex);
	}
	
	static public void sort(float[] a, int[] o, Object[] o2) {
		quickSort(a, o, o2, 0, a.length-1);
	}
	
	static public void sort(float[] a, int[] o, Object[] o2, int fromIndex, int toIndex) {
		quickSort(a, o, o2, fromIndex, toIndex);
	}
	
	////////////////////////////
	
	static private void quickSort(float[] a, int[] o, int left,int right){
		if (left >= right) return;
		
		int partition = partition(a, o, left, right);
		
		int subLeft , subRight , subPartition ;
		
		subLeft = left ;
		subRight = partition-1 ;
		
		if (subLeft < subRight) {
			subPartition = partition(a, o, subLeft, subRight);
			quickSort(a, o, subLeft, subPartition-1);
			quickSort(a, o, subPartition+1, subRight);
		}
	
		subLeft = partition+1 ;
		subRight = right ;
		
		if (subLeft < subRight) {
			subPartition = partition(a, o, subLeft, subRight);
			quickSort(a, o, subLeft, subPartition-1);
			quickSort(a, o, subPartition+1, subRight);
		}
	}
	
	static private void quickSort(float[] a, float[] o, int left,int right){
		if (left >= right) return;
		
		int partition = partition(a, o, left, right);
		
		int subLeft , subRight , subPartition ;
		
		subLeft = left ;
		subRight = partition-1 ;
		
		if (subLeft < subRight) {
			subPartition = partition(a, o, subLeft, subRight);
			quickSort(a, o, subLeft, subPartition-1);
			quickSort(a, o, subPartition+1, subRight);
		}
	
		subLeft = partition+1 ;
		subRight = right ;
		
		if (subLeft < subRight) {
			subPartition = partition(a, o, subLeft, subRight);
			quickSort(a, o, subLeft, subPartition-1);
			quickSort(a, o, subPartition+1, subRight);
		}
	}
	
	static private void quickSort(float[] a, int[] o, float[][] o2, int left,int right){
		if (left >= right) return;
		
		int partition = partition(a, o, o2, left, right);
		
		int subLeft , subRight , subPartition ;
		
		subLeft = left ;
		subRight = partition-1 ;
		
		if (subLeft < subRight) {
			subPartition = partition(a, o, o2, subLeft, subRight);
			quickSort(a, o, o2, subLeft, subPartition-1);
			quickSort(a, o, o2, subPartition+1, subRight);
		}
	
		subLeft = partition+1 ;
		subRight = right ;
		
		if (subLeft < subRight) {
			subPartition = partition(a, o, o2, subLeft, subRight);
			quickSort(a, o, o2, subLeft, subPartition-1);
			quickSort(a, o, o2, subPartition+1, subRight);
		}
	}
	
	static private void quickSort(float[] a, int[] o, Object[] o2, int left,int right){
		if (left >= right) return;
		
		int partition = partition(a, o, o2, left, right);
		
		int subLeft , subRight , subPartition ;
		
		subLeft = left ;
		subRight = partition-1 ;
		
		if (subLeft < subRight) {
			subPartition = partition(a, o, o2, subLeft, subRight);
			quickSort(a, o, o2, subLeft, subPartition-1);
			quickSort(a, o, o2, subPartition+1, subRight);
		}
	
		subLeft = partition+1 ;
		subRight = right ;
		
		if (subLeft < subRight) {
			subPartition = partition(a, o, o2, subLeft, subRight);
			quickSort(a, o, o2, subLeft, subPartition-1);
			quickSort(a, o, o2, subPartition+1, subRight);
		}
	}
	
	////////////////////////////
	
	static private void quickSort(int[] a, Object[] o, int left,int right){
		if (left >= right) return;
		
		int partition = partition(a, o, left, right);
		
		int subLeft , subRight , subPartition ;
		
		subLeft = left ;
		subRight = partition-1 ;
		
		if (subLeft < subRight) {
			subPartition = partition(a, o, subLeft, subRight);
			quickSort(a, o, subLeft, subPartition-1);
			quickSort(a, o, subPartition+1, subRight);
		}
	
		subLeft = partition+1 ;
		subRight = right ;
		
		if (subLeft < subRight) {
			subPartition = partition(a, o, subLeft, subRight);
			quickSort(a, o, subLeft, subPartition-1);
			quickSort(a, o, subPartition+1, subRight);
		}
	}
	
	static private void quickSort(float[] a, Object[] o, int left,int right){
		if (left >= right) return;
		
		int partition = partition(a, o, left, right);
		
		int subLeft , subRight , subPartition ;
		
		subLeft = left ;
		subRight = partition-1 ;
		
		if (subLeft < subRight) {
			subPartition = partition(a, o, subLeft, subRight);
			quickSort(a, o, subLeft, subPartition-1);
			quickSort(a, o, subPartition+1, subRight);
		}
	
		subLeft = partition+1 ;
		subRight = right ;
		
		if (subLeft < subRight) {
			subPartition = partition(a, o, subLeft, subRight);
			quickSort(a, o, subLeft, subPartition-1);
			quickSort(a, o, subPartition+1, subRight);
		}
	}
	
	static private void quickSort(double[] a, Object[] o, int left,int right){
		if (left >= right) return;
		
		int partition = partition(a, o, left, right);
		
		int subLeft , subRight , subPartition ;
		
		subLeft = left ;
		subRight = partition-1 ;
		
		if (subLeft < subRight) {
			subPartition = partition(a, o, subLeft, subRight);
			quickSort(a, o, subLeft, subPartition-1);
			quickSort(a, o, subPartition+1, subRight);
		}
	
		subLeft = partition+1 ;
		subRight = right ;
		
		if (subLeft < subRight) {
			subPartition = partition(a, o, subLeft, subRight);
			quickSort(a, o, subLeft, subPartition-1);
			quickSort(a, o, subPartition+1, subRight);
		}
	}
	
	//////////////////////////
	
	public static int getMedian(int[]a, Object[] o, int left,int right){
		int center = (left+right)/2;
		
		if(a[left] > a[center])
			swap(a,o, left,center);
		
		if(a[left] > a[right])
			swap(a,o, left, right);
		
		if(a[center] > a[right])
			swap(a,o, center, right);
		
		// put median at pivot position:
		swap(a,o, center, right);
		
		return a[right];
	}
	
	public static float getMedian(float[]a, Object[] o, int left,int right){
		int center = (left+right)/2;
		
		if(a[left] > a[center])
			swap(a,o, left,center);
		
		if(a[left] > a[right])
			swap(a,o, left, right);
		
		if(a[center] > a[right])
			swap(a,o, center, right);
		
		// put median at pivot position:
		swap(a,o, center, right);
		
		return a[right];
	}
	
	public static float getMedian(float[]a, int[] o, int left,int right){
		int center = (left+right)/2;
		
		if(a[left] > a[center])
			swap(a,o, left,center);
		
		if(a[left] > a[right])
			swap(a,o, left, right);
		
		if(a[center] > a[right])
			swap(a,o, center, right);
		
		// put median at pivot position:
		swap(a,o, center, right);
		
		return a[right];
	}
	
	public static float getMedian(float[]a, float[] o, int left,int right){
		int center = (left+right)/2;
		
		if(a[left] > a[center])
			swap(a,o, left,center);
		
		if(a[left] > a[right])
			swap(a,o, left, right);
		
		if(a[center] > a[right])
			swap(a,o, center, right);
		
		// put median at pivot position:
		swap(a,o, center, right);
		
		return a[right];
	}
	
	public static float getMedian(float[]a, int[] o, float[][] o2, int left,int right){
		int center = (left+right)/2;
		
		if(a[left] > a[center])
			swap(a,o, o2, left,center);
		
		if(a[left] > a[right])
			swap(a,o, o2, left, right);
		
		if(a[center] > a[right])
			swap(a,o, o2, center, right);
		
		// put median at pivot position:
		swap(a,o, o2, center, right);
		
		return a[right];
	}
	
	public static float getMedian(float[]a, int[] o, Object[] o2, int left,int right){
		int center = (left+right)/2;
		
		if(a[left] > a[center])
			swap(a,o, o2, left,center);
		
		if(a[left] > a[right])
			swap(a,o, o2, left, right);
		
		if(a[center] > a[right])
			swap(a,o, o2, center, right);
		
		// put median at pivot position:
		swap(a,o, o2, center, right);
		
		return a[right];
	}
	
	public static double getMedian(double[]a, Object[] o, int left,int right){
		int center = (left+right)/2;
		
		if(a[left] > a[center])
			swap(a,o, left,center);
		
		if(a[left] > a[right])
			swap(a,o, left, right);
		
		if(a[center] > a[right])
			swap(a,o, center, right);
		
		// put median at pivot position:
		swap(a,o, center, right);
		
		return a[right];
	}
	
	//////////////////////////
	
	static private int partition(int[] a, Object[] o, int left,int right) {
		int pivot = getMedian(a, o, left, right) ;
		
		int leftCursor = left-1;
		int rightCursor = right;
		
		int tmp ;
		Object tmpO ;
		
		while(leftCursor < rightCursor){
            while(a[++leftCursor] < pivot);
            while(rightCursor > 0 && a[--rightCursor] > pivot);
            
			if (leftCursor >= rightCursor) {
				break;
			}
			else {
				//swap(a, o, leftCursor, rightCursor);
				
				tmp = a[leftCursor];
				a[leftCursor] = a[rightCursor];
				a[rightCursor] = tmp;
				
				tmpO = o[leftCursor];
				o[leftCursor] = o[rightCursor];
				o[rightCursor] = tmpO;
			}
		}
		
		//swap(a, o, leftCursor, right);
		
		tmp = a[leftCursor];
		a[leftCursor] = a[right];
		a[right] = tmp;
		
		tmpO = o[leftCursor];
		o[leftCursor] = o[right];
		o[right] = tmpO;
		
		return leftCursor;
	}
	
	static private int partition(float[] a, Object[] o, int left,int right) {
		float pivot = getMedian(a, o, left, right) ;
		
		int leftCursor = left-1;
		int rightCursor = right;
		
		float tmp ;
		Object tmpO ;
		
		while(leftCursor < rightCursor){
            while(a[++leftCursor] < pivot);
            while(rightCursor > 0 && a[--rightCursor] > pivot);
            
			if (leftCursor >= rightCursor) {
				break;
			}
			else {
				//swap(a, o, leftCursor, rightCursor);
				
				tmp = a[leftCursor];
				a[leftCursor] = a[rightCursor];
				a[rightCursor] = tmp;
				
				tmpO = o[leftCursor];
				o[leftCursor] = o[rightCursor];
				o[rightCursor] = tmpO;
			}
		}
		
		//swap(a, o, leftCursor, right);
		
		tmp = a[leftCursor];
		a[leftCursor] = a[right];
		a[right] = tmp;
		
		tmpO = o[leftCursor];
		o[leftCursor] = o[right];
		o[right] = tmpO;
		
		return leftCursor;
	}
	
	static private int partition(float[] a, int[] o, int left,int right) {
		float pivot = getMedian(a, o, left, right) ;
		
		int leftCursor = left-1;
		int rightCursor = right;
		
		float tmp ;
		int tmpO ;
		
		while(leftCursor < rightCursor){
            while(a[++leftCursor] < pivot);
            while(rightCursor > 0 && a[--rightCursor] > pivot);
            
			if (leftCursor >= rightCursor) {
				break;
			}
			else {
				//swap(a, o, leftCursor, rightCursor);
				
				tmp = a[leftCursor];
				a[leftCursor] = a[rightCursor];
				a[rightCursor] = tmp;
				
				tmpO = o[leftCursor];
				o[leftCursor] = o[rightCursor];
				o[rightCursor] = tmpO;
			}
		}
		
		//swap(a, o, leftCursor, right);
		
		tmp = a[leftCursor];
		a[leftCursor] = a[right];
		a[right] = tmp;
		
		tmpO = o[leftCursor];
		o[leftCursor] = o[right];
		o[right] = tmpO;
		
		return leftCursor;
	}
	
	static private int partition(float[] a, float[] o, int left,int right){
		float pivot = getMedian(a, o, left, right) ;
		
		int leftCursor = left-1;
		int rightCursor = right;
		
		float tmp ;
		float tmpO ;
		
		while(leftCursor < rightCursor){
            while(a[++leftCursor] < pivot);
            while(rightCursor > 0 && a[--rightCursor] > pivot);
            
			if (leftCursor >= rightCursor) {
				break;
			}
			else {
				//swap(a, o, leftCursor, rightCursor);
				
				tmp = a[leftCursor];
				a[leftCursor] = a[rightCursor];
				a[rightCursor] = tmp;
				
				tmpO = o[leftCursor];
				o[leftCursor] = o[rightCursor];
				o[rightCursor] = tmpO;
			}
		}
		
		//swap(a, o, leftCursor, right);
		
		tmp = a[leftCursor];
		a[leftCursor] = a[right];
		a[right] = tmp;
		
		tmpO = o[leftCursor];
		o[leftCursor] = o[right];
		o[right] = tmpO;
		
		return leftCursor;
	}
	
	static private int partition(float[] a, int[] o, float[][] o2, int left,int right) {
		float pivot = getMedian(a, o, o2, left, right) ;
		
		int leftCursor = left-1;
		int rightCursor = right;
		
		float tmp ;
		int tmpO ;
		float[] tmp1 ;
		
		while(leftCursor < rightCursor){
            while(a[++leftCursor] < pivot);
            while(rightCursor > 0 && a[--rightCursor] > pivot);
            
			if (leftCursor >= rightCursor) {
				break;
			}
			else {
				//swap(a, o, leftCursor, rightCursor);
				
				tmp = a[leftCursor];
				a[leftCursor] = a[rightCursor];
				a[rightCursor] = tmp;
				
				tmpO = o[leftCursor];
				o[leftCursor] = o[rightCursor];
				o[rightCursor] = tmpO;
				
				tmp1 = o2[leftCursor];
				o2[leftCursor] = o2[rightCursor];
				o2[rightCursor] = tmp1;
			}
		}
		
		//swap(a, o, leftCursor, right);
		
		tmp = a[leftCursor];
		a[leftCursor] = a[right];
		a[right] = tmp;
		
		tmpO = o[leftCursor];
		o[leftCursor] = o[right];
		o[right] = tmpO;
		
		tmp1 = o2[leftCursor];
		o2[leftCursor] = o2[right];
		o2[right] = tmp1;
		
		return leftCursor;
	}
	
	static private int partition(float[] a, int[] o, Object[] o2, int left,int right) {
		float pivot = getMedian(a, o, o2, left, right) ;
		
		int leftCursor = left-1;
		int rightCursor = right;
		
		float tmp ;
		int tmpO ;
		Object tmp1 ;
		
		while(leftCursor < rightCursor){
            while(a[++leftCursor] < pivot);
            while(rightCursor > 0 && a[--rightCursor] > pivot);
            
			if (leftCursor >= rightCursor) {
				break;
			}
			else {
				//swap(a, o, leftCursor, rightCursor);
				
				tmp = a[leftCursor];
				a[leftCursor] = a[rightCursor];
				a[rightCursor] = tmp;
				
				tmpO = o[leftCursor];
				o[leftCursor] = o[rightCursor];
				o[rightCursor] = tmpO;
				
				tmp1 = o2[leftCursor];
				o2[leftCursor] = o2[rightCursor];
				o2[rightCursor] = tmp1;
			}
		}
		
		//swap(a, o, leftCursor, right);
		
		tmp = a[leftCursor];
		a[leftCursor] = a[right];
		a[right] = tmp;
		
		tmpO = o[leftCursor];
		o[leftCursor] = o[right];
		o[right] = tmpO;
		
		tmp1 = o2[leftCursor];
		o2[leftCursor] = o2[right];
		o2[right] = tmp1;
		
		return leftCursor;
	}
	
	static private int partition(double[] a, Object[] o, int left,int right){
		double pivot = getMedian(a, o, left, right) ;
		
		int leftCursor = left-1;
		int rightCursor = right;
		
		double tmp ;
		Object tmpO ;
		
		while(leftCursor < rightCursor){
            while(a[++leftCursor] < pivot);
            while(rightCursor > 0 && a[--rightCursor] > pivot);
            
			if (leftCursor >= rightCursor) {
				break;
			}
			else {
				//swap(a, o, leftCursor, rightCursor);
				
				tmp = a[leftCursor];
				a[leftCursor] = a[rightCursor];
				a[rightCursor] = tmp;
				
				tmpO = o[leftCursor];
				o[leftCursor] = o[rightCursor];
				o[rightCursor] = tmpO;
			}
		}
		
		//swap(a, o, leftCursor, right);
		
		tmp = a[leftCursor];
		a[leftCursor] = a[right];
		a[right] = tmp;
		
		tmpO = o[leftCursor];
		o[leftCursor] = o[right];
		o[right] = tmpO;
		
		return leftCursor;
	}
	
	//////////////////////////
	
	static private void swap(int[] a, Object[] o, int idx1,int idx2){
		int tmp = a[idx1];
		a[idx1] = a[idx2];
		a[idx2] = tmp;
		
		Object tmpO = o[idx1];
		o[idx1] = o[idx2];
		o[idx2] = tmpO;
	}
	
	static private void swap(float[] a, Object[] o, int idx1,int idx2){
		float tmp = a[idx1];
		a[idx1] = a[idx2];
		a[idx2] = tmp;
		
		Object tmpO = o[idx1];
		o[idx1] = o[idx2];
		o[idx2] = tmpO;
	}
	
	static private void swap(float[] a, int[] o, int idx1,int idx2){
		float tmp = a[idx1];
		a[idx1] = a[idx2];
		a[idx2] = tmp;
		
		int tmpO = o[idx1];
		o[idx1] = o[idx2];
		o[idx2] = tmpO;
	}
	
	static private void swap(float[] a, float[] o, int idx1,int idx2){
		float tmp = a[idx1];
		a[idx1] = a[idx2];
		a[idx2] = tmp;
		
		float tmpO = o[idx1];
		o[idx1] = o[idx2];
		o[idx2] = tmpO;
	}
	
	static private void swap(float[] a, int[] o, float[][] o2, int idx1,int idx2){
		float tmp = a[idx1];
		a[idx1] = a[idx2];
		a[idx2] = tmp;
		
		int tmpO = o[idx1];
		o[idx1] = o[idx2];
		o[idx2] = tmpO;
		
		float[] tmp1 = o2[idx1];
		o2[idx1] = o2[idx2];
		o2[idx2] = tmp1;
	}
	
	static private void swap(float[] a, int[] o, Object[] o2, int idx1,int idx2){
		float tmp = a[idx1];
		a[idx1] = a[idx2];
		a[idx2] = tmp;
		
		int tmpO = o[idx1];
		o[idx1] = o[idx2];
		o[idx2] = tmpO;
		
		Object tmp1 = o2[idx1];
		o2[idx1] = o2[idx2];
		o2[idx2] = tmp1;
	}
	
	static private void swap(double[] a, Object[] o, int idx1,int idx2){
		double tmp = a[idx1];
		a[idx1] = a[idx2];
		a[idx2] = tmp;
		
		Object tmpO = o[idx1];
		o[idx1] = o[idx2];
		o[idx2] = tmpO;
	}
	
}

