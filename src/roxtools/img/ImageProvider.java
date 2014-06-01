package roxtools.img;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import roxtools.ImageUtils;

abstract public class ImageProvider {

	static public File createSubFile(File file, int x, int y, int w, int h) {
		String sufix = "-subimg-"+x+"-"+y+"-"+w+"x"+h ;
		return createSubFile(file, sufix) ;
	}
	
	static public File createSubFile(File file, int[] coords) {
		StringBuilder str = new StringBuilder() ;
		
		for (int i = 0; i < coords.length; i++) {
			str.append("-") ;
			str.append( coords[i] ) ;
		}
		
		String sufix = "-subimg-"+str ;
		
		return createSubFile(file, sufix) ;
	}
	
	static public File createSubFile(File file, String sufix) {
		if (file == null) return null ;
		
		String name = file.getName() ;
		
		String[] parts = name.split("\\.") ;
		
		String subName ;
		
		if ( parts.length == 1 ) {
			subName = parts[0] + sufix ;
		}
		else if (parts.length > 1) {
			subName = parts[0] ;
			for (int i = 1; i < parts.length-1; i++) {
				subName +="."+ parts[i] ;
			}
			subName += sufix ;
			subName +="."+ parts[parts.length-1] ;
		}
		else {
			subName = name + sufix ;
		}
		
		return new File( file.getParentFile() , subName ) ;
	}
	
	
	protected ImageProvider() {
	}
	
	protected ImageProvider(BufferedImage bufferedImage, ImagePixels imagePixelsRGB, ImagePixels imagePixelsYUV) {
		if (bufferedImage != null) this.imageRef = new SoftReference<BufferedImage>(bufferedImage) ;
		if (imagePixelsRGB != null) this.ImagePixelsRGBRef = new SoftReference<ImagePixels>(imagePixelsRGB) ;
		if (imagePixelsYUV != null) this.ImagePixelsYUVRef = new SoftReference<ImagePixels>(imagePixelsYUV) ;
	}
	
	abstract protected BufferedImage readBufferedImage() throws IOException ;
	
	abstract protected ImagePixels readImagePixels() throws IOException ;
	
	
	abstract protected ImagePixels readImagePixelsRGB() throws IOException ;
	abstract protected ImagePixels readImagePixelsYUV() throws IOException ;
	
	private Reference<BufferedImage> imageRef ;
	
	final protected BufferedImage getCachedBufferedImage() {
		if (imageRef == null) return null ;
		else return imageRef.get() ;
	}
	
	final public BufferedImage getBufferedImage(boolean allowShared) throws IOException {
		BufferedImage img = getCachedBufferedImage() ;
		
		if (img == null) {
			img = readBufferedImage() ;
			
			if (allowShared) {
				this.imageRef = new SoftReference<BufferedImage>(img) ;
			}
			
			return img ;
		}
		else {
			if (imageRef instanceof WeakReference) this.imageRef = new SoftReference<BufferedImage>(img) ;
			
			if (!allowShared) {
				return ImageUtils.copyImage(img) ;
			}
			else {
				return img ;	
			}
				
		}
		
	}

	private Reference<ImagePixels> ImagePixelsRGBRef ;
	
	final protected ImagePixels getCachedImagePixelsRGB() {
		if (ImagePixelsRGBRef == null) return null ;
		else return ImagePixelsRGBRef.get() ;
	}
	
	final public ImagePixels getImagePixelsRGB(boolean allowShared) throws IOException {
		ImagePixels pixels = getCachedImagePixelsRGB() ;
		
		if (pixels == null) {
			pixels = readImagePixelsRGB() ;
			
			if (allowShared) {
				ImagePixelsRGBRef = new SoftReference<ImagePixels>(pixels) ;
			}
			
			return pixels ;
		}
		else {
			if (ImagePixelsRGBRef instanceof WeakReference) this.ImagePixelsRGBRef = new SoftReference<ImagePixels>(pixels) ;
			
			if (allowShared) {
				return pixels ; 
			}
			else {
				return pixels.copy() ;
			}
		}
	}
	
	private Reference<ImagePixels> ImagePixelsYUVRef ;
	
	final protected ImagePixels getCachedImagePixelsYUV() {
		if (ImagePixelsYUVRef == null) return null ;
		else return ImagePixelsYUVRef.get() ;
	}
	
	final public ImagePixels getImagePixelsYUV(boolean allowShared) throws IOException {
		ImagePixels pixels = getCachedImagePixelsYUV() ;
		
		if (pixels == null) {
			pixels = readImagePixelsYUV() ;
			
			if (allowShared) {
				ImagePixelsYUVRef = new SoftReference<ImagePixels>(pixels) ;
			}
			
			return pixels ;
		}
		else {
			if (ImagePixelsYUVRef instanceof WeakReference) this.ImagePixelsYUVRef = new SoftReference<ImagePixels>(pixels) ;
			
			if (allowShared) {
				return pixels ; 
			}
			else {
				return pixels.copy() ;
			}
		}
	}
	
