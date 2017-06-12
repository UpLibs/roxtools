package roxtools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ImageViewer extends JFrame {
	private static final long serialVersionUID = -1269725251699879830L;

	private JScrollPane scrollpane ;
	private MyPanel panel ;
	private double[] ratios ;
	
	public ImageViewer() {
		this(1) ;
	}
	
	public ImageViewer(double... ratios) {
		super( ImageViewer.class.getName() ) ;
	
		this.ratios = ratios ;
		
		Container contentPane = getContentPane() ;
		contentPane.setLayout( new BorderLayout() ) ;
		
		this.panel = new MyPanel() ;
		
		scrollpane = new JScrollPane(this.panel);
		scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS) ;
		
		contentPane.add(scrollpane, BorderLayout.CENTER) ;
		
		setSize(1000, 800);
		setLocation(-1, -1) ;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) ;
		
		setVisible(true) ;
	}
	
	private Image[] images ;
	
	public void setImageFiles(List<File> imageFiles) throws IOException {
		setImageFiles( imageFiles.toArray( new File[ imageFiles.size() ] ) ) ;
	}
	
	public void setImageFiles(File... imageFiles) throws IOException {
		Image[] images = new Image[ imageFiles.length ] ;
		
		for (int i = 0; i < images.length; i++) {
			images[i] = ImageIO.read( imageFiles[i] ) ;
		}
		
		setImages( images ) ;
	}
	
	public void setImages(List<Image> images) {
		setImages( images.toArray(new Image[images.size()]) ) ;
	}
	
	private int stepImageCount = 0 ;
	
	public void setImages(Image... images) {
		this.images = images ;
		this.stepImageCount++ ;
		forceRepaint();
	}

	private void forceRepaint() {
		this.panel.repaint() ;
	}
	
	public Image[] getImages() {
		return images;
	}
	
	public void stepImages() {
		this.stepImageCount++ ;
		forceRepaint();
	}
	
	private boolean[][][] masks ;
	
	public int getMasksLayers() {
		return masks == null ? 0 : masks.length ;
	}
	
	private void ensureMaskLayersCapacity(int layer) {
		if (masks == null) {
			this.masks = new boolean[layer+1][][] ;
		}
		else if ( layer >= this.masks.length ) {
			boolean[][][] masks2 = new boolean[layer+1][][] ;
			System.arraycopy(this.masks, 0, masks2, 0, this.masks.length);
			this.masks = masks2 ;
		}
	}
	
	public void clearMasks() {
		masks = null ;
	}
	
	public void setMasks(int layer, List<boolean[]> masks) {
		setMasks( layer , masks.toArray( new boolean[ masks.size() ][] ) ) ;
	}

	private int maskMaxHistory = 10 ;
	
	public void setMaskMaxHistory(int maskMaxHistory) {
		this.maskMaxHistory = maskMaxHistory;
	}
	
	public int getMaskMaxHistory() {
		return maskMaxHistory;
	}
	
	private HashMap<Integer, Boolean> masksHistoryEnabled = new HashMap<>() ;
	private HashMap<Integer, RoxDeque<boolean[][]>> masksHistory = new HashMap<>() ; 
	
	public void setMaskLayerHistoryEnabled(int layer, boolean enabled) {
		masksHistoryEnabled.put(layer, enabled) ;
	}
	
	public boolean getMaskLayerHistoryEnabled(int layer) {
		Boolean b = masksHistoryEnabled.get(layer) ;
		return b != null ? b : false ;
	}
	
	public void setMasks(int layer, boolean[]... masks) {
		ensureMaskLayersCapacity(layer);
		
		masks = deepClone(masks) ;
		
		boolean[][] prevMasks = this.masks[layer] ;
		
		this.masks[layer] = masks;
		
		if (prevMasks != null && getMaskLayerHistoryEnabled(layer)) {
			RoxDeque<boolean[][]> layerHistory = masksHistory.get(layer) ;
			
			if (layerHistory == null) {
				masksHistory.put(layer, layerHistory = new RoxDeque<>()) ;
			}
			
			layerHistory.addFirst(prevMasks) ;
			
			while (layerHistory.size() > maskMaxHistory) {
				layerHistory.removeLast() ;	
			}
		}
	}
	
	private boolean[][] deepClone(boolean[][] masks) {
		if (masks == null) return null ;
		
		boolean[][] clone = new boolean[ masks.length ][] ;
		
		for (int i = 0; i < clone.length; i++) {
			boolean[] m = masks[i] ;
			clone[i] = m != null ? m.clone() : null ;
		}
		
		return clone ;
	}

	public boolean[][] getMasks(int layer) {
		if (masks == null || layer >= masks.length) return null ;
		return masks[layer] ;
	}
	
	private Color[] masksLayersColors ;
	
	public void setMasksLayersColors(List<Color> layersColors) {
		setMasksLayersColors( layersColors.toArray( new Color[layersColors.size()] ) );
	}
	
	public void setMasksLayersColors(Color... layersColors) {
		this.masksLayersColors = layersColors;
	}
	
	public Color[] getMasksLayersColors() {
		return masksLayersColors;
	}
	
	public void setImagesAndMasks(List<Image> images, List<boolean[]> masks) {
		setImagesAndMasks( images.toArray(new Image[images.size()])  , masks.toArray( new boolean[ masks.size() ][] ) ) ;
	}
	
	public void setImagesAndMasks(Image[] images, boolean[][] masks) {
		this.images = images ;
		this.stepImageCount++ ;
		clearMasks();
		setMasks(0, masks);
		forceRepaint();
	}
	
	private Color defaultMaskColor = new Color(0,255,0,50) ;
	
	public Color getDefaultMaskColor() {
		return defaultMaskColor;
	}
	
	public void setDefaultMaskColor(Color defaultMaskColor) {
		this.defaultMaskColor = defaultMaskColor;
	}
	
	private Hashtable<String, Object> properties = new Hashtable<>() ;
	
	public void clearProperties() {
		properties.clear();
	}
	
	public Hashtable<String, Object> getProperties() {
		return properties;
	}
	
	public void setProperty(String key, Object val) {
		properties.put(key, val) ;
	}
	
	@SuppressWarnings("unchecked")
	public <V> V getProperty(String key) {
		return (V) properties.get(key) ;
	}
	
	private Rectangle panelDimension ;
	synchronized private void updatePanelDimension( Rectangle dim ) {
		if (panelDimension == null || !panelDimension.equals(dim)) {
			panelDimension = dim ;
			
			this.panel.setPreferredSize( new Dimension( dim.width, dim.height) ) ;
			this.scrollpane.revalidate() ;
		}
	}
	
	private class MyPanel extends JPanel {
		private static final long serialVersionUID = -8120268602701359110L;

		@Override
		public void paint(Graphics g) {
			super.paint(g) ;
			
			Image[] images = ImageViewer.this.images ;
			boolean[][][] masks = ImageViewer.this.masks ;
			
			if (images == null || images.length == 0) return ;
			
			Graphics2D g2 = (Graphics2D)g ;
			
			int x = 0 ;
			int y = 20 ;
			
			String infoLine = "Step: "+stepImageCount ;
			
			if (!properties.isEmpty()) {
				infoLine += " "+properties.toString() ;
			}
			
			g2.drawString(infoLine , x+1, y-4) ;
			
			int maxH = 0 ;
			
			Area drawArea = new Area() ;
			
			int panelWidth = scrollpane.getWidth() ;
			
			for (int imgI = 0; imgI < images.length; imgI++) {
				Image img = images[imgI];
				
				if (img == null) continue ;
				
				double ratio = ratios != null && ratios.length > 1 ? ratios[imgI] : 1 ;
				
				int w = img.getWidth(this) ;
				int h = img.getHeight(this) ;
				
				int w2 = (int) (w * ratio) ;
				int h2 = (int) (h * ratio) ;
				
				if (x+w2 > panelWidth) {
					x = 0 ;
					y += maxH ;
					maxH = 0 ;
				}
				
				g2.drawImage(img, x,y, w2,h2 , this) ;

				if (masks != null) {
					for (int layer = 0; layer < masks.length; layer++) {
						boolean[][] layerMasks = masks[layer];
				
						boolean[] mask = layerMasks != null && layerMasks.length > imgI ? layerMasks[imgI] : null ;
						boolean[][] maskHistory = getMaskLayerHistory(layer, imgI);
						
						if (mask != null || maskHistory != null) {
							Color maskColor = masksLayersColors != null && masksLayersColors.length > layer ? masksLayersColors[layer] : null ;
							if (maskColor == null) maskColor = defaultMaskColor ;
							
							paintMask(g2, layer, mask, maskHistory, maskColor, x, y, w, h, ratio) ;
						}
					}
				}
				
				
				drawArea.add( new Area( new Rectangle(x,y, w2,h2) ) )  ;
				
				x += w2+2 ;
				maxH = Math.max(maxH, h2) ;
			}
			
			Rectangle bounds = drawArea.getBounds() ;
			
			updatePanelDimension(bounds) ;
		}
	}
	
	private boolean[][] getMaskLayerHistory(int layer, int imgIdx) {
		if ( !getMaskLayerHistoryEnabled(layer) ) return null ;
		
		RoxDeque<boolean[][]> layerHistory = this.masksHistory.get(layer) ;
		if (layerHistory == null || layerHistory.isEmpty()) return null ;
		
		ArrayList<boolean[]> imgMasks = new ArrayList<>() ;
		
		for (boolean[][] masks : layerHistory) {
			boolean[] mask = masks[imgIdx] ;
			imgMasks.add(mask) ;
		}
		
		return imgMasks.toArray(new boolean[ imgMasks.size() ][]) ;
	}
	
	private void paintMask(Graphics2D g2, int maskLayer, boolean[] mask, boolean[][] maskHistory, Color maskColor, int x, int y, int w, int h, double ratio) {
		
		boolean maskLayerHistoryEnabled = getMaskLayerHistoryEnabled(maskLayer) ;
		
		Color prevColor = g2.getColor() ;
		
		int maskI = 0 ;
		
		int mSize = (int) ratio ;
		if (mSize < 1) mSize = 1 ;
		
		while (ratio > mSize) mSize++ ;
		
		final float maxHistory = maskMaxHistory ;
		
		int colorR = maskColor.getRed() ;
		int colorG = maskColor.getGreen() ;
		int colorB = maskColor.getBlue() ;
		int colorAlpha = maskColor.getAlpha() ;
		
		g2.setColor(maskColor) ;
		
		int prevAlpha = colorAlpha ;
		
		for (int j = 0; j < h; j++) {
			int mY = (int) (y + ( j * ratio )) ;
			
			for (int i = 0; i < w; i++) {
				int mX = (int) (x + ( i * ratio )) ;
				
				boolean v = mask != null ? mask[maskI] : false ;
				
				int mAlpha ;
				if (v) {
					mAlpha = colorAlpha ;
					
					if (prevAlpha != mAlpha) {
						Color color = new Color(colorR,colorG,colorB,mAlpha) ;
						g2.setColor(color) ;
						prevAlpha = mAlpha ;
					}
					
					g2.fillRect(mX, mY, mSize,mSize) ;
				}
				else if (maskLayerHistoryEnabled) {
					int history = countMasksHits(maskHistory, maskI) ;
					
					if (history > 0) {
						float historyScale = (history/maxHistory) ;
						mAlpha = (int) (colorAlpha*(0.5f+(historyScale*0.5f)) ) ;
						
						if (prevAlpha != mAlpha) {
							Color color = new Color(colorR,colorG,colorB,mAlpha) ;
							g2.setColor(color) ;
							prevAlpha = mAlpha ;
						}
						
						g2.fillRect(mX, mY, mSize,mSize) ;
					}
				}
				
				maskI++ ;
			}
		}
		
		g2.setColor(prevColor) ;
	}
	
	private int countMasksHits(boolean[][] masks, int idx) {
		if (masks == null) return 0 ;
		
		int count = 0 ;
		for (int i = 0; i < masks.length; i++) {
			boolean[] m = masks[i];
			if (m != null && m[idx]) count++ ;
		}
		
		return count ;
	}
	
}
