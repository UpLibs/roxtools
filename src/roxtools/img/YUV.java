package roxtools.img;

import java.text.DecimalFormat;
import java.util.Random;

import roxtools.math.FastMathSqrt;
import roxtools.math.FastMathSqrtInt;

public class YUV {
	
	static final public DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###") ;
	static final private FastMathSqrt FAST_MATH_SQRT = new FastMathSqrt(1024*128, 256*256*2) ;
	static final private FastMathSqrtInt FAST_MATH_SQRT_INT = new FastMathSqrtInt(1024*65, 256*256*2) ;
	
	static public float[] pixelRGB_to_arrayYUV(int pixelRGB) {
		float[] yuv = new float[3] ;
		pixelRGB_to_arrayYUV(pixelRGB, yuv) ;
		return yuv ;
	}
	
	static public void RGB_to_arrayYUV(int r, int g, int b, float[] yuv) {
		float y = r *  0.299000f + g *  0.587000f + b *  0.114000f ;
		float u = r * -0.168736f + g * -0.331264f + b *  0.500000f + 128.50f ;
		float v = r *  0.500000f + g * -0.418688f + b * -0.081312f + 128.50f ;
	
		yuv[0] = y ;
		yuv[1] = u ;
		yuv[2] = v ;
	}
	
	static public void RGB_to_arrayYUV(int r, int g, int b, int[] yuv) {
		float y = r *  0.299000f + g *  0.587000f + b *  0.114000f ;
		float u = r * -0.168736f + g * -0.331264f + b *  0.500000f + 128.50f ;
		float v = r *  0.500000f + g * -0.418688f + b * -0.081312f + 128.50f ;
	
		yuv[0] = (int) y ;
		yuv[1] = (int) u ;
		yuv[2] = (int) v ;
	}
	
	static public void RGB_to_arrayYUV(int r, int g, int b, byte[] yuv) {
		float y = r *  0.299000f + g *  0.587000f + b *  0.114000f ;
		float u = r * -0.168736f + g * -0.331264f + b *  0.500000f + 128.50f ;
		float v = r *  0.500000f + g * -0.418688f + b * -0.081312f + 128.50f ;
	
		yuv[0] = (byte) y ;
		yuv[1] = (byte) u ;
		yuv[2] = (byte) v ;
	}
	
	static public void pixelRGB_to_arrayYUV(int pixelRGB, float[] yuv) {
		int r = (pixelRGB >> 16) & 0xff ;
		int g = (pixelRGB >> 8) & 0xff ;
		int b = pixelRGB & 0xff ;
		
		
		float y = r *  0.299000f + g *  0.587000f + b *  0.114000f ;
		float u = r * -0.168736f + g * -0.331264f + b *  0.500000f + 128.50f ;
		float v = r *  0.500000f + g * -0.418688f + b * -0.081312f + 128.50f ;
	
		yuv[0] = y ;
		yuv[1] = u ;
		yuv[2] = v ;
	}
	
	static public void pixelRGB_to_arrayYUV(int pixelRGB, byte[] yuv) {
		int r = (pixelRGB >> 16) & 0xff ;
		int g = (pixelRGB >> 8) & 0xff ;
		int b = pixelRGB & 0xff ;
		
		
		float y = r *  0.299000f + g *  0.587000f + b *  0.114000f ;
		float u = r * -0.168736f + g * -0.331264f + b *  0.500000f + 128.50f ;
		float v = r *  0.500000f + g * -0.418688f + b * -0.081312f + 128.50f ;
			 
		yuv[0] = (byte) (y) ;
		yuv[1] = (byte) (u > 255 ? 255 : u) ;
		yuv[2] = (byte) (v > 255 ? 255 : v) ;
	}
	
