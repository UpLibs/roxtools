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
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
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
		this.panel.repaint() ;
	}
	
	public void stepImages() {
		this.stepImageCount++ ;
		this.panel.repaint() ;
	}
	
	private boolean[][] masks ;
	
	public void setMasks(List<boolean[]> masks) {
		setMasks( masks.toArray( new boolean[ masks.size() ][] ) ) ;
	}
	
	public void setMasks(boolean[]... masks) {
		this.masks = masks;
		this.panel.repaint() ;
	}
	
	public void setImagesAndMasks(List<Image> images, List<boolean[]> masks) {
		setImagesAndMasks( images.toArray(new Image[images.size()])  , masks.toArray( new boolean[ masks.size() ][] ) ) ;
	}
	
	public void setImagesAndMasks(Image[] images, boolean[][] masks) {
		this.images = images ;
		this.stepImageCount++ ;
		this.masks = masks ;
		this.panel.repaint() ;
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
			boolean[][] masks = ImageViewer.this.masks ;
			
			if (images == null || images.length == 0) return ;
			
			Graphics2D g2 = (Graphics2D)g ;
			
			int x = 0 ;
			int y = 20 ;
			
			g2.drawString(""+stepImageCount, x+1, y-4) ;
			
			int maxH = 0 ;
			
			Area drawArea = new Area() ;
			
			for (int i = 0; i < images.length; i++) {
				Image img = images[i];
				
				if (img == null) continue ;
				
				boolean[] mask = masks != null && masks.length > i ? masks[i] : null ;
				
				double ratio = ratios[i] ;
				
				int w = img.getWidth(this) ;
				int h = img.getHeight(this) ;
				
				int w2 = (int) (w * ratio) ;
				int h2 = (int) (h * ratio) ;
				
				if (x+w2 > this.getWidth()) {
					x = 0 ;
					y += maxH ;
					maxH = 0 ;
				}
				
				g2.drawImage(img, x,y, w2,h2 , this) ;
				
				if (mask != null) paintMask(g2, mask, x, y, w, h, ratio) ;
				
				
				drawArea.add( new Area( new Rectangle(x,y, w2,h2) ) )  ;
				
				x += w2+2 ;
				maxH = Math.max(maxH, h2) ;
			}
			
			Rectangle bounds = drawArea.getBounds() ;
			
			updatePanelDimension(bounds) ;
		}
	}
	
	private void paintMask(Graphics2D g2, boolean[] mask, int x, int y, int w, int h, double ratio) {
		
		Color prevColor = g2.getColor() ;
		
		Color color = new Color(0,255,0,128) ;
		
		g2.setColor(color) ;
		
		int maskI = 0 ;
		
		int mSize = (int) ratio ;
		if (mSize < 1) mSize = 1 ;
		
		for (int j = 0; j < h; j++) {
			int mY = (int) (y + ( j * ratio )) ;
			
			for (int i = 0; i < w; i++) {
				boolean v = mask[maskI++] ;
				
				if (v) {
					int mX = (int) (x + ( i * ratio )) ;
					g2.fillRect(mX, mY, mSize,mSize) ;
				}
			}
		}
		
		g2.setColor(prevColor) ;
	}
	
	public static void main(String[] args) throws Exception {
		
		File imagesDir = new File("/Volumes/SAFEZONE/uppoints/test-snap9/") ;
		
		File[] listFiles = imagesDir.listFiles( new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName() ;
				return !name.startsWith(".") && pathname.length() > 1024*3 ;
			}
		}) ;
		
		ImageViewer imageViewer = new ImageViewer(0.2) ;
		
		for (int i = 0; i < listFiles.length; i+= 4) {
			File[] imgs = new File[4] ;
			System.arraycopy(listFiles, i, imgs, 0, 4) ;
			
			System.out.println(">> "+ Arrays.toString(imgs));
			
			imageViewer.setImageFiles(imgs) ;
			
			Thread.sleep(200) ;
		}
		
	}
	
}