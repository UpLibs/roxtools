package roxtools.img;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import roxtools.ImageUtils;
import roxtools.SerializationUtils;

public class ImagePixels implements Cloneable {
	

	static public void writePixels(OutputStream out , byte[] pixels, int w, int h) throws IOException {
		
		int w4 = w - (w % 4) ;
		int h4 = h - (h % 4) ;
		
		if (w4 == w && h4 == h) {
			writePixels_sizeMod4(out, pixels, w4, h4) ;
		}
		else {
			writePixels_anySize(out, pixels, w, h) ;
		}
		
	}

	static public byte[] readPixels(InputStream in , int w, int h) throws IOException {
		
		int w4 = w - (w % 4) ;
		int h4 = h - (h % 4) ;
		
		if (w4 == w && h4 == h) {
			return readPixels_sizeMod4(in, w4, h4) ;
		}
		else {
			return readPixels_anySize(in, w, h) ;
		}
	}
	
	static public void writePixels_anySize(OutputStream out , byte[] pixels, int w, int h) throws IOException {
		byte[] buff = new byte[4*4] ;
		int bufSz = 0 ;
		
		int wMod4 = (w/4) * 4 ;
		int hMod4 = (h/4) * 4 ;
		
		for (int j = 0; j < hMod4; j+=4) {
			int jIdx0 = j*w ;
			int jIdx1 = (j+1)*w ;
			int jIdx2 = (j+2)*w ;
			int jIdx3 = (j+3)*w ;
			
			for (int i = 0; i < wMod4; i+=4) {
				int idx00 = jIdx0+i ;
				int idx10 = jIdx0+i+1 ;
				int idx20 = jIdx0+i+2 ;
				int idx30 = jIdx0+i+3 ;
				
				int idx01 = jIdx1+i ;
				int idx11 = jIdx1+i+1 ;
				int idx21 = jIdx1+i+2 ;
				int idx31 = jIdx1+i+3 ;
				
				int idx02 = jIdx2+i ;
				int idx12 = jIdx2+i+1 ;
				int idx22 = jIdx2+i+2 ;
				int idx32 = jIdx2+i+3 ;
				
				int idx03 = jIdx3+i ;
				int idx13 = jIdx3+i+1 ;
				int idx23 = jIdx3+i+2 ;
				int idx33 = jIdx3+i+3 ;
				
				buff[0] = pixels[idx00] ;
				
				buff[1] = pixels[idx10] ;
				buff[2] = pixels[idx01] ;
				
				buff[3] = pixels[idx02] ;
				buff[4] = pixels[idx11] ;
				buff[5] = pixels[idx20] ;

				buff[6] = pixels[idx30] ;
				buff[7] = pixels[idx21] ;
				buff[8] = pixels[idx12] ;
				buff[9] = pixels[idx03] ;
				
				buff[10] = pixels[idx13] ;
				buff[11] = pixels[idx22] ;
				buff[12] = pixels[idx31] ;
				buff[13] = pixels[idx32] ;
				buff[14] = pixels[idx23] ;
				buff[15] = pixels[idx33] ;
								
				out.write(buff, 0, buff.length) ;
			}
			
			for (int i = wMod4; i < w; i+=4) {
				int idx00 = jIdx0+i ;
				int idx10 = jIdx0+i+1 ;
				int idx20 = jIdx0+i+2 ;
				int idx30 = jIdx0+i+3 ;
				
				int idx01 = jIdx1+i ;
				int idx11 = jIdx1+i+1 ;
				int idx21 = jIdx1+i+2 ;
				int idx31 = jIdx1+i+3 ;
				
				int idx02 = jIdx2+i ;
				int idx12 = jIdx2+i+1 ;
				int idx22 = jIdx2+i+2 ;
				int idx32 = jIdx2+i+3 ;
				
				int idx03 = jIdx3+i ;
				int idx13 = jIdx3+i+1 ;
				int idx23 = jIdx3+i+2 ;
				int idx33 = jIdx3+i+3 ;
				
				bufSz = 0 ;
				
				               buff[bufSz++] = pixels[idx00] ;
				
				if ( i+1 < w ) buff[bufSz++] = pixels[idx10] ;
							   buff[bufSz++] = pixels[idx01] ;
				
							   buff[bufSz++] = pixels[idx02] ;
				if ( i+1 < w ) buff[bufSz++] = pixels[idx11] ;
				if ( i+2 < w ) buff[bufSz++] = pixels[idx20] ;

				if ( i+3 < w ) buff[bufSz++] = pixels[idx30] ;
				if ( i+2 < w ) buff[bufSz++] = pixels[idx21] ;
				if ( i+1 < w ) buff[bufSz++] = pixels[idx12] ;
							   buff[bufSz++] = pixels[idx03] ;
				
				if ( i+1 < w ) buff[bufSz++] = pixels[idx13] ;
				if ( i+2 < w ) buff[bufSz++] = pixels[idx22] ;
				if ( i+3 < w ) buff[bufSz++] = pixels[idx31] ;
				if ( i+3 < w ) buff[bufSz++] = pixels[idx32] ;
				if ( i+2 < w ) buff[bufSz++] = pixels[idx23] ;
				if ( i+3 < w ) buff[bufSz++] = pixels[idx33] ;
				
				out.write(buff, 0, bufSz) ;
			}
		}
		
		//////////////////////////////////////////////////////
		
		for (int j = hMod4; j < h; j+=4) {
			int jIdx0 = j*w ;
			int jIdx1 = (j+1)*w ;
			int jIdx2 = (j+2)*w ;
			int jIdx3 = (j+3)*w ;
			
			for (int i = 0; i < wMod4; i+=4) {
				int idx00 = jIdx0+i ;
				int idx10 = jIdx0+i+1 ;
				int idx20 = jIdx0+i+2 ;
				int idx30 = jIdx0+i+3 ;
				
				int idx01 = jIdx1+i ;
				int idx11 = jIdx1+i+1 ;
				int idx21 = jIdx1+i+2 ;
				int idx31 = jIdx1+i+3 ;
				
				int idx02 = jIdx2+i ;
				int idx12 = jIdx2+i+1 ;
				int idx22 = jIdx2+i+2 ;
				int idx32 = jIdx2+i+3 ;
				
				int idx03 = jIdx3+i ;
				int idx13 = jIdx3+i+1 ;
				int idx23 = jIdx3+i+2 ;
				int idx33 = jIdx3+i+3 ;
				
				bufSz = 0 ;
				
							   buff[bufSz++] = pixels[idx00] ;
				
							   buff[bufSz++] = pixels[idx10] ;
				if ( j+1 < h ) buff[bufSz++] = pixels[idx01] ;
				
				if ( j+2 < h ) buff[bufSz++] = pixels[idx02] ;
				if ( j+1 < h ) buff[bufSz++] = pixels[idx11] ;
							   buff[bufSz++] = pixels[idx20] ;

							   buff[bufSz++] = pixels[idx30] ;
				if ( j+1 < h ) buff[bufSz++] = pixels[idx21] ;
				if ( j+2 < h ) buff[bufSz++] = pixels[idx12] ;
				if ( j+3 < h ) buff[bufSz++] = pixels[idx03] ;
				
				if ( j+3 < h ) buff[bufSz++] = pixels[idx13] ;
				if ( j+2 < h ) buff[bufSz++] = pixels[idx22] ;
				if ( j+1 < h ) buff[bufSz++] = pixels[idx31] ;
				if ( j+2 < h ) buff[bufSz++] = pixels[idx32] ;
				if ( j+3 < h ) buff[bufSz++] = pixels[idx23] ;
				if ( j+3 < h ) buff[bufSz++] = pixels[idx33] ;
				
				out.write(buff, 0, bufSz) ;
			}
			
			for (int i = wMod4; i < w; i+=4) {
				int idx00 = jIdx0+i ;
				int idx10 = jIdx0+i+1 ;
				int idx20 = jIdx0+i+2 ;
				int idx30 = jIdx0+i+3 ;
				
				int idx01 = jIdx1+i ;
				int idx11 = jIdx1+i+1 ;
				int idx21 = jIdx1+i+2 ;
				int idx31 = jIdx1+i+3 ;
				
				int idx02 = jIdx2+i ;
				int idx12 = jIdx2+i+1 ;
				int idx22 = jIdx2+i+2 ;
				int idx32 = jIdx2+i+3 ;
				
				int idx03 = jIdx3+i ;
				int idx13 = jIdx3+i+1 ;
				int idx23 = jIdx3+i+2 ;
				int idx33 = jIdx3+i+3 ;
				
				bufSz = 0 ;
				
										  buff[bufSz++] = pixels[idx00] ;
				
				if ( i+1 < w ) 			  buff[bufSz++] = pixels[idx10] ;
				if ( j+1 < h ) 			  buff[bufSz++] = pixels[idx01] ;
				
				if ( j+2 < h ) 			  buff[bufSz++] = pixels[idx02] ;
				if ( i+1 < w && j+1 < h ) buff[bufSz++] = pixels[idx11] ;
				if ( i+2 < w ) 			  buff[bufSz++] = pixels[idx20] ;

				if ( i+3 < w ) 			  buff[bufSz++] = pixels[idx30] ;
				if ( i+2 < w && j+1 < h ) buff[bufSz++] = pixels[idx21] ;
				if ( i+1 < w && j+2 < h ) buff[bufSz++] = pixels[idx12] ;
				if ( j+3 < h ) 			  buff[bufSz++] = pixels[idx03] ;
				
				if ( i+1 < w && j+3 < h ) buff[bufSz++] = pixels[idx13] ;
				if ( i+2 < w && j+2 < h ) buff[bufSz++] = pixels[idx22] ;
				if ( i+3 < w && j+1 < h ) buff[bufSz++] = pixels[idx31] ;
				if ( i+3 < w && j+2 < h ) buff[bufSz++] = pixels[idx32] ;
				if ( i+2 < w && j+3 < h ) buff[bufSz++] = pixels[idx23] ;
				if ( i+3 < w && j+3 < h ) buff[bufSz++] = pixels[idx33] ;
				
				out.write(buff, 0, bufSz) ;
			}
		}
		
	}
	