	static public void pixelRGB_to_arrayYUV(int pixelRGB, int[] yuv) {
		int r = (pixelRGB >> 16) & 0xff ;
		int g = (pixelRGB >> 8) & 0xff ;
		int b = pixelRGB & 0xff ;
		
		
		 float y = r *  0.299000f + g *  0.587000f + b *  0.114000f ;
		 float u = r * -0.168736f + g * -0.331264f + b *  0.500000f + 128.50f ;
		 float v = r *  0.500000f + g * -0.418688f + b * -0.081312f + 128.50f ;
	
		 yuv[0] = (int) (y) ;
		 yuv[1] = (int) (u > 255 ? 255 : u) ;
		 yuv[2] = (int) (v > 255 ? 255 : v) ;
	}
	
	static public void pixelRGB_to_arrayYUV_fast(int pixelRGB, int[] yuv) {
		int r = (pixelRGB >> 16) & 0xff ;
		int g = (pixelRGB >> 8) & 0xff ;
		int b = pixelRGB & 0xff ;
		
		int y = clip(( (  66 * r + 129 * g +  25 * b + 128) >> 8) +  16) ;
		int u = clip(( ( -38 * r -  74 * g + 112 * b + 128) >> 8) + 128) ;
		int v = clip(( ( 112 * r -  94 * g -  18 * b + 128) >> 8) + 128) ;
		
		yuv[0] = y ;
		yuv[1] = u ;
		yuv[2] = v ;
	}
	
	static public void pixelRGB_to_arrayYUV_fast(int pixelRGB, byte[] yuv) {
		int r = (pixelRGB >> 16) & 0xff ;
		int g = (pixelRGB >> 8) & 0xff ;
		int b = pixelRGB & 0xff ;
		
		int y = clip(( (  66 * r + 129 * g +  25 * b + 128) >> 8) +  16) ;
		int u = clip(( ( -38 * r -  74 * g + 112 * b + 128) >> 8) + 128) ;
		int v = clip(( ( 112 * r -  94 * g -  18 * b + 128) >> 8) + 128) ;
		
		yuv[0] = (byte) y ;
		yuv[1] = (byte) u ;
		yuv[2] = (byte) v ;
	}
	

	static public void RGB_to_arrayYUV_fast(int r, int g, int b, int[] yuv) {
		int y = clip(( (  66 * r + 129 * g +  25 * b + 128) >> 8) +  16) ;
		int u = clip(( ( -38 * r -  74 * g + 112 * b + 128) >> 8) + 128) ;
		int v = clip(( ( 112 * r -  94 * g -  18 * b + 128) >> 8) + 128) ;
		
		yuv[0] = y ;
		yuv[1] = u ;
		yuv[2] = v ;
	}
	
	static public void RGB_to_arrayYUV_fast(int r, int g, int b, byte[] yuv) {
		int y = clip(( (  66 * r + 129 * g +  25 * b + 128) >> 8) +  16) ;
		int u = clip(( ( -38 * r -  74 * g + 112 * b + 128) >> 8) + 128) ;
		int v = clip(( ( 112 * r -  94 * g -  18 * b + 128) >> 8) + 128) ;
		
		yuv[0] = (byte) y ;
		yuv[1] = (byte) u ;
		yuv[2] = (byte) v ;
	}
	
	static public int arrayYUV_to_pixelRGB(float[] yuv) {
		return YUV_to_pixelRGB(yuv[0], yuv[1], yuv[2]) ;
	}
	
	static public int arrayYUV_to_pixelRGB(byte[] yuv) {
		return YUV_to_pixelRGB( yuv[0] & 0xFF , yuv[1] & 0xFF , yuv[2] & 0xFF) ;
	}
	
	static public int arrayYUV_to_pixelRGB(int[] yuv) {
		return YUV_to_pixelRGB(yuv[0] , yuv[1] , yuv[2]) ;
	}
	
	static public void arrayYUV_to_pixelRGB(float[] yuv, int[] rgb) {
		YUV_to_arrayRGB(yuv[0], yuv[1], yuv[2], rgb) ;
	}
	
	static public void arrayYUV_to_arrayRGB(int[] yuv, int[] rgb) {
		YUV_to_arrayRGB(yuv[0], yuv[1], yuv[2], rgb) ;
	}
	
	static private final float FLOAT_FIX = 0.3333333333333f ;
	
