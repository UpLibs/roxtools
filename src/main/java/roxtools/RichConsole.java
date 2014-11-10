package roxtools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import roxtools.RichConsole.ConsoleImageHighlight.Highlight;
import roxtools.img.ImagePixels;


final public class RichConsole extends JFrame implements RichConsoleListener {
	private static final long serialVersionUID = -8443078709304588707L;

	final static private HashMap<File, SoftReference<BufferedImage>> readImagesCache = new HashMap<File, SoftReference<BufferedImage>>() ;
	
	static private BufferedImage readImage(File fileImg) {
		
		synchronized (readImagesCache) {
			SoftReference<BufferedImage> ref = readImagesCache.get(fileImg) ;
			
			BufferedImage img = ref != null ? ref.get() : null ;
			
			if (img != null) return img ;
			
			try {
				img = ImageIO.read(fileImg);
				
				readImagesCache.put(fileImg, new SoftReference<BufferedImage>(img)) ;
				
				return img ;
			}
			catch (IOException e) {
				throw new IllegalStateException(e) ;
			}
		}
		
		
	}
	
	////////////////////////////////////////////
	
	static private interface MouseHandler {
		public void mouseClicked( RichConsole richConsole , int x, int y ) ;
		
		public void mousePressed( RichConsole richConsole , int x, int y ) ;
		
		public void mouseRelease( RichConsole richConsole , int x, int y ) ;
		
		public void mouseDragged( RichConsole richConsole , int x, int y ) ;
	}
	
	static public class ChartMap {
		final private float[][] map ;
		private float minValue ;
		private float maxValue ;
		final private int valueSize ;
		
		public ChartMap(boolean[] mapArray, int w, int h, int valueSize) {
			float[][] map = new float[w][h] ;
			
			{
				int idx = 0 ;
				for (int j = 0; j < h; j++) {
					for (int i = 0; i < w; i++) {
						map[i][j] = mapArray[idx++] ? 1 : 0 ;
					}
				}
			}
			
			this.map = map;
			
			this.minValue = 0 ;
			this.maxValue = 1 ;
			
			this.valueSize = valueSize ;
		}
		
		public ChartMap(float[] mapArray, int w, int h, int valueSize) {
			
			float[][] map = new float[w][h] ;
			
			{
				int idx = 0 ;
				for (int j = 0; j < h; j++) {
					for (int i = 0; i < w; i++) {
						map[i][j] = mapArray[idx++] ;
					}
				}
			}
			
			this.map = map;
			
			float[] minMax = StatisticsUtils.calcMinMax(mapArray) ;
			
			this.minValue = minMax[0];
			this.maxValue = minMax[1];
			
			this.valueSize = valueSize ;
		}
		
		public ChartMap(float[][] map, int valueSize) {
			this.map = map;
			
			float[] minMax = StatisticsUtils.calcMinMax( StatisticsUtils.floatMatrix2FloatList(map) ) ;
			
			this.minValue = minMax[0];
			this.maxValue = minMax[1];
			
			this.valueSize = valueSize ;
		}
		
		private int[] colors ;
		private float[] colorsValues ;
		
		public void setColorsValues(int[] colors, float[] values) {
			if (values.length != colors.length) throw new IllegalArgumentException("Colors and values should be of same size.") ;
			this.colors = colors ;
			this.colorsValues = values ;
		}
		
		public void clearColorsValues() {
			this.colors = null ;
			this.colorsValues = null ;
		}
		
		private boolean invertColor = false ;
		
		public void setInvertColor(boolean invertColor) {
			this.invertColor = invertColor;
		}
		
		public boolean getInvertColor() {
			return invertColor;
		}
		
		public ChartMap(float[][] map, float minValue, float maxValue, int valueSize) {
			this.map = map;
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.valueSize = valueSize ;
		}

		public ChartMap setMinMax(float minValue, float maxValue) {
			this.minValue = minValue;
			this.maxValue = maxValue;
			
			return this ;
		}
		
		public float[][] getMap() {
			return map;
		}

		public float getMinValue() {
			return minValue;
		}

		public float getMaxValue() {
			return maxValue;
		}

		public int getValueSize() {
			return valueSize;
		}
		
	}
	
	static public class ChartBars {
		private float[] data ;
		final private int barWidth ;
		final private int barHeight ;
		final private Color color ;
		
		public ChartBars(float[] data) {
			this(data, 5, 100) ;
		}
		
		public ChartBars(float[] data, int barWidth, int barHeight) {
			this(data, barWidth, barHeight, Color.BLUE) ;
		}
		
		public ChartBars(float[] data, int barWidth, int barHeight, Color color) {
			this.data = data;
			this.barWidth = barWidth;
			this.barHeight = barHeight;
			this.color = color ;
		}
		
		private int[] dataColors ;
		
		public void setDataColors(int[] dataColors) {
			this.dataColors = dataColors;
		}
		
		public int[] getDataColors() {
			return dataColors;
		}
		
		public Color getColor() {
			return color;
		}
		
		public float[] getData() {
			return data;
		}
		
		public int getBarWidth() {
			return barWidth;
		}
		
		public int getBarHeight() {
			return barHeight;
		}
		
		public ChartBars normalizeData() {
			float[] minMax = StatisticsUtils.calcMinMax(data) ;
			float min = minMax[0] ; 
			float max = minMax[1] ;
			float scale = max - min ;
			
			float[] data2 = new float[data.length] ;
			
			for (int i = 0; i < data.length; i++) {
				float v = data[i];
				v = (v-min) / scale ;
				data2[i] = v ;
			}
			
			this.data = data2 ;
			
			return this ;
		}
		
		public ChartBars normalizeData(float min, float max) {
			float scale = max - min ;
			
			float[] data2 = new float[data.length] ;
			
			for (int i = 0; i < data.length; i++) {
				float v = data[i];
				v = (v-min) / scale ;
				data2[i] = v ;
			}
			
			this.data = data2 ;
			
			return this ;
		}
		
	}
	
	static public class ChartDistribution {
		final private float[] data ;
		int width ;
		int height ;
		int divisions ;
		
		Float highlightData ;
		
		public ChartDistribution(float[] data) {
			this(data, 200, 100, 50) ;
		}
		
		public ChartDistribution(float[] data, int width, int height, int divisions) {
			this.data = data;
			this.width = width ;
			this.height = height ;
			this.divisions = divisions ;
		}
		
		public float[] getData() {
			return data;
		}
		
		public int getWidth() {
			return width;
		}
		
		public int getHeight() {
			return height;
		}
		
		public int getDivisions() {
			return divisions;
		}
		
		public ChartDistribution setHighlightData(float highlightData) {
			this.highlightData = highlightData;
			return this ;
		}
		
		public float getHighlightData() {
			return highlightData;
		}
		
	}
	
