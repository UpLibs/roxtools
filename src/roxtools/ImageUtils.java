package roxtools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;

import roxtools.img.YUV;

final public class ImageUtils {
	
	static private JPanel DUMMY_JPANEL ;
	
	static public void ensureLoaded(Image image) {
		if (DUMMY_JPANEL == null) {
			synchronized (ImageUtils.class) {
				if (DUMMY_JPANEL == null) {
					DUMMY_JPANEL = new JPanel() ;
				}
			}
		}
		
		ensureLoaded(DUMMY_JPANEL, image) ;
	}
	
	static public void ensureLoaded(Component component, Image image) {
		
		MediaTracker mediaTracker = new MediaTracker(component) ;
		mediaTracker.addImage(image, 1) ;
		try {
			mediaTracker.waitForAll() ;
		}
		catch (InterruptedException e) {
			throw new IllegalStateException(e) ;
		}
		
	}
	
	static public BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) return (BufferedImage) img ;
		
		BufferedImage buffImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB) ;
		
		Graphics g = buffImg.createGraphics() ;
		
		g.drawImage(img, 0,0, null) ;
		
		g.dispose() ;
		
		return buffImg ;
	}
	
	static public BufferedImage copyImage(Image img) {
		BufferedImage buffImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB) ;
		
		Graphics g = buffImg.createGraphics() ;
		
		g.drawImage(img, 0,0, null) ;
		
		g.dispose() ;
		
		return buffImg ;
	}
	
	
	static public BufferedImage readFile(String filePath) {
		return readFile(new File(filePath)) ;
	}
	
	static public BufferedImage readFile(File file) {
		try {
			return ImageIO.read(file) ;
		} catch (IOException e) {
			e.printStackTrace();
			return null ;
		}
	}
	
	static public void savePNG(int[] pixels, int w, int h, String filePath) {
		savePNG(pixels, w,h, new File(filePath)) ;
	}
	
	static public void savePNG(int[] pixels, int w, int h, File file) {
		savePNG( createImage(pixels, w, h), file) ;
	}
	
	static public void savePNG(BufferedImage img, String filePath) {
		savePNG(img, new File(filePath)) ;
	}
	
	static public void savePNG(BufferedImage img, File file) {
		try {
			ImageIO.write(img, "PNG", file) ;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static public BufferedImage createImage(int[][] pixels) {
		int w = pixels.length ;
		int h = pixels[0].length ;
		
		BufferedImage buffImg = new BufferedImage(w , h, BufferedImage.TYPE_INT_RGB) ;
		
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				int rgb = pixels[i][j] ;
				buffImg.setRGB(i, j, rgb) ;
			}
		}
		
		return buffImg ;
	}
	
	static public BufferedImage createImage(int[] pixels, int w, int h) {
		BufferedImage buffImg = new BufferedImage(w , h, BufferedImage.TYPE_INT_RGB) ;
		
		buffImg.setRGB(0, 0, w, h, pixels, 0, w) ;
		
		return buffImg ;
	}
	
	static public BufferedImage createImage(Color bgColor, int w, int h) {
		BufferedImage buffImg = new BufferedImage(w , h, BufferedImage.TYPE_INT_RGB) ;
		
		Graphics2D g = buffImg.createGraphics() ;
		
		g.setColor(bgColor) ;
		
		g.fillRect(0, 0, w,h) ;
		
		g.dispose() ;
		
		return buffImg ;
	}
	
	static public BufferedImage createAlphaImage(int w, int h) {
		BufferedImage buffImg = new BufferedImage(w , h, BufferedImage.TYPE_INT_ARGB) ;
		
		return buffImg ;
	}
	
	static public BufferedImage mergeImages(Image img1, Rectangle img1Rect, Image img2, Rectangle img2Rect, int w, int h) {
		BufferedImage buffImg = new BufferedImage(w , h, BufferedImage.TYPE_INT_RGB) ;
		
		Graphics2D g = buffImg.createGraphics() ;
		
		g.setColor(Color.BLACK) ;
		g.fillRect(0, 0, w,h) ;
		
		g.drawImage(img1, img1Rect.x, img1Rect.y, img1Rect.width, img1Rect.height, null) ;
		g.drawImage(img2, img2Rect.x, img2Rect.y, img2Rect.width, img2Rect.height, null) ;
		
		g.dispose() ;
		
		return buffImg ;
	}
	
	
	static public int[] mergePixels(byte[] pixelsR, byte[] pixelsG, byte[] pixelsB, int w, int h) {
		int[] pixels = new int[ w*h ];
		
		for (int i = pixels.length-1 ; i >= 0 ; i--) {
			pixels[i] = (pixelsR[i] & 0xff) << 16 | (pixelsG[i] & 0xff) << 8 | (pixelsB[i] & 0xff) ; 
		}
		
		return pixels ;
	}
	
	static public int[] mergePixelsYUV(byte[] pixelsY, byte[] pixelsU, byte[] pixelsV, int w, int h) {
		int[] pixels = new int[ w*h ];
		
		YUV.YUVarrays_to_RGBarray_fast(pixelsY, pixelsU, pixelsV, pixels) ;
		
		return pixels ;
	}
	
	static public byte[] getPixelsPart(int[] pixels, int shift) {
		byte[] part = new byte[pixels.length] ;
		
		for (int i = 0; i < part.length; i++) {
			part[i] = (byte) ((pixels[i] >> shift) & 0xff) ;
		}
		
		return part ;
	}
	
	static public BufferedImage createScaledImage(Image img, int w, int h) {
		BufferedImage buffImg = new BufferedImage(w , h, BufferedImage.TYPE_INT_RGB) ;
		
		Graphics2D g = buffImg.createGraphics() ;
		
		//g.setRenderingHint(RenderingHints.KEY_RENDERING , RenderingHints.VALUE_RENDER_QUALITY) ;
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR) ;
		
		g.drawImage(img, 0,0 , w,h , null) ;
		
		g.dispose() ;
		
		return buffImg ;
	}
	
	
	static public int[] pixelMatrix2PixelList(int[][] pixels) {
		int w = pixels.length ;
		int h = pixels[0].length ;
		
		int[] pixelsList = new int[w*h] ;
		int pixelsListSz = 0 ;

		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				pixelsList[ pixelsListSz++ ] = pixels[i][j] ;
			}
		}
		
		return pixelsList ;
	}
	
	static public int[][] pixelList2PixelMatrix(int[] pixels, int w , int h) {
		int[][] matrix = new int[w][h] ;
		
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				int idx = ( j*w ) + i ;
				matrix[i][j] = pixels[idx] ;
			}
		}
		
		return matrix ;
	}
	
	static public int[] grabPixels(BufferedImage image) {
		BufferedImage bufImg = (BufferedImage) image ;
		int w = bufImg.getWidth() ;
		int h = bufImg.getHeight() ;
		
		int[] rgbArray = new int[w*h] ;
		bufImg.getRGB(0, 0, w, h, rgbArray, 0, w) ;
		
		return rgbArray ;
	}
	
	static public int[] grabPixels(Image image) {
		if (image instanceof BufferedImage) {
			BufferedImage bufImg = (BufferedImage) image ;
			int w = bufImg.getWidth() ;
			int h = bufImg.getHeight() ;
			
			int[] rgbArray = new int[w*h] ;
			bufImg.getRGB(0, 0, w, h, rgbArray, 0, w) ;
			
			return rgbArray ;
		}
		
		int w = image.getWidth(null) ;
		int h = image.getHeight(null) ;
		
		PixelGrabber grabber = new PixelGrabber(image, 0, 0, w,h , true);

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
	
	static public int[] grabPixels(BufferedImage bufImage, int imgWidth, int imgHeight) {
		int[] rgbArray = new int[imgWidth*imgHeight] ;
		bufImage.getRGB(0, 0, imgWidth, imgHeight, rgbArray, 0, imgWidth) ;
		return rgbArray ;
	}
	
	static public int[] grabPixels(Image image, int x, int y, int width, int height) {
		if (x == 0 && y == 0 && image instanceof BufferedImage) {
			BufferedImage bufImg = (BufferedImage) image ;
			int w = bufImg.getWidth() ;
			int h = bufImg.getHeight() ;
			
			if (w == width && h == height) {
				int[] rgbArray = new int[w*h] ;
				bufImg.getRGB(0, 0, w, h, rgbArray, 0, w) ;
				
				return rgbArray ;	
			}
		}
		
		PixelGrabber grabber = new PixelGrabber(image, x,y , width, height, true);

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

	static public float calculateColorBrightness(int r, int g, int b) {
		int cmax = (r > g) ? r : g;
		if (b > cmax) cmax = b;

		float brightness = ((float) cmax) / 255.0f;
		return brightness;
	}


	static public float calculateAmbientLight(int[][] pixels, int x , int y, int range) {
		
		int initX = x-range ;
		if (initX < 0) initX = 0 ;
		
		int endX = x+range ;
		if (endX >= pixels.length) endX = pixels.length-1 ;
		
		int initY = y-range ;
		if (initY < 0) initY = 0 ;
		
		int endY = y+range ;
		if (endY >= pixels[0].length) endY = pixels[0].length-1 ;
		
		int allR = 0 ;
		int allG = 0 ;
		int allB = 0 ;
		
		int allCount = 0 ;
		
		for (int j = initY; j <= endY; j++) {
			for (int i = initX; i <= endX; i++) {
				int rgb = pixels[i][j] ;
				
				int r = (rgb >> 16) & 0xFF ;
				int g = (rgb >> 8) & 0xFF ;
				int b = rgb & 0xFF ;

				allR += r ;
				allG += g ;
				allB += b ;
				
				allCount++ ;
			}
		}
		
		allR /= allCount ;
		allG /= allCount ;
		allB /= allCount ;
		
		float brightness = calculateColorBrightness(allR, allG, allB) ;
		
		return brightness ;
	}
	
	static public float calculateAmbientLight(int[][] pixels) {
		int width = pixels.length ;
		int height = pixels[0].length ;
		
		int allR = 0 ;
		int allG = 0 ;
		int allB = 0 ;
		
		int allCount = 0 ;
		
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				int rgb = pixels[i][j] ;
				
				int r = (rgb >> 16) & 0xFF ;
				int g = (rgb >> 8) & 0xFF ;
				int b = rgb & 0xFF ;

				allR += r ;
				allG += g ;
				allB += b ;
				
				allCount++ ;
			}
		}
		
		allR /= allCount ;
		allG /= allCount ;
		allB /= allCount ;
		
		float brightness = calculateColorBrightness(allR, allG, allB) ;
		
		return brightness ;
	}
	
	static public boolean[][] calcDifferences( BufferedImage img1, BufferedImage img2 , double tolerance ) {
		
		int w = img1.getWidth() ;
		int h = img1.getHeight() ;
		
		int[][] px1 = pixelList2PixelMatrix( grabPixels(img1) , w,h );
		int[][] px2 = pixelList2PixelMatrix( grabPixels(img2) , w,h );
		
		boolean[][] diff = new boolean[w][h];
		
		int countDiff = 0 ;
		
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				
				if ( !isColorSimilar(px1[i][j] , px2[i][j], tolerance) ) {
					diff[i][j] = true ;
					countDiff++ ;
				}
			}
		}
		
		double diffRatio = countDiff / ( w*h*1d ) ;
		
		System.out.println("diffRatio> "+ diffRatio);
		
		return diff ;
	}
	
	//////////////////////////////////////////////////////////


	static public void toHSV(int color1, float[] hsv) {
		Color.RGBtoHSB(
				(color1 >> 16) & 0xFF ,
				(color1 >> 8) & 0xFF ,
				color1 & 0xFF
				, hsv) ;
	}
	
	static public boolean isColorSimilar(Color color1, Color color2, double tolerance) {
		return isColorSimilar(color1.getRGB(), color2.getRGB(), tolerance) ;
	}
	
	static public boolean isColorSimilar(int color1, int color2, double tolerance) {
		if (color1 == color2) return true ;
		
		float[] hsv = new float[3] ;
		
		toHSV(color1, hsv) ;
		
		float h1 = hsv[0] ;
		float s1 = hsv[1] ;
		float v1 = hsv[2] ;
		
		toHSV(color2, hsv) ;
		
		float h2 = hsv[0] ;
		float s2 = hsv[1] ;
		float v2 = hsv[2] ;
		
		return isColorSimilar(h1, s1, v1, h2, s2, v2, tolerance) ;
	}
	
	static public boolean isColorSimilar(float h1, float s1, float v1, float h2, float s2, float v2, double tolerance) {
		float cV = v2 - v1 ;
		
		float minV = v1 < v2 ? v1 : v2 ;
		
		float cS = (s2 * minV) - (s1 * minV) ;
		
		double hip1 = Math.sqrt( cV*cV + cS*cS ) ;
		if (hip1 > tolerance) return false ;
		
		float minS = s1 < s2 ? s1 : s2 ;
		float cHScale = minV*minS ;
		
		float cH = h2 - h1 ;
		if (cH > 0.5f) cH = 1f - cH ;
		else if (cH < -0.5f) cH = 1f + cH ;
		
		cH *= cHScale ;
		
		double hip2 = Math.sqrt( hip1*hip1 + cH*cH ) ;
		if (hip2 > tolerance) return false ;
		
		return true ;
	}
	
	static public File[] listImageFiles(File dir, final boolean onlyNumerated) {
		File[] listFiles = dir.listFiles( new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String fileName = pathname.getName().toLowerCase() ;
				
				if (fileName.startsWith(".")) return false ;
				
				if ( fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") ) {
					return !onlyNumerated || getImageFileNameIndex(fileName) >= 0 ; 
				}

				return false ;
			}
		}) ;
		
		Arrays.sort(listFiles , new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				long thisVal = getImageFileNameIndex( o1.getName() ) ;
				long anotherVal = getImageFileNameIndex( o2.getName() ) ;
				return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
			}
		}) ;
		
		return listFiles ;
	}
	
	static private Pattern PATTERN_IMG_FILE_NAME_IDX = Pattern.compile("(\\d+)\\.") ;
	
	static private long getImageFileNameIndex(String fileName) {
		
		Matcher matcher = PATTERN_IMG_FILE_NAME_IDX.matcher(fileName) ;
		
		if ( matcher.find() ) {
			return Long.valueOf( matcher.group(1) ) ;
		}
		
		return -1 ;
	}
	
	static public BufferedImage groupImages(int maxWidth, File... imageFiles) throws IOException {
		Image[] images  = new Image[imageFiles.length] ;
		
		for (int i = 0; i < images.length; i++) {
			images[i] = ImageIO.read(imageFiles[i]) ;
		}
		
		return groupImages(maxWidth, images) ;
	}
	
	static public BufferedImage groupImages(int maxWidth, Image... images) {
		
		Dimension[] dimensions = new Dimension[images.length] ;
		
		int lineWidth = 0 ;
		int lineHeight = 0 ;
		
		int maxLineWidth = 0 ;
		int totalHeight = 0 ;
		
		for (int i = 0; i < dimensions.length; i++) {
			Image img = images[i] ;
			int w = img.getWidth(null) ;
			int h = img.getHeight(null) ; 
			
			dimensions[i] = new Dimension(w,h) ;
			
			if ( lineWidth > 0 && lineWidth+w > maxWidth ) {
				if (lineWidth > maxLineWidth) maxLineWidth = lineWidth ;
				totalHeight += lineHeight ;
				
				lineWidth = 0 ;
				lineHeight = 0 ;
			}
			
			lineWidth += w ;
			if (h > lineHeight) lineHeight = h ;
		}
		
		if (lineWidth > maxLineWidth) maxLineWidth = lineWidth ;
		totalHeight += lineHeight ;
		
		BufferedImage groupImg = new BufferedImage(maxLineWidth, totalHeight, BufferedImage.TYPE_INT_RGB) ;
		
		Graphics g = groupImg.createGraphics() ;

		lineWidth = 0 ;
		lineHeight = 0 ;
		
		totalHeight = 0 ;
		
		for (int i = 0; i < dimensions.length; i++) {
			Image img = images[i] ;
			Dimension dim = dimensions[i] ;
			int w = dim.width ;
			int h = dim.height ;
			
			if ( lineWidth > 0 && lineWidth+w > maxWidth ) {
				totalHeight += lineHeight ;
				
				lineWidth = 0 ;
				lineHeight = 0 ;
			}
			
			g.drawImage(img, lineWidth,totalHeight, null) ;	
			
			lineWidth += w ;
			if (h > lineHeight) lineHeight = h ;
		}
		
		g.dispose() ;		
		
		return groupImg ;
	}
	
	static public String imageToPNGBase64(BufferedImage image) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
		ImageIO.write(image, "PNG", bout) ;
		return Base64Utils.encode(bout.toByteArray()) ;
	}
	
	static public BufferedImage pngBase64ToImage(String pngBase64) throws IOException {
		byte[] data = Base64Utils.decode(pngBase64) ;
		ByteArrayInputStream bin = new ByteArrayInputStream(data) ;
		return ImageIO.read(bin) ;
	}
	
}
