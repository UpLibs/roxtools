package roxtools;

import java.awt.Point;

public interface RichConsoleListener {
	
	public void onRichConsoleClick(RichConsoleInterface richConsole, Point position) ;
	
	public void onRichConsolePress(RichConsoleInterface richConsole, Point position) ;
	
	public void onRichConsoleRelease(RichConsoleInterface richConsole, Point position) ;
	
	public void onRichConsoleDrag(RichConsoleInterface richConsole, Point position) ;

}