	static public void YUV_to_arrayRGB(float y, float u, float v, int[] rgb) {
		y += FLOAT_FIX ;
		v -= 128f - FLOAT_FIX ;
		u -= 128f - FLOAT_FIX ;
		
		int r = clip( (int) (y + 1.4075f * v) );
		int g = clip( (int) (y - 0.3455f * u - (0.7169f * v)) );
		int b = clip( (int) (y + 1.7790f * u) );
		
		rgb[0] = r ;
		rgb[1] = g ;
		rgb[2] = b ;
	}
	
	static public void YUV_to_arrayRGB(float y, float u, float v, byte[] rgb) {
		y += FLOAT_FIX ;
		v -= 128f - FLOAT_FIX ;
		u -= 128f - FLOAT_FIX ;
		
		int r = clip( (int) (y + 1.4075f * v) );
		int g = clip( (int) (y - 0.3455f * u - (0.7169f * v)) );
		int b = clip( (int) (y + 1.7790f * u) );
		
		rgb[0] = (byte) r ;
		rgb[1] = (byte) g ;
		rgb[2] = (byte) b ;
	}
	
	static public int YUV_to_pixelRGB(float y, float u, float v) {
		y += FLOAT_FIX ;
		v -= 128f - FLOAT_FIX ;
		u -= 128f - FLOAT_FIX ;
		
		int r = clip( (int) (y + 1.4075f * v) );
		int g = clip( (int) (y - 0.3455f * u - (0.7169f * v)) );
		int b = clip( (int) (y + 1.7790f * u) );
		
		int c = (r << 16) | (g << 8) | b ;
		
		return c ;
	}
	
	static public void YUVarrays_to_RGBarray_fast(byte[] yAr, byte[] uAr, byte[] vAr, int[] rgbAr) {
		for (int i = rgbAr.length-1 ; i >= 0; i--) {
			int y = yAr[i] & 0xFF ;
			int u = uAr[i] & 0xFF ;
			int v = vAr[i] & 0xFF ;
		
			y -= 16 ;
			u -= 128 ;
			v -= 128 ;
			
			int r = clip( (298 * y                 + 409 * v   + 128) >> 8 ) ;
			int g = clip( (298 * y   -   100 * u   - 208 * v   + 128) >> 8 ) ;
			int b = clip( (298 * y   +   516 * u               + 128) >> 8 ) ;
			
			rgbAr[i] = (r << 16) | (g << 8) | b ;
		}
	}
	
	static public void YUV_to_arrayRGB_fast(int y, int u, int v, int[] rgb) {
		y -= 16 ;
		u -= 128 ;
		v -= 128 ;
		
		int r = clip( (298 * y                 + 409 * v   + 128) >> 8 ) ;
		int g = clip( (298 * y   -   100 * u   - 208 * v   + 128) >> 8 ) ;
		int b = clip( (298 * y   +   516 * u               + 128) >> 8 ) ;
		
		rgb[0] = r ;
		rgb[1] = g ;
		rgb[2] = b ;
	}
	
	static public void YUV_to_arrayRGB_fast(int y, int u, int v, byte[] rgb) {
		y -= 16 ;
		u -= 128 ;
		v -= 128 ;
		
		int r = clip( (298 * y                 + 409 * v   + 128) >> 8 ) ;
		int g = clip( (298 * y   -   100 * u   - 208 * v   + 128) >> 8 ) ;
		int b = clip( (298 * y   +   516 * u               + 128) >> 8 ) ;
		
		rgb[0] = (byte) r ;
		rgb[1] = (byte) g ;
		rgb[2] = (byte) b ;
	}
	
	static public int YUV_to_pixelRGB_fast(int y, int u, int v) {
		y -= 16 ;
		u -= 128 ;
		v -= 128 ;
		
		int r = clip( (298 * y                 + 409 * v   + 128) >> 8 ) ;
		int g = clip( (298 * y   -   100 * u   - 208 * v   + 128) >> 8 ) ;
		int b = clip( (298 * y   +   516 * u               + 128) >> 8 ) ;
		
		int c = (r << 16) | (g << 8) | b ;
		
		return c ;
	}
	