	static public void writePixels_sizeMod4(OutputStream out , byte[] pixels, int w, int h) throws IOException {
		byte[] buff = new byte[4*4] ;
		
		int w4 = w - (w % 4) ;
		int h4 = h - (h % 4) ;
		
		for (int j = 0; j < h4; j+=4) {
			int jIdx0 = j*w ;
			int jIdx1 = (j+1)*w ;
			int jIdx2 = (j+2)*w ;
			int jIdx3 = (j+3)*w ;
			
			for (int i = 0; i < w4; i+=4) {
				int idx00 = jIdx0+i ;
				int idx10 = jIdx0+i+1 ;
				int idx20 = jIdx0+i+2 ;
				int idx30 = jIdx0+i+3 ;
				
				int idx01 = jIdx1+i ;
				int idx11 = jIdx1+i+1 ;
				int idx21 = jIdx1+i+2 ;
				int idx31 = jIdx1+i+3 ;
				
				int idx02 = jIdx2+i ;
				int idx12 = jIdx2+i+1 ;
				int idx22 = jIdx2+i+2 ;
				int idx32 = jIdx2+i+3 ;
				
				int idx03 = jIdx3+i ;
				int idx13 = jIdx3+i+1 ;
				int idx23 = jIdx3+i+2 ;
				int idx33 = jIdx3+i+3 ;
				
				buff[0] = pixels[idx00] ;
				
				buff[1] = pixels[idx10] ;
				buff[2] = pixels[idx01] ;
				
				buff[3] = pixels[idx02] ;
				buff[4] = pixels[idx11] ;
				buff[5] = pixels[idx20] ;

				buff[6] = pixels[idx30] ;
				buff[7] = pixels[idx21] ;
				buff[8] = pixels[idx12] ;
				buff[9] = pixels[idx03] ;
				
				buff[10] = pixels[idx13] ;
				buff[11] = pixels[idx22] ;
				buff[12] = pixels[idx31] ;
				buff[13] = pixels[idx32] ;
				buff[14] = pixels[idx23] ;
				buff[15] = pixels[idx33] ;
			
				out.write(buff, 0, buff.length) ;
			}
		}
	}
	
