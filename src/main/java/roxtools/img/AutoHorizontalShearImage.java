package roxtools.img;

import roxtools.ArrayUtils;
import roxtools.ImageUtils;
import roxtools.RichConsole;
import roxtools.RichConsole.ChartBars;
import roxtools.StatisticsUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

final public class AutoHorizontalShearImage {

	private BufferedImage image ;
	private BufferedImage grayscaleImage;
	
	static private final double MIN_SHEAR_LINE_RATIO = 0.60 ;
	static private final int GRAYSCALE_QUANTIFICATION_BITS = 7 ;
	private int minimalShearImageWidth;
	
	static final private boolean DEBUG = false ;
	
	public AutoHorizontalShearImage(File imageFile) throws IOException {
		this( ImageIO.read(imageFile) ) ;
	}
	
	public AutoHorizontalShearImage(BufferedImage image) {
		
		if (image.getWidth() % 2 != 0) {
			int w = image.getWidth() ;
			int h = image.getHeight() ;
			
			image = ImageUtils.copyImage( image.getSubimage(0, 0, w-1, h) ) ;
		}
		
		this.image = image ;
		
		int w = image.getWidth() ;
		int h = image.getHeight() ;
		
		double whRatio = w / (h*1D) ;
		
		int grayH = Math.min(h, 500) ;
		int grayW = (int) (grayH * whRatio) ;
		
		if (grayH % 2 != 0) grayH-- ;
		if (grayW % 2 != 0) grayW-- ;
		
		ImagePixels imgYUV = new ImagePixels(image).convertToYUV() ;
		
		this.grayscaleImage = imgYUV.scale(grayW, grayH).createGrayscaleImage(GRAYSCALE_QUANTIFICATION_BITS , true).createImage() ;
		
		this.minimalShearImageWidth = (int) (grayW * MIN_SHEAR_LINE_RATIO);
		
		if (DEBUG) RichConsole.printLn( this.grayscaleImage ) ;
		
	}
	
	public BufferedImage autoHorizontalShear() {
		
		if (DEBUG) RichConsole.printLn(image) ;
		
		double bestShear = findBestShear() ;
		
		HorizontalShearImage shearImage = new HorizontalShearImage(image, bestShear) ;
		
		BufferedImage imgAutoShear = shearImage.createImageCropped(false) ;
		
		if (DEBUG) {
			RichConsole.printLn( image ) ;
			RichConsole.printLn( imgAutoShear ) ;
		}
		
		return imgAutoShear ;
	}
	
	private double findBestShear() {
		ArrayList<ShearVerticality> shears = new ArrayList<ShearVerticality>() ;
		
		for (double lineRatio = 1d ; lineRatio >= MIN_SHEAR_LINE_RATIO ; lineRatio -= 0.025) {
			if (lineRatio == 1) continue ;
			
			ShearVerticality shearVerticality = calcShearVerticality(grayscaleImage, lineRatio) ;
			shears.add(shearVerticality) ;
		}
		
		Collections.sort(shears) ;
		
		if (DEBUG) {
			RichConsole.printLn("---------------------------------------------------------------") ;
			
			shears.get(0).show() ;
			shears.get( shears.size()-1 ).show() ;
			
			RichConsole.printLn("---------------------------------------------------------------") ;
			
			for (ShearVerticality shearVerticality : shears) {
				shearVerticality.show() ;
			}
			
			RichConsole.printLn("---------------------------------------------------------------") ;
		}
		
		ShearVerticality bestShear = shears.get(0) ;
		
		return bestShear.shearLineRatio ;
	}
	
	private class ShearVerticality implements Comparable<ShearVerticality>{
		double shearLineRatio ;
		BufferedImage img ;
		float[] verticality ;
		float verticalityMean ;
		BufferedImage imgLeftPart ;
		
		public ShearVerticality(double shearLineRatio, BufferedImage img, float[] verticality) {
			this.shearLineRatio = shearLineRatio ;
			this.img = img;
			
			int imgW = img.getWidth() ;
			
			int cutImgIdent = (imgW - minimalShearImageWidth) / 2 ;
			int cutImgW = verticality.length ;
			
			imgLeftPart = img.getSubimage(cutImgIdent, 0, cutImgW , img.getHeight()) ;
			
			this.verticality = verticality;
			
			float means = StatisticsUtils.calcMean(verticality) ;
			
			this.verticalityMean = means ;
		}
		
