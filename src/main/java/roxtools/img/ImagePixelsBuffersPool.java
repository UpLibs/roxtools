package roxtools.img;

import roxtools.RoxDeque;

final public class ImagePixelsBuffersPool {
	
	private int maxPoolSize ;
	
	public ImagePixelsBuffersPool(int maxPoolSize) {
		if (maxPoolSize < 1) throw new IllegalArgumentException("Invalid max pool size: "+ maxPoolSize) ;
		this.maxPoolSize = maxPoolSize;
	}
	
	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	private final RoxDeque<ImagePixels> pool = new RoxDeque<ImagePixels>() ;
	
	public ImagePixels copy(ImagePixels target) {
		ImagePixels bufferRecicled = catchFromPool() ;
		
		if (bufferRecicled != null) {
			return target.copy(bufferRecicled) ;
		}
		else {
			return target.copy() ;
		}
	}
	
	public ImagePixels catchFromPool() {
		synchronized (pool) {
			return pool.pollFirst() ;
		}
	}
	
	public void addToPool(ImagePixels img) {
		synchronized (pool) {
			if ( pool.size() >= maxPoolSize ) return ; 
			pool.add(img) ;
			//System.out.println("** ImagePixelsBuffersPool size: "+ pool.size());
		}
	}
	
}