	static public byte[] readPixels_anySize(InputStream in , int w, int h) throws IOException {
		byte[] pixels = new byte[w*h] ;
		
		byte[] buff = new byte[4*4] ;
		int bufSz = 0 ;
		
		int wMod4 = (w/4) * 4 ;
		int hMod4 = (h/4) * 4 ;
		
		for (int j = 0; j < hMod4; j+=4) {
			int jIdx0 = j*w ;
			int jIdx1 = (j+1)*w ;
			int jIdx2 = (j+2)*w ;
			int jIdx3 = (j+3)*w ;
			
			for (int i = 0; i < wMod4; i+=4) {
				int idx00 = jIdx0+i ;
				int idx10 = jIdx0+i+1 ;
				int idx20 = jIdx0+i+2 ;
				int idx30 = jIdx0+i+3 ;
				
				int idx01 = jIdx1+i ;
				int idx11 = jIdx1+i+1 ;
				int idx21 = jIdx1+i+2 ;
				int idx31 = jIdx1+i+3 ;
				
				int idx02 = jIdx2+i ;
				int idx12 = jIdx2+i+1 ;
				int idx22 = jIdx2+i+2 ;
				int idx32 = jIdx2+i+3 ;
				
				int idx03 = jIdx3+i ;
				int idx13 = jIdx3+i+1 ;
				int idx23 = jIdx3+i+2 ;
				int idx33 = jIdx3+i+3 ;
				
				///
				
				int r = in.read(buff, 0, buff.length) ;
				while (r < buff.length) {
					r += in.read(buff, r , buff.length-r) ;
				}
				
				///
				
				pixels[idx00] = buff[0] ;
				
				pixels[idx10] = buff[1] ;
				pixels[idx01] = buff[2] ;
				
				pixels[idx02] = buff[3] ;
				pixels[idx11] = buff[4] ;
				pixels[idx20] = buff[5] ;

				pixels[idx30] = buff[6] ;
				pixels[idx21] = buff[7] ;
				pixels[idx12] = buff[8] ;
				pixels[idx03] = buff[9] ;
				
				pixels[idx13] = buff[10] ;
				pixels[idx22] = buff[11] ;
				pixels[idx31] = buff[12] ;
				pixels[idx32] = buff[13] ;
				pixels[idx23] = buff[14] ;
				pixels[idx33] = buff[15] ;
			}
			
			for (int i = wMod4; i < w; i+=4) {
				int idx00 = jIdx0+i ;
				int idx10 = jIdx0+i+1 ;
				int idx20 = jIdx0+i+2 ;
				int idx30 = jIdx0+i+3 ;
				
				int idx01 = jIdx1+i ;
				int idx11 = jIdx1+i+1 ;
				int idx21 = jIdx1+i+2 ;
				int idx31 = jIdx1+i+3 ;
				
				int idx02 = jIdx2+i ;
				int idx12 = jIdx2+i+1 ;
				int idx22 = jIdx2+i+2 ;
				int idx32 = jIdx2+i+3 ;
				
				int idx03 = jIdx3+i ;
				int idx13 = jIdx3+i+1 ;
				int idx23 = jIdx3+i+2 ;
				int idx33 = jIdx3+i+3 ;
				
				bufSz = 0 ;
				
				bufSz++ ;
				
				if ( i+1 < w ) bufSz++ ;
				bufSz++ ;
				
				bufSz++ ;
				if ( i+1 < w ) bufSz++ ;
				if ( i+2 < w ) bufSz++ ;

				if ( i+3 < w ) bufSz++ ;
				if ( i+2 < w ) bufSz++ ;
				if ( i+1 < w ) bufSz++ ;
				if ( j+3 < h ) bufSz++ ;
				
				if ( i+1 < w ) bufSz++ ;
				if ( i+2 < w ) bufSz++ ;
				if ( i+3 < w ) bufSz++ ;
				if ( i+3 < w ) bufSz++ ;
				if ( i+2 < w ) bufSz++ ;
				if ( i+3 < w ) bufSz++ ;
				
				///
				
				int r = in.read(buff, 0, bufSz) ;
				while (r < bufSz) {
					r += in.read(buff, r , bufSz-r) ;
				}
				
				///
				
				bufSz = 0 ;
				
							   pixels[idx00] = buff[bufSz++] ;
				
				if ( i+1 < w ) pixels[idx10] = buff[bufSz++] ;
							   pixels[idx01] = buff[bufSz++] ;
				
							   pixels[idx02] = buff[bufSz++] ;
				if ( i+1 < w ) pixels[idx11] = buff[bufSz++] ;
				if ( i+2 < w ) pixels[idx20] = buff[bufSz++] ;

				if ( i+3 < w ) pixels[idx30] = buff[bufSz++] ;
				if ( i+2 < w ) pixels[idx21] = buff[bufSz++] ;
				if ( i+1 < w ) pixels[idx12] = buff[bufSz++] ;
							   pixels[idx03] = buff[bufSz++] ;
				
				if ( i+1 < w ) pixels[idx13] = buff[bufSz++] ;
				if ( i+2 < w ) pixels[idx22] = buff[bufSz++] ;
				if ( i+3 < w ) pixels[idx31] = buff[bufSz++] ;
				if ( i+3 < w ) pixels[idx32] = buff[bufSz++] ;
				if ( i+2 < w ) pixels[idx23] = buff[bufSz++] ;
				if ( i+3 < w ) pixels[idx33] = buff[bufSz++] ;	
			
			}
			
		}
		
		//////////////////////////////////////////////////////
		
		for (int j = hMod4; j < h; j+=4) {
			int jIdx0 = j*w ;
			int jIdx1 = (j+1)*w ;
			int jIdx2 = (j+2)*w ;
			int jIdx3 = (j+3)*w ;
			
			for (int i = 0; i < wMod4; i+=4) {
				int idx00 = jIdx0+i ;
				int idx10 = jIdx0+i+1 ;
				int idx20 = jIdx0+i+2 ;
				int idx30 = jIdx0+i+3 ;
				
				int idx01 = jIdx1+i ;
				int idx11 = jIdx1+i+1 ;
				int idx21 = jIdx1+i+2 ;
				int idx31 = jIdx1+i+3 ;
				
				int idx02 = jIdx2+i ;
				int idx12 = jIdx2+i+1 ;
				int idx22 = jIdx2+i+2 ;
				int idx32 = jIdx2+i+3 ;
				
				int idx03 = jIdx3+i ;
				int idx13 = jIdx3+i+1 ;
				int idx23 = jIdx3+i+2 ;
				int idx33 = jIdx3+i+3 ;
				
				bufSz = 0 ;
				
				bufSz++ ;
				
				bufSz++ ;
				if ( j+1 < h ) bufSz++ ;
				
				if ( j+2 < h ) bufSz++ ;
				if ( j+1 < h ) bufSz++ ;
				bufSz++ ;

				bufSz++ ;
				if ( j+1 < h ) bufSz++ ;
				if ( j+2 < h ) bufSz++ ;
				if ( j+3 < h ) bufSz++ ;
				
				if ( j+3 < h ) bufSz++ ;
				if ( j+2 < h ) bufSz++ ;
				if ( j+1 < h ) bufSz++ ;
				if ( j+2 < h ) bufSz++ ;
				if ( j+3 < h ) bufSz++ ;
				if ( j+3 < h ) bufSz++ ;
				
				///
				
				int r = in.read(buff, 0, bufSz) ;
				while (r < bufSz) {
					r += in.read(buff, r , bufSz-r) ;
				}
				
				///
				
				bufSz = 0 ;
				
							   pixels[idx00] = buff[bufSz++] ;
				
							   pixels[idx10] = buff[bufSz++] ;
				if ( j+1 < h ) pixels[idx01] = buff[bufSz++] ;
				
				if ( j+2 < h ) pixels[idx02] = buff[bufSz++] ;
				if ( j+1 < h ) pixels[idx11] = buff[bufSz++] ;
							   pixels[idx20] = buff[bufSz++] ;

							   pixels[idx30] = buff[bufSz++] ;
				if ( j+1 < h ) pixels[idx21] = buff[bufSz++] ;
				if ( j+2 < h ) pixels[idx12] = buff[bufSz++] ;
				if ( j+3 < h ) pixels[idx03] = buff[bufSz++] ;
				
				if ( j+3 < h ) pixels[idx13] = buff[bufSz++] ;
				if ( j+2 < h ) pixels[idx22] = buff[bufSz++] ;
				if ( j+1 < h ) pixels[idx31] = buff[bufSz++] ;
				if ( j+2 < h ) pixels[idx32] = buff[bufSz++] ;
				if ( j+3 < h ) pixels[idx23] = buff[bufSz++] ;
				if ( j+3 < h ) pixels[idx33] = buff[bufSz++] ;	
			
			}
			
			for (int i = wMod4; i < w; i+=4) {
				int idx00 = jIdx0+i ;
				int idx10 = jIdx0+i+1 ;
				int idx20 = jIdx0+i+2 ;
				int idx30 = jIdx0+i+3 ;
				
				int idx01 = jIdx1+i ;
				int idx11 = jIdx1+i+1 ;
				int idx21 = jIdx1+i+2 ;
				int idx31 = jIdx1+i+3 ;
				
				int idx02 = jIdx2+i ;
				int idx12 = jIdx2+i+1 ;
				int idx22 = jIdx2+i+2 ;
				int idx32 = jIdx2+i+3 ;
				
				int idx03 = jIdx3+i ;
				int idx13 = jIdx3+i+1 ;
				int idx23 = jIdx3+i+2 ;
				int idx33 = jIdx3+i+3 ;
				
				bufSz = 0 ;
				
				bufSz++ ;
				
				if ( i+1 < w ) bufSz++ ;
				if ( j+1 < h ) bufSz++ ;
				
				if ( j+2 < h ) bufSz++ ;
				if ( i+1 < w && j+1 < h ) bufSz++ ;
				if ( i+2 < w ) bufSz++ ;

				if ( i+3 < w ) bufSz++ ;
				if ( i+2 < w && j+1 < h ) bufSz++ ;
				if ( i+1 < w && j+2 < h ) bufSz++ ;
				if ( j+3 < h ) bufSz++ ;
				
				if ( i+1 < w && j+3 < h ) bufSz++ ;
				if ( i+2 < w && j+2 < h ) bufSz++ ;
				if ( i+3 < w && j+1 < h ) bufSz++ ;
				if ( i+3 < w && j+2 < h ) bufSz++ ;
				if ( i+2 < w && j+3 < h ) bufSz++ ;
				if ( i+3 < w && j+3 < h ) bufSz++ ;
				
				///
				
				int r = in.read(buff, 0, bufSz) ;
				while (r < bufSz) {
					r += in.read(buff, r , bufSz-r) ;
				}
				
				///
				
				bufSz = 0 ;
				
										  pixels[idx00] = buff[bufSz++] ;
				
				if ( i+1 < w ) 			  pixels[idx10] = buff[bufSz++] ;
				if ( j+1 < h ) 			  pixels[idx01] = buff[bufSz++] ;
				
				if ( j+2 < h ) 			  pixels[idx02] = buff[bufSz++] ;
				if ( i+1 < w && j+1 < h ) pixels[idx11] = buff[bufSz++] ;
				if ( i+2 < w ) 			  pixels[idx20] = buff[bufSz++] ;

				if ( i+3 < w ) 			  pixels[idx30] = buff[bufSz++] ;
				if ( i+2 < w && j+1 < h ) pixels[idx21] = buff[bufSz++] ;
				if ( i+1 < w && j+2 < h ) pixels[idx12] = buff[bufSz++] ;
				if ( j+3 < h )			  pixels[idx03] = buff[bufSz++] ;
				
				if ( i+1 < w && j+3 < h ) pixels[idx13] = buff[bufSz++] ;
				if ( i+2 < w && j+2 < h ) pixels[idx22] = buff[bufSz++] ;
				if ( i+3 < w && j+1 < h ) pixels[idx31] = buff[bufSz++] ;
				if ( i+3 < w && j+2 < h ) pixels[idx32] = buff[bufSz++] ;
				if ( i+2 < w && j+3 < h ) pixels[idx23] = buff[bufSz++] ;
				if ( i+3 < w && j+3 < h ) pixels[idx33] = buff[bufSz++] ;	
			
			}
		}
		
		return pixels ;
	}
	
