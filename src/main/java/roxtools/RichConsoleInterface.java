package roxtools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

import roxtools.RichConsole.ConsoleImage;
import roxtools.RichConsole.RichConsoleMenuItem;
import roxtools.img.ImagePixels;

public interface RichConsoleInterface {

	public abstract void setVisible(boolean b);

	public abstract BufferedImage toBufferedImage(Color bgColor);

	public abstract void addMenuItem(String menuName, RichConsoleMenuItem item);

	public abstract void removeMenuItem(String menuName, RichConsoleMenuItem item);

	public abstract void removeMenu(String menuName);

	public abstract Dimension getPanelDimension();

	public abstract void setMaxConsoleElements(int maxConsoleElements);

	public abstract int getMaxConsoleElements();

	public abstract void waitConsoleElementsBelowSize(int size);

	public abstract void removeFromConsole(int nElements);

	public abstract void removeFromConsoleIfSizeBiggerThan(int size);

	public abstract void focus();

	public abstract void clearConsole();

	public abstract void printObj(Object obj);

	public abstract void printObjLn(Object obj);

	public abstract ConsoleImage printImage(Image img);

	public abstract ConsoleImage printImageLn(Image img);

	public abstract ConsoleImage printImage(ImagePixels img);

	public abstract ConsoleImage printImageLn(ImagePixels img);

	public abstract void newLine();

	public abstract void write(byte... bs);

	public abstract void writeLn(byte... bs);

	public abstract void setListener(RichConsoleListener listener);

	public abstract RichConsoleListener getListener();

}