	static private int clip(int i) {
		return i < 0 ? 0 : (i > 255 ? 255 : i) ;
	}
	
	static public double distance(int pixelRGB1, int pixelRGB2) {
		float y1 , u1, v1 ;
		
		int r = (pixelRGB1 >> 16) & 0xff ;
		int g = (pixelRGB1 >> 8) & 0xff ;
		int b = pixelRGB1 & 0xff ;
		
		y1 = r *  0.299000f + g *  0.587000f + b *  0.114000f ;
		u1 = r * -0.168736f + g * -0.331264f + b *  0.500000f + 128.50f ;
		v1 = r *  0.500000f + g * -0.418688f + b * -0.081312f + 128.50f ;

		///////////////////////////////

		r = (pixelRGB2 >> 16) & 0xff ;
		g = (pixelRGB2 >> 8) & 0xff ;
		b = pixelRGB2 & 0xff ;
		
		float y2 , u2, v2 ;
		
		y2 = r *  0.299000f + g *  0.587000f + b *  0.114000f ;
		u2 = r * -0.168736f + g * -0.331264f + b *  0.500000f + 128.50f ;
		v2 = r *  0.500000f + g * -0.418688f + b * -0.081312f + 128.50f ;
		
		return distance(y1, u1, v1, y2, u2, v2) ;
	}
	
	static public double distance(float y1, float u1, float v1 , float y2, float u2, float v2) {
		float distY = y1 - y2 ;
		float distU = u1 - u2 ;
		float distV = v1 - v2 ;
		
		return FAST_MATH_SQRT.calcSimple( distY*distY + distU*distU + distV*distV ) ;
	}
	
	static public boolean isSimilar(int pixelRGB1, int pixelRGB2, double tolerance) {
		float y1 , u1, v1 ;
		
		int r = (pixelRGB1 >> 16) & 0xff ;
		int g = (pixelRGB1 >> 8) & 0xff ;
		int b = pixelRGB1 & 0xff ;
		
		y1 = r *  0.299000f + g *  0.587000f + b *  0.114000f ;
		u1 = r * -0.168736f + g * -0.331264f + b *  0.500000f + 128.50f ;
		v1 = r *  0.500000f + g * -0.418688f + b * -0.081312f + 128.50f ;

		///////////////////////////////

		r = (pixelRGB2 >> 16) & 0xff ;
		g = (pixelRGB2 >> 8) & 0xff ;
		b = pixelRGB2 & 0xff ;
		
		float y2 , u2, v2 ;
		
		y2 = r *  0.299000f + g *  0.587000f + b *  0.114000f ;
		u2 = r * -0.168736f + g * -0.331264f + b *  0.500000f + 128.50f ;
		v2 = r *  0.500000f + g * -0.418688f + b * -0.081312f + 128.50f ;
		
		return isSimilar(y1, u1, v1, y2, u2, v2, tolerance) ;
	}
	
	static public boolean isSimilarRGB(int r, int g, int b , int r2, int g2, int b2, double tolerance) {
		float y1 , u1, v1 ;
		
		y1 = r *  0.299000f + g *  0.587000f + b *  0.114000f ;
		u1 = r * -0.168736f + g * -0.331264f + b *  0.500000f + 128.50f ;
		v1 = r *  0.500000f + g * -0.418688f + b * -0.081312f + 128.50f ;

		///////////////////////////////
		
		float y2 , u2, v2 ;
		
		y2 = r2 *  0.299000f + g2 *  0.587000f + b2 *  0.114000f ;
		u2 = r2 * -0.168736f + g2 * -0.331264f + b2 *  0.500000f + 128.50f ;
		v2 = r2 *  0.500000f + g2 * -0.418688f + b2 * -0.081312f + 128.50f ;
		
		return isSimilar(y1, u1, v1, y2, u2, v2, tolerance) ;
	}
	