	static public byte[] readPixels_sizeMod4(InputStream in , int w, int h) throws IOException {
		byte[] pixels = new byte[w*h] ;
		
		byte[] buff = new byte[4*4] ;
		
		int w4 = w - (w % 4) ;
		int h4 = h - (h % 4) ;
		
		
		for (int j = 0; j < h4; j+=4) {
			int jIdx0 = j*w ;
			int jIdx1 = (j+1)*w ;
			int jIdx2 = (j+2)*w ;
			int jIdx3 = (j+3)*w ;
			
			for (int i = 0; i < w4; i+=4) {
				int idx00 = jIdx0+i ;
				int idx10 = jIdx0+i+1 ;
				int idx20 = jIdx0+i+2 ;
				int idx30 = jIdx0+i+3 ;
				
				int idx01 = jIdx1+i ;
				int idx11 = jIdx1+i+1 ;
				int idx21 = jIdx1+i+2 ;
				int idx31 = jIdx1+i+3 ;
				
				int idx02 = jIdx2+i ;
				int idx12 = jIdx2+i+1 ;
				int idx22 = jIdx2+i+2 ;
				int idx32 = jIdx2+i+3 ;
				
				int idx03 = jIdx3+i ;
				int idx13 = jIdx3+i+1 ;
				int idx23 = jIdx3+i+2 ;
				int idx33 = jIdx3+i+3 ;
				
				///
				
				int r = in.read(buff, 0, buff.length) ;
				while (r < buff.length) {
					r += in.read(buff, r , buff.length-r) ;
				}
				
				///
				
				pixels[idx00] = buff[0] ;
				
				pixels[idx10] = buff[1] ;
				pixels[idx01] = buff[2] ;
				
				pixels[idx02] = buff[3] ;
				pixels[idx11] = buff[4] ;
				pixels[idx20] = buff[5] ;

				pixels[idx30] = buff[6] ;
				pixels[idx21] = buff[7] ;
				pixels[idx12] = buff[8] ;
				pixels[idx03] = buff[9] ;
				
				pixels[idx13] = buff[10] ;
				pixels[idx22] = buff[11] ;
				pixels[idx31] = buff[12] ;
				pixels[idx32] = buff[13] ;
				pixels[idx23] = buff[14] ;
				pixels[idx33] = buff[15] ;	
				
			}
		}
		
		return pixels ;
	}

	static public void toHalf(byte[] src, byte[] dst, int width, int height) {
		int dstSz = 0 ;
		
		for (int j = 0; j < height; j++) {
			int jIdx = j*width ;
			
			for (int i = 0; i < width; i+=2) {
				int srcI = jIdx + i ;
				dst[dstSz++] = src[srcI] ;	
			}
		}
		
	}
	
	static public void toDouble(byte[] src, byte[] dst, int width, int height) {
		int srcI = 0 ;
		
		int widthMod2 = width - (width % 2) ;
		
		for (int j = 0; j < height; j++) {
			int jIdx = j*width ;
			
			for (int i = 0; i < widthMod2; i+=2) {
				int dstI = jIdx + i ;
				byte b = src[srcI++] ;
				
				dst[dstI] = b ;
				dst[dstI + 1] = b ;
			}
			
			for (int i = widthMod2; i < width; i+=2) {
				int dstI = jIdx + i ;
				byte b = src[srcI++] ;
				
				dst[dstI] = b ;
			}
		}
		
	}
	
	static public int[] toInts(byte[] bs) {
		int[] ns = new int[bs.length] ;
		
		for (int i = ns.length-1; i >= 0; i--) {
			ns[i] = bs[i] & 0xff ;
		}
		
		return ns ;
	}
	
	static public byte[] toBytes(int[] ns) {
		byte[] bs = new byte[ns.length] ;
		
		for (int i = bs.length-1; i >= 0; i--) {
			bs[i] = (byte) ns[i] ;
		}
		
		return bs ;
	}
	
	static public int clip(int i) {
		return i < 0 ? 0 : (i > 255 ? 255 : i) ;
	}
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////
	
	protected byte[] pixelsC1 ;
	protected byte[] pixelsC2 ;
	protected byte[] pixelsC3 ;
	
	protected int width ;
	protected int height ;
	

	public ImagePixels(File imageFile) {
		this( ImageUtils.readFile(imageFile) ) ;
	}
	
	public ImagePixels(BufferedImage image) {
		this( ImageUtils.grabPixels(image) , image.getWidth() , image.getHeight() ) ;
	}
	
	public ImagePixels(int[] pixels, int width, int height) {
		this( 
				ImageUtils.getPixelsPart(pixels, 16) ,
				ImageUtils.getPixelsPart(pixels, 8) ,
				ImageUtils.getPixelsPart(pixels, 0) ,
				width, height,
				false
				) ;
	}
	
	private boolean mutable = true ;
	
	final public void setMutable(boolean mutable) {
		this.mutable = mutable;
	}
	
	final public boolean isMutable() {
		return mutable;
	}
	
	private void checkMutable() {
		if (!mutable) throw new AssertionError("Can't mutate") ;
	}
	
	static public byte[] createChannel(int size, byte val) {
		byte[] bs = new byte[size] ;
		for (int i = 0; i < bs.length; i++) {
			bs[i] = val ;
		}
		return bs ;
	}
	
	public ImagePixels(byte[] pixelsC1, int width, int height, boolean yuvFormat) {
		this(pixelsC1 , createChannel(pixelsC1.length , (byte)128) , createChannel(pixelsC1.length , (byte)128) , width , height, yuvFormat) ;
	}
	
	public ImagePixels(int[] pixelsC1, int[] pixelsC2, int[] pixelsC3, int width, int height, boolean yuvFormat) {
		this( toBytes(pixelsC1) , toBytes(pixelsC2) , toBytes(pixelsC3) , width , height , yuvFormat ) ;
	}
	
	public ImagePixels(byte[] pixelsC1, byte[] pixelsC2, byte[] pixelsC3, int width, int height, boolean yuvFormat) {
		int totalPixels = width*height ;
		
		if (pixelsC1.length != totalPixels) throw new IllegalArgumentException("pixelsC1.length: "+ pixelsC1.length +" != "+ totalPixels +" ["+ width +"x"+ height + "]") ;
		if (pixelsC2.length != totalPixels) throw new IllegalArgumentException("pixelsC2.length: "+ pixelsC2.length + "!= "+ totalPixels +" ["+ width +"x"+ height + "]") ;
		if (pixelsC2.length != totalPixels) throw new IllegalArgumentException("pixelsC2.length: "+ pixelsC3.length +"!= "+ totalPixels +" ["+ width +"x"+ height + "]") ;
		
		this.pixelsC1 = pixelsC1;
		this.pixelsC2 = pixelsC2;
		this.pixelsC3 = pixelsC3;

		this.width = width;
		this.height = height;
		
		this.yuvFormat = yuvFormat ;
	}
	
	private ImagePixels(ImagePixels other) {
		this.pixelsC1 = other.pixelsC1.clone() ;
		this.pixelsC2 = other.pixelsC2.clone() ;
		this.pixelsC3 = other.pixelsC3.clone() ;
		
		this.width = other.width ;
		this.height = other.height ;
		this.yuvFormat = other.yuvFormat ;
	}
	
	private ImagePixels(ImagePixels other, ImagePixels recicledBufferProvider) {
		this(other, recicledBufferProvider.pixelsC1, recicledBufferProvider.pixelsC2, recicledBufferProvider.pixelsC3) ;
	}
	
	private ImagePixels(ImagePixels other, byte[] bufferC1, byte[] bufferC2, byte[] bufferC3) {
		System.arraycopy(other.pixelsC1, 0, bufferC1, 0, other.pixelsC1.length) ;
		System.arraycopy(other.pixelsC2, 0, bufferC2, 0, other.pixelsC2.length) ;
		System.arraycopy(other.pixelsC3, 0, bufferC3, 0, other.pixelsC3.length) ;
		
		this.pixelsC1 = bufferC1 ;
		this.pixelsC2 = bufferC2 ;
		this.pixelsC3 = bufferC3 ;
		
		this.width = other.width ;
		this.height = other.height ;
		this.yuvFormat = other.yuvFormat ;
	}
	
	@Override
	public ImagePixels clone() {
		return new ImagePixels(this) ;
	}
	
	final public byte[] getPixelsC1() {
		return pixelsC1;
	}
	
	final public byte[] getPixelsC2() {
		return pixelsC2;
	}
	
	final public byte[] getPixelsC3() {
		return pixelsC3;
	}
	
	final public int getWidth() {
		return width;
	}
	