	static public interface ConsoleImageHighlightAction  {
		
		public void handleHighlightAction( Rectangle rect , String hint ) ;
		
	}
	
	static public class ConsoleImageHighlight extends ConsoleImage implements MouseHandler {

		public ConsoleImageHighlight(File fileImg, int width, int height) {
			super(fileImg, width, height);
		}

		public ConsoleImageHighlight(File fileImg) {
			super(fileImg);
		}

		public ConsoleImageHighlight(Image img, int width, int height) {
			super(img, width, height);
		}

		public ConsoleImageHighlight(Image img) {
			super(img);
		}
		
		protected class Highlight {
			private Rectangle rectangle ;
			private String hint ;
			private ConsoleImageHighlightAction action ;
			private boolean movable ;
			
			public Highlight(Rectangle rectangle) {
				this.rectangle = rectangle;
			}
			
			public boolean handlesMouseEvent() {
				return movable || hint != null || action != null ;
			}
			
			public void setHint(String hint) {
				this.hint = hint;
			}
			
			public void setAction(ConsoleImageHighlightAction action) {
				this.action = action;
			}
			
			public void setMovable(boolean movable) {
				this.movable = movable;
			}
			
			public boolean isMovable() {
				return movable;
			}
			
			public String getHint() {
				return hint;
			}
			
			public ConsoleImageHighlightAction getAction() {
				return action;
			}

			public void updatePosition(int x, int y) {
				updateRect( new Rectangle(x,y , rectangle.width , rectangle.height) ) ;
			}
			
			public void updateSize(int width, int height) {
				updateRect( new Rectangle(rectangle.x , rectangle.y, width, height) ) ;
			}
			
			public void updateRect(Rectangle rect) {
				
				synchronized (rectangleHighlights) {
					rectangleHighlights.remove(this.rectangle) ;
					rectangleHighlights.put(rect, this) ;
					
					this.rectangle = rect ;
				}
				
			}
		}
		
		private HashMap<Rectangle, Highlight> rectangleHighlights = new HashMap<Rectangle, Highlight>() ;
		private LinkedHashMap<Highlight, Color> highlights = new LinkedHashMap<Highlight, Color>() ;
		
		private LinkedHashMap<Highlight, Color> getHighlights() {
			return highlights;
		}
		
		private Highlight getHighlight(Rectangle rect, boolean autoCreate) {
			synchronized (rectangleHighlights) {
				Highlight highlight = rectangleHighlights.get(rect) ;
				if (highlight != null) return highlight ;
				
				if (!autoCreate) return null ;
				
				highlight = new Highlight(rect) ;
				rectangleHighlights.put(rect, highlight) ;
				return highlight ;
			}
			
		}
		
		public void addHighlight(Rectangle rect, int rgb) {
			addHighlight(rect, rgb, null) ;
		}
		
		public void addHighlight(Rectangle rect, int rgb, String hint) {
			addHighlight(rect, new Color(rgb), hint) ;
		}
		
		public void addHighlight(Rectangle rect, Color color) {
			addHighlight(rect, color, null) ;
		}
		
		public void addHighlight(Rectangle rect, Color color, String hint) {
			synchronized (highlights) {
				Highlight highlight = getHighlight(rect, true) ;
				
				highlights.put(highlight, color) ;
				if (hint != null) highlight.setHint(hint) ;
			}
		}
		
		public void addHighlight(Rectangle rect, Color color, String hint, ConsoleImageHighlightAction action ) {
			synchronized (highlights) {
				Highlight highlight = getHighlight(rect, true) ;
				
				highlights.put(highlight, color) ;
				
				if (hint != null) highlight.setHint(hint) ;
				if (action != null) highlight.setAction(action) ;
			}
		}
		
		public void addHighlight(Rectangle rect, Color color, String hint, ConsoleImageHighlightAction action , boolean movable) {
			synchronized (highlights) {
				Highlight highlight = getHighlight(rect, true) ;
				
				highlights.put(highlight, color) ;
				
				if (hint != null) highlight.setHint(hint) ;
				if (action != null) highlight.setAction(action) ;
				
				highlight.setMovable(movable) ;
			}
		}

		public void setHighlightHint( Rectangle rect , String hint ) {
			synchronized (highlights) {
				Highlight highlight = getHighlight(rect, false) ;
				if (highlight != null) highlight.setHint(hint) ;
			}
		}
		
		public void setHighlightAction( Rectangle rect , ConsoleImageHighlightAction action ) {
			synchronized (highlights) {
				Highlight highlight = getHighlight(rect, false) ;
				if (highlight != null) highlight.setAction(action) ;
			}
		}

		public void setHighlightMovable( Rectangle rect , boolean movable ) {
			synchronized (highlights) {
				Highlight highlight = getHighlight(rect, false) ;
				if (highlight != null) highlight.setMovable(movable) ;
			}
		}
		
		private boolean preserveHighlightRatio = true ;
		
		public void setPreserveHighlightRatio(boolean preserveHighlightRatio) {
			this.preserveHighlightRatio = preserveHighlightRatio;
		}
		
		public boolean getPreserveHighlightRatio() {
			return preserveHighlightRatio;
		}
		
		static private interface EventProcessor {
			public void process(Highlight highlight) ;
		}
		
		private void processMouseEvent(int x, int y, EventProcessor eventProcessor) {
			Highlight recClickWrapper = null ;
			double recCLickDist = 0 ;
			
			double clipWRatio = (this.width*1D) / getOriginalWidth() ;
			double clipHRatio = (this.height*1D) / getOriginalHeight() ;
			
			x = (int) (x * clipWRatio) ;
			y = (int) (y * clipHRatio) ;
			
			for (Highlight highlight : highlights.keySet()) {
				Rectangle rec = highlight.rectangle ;
				
				if ( highlight.handlesMouseEvent() && rec.contains(x,y) ) {
					int xDist = x - rec.x ;
					int yDist = y - rec.y ;
					
					double dist = Math.sqrt( xDist*xDist + yDist*yDist ) ;
					
					if (recClickWrapper == null || dist < recCLickDist) {
						recClickWrapper = highlight ;
						recCLickDist = dist ; 
					}
				}
			}
			
			if (recClickWrapper != null) eventProcessor.process(recClickWrapper) ;
		}
		
		@Override
		public void mouseClicked(RichConsole richConsole, int x, int y) {
			processMouseEvent(x, y, new EventProcessor() {
				@Override
				public void process(Highlight highlight) {
					showHint(highlight.rectangle) ;	
				}
			}) ;
		}
		