	static public boolean isSimilar(float y1, float u1, float v1 , float y2, float u2, float v2, double tolerance) {
		float distY = y1 - y2 ;
		float distU = u1 - u2 ;
		float distV = v1 - v2 ;
		
		double distYUV = FAST_MATH_SQRT.calcSimple( distY*distY + distU*distU + distV*distV ) ;
		
		return distYUV/255D <= tolerance ;
	}
	
	static public boolean isSimilar(int y1, int u1, int v1 , int y2, int u2, int v2, double tolerance) {
		int distY = y1 - y2 ;
		int distU = u1 - u2 ;
		int distV = v1 - v2 ;
		
		int distYUV = FAST_MATH_SQRT_INT.calcSimple( distY*distY + distU*distU + distV*distV ) ;
		
		return distYUV/255D <= tolerance ;
	}
	
	static public boolean isSimilar_Precise(int y1, int u1, int v1 , int y2, int u2, int v2, double tolerance) {
		int distY = y1 - y2 ;
		int distU = u1 - u2 ;
		int distV = v1 - v2 ;
		
		double distYUV = Math.sqrt( distY*distY + distU*distU + distV*distV ) ;
		
		return distYUV/255D <= tolerance ;
	}
	
	static public double getDistance_Precise(int y1, int u1, int v1 , int y2, int u2, int v2) {
		int distY = y1 - y2 ;
		int distU = u1 - u2 ;
		int distV = v1 - v2 ;
		
		double distYUV = Math.sqrt( distY*distY + distU*distU + distV*distV ) ;
		
		return distYUV ;
	}
		
	static public boolean isSimilar_IntegerTolerance(int y1, int u1, int v1 , int y2, int u2, int v2, int tolerance) {
		int diffSum = ( (y1 ^ y2) | (u1 ^ u2) | (v1 ^ v2) ) & 0x7FFFFFFF ;
		if ( diffSum <= 2 ) return true ;
		
		int distY = y1 - y2 ;
		int distU = u1 - u2 ;
		int distV = v1 - v2 ;
		
		int distYUV = FAST_MATH_SQRT_INT.calcSimple( distY*distY + distU*distU + distV*distV ) ;
				
		return distYUV <= tolerance ;
	}
	
	static public double[] getSimilarityColorAndLightRatio(int y1, int u1, int v1 , int y2, int u2, int v2) {
		int[] ret = getSimilarityColorAndLight(y1, u1, v1, y2, u2, v2) ;
		return new double[] { ret[0]/255d , ret[1]/255d } ;
	}
	
	static public int[] getSimilarityColorAndLight(int y1, int u1, int v1 , int y2, int u2, int v2) {
		int distU = u1 - u2 ;
		int distV = v1 - v2 ;
		
		int distUV = FAST_MATH_SQRT_INT.calcSimple( distU*distU + distV*distV ) ;
		
		int distY = y1 - y2 ;

		int dist = FAST_MATH_SQRT_INT.calcSimple( distY*distY + distUV*distUV ) ;
		
		return new int[] { distUV , dist } ;
	}
	
	static public ImagePixels mergePixels( ImagePixels imagePixels1 , ImagePixels imagePixels2) {

		imagePixels1.convertToYUV() ;
		imagePixels2.convertToYUV() ;
		byte[] pixelsC1A = imagePixels1.getPixelsC1() ;
		byte[] pixelsC2A = imagePixels1.getPixelsC2() ;
		byte[] pixelsC3A = imagePixels1.getPixelsC3() ;

		byte[] pixelsC1B = imagePixels2.getPixelsC1() ;
		byte[] pixelsC2B = imagePixels2.getPixelsC2() ;
		byte[] pixelsC3B = imagePixels2.getPixelsC3() ;
		
		byte[] mergeC1 = new byte[pixelsC1A.length] ;
		byte[] mergeC2 = new byte[pixelsC1A.length] ;
		byte[] mergeC3 = new byte[pixelsC1A.length] ;
		
		for (int i = 0; i < pixelsC1A.length; i++) {
			
			byte c1A = pixelsC1A[i] ;
			byte c2A = pixelsC2A[i] ;
			byte c3A = pixelsC3A[i] ;
			
			byte c1B = pixelsC1B[i] ;
			byte c2B = pixelsC2B[i] ;
			byte c3B = pixelsC3B[i] ;
			
			mergeC1[i] = (byte) (((c1A & 0xFF) + (c1B & 0xFF)) / 2) ;
			mergeC2[i] = (byte) (((c2A & 0xFF) + (c2B & 0xFF)) / 2) ;
			mergeC3[i] = (byte) (((c3A & 0xFF) + (c3B & 0xFF)) / 2) ;
		}
		
		return new ImagePixels(mergeC1, mergeC2, mergeC3 , imagePixels1.getWidth(), imagePixels1.getHeight() , true) ;
	}
	