	abstract public boolean isStored() ;

	abstract public File getStoreFile() ;
	
	abstract public File getOriginalStoreFile() ;

	abstract public boolean store(File file) throws IOException ;
	
	final public void delete() {
		File storeFile = getStoreFile() ;
		if (storeFile != null) {
			storeFile.delete() ;
		}
		
		clearCaches() ;
	}

	final public boolean store() throws IOException {
		File file = getStoreFile() ;
		if (file == null) return false ;
		return store(file) ;
	}
	
	final public void clearCacheBufferedImage() {
		BufferedImage img = imageRef != null ? imageRef.get() : null ;
		if (img != null) this.imageRef = new WeakReference<BufferedImage>(img) ;
	}
	
	final public void clearCacheImagePixelsRGB() {
		ImagePixels pixelsRGB = ImagePixelsRGBRef != null ? ImagePixelsRGBRef.get() : null ;
		if (pixelsRGB != null) this.ImagePixelsRGBRef = new WeakReference<ImagePixels>(pixelsRGB) ;
	}
	
	final public void clearCacheImagePixelsYUV() {
		ImagePixels pixelsYUV = ImagePixelsYUVRef != null ? ImagePixelsYUVRef.get() : null ;
		if (pixelsYUV != null) this.ImagePixelsYUVRef = new WeakReference<ImagePixels>(pixelsYUV) ;
	}
	
	final public void clearCaches() {
		clearCacheBufferedImage() ;
		clearCacheImagePixelsRGB() ;
		clearCacheImagePixelsYUV() ;
	}
	
	abstract public int getHoldingMemorySize() ;

	abstract public long getImageTime() ;
	
	private boolean loadedDimensions ;
	private int width ;
	private int height ;
	
	final public boolean isLoadedDimensions() {
		return loadedDimensions ;
	}
	
	final public int getWidth() {
		if (!loadedDimensions) loadDimensions() ;
		return width;
	}
	
	final public int getHeight() {
		if (!loadedDimensions) loadDimensions() ;
		return height;
	}

	final protected void setDimensions(int width, int height) {
		this.width = width ;
		this.height = height ;
		this.loadedDimensions = true ;
	}
	
	final protected void loadDimensions() {
		if (loadedDimensions) return ;
		
		BufferedImage bufferedImage = getCachedBufferedImage() ;
		if (bufferedImage != null) {
			setDimensions(bufferedImage.getWidth(), bufferedImage.getHeight()) ;
		}
		
		ImagePixels pixels = getCachedImagePixelsYUV() ;
		if (pixels != null) setDimensions(pixels.getWidth(), pixels.getHeight()) ;
		
		pixels = getCachedImagePixelsRGB() ;
		if (pixels != null) setDimensions(pixels.getWidth(), pixels.getHeight()) ;
		
		readDimensions() ;
		
		if (!loadedDimensions) {
			throw new IllegalStateException("Can't load dimensions!") ;
		}
	}
	
	abstract protected void readDimensions() ;
	
	abstract public void unholdMemory() throws IOException ;
	
	public ImagePixels[] getAllHoldingImagePixels() {
		ImagePixels imgRGB = getCachedImagePixelsYUV() ;
		ImagePixels imgYUV = getCachedImagePixelsRGB() ;
		
		return new ImagePixels[] {imgRGB , imgYUV} ;
	}
	
	abstract public ImageProvider createSubImageProvider(int x, int y, int w, int h) ;
	
	abstract public ImageProvider createPerspectiveImageProvider(int[] perspectiveFilterPoints, boolean cropBlankArea) ;
	
	public ImageProvider createShearDistortion(boolean rightLeft, float ratio, boolean cropBlankArea) {
		int w = this.getWidth() ;
		int h = this.getHeight() ;
		
		int shear = (int) (h * ratio) ;
		
		int x1,y1 , x2,y2 , x3,y3 , x4,y4 ;
		
		if (rightLeft) {
			x1 = 0 ;
			y1 = shear ;
			
			x2 = w ;
			y2 = 0 ;
			
			x3 = w ;
			y3 = h-shear ;
			
			x4 = 0 ;
			y4 = h ;
		}
		else {
			x1 = 0 ;
			y1 = 0 ;
			
			x2 = w ;
			y2 = shear ;
			
			x3 = w ;
			y3 = h ;
			
			x4 = 0 ;
			y4 = h-shear ;
		}
		
		ImageProvider imageProviderShear = this.createPerspectiveImageProvider(new int[] { x1,y1 , x2,y2 , x3,y3 , x4,y4 }, cropBlankArea) ;
		
		return imageProviderShear ;
	}
	
}
