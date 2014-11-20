package roxtools.img;

import java.awt.image.BufferedImage;

public class ImageHistogram {
	
	static public float[] calculateHistogramRatio(BufferedImage image, int totalColors, int lightTolerance) {
		return calculateHistogramRatio(new ImagePixels(image), totalColors, lightTolerance) ;
	}
	
	static public int[] calculateHistogram(BufferedImage image, int totalColors, int lightTolerance) {
		return calculateHistogram(new ImagePixels(image), totalColors, lightTolerance) ;
	}
	
	static public float[] calculateHistogramRatio(ImagePixels imagePixels, int totalColors, int lightTolerance) {
		
		int[] hist = calculateHistogram(imagePixels, totalColors, lightTolerance) ;
		
		float total = 0 ;
		
		for (int i = hist.length-1; i >= 0; i--) {
			total += hist[i];
		}
		
		float[] histRatio = new float[hist.length] ; 
		
		for (int i = 0; i < histRatio.length; i++) {
			histRatio[i] = hist[i] / total ;
		}
		
		return histRatio ;
	}
	
	static public int[] calculateHistogram(ImagePixels imagePixels, int totalColors, int lightTolerance) {
		
		imagePixels.convertToYUV() ;
		
		byte[] c1 = imagePixels.getPixelsC1() ;
		byte[] c2 = imagePixels.getPixelsC2() ;
		byte[] c3 = imagePixels.getPixelsC3() ;

		double possibleColors = 256d * (256d/lightTolerance) ;
		
		double colorRange = possibleColors / (totalColors*1d) ;
		
		int[] hist = new int[totalColors] ;
		
		for (int i = c1.length-1; i >= 0 ; i--) {
			
			int y = c1[i] & 0xff ;
			int u = c2[i] & 0xff ;
			int v = c3[i] & 0xff ;
			
			int color = (u * v) / 256 ;
			color = color * (y/lightTolerance) ;
			
			int colorIdx = (int) (color / colorRange) ;
			
			hist[colorIdx]++ ;
			
		}
		
		return hist ;
	}
	
}