		private void showHint(Rectangle rec) {
			Highlight highlight = getHighlight(rec, false) ;
			
			String hintStr = highlight.getHint() ;
			
			if (hintStr != null) {
				System.out.println(this+"> "+ rec.x +","+ rec.y +" ; "+ rec.width +"x"+ rec.height +">> "+ hintStr);
			}
			
			ConsoleImageHighlightAction action = highlight.getAction() ;
			
			if (action != null) {
				try {
					action.handleHighlightAction(rec, hintStr) ;
				} catch (Exception e) {
					e.printStackTrace() ;
				}
			}
		}

		private Point pressPoint ;
		private Point pressRectanglePoint ;
		private WeakReference<Highlight> pressHighlight ;
		
		@Override
		public void mousePressed(RichConsole richConsole, int x, int y) {
			pressPoint = new Point(x, y) ;
			
			processMouseEvent(x, y, new EventProcessor() {
				@Override
				public void process(Highlight highlight) {
					if ( !highlight.isMovable() ) return ; 
						
					pressRectanglePoint = new Point(highlight.rectangle.x , highlight.rectangle.y) ;
					pressHighlight = new WeakReference<Highlight>(highlight) ;
				}
			}) ;
		}

		@Override
		public void mouseRelease(RichConsole richConsole, int x, int y) {
			pressPoint = null ;
			pressRectanglePoint = null ;
			pressHighlight = null ;
		}

		@Override
		public void mouseDragged(RichConsole richConsole, final int x, final int y) {
			if (pressPoint == null || pressRectanglePoint == null || pressHighlight == null) return ;
			
			int dX = x - pressPoint.x ;
			int dY = y - pressPoint.y ;
			
			Highlight highlight = pressHighlight.get() ;
			
			highlight.updatePosition( pressRectanglePoint.x + dX, pressRectanglePoint.y + dY ) ;
			
			richConsole.repaintPanel() ;
		}
		
		
	}
	
	static public class ConsoleImage {
		public Image img ;
		public int width ;
		public int height ;
		
		public ConsoleImage(File fileImg) {
			this.img = readImage(fileImg) ;
			this.width = img.getWidth(null) ;
			this.height = img.getHeight(null) ;
		}
		
		public ConsoleImage(File fileImg, int width, int height) {
			this.img = readImage(fileImg) ;
			this.width = width ;
			this.height = height ;
		}
		
		public ConsoleImage(Image img) {
			this.img = img ;
			this.width = img.getWidth(null) ;
			this.height = img.getHeight(null) ;
		}
		
		public ConsoleImage(Image img, int width, int height) {
			this.img =  img;
			this.width = width;
			this.height = height;
		}
		
		public Image getImage() {
			return img;
		}
		
		public int getOriginalWidth() {
			return img.getWidth(null) ;
		}
		
		public int getOriginalHeight() {
			return img.getHeight(null) ;
		}
		
		public ConsoleImage configureMaxWidth(int maxWidth) {
			if ( this.width > maxWidth ) {
				double ratio = maxWidth / (this.width*1d) ;
				
				this.width *= ratio ;
				this.height *= ratio ;
			}
			
			return this ;
		}
		
		public ConsoleImage configureMaxHeight(int maxHeight) {
			if ( this.height > maxHeight ) {
				double ratio = maxHeight / (this.height*1d) ;
				
				this.width *= ratio ;
				this.height *= ratio ;
			}
			
			return this ;
		}
		
		public ConsoleImage scale(double ratio) {
			this.width *= ratio ;
			this.height *= ratio ;
			
			return this ;
		}
		
		public ConsoleImage scale(int w, int h) {
			this.width = w ;
			this.height = h ;
			
			return this ;
		}
		
	}
	
	static public Color toColor(int rgb) {
		return new Color(rgb) ;
	}
	
	static public Color toColor(int r, int g, int b) {
		return new Color(r, g, b) ;
	}
	
	static public Color toColor(double light) {
		int l = (int) (255 * light) ;
		return new Color(l,l,l) ;
	}
	
	static public class Rec implements Cloneable {
		public Color color ;
		public int width ;
		public int height ;
		
		public Rec(int colorRGB) {
			this( new Color(colorRGB) ) ;
		}
		
		public Rec(Color color) {
			this(color, 10, 10) ;
		}
		
		public Rec(int colorRGB, int width, int height) {
			this( new Color(colorRGB), width, height ) ;
		}
		
		public Rec(Color color, int width, int height) {
			this.color = color;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public Rec clone() {
			try {
				return (Rec) super.clone() ;
			} catch (CloneNotSupportedException e) {
				throw new IllegalStateException(e) ;
			}
		}
		
		public Rec scale(double ratio) {
			Rec clone = clone() ;
			
			clone.width = (int) (clone.width * ratio) ;
			clone.height = (int) (clone.height * ratio) ;
			
			return clone ;
		}
		
		
	}
	

	static public class RecLine extends Rec {

		public RecLine(Color color, int width, int height) {
			super(color, width, height);
		}

		public RecLine(Color color) {
			super(color);
		}

		public RecLine(int colorRGB, int width, int height) {
			super(colorRGB, width, height);
		}

		public RecLine(int colorRGB) {
			super(colorRGB);
		}
		
	}
	
	static public class RecText extends Rec {

		public String text ;
		
		public RecText(Color color, int width, int height, String text) {
			super(color, width, height);
			this.text = text ;
		}

		public RecText(Color color, String text) {
			super(color);
			this.text = text ;
		}

		public RecText(int colorRGB, int width, int height, String text) {
			super(colorRGB, width, height);
			this.text = text ;
		}

		public RecText(int colorRGB, String text) {
			super(colorRGB);
			this.text = text ;
		}
		
	}
	
	/////////////////////////////////////////////////////////
	
	static private RichConsole defaultInstance ;
	
	static private ThreadLocal<RichConsole> threadDefaultInstance = new ThreadLocal<RichConsole>() ;
	
	public static void setThreadDefaultInstance(RichConsole richConsole) {
		threadDefaultInstance.set(richConsole) ;
	}
	
	public static void clearThreadDefaultInstance() {
		threadDefaultInstance.remove() ;
	}
	
	public static RichConsole getDefaultInstance() {
		RichConsole threadLocalInstance = threadDefaultInstance.get() ;
		
		if (threadLocalInstance != null) {
			return threadLocalInstance ;
		}
		
		if ( defaultInstance == null ) {
			synchronized (RichConsole.class) {
				if ( defaultInstance == null ) {
					defaultInstance = new RichConsole(true) ;
				}
			}
		}
		return defaultInstance;
	}
	
	static private boolean redirectedSysOut = false ;
	
	static public void redirectSystemOut() {
		
		synchronized (RichConsole.class) {
			if (redirectedSysOut) return ;
			redirectedSysOut = true ;
		}
		
		getDefaultInstance() ;
		
		final PrintStream originalOut = System.out ;
		
		OutputStream out = new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				originalOut.write(b) ;
				getDefaultInstance().write((byte)b) ;
			}
			