		public void show() {
			RichConsole.printLn("verticality> "+ shearLineRatio +" -> "+ verticalityMean) ;
			RichConsole.printLn( img ).configureMaxHeight(300) ;
			
			float[] data1 = StatisticsUtils.normalize(this.verticality , 0, 200) ;
			
			ChartBars chartBars1 = new RichConsole.ChartBars(data1, 1 , 50) ;
			
			RichConsole.printLn( imgLeftPart ).scale( data1.length * 2 , 200 ) ;
			RichConsole.printLn(chartBars1) ;
			
		}

		@Override
		public int compareTo(ShearVerticality o) {
			return Float.compare(o.verticalityMean , this.verticalityMean) ;
		}
	}
	
	private ShearVerticality calcShearVerticality(BufferedImage img, double lineRatio) {
		HorizontalShearImage shearImage = new HorizontalShearImage(grayscaleImage, lineRatio) ;
		BufferedImage img2 = shearImage.createImageCropped(false) ;
		
		ImagePixels grayscaleImage2 = new ImagePixels(img2).createGrayscaleImage(GRAYSCALE_QUANTIFICATION_BITS) ;
		
		float[] verticality = calcVerticality(grayscaleImage2) ;
		
		return new ShearVerticality(lineRatio , grayscaleImage2.createImage(), verticality) ;
	}
	
	private float[] calcVerticality(ImagePixels img) {

		byte[] pxs = img.getPixelsC1() ;
		
		int w = img.getWidth() ;
		int h = img.getHeight() ;
		
		int hInit = (h/5) ;
		int hEnd = h-hInit ;
		
		int cutImgIdent = (w - minimalShearImageWidth) / 2 ;
		int cutImgW = ((w/2) - cutImgIdent) /2 ;
		
		int limit = cutImgIdent + cutImgW ;
		
		byte[] pxs2 = pxs.clone() ;
		
		////////////////////////////////////
		
		for (int i = cutImgIdent; i < limit; i++) {
			for (int j = hInit; j < hEnd; j++) {
				int idx_0_0 = ((j-1)*w) + (i-1) ;
				int idx_0_1 = (j*w) + (i-1) ;
				int idx_0_2 = ((j+1)*w) + (i-1) ;
				
				int idx_1_0 = ((j-1)*w) + i ;
				int idx_1_1 = (j*w) + i ;
				int idx_1_2 = ((j+1)*w) + i ;
				
				int idx_2_0 = ((j-1)*w) + (i+1) ;
				int idx_2_1 = (j*w) + (i+1) ;
				int idx_2_2 = ((j+1)*w) + (i+1) ;
				
				byte p_0_0 = pxs[idx_0_0] ;
				byte p_0_1 = pxs[idx_0_1] ;
				byte p_0_2 = pxs[idx_0_2] ;
				
				byte p_1_0 = pxs[idx_1_0] ;
				byte p_1_1 = pxs[idx_1_1] ;
				byte p_1_2 = pxs[idx_1_2] ;
				
				byte p_2_0 = pxs[idx_2_0] ;
				byte p_2_1 = pxs[idx_2_1] ;
				byte p_2_2 = pxs[idx_2_2] ;
				
				int pixel = p_1_1 & 0xff ;
				
				if (pixel > 64) {
					pxs2[idx_1_1] = 0 ;
					continue ;
				}
				
				boolean verticalPixel = false ;
				
				if ( p_1_0 == p_1_1 && p_1_1 == p_1_2 ) {
					boolean isLeftBlank = ( (p_0_1 & 0xff) > 64 ) &&  p_0_0 == p_0_1 && p_0_1 == p_0_2 ;
					boolean isRightBlank = ( (p_2_1 & 0xff) > 64 ) &&  p_2_0 == p_2_1 && p_2_1 == p_2_2 ;
					
					if (isLeftBlank || isRightBlank) {
						verticalPixel = true ;
					}
				}
				
				if (verticalPixel) {
					pxs2[idx_1_1] = (byte) 255 ;
				}
				else {
					pxs2[idx_1_1] = 0 ;
				}
				
			}	
		}
		
		////////////////////////////////////
		
		for (int expandI = 0; expandI < 2; expandI++) {

			for (int i = cutImgIdent; i < limit; i++) {
				for (int j = hInit; j < hEnd; j++) {
					int idx_1_0 = ((j-1)*w) + i ;
					int idx_1_1 = (j*w) + i ;
					int idx_1_2 = ((j+1)*w) + i ;
					
					byte p_1_0 = pxs2[idx_1_0] ;
					byte p_1_1 = pxs2[idx_1_1] ;
					byte p_1_2 = pxs2[idx_1_2] ;
					
					if (p_1_1 == 0) continue ;
					
					if ( p_1_0 == 0 && p_1_2 != 0 ) {
						pxs2[idx_1_0] = (byte) 255 ;
					}
					else if ( p_1_0 != 0 && p_1_2 == 0 ) {
						pxs2[idx_1_0] = (byte) 255 ;
					}
				}	
			}
			
		}
		
		////////////////////////////////////		
		
		float[] lineVerticals = new float[cutImgW] ;
		
		for (int i = cutImgIdent; i < limit; i++) {
			
			int countVerticals = 0 ;
			boolean lastBlank = false ;
			
			for (int j = hInit; j < hEnd; j++) {
				int idx_1_1 = (j*w) + i ;
				
				byte p_1_1 = pxs2[idx_1_1] ;
				
				if ( p_1_1 == 0 ) {
					if (lastBlank) {
						lineVerticals[i-cutImgIdent] += countVerticals * countVerticals ;
					}
					
					countVerticals = countVerticals/2 ;
					
					lastBlank = false ;
					continue ;
				}
				else {
					countVerticals++ ;
					lastBlank = true ;
					
					//countVerticals = Math.min(countVerticals, 5) ;
					
					//lineVerticals[i-cutImgIdent] += countVerticals ;//* countVerticals ;
				}
				
				
			}	
		}
		
		////////////////////////////////////		
		
		img.pixelsC1 = pxs2 ;
		

		/*
		float mean = StatisticsUtils.calcMean(lineVerticals) ;
		float deviation = StatisticsUtils.calcStandardDeviation(lineVerticals) ;
		
		float maxDev = deviation * 1f ;
		
		float max = mean + maxDev ;
		
		for (int i = 0; i < lineVerticals.length; i++) {
			float v = lineVerticals[i] ;
			
			float diff = v-mean ;
			
			if (diff > maxDev) {
				lineVerticals[i] = 100 ;
			}
			else {
				lineVerticals[i] = 0 ;
			}
		}
		*/
		
		return lineVerticals ;
	}
	