	final public int getHeight() {
		return height;
	}
	
	final public Dimension getDimension() {
		return new Dimension(width, height) ;
	}
	
	final public int getTotalPixels() {
		return width * height ;
	}
	
	public ImagePixels copy() {
		return new ImagePixels(this) ;
	}
	
	public ImagePixels copy(byte[] bufferC1, byte[] bufferC2, byte[] bufferC3) {
		return new ImagePixels(this, bufferC1, bufferC2, bufferC3) ;
	}
	
	public ImagePixels copy(ImagePixels recicledBufferProvider) {
		return new ImagePixels(this, recicledBufferProvider) ;
	}
	
	/////////////////////////////////////////////////////////
	
	private HashMap<String, Object> properties ;
	
	public void setProperty(String key, Object val) {
		if (properties == null) properties = new HashMap<String, Object>() ;
		properties.put(key, val) ;
	}
	
	public Object getProperty(String key) {
		if (properties == null) return null ;
		return properties.get(key) ;
	}
	
	public Object removeProperty(String key) {
		if (properties == null) return null ;
		return properties.remove(key) ;
	}
	
	public boolean containsProperty(String key) {
		if (properties == null) return false ;
		return properties.containsKey(key) ;
	}
	
	public int getPropertiesSize() {
		if (properties == null) return 0 ;
		return properties.size() ;
	}
	
	public void clearProperties() {
		if (properties == null) return ;
		properties.clear();
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getProperties() {
		if (properties == null) return new HashMap<String, Object>() ;
		return (HashMap<String, Object>) properties.clone() ;
	}
	
	/////////////////////////////////////////////////////////
		
	protected boolean yuvFormat = false ;
	
	final public boolean isYUVFormat() {
		return yuvFormat;
	}
	
	final public ImagePixels convertToYUV() {
		if (yuvFormat) return this ;
		
		checkMutable() ;
		
		convertToYUV(pixelsC1, pixelsC2, pixelsC3) ;
		yuvFormat = true ;
		return this ;
	}

	static public void convertToYUV( byte[] pixelsR , byte[] pixelsG, byte[] pixelsB ) {
		byte[] yuv = new byte[3] ;
		
		for (int i = 0; i < pixelsR.length; i++) {
			byte r = pixelsR[i] ;
			byte g = pixelsG[i] ;
			byte b = pixelsB[i] ;
			
			YUV.RGB_to_arrayYUV_fast(r & 0xff, g & 0xff, b&0xff, yuv) ;
			
			pixelsR[i] = yuv[0] ;
			pixelsG[i] = yuv[1] ;
			pixelsB[i] = yuv[2] ;
		}
	}
	
	final public ImagePixels convertToRGB() {
		if (!yuvFormat) return this ;
		
		checkMutable() ;
		
		convertToRGB(pixelsC1, pixelsC2, pixelsC3) ;
		yuvFormat = false ;
		return this ;
	}
	
	static public void convertToRGB( byte[] pixelsY , byte[] pixelsU, byte[] pixelsV ) {
		byte[] rgb = new byte[3] ;
		
		for (int i = 0; i < pixelsY.length; i++) {
			int y = pixelsY[i] & 0xff ;
			int u = pixelsU[i] & 0xff ;
			int v = pixelsV[i] & 0xff ;
			
			YUV.YUV_to_arrayRGB_fast(y, u, v, rgb) ;
			
			pixelsY[i] = rgb[0] ;
			pixelsU[i] = rgb[1] ;
			pixelsV[i] = rgb[2] ;
		}
	}
	
	final public int[] createRGBPixelsList() {
		int[] mergePixels ;
		if (isYUVFormat()) {
			mergePixels = ImageUtils.mergePixelsYUV(pixelsC1, pixelsC2, pixelsC3, width,height);
		}
		else {
			mergePixels = ImageUtils.mergePixels(pixelsC1, pixelsC2, pixelsC3, width,height);
		}
		
		return mergePixels ;
	}
	
	final public BufferedImage createImage() {
		int[] pixelsList = createRGBPixelsList() ;
		BufferedImage img = ImageUtils.createImage( pixelsList , width, height ) ;
		return img ;
	}
	
	final public void writeTo(OutputStream out) throws IOException {
		byte[] buffInt = new byte[9] ;
		
		buffInt[0] = (byte) (yuvFormat ? 1 : 0) ;
		SerializationUtils.writeInt(width, buffInt, 1) ;
		SerializationUtils.writeInt(height, buffInt, 1+4) ;
		
		out.write(buffInt) ;
		
		writePixels(out, pixelsC1, width, height) ;
		
		if ( yuvFormat ) {
			int widthHalf = width/2 ;
			if (width % 2 != 0) widthHalf++ ;
			
			byte[] half = new byte[widthHalf * height] ;
			
			toHalf(pixelsC2, half, width, height) ;
			writePixels(out, half, widthHalf, height) ;
			
			toHalf(pixelsC3, half, width, height) ;
			writePixels(out, half, widthHalf, height) ;	
		}
		else {
			writePixels(out, pixelsC2, width, height) ;
			writePixels(out, pixelsC3, width, height) ;
		}
		
	}
	
	final public byte[] getBytes() throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream( (int)Math.max(1024*4, (width * height * 3) * 0.67 )) ;
		writeTo(bout);
		return bout.toByteArray() ;
	}
	
	final public byte[] getBytesCompressed() throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream( (int)Math.max(1024*4, (width * height * 3) * 0.33 )) ;
		
		BufferedOutputStream bufOut = new BufferedOutputStream(bout, 1024*8) ;
		
		GZIPOutputStream gzOut = new GZIPOutputStream(bufOut, 1024*8) ;
		
		writeTo(gzOut) ;
		
		gzOut.close() ;
		