	static public ImagePixels mergePixels( ImagePixels imagePixels1 , ImagePixels imagePixels2 , float mergeRatio) {

		float part1Ratio = mergeRatio ;
		float part2Ratio = 1 - mergeRatio ;
		
		imagePixels1.convertToYUV() ;
		imagePixels2.convertToYUV() ;
		byte[] pixelsC1A = imagePixels1.getPixelsC1() ;
		byte[] pixelsC2A = imagePixels1.getPixelsC2() ;
		byte[] pixelsC3A = imagePixels1.getPixelsC3() ;

		byte[] pixelsC1B = imagePixels2.getPixelsC1() ;
		byte[] pixelsC2B = imagePixels2.getPixelsC2() ;
		byte[] pixelsC3B = imagePixels2.getPixelsC3() ;
		
		byte[] mergeC1 = new byte[pixelsC1A.length] ;
		byte[] mergeC2 = new byte[pixelsC1A.length] ;
		byte[] mergeC3 = new byte[pixelsC1A.length] ;
		
		for (int i = 0; i < pixelsC1A.length; i++) {
			
			byte c1A = pixelsC1A[i] ;
			byte c2A = pixelsC2A[i] ;
			byte c3A = pixelsC3A[i] ;
			
			byte c1B = pixelsC1B[i] ;
			byte c2B = pixelsC2B[i] ;
			byte c3B = pixelsC3B[i] ;
			
			mergeC1[i] = (byte) ( ((c1A & 0xFF) * part1Ratio) + ((c1B & 0xFF) * part2Ratio) ) ;
			mergeC2[i] = (byte) ( ((c2A & 0xFF) * part1Ratio) + ((c2B & 0xFF) * part2Ratio) ) ;
			mergeC3[i] = (byte) ( ((c3A & 0xFF) * part1Ratio) + ((c3B & 0xFF) * part2Ratio) ) ;
		}
		
		return new ImagePixels(mergeC1, mergeC2, mergeC3 , imagePixels1.getWidth(), imagePixels1.getHeight() , true) ;
	}
	
	static public ImagePixels blurPixels( ImagePixels imagePixels1 , double blurSizeRatio) {
		return blurPixels(imagePixels1, blurSizeRatio, 1) ;	
	}
	
	static public ImagePixels blurPixels( ImagePixels imagePixels1 , double blurSizeRatio, double blurIntensity) {
		
		int blurSize = (int) Math.max(
				1 ,
				(Math.min( imagePixels1.getWidth() , imagePixels1.getHeight() ) * blurSizeRatio)
				) ;
		
		if (blurSizeRatio < 0) blurSize = - ((int) blurSizeRatio) ;
		
		return blurPixels(imagePixels1, blurSize, blurIntensity) ;
	}
	
	static public ImagePixels blurPixels( ImagePixels imagePixels1 , int blurSize ) {
		return blurPixels(imagePixels1, blurSize, 1) ;
	}
	
