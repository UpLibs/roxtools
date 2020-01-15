package roxtools.img;

import java.util.Arrays;
import java.util.Random;

final public class YUVCachedPrecision2 {
	
	static final private int precision = 2 ;
	
	final private int range ;
	final private int range2 ;
	
	public YUVCachedPrecision2() {
		this.range = 256 / precision ;
		this.range2 = range * range ;
		
		compute() ;
	}
	
	private byte[] encoded ;
	private byte[] decoded ;
	
	private void compute() {
		
		System.out.println("** Computing encoding with precision: "+ precision +"... ");
		
		byte[] encoded = new byte[ range * range * range * 3 ] ;
		int encodedSz = 0 ; 
		
		byte[] yuv = new byte[3] ;
		
		for (int r = 0; r < 256; r+= precision) {
			for (int g = 0; g < 256; g+= precision) {
				for (int b = 0; b < 256; b+= precision) {
					int pixelRGB = RGB.toPixelRGB(r, g, b) ;
					YUV.pixelRGB_to_arrayYUV(pixelRGB, yuv) ;
					System.arraycopy(yuv, 0, encoded, encodedSz, 3) ;
					encodedSz += 3 ;
				}
			}
		}
		
		System.out.println("** Computed encoding: "+ (encoded.length/3) +" > size: "+ (encoded.length / 1024f) +"K" );
		
		this.encoded = encoded ;
		
		System.out.println("** Computing decoding with precision: "+ precision +"... ");
		
		int[] rgb = new int[3] ;
		
		byte[] decoded = new byte[ range * range * range * 3 ] ;
		int decodedSz = 0 ; 
		
		for (int y = 0; y < 256; y+= precision) {
			for (int u = 0; u < 256; u+= precision) {
				for (int v = 0; v < 256; v+= precision) {
					YUV.YUV_to_arrayRGB(y, u, v, rgb) ;
					
					decoded[decodedSz++] = (byte) rgb[0] ;
					decoded[decodedSz++] = (byte) rgb[1] ;
					decoded[decodedSz++] = (byte) rgb[2] ;
				}
			}
		}
		
		System.out.println("** Computed decodign: "+ (decoded.length/3) +" > size: "+ (decoded.length / 1024f) +"K" );
		
		this.decoded = decoded ;
		
	}
	
	public void encode(int r, int g, int b, byte[] yuv) {
		int idx = (((r/precision)*range2 + (g/precision)*range + b/precision) * 3) ;
		
		yuv[0] = encoded[idx] ;
		yuv[1] = encoded[idx+1] ;
		yuv[2] = encoded[idx+2] ;
	}
	
	public void decode(int y, int u, int v, byte[] yuv) {
		int idx = (((y/precision)*range2 + (u/precision)*range + v/precision) * 3) ;

		yuv[0] = decoded[idx] ;
		yuv[1] = decoded[idx+1] ;
		yuv[2] = decoded[idx+2] ;
	}
	
}
