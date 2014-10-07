package roxtools.img;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageProviderYUV extends ImageProvider {

	private File fileYUV ;
	private ImagePixels imagePixelsYUV ;
	private long time ;
	
	public ImageProviderYUV(ImagePixels imagePixelsYUV) {
		this(imagePixelsYUV, System.currentTimeMillis()) ;
	}
	
	public ImageProviderYUV(ImagePixels imagePixelsYUV, long time) {
		imagePixelsYUV.convertToYUV() ;
		imagePixelsYUV.setMutable(false) ;
		
		this.imagePixelsYUV = imagePixelsYUV ;
		this.time = time ;
		setDimensions(imagePixelsYUV.getWidth(), imagePixelsYUV.getHeight()) ;
	}
	
	public ImageProviderYUV(ImagePixels imagePixelsYUV, File fileYUV) {
		this(imagePixelsYUV, fileYUV, fileYUV.length() > 0 ? fileYUV.lastModified() : System.currentTimeMillis()) ;
	}
	
	public ImageProviderYUV(ImagePixels imagePixelsYUV, File fileYUV, long time) {
		this(imagePixelsYUV, fileYUV, time, fileYUV.length() > 0) ;
	}
	
	public ImageProviderYUV(ImagePixels imagePixelsYUV, File fileYUV, long time, boolean fileExists) {
		super(null, null, imagePixelsYUV) ;
		imagePixelsYUV.convertToYUV() ;
		imagePixelsYUV.setMutable(false) ;
		
		this.fileYUV = fileYUV;
		this.time = time ;
		
		setDimensions(imagePixelsYUV.getWidth(), imagePixelsYUV.getHeight()) ;
		
		if (!fileExists) {
			this.imagePixelsYUV = imagePixelsYUV ;
		}
	}
	
	public ImageProviderYUV(File fileYUV) {
		this(fileYUV, fileYUV.lastModified()) ;
	}
	
	public ImageProviderYUV(File fileYUV, long time) {
		this.fileYUV = fileYUV;
		this.time = time ;
	}

	@Override
	protected BufferedImage readBufferedImage() throws IOException {
		return getImagePixelsYUV(true).createImage() ;
	}

	@Override
	protected ImagePixels readImagePixelsRGB() throws IOException {
		return getImagePixelsYUV(false).convertToRGB() ;
	}

	@Override
	protected ImagePixels readImagePixels() throws IOException {
		return readImagePixelsYUV() ;
	}
	
	@Override
	protected ImagePixels readImagePixelsYUV() throws IOException {
		if ( imagePixelsYUV != null ) return imagePixelsYUV ;
		
		FileInputStream fin = new FileInputStream(fileYUV) ;
		BufferedInputStream buffIn = new BufferedInputStream(fin) ;
		ImagePixels pixels = new ImagePixels(buffIn) ;
		fin.close() ;
		
		setDimensions(pixels.getWidth(), pixels.getHeight()) ;
		
		return pixels ;
	}

	@Override
	public boolean isStored() {
		return fileYUV != null && fileYUV.length() > 1 ;
	}

	@Override
	public File getStoreFile() {
		return fileYUV ;
	}
	
	@Override
	public File getOriginalStoreFile() {
		return fileYUV ;
	}

	public boolean store(File file) throws IOException {
		if ( file.equals(this.fileYUV) && file.length() > 1 ) return true ; 
		
		ImagePixels pixels ;
		
		if (this.imagePixelsYUV != null) {
			pixels = this.imagePixelsYUV ;
		}
		else {
			pixels = getCachedImagePixelsYUV() ;
			
			if (pixels == null) {
				pixels = getCachedImagePixelsRGB() ;
				if (pixels != null) pixels = pixels.copy().convertToYUV() ;
			}
		}
		
		if ( pixels == null ) return false ;
		
		FileOutputStream fout = new FileOutputStream(file) ;
		BufferedOutputStream buffOut = new BufferedOutputStream(fout, 1024*512) ;
		pixels.writeTo(buffOut) ;
		buffOut.close() ;
		
		this.fileYUV = file ;
		this.imagePixelsYUV = null ;
		
		return true ;
	}
	
	public int getHoldingMemorySize() {
		if (this.imagePixelsYUV != null) {
			return this.imagePixelsYUV.getHoldingMemorySize() ;
		}
		return 0 ;
	}
	
	@Override
	public void unholdMemory() throws IOException {
		if (this.fileYUV == null) throw new IOException("Can't define file to store!") ;
		store(this.fileYUV) ;
	}

	@Override
	public long getImageTime() {
		return time ;
	}

	@Override
	protected void readDimensions() {
		try {
			readImagePixelsYUV() ;
		} catch (IOException e) {
			throw new IllegalStateException(e) ;
		}
	}

	public ImagePixels[] getAllHoldingImagePixels() {
		ImagePixels imgRGB = getCachedImagePixelsYUV() ;
		ImagePixels imgYUV = getCachedImagePixelsRGB() ;
		ImagePixels imgYUV2 = this.imagePixelsYUV ;
		
		if (imgYUV2 == imgYUV) imgYUV2 = null ;
		
		return new ImagePixels[] {imgRGB , imgYUV , imgYUV2} ;
	}
	

	public ImageProvider createSubImageProvider(int x, int y, int width, int height) {
		Rectangle bounds = new Rectangle(0,0,this.getWidth(),this.getHeight()) ;
		Rectangle sub = new Rectangle(x,y,width,height) ;
		if ( !bounds.contains(sub) ) throw new IllegalArgumentException("Sub image out of bounds: "+ bounds +" !~ "+ sub) ;
		
		return new SubImageProvider(this, x, y, width, height) ;
	}
	
	final static private class SubImageProvider extends ImageProviderYUV {
		private ImageProviderYUV parent ;
		private int x ;
		private int y ;
		private int width ;
		private int height ;
		
		public SubImageProvider(ImageProviderYUV parent, int x, int y, int width, int height) {
			super( createSubFile(parent.fileYUV, x, y, width, height) , parent.time ) ;
			
			this.parent = parent;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public File getOriginalStoreFile() {
			return parent.getOriginalStoreFile();
		}
		
		@Override
		protected void readDimensions() {
			setDimensions(this.width, this.height) ;
		}
		
		@Override
		protected BufferedImage readBufferedImage() throws IOException {
			BufferedImage parentImg = parent.getBufferedImage(true) ;
			return parentImg.getSubimage(x, y, width, height);
		}
		
		@Override
		protected ImagePixels readImagePixelsRGB() throws IOException {
			ImagePixels parentImg = parent.getCachedImagePixelsRGB() ;
			if (parentImg == null) parentImg = parent.getCachedImagePixelsYUV() ;
			if (parentImg == null) parentImg = parent.getImagePixelsYUV(true) ;
			
			ImagePixels subImg = parentImg.createSubImagePixels(x, y, width, height) ;
			subImg.convertToRGB() ;			
			
			return subImg ;
		}
		
		@Override
		protected ImagePixels readImagePixels() throws IOException {
			return readImagePixelsYUV();
		}
		
		@Override
		protected ImagePixels readImagePixelsYUV() throws IOException {
			ImagePixels parentImgYUV = parent.getImagePixelsYUV(true) ;
			return parentImgYUV.createSubImagePixels(x, y, width, height) ;
		}

		@Override
		public ImageProvider createSubImageProvider(int x, int y, int width, int height) {
			Rectangle bounds = new Rectangle(this.x,this.y,this.width,this.height) ;
			Rectangle sub = new Rectangle(x,y,width,height) ;
			if ( !bounds.contains(sub) ) throw new IllegalArgumentException("Sub image out of bounds: "+ bounds +" !~ "+ sub) ;
			
			return new SubImageProvider(parent, this.x+x, this.y+y, width, height) ;
		}
		
	}

	@Override
	public ImageProvider createPerspectiveImageProvider(int[] perspectiveFilterPoints, boolean cropBlankArea) {
		return new SubPerspectiveImageProvider(this, perspectiveFilterPoints, cropBlankArea) ;
	}
	
	final static private class SubPerspectiveImageProvider extends ImageProviderFile {
		private ImageProviderYUV parent ;
		private int[] perspectiveFilterPoints ;
		private boolean cropBlankArea ;
		
		public SubPerspectiveImageProvider(ImageProviderYUV parent, int[] perspectiveFilterPoints, boolean cropBlankArea) {
			super( createSubFile(parent.fileYUV, perspectiveFilterPoints) , parent.time ) ;
			
			this.parent = parent;
			this.perspectiveFilterPoints = perspectiveFilterPoints ;
			this.cropBlankArea = cropBlankArea ;
		}
		
		@Override
		public File getOriginalStoreFile() {
			return parent.getOriginalStoreFile();
		}
		
		private BufferedImage applyPerspectiveFilter(BufferedImage image) {
			return ImagePerspectiveFilter.applyPerspective(image, 
					perspectiveFilterPoints[0],
					perspectiveFilterPoints[1],
					perspectiveFilterPoints[2],
					perspectiveFilterPoints[3],
					perspectiveFilterPoints[4],
					perspectiveFilterPoints[5],
					perspectiveFilterPoints[6],
					perspectiveFilterPoints[7],
					 
					cropBlankArea) ;
		}
		
		@Override
		protected BufferedImage readBufferedImage() throws IOException {
			BufferedImage parentImg = parent.getBufferedImage(true) ;
			return applyPerspectiveFilter(parentImg) ;
		}
		
		@Override
		protected ImagePixels readImagePixelsRGB() throws IOException {
			ImagePixels parentImg = parent.getCachedImagePixelsRGB() ;
			if (parentImg == null) parentImg = parent.getCachedImagePixelsYUV() ;
			if (parentImg == null) parentImg = parent.getImagePixelsYUV(true) ;
			
			BufferedImage filtered = applyPerspectiveFilter( parentImg.createImage() ) ;
			return new ImagePixels(filtered).convertToRGB() ;
		}
		
		@Override
		protected ImagePixels readImagePixels() throws IOException {
			return readImagePixelsYUV();
		}
		
		@Override
		protected ImagePixels readImagePixelsYUV() throws IOException {
			ImagePixels parentImgYUV = parent.getImagePixelsYUV(true) ;
			
			BufferedImage filtered = applyPerspectiveFilter( parentImgYUV.createImage() ) ;
			return new ImagePixels(filtered).convertToYUV() ;
		}
		
	}
	
}
