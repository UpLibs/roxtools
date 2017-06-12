package roxtools.img;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageProviderFile extends ImageProvider {

	private File file ;
	private long time ;
	
	private File cacheFileYUV ;
	
	public ImageProviderFile(File file) {
		this(file, file.lastModified()) ;
	}
	
	public ImageProviderFile(File file, long time) {
		this.file = file;
		this.time = time ;
		
		this.cacheFileYUV = new File( file.getParentFile() , file.getName() +".yuv.cache" ) ;
	}

	@Override
	protected BufferedImage readBufferedImage() throws IOException {
		ImagePixels imagePixelsRGB = getCachedImagePixelsRGB() ;
		
		BufferedImage bufferedImage ;
		
		if (imagePixelsRGB != null) {
			bufferedImage = imagePixelsRGB.createImage() ;
		}
		else {
			bufferedImage = ImageIO.read(file) ;	
		}
		
		setDimensions(bufferedImage.getWidth(), bufferedImage.getHeight()) ;
		return bufferedImage ;
	}

	@Override
	protected ImagePixels readImagePixelsRGB() throws IOException {
		ImagePixels imagePixels = new ImagePixels( getBufferedImage(true) ) ;
		setDimensions(imagePixels.getWidth(), imagePixels.getHeight()) ;
		return imagePixels ;
	}

	@Override
	protected ImagePixels readImagePixels() throws IOException {
		try {
			if ( cacheFileYUV.length() > 1 && cacheFileYUV.lastModified() >= file.lastModified() ) {
				FileInputStream fin = new FileInputStream(cacheFileYUV) ;
				
				ImagePixels imagePixelsYUV = new ImagePixels(fin, true) ;
				
				fin.close() ;
				
				return imagePixelsYUV ;
			}	
		}
		catch (Exception e) {
			System.err.println("** cacheFileYUV: "+ cacheFileYUV);
			e.printStackTrace() ;
		}
		
		ImagePixels imagePixels = new ImagePixels( getBufferedImage(true) ) ;
		
		imagePixels.saveCompressed(cacheFileYUV) ;
		
		setDimensions(imagePixels.getWidth(), imagePixels.getHeight()) ;
		return imagePixels ;
	}
	
	@Override
	protected ImagePixels readImagePixelsYUV() throws IOException {
		
		try {
			if ( cacheFileYUV.length() > 1 && cacheFileYUV.lastModified() >= file.lastModified() ) {
				FileInputStream fin = new FileInputStream(cacheFileYUV) ;
				
				BufferedInputStream bufIn = new BufferedInputStream(fin) ;
				ImagePixels imagePixelsYUV = new ImagePixels(bufIn, true) ;
				
				bufIn.close() ;
				
				setDimensions(imagePixelsYUV.getWidth(), imagePixelsYUV.getHeight()) ;
				
				return imagePixelsYUV ;
			}	
		}
		catch (Exception e) {
			System.err.println("** cacheFileYUV: "+ cacheFileYUV);
			e.printStackTrace() ;
		}
		
		ImagePixels imagePixels = new ImagePixels( getBufferedImage(true) ) ;
		imagePixels.convertToYUV() ;
		
		imagePixels.saveCompressed(cacheFileYUV) ;
		
		setDimensions(imagePixels.getWidth(), imagePixels.getHeight()) ;
		return imagePixels ;
	}

	@Override
	final public boolean isStored() {
		return file.length() > 1 ;
	}

	@Override
	final public File getStoreFile() {
		return file ;
	}
	
	@Override
	public File getOriginalStoreFile() {
		return file;
	}


	final public boolean store(File file) throws IOException {
		if ( file.equals(getStoreFile()) && file.length() > 1 ) return true ; 
		
		RenderedImage renderImg = getBufferedImage(true) ;
		
		String name = file.getName().toLowerCase() ;
		
		if ( name.endsWith(".png") ) {
			ImageIO.write(renderImg, "PNG", file) ;
			this.file = file ;
			return true ;
		}
		else if ( name.endsWith(".jpg") || name.endsWith(".jpeg") ) {
			ImageIO.write(renderImg, "JPEG", file) ;
			this.file = file ;
			return true ;
		}
		
		return false ;
	}
	
	@Override
	final public int getHoldingMemorySize() {
		return 0;
	}
	
	@Override
	final public long getImageTime() {
		return time ;
	}
	
	@Override
	protected void readDimensions() {
		try {
			if ( cacheFileYUV.length() > 1 && cacheFileYUV.lastModified() >= file.lastModified() ) {
				getImagePixelsYUV(true) ;
			}
			else {
				getBufferedImage(true) ;
			}
		} catch (IOException e) {
			throw new IllegalStateException(e) ;
		}
	}
	
	@Override
	final public void unholdMemory() throws IOException {
	}
	
	public ImageProvider createSubImageProvider(int x, int y, int width, int height) {
		Rectangle bounds = new Rectangle(0,0,this.getWidth(),this.getHeight()) ;
		Rectangle sub = new Rectangle(x,y,width,height) ;
		if ( !bounds.contains(sub) ) throw new IllegalArgumentException("Sub image out of bounds: "+ bounds +" !~ "+ sub) ;
		
		return new SubImageProvider(this, x, y, width, height) ;
	}
	
	final static private class SubImageProvider extends ImageProviderFile {
		private ImageProviderFile parent ;
		private int x ;
		private int y ;
		private int width ;
		private int height ;
		
		public SubImageProvider(ImageProviderFile parent, int x, int y, int width, int height) {
			super( createSubFile(parent.file, x, y, width, height) , parent.time ) ;
			
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
			ImagePixels imagePixelsRGB = getCachedImagePixelsRGB() ;
			if (imagePixelsRGB != null) return imagePixelsRGB.createImage() ;
			
			BufferedImage parentImg = parent.getBufferedImage(true) ;
			return parentImg.getSubimage(x, y, width, height);
		}

		@Override
		protected ImagePixels readImagePixels() throws IOException {
			return readImagePixelsYUV();
		}
		
		@Override
		protected ImagePixels readImagePixelsRGB() throws IOException {
			ImagePixels parentImg = parent.getCachedImagePixelsRGB() ;
			if (parentImg == null) parentImg = parent.getCachedImagePixelsYUV() ;
			if (parentImg == null) parentImg = parent.getImagePixelsRGB(true) ;
			
			ImagePixels subImg = parentImg.createSubImagePixels(x, y, width, height) ;
			subImg.convertToRGB() ;
			return subImg ;
		}
		
		@Override
		protected ImagePixels readImagePixelsYUV() throws IOException {
			ImagePixels parentImg = parent.getCachedImagePixelsYUV() ;
			if (parentImg == null) parentImg = parent.getCachedImagePixelsRGB() ;
			if (parentImg == null) parentImg = parent.getImagePixelsYUV(true) ;
			
			ImagePixels subImg = parentImg.createSubImagePixels(x, y, width, height) ;
			subImg.convertToYUV() ;
			return subImg ;
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
		private ImageProviderFile parent ;
		private int[] perspectiveFilterPoints ;
		private boolean cropBlankArea ;
		
		public SubPerspectiveImageProvider(ImageProviderFile parent, int[] perspectiveFilterPoints, boolean cropBlankArea) {
			super( createSubFile(parent.file, perspectiveFilterPoints) , parent.time ) ;
			
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
			ImagePixels imagePixelsRGB = getCachedImagePixelsRGB() ;
			if (imagePixelsRGB != null) return imagePixelsRGB.createImage() ;
			
			BufferedImage parentImg = parent.getBufferedImage(true) ;
			return applyPerspectiveFilter( parentImg ) ;
		}

		@Override
		protected ImagePixels readImagePixels() throws IOException {
			return readImagePixelsYUV();
		}
		
		@Override
		protected ImagePixels readImagePixelsRGB() throws IOException {
			ImagePixels parentImg = parent.getCachedImagePixelsRGB() ;
			if (parentImg == null) parentImg = parent.getCachedImagePixelsYUV() ;
			if (parentImg == null) parentImg = parent.getImagePixelsRGB(true) ;
			
			BufferedImage filtered = applyPerspectiveFilter( parentImg.createImage() ) ;
			return new ImagePixels(filtered).convertToRGB() ;
		}
		
		@Override
		protected ImagePixels readImagePixelsYUV() throws IOException {
			ImagePixels parentImg = parent.getCachedImagePixelsYUV() ;
			if (parentImg == null) parentImg = parent.getCachedImagePixelsRGB() ;
			if (parentImg == null) parentImg = parent.getImagePixelsYUV(true) ;
			
			BufferedImage filtered = applyPerspectiveFilter( parentImg.createImage() ) ;
			return new ImagePixels(filtered).convertToYUV() ;
		}
		
	}
	
}
