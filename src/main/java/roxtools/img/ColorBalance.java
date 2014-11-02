package roxtools.img;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import roxtools.RichConsole;

public class ColorBalance {
	
	static final public float BALANCE_COLORS_S1_DEFAULT = 1.50f ;
	static final public float BALANCE_COLORS_S2_DEFAULT = 1.50f ;
	static final public float BALANCE_COLORS_MIN_SCALE_RATIO_DEFAULT = 0.10f ;
	
	static private int[] toInts(float[] fs, int scale) {
		int[] ns = new int[fs.length] ;
		
		for (int i = ns.length-1; i >= 0; i--) {
			ns[i] = (int) (fs[i] * scale) ;
		}
		
		return ns ;
	}
	
	static public void balanceColors(float[] pixelsChannel) {
		balanceColors(pixelsChannel, BALANCE_COLORS_S1_DEFAULT, BALANCE_COLORS_S2_DEFAULT, BALANCE_COLORS_MIN_SCALE_RATIO_DEFAULT) ;
	}
	
	static public void balanceColors(byte[] pixelsChannel) {
		balanceColors(pixelsChannel, BALANCE_COLORS_S1_DEFAULT, BALANCE_COLORS_S2_DEFAULT, BALANCE_COLORS_MIN_SCALE_RATIO_DEFAULT) ;
	}
	
	static public void balanceColors(int[] pixelsChannel) {
		balanceColors(pixelsChannel, BALANCE_COLORS_S1_DEFAULT, BALANCE_COLORS_S2_DEFAULT, BALANCE_COLORS_MIN_SCALE_RATIO_DEFAULT) ;
	}
	
	static public void balanceColors(float[] pixelsChannel, float s1, float s2) {
		balanceColors(pixelsChannel, s1, s2, BALANCE_COLORS_MIN_SCALE_RATIO_DEFAULT);
	}
	
	static public void balanceColors(float[] pixelsChannel, float s1, float s2, float minScaleRatio) {
		int[] c1 = toInts(pixelsChannel, 255) ;
		
		for (int i = 0; i < c1.length; i++) {
			int v = c1[i] ;
			if (v < 0) c1[i] = 0 ;
			else if (v > 255) c1[i] = 255 ;
		}
		
		balanceColors(c1, s1, s2, minScaleRatio) ;
		
		for (int i = pixelsChannel.length-1 ; i >= 0 ; i--) {
			pixelsChannel[i] = c1[i] / 255f ;
		}
	}
	
	static public void balanceColors(byte[] pixelsChannel, float s1, float s2) {
		balanceColors(pixelsChannel, s1, s2, BALANCE_COLORS_MIN_SCALE_RATIO_DEFAULT);
	}
	
	static public void balanceColors(byte[] pixelsChannel, float s1, float s2, float minScaleRatio) {
		int[] c1 = ImagePixels.toInts(pixelsChannel) ;
		balanceColors(c1, s1, s2, minScaleRatio) ;
		
		for (int i = pixelsChannel.length-1 ; i >= 0 ; i--) {
			pixelsChannel[i] = (byte) c1[i] ;
		}
	}
	
	static public void balanceColors(int[] pixelsChannel, float s1, float s2) {
		balanceColors(pixelsChannel, s1, s2, BALANCE_COLORS_MIN_SCALE_RATIO_DEFAULT);
	}
	
	static public void balanceColors(int[] pixelsChannel, float s1, float s2, float minScaleRatio) {
		
		int n = pixelsChannel.length ;
		
		float s1_100_N = n * ( s1/100f ) ;
		float s2_100_min1 = n * (1 - (s2/100f)) ; 
		
		int[] hist = new int[256] ;
		
		for (int i = pixelsChannel.length-1 ; i >= 0 ; i--) {
			int p = pixelsChannel[i];
			hist[p]++ ;
		}
		
		for (int i = 1; i < 256; i++) {
			hist[i] += hist[i-1] ;
		}
		
		int vMin = 0 ;
		while ( hist[vMin+1] <= s1_100_N ) {
			vMin++ ;
		}

		
		int vMax = 255-1 ;
		while ( hist[vMax - 1] > s2_100_min1 ) {
			vMax-- ;
		}

		if (vMax < 255-1) vMax++ ;
		
		int vScale = vMax-vMin ; 
		
		int minScale = (int) (255 * minScaleRatio) ;
		
		//System.out.println("color balance>> "+ s1+" ; "+s2 +" > "+ vMin +" .. "+ vMax +" > "+ vScale +" / "+ minScale );
		
		
		if (vScale <= minScale) return ;
		
		for (int i = 0; i < n; i++) {
			int p = pixelsChannel[i] ;
			if ( p < vMin ) {
				pixelsChannel[i] = vMin ;
			}
			else if ( p > vMax ) {
				pixelsChannel[i] = vMax ;
			}
		}
		
		for (int i = 0; i < n; i++) {
			int p = pixelsChannel[i] ;
			
			pixelsChannel[i] = (int) ((p-vMin) * (255f / vScale)) ;
		}
		
	}

	static public void testGray(File file, float s1, float s2, float minScaleRatio) throws IOException {
		BufferedImage img = ImageIO.read(file) ;
		
		ImagePixels imagePixels = new ImagePixels(img) ;
		imagePixels.convertToYUV() ;
		
		byte[] c1 = imagePixels.getPixelsC1() ;
		
		ImagePixels imagePixels1 = new ImagePixels( c1, imagePixels.getWidth(), imagePixels.getHeight() , imagePixels.isYUVFormat() ) ;
		RichConsole.print( imagePixels1.createImage() ).configureMaxHeight(300) ;
		
		balanceColors(c1, s1, s2, minScaleRatio) ;
		
		ImagePixels imagePixels3 = new ImagePixels( c1, imagePixels.getWidth(), imagePixels.getHeight() , imagePixels.isYUVFormat() ) ;
	
		RichConsole.printLn( imagePixels3.createImage() ).configureMaxHeight(300) ;
	}
	
	static public void testRGB(File file, float s1, float s2, float minScaleRatio) throws IOException {

		BufferedImage img = ImageIO.read(file) ;
		
		ImagePixels imagePixels = new ImagePixels(img) ;
		
		RichConsole.print( imagePixels.createImage() ).configureMaxHeight(300) ;
		
		byte[] c1 = imagePixels.getPixelsC1() ;
		byte[] c2 = imagePixels.getPixelsC2() ;
		byte[] c3 = imagePixels.getPixelsC3() ;
		
		balanceColors(c1, s1, s2, minScaleRatio) ;
		balanceColors(c2, s1, s2, minScaleRatio) ;
		balanceColors(c3, s1, s2, minScaleRatio) ;
		
		ImagePixels imagePixels2 = new ImagePixels(c1, c2, c3, imagePixels.getWidth(), imagePixels.getHeight(), imagePixels.isYUVFormat()) ;
		
		RichConsole.printLn( imagePixels2.createImage() ).configureMaxHeight(300) ;
		
	}
	
	public static void main(String[] args) throws Exception {
		
		float s1 = 1.5f ;
		float s2 = 1.5f ;
		float minScaleRatio = 0.10f ;
		
		for (int i = 0; i < 12; i++) {
			File file = new File("/Volumes/SAFEZONE/tmp/img-color-balance-test"+i+".png") ;
			System.out.println(file.length() +"> "+file);
			
			testRGB(file, s1, s2, minScaleRatio) ;
			testGray(file, s1, s2, minScaleRatio) ;
		}
		
		
	}
	
}
