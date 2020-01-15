package roxtools.img;

import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;

import javax.imageio.ImageIO;

import roxtools.ImageUtils;
import roxtools.RichConsole;

final public class HorizontalShearImage {

	final private BufferedImage image ;
	final private double lineRatio ;
	
	public HorizontalShearImage(BufferedImage image, double lineRatio) {
		this.image = image;
		this.lineRatio = lineRatio ;
	}
	
	private int[] getPixels() {

		PixelGrabber grabber = new PixelGrabber(image, 0, 0, image.getWidth(), image.getHeight(), false);

		try {
			if (grabber.grabPixels()) {
				int[] data = (int[]) grabber.getPixels();
				return data ;
			} else {
				throw new IllegalStateException();
			}
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
		
	}
	
	public BufferedImage createImageCropped(boolean topDown) {
		return createImageCropped(topDown , lineRatio) ;
	}
	
	public BufferedImage createImageCropped(boolean topDown, double cropLineRatio) {
		BufferedImage img = createImage(topDown) ;
		
		int w = img.getWidth() ;
		int h = img.getHeight() ;
		
		int topLine = (int) (w * cropLineRatio) ;
		
		int ident = (w - topLine) / 2 ;
		
		BufferedImage subimage = img.getSubimage(ident, 0, topLine, h) ;
		
		return ImageUtils.copyImage( subimage ) ;
	}
	
	public BufferedImage createImage(boolean topDown) {
		
		int w = image.getWidth() ;
		int h = image.getHeight() ;
		
		
		int wMinus = w-1 ;
		double hD = h ;
		
		int blurSize = 2 ;// h/10 ;
		
		int w2 = (int) (w * lineRatio) ;
		
		int wCenter = w/2 ;
		int wCenter2 = w - wCenter ;
		double wCenterD = wCenter ;
		double wCenter2D = wCenter2 ;
		
		double halfPixel = (1 / wCenterD) / 2d ;  
		
		int[] pixels = getPixels() ;
		
		BufferedImage buffImg = new BufferedImage(w , h, BufferedImage.TYPE_INT_RGB) ;
		
		int wDiff = w - w2 ;
		
		int prevWLine = -1 ;
		int prevLineIdent = 0 ;
		
		for (int j = 0; j < h; j++) {
			double hRatio = j / (hD-1) ;
			if (!topDown) hRatio = 1d - hRatio ;
			
			int wLine = (int) (w - (wDiff * hRatio)) ;
			int wLineCenter = wLine/2 ;
			int wLineCenter2 = wLine - wLineCenter ;
			
			int lineIdent ;
			if ( wLine == prevWLine ) {
				lineIdent = prevLineIdent ;
			}
			else {
				lineIdent = (int) ((wDiff * hRatio) / 2d) ;	
			}
			
			int prevX = -1 ;
			
			int xCount = 0 ;
			int xVal = 0 ;
			
			int minX = w ;
			int maxX = 0 ;
			
			for (int i = 0; i < w; i++) {
				int x = lineIdent ;
				
				if ( i >= wCenter ) {
					double xRatio = (i - wCenter) / (wCenter2D-1d) + halfPixel ;
					
					x += wLineCenter + ((int) (xRatio * (wLineCenter2-1))) ;
				}
				else {
					double xRatio = i / (wCenterD-1d) + halfPixel;
					
					x += (int) (xRatio * (wLineCenter-1)) ;
				}
				
				assert(x >= 0) ;
				assert(x < w) ;
				
				int idx = j * w + i ;
				int rgb = pixels[idx] ;
				
				if ( x == prevX ) {
					xVal = addRGB(xVal, xCount++, rgb) ;
				}
				else {
					xCount = 1 ;
					xVal = rgb ;
				}
				
				buffImg.setRGB(x, j, xVal) ;
				
				prevX = x ;
				
				if (x < minX) minX = x ;
				if (x > maxX) maxX = x ;
			}
			
			{
				for (int i = 0; i < minX; i++) {
					int val = 0 ;
					int valCount = 0 ;
					
					for (int k = 0; k < blurSize; k++) {
						int j2 = j-k ;
						if (j2 < 0) continue ;
						int idx = j2 * w + 0 ;

						val = addRGB(val, valCount++, pixels[idx]) ;
					}
					
					for (int k = 0; k < blurSize; k++) {
						int j2 = j+k ;
						if (j2 >= h) continue ;
						int idx = j2 * w + 0 ;

						val = addRGB(val, valCount++, pixels[idx]) ;
					}
					
					buffImg.setRGB(i, j, val) ;
				}
			}
			
			{
				
				
				for (int i = maxX+1 ; i < w; i++) {
					int val = 0 ;
					int valCount = 0 ;
					
					for (int k = 0; k < blurSize; k++) {
						int j2 = j-k ;
						if (j2 < 0) continue ;
						int idx = j2 * w + wMinus ;

						val = addRGB(val, valCount++, pixels[idx]) ;
					}
					
					for (int k = 0; k < blurSize; k++) {
						int j2 = j+k ;
						if (j2 >= h) continue ;
						int idx = j2 * w + wMinus ;

						val = addRGB(val, valCount++, pixels[idx]) ;
					}
					
					
					buffImg.setRGB(i, j, val) ;
				}
			}
			
			prevWLine = wLine ;
			prevLineIdent = lineIdent ;
		}
		
		return buffImg ;
	}
	
	static private int addRGB(int val, int valCount, int rgb) {
		int rV   = (val >> 16) & 0xff;
		int gV = (val >> 8) & 0xff;
		int bV  = val & 0xff;
		
		int r   = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b  = rgb & 0xff;
		
		int valCountPlus = valCount+1 ;
		
		r = (( rV * valCount ) + r ) / valCountPlus ;
		g = (( gV * valCount ) + g ) / valCountPlus ;
		b = (( bV * valCount ) + b ) / valCountPlus ;
		
		int add = (r << 16) | (g << 8) | b ;
		
		return add ;
	}

}
