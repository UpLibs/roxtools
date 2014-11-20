package roxtools.img;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;

import roxtools.ArrayUtils;
import roxtools.ImageUtils;
import roxtools.RichConsole;
import roxtools.RichConsole.ChartBars;
import roxtools.StatisticsUtils;

final public class AutoHorizontalShearImage2 {

	private BufferedImage image ;
	private BufferedImage lowResolutionImage;
	
	static private final double MIN_SHEAR_LINE_RATIO = 0.60 ;
	static private final int GRAYSCALE_QUANTIFICATION_BITS = 7 ;
	private int minimalShearImageWidth;
	
	static final private boolean DEBUG = true ;
	
	public AutoHorizontalShearImage2(File imageFile) throws IOException {
		this( ImageIO.read(imageFile) ) ;
	}
	
	public AutoHorizontalShearImage2(BufferedImage image) {
		
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
		
		this.lowResolutionImage = imgYUV.scale(grayW, grayH).createImageLowColor(7).createImage() ;
		
		this.minimalShearImageWidth = (int) (grayW * MIN_SHEAR_LINE_RATIO);
		
		if (DEBUG) RichConsole.printLn( this.lowResolutionImage ) ;
		
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
			
			ShearVerticality shearVerticality = calcShearVerticality(lowResolutionImage, lineRatio) ;
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
		HorizontalShearImage shearImage = new HorizontalShearImage(lowResolutionImage, lineRatio) ;
		BufferedImage img2 = shearImage.createImageCropped(false) ;
		
		ImagePixels lowResImg2 = new ImagePixels(img2).createImageLowColor(7) ;
		
		float[] verticality = calcVerticality(lowResImg2) ;
		
		return new ShearVerticality(lineRatio , lowResImg2.createImage(), verticality) ;
	}
	
	private float[] calcVerticality(ImagePixels img) {

		int[] pxs = img.createRGBPixelsList() ;
		
		int w = img.getWidth() ;
		int h = img.getHeight() ;
		
		int hInit = (h/5) ;
		int hEnd = h-hInit ;
		
		int cutImgIdent = (w - minimalShearImageWidth) / 2 ;
		int cutImgW = ((w/2) - cutImgIdent) /2 ;
		
		int limit = cutImgIdent + cutImgW ;
		
		////////////////////////////////////
		
		float[] lineVerticals = new float[cutImgW] ;
		
		for (int i = cutImgIdent; i < limit; i++) {
			
			int countVerticals = 0 ;
			boolean lasEq = false ;
			
			for (int j = hInit; j < hEnd; j++) {
				int idx_1_0 = ((j-1)*w) + i ;
				int idx_1_1 = (j*w) + i ;
				
				int p_1_0 = pxs[idx_1_0] ;
				int p_1_1 = pxs[idx_1_1] ;
				
				boolean eqPx = p_1_0 == p_1_1 ;
				
				if (!eqPx) {
					if (lasEq) {
						lineVerticals[i-cutImgIdent] += (countVerticals * countVerticals * countVerticals) /1000f ;
					}
					
					countVerticals = 0 ;
					
					lasEq = false ;
				}
				else {
					countVerticals++ ;
					lasEq = true ;
				}
				
				
			}	
		}
		
		////////////////////////////////////		
		

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
	
	public static void main(String[] args) throws Exception {
		
		//File file = new File("/Volumes/SAFEZONE/products/fanta.jpg");
		
		//File file = new File("/Volumes/SAFEZONE/uppoints/mercado-test1/photo-test1.png");
		
		//File file = new File("/Volumes/SAFEZONE/uppoints/mercado-test1/full-img-2.png");
		
		//File file = new File("/Volumes/SAFEZONE/uppoints/mercado-test1/full-img-3.png");
		
		//File file = new File("/Volumes/SAFEZONE/uppoints/mercado-test1/full-img-4.png");
		
		//File file = new File("/Volumes/SAFEZONE/uppoints/mercado-test1/full-img-4.png");
		
		//File file = new File("/Volumes/SAFEZONE/uppoints/mercado-test1/full-imgs-save/full-img-7.png") ;
		
		//File file = new File("/Users/gracilianomp/minicam-test-stream2/saves-png/img-stream-498.png");
		
		File file = new File("/Volumes/SAFEZONE/uppoints/mercado-test1/img-stream-26.png");
		
		//RichConsole.printLn( new RichConsole.ConsoleImage( new ImagePixels(file).createImageLowColor(7).createImage() ) ) ;
		
		AutoHorizontalShearImage2 autoHorizontalShearImage = new AutoHorizontalShearImage2(file) ;
		
		
		
		BufferedImage imgFix = autoHorizontalShearImage.autoHorizontalShear() ;
		
		RichConsole.printLn( imgFix ) ;
		
		
	}
}
