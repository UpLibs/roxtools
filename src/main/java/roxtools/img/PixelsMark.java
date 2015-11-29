package roxtools.img;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import roxtools.ImageUtils;
import roxtools.SerializationUtils;

final public class PixelsMark implements Cloneable {

	final private boolean[] pixels;
	final private int width;
	final private int height;
	
	public PixelsMark(boolean[][] pixels) {
		this.width = pixels.length ;
		this.height = pixels[0].length ;

		this.pixels = new boolean[width * height] ;
		int pixelsSz = 0 ;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				this.pixels[pixelsSz++] = pixels[x][y] ;
			}
		}
	}
	
	public PixelsMark(boolean[] pixels, int macroPixelWidth, int macroPixelHeight) {
		this.pixels = pixels;
		this.width = macroPixelWidth;
		this.height = macroPixelHeight;
	}
	
	@Override
	public PixelsMark clone() {
		return new PixelsMark( this.pixels.clone() , this.width , this.height ) ;
	}
	
	public int countMarks() {
		int count = 0 ;
		for (int i = 0; i < this.pixels.length; i++) {
			if (this.pixels[i]) count++ ;
		}
		return count ;
	}
	
	public BufferedImage createImage(Color bgColor, Color markColor, boolean alphaImage) {
		BufferedImage buffImg = new BufferedImage(width , height, alphaImage ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB ) ;
		
		Graphics2D g = buffImg.createGraphics() ;
		
		if (!alphaImage) {
			g.setColor(Color.BLACK) ;
			g.fillRect(0, 0, width, height) ;
		}
		
		if (bgColor != null) {
			g.setColor(bgColor) ;
			g.fillRect(0, 0, width, height) ;
		}
		
		g.setColor(markColor) ;
		
		for (int j = 0; j < height; j++) {
			int jIdx = j*width ;
			for (int i = 0; i < width; i++) {
				int iIdx = jIdx+i ;
				
				boolean p = pixels[iIdx] ;
				
				if (p) {
					g.fillRect(i, j, 1, 1) ;
				}
			}
		}
		
		g.dispose() ;
		
		return buffImg ;
	}
	
	public PixelsMark scale(int w2, int h2) {
		BufferedImage img = createImage(Color.BLACK, Color.GREEN, false) ;
		img = ImageUtils.copyImage( img.getScaledInstance(w2, h2, BufferedImage.SCALE_AREA_AVERAGING) ) ;
		
		int[] pixels = ImageUtils.grabPixels(img) ;
		
		boolean[] marks = new boolean[pixels.length] ;
		
		int[] rgb = new int[3] ;
		for (int i = 0; i < marks.length; i++) {
			int p = pixels[i] ;
			
			RGB.pixelRGB_to_ArrayRGB(p, rgb) ;
			
			marks[i] = rgb[0] != 0 || rgb[1] != 0 || rgb[2] != 0 ;
		}
		
		return new PixelsMark(marks , img.getWidth(), img.getHeight()) ;
	}
	
	public BufferedImage createImage(Image bgImage, Color markColor) {
		int w = bgImage.getWidth(null) ;
		int h = bgImage.getHeight(null) ;
		
		BufferedImage buffImg = new BufferedImage(w , h, BufferedImage.TYPE_INT_RGB) ;
		
		Graphics2D g = buffImg.createGraphics() ;

		g.drawImage(bgImage, 0, 0, null) ;
		
		AffineTransform af = new AffineTransform() ;
		af.scale( w/(width*1d) , h/(height*1d) ) ;

		g.setTransform(af) ;
		
		g.setColor(markColor) ;
		
		for (int j = 0; j < height; j++) {
			int jIdx = j*width ;
			for (int i = 0; i < width; i++) {
				int iIdx = jIdx+i ;
				
				boolean p = pixels[iIdx] ;
				
				if (p) {
					g.fillRect(i, j, 1, 1) ;
				}
			}
		}
		
		g.dispose() ;
		
		return buffImg ;
	}
	
	public boolean[] getPixels() {
		return pixels;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	private int getBytesSize() {
		return 4*2 + pixels.length ;
	}
	
	public PixelsMark(byte[] bytes) {

		this.width = SerializationUtils.readInt(bytes, 0) ;
		this.height = SerializationUtils.readInt(bytes, 4) ;
		
		this.pixels = new boolean[ width * height ] ;
		
		int bytesRead = 2*4 ;
		
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = bytes[bytesRead++] == (byte)1 ;
		}
		
	}
	
	public byte[] toBytes(byte[] bytes) {
		int metaDataBytesSize = getBytesSize() ;
		if ( bytes == null || bytes.length != metaDataBytesSize ) {
			bytes = new byte[metaDataBytesSize] ;
		}
		
		int metaDataBytesSz = 0 ;
		
		SerializationUtils.writeInt(width, bytes, metaDataBytesSz) ;
		metaDataBytesSz += 4 ;
		
		SerializationUtils.writeInt(height, bytes, metaDataBytesSz) ;
		metaDataBytesSz += 4 ;
		
		for (int i = 0; i < pixels.length; i++) {
			bytes[metaDataBytesSz++] = pixels[i] ? (byte)1 : (byte)0 ;
		}
		
		return bytes ;
	}
	
	public PixelsMark(InputStream in) throws IOException {
		this.width = SerializationUtils.readInt(in) ;
		this.height = SerializationUtils.readInt(in) ;
		
		this.pixels = new boolean[ width * height ] ;
		
		byte[] buffer = new byte[1024] ;
		int bufferSz = buffer.length ;
		
		for (int i = 0; i < pixels.length; i++) {
			if (bufferSz == buffer.length) {
				int lng = pixels.length -i ;
				if (lng > buffer.length) lng = buffer.length ;
				
				SerializationUtils.readFull(in, buffer, lng) ;
				bufferSz = 0 ;
			}
			
			pixels[i] = buffer[bufferSz++] == (byte)1 ;
		}
	}
	
	public void writeTo( OutputStream out ) throws IOException {
		SerializationUtils.writeInt(width, out) ;
		SerializationUtils.writeInt(height, out) ;
		
		assert( pixels.length == width * height ) ;
		
		byte[] buffer = new byte[1024] ; 
		int bufferSz = 0 ;
		
		for (int i = 0; i < pixels.length; i++) {
			byte b = pixels[i] ? (byte)1 : (byte)0 ;
			
			buffer[bufferSz++] = b ;
			
			if (bufferSz == buffer.length) {
				out.write(buffer) ;
				bufferSz = 0 ;
			}
		}
		
		if (bufferSz > 0) {
			out.write(buffer, 0, bufferSz) ;
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	
	public boolean getPixelScaled(int x, int y, int w, int h) {
		double ratioX = w / (this.width*1d) ;
		double ratioY = h / (this.height*1d) ;
		
		int x0 = (int) (x/ratioX) ;
		int y0 = (int) (y/ratioY) ;
		
		return getPixel(x0, y0) ;
	}
	
	public boolean getPixel(int x, int y) {
		return pixels[ (y*width) + x ] ;
	}
	
	public int walkPixel(int x, int y) {
		boolean[] walkMemory = new boolean[ width * height ] ;
		return walkPixel(x, y, walkMemory) ;
	}
	
	public int walkPixel(int x, int y, boolean[] walkMemory) {
		int[] stepCount = new int[1] ;
		
		walkPixelImplem(x, y, walkMemory, stepCount, Integer.MAX_VALUE) ;
		
		return stepCount[0] ;
	}
	
	private void walkPixelImplem(int x, int y, boolean[] walkMemory, int[] stepCount, int maxStepCount) {
		if (x < 0 || x >= width) return ;
		if (y < 0 || y >= height) return ;
		
		int pIdx = (y*width) + x ;
		
		if ( !walkMemory[pIdx] && pixels[pIdx] ) {
			walkMemory[pIdx] = true ;
			stepCount[0]++ ;
		 
			if (stepCount[0] < maxStepCount) {
				walkPixelImplem(x-1, y, walkMemory, stepCount, maxStepCount) ;
				walkPixelImplem(x+1, y, walkMemory, stepCount, maxStepCount) ;
				
				walkPixelImplem(x, y-1, walkMemory, stepCount, maxStepCount) ;
				walkPixelImplem(x, y+1, walkMemory, stepCount, maxStepCount) ;
			}
		}
	}
	
	private void walkPixelImplem(int x, int y, int[] walkMemory, int memoryId, int[] stepCount, int maxStepCount) {
		if (x < 0 || x >= width) return ;
		if (y < 0 || y >= height) return ;
		
		int pIdx = (y*width) + x ;
		
		if ( pixels[pIdx] && walkMemory[pIdx] != memoryId) {
			walkMemory[pIdx] = memoryId ;
			stepCount[0]++ ;
			
			if (stepCount[0] < maxStepCount) {
				walkPixelImplem(x-1, y, walkMemory, memoryId, stepCount, maxStepCount) ;
				walkPixelImplem(x+1, y, walkMemory, memoryId, stepCount, maxStepCount) ;
				
				walkPixelImplem(x, y-1, walkMemory, memoryId, stepCount, maxStepCount) ;
				walkPixelImplem(x, y+1, walkMemory, memoryId, stepCount, maxStepCount) ;
			}
		}
	}
	
	private void walkPixelImplem(int x, int y, int[] walkMemory, int memoryId, int[] walkClip) {
		if (x < 0 || x >= width) return ;
		if (y < 0 || y >= height) return ;
		
		int pIdx = (y*width) + x ;
		
		if ( pixels[pIdx] && walkMemory[pIdx] != memoryId) {
			walkMemory[pIdx] = memoryId ;
			walkPixelUpdateClip(x, y, walkClip);
			
			try {
				walkPixelImplem(x-1, y, walkMemory, memoryId, walkClip) ;	
			}
			catch (StackOverflowError e) {}
			
			try {
				walkPixelImplem(x+1, y, walkMemory, memoryId, walkClip) ;	
			}
			catch (StackOverflowError e) {}
			
			try {
				walkPixelImplem(x, y-1, walkMemory, memoryId, walkClip) ;	
			}
			catch (StackOverflowError e) {}
			
			try {
				walkPixelImplem(x, y+1, walkMemory, memoryId, walkClip) ;	
			}
			catch (StackOverflowError e) {}
			
		}
	}
	
	private void walkPixelUpdateClip(int x, int y, int[] walkClip) {
		if ( walkClip[0] < 0 || x < walkClip[0] ) walkClip[0] = x ;
		if ( walkClip[1] < 0 || y < walkClip[1] ) walkClip[1] = y ;
		
		if ( walkClip[2] < 0 || x > walkClip[2] ) walkClip[2] = x ;
		if ( walkClip[3] < 0 || y > walkClip[3] ) walkClip[3] = y ;
	}
	
	public int countMarksWithSize(int minSize) {
		int count = 0 ;
		
		boolean[] walkMemory = new boolean[ width * height ] ;
		int[] stepCount = new int[1] ;

		for (int j = height-1; j >= 0; j--) {
			for (int i = width-1; i >= 0; i--) {
				stepCount[0] = 0 ;
				walkPixelImplem(i,j, walkMemory,stepCount, minSize) ;	
				if (stepCount[0] >= minSize) count++ ;
			}
		}
		
		return count ;
	}
	
	public Dimension getDimension() {
		return new Dimension(width , height) ;
	}
	
	public PixelsMark merge(PixelsMark other) {
		if ( this.width != other.width || this.height != other.height ) throw new IllegalArgumentException("Different dimention: "+ this.getDimension() +" != "+ other.getDimension()) ;
	
		boolean[] merge = this.pixels.clone() ;
		
		boolean[] otherPixels = other.pixels ;
		
		for (int i = otherPixels.length-1; i >= 0; i--) {
			merge[i] |= otherPixels[i];
		}
		
		return new PixelsMark(merge, width, height) ;
	}
	
	public PixelsMark removeSmallMarks(int minSize) {

		boolean[] filtered = this.pixels.clone() ;
		
		int[] stepCount = new int[1] ;
		int[] walkMemory = new int[ width * height ] ;
		int memoryId = 1 ;
		
		for (int j = 0; j < height; j++) {
			int jIdx = j*width ;
			
			for (int i = 0; i < width; i++) {
				int iIdx = jIdx + i ;
		
				if ( pixels[iIdx] ) {
					stepCount[0] = 0 ;
					walkPixelImplem(i, j, walkMemory, memoryId, stepCount, minSize) ;
					
					if (stepCount[0] < minSize) {
						filtered[iIdx] = false ;
					}
					
					memoryId++ ;
					
					if (memoryId < 1) {
						walkMemory = new int[ width * height ] ;
						memoryId = 1 ;
					}
				}
				
			}
		}
		
		return new PixelsMark(filtered, width, height) ;		
	}
	

	final public ArrayList<Rectangle> calcRectangles(int minRectArea) {
				
		int[] walkClipClean = new int[4] ;
		for (int i = 0; i < walkClipClean.length; i++) {
			walkClipClean[i] = -1 ;
		}
		
		int[] walkMemory = new int[ width * height ] ;
		int memoryId = 1 ;
		
		ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>() ;
		
		for (int j = 0; j < height; j++) {
			int jIdx = j*width ;
			
			for (int i = 0; i < width; i++) {
				int iIdx = jIdx + i ;
		
				if ( pixels[iIdx] && walkMemory[iIdx] == 0 ) {
					int[] walkClip = walkClipClean.clone() ;
					walkPixelImplem(i, j, walkMemory, memoryId, walkClip) ;
					
					int w = walkClip[2]-walkClip[0]+1;
					int h = walkClip[3]-walkClip[1]+1;
					
					if ( w*h >= minRectArea ) {
						Rectangle rec = new Rectangle( walkClip[0] , walkClip[1] , w , h ) ;
						rectangles.add(rec) ;
					}
					
					memoryId++ ;
					
					if (memoryId < 1) {
						walkMemory = new int[ width * height ] ;
						memoryId = 1 ;
					}
				}
				
			}
		}
		
		return rectangles ;
	}

	
}