	static public ImagePixels blurPixels( ImagePixels imagePixels1 , int blurSize , double blurIntensity ) {
		if (blurSize < 0) blurSize = -blurSize ;
		
		imagePixels1.convertToYUV() ;

		int width = imagePixels1.getWidth() ;
		int height = imagePixels1.getHeight() ;
		
		byte[] pixelsC1A = imagePixels1.getPixelsC1() ;
		byte[] pixelsC2A = imagePixels1.getPixelsC2() ;
		byte[] pixelsC3A = imagePixels1.getPixelsC3() ;
		
		byte[] mergeC1 = new byte[pixelsC1A.length] ;
		byte[] mergeC2 = new byte[pixelsC1A.length] ;
		byte[] mergeC3 = new byte[pixelsC1A.length] ;
		
		int blurRange = blurSize + 1 + blurSize ;
		
		for (int j = 0; j < height; j++) {
			int jIdx = j*width ;
			
			for (int i = 0; i < width; i++) {
				int iIdx = jIdx + i ;
				
				int c1 = pixelsC1A[iIdx] & 0xFF ;
				int c2 = pixelsC2A[iIdx] & 0xFF ;
				int c3 = pixelsC3A[iIdx] & 0xFF ;
				
				int blur1 = 0 ;
				int blur2 = 0 ;
				int blur3 = 0 ;
				int blurCount = 0 ;
				
				for (int yShift = 0; yShift < blurRange; yShift++) {
					int y = j + (yShift-blurSize) ;
					if (y < 0 || y >= height) continue ;
					
					int yIdx = y * width ;
					
					for (int xShift = 0; xShift < blurRange; xShift++) {
						int x = i + (xShift - blurSize) ;
						if (x < 0 || x >= width) continue ;
						
						int xIdx = yIdx + x ;
						
						if (xIdx == iIdx) continue ;
						
						blur1 += pixelsC1A[xIdx] & 0xFF ;
						blur2 += pixelsC2A[xIdx] & 0xFF ;
						blur3 += pixelsC3A[xIdx] & 0xFF ;
						blurCount++ ;
					}
				}
				
				blur1 = blur1 / blurCount ;
				blur2 = blur2 / blurCount ;
				blur3 = blur3 / blurCount ;
				
				double blurCountWeight = blurCount * blurIntensity ;
				double blurCountTotal = blurCountWeight + 1d ;
				
				double cRatio = 1d / blurCountTotal ;
				double blurRatio = blurCountWeight / blurCountTotal ;
				
				blur1 = (int) ((c1 * cRatio) + (blur1 * blurRatio)) ;
				blur2 = (int) ((c2 * cRatio) + (blur2 * blurRatio)) ;
				blur3 = (int) ((c3 * cRatio) + (blur3 * blurRatio)) ;
				
				mergeC1[iIdx] = (byte) blur1 ;
				mergeC2[iIdx] = (byte) blur2 ;
				mergeC3[iIdx] = (byte) blur3 ;
			}
		}
		
		return new ImagePixels(mergeC1, mergeC2, mergeC3 , imagePixels1.getWidth(), imagePixels1.getHeight() , true) ;
	}
	
	
	static public ImagePixels noisePixels( ImagePixels imagePixels1 , long seed, double intensity) {
		
		Random random = new Random(seed) ;
		
		imagePixels1.convertToYUV() ;

		int width = imagePixels1.getWidth() ;
		int height = imagePixels1.getHeight() ;
		
		byte[] pixelsC1A = imagePixels1.getPixelsC1() ;
		byte[] pixelsC2A = imagePixels1.getPixelsC2() ;
		byte[] pixelsC3A = imagePixels1.getPixelsC3() ;
		
		byte[] mergeC1 = new byte[pixelsC1A.length] ;
		byte[] mergeC2 = new byte[pixelsC1A.length] ;
		byte[] mergeC3 = new byte[pixelsC1A.length] ;
		
		int noiseRange = (int) (intensity * 255) ;
		noiseRange = clip(noiseRange) ;
		int noiseRangeBase = noiseRange/2 ;
		
		for (int j = 0; j < height; j++) {
			int jIdx = j*width ;
			
			for (int i = 0; i < width; i++) {
				int iIdx = jIdx + i ;
				
				int n1 = random.nextInt(noiseRange) - noiseRangeBase ;
				int n2 = random.nextInt(noiseRange) - noiseRangeBase ;
				int n3 = random.nextInt(noiseRange) - noiseRangeBase ;
				
				int c1 = (pixelsC1A[iIdx] & 0xFF) + n1 ;
				int c2 = (pixelsC2A[iIdx] & 0xFF) + n2 ;
				int c3 = (pixelsC3A[iIdx] & 0xFF) + n3 ;
				
				mergeC1[iIdx] = (byte) clip(c1) ;
				mergeC2[iIdx] = (byte) clip(c2) ;
				mergeC3[iIdx] = (byte) clip(c3) ;
			}
		}
		
		return new ImagePixels(mergeC1, mergeC2, mergeC3 , imagePixels1.getWidth(), imagePixels1.getHeight() , true) ;
	}
	
