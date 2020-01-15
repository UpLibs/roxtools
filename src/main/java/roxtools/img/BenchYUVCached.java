package roxtools.img;

import java.util.Random;

public class BenchYUVCached {

	static YUVCached yuvCached = new YUVCached(2) ;
	//static YUVCached2 yuvCached = new YUVCached2(2) ;

	//static YUVCachedPrecision2 yuvCached = new YUVCachedPrecision2() ;
	
	static int samplesSize = 10000 ;
	static int[] samplesY ;
	static int[] samplesU ;
	static int[] samplesV ;
	
	public static void prepare() throws InterruptedException {

		System.out.println("YUVCached: "+ yuvCached);
		
		samplesY = new int[samplesSize] ;
		samplesU = new int[samplesSize] ;
		samplesV = new int[samplesSize] ;
		
		Random rand = new Random(123) ;
		
		for (int i = 0; i < samplesSize; i++) {
			samplesY[i] = rand.nextInt(256) ;
			samplesU[i] = rand.nextInt(256) ;
			samplesV[i] = rand.nextInt(256) ;
		}
		
		System.out.println("-------------------------------------------------");
		
		System.out.println("preparing...");
		

		byte[] ret = new byte[3] ;
		
		for (int loop = 0; loop < 2000; loop++) {
			for (int i = 0; i < samplesSize; i++) {
				int y = samplesY[i] ;
				int u = samplesU[i] ;
				int v = samplesV[i] ;
				
				try {

					YUV.RGB_to_arrayYUV(y, u, v, ret) ;
					YUV.YUV_to_arrayRGB(y, u, v, ret) ;
					
					yuvCached.encode(y, u, v, ret) ;
					yuvCached.decode(y, u, v, ret) ;
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
		

		int samplesSize = BenchYUVCached.samplesSize ;
		int[] samplesY = BenchYUVCached.samplesY ;
		int[] samplesU = BenchYUVCached.samplesU ;
		int[] samplesV = BenchYUVCached.samplesV ;

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
				
				yuvCached.encode(y, u, v, ret) ;
				
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
				
				yuvCached.decode(y, u, v, ret) ;
				
				avoidJIT += ret[0] + ret[1] + ret[2] ;
			}
			
		}
		
		timeDecCahce = System.currentTimeMillis() - timeDecCahce ;
		
		System.out.println(timeDecCahce);
		
		System.out.println("time decode: "+ timeDecNorm +" -> "+ timeDecCahce);
		
		System.out.println("avoidJIT> "+ avoidJIT);
	}
	
}