	private float[] calcVerticality3(ImagePixels img) {

		byte[] pxs = img.getPixelsC1() ;
		
		int w = img.getWidth() ;
		int h = img.getHeight() ;
		
		int hInit = (h/5) ;
		int hEnd = h-hInit ;
		
		int cutImgIdent = (w - minimalShearImageWidth) / 2 ;
		int cutImgW = ((w/2) - cutImgIdent) /2 ;
		
		float[] lineVerticals = new float[cutImgW] ;
		
		int limit = cutImgIdent + cutImgW ;
		
		byte[] pxs2 = pxs.clone() ;
		
		for (int i = cutImgIdent; i < limit; i++) {
			
			int countVerticalPixels = 0 ;
			
			for (int j = hInit; j < hEnd; j++) {
				int idx_0_0 = ((j-1)*w) + (i-1) ;
				int idx_0_1 = (j*w) + (i-1) ;
				int idx_0_2 = ((j+1)*w) + (i-1) ;
				
				int idx_1_0 = ((j-1)*w) + i ;
				int idx_1_1 = (j*w) + i ;
				int idx_1_2 = ((j+1)*w) + i ;
				
				int idx_2_0 = ((j-1)*w) + (i+1) ;
				int idx_2_1 = (j*w) + (i+1) ;
				int idx_2_2 = ((j+1)*w) + (i+1) ;
				
				byte p_0_0 = pxs[idx_0_0] ;
				byte p_0_1 = pxs[idx_0_1] ;
				byte p_0_2 = pxs[idx_0_2] ;
				
				byte p_1_0 = pxs[idx_1_0] ;
				byte p_1_1 = pxs[idx_1_1] ;
				byte p_1_2 = pxs[idx_1_2] ;
				
				byte p_2_0 = pxs[idx_2_0] ;
				byte p_2_1 = pxs[idx_2_1] ;
				byte p_2_2 = pxs[idx_2_2] ;
				
				int pixel = p_1_1 & 0xff ;
				
				if (pixel > 64) {
					pxs2[idx_1_1] = 0 ;
					countVerticalPixels = 0 ;
					continue ;
				}
				
				boolean verticalPixel = false ;
				
				if ( p_1_0 == p_1_1 && p_1_1 == p_1_2 ) {
					boolean isLeftBlank = ( (p_0_1 & 0xff) > 64 ) &&  p_0_0 == p_0_1 && p_0_1 == p_0_2 ;
					boolean isRightBlank = ( (p_2_1 & 0xff) > 64 ) &&  p_2_0 == p_2_1 && p_2_1 == p_2_2 ;
					
					if (isLeftBlank || isRightBlank) {
						verticalPixel = true ;
					}
				}
				
				if (verticalPixel) {
					lineVerticals[i-cutImgIdent] += countVerticalPixels*countVerticalPixels ;
					pxs2[idx_1_1] = (byte) 255 ;
					
					countVerticalPixels++ ;
					if (countVerticalPixels > 4) countVerticalPixels = 4 ;
				}
				else {
					pxs2[idx_1_1] = 0 ;
					countVerticalPixels-- ;
					if (countVerticalPixels < 0) countVerticalPixels = 0 ;
				}
				
			}	
		}
		
		img.pixelsC1 = pxs2 ;
		
		float mean = StatisticsUtils.calcMean(lineVerticals) ;
		float deviation = StatisticsUtils.calcStandardDeviation(lineVerticals) ;
		
		float maxDev = deviation * 1f ;
		
		float max = mean + maxDev ;
		
		for (int i = 0; i < lineVerticals.length; i++) {
			float v = lineVerticals[i] ;
			
			float diff = v-mean ;
			
			if (diff > maxDev) {
				lineVerticals[i] = 100 ;
			}
			else {
				lineVerticals[i] = 0 ;
			}
		}
		
		return lineVerticals ;
	}
	