	static public byte[] createSimilarPixels( ImagePixels imagePixels1 , ImagePixels imagePixels2, double tolerance) {

		imagePixels1.convertToYUV() ;
		imagePixels2.convertToYUV() ;
		
		byte[] pixelsC1A = imagePixels1.getPixelsC1() ;
		byte[] pixelsC2A = imagePixels1.getPixelsC2() ;
		byte[] pixelsC3A = imagePixels1.getPixelsC3() ;

		byte[] pixelsC1B = imagePixels2.getPixelsC1() ;
		byte[] pixelsC2B = imagePixels2.getPixelsC2() ;
		byte[] pixelsC3B = imagePixels2.getPixelsC3() ;
		
		byte[] simPixels = new byte[pixelsC1A.length] ;
		
		for (int i = 0; i < pixelsC1A.length; i++) {
			
			byte c1A = pixelsC1A[i] ;
			byte c2A = pixelsC2A[i] ;
			byte c3A = pixelsC3A[i] ;
			
			byte c1B = pixelsC1B[i] ;
			byte c2B = pixelsC2B[i] ;
			byte c3B = pixelsC3B[i] ;
			
			boolean sim = isSimilar_Precise(c1A,c2A,c3A , c1B,c2B,c3B, tolerance) ;
			
			simPixels[i] = sim ? (byte)255 : (byte)0 ;
		}
		
		return simPixels ;
	}
	
	static public byte[] createDistancePixels( ImagePixels imagePixels1 , ImagePixels imagePixels2, boolean inverse) {

		imagePixels1.convertToYUV() ;
		imagePixels2.convertToYUV() ;
		
		byte[] pixelsC1A = imagePixels1.getPixelsC1() ;
		byte[] pixelsC2A = imagePixels1.getPixelsC2() ;
		byte[] pixelsC3A = imagePixels1.getPixelsC3() ;

		byte[] pixelsC1B = imagePixels2.getPixelsC1() ;
		byte[] pixelsC2B = imagePixels2.getPixelsC2() ;
		byte[] pixelsC3B = imagePixels2.getPixelsC3() ;
		
		byte[] simPixels = new byte[pixelsC1A.length] ;
		
		for (int i = 0; i < pixelsC1A.length; i++) {
			
			byte c1A = pixelsC1A[i] ;
			byte c2A = pixelsC2A[i] ;
			byte c3A = pixelsC3A[i] ;
			
			byte c1B = pixelsC1B[i] ;
			byte c2B = pixelsC2B[i] ;
			byte c3B = pixelsC3B[i] ;
			
			double distance = getDistance_Precise(c1A,c2A,c3A , c1B,c2B,c3B) ;
			
			int pixel = (int) distance ;
			if (pixel < 0) pixel = 0 ;
			else if (pixel > 255) pixel = 255 ;
			
			if (inverse) pixel = 255 - pixel ;
			
			simPixels[i] = (byte) pixel ;
		}
		
		return simPixels ;
	}
	
	static public ImagePixels createSimilarImage( ImagePixels imagePixels1 , ImagePixels imagePixels2, double tolerance) {
		byte[] simPixels = createSimilarPixels(imagePixels1, imagePixels2, tolerance) ;
		
		return new ImagePixels( simPixels , imagePixels1.getWidth() , imagePixels1.getHeight() , true ) ;
	}
	
}
