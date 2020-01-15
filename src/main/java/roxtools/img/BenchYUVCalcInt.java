package roxtools.img;

import java.util.Random;

public class BenchYUVCalcInt {

	static int samplesSize = 10000 ;
	static int[] samplesY ;
	static int[] samplesU ;
	static int[] samplesV ;
	
	static private int maxDiff(byte[] bs1 , byte[] bs2) {
		
		int max = 0 ;
		
		for (int i = 0; i < bs1.length; i++) {
			int diff = (bs1[i] & 0xff) - (bs2[i] & 0xff) ;
			if (diff > max) max = diff ;
		}
		
		return max ;
	}
	
	public static void prepare() throws InterruptedException {
		
		samplesY = new int[samplesSize] ;
		samplesU = new int[samplesSize] ;
		samplesV = new int[samplesSize] ;
		
		Random rand = new Random(123) ;
		
		byte[] yuv1 = new byte[3] ;
		byte[] yuv2 = new byte[3] ;
		byte[] rgb1 = new byte[3] ;
		byte[] rgb2 = new byte[3] ;
		
		int maxALL = 0 ;
		int maxMean = 0 ;
		int maxMeanSz = 0 ;
		
		for (int i = 0; i < samplesSize; i++) {
			int y = rand.nextInt(256) ;
			int u = rand.nextInt(256) ;
			int v = rand.nextInt(256) ;
			
			samplesY[i] = y ;
			samplesU[i] = u ;
			samplesV[i] = v ;
			
			YUV.RGB_to_arrayYUV(y, u, v, yuv1) ;
			YUV.YUV_to_arrayRGB(y, u, v, rgb1) ;
			
			YUV.RGB_to_arrayYUV_fast(y, u, v, yuv2) ;
			YUV.YUV_to_arrayRGB_fast(y, u, v, rgb2) ;
			
			int yuvDiff = maxDiff(yuv1, yuv2) ;
			int rgbDiff = maxDiff(rgb1, rgb2) ;
			
			//System.out.println(yuvDiff +" > "+ rgbDiff);
			
			maxMean += yuvDiff ;
			maxMeanSz++ ;
			
			maxMean += rgbDiff ;
			maxMeanSz++ ;
			
			if (yuvDiff > maxALL) maxALL = yuvDiff ;
			if (rgbDiff > maxALL) maxALL = rgbDiff ;
		}
		
		maxMean /= maxMeanSz ;
		
		System.out.println("maxMean> "+ maxMean);
		System.out.println("maxALL> "+ maxALL);
		
		//Thread.sleep(10000) ;
		
		System.out.println("-------------------------------------------------");
		
		byte[] ret = new byte[3] ;
		
		System.out.println("preparing...");

		for (int loop = 0; loop < 2000; loop++) {
			for (int i = 0; i < samplesSize; i++) {
				int y = samplesY[i] ;
				int u = samplesU[i] ;
				int v = samplesV[i] ;
				
				try {

					YUV.RGB_to_arrayYUV(y, u, v, ret) ;
					YUV.YUV_to_arrayRGB(y, u, v, ret) ;
					
					YUV.RGB_to_arrayYUV_fast(y, u, v, ret) ;
					YUV.YUV_to_arrayRGB_fast(y, u, v, ret) ;
				} catch (Exception e) {
					throw new IllegalStateException(y+","+u+","+v , e) ;
				}
			}
			
		}
		
		for (int i = 0; i < 1; i++) {
			System.out.println("GC sleep... "+i);
			
			System.gc() ;
			Thread.sleep(1000) ;
		}
		
		Thread.sleep(1000) ;
		
		System.out.println("-------------------------------------------------");
		
	}
	
	public static void bench(int loops) {
		

		int samplesSize = BenchYUVCalcInt.samplesSize ;
		int[] samplesY = BenchYUVCalcInt.samplesY ;
		int[] samplesU = BenchYUVCalcInt.samplesU ;
		int[] samplesV = BenchYUVCalcInt.samplesV ;

		byte[] ret = new byte[3] ;
		
		int avoidJIT = 0 ;
		
		System.out.print("Bench encode normal... ");
		
		long timeEncNorm = System.currentTimeMillis() ;
		
		for (int loop = 0; loop < loops; loop++) {
			for (int i = 0; i < samplesSize; i++) {
				int y = samplesY[i] ;
				int u = samplesU[i] ;
				int v = samplesV[i] ;
				
				YUV.RGB_to_arrayYUV(y, u, v, ret) ;
				
				avoidJIT += ret[0] + ret[1] + ret[2] ;
			}
			
		}
		
		timeEncNorm = System.currentTimeMillis() - timeEncNorm ;
		
		System.out.println(timeEncNorm);
		System.out.print("Bench encode cached... ");
		
		long timeEncCahce = System.currentTimeMillis() ;
		
		for (int loop = 0; loop < loops; loop++) {
			for (int i = 0; i < samplesSize; i++) {
				int y = samplesY[i] ;
				int u = samplesU[i] ;
				int v = samplesV[i] ;
				
				YUV.RGB_to_arrayYUV_fast(y, u, v, ret) ;
				
				avoidJIT += ret[0] + ret[1] + ret[2] ;
			}
			
		}
		
		timeEncCahce = System.currentTimeMillis() - timeEncCahce ;
		
		System.out.println(timeEncCahce);
		
		System.out.println("time encode: "+ timeEncNorm +" -> "+ timeEncCahce);
		
		System.out.println("-------------------------------------------------");
		
		System.out.print("Bench decode normal... ");
		
		long timeDecNorm = System.currentTimeMillis() ;
		
		for (int loop = 0; loop < loops; loop++) {
			for (int i = 0; i < samplesSize; i++) {
				int y = samplesY[i] ;
				int u = samplesU[i] ;
				int v = samplesV[i] ;
				
				YUV.YUV_to_arrayRGB(y, u, v, ret) ;
				
				avoidJIT += ret[0] + ret[1] + ret[2] ;
			}
			
		}
		
		timeDecNorm = System.currentTimeMillis() - timeDecNorm ;
		
		System.out.println(timeDecNorm);
		
		System.out.print("Bench decode cached... ");
		
		long timeDecCahce = System.currentTimeMillis() ;
		
		for (int loop = 0; loop < loops; loop++) {
			for (int i = 0; i < samplesSize; i++) {
				int y = samplesY[i] ;
				int u = samplesU[i] ;
				int v = samplesV[i] ;
				
				YUV.YUV_to_arrayRGB_fast(y, u, v, ret) ;
				
				avoidJIT += ret[0] + ret[1] + ret[2] ;
			}
			
		}
		
		timeDecCahce = System.currentTimeMillis() - timeDecCahce ;
		
		System.out.println(timeDecCahce);
		
		System.out.println("time decode: "+ timeDecNorm +" -> "+ timeDecCahce);
		
		System.out.println("avoidJIT> "+ avoidJIT);
	}
	
}
