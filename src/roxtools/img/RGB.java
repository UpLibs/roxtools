package roxtools.img;

final public class RGB {
	
	static public void pixelRGB_to_ArrayRGB(int pixelRGB, int[] rgb) {
		rgb[0] = (pixelRGB >> 16) & 0xff ;
		rgb[1] = (pixelRGB >> 8) & 0xff ;
		rgb[2] = pixelRGB & 0xff ;
		
	}
	
	static public int arrayRGB_to_PixelRGB(int[] arrayRGB) {
		return arrayRGB[0] << 16 | arrayRGB[1] << 8 | arrayRGB[2] ;
	}
	
	static public int arrayRGB_to_PixelRGB(byte[] arrayRGB) {
		return (arrayRGB[0] & 0xFF) << 16 | (arrayRGB[1] & 0xFF) << 8 | (arrayRGB[2] & 0xFF) ;
	}
	
	static public int toPixelRGB(int r, int g, int b) {
		return r << 16 | g << 8 | b ;
	}

}