	private float[] calcVerticality2(ImagePixels img) {
		
		byte[] pxs = img.getPixelsC1() ;
		
		int w = img.getWidth() ;
		int h = img.getHeight() ;
		
		int hMin1 = h-1 ;
		
		int cutImgIdent = (w - minimalShearImageWidth) / 2 ;
		int cutImgW = (w/2) - cutImgIdent ;
		
		float[] lineVerticals = new float[cutImgW] ;
		
		int limit = cutImgIdent + cutImgW ;
		
		for (int i = cutImgIdent; i < limit; i++) {
			for (int j = 1; j < hMin1; j++) {
				int idx = (j*w) + i ;
				
				byte p = pxs[idx] ;
				int pixel = p & 0xff ;
				
				// ignore light pixels:
				if (pixel > 64) continue ;
				
				/*
				for (int k = 1; k <= 2; k++) {
					int jPrev = j-k ;
					if (jPrev < 0) continue ;
					
					int jNext = j+k ;
					if (jNext >= h) continue ;
					
					int idxPrev = (jPrev*w) + i ;
					int idxNext = (jNext*w) + i ;
					
					byte pPrev = pxs[idxPrev] ;
					byte pNext = pxs[idxNext] ;
					
					if ( p == pPrev && p == pNext ) {
						lineVerticals[i-cutImgIdent] += 1 ;	
					}
				}
				*/
				
				
				byte pPrev = pxs[idx-1] ;
				byte pNext = pxs[idx+1] ;
				
				if ( p == pPrev && p == pNext ) {
					lineVerticals[i-cutImgIdent] += 1 ;	
				}
				
				//lineVerticals[i-cutImgIdent] += 1 ;
			}	
		}
		
		float mean = StatisticsUtils.calcMean(lineVerticals) ;
		float max = mean*2 ;
		
		ArrayUtils.clip(lineVerticals, 0, max) ;
		lineVerticals = StatisticsUtils.normalize(lineVerticals, 0, max) ;
		
		for (int i = 0; i < lineVerticals.length; i++) {
			float v = lineVerticals[i] ;
			
			if (v >= 0.55) {
				lineVerticals[i] = 1 ;
			}
			else {
				lineVerticals[i] = 0 ;	
			}
		}
		
		lineVerticals = StatisticsUtils.scale(lineVerticals, max) ;
		
		return lineVerticals ;
		
		/*
		float[] minMax = StatisticsUtils.calcMinMax(lineVerticals) ;
		float max = minMax[1] ;
		
		float highPoint = max * 0.75f ;
		
		for (int i = 0; i < lineVerticals.length; i++) {
			float v = lineVerticals[i] ;
			lineVerticals[i] = v > highPoint ? 1 : 0 ;
		}
		
		lineVerticals = StatisticsUtils.scale(lineVerticals, max) ;
		
		return lineVerticals ;
		*/
		
		/*
		float[] minMax = StatisticsUtils.calcMinMax(lineVerticals) ;
		float max = minMax[1] ;
		
		lineVerticals = StatisticsUtils.normalize(lineVerticals, 0 , max) ;
		
		for (int i = 0; i < lineVerticals.length; i++) {
			float v = lineVerticals[i] ;
			lineVerticals[i] = (float) Math.pow(v, 7) ;
		}
		
		lineVerticals = StatisticsUtils.scale(lineVerticals, max) ;
	
		return lineVerticals ;
		*/
	}

}
