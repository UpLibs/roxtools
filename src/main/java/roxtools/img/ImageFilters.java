package roxtools.img;

import com.jhlabs.image.ContrastFilter;
import com.jhlabs.image.GammaFilter;
import com.jhlabs.image.InvertFilter;
import com.jhlabs.image.SharpenFilter;
import com.jhlabs.image.UnsharpFilter;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

public class ImageFilters {
	
	public enum AutoFilter {
		SHARPEN_1,
		SHARPEN_2,
		SHARPEN_3,
		UNSHARPEN_1,
		UNSHARPEN_2,
		UNSHARPEN_3,
		SHARPEN_BALANCED,
		UNSHARPEN_BALANCED,
		;
		
		public BufferedImage apply(BufferedImage img) {
			
			switch (this) {
				case SHARPEN_1: return filterSharpen(img) ;
				case SHARPEN_2: return filterSharpenLoop(img, 2) ;
				case SHARPEN_3: return filterSharpenLoop(img, 3) ;
				case UNSHARPEN_1: return filterUnsharpen(img) ;
				case UNSHARPEN_2: return filterUnsharpenLoop(img, 2) ;
				case UNSHARPEN_3: return filterUnsharpenLoop(img, 3) ;
				case SHARPEN_BALANCED: return autoFilterUnsharpenBalanced(img) ;
				case UNSHARPEN_BALANCED: return autoFilterUnsharpenBalanced(img) ;
				default: throw new UnsupportedOperationException("Can't handle filter: "+ this) ;
			}
			
		}
	}

	public static BufferedImage filterSharpenLoop(BufferedImage img, int loops) {
		for (int i = 0 ; i < loops ; i++) {
			img = filterSharpen(img) ;
		}

		return img ;
	}
	
	public static BufferedImage filterSharpen(BufferedImage img) {
		SharpenFilter filter = new SharpenFilter() ;
		
		BufferedImage dst = filter.createCompatibleDestImage(img, ColorModel.getRGBdefault()) ;
		
		filter.filter(img , dst) ;
		
		return dst ;
	}
	
	public static BufferedImage filterUnsharpenLoop(BufferedImage img, int loops) {
		for (int i = 0 ; i < loops ; i++) {
			img = filterUnsharpen(img) ;
		}

		return img ;
	}
	
	public static BufferedImage filterUnsharpen(BufferedImage img) {
		UnsharpFilter filter = new UnsharpFilter() ;
		
		BufferedImage dst = filter.createCompatibleDestImage(img, ColorModel.getRGBdefault()) ;
		
		filter.filter(img , dst) ;
		
		return dst ;
	}
	
	public static BufferedImage filterGrayScale(BufferedImage img) {
		ImagePixels imagePixels = new ImagePixels(img) ;
		imagePixels.convertToYUV() ;
		
		return new ImagePixels( imagePixels.getPixelsC1() , imagePixels.getWidth() , imagePixels.getHeight() , true ).createImage() ;
	}
	
	public static BufferedImage filterInvert(BufferedImage img) {
		InvertFilter filter = new InvertFilter() ;
		
		BufferedImage dst = filter.createCompatibleDestImage(img, ColorModel.getRGBdefault()) ;
		
		filter.filter(img, dst) ;
		
		return dst ;
	}
	
	public static BufferedImage filterContrast(BufferedImage img, double bright, double contrast) {
		ContrastFilter filter = new ContrastFilter() ;
		
		filter.setBrightness((float)bright);
		filter.setContrast((float)contrast);
		
		BufferedImage dst = filter.createCompatibleDestImage(img, ColorModel.getRGBdefault()) ;
		
		filter.filter(img, dst) ;
		
		return dst ;
	}
	
	public static BufferedImage filterGamma(BufferedImage img, double gamma) {
		GammaFilter filter = new GammaFilter() ;
		
		filter.setGamma((float)gamma) ;
		
		BufferedImage dst = filter.createCompatibleDestImage(img, ColorModel.getRGBdefault()) ;
		
		filter.filter(img, dst) ;
		
		return dst ;
	}
	
	public static BufferedImage mergeLight(BufferedImage img1, BufferedImage img2, double ratio) {
		ImagePixels imagePixels1 = new ImagePixels(img1) ;
		ImagePixels imagePixels2 = new ImagePixels(img2) ;
		
		imagePixels1.convertToYUV() ;
		imagePixels2.convertToYUV() ;
		
		byte[] y1 = imagePixels1.getPixelsC1() ;
		byte[] y2 = imagePixels2.getPixelsC1() ;
		
		byte[] y = new byte[y1.length] ;
		
		float r1 = (float) ( 1-ratio ) ;
		float r2 = (float) ( ratio ) ;
		
		for (int i = 0; i < y.length; i++) {
			int b1 = y1[i] & 0xff ;
			int b2 = y2[i] & 0xff ;
			
			int b = (int) ((b1 * r1) + (b2 * r2)) ;
			
			y[i] = (byte) b ;
		}
		
		return new ImagePixels( y , imagePixels1.getPixelsC2().clone() , imagePixels1.getPixelsC3().clone() , imagePixels1.getWidth() , imagePixels1.getHeight() , true ).createImage() ;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	
	public static BufferedImage autoFilterSharpenBalanced(BufferedImage img) {
		BufferedImage imgSharpen3 = filterSharpenLoop(img, 3) ;
		
		BufferedImage imgSharpenMerge = mergeLight(img , imgSharpen3, 0.80) ;
		
		BufferedImage imgSharpen = imgSharpenMerge ;
		
		BufferedImage mask = filterContrast( filterInvert( filterGrayScale(imgSharpen) ) , 0.20, 1.50) ;
		
		BufferedImage img3 = filterGamma( filterContrast( mergeLight(imgSharpen , mask, 0.20) , 1.1 , 1.4 )  , 1.3 ) ;
		
		return img3 ;
	}
	

	public static BufferedImage autoFilterUnsharpenBalanced(BufferedImage img) {
		BufferedImage imgSharpen3 = filterUnsharpenLoop(img, 2) ;
		
		BufferedImage imgSharpenMerge = mergeLight(img , imgSharpen3, 0.80) ;
		
		BufferedImage imgSharpen = imgSharpenMerge ;
		
		BufferedImage mask = filterContrast( filterInvert( filterGrayScale(imgSharpen) ) , 0.20, 1.50) ;
		
		BufferedImage img3 = filterGamma( filterContrast( mergeLight(imgSharpen , mask, 0.20) , 1.1 , 1.4 )  , 1.3 ) ;
		
		return img3 ;
	}

}