			@Override
			public void write(byte[] b) throws IOException {
				originalOut.write(b) ;
				getDefaultInstance().write(b) ;
			}
			
		};
		
		PrintStream printOut = new PrintStream(out) {
			@Override
			public void print(boolean b) {
				originalOut.print(b) ;
				getDefaultInstance().printObj(b) ;
			}
			
			@Override
			public void print(char c) {
				originalOut.print(c) ;
				getDefaultInstance().printObj(c) ;
			}
			
			@Override
			public void print(char[] s) {
				originalOut.print(s) ;
				getDefaultInstance().printObj(new String(s)) ;
			}
			
			@Override
			public void print(double d) {
				originalOut.print(d) ;
				getDefaultInstance().printObj(d) ;
			}
			@Override
			public void print(float f) {
				originalOut.print(f) ;
				getDefaultInstance().printObj(f) ;
			}
			@Override
			public void print(int i) {
				originalOut.print(i) ;
				getDefaultInstance().printObj(i) ;
			}
			@Override
			public void print(long l) {
				originalOut.print(l) ;
				getDefaultInstance().printObj(l) ;
			}
			@Override
			public void print(Object obj) {
				originalOut.print(obj) ;
				getDefaultInstance().printObj(obj) ;
			}
			@Override
			public void print(String s) {
				originalOut.print(s) ;
				getDefaultInstance().printObj(s) ;
			}
			@Override
			public void println() {
				originalOut.println() ;
				getDefaultInstance().newLine() ;
			}
			
			@Override
			public void println(boolean x) {
				originalOut.println(x) ;
				getDefaultInstance().printObjLn(x) ;
			}
			
			@Override
			public void println(char x) {
				originalOut.println(x) ;
				getDefaultInstance().printObjLn(x) ;
			}
			
			@Override
			public void println(char[] x) {
				originalOut.println(x) ;
				getDefaultInstance().printObjLn(new String(x)) ;
			}
			
			@Override
			public void println(double x) {
				originalOut.println(x) ;
				getDefaultInstance().printObjLn(x) ;
			}
			
			@Override
			public void println(float x) {
				originalOut.println(x) ;
				getDefaultInstance().printObjLn(x) ;
			}
			
			@Override
			public void println(int x) {
				originalOut.println(x) ;
				getDefaultInstance().printObjLn(x) ;
			}
			
			@Override
			public void println(long x) {
				originalOut.println(x) ;
				getDefaultInstance().printObjLn(x) ;
			}
			
			@Override
			public void println(Object x) {
				originalOut.println(x) ;
				getDefaultInstance().printObjLn(x) ;
			}
			
			@Override
			public void println(String x) {
				originalOut.println(x) ;
				getDefaultInstance().printObjLn(x) ;
			}
						
		};
		
