package roxtools;

import java.awt.Point;

public interface RichConsoleListener {
	
	public void onRichConsoleClick(RichConsole richConsole, Point position) ;
	
	public void onRichConsolePress(RichConsole richConsole, Point position) ;
	
	public void onRichConsoleRelease(RichConsole richConsole, Point position) ;
	
	public void onRichConsoleDrag(RichConsole richConsole, Point position) ;

}