		return bout.toByteArray() ;
	}
	
	final public void save(File file) throws IOException {
		FileOutputStream fout = new FileOutputStream( file ) ;
		
		BufferedOutputStream bufOut = new BufferedOutputStream(fout, 1024*8) ;
		
		writeTo(bufOut) ;
		
		bufOut.close() ;
	}
	
	final public void saveCompressed(File file) throws IOException {
		FileOutputStream fout = new FileOutputStream( file ) ;
		
		BufferedOutputStream bufOut = new BufferedOutputStream(fout, 1024*8) ;
		
		GZIPOutputStream gzOut = new GZIPOutputStream(bufOut, 1024*8) ;
		
		writeTo(gzOut) ;
		
		gzOut.close() ;
	}
	
	public ImagePixels(InputStream in) throws IOException {
		readFrom(in) ;
	}
	
	public ImagePixels(InputStream in, boolean compressed) throws IOException {
		
		if (compressed) {
			GZIPInputStream gzIn = new GZIPInputStream(in, 1024*8) ;
			readFrom(gzIn) ;
			gzIn.close() ;
		}
		else {
			readFrom(in) ;	
		}
		
	}
	
	final public void readFrom(InputStream in) throws IOException {
		
		byte[] buffInt = SerializationUtils.readFull(in, 9) ;
				
		boolean yuvFormat = buffInt[0] == 1 ;
		int w = SerializationUtils.readInt(buffInt, 1) ;
		int h = SerializationUtils.readInt(buffInt, 1+4) ;
		
		byte[] c1 = readPixels(in, w, h) ;
		byte[] c2 ;
		byte[] c3 ;
		
		if (yuvFormat) {
			int widthHalf = w/2 ;
			if (w % 2 != 0) widthHalf++ ;
			
			byte[] c2Half = readPixels(in, widthHalf, h) ;
			c2 = new byte[c1.length] ;
			toDouble(c2Half, c2, w, h) ;
			
			byte[] c3Half = readPixels(in, widthHalf, h) ;
			c3 = new byte[c1.length] ;
			toDouble(c3Half, c3, w, h) ;
				
		}
		else {
			c2 = readPixels(in, w, h) ;
			c3 = readPixels(in, w, h) ;
		}
		
		this.pixelsC1 = c1 ;
		this.pixelsC2 = c2 ;
		this.pixelsC3 = c3 ;
		
		this.width = w ;
		this.height = h ;
		this.yuvFormat = yuvFormat ;
	}
	
	final public int getHoldingMemorySize() {
		int mem = this.width * this.height * 3 ;
		return mem ;
	}
	
	//////////////////////////////////////////////////////////////
	
	final public boolean[] computeDiff(ImagePixels other, double tolerance) {
		int w = this.width ;
		int h = this.height ;
		
		boolean[] diff = new boolean[w*h] ;
		
		computeDiff(other, tolerance, diff) ;
		
		return diff ;
	}
	
	final public void computeDiff(ImagePixels other, double tolerance, boolean[] diff) {
		
		int toleranceInt = (int) (tolerance * 255) ;
		
		for (int i = diff.length-1 ; i >= 0; i--) {
			
			boolean diffPixel = !YUV.isSimilar_IntegerTolerance(
									this.pixelsC1[i] & 0xFF ,
									this.pixelsC2[i] & 0xFF ,
									this.pixelsC3[i] & 0xFF ,
									other.pixelsC1[i] & 0xFF ,
									other.pixelsC2[i] & 0xFF ,
									other.pixelsC3[i] & 0xFF ,
									toleranceInt
								) ;
			
			diff[i] = diffPixel ;
			
			////////////////////////////////////////////////////////////
			
			int i2 = i-1 ;
		
			int diffSum2 = ( (this.pixelsC1[i] ^ this.pixelsC1[i2]) |
						  (this.pixelsC2[i] ^ this.pixelsC2[i2]) |
						  (this.pixelsC3[i] ^ this.pixelsC3[i2]) ) & 0x7FFFFFFF ;
			
			boolean diffPixel2 ;
			do {
				if ( diffSum2 <= 1 ) {
					int diffSumOther = ( (other.pixelsC1[i] ^ other.pixelsC1[i2]) |
									   (other.pixelsC2[i] ^ other.pixelsC2[i2]) |
									   (other.pixelsC3[i] ^ other.pixelsC3[i2]) ) & 0x7FFFFFFF ;
			
					if ( diffSumOther <= 1 ) {
						diffPixel2 = diffPixel ;
						break ;
					}
				}
			
				diffPixel2 = !YUV.isSimilar_IntegerTolerance(
						this.pixelsC1[i2] & 0xFF ,
						this.pixelsC2[i2] & 0xFF ,
						this.pixelsC3[i2] & 0xFF ,
						
						other.pixelsC1[i2] & 0xFF ,
						other.pixelsC2[i2] & 0xFF ,
						other.pixelsC3[i2] & 0xFF ,
						
						toleranceInt
				) ;
				
				
			} while(false) ;
			
			diff[i2] = diffPixel2 ;
			
			i = i2 ;
		}
		
	}
	
	final public void computeDiff_simple(ImagePixels other, double tolerance, boolean[] diff) {
		
		int toleranceInt = (int) (tolerance * 255) ;
		
		for (int i = diff.length-1 ; i >= 0; i--) {
			boolean diffPixel = !YUV.isSimilar_IntegerTolerance(
					this.pixelsC1[i] & 0xFF ,
					this.pixelsC2[i] & 0xFF ,
					this.pixelsC3[i] & 0xFF ,
					
					other.pixelsC1[i] & 0xFF ,
					other.pixelsC2[i] & 0xFF ,
					other.pixelsC3[i] & 0xFF ,
					
					toleranceInt
			) ;
			
			diff[i] = diffPixel ;
		}
		
	}
	
	
	final public boolean merge(ImagePixels other, boolean[][] mergePixels) {
		checkMutable() ;
		
		int w = this.width ;
		int h = this.height ;
		
		boolean changed = false ;
		
		for (int j = 0; j < h; j++) {
			int jIdx = j*w ;
			
			for (int i = 0; i < w; i++) {
				int iIdx = jIdx + i ;
				
				if ( mergePixels[i][j] ) {
					this.pixelsC1[iIdx] = other.pixelsC1[iIdx] ;
					this.pixelsC2[iIdx] = other.pixelsC2[iIdx] ;
					this.pixelsC3[iIdx] = other.pixelsC3[iIdx] ;
					changed = true ;
				}
				
			}
		}
		
		return changed ;
	}
	
	final public boolean mergeAndUpdate(ImagePixels other, boolean[][] mergePixels, boolean[][] changeAccumulator) {
		checkMutable() ;
		
		int w = this.width ;
		int h = this.height ;
		
		boolean changed = false ;
		
		for (int j = 0; j < h; j++) {
			int jIdx = j*w ;
			
			for (int i = 0; i < w; i++) {
				int iIdx = jIdx + i ;
				
				if ( mergePixels[i][j] ) {
					this.pixelsC1[iIdx] = other.pixelsC1[iIdx] ;
					this.pixelsC2[iIdx] = other.pixelsC2[iIdx] ;
					this.pixelsC3[iIdx] = other.pixelsC3[iIdx] ;
					changed = true ;
					
					changeAccumulator[i][j] = true ;
				}
				
			}
		}
		
		return changed ;
	}
	
	final public int[] mergeAndUpdateAndChangeCount(ImagePixels other, boolean[][] mergePixels, boolean[][] changeAccumulator) {
		checkMutable() ;
		
		int w = this.width ;
		int h = this.height ;
		
		int changeCount = 0 ;
		int changeCountAccumulated = 0 ;
		
		for (int j = 0; j < h; j++) {
			int jIdx = j*w ;
			
			for (int i = 0; i < w; i++) {
				int iIdx = jIdx + i ;
				
				if ( mergePixels[i][j] ) {
					this.pixelsC1[iIdx] = other.pixelsC1[iIdx] ;
					this.pixelsC2[iIdx] = other.pixelsC2[iIdx] ;
					this.pixelsC3[iIdx] = other.pixelsC3[iIdx] ;
					
					changeCount++ ;
					
					if (!changeAccumulator[i][j]) {
						changeAccumulator[i][j] = true ;
						changeCountAccumulated++ ;
					}
				}
				
			}
		}
		
		return new int[] { changeCount , changeCountAccumulated } ;
	}
	
	final public boolean merge(ImagePixels other, boolean[] mergePixels) {
		checkMutable() ;
		
		boolean changed = false ;
		
		for (int i = mergePixels.length-1 ; i >= 0 ; i--) {
			if ( mergePixels[i] ) {
				this.pixelsC1[i] = other.pixelsC1[i] ;
				this.pixelsC2[i] = other.pixelsC2[i] ;
				this.pixelsC3[i] = other.pixelsC3[i] ;
				changed = true ;
			}
		}
		
		return changed ;
	}
	
	final public boolean mergeAndUpdate(ImagePixels other, byte[] mergePixels, boolean[] changeAccumulator) {
		checkMutable() ;
		
		boolean change = false ;
		
		for (int i = mergePixels.length-1 ; i >= 0 ; i--) {
			byte b = mergePixels[i] ;
			if ( b != 0 ) {
				this.pixelsC1[i] = other.pixelsC1[i] ;
				this.pixelsC2[i] = other.pixelsC2[i] ;
				this.pixelsC3[i] = other.pixelsC3[i] ;
				
				if (b == 1) {
					change = true ;
					changeAccumulator[i] = true ;
				}
			}
		}
		
		return change ;
	}
	
	final public int[] mergeAndUpdateAndChangeCount(ImagePixels other, byte[] mergePixels, boolean[] changeAccumulator) {
		checkMutable() ;
		
		int changeCount = 0 ;
		int changeCountAccumulated = 0 ;
		
		for (int i = mergePixels.length-1 ; i >= 0 ; i--) {
			byte b = mergePixels[i] ;
			if ( b != 0 ) {
				this.pixelsC1[i] = other.pixelsC1[i] ;
				this.pixelsC2[i] = other.pixelsC2[i] ;
				this.pixelsC3[i] = other.pixelsC3[i] ;
				
				if (b == 1) {
					changeCount++ ;
					
					if (!changeAccumulator[i]) {
						changeAccumulator[i] = true ;
						changeCountAccumulated++ ;
					}
				}
			}
		}
		
		return new int[] { changeCount , changeCountAccumulated } ;
	}

	final public ImagePixels scale(double ratio) {
		return scale( (int)(width*ratio) , (int)(height*ratio) ) ;
	}
	
	final public ImagePixels scaleFast(double ratio) {
		return scaleFast( (int)(width*ratio) , (int)(height*ratio) ) ;
	}
	
	final public ImagePixels scaleFast1(int width, int height) {
		int width2 = width ;
		int height2 = height ;
		
		int w = this.width ;
		int h = this.height ;
		
		double wRatio = (width2*1D) / w ;
		double wRatio2 = w / (width2*1D) ;
		double hRatio = (height2*1D) / h ;
		double hRatio2 = h / (height2*1D) ;
		
		int wSteps = 1 ;
		int hSteps = 1 ;
		
		if (wRatio2 > 7) wSteps = 4 ;
		else if (wRatio2 > 4) wSteps = 3 ;
		else if (wRatio2 > 2) wSteps = 2 ;
		
		if (hRatio2 > 7) hSteps = 4 ;
		else if (hRatio2 > 4) hSteps = 3 ;
		else if (hRatio2 > 2) hSteps = 2 ;
		
		byte[] scaleY = new byte[width2*height2] ;
		byte[] scaleU = new byte[width2*height2] ;
		byte[] scaleV = new byte[width2*height2] ;
		
		short[] scaleYSum = new short[width2*height2] ;
		short[] scaleUSum = new short[width2*height2] ;
		short[] scaleVSum = new short[width2*height2] ;
		short[] scaleCount = new short[width2*height2] ;
		
		for (int j = h-1; j >= 0; j-=hSteps) {
			int jIdx = j * w ;
			
			int y = (int) (j * hRatio) ;
			int yIdx = y * width2 ;
			
			for (int i = w-1; i >= 0; i-=wSteps) {
				int iIdx = jIdx + i ;
				
				int x = (int) (i * wRatio) ;
				int xIdx = yIdx + x ;
				
				scaleYSum[xIdx] += this.pixelsC1[iIdx] & 0xFF ;
				scaleUSum[xIdx] += this.pixelsC2[iIdx] & 0xFF ;
				scaleVSum[xIdx] += this.pixelsC3[iIdx] & 0xFF ;
				scaleCount[xIdx]++ ;
			}
		}
		
		for (int i = scaleCount.length-1; i >= 0; i--) {
			int count = scaleCount[i] ;
			scaleY[i] = (byte) (scaleYSum[i] / count) ;
			scaleU[i] = (byte) (scaleUSum[i] / count) ;
			scaleV[i] = (byte) (scaleVSum[i] / count) ;
		}
		
		return new ImagePixels( scaleY , scaleU , scaleV , width2, height2, this.yuvFormat ) ;
	}
	
	final public ImagePixels scaleIfNeeded(double ratio) {
		return scaleIfNeeded( (int)(width*ratio) , (int)(height*ratio) ) ;
	}
	
	final public ImagePixels scaleIfNeeded(int width, int height) {
		if (this.width == width && this.height == height) return this ;
		
		return this.scale(width, height) ;
	}
	
	final public ImagePixels scale(int width, int height) {
		byte[] scaleY = new byte[width*height] ;
		byte[] scaleU = new byte[width*height] ;
		byte[] scaleV = new byte[width*height] ;
		
		return scale(width, height, scaleY, scaleU, scaleV) ;
	}
	
	final public ImagePixels scale(int width, int height, byte[] scaleY, byte[] scaleU, byte[] scaleV) {
		
		int w = this.width ;
		int h = this.height ;
		
		double wRatio = w / (width*1D) ;
		double hRatio = h / (height*1D) ;
		
		int scaleSz = 0 ;
		
		if ( hRatio < 1 && wRatio < 1 ) {
			scaleSz = scale_smallerRatioWH(width, height, scaleY, scaleU, scaleV, w, wRatio, hRatio, scaleSz);
		}
		else if ( hRatio < 1 ) {
			scaleSz = scale_smallerRatioH(width, height, scaleY, scaleU, scaleV, w, wRatio, hRatio, scaleSz);
		}
		else if ( wRatio < 1 ) {
			scaleSz = scale_smallerRatioW(width, height, scaleY, scaleU, scaleV, w, h, wRatio, hRatio, scaleSz);
		}
		else {
			scaleSz = scale_biggerRatioWH(width, height, scaleY, scaleU, scaleV, w, h, wRatio, hRatio, scaleSz);
		}
		
		return new ImagePixels( scaleY , scaleU , scaleV , width, height, this.yuvFormat ) ;
	}

	private int scale_biggerRatioWH(int width, int height, byte[] scaleY, byte[] scaleU, byte[] scaleV, int w, int h, double wRatio, double hRatio, int scaleSz) {
		for (int j = 0; j < height; j++) {
			int y = (int) (j*hRatio) ;
			int y2 = ((int) ((j+1)*hRatio))-1 ;
			
			if (y2 > h) y2 = h ;
			
			int totalSumY = ((y2-y)+1) ;
			
			for (int i = 0; i < width; i++) {
				int x = (int) (i*wRatio) ;
				int x2 = ((int) ((i+1)*wRatio))-1 ;
			
				if (x2 > w) x2 = w ;
				
				int Y = 0 ;
				int U = 0 ;
				int V = 0 ;
				
				int totalSum = ((x2-x)+1) * totalSumY ;
				
				for (int jS = y; jS <= y2; jS++) {
					int jSidx = jS*w ;
					
					for (int iS = x; iS <= x2; iS++) {
						int iSidx = jSidx + iS ;
						
						Y += this.pixelsC1[iSidx] & 0xFF ;
						U += this.pixelsC2[iSidx] & 0xFF ;
						V += this.pixelsC3[iSidx] & 0xFF ;
					}
				}
				
				scaleY[scaleSz] = (byte) (Y / totalSum) ;
				scaleU[scaleSz] = (byte) (U / totalSum) ;
				scaleV[scaleSz] = (byte) (V / totalSum) ;
				scaleSz++ ;
			}
		}
		return scaleSz;
	}

	private int scale_smallerRatioW(int width, int height, byte[] scaleY, byte[] scaleU, byte[] scaleV, int w, int h, double wRatio, double hRatio, int scaleSz) {
		for (int j = 0; j < height; j++) {
			int y = (int) (j*hRatio) ;
			int y2 = ((int) ((j+1)*hRatio))-1 ;
			
			if (y2 > h) y2 = h ;
			
			int totalSum = ((y2-y)+1) ;
			
			for (int i = 0; i < width; i++) {
				int x = (int) (i*wRatio) ;
				
				int Y = 0 ;
				int U = 0 ;
				int V = 0 ;
				
				for (int jS = y; jS <= y2; jS++) {
					int jSidx = jS*w ;
					
					{
						int iSidx = jSidx + x ;
						
						Y += this.pixelsC1[iSidx] & 0xFF ;
						U += this.pixelsC2[iSidx] & 0xFF ;
						V += this.pixelsC3[iSidx] & 0xFF ;
					}
				}
				
				scaleY[scaleSz] = (byte) (Y / totalSum) ;
				scaleU[scaleSz] = (byte) (U / totalSum) ;
				scaleV[scaleSz] = (byte) (V / totalSum) ;
				scaleSz++ ;
			}
		}
		return scaleSz;
	}

	private int scale_smallerRatioH(int width, int height, byte[] scaleY, byte[] scaleU, byte[] scaleV, int w, double wRatio, double hRatio, int scaleSz) {
		for (int j = 0; j < height; j++) {
			int y = (int) (j*hRatio) ;
			
			for (int i = 0; i < width; i++) {
				int x = (int) (i*wRatio) ;
				int x2 = ((int) ((i+1)*wRatio))-1 ;
			
				if (x2 > w) x2 = w ;
				
				int Y = 0 ;
				int U = 0 ;
				int V = 0 ;
				
				int totalSum = ((x2-x)+1) ;
				
				{
					int jSidx = y*w ;
					
					for (int iS = x; iS <= x2; iS++) {
						int iSidx = jSidx + iS ;
						
						Y += this.pixelsC1[iSidx] & 0xFF ;
						U += this.pixelsC2[iSidx] & 0xFF ;
						V += this.pixelsC3[iSidx] & 0xFF ;
					}
				}
				
				scaleY[scaleSz] = (byte) (Y / totalSum) ;
				scaleU[scaleSz] = (byte) (U / totalSum) ;
				scaleV[scaleSz] = (byte) (V / totalSum) ;
				scaleSz++ ;
			}
		}
		return scaleSz;
	}

	private int scale_smallerRatioWH(int width, int height, byte[] scaleY, byte[] scaleU, byte[] scaleV, int w, double wRatio, double hRatio, int scaleSz) {
		for (int j = 0; j < height; j++) {
			int y = (int) (j*hRatio) ;
			
			for (int i = 0; i < width; i++) {
				int x = (int) (i*wRatio) ;
				int x2 = ((int) ((i+1)*wRatio))-1 ;
			
				if (x2 > w) x2 = w ;
				
				int iSidx = (y*w) + x ;
				
				int Y = this.pixelsC1[iSidx] & 0xFF ;
				int U = this.pixelsC2[iSidx] & 0xFF ;
				int V = this.pixelsC3[iSidx] & 0xFF ;
				
				scaleY[scaleSz] = (byte) (Y) ;
				scaleU[scaleSz] = (byte) (U) ;
				scaleV[scaleSz] = (byte) (V) ;
				scaleSz++ ;
			}
		}
		return scaleSz;
	}
	
	final public ImagePixels scaleFast(int scaleWidth, int scaleHeight) {
		byte[] scaleY = new byte[scaleWidth*scaleHeight] ;
		byte[] scaleU = new byte[scaleWidth*scaleHeight] ;
		byte[] scaleV = new byte[scaleWidth*scaleHeight] ;
		
		return scaleFast(scaleWidth, scaleHeight, scaleY, scaleU, scaleV) ;
	}
	
	final public ImagePixels scaleFast(int scaleWidth, int scaleHeight, byte[] scaleY, byte[] scaleU, byte[] scaleV) {
		int w = this.width ;
		int h = this.height ;
		
		double wRatio = w / (scaleWidth*1D) ;
		double hRatio = h / (scaleHeight*1D) ;
		
		if ( wRatio < 1 || hRatio < 1 ) {
			return scale(scaleWidth, scaleHeight, scaleY, scaleU, scaleV) ;
		}
		else {
			return scaleFast_biggerRatio(scaleWidth, scaleHeight, scaleY, scaleU, scaleV, wRatio, hRatio);
		}
	}

	private ImagePixels scaleFast_biggerRatio(int scaleWidth, int scaleHeight, byte[] scaleY, byte[] scaleU, byte[] scaleV, double wRatio, double hRatio) {
		int w = this.width ;
		int h = this.height ;
		
		int wSteps = 1 ;
		int hSteps = 1 ;
		
		if (wRatio > 7) wSteps = 4 ;
		else if (wRatio > 4) wSteps = 3 ;
		else if (wRatio > 2) wSteps = 2 ;
		
		if (hRatio > 7) hSteps = 4 ;
		else if (hRatio > 4) hSteps = 3 ;
		else if (hRatio > 2) hSteps = 2 ;
		
		int scaleSz = 0 ;
		
		for (int j = 0; j < scaleHeight; j++) {
			int y = (int) (j*hRatio) ;
			int y2 = ((int) ((j+1)*hRatio))-1 ;
			
			if (y2 > h) y2 = h ;
			
			for (int i = 0; i < scaleWidth; i++) {
				int x = (int) (i*wRatio) ;
				int x2 = ((int) ((i+1)*wRatio))-1 ;
			
				if (x2 > w) x2 = w ;
				
				int Y = 0 ;
				int U = 0 ;
				int V = 0 ;
				
				int totalSum = 0 ;
				
				for (int jS = y; jS <= y2; jS+=hSteps) {
					int jSidx = jS*w ;
					
					for (int iS = x; iS <= x2; iS+=wSteps) {
						int iSidx = jSidx + iS ;
						
						Y += this.pixelsC1[iSidx] & 0xFF ;
						U += this.pixelsC2[iSidx] & 0xFF ;
						V += this.pixelsC3[iSidx] & 0xFF ;
						totalSum++ ;
					}
				}
				
				scaleY[scaleSz] = (byte) (Y / totalSum) ;
				scaleU[scaleSz] = (byte) (U / totalSum) ;
				scaleV[scaleSz] = (byte) (V / totalSum) ;
				scaleSz++ ;
			}
		}
		
		return new ImagePixels( scaleY , scaleU , scaleV , scaleWidth, scaleHeight, this.yuvFormat ) ;
	}
	
	final public void get(int x, int y, byte[] ret) {
		int idx = (y*width)+x ;
		
		ret[0] = pixelsC1[idx] ;
		ret[1] = pixelsC2[idx] ;
		ret[2] = pixelsC3[idx] ;
	}
	
	final public boolean isSimillarPixel(ImagePixels other, int i, int j, double tolerance) {
		int idx = (j*width)+i ;
		
		boolean similar = YUV.isSimilar(
				this.pixelsC1[idx] & 0xFF ,
				this.pixelsC2[idx] & 0xFF ,
				this.pixelsC3[idx] & 0xFF ,
				
				other.pixelsC1[idx] & 0xFF ,
				other.pixelsC2[idx] & 0xFF ,
				other.pixelsC3[idx] & 0xFF ,
				
				tolerance
		) ;
		
		return similar ;
	}
	
	final public boolean isSimillarPixel_IntegerTolerance(ImagePixels other, int i, int j, int tolerance) {
		int idx = (j*width)+i ;
		
		boolean similar = YUV.isSimilar_IntegerTolerance(
				this.pixelsC1[idx] & 0xFF ,
				this.pixelsC2[idx] & 0xFF ,
				this.pixelsC3[idx] & 0xFF ,
				
				other.pixelsC1[idx] & 0xFF ,
				other.pixelsC2[idx] & 0xFF ,
				other.pixelsC3[idx] & 0xFF ,
				
				tolerance
		) ;
		
		return similar ;
	}
	
	final public ImagePixels createSubImagePixels(int x, int y, int width, int height) {
		Rectangle bounds = new Rectangle(0,0,this.width,this.height) ;
		Rectangle sub = new Rectangle(x,y,width,height) ;
		if ( !bounds.contains(sub) ) throw new IllegalArgumentException("Sub image out of bounds: "+ bounds +" !~ "+ sub) ;
		
		byte[] subC1 = new byte[width * height] ;
		byte[] subC2 = new byte[width * height] ;
		byte[] subC3 = new byte[width * height] ;
		
		int subSz = 0 ;
		
		for (int j = 0; j < height; j++) {
			int jIdx = (y+j)*this.width ;
			int iIdx = jIdx+x ;
			
			System.arraycopy(this.pixelsC1, iIdx, subC1, subSz, width) ;
			System.arraycopy(this.pixelsC2, iIdx, subC2, subSz, width) ;
			System.arraycopy(this.pixelsC3, iIdx, subC3, subSz, width) ;
			
			subSz += width ;
		}
		
		return new ImagePixels(subC1, subC2, subC3, width, height, this.yuvFormat) ;
	}
	

	public static void colorQuantization(byte[] data, int bits) {
		int mask = -1 << bits;
	    
	    for (int i = data.length-1 ; i >= 0 ; i--) {
	    	data[i] = (byte) ( (data[i] & 0xFF) & mask);
	    }
	}
	
	final public ImagePixels createGrayscaleImage(int colorQuantizationBitz) {
		return createGrayscaleImage(colorQuantizationBitz, false) ;
	}
	
	final public ImagePixels createGrayscaleImage(int colorQuantizationBitz, boolean balanceColors) {
		ImagePixels yuvImg = isYUVFormat() ? this : copy().convertToYUV() ;
		
		byte[] c1 = yuvImg.getPixelsC1().clone() ;
		
		if (balanceColors) {
			ColorBalance.balanceColors(c1) ;
		}
		
		if (colorQuantizationBitz > 0 && colorQuantizationBitz < 8) {
			colorQuantization(c1, colorQuantizationBitz) ;
		}
		
		return new ImagePixels(c1 , getWidth() , getHeight() , true) ;
	}
	
	final public ImagePixels createImageLowColor(int colorQuantizationBitz) {
		ImagePixels img = isYUVFormat() ? this.convertToRGB() : this ;
		
		byte[] c1 = img.getPixelsC1().clone() ;
		byte[] c2 = img.getPixelsC2().clone() ;
		byte[] c3 = img.getPixelsC3().clone() ;
		
		if (colorQuantizationBitz > 0 && colorQuantizationBitz < 8) {
			colorQuantization(c1, colorQuantizationBitz) ;
			colorQuantization(c2, colorQuantizationBitz) ;
			colorQuantization(c3, colorQuantizationBitz) ;
		}
		
		return new ImagePixels(c1,c2,c3, getWidth() , getHeight() , false) ;
	}
	
}