		System.setOut(printOut) ;
		
		
		
	}
	
	static public void print(Object obj) {
		getDefaultInstance().printObj(obj) ;
	}
	
	static public void printLn(Object obj) {
		getDefaultInstance().printObjLn(obj) ;
	}
	
	static public ConsoleImage print(Image img) {
		return getDefaultInstance().printImage(img) ;
	}
	
	static public ConsoleImage printLn(Image img) {
		return getDefaultInstance().printImageLn(img) ;
	}
	
	static public ConsoleImage print(ImagePixels img) {
		return getDefaultInstance().printImage(img) ;
	}
	
	static public ConsoleImage printLn(ImagePixels img) {
		return getDefaultInstance().printImageLn(img) ;
	}
	
	static public void printLn() {
		getDefaultInstance().newLine() ;
	}
	
	///////////////////////////////////////////////////////
	
	static final public Color DEFAULT_BG_COLOR = new Color(240,240,240) ;
	static final public Color DEFAULT_COLOR = Color.BLACK ;
	
	final private Color bgColor ;
	final private Color defaultColor ;
	
	private JMenuBar menuBar ;
	private MyPanel panel ;
	
	public RichConsole() {
		this(false) ;
	}
	
	public RichConsole(boolean onCloseExit) {
		this( DEFAULT_BG_COLOR , DEFAULT_COLOR , onCloseExit ) ;
	}
	
	static final public int DEFAULT_WIDTH = 1000 ;
	static final public int DEFAULT_HEIGHT = 800 ;
	
	public RichConsole(Color bgColor, Color defaultColor) {
		this(bgColor, defaultColor, false) ;
	}
	
	public RichConsole(Color bgColor, Color defaultColor, boolean onCloseExit) {
		super( "RichConsole[ "+ getMainClassName() +" ]");
		
		this.bgColor = bgColor ;
		this.defaultColor = defaultColor ;
		
		this.menuBar = new JMenuBar() ;
		
		this.setJMenuBar(this.menuBar) ;
		
		this.panel = new MyPanel() ;
		this.panel.setBackground(bgColor) ;
		
		Container contentPane = getContentPane() ;
		
		contentPane.setLayout( new BorderLayout() ) ;
		
		this.panel.setPreferredSize(new Dimension(1300,6000)) ;
		
		scrollpane = new JScrollPane(this.panel);
		scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS) ;
		
		contentPane.add(scrollpane, BorderLayout.CENTER) ;
		
		
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT) ;
		setLocation(-1, -1) ;
		
		setVisible(true) ;
		
		if (onCloseExit) {
			setDefaultCloseOperation( EXIT_ON_CLOSE ) ;
		}
	}
	
	public BufferedImage toBufferedImage(Color bgColor) {
		return this.panel.toBufferedImage(bgColor) ;
	}
	
	private HashMap<String, List<RichConsoleMenuItem>> menusItems = new HashMap<String, List<RichConsoleMenuItem>>() ;
	
	public void addMenuItem(String menuName, RichConsoleMenuItem item) {
		boolean rebuilt = false ;
		
		synchronized (menusItems) {
			List<RichConsoleMenuItem> items = menusItems.get(menuName) ;
			
			if ( items == null ) menusItems.put( menuName , items = new ArrayList<RichConsoleMenuItem>() ) ;
			
			if ( !items.contains(item) ) {
				items.add(item) ;
				item.setRichConsole(this) ;
				rebuilt = true ;
			}
		}
		
		if (rebuilt) rebuildMenuBar() ;
	}
	
	public void removeMenuItem(String menuName, RichConsoleMenuItem item) {
		boolean rebuilt = false ;
		
		synchronized (menusItems) {
			List<RichConsoleMenuItem> items = menusItems.get(menuName) ;
			
			if ( items != null ) {
				if ( items.remove(item) ) {
					item.setRichConsole(null) ;
					rebuilt = true ;
				}	
			}
		}
		
		if (rebuilt) rebuildMenuBar() ;	
	}
	
	public void removeMenu(String menuName) {
		boolean rebuilt = false ;
		
		synchronized (menusItems) {
			List<RichConsoleMenuItem> items = menusItems.remove(menuName) ;
			
			if ( items != null ) {
				rebuilt = true ;
			}
		}
		
		if (rebuilt) rebuildMenuBar() ;	
	}
	
	private void rebuildMenuBar() {
		
		this.menuBar.removeAll() ;

		synchronized (menusItems) {
		
			ArrayList<String> keys = new ArrayList<String>() ;
			
			for (String key : menusItems.keySet()) {
				keys.add(key) ;
			}
			
			Collections.sort(keys) ;
			
			for (String key : keys) {
				List<RichConsoleMenuItem> items = menusItems.get(key) ;
				
				JMenu menu = new JMenu(key) ;
				
				for (final RichConsoleMenuItem item : items) {
					JMenuItem menuItem = new JMenuItem(  item.getTitle() ) ;

					menuItem.addActionListener( new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							item.action() ;
						}
					});
					
					menu.add(menuItem) ;
				}
				
				this.menuBar.add(menu) ;
				
			}
			
		}
		
		this.menuBar.revalidate() ;
		this.menuBar.repaint() ;
		
	}
	
	abstract static public class RichConsoleMenuItem {
		
		private String title ;
		
		public RichConsoleMenuItem(String title) {
			this.title = title;
		}
		
		public void setTitle(String title) {
			this.title = title;
			
			RichConsole richConsole = this.richConsole ;
			
			if (richConsole != null) richConsole.rebuildMenuBar() ;
		}

		public String getTitle() {
			return title;
		}
		
		private RichConsole richConsole ;
		
		private void setRichConsole(RichConsole richConsole) {
			this.richConsole = richConsole;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + title.hashCode() ;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			
			RichConsoleMenuItem other = (RichConsoleMenuItem) obj;
			
			if (!title.equals(other.title)) return false;
			return true;
		}
		
		abstract public void action() ;
	}
	
	public Dimension getPanelDimension() {
		return panel.getDimension() ;
	}
	
	private ArrayList<Object[]> output = new ArrayList<Object[]>() ;

	private int maxConsoleElements = 0 ;
	
	public void setMaxConsoleElements(int maxConsoleElements) {
		this.maxConsoleElements = maxConsoleElements;
	}
	
	public int getMaxConsoleElements() {
		return maxConsoleElements;
	}
	
	private void checkMaxConsoleElements() {
		int max = maxConsoleElements ;
		if (max <= 0) return ;
		
		synchronized (output) {
			while ( output.size() > max ) {
				output.remove(0) ;
			}
			
			output.notifyAll() ;
		}
		
		repaintPanel() ;
	}
	
	public void waitConsoleElementsBelowSize(int size) {
		synchronized (output) {
			while ( output.size() > size ) {
				try {
					output.wait(10000) ;
				} catch (InterruptedException e) {}
			}
		}
	}
	
	public void removeFromConsole(int nElements) {
		synchronized (output) {
			for (int i = 0; i < nElements && !output.isEmpty() ; i++) {
				output.remove(0) ;
			}
			
			output.notifyAll() ;
		}
		
		repaintPanel() ;
	}
	
	public void removeFromConsoleIfSizeBiggerThan(int size) {
		synchronized (output) {
			while ( output.size() > size ) {
				output.remove(0) ;
			}
			
			output.notifyAll() ;
		}
		
		repaintPanel() ;
	}
	
	public void focus() {
		this.setAlwaysOnTop(true) ;
		
		try {
			Thread.sleep(100) ;
		} catch (InterruptedException e) {}
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setAlwaysOnTop(false) ;		
			}
		});
		
	}
	
	public void clearConsole() {
		synchronized (output) {
			output.clear() ;
			output.notifyAll() ;
		}
		
		repaintPanel() ;
	}
	
	private JScrollPane scrollpane;
	
	public void printObj(Object obj) {
		synchronized (output) {
			output.add( new Object[] { obj } ) ;
			checkMaxConsoleElements() ;
		}
		
		repaintPanel() ;
	}
	
	public void printObjLn(Object obj) {
		synchronized (output) {
			output.add( new Object[] { obj , "\n" } ) ;
			checkMaxConsoleElements() ;
		}
		
		repaintPanel() ;
	}
	
	public ConsoleImage printImage(Image img) {
		ConsoleImage consoleImage = new ConsoleImage(img) ;
		
		synchronized (output) {
			output.add( new Object[] { consoleImage } ) ;
			checkMaxConsoleElements() ;
		}
		
		repaintPanel() ;
		
		return consoleImage ;
	}
	
	public ConsoleImage printImageLn(Image img) {
		ConsoleImage consoleImage = new ConsoleImage(img) ;
		
		synchronized (output) {
			output.add( new Object[] { consoleImage , "\n" } ) ;
			checkMaxConsoleElements() ;
		}
		
		repaintPanel() ;
		
		return consoleImage ;
	}
	
	public ConsoleImage printImage(ImagePixels img) {
		return print(img.createImage()) ;
	}
	
	public ConsoleImage printImageLn(ImagePixels img) {
		return printLn(img.createImage()) ;
	}
	
	public void newLine() {
		synchronized (output) {
			output.add( new Object[] { "\n" } ) ;
			checkMaxConsoleElements() ;
		}
		
		repaintPanel() ;
	}
	
	public void write(byte... bs) {
		print( new String(bs) ) ;
	}
	
	public void writeLn(byte... bs) {
		printLn( new String(bs) ) ;
	}
	
	private void repaintPanel() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				synchronized (this) {
					panel.revalidate() ;
					panel.repaint() ;
				}
			}
		});
	}
	
	private class MyPanel extends JPanel implements MouseListener , MouseMotionListener {
		private static final long serialVersionUID = -8309771023650295796L;

		private final Font font = new Font(Font.MONOSPACED, 0, 11) ;
		private final Font fontSmall = new Font(Font.MONOSPACED, 0, 9) ;
		
		public MyPanel() {
			this.addMouseListener(this) ;
			this.addMouseMotionListener(this) ;
		}
		
		private Graphics2D g2D ;
		
		private HashMap<Rectangle, MouseHandler> mouseHandlers = new HashMap<Rectangle, RichConsole.MouseHandler>() ; 
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			
			this.g2D = (Graphics2D) g ;	

			int maxX = 0 ;
			int maxY = 0 ;
					
			synchronized (output) {
				
				int x = 0 ;
				int y = 0 ;
				
				int lastW = 0 ;
				int lastH = 0 ;
				int[] ret = new int[2];
				
				for (Object[] objs : output) {
					
					for (Object obj : objs) {
						
						if (obj.equals("\n")) {
							y += lastH+1 ;
							x = 0 ;
							
							lastH = 0 ;
							lastW = 0 ;
						}
						else {
							int h = draw(obj, x, y, ret) ;
							
							lastW = ret[0] ;
							if (lastH < ret[1]) lastH = ret[1] ;
							
							x += lastW+1 ;
							
							if (h > 0) {
								y += h+1 ;
								x = 0 ;
							}
							else if (h < 0) {
								y += (-h)+1 ;
							}
						}
						
						if (maxX < x) maxX = x ;
						if (maxY < y) maxY = y ;
					}
					
				}
			
				maxX += lastW ;
				maxY += lastH ;
			}
			
			checkSize(maxX+10, maxY+10) ;
			
			this.g2D = null ;
		
		}
		
		public BufferedImage toBufferedImage(final Color bgColor) {
			
			if ( SwingUtilities.isEventDispatchThread() ) {
				return toBufferedImageImplem(bgColor) ;
			}
			
			try {
				final Vector<BufferedImage> ret = new Vector<BufferedImage>() ;
				
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						BufferedImage img = toBufferedImageImplem(bgColor) ;
						ret.add(img) ;
					}
				}) ;
				
				return ret.get(0) ;
			}
			catch (Exception e) {
				throw new java.lang.IllegalStateException(e) ;
			}
		}
		
		private BufferedImage toBufferedImageImplem(Color bgColor) {
			Dimension dim = getPreferredSize() ;
			
			BufferedImage buffImg = new BufferedImage(dim.width , dim.height, BufferedImage.TYPE_INT_RGB) ;
			
			Graphics2D g = buffImg.createGraphics() ;
			
			g.setColor(bgColor) ;
			
			g.fillRect(0, 0, dim.width , dim.height) ;
			
			paint(g) ;
			
			g.dispose() ;
			
			return buffImg ;
		}
		
		private int myWidth = -1 ;
		private int myHeight = -1 ;
		
		public Dimension getDimension() {
			return new Dimension(myWidth, myHeight) ;
		}
		
		private void checkSize(int w, int h) {
			if (myWidth != w || myHeight != h) {
				this.myWidth = w ;
				this.myHeight = h ;
				
				this.setPreferredSize(new Dimension(w,h)) ;
				
				scrollpane.revalidate() ;
			}
		}
		
		private void resetG2D() {

			g2D.setBackground(bgColor) ;
			g2D.setColor(defaultColor) ;
			g2D.setFont(font) ;
			
		}
		
		private final int[] dummyRet = new int[2] ;
		
		public int draw(Object obj, int x, int y, int[] ret) {
			resetG2D() ;
			
			if ( obj instanceof RecLine ) {
				return drawRecLine((RecLine) obj, x, y, ret) ;
			}
			else if ( obj instanceof RecText ) {
				int h = drawRec((Rec) obj, x, y, ret) ;
				
				resetG2D() ;
				g2D.setFont(fontSmall) ;
				drawString( ((RecText)obj).text , x, y, dummyRet) ;
				
				return h ;
			}
			else if ( obj instanceof Rec ) {
				return drawRec((Rec) obj, x, y, ret) ;
			}
			else if ( obj instanceof Image ) {
				return drawImage((Image) obj, x, y, ret) ;
			}
			else if ( obj instanceof ConsoleImageHighlight ) {
				return drawImageHighlight((ConsoleImageHighlight) obj, x, y, ret) ;
			}
			else if ( obj instanceof ConsoleImage ) {
				return drawImage((ConsoleImage) obj, x, y, ret) ;
			}
			else if ( obj instanceof ChartMap ) {
				return drawChartMap((ChartMap) obj, x, y, ret) ;
			}
			else if ( obj instanceof ChartBars ) {
				return drawChartBars((ChartBars) obj, x, y, ret) ;
			}
			else if ( obj instanceof ChartDistribution ) {
				return drawChartDistribution((ChartDistribution) obj, x, y, ret) ;
			}
			else {
				return drawString(obj.toString(), x, y, ret) ;
			}
			
		}
		
		public int drawImageHighlight(ConsoleImageHighlight img, int x, int y, int[] ret) {
			int drawRet = drawImage(img, x, y, ret) ;
			
			double wRatio = img.width / (img.getOriginalWidth() * 1d) ;
			double hRatio = img.height / (img.getOriginalHeight() * 1d) ;
			
			if (!img.getPreserveHighlightRatio()) {
				wRatio = hRatio = 1 ;
			}
			
			LinkedHashMap<Highlight,Color> highlights = img.getHighlights() ;
			
			synchronized (highlights) {
				Color color0 = g2D.getColor() ;
				
				for (Entry<Highlight, Color> entry : highlights.entrySet()) {
					Rectangle rec = entry.getKey().rectangle ;
					Color color = entry.getValue() ;
					
					int hX = (int) (rec.x * wRatio) ;
					int hY = (int) (rec.y * hRatio) ;
					int hW = (int) (rec.width * wRatio) ;
					int hH = (int) (rec.height * hRatio) ;
					
					g2D.setColor( color ) ;
					
					g2D.drawRect(x+hX, y+hY, hW, hH) ;
				}
				
				g2D.setColor(color0) ;
				
				mouseHandlers.put(new Rectangle(x,y , ret[0], ret[1]), img) ;
				
				return drawRet ;
			}
		}
		
		public int drawImage(ConsoleImage img, int x, int y, int[] ret) {
			g2D.drawImage(img.img , x,y, img.width, img.height , this) ;
			
			ret[0] = img.width ;
			ret[1] = img.height ;
			
			return 0 ;
		}
		
		public int drawImage(Image img, int x, int y, int[] ret) {
			g2D.drawImage(img, x,y , this) ;
			
			ret[0] = img.getWidth(this) ;
			ret[1] = img.getHeight(this) ;
			
			return 0 ;
		}
		
		public int drawRec(Rec rec, int x, int y, int[] ret) {
			
			g2D.setColor(rec.color) ;
			g2D.fillRect(x, y, rec.width, rec.height) ;
			
			ret[0] = rec.width ;
			ret[1] = rec.height ;
			
			return 0 ;
		}
		
		public int drawRecLine(RecLine rec, int x, int y, int[] ret) {
			
			g2D.setColor(rec.color) ;
			g2D.drawRect(x, y, rec.width, rec.height) ;
			
			ret[0] = rec.width ;
			ret[1] = rec.height ;
			
			return 0 ;
		}
		
		public int drawString(String str, int x, int y, int[] ret) {
			String[] lines = str.split("\n") ;
			
			int offY = 0 ;
			int maxW = 0 ;
			
			int lastW = 0 ;
			int lastH = 0 ;
			
			for (String line : lines) {
				drawStringLine(line, x, y+offY, ret) ;
				
				int w = ret[0] ;
				int h = ret[1] ;
				
				if (w > maxW) maxW = w ;
				
				offY += h ;
				
				lastW = w ;
				lastH = h ;
			}
			
			ret[0] = lastW ;
			ret[1] = lastH ;
			
			boolean changedLine = lines.length > 1 || str.endsWith("\n");
			
			return changedLine ? -(offY-lastH) : 0 ;
		}
		
		public void drawStringLine(String str, int x, int y, int[] ret) {
			
			FontRenderContext fontRenderContext = g2D.getFontRenderContext() ;
			
			Rectangle2D strRec = font.getStringBounds(str, fontRenderContext) ;
			
			int w = (int) strRec.getWidth() ;
			int h = (int) strRec.getHeight() ;
			
			g2D.drawString(str, x, y+h-1) ;
			
			ret[0] = w ;
			ret[1] = h ;
		}
		
		public int drawChartMap(ChartMap chart, int x, int y, int[] ret) {
			
			float[][] map = chart.map ;
			
			int w = map.length ;
			int h = map[0].length ;
			
			g2D.setColor(Color.WHITE) ;
			
			int valueSize = chart.valueSize ;
			
			int chartW = valueSize*w +2;
			int chartH = valueSize*h +2;
			
			g2D.fillRect(x, y, chartW, chartH) ;
			
			float min = chart.minValue ;
			float scale = chart.maxValue - chart.minValue ;
			
			boolean invert = chart.invertColor ;
			
			int[] colors = chart.colors ;
			float[] colorsValues = chart.colorsValues ;
			
			for (int j = 0; j < h; j++) {
				for (int i = 0; i < w; i++) {
					float v = map[i][j] ;
					
					float n = 1 - ((v - min) / scale) ;
					
					if (invert) n = 1-n ;
					
					int c = (int) (n * 255) ;
					
					if (c < 0) c = 0 ;
					else if (c > 255) c = 255 ;
					
					Color color = new Color(c,c,c) ;
					
					if (colors != null) {
						int colorIdx = ArrayUtils.indexOf(colorsValues, v) ;
						if (colorIdx >= 0) color = new Color( colors[colorIdx] ) ;
					}
					
					g2D.setColor(color) ;
					
					int vX = x + 1 + (valueSize * i) ;
					int vY = y + 1 + (valueSize * j) ;
					
					g2D.fillRect(vX, vY, valueSize,valueSize) ;
				}	
			}
			
			ret[0] = chartW ;
			ret[1] = chartH ;
			
			return 0 ;
		}
		
		public int drawChartBars(ChartBars chart, int x, int y, int[] ret) {
			
			float[] data = chart.data ;
			
			int barWidth = chart.barWidth ;
			int barHeight = chart.barHeight ;
			
			Color color = chart.color ;
			int[] dataColor = chart.dataColors ;
			
			Color color0 = g2D.getColor() ;
			
			for (int i = 0; i < data.length; i++) {
				int x2 = 2 + x + ((barWidth+1) * i) ;
				int h = (int) (barHeight * data[i]) ;
				int y2 = y + (barHeight - h) ;
				
				if (dataColor != null && i < dataColor.length) {
					int barColor = dataColor[i] ;
					g2D.setColor( new Color(barColor) ) ;
				}
				else {
					g2D.setColor(color) ;
				}
				
				g2D.fillRect(x2, y2, barWidth, h) ;
			}
			
			g2D.setColor(color) ;
			
			int chartW = ((barWidth+1) * data.length) + 2 ;
			int chartH = barHeight ;
			
			g2D.drawRect(x, y, chartW, chartH) ;
			
			g2D.setColor(color0) ;
			
			
			ret[0] = chartW+1 ;
			ret[1] = chartH+1 ;
			
			return 0 ;
		}
		
		public int drawChartDistribution(ChartDistribution chart, int x, int y, int[] ret) {
			float[] data = chart.data ;
			
			float dataMean = StatisticsUtils.calcMean(data) ;
			float dataDeviation = StatisticsUtils.calcStandardDeviation(data) ;
			float[] dataMinMax = StatisticsUtils.calcMinMax(data) ;
			float dataMin = dataMinMax[0] ;
			float dataMax = dataMinMax[1] ;
			float dataScale = dataMax - dataMin ;
			
			int chartWidth = chart.width ;
			int chartHeight = chart.height ;
			int chartDivisions = chart.divisions ;
			int chartDivisionsM1 = chartDivisions-1 ;
			
			int divWidth = chartWidth/chartDivisions ;
			if (divWidth > 1) divWidth-- ;
			
			Color color = Color.BLUE ;
			
			Color color0 = g2D.getColor() ;
			
			g2D.setColor(color) ;
			
			
			int[] divs = new int[chartDivisions] ;
			
			for (int i = 0; i < data.length; i++) {
				float v = data[i] ;
				v = (v-dataMin) / dataScale ;
				
				int divIdx = (int) (v * chartDivisionsM1 + 0.01) ;
				
				divs[divIdx]++ ;
			}
			
			float divsMax = StatisticsUtils.calcMinMax(divs)[1] ;

			for (int i = 0; i < divs.length; i++) {
				int d = divs[i];
				
				int divX = (int) (x + ((i*1d) / divs.length) * chartWidth) ;
				int divH = (int) ((d/divsMax) * chartHeight) ;
				int divY = y+(chartHeight-divH) ;
				
				g2D.fillRect(2+divX, divY, divWidth, divH) ;
			}
			
			{
				float v = (dataMean - dataMin) / dataScale ;
				
				int divX = (int) (x + v*chartWidth) ;
				
				g2D.setColor(Color.RED) ;
				g2D.fillRect(divX, y, 1, chartHeight) ;
				g2D.setColor(color) ;
			}
			
			{
				float vMean = (dataMean - dataMin) / dataScale ;
				float vDev = dataDeviation/dataScale ;
				
				float v1 = vMean+vDev ;
				float v2 = vMean-vDev ;
				
				int divX1 = (int) (x + v1*chartWidth) ;
				int divX2 = (int) (x + v2*chartWidth) ;
				
				g2D.setColor(Color.RED) ;
				g2D.fillRect(divX1, y, 1, chartHeight) ;
				g2D.fillRect(divX2, y, 1, chartHeight) ;
				g2D.setColor(color) ;
			}
			
			if ( chart.highlightData != null ) {
				float v = (chart.highlightData - dataMin) / dataScale ;
				
				int divX = (int) (x + v*chartWidth) ;
				
				g2D.setColor(Color.GREEN) ;
				g2D.fillRect(divX, y, 1, chartHeight) ;
				g2D.setColor(color) ;
			}
			
			
			g2D.setColor(Color.BLACK) ;
			g2D.drawRect(x, y, chartWidth+2, chartHeight) ;
			
			g2D.setColor(color0) ;
			
			ret[0] = chartWidth+1 ;
			ret[1] = chartHeight+1 ;
			
			return 0 ;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			int x = e.getX() ;
			int y = e.getY() ;
			
			for (Rectangle rec : mouseHandlers.keySet()) {
				if (rec.contains(x, y)) {
					MouseHandler mouseHandler = mouseHandlers.get(rec) ;
					mouseHandler.mouseClicked(RichConsole.this ,  x-rec.x, y-rec.y) ;
				}
			}
			
			listener.onRichConsoleClick( RichConsole.this , new Point(x, y) ) ;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			int x = e.getX() ;
			int y = e.getY() ;
			
			for (Rectangle rec : mouseHandlers.keySet()) {
				if (rec.contains(x, y)) {
					MouseHandler mouseHandler = mouseHandlers.get(rec) ;
					mouseHandler.mousePressed(RichConsole.this ,  x-rec.x, y-rec.y) ;
				}
			}
			
			listener.onRichConsoleClick( RichConsole.this , new Point(x, y) ) ;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			int x = e.getX() ;
			int y = e.getY() ;
			
			for (Rectangle rec : mouseHandlers.keySet()) {
				if (rec.contains(x, y)) {
					MouseHandler mouseHandler = mouseHandlers.get(rec) ;
					mouseHandler.mouseRelease(RichConsole.this ,  x-rec.x, y-rec.y) ;
				}
			}
			
			listener.onRichConsoleClick( RichConsole.this , new Point(x, y) ) ;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int x = e.getX() ;
			int y = e.getY() ;
			
			for (Rectangle rec : mouseHandlers.keySet()) {
				if (rec.contains(x, y)) {
					MouseHandler mouseHandler = mouseHandlers.get(rec) ;
					mouseHandler.mouseDragged(RichConsole.this ,  x-rec.x, y-rec.y) ;
				}
			}
			
			listener.onRichConsoleClick( RichConsole.this , new Point(x, y) ) ;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
	}
	
	static public String getMainClassName() {
		Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces() ;
		
		StackTraceElement[] mainStak = Thread.currentThread().getStackTrace(); 
		
		for (Entry<Thread, StackTraceElement[]> entry : allStackTraces.entrySet()) {
			//System.out.println(entry.getKey());
			if ( entry.getKey().getName().equals("main") ) {
				mainStak = entry.getValue() ;
				break ;
			}
		}
		
	    StackTraceElement main = mainStak[mainStak.length - 1];
	    String mainClassName = main.getClassName();
	    
	    return mainClassName ;
	}
	
	private RichConsoleListener listener = this ;
	
	public void setListener(RichConsoleListener listener) {
		this.listener = listener;
	}
	
	public RichConsoleListener getListener() {
		return listener;
	}

	@Override
	public void onRichConsoleClick(RichConsole richConsole, Point position) {
	}
	

	@Override
	public void onRichConsolePress(RichConsole richConsole, Point position) {
	}

	@Override
	public void onRichConsoleRelease(RichConsole richConsole, Point position) {
	}

	@Override
	public void onRichConsoleDrag(RichConsole richConsole, Point position) {
	}

	
	////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {
	
		//RichConsole.redirectSystemOut() ;
		
		RichConsole.getDefaultInstance().addMenuItem("Test", new RichConsoleMenuItem("Mohh") {
			@Override
			public void action() {
				System.out.println("Menu click!");
			}
		});
		

		float[][] map = new float[][] {
				{0,0,0,0.1f} ,
				{0,1,0.5f,0} ,
				{0,0.5f,1,0} ,
				{0.1f,0,0,0} ,
		};
		
		ChartMap chartMap = new RichConsole.ChartMap(map, 10) ;
		
		RichConsole.printLn(chartMap) ;
		
		RichConsole.printLn("--------------------------------------------------") ;
		
		RichConsole.print("moh\nhahah issa\nawwaawwawa") ;
		RichConsole.print( new Rec(Color.GRAY) ) ;
		RichConsole.print("beh") ;
		RichConsole.print("bah") ;
		RichConsole.printLn() ;
		RichConsole.print( new RecLine(Color.RED) ) ;
		RichConsole.print( new Rec(Color.BLUE) ) ;
		RichConsole.printLn() ;
		RichConsole.printLn( new Rec(Color.GREEN) ) ;
		RichConsole.printLn( new Rec(Color.YELLOW) ) ;
		
		RichConsole.printLn("\n> "+ getMainClassName() ) ;
		
		System.out.println("!!! NORMAL SYS OUT !!!");
		
		RichConsole.printLn("-----------------") ;
		
		/*
		RichConsole.print( new ConsoleImage(new File("/tmp/coca-main.png")).configureMaxWidth(100).scale(0.5) ) ;
		RichConsole.print( new ConsoleImage(new File("/tmp/coca-main.png")).configureMaxWidth(200).scale(0.5) ) ;
		RichConsole.printLn( new ConsoleImage(new File("/tmp/coca-main.png")).configureMaxWidth(100).scale(0.7) ) ;
		*/
		
		RichConsole.printLn("-----------------");
		
		float[] data = new float[] {1 , 2 , 3 , 10, 10.5f , 10.6f, 10.7f, 11, 11.5f, 11.6f, 11.7f, 12, 30,31,32, 50,51,52} ;
		
		data = new float[] {1 , 11 , 12 , 13 , 50,51,52} ;
		
		ChartBars chartBars = new ChartBars(data).normalizeData() ;
		
		RichConsole.printLn(chartBars);
		
		RichConsole.printLn("-----------------");
		

		ChartDistribution chartDist = new ChartDistribution(data, 200, 100, 10) ;
		
		chartDist.setHighlightData(20) ;
		
		RichConsole.printLn(chartDist);
		
		RichConsole.printLn("-----------------");

		
		ConsoleImageHighlight consoleImageHighlight = new RichConsole.ConsoleImageHighlight(new File("/Users/gracilianomp/ASSADii/assadii-logo-industrial-bg-16-9.png")) ;
		
		consoleImageHighlight.addHighlight(new Rectangle(100,100, 100,100), Color.GREEN, "mohh", new ConsoleImageHighlightAction() {
			@Override
			public void handleHighlightAction(Rectangle rect, String hint) {
				System.out.println("Action 1!!!!!");
			}
		}) ;
		

		consoleImageHighlight.addHighlight(new Rectangle(220,100, 100,100), Color.RED, "kkkk", new ConsoleImageHighlightAction() {
			@Override
			public void handleHighlightAction(Rectangle rect, String hint) {
				System.out.println("Action 2!!!!!");
			}
		} , true ) ;
		
		RichConsole.printLn(consoleImageHighlight) ;
		
	}

}
