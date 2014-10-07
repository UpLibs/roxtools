package roxtools.img;

import java.awt.image.BufferedImage;

import com.jhlabs.image.PerspectiveFilter;

public class ImagePerspectiveFilter {

	static public BufferedImage applyPerspectiveByCornerRatio( BufferedImage img , 

			double x1Ratio ,
			double y1Ratio ,
			
			double x2Ratio ,
			double y2Ratio ,
			
			double x3Ratio ,
			double y3Ratio ,
			
			double x4Ratio ,
			double y4Ratio ,
			
			boolean crop
			)
	{
		int w = img.getWidth() ;
		int h = img.getHeight() ;
		
		float x1 = (float) (x1Ratio*w) ;
		float y1 = (float) (y1Ratio*h) ;
		
		float x2 = (float) (w+(x2Ratio*w)) ;
		float y2 = (float) (y2Ratio*h) ;
		
		float x3 = (float) (w+(x3Ratio*w)) ;
		float y3 = (float) (h+(y3Ratio*h)) ;
		
		float x4 = (float) (x4Ratio*w) ;
		float y4 = (float) (h+(y4Ratio*h)) ;
		
		return applyPerspectiveImplem(img, x1, y1, x2, y2, x3, y3, x4, y4, crop) ;
	}
	
	
	static public BufferedImage applyPerspectiveHorizontal( BufferedImage img , double topRatio , double bottomRatio , boolean crop) {
		int w = img.getWidth() ;
		int h = img.getHeight() ;
		
		float wRatioTop = (float) (w*topRatio) ;
		float wRatioBottom = (float) (w*bottomRatio) ;
		
		float topCut = w-wRatioTop ;
		float bottomCut = w-wRatioBottom ;
		
		float x1 = topCut ;
		float y1 = 0 ;
		
		float x2 = w-topCut ;
		float y2 = 0 ;
		
		float x3 = w-bottomCut ;
		float y3 = h ;
		
		float x4 = bottomCut ;
		float y4 = h ;
		
		return applyPerspectiveImplem(img, x1, y1, x2, y2, x3, y3, x4, y4, crop) ;
	}
	
	static public BufferedImage applyPerspective( BufferedImage img , float x1 , float y1 , float x2 , float y2 , float x3 , float y3 , float x4 , float y4 , boolean crop) {
		return applyPerspectiveImplem(img, x1, y1, x2, y2, x3, y3, x4, y4, crop) ;
	}
	
	static private BufferedImage applyPerspectiveImplem( BufferedImage img , float x1 , float y1 , float x2 , float y2 , float x3 , float y3 , float x4 , float y4 , boolean crop) {
		PerspectiveFilter perspectiveFilter = new PerspectiveFilter();
		
		if (!crop) {
			perspectiveFilter.setEdgeAction(1) ;
		}
	    
	    perspectiveFilter.setCorners(
	    		x1 , y1 ,
	    		x2 , y2 ,
	    		x3 , y3 ,
	    		x4 , y4 
	    ) ;
	    
	    
	    BufferedImage destImage = perspectiveFilter.filter(img, null);
	    
	    if (crop) {
	    	
	    	int x0 = (int) Math.min(x1, x4) ;
		    int y0 = (int) Math.min(y1, y2) ;
		    
		    int xA = (int) Math.max(x1, x4) ;
		    int yA = (int) Math.max(y1, y2) ;
		    
		    int xB = (int) Math.min(x2, x3) ;
		    int yB = (int) Math.min(y3, y4) ;
		    
		    int x = xA-x0 ;
		    int y = yA-y0 ;
		    
		    int w = xB-xA ;
		    int h = yB-yA ; 
		    
		    destImage = destImage.getSubimage(x,y,w,h) ;
	    }
	    
		return destImage ;
	}
	
}
