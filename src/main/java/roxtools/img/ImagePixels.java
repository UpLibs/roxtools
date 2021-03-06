package roxtools.img;

import java.awt.Color;
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
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import roxtools.ArrayUtils;
import roxtools.CountTable;
import roxtools.DigestMD5;
import roxtools.ImageUtils;
import roxtools.SerializationUtils;

public class ImagePixels implements Cloneable , Serializable {
	private static final long serialVersionUID = -3570047060045369142L;

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
	
	static public byte[] toBytes(float[] ns) {
		byte[] bs = new byte[ns.length] ;
		
		for (int i = bs.length-1; i >= 0; i--) {
			bs[i] = (byte) (ns[i] * 255) ;
		}
		
		return bs ;
	}
	
	static public int clip(int i) {
		return i < 0 ? 0 : (i > 255 ? 255 : i) ;
	}

	static public int clip(int v, int min, int max) {
		if (v < min) return min ;
		if (v > max) return max ;
		return v ;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	
	protected byte[] pixelsC1 ;
	protected byte[] pixelsC2 ;
	protected byte[] pixelsC3 ;
	
	protected int width ;
	protected int height ;
	
	public ImagePixels(URL imageURL) {
		this( ImageUtils.readURL(imageURL) ) ;
	}
	
	public ImagePixels(String base64) throws IOException {
		this( ImageUtils.base64ToImage(base64) ) ;
	}
	
	public ImagePixels(byte[] data) throws IOException {
		this( ImageUtils.readImage(data) ) ;
	}

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
	
	static public byte[] createEmptyChannel(int size) {
		byte[] bs = new byte[size] ;
		return bs ;
	}
	
	public ImagePixels(int width, int height, byte c1, byte c2, byte c3, boolean yuvFormat) {
		this(
				createChannel(width*height , c1) ,
				createChannel(width*height , c2) ,
				createChannel(width*height , c3) ,
				width , height, yuvFormat
		) ;
	}
	
	public ImagePixels(int width, int height, Color color) {
		this(
				createChannel(width*height , (byte)color.getRed() ) ,
				createChannel(width*height , (byte)color.getGreen() ) ,
				createChannel(width*height , (byte)color.getBlue() ) ,
				width , height, false
		) ;
	}
	
	public ImagePixels(int width, int height, int color, boolean yuvFormat) {
		this(
				createChannel(width*height , (byte)((color >> 16) & 0xFF) ) ,
				createChannel(width*height , (byte)((color >> 8) & 0xFF) ) ,
				createChannel(width*height , (byte)(color & 0xFF) ) ,
				width , height, yuvFormat
		) ;
	}
	
	public ImagePixels(int width, int height, boolean yuvFormat) {
		this(
				createEmptyChannel(width*height) ,
				createEmptyChannel(width*height) ,
				createEmptyChannel(width*height) ,
				width , height, yuvFormat
		) ;
	}
	
	public ImagePixels(byte[] pixelsC1, int width, int height, boolean yuvFormat) {
		this(pixelsC1 , createChannel(pixelsC1.length , (byte)128) , createChannel(pixelsC1.length , (byte)128) , width , height, yuvFormat) ;
	}
	
	public ImagePixels(float[] pixelsC1, int width, int height, boolean yuvFormat) {
		this(toBytes(pixelsC1) , createChannel(pixelsC1.length , (byte)128) , createChannel(pixelsC1.length , (byte)128) , width , height, yuvFormat) ;
	}
	
	public ImagePixels(int[] pixelsC1, int[] pixelsC2, int[] pixelsC3, int width, int height, boolean yuvFormat) {
		this( toBytes(pixelsC1) , toBytes(pixelsC2) , toBytes(pixelsC3) , width , height , yuvFormat ) ;
	}
	
	public ImagePixels(float[] pixelsC1, float[] pixelsC2, float[] pixelsC3, int width, int height, boolean yuvFormat) {
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
	
	final public byte[] getPixelsChannel(int channelIndex) {
		switch (channelIndex) {
			case 1: return pixelsC1 ;
			case 2: return pixelsC2 ;
			case 3: return pixelsC3 ;
			default: throw new IllegalArgumentException("Invalid channel index: "+ channelIndex +". Indexes are from 1 to 3.") ;
		}
	}
	
	final public byte[][] getPixelsChannels() {
		return new byte[][] {
				pixelsC1 ,
				pixelsC2 ,
				pixelsC3
		};
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
	
	final public String getInternalFormat() {
		return isYUVFormat() ? "YUV" : "RGB" ;
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
	
	static private final DigestMD5 DIGEST_MD5 = new DigestMD5() ;
	
	public byte[] calcMD5() throws IOException {
		return DIGEST_MD5.calcMD5(getSerial()) ;
	}
	
	public String calcMD5Hex() throws IOException {
		return DIGEST_MD5.calcMD5Hex(getSerial()) ;
	}
	
	final public byte[] getSerial() throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
		writeTo(bout);
		return bout.toByteArray() ;
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
	

	final public int[] computeDiffValues(ImagePixels other) {
		int w = this.width ;
		int h = this.height ;
		
		int[] diff = new int[w*h] ;
		
		computeDiffValues(other, diff) ;
		
		return diff ;
	}
	
	final public void computeDiffValues(ImagePixels other, int[] diff) {
		
		for (int i = diff.length-1 ; i >= 0; i--) {
			diff[i] = YUV.getDistance(
					this.pixelsC1[i] & 0xFF ,
					this.pixelsC2[i] & 0xFF ,
					this.pixelsC3[i] & 0xFF ,
					other.pixelsC1[i] & 0xFF ,
					other.pixelsC2[i] & 0xFF ,
					other.pixelsC3[i] & 0xFF
					) ;
		}
		
	}
	

	final public int[] computeDiffValues_Simple(ImagePixels other) {
		int w = this.width ;
		int h = this.height ;
		
		int[] diff = new int[w*h] ;
		
		computeDiffValues_Simple(other, diff) ;
		
		return diff ;
	}
	
	final public void computeDiffValues_Simple(ImagePixels other, int[] diff) {
		
		for (int i = diff.length-1 ; i >= 0; i--) {
			
			int c1D = ( this.pixelsC1[i] & 0xFF ) - ( other.pixelsC1[i] & 0xFF ) ;
			int c2D = ( this.pixelsC2[i] & 0xFF ) - ( other.pixelsC2[i] & 0xFF ) ;
			int c3D = ( this.pixelsC3[i] & 0xFF ) - ( other.pixelsC3[i] & 0xFF ) ;
			
			if (c1D < 0) c1D = -c1D ;
			if (c2D < 0) c2D = -c2D ;
			if (c3D < 0) c3D = -c3D ;
			
			diff[i] = Math.max(c1D, Math.max(c2D, c3D)) ;
		}
		
	}
	
	final public boolean[] computeDiff(ImagePixels other, double tolerance) {
		int w = this.width ;
		int h = this.height ;
		
		boolean[] diff = new boolean[w*h] ;
		
		computeDiff(other, tolerance, diff) ;
		
		return diff ;
	}
	
	final public void computeDiff(ImagePixels other, double tolerance, boolean[] diff) {
		
		int toleranceInt = (int) (tolerance * 255) ;

		int length = diff.length;
		int lastIndex = length - 1;

		if (length % 2 != 0) {
			int i = lastIndex ;

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

			lastIndex-- ;
		}

		for (int i = lastIndex; i >= 0; i--) {
			
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
	
	final public boolean[] computeDiffByDensityMap(ImagePixels other, double tolerance) {
		boolean[] diff0 = computeDiff(other, tolerance) ;
		
		int[] map1 = computeDensityMap(width, height, diff0, 1, 0) ;
		boolean[] diff1 = densityMapToBooleans(map1, 4) ;
		
		int[] map2 = computeDensityMap(width, height, diff1, 1, 0) ;
		boolean[] diff2 = densityMapToBooleans(map2, 4) ;
		
		return diff2 ;
	}
	
	final public Rectangle computeDiffByDensityMapBounds(ImagePixels other, double tolerance) {
		boolean[] diff = computeDiffByDensityMap(other, tolerance) ;
		return computeBounds(width, height, diff) ;
	}
	
	static final public Rectangle computeBounds(int width, int height, boolean[] map) {
		int minY = height ;
		int maxY = 0 ;
		
		int minX = width ;
		int maxX = 0 ;

		for (int y = 0; y < height; y++) {
			int yIdx = y*width ;
			
			for (int x = 0; x < width; x++) {
				int xIdx = yIdx + x ;
				
				if ( map[xIdx] ) {
					if ( x < minX ) minX = x ;
					if ( y < minY ) minY = y ;
					if ( x > maxX ) maxX = x ;
					if ( y > maxY ) maxY = y ;
				}
			}
		}
		
		Rectangle bounds = new Rectangle(minX, minY, ((maxX-minX)+1), (maxY-minY)+1) ;
		return bounds ;
	}
	
	static final public int[] computeDensityMap(int width, int height, boolean[] diff, int radius, int minimalDensity) {
		int[] map = new int[diff.length] ;
		
		for (int y = 0; y < height; y++) {
			int yIdx = y*width ;
			
			for (int x = 0; x < width; x++) {
				int xIdx = yIdx + x ;
				
				if ( diff[xIdx] ) {
				
					for (int yShift = -radius; yShift <= radius ; yShift++) {
						for (int xShift = -radius; xShift <= radius ; xShift++) {
							int y1 = y+yShift ;
							if (y1 < 0 || y1 >= height) continue ;
							
							int x1 = x+xShift ;
							if (x1 < 0 || x1 >= width) continue ;
							
							if (xShift != 0 && yShift != 0) continue ;
							
							int idx = (y1*width)+x1 ;
							
							if ( diff[idx] ) {
								int val = map[idx] ;
								if (val == 0) {
									map[idx] = 1 ;
								}
								else {
									map[idx] = val*2 ;	
								}
									
							}
						}
					}
					
				}
			}
		}
		
		if (minimalDensity > 0) {
			for (int i = 0; i < map.length; i++) {
				int d = map[i];
				if (d < minimalDensity) {
					map[i] = 0 ;
				}
			}
		}
		
		return map ;
	}
	
	static final public boolean[] densityMapToBooleans(int[] map, int minimalDensity) {
		boolean[] bs = new boolean[map.length] ;
		
		for (int i = 0; i < bs.length; i++) {
			bs[i] = map[i] >= minimalDensity ;
		}
		
		return bs ;
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
	
	final public boolean get(int x, int y, int[] ret) {
		if (x < 0 || y < 0 || x >= width || y >= height) return false ;
		
		int idx = (y*width)+x ;
		
		ret[0] = pixelsC1[idx] & 0xff ;
		ret[1] = pixelsC2[idx] & 0xff ;
		ret[2] = pixelsC3[idx] & 0xff ;
		
		return true ;
	}
	
	final public int getC1(int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) return -1 ;
		int idx = (y*width)+x ;
		return pixelsC1[idx] & 0xff ;
	}
	
	final public int getC2(int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) return -1 ;
		int idx = (y*width)+x ;
		return pixelsC2[idx] & 0xff ;
	}
	
	final public int getC3(int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) return -1 ;
		int idx = (y*width)+x ;
		return pixelsC3[idx] & 0xff ;
	}
	
	final public boolean getAndAdd(int x, int y, int[] ret) {
		if (x < 0 || y < 0 || x >= width || y >= height) return false ;
		
		int idx = (y*width)+x ;
		
		ret[0] += pixelsC1[idx] & 0xff ;
		ret[1] += pixelsC2[idx] & 0xff ;
		ret[2] += pixelsC3[idx] & 0xff ;
		
		return true ;
	}
	
	final public boolean get(int x, int y, byte[] ret) {
		if (x < 0 || y < 0 || x >= width || y >= height) return false ;
		
		int idx = (y*width)+x ;
		
		ret[0] = pixelsC1[idx] ;
		ret[1] = pixelsC2[idx] ;
		ret[2] = pixelsC3[idx] ;
		
		return true ;
	}
	
	public int getPixelsAround(int x, int y, int[] yuv) {
		int samples = 0 ;
		
		if ( this.get(x-1, y, yuv) ) samples++ ;
		else resetTriple(yuv) ;
		
		if ( this.getAndAdd(x+1, y, yuv) ) samples++ ;
		
		if ( this.getAndAdd(x, y-1, yuv) ) samples++ ;
		if ( this.getAndAdd(x, y+1, yuv) ) samples++ ;
		
		if ( this.getAndAdd(x-1, y-1, yuv) ) samples++ ;
		if ( this.getAndAdd(x+1, y-1, yuv) ) samples++ ;
		
		if ( this.getAndAdd(x-1, y+1, yuv) ) samples++ ;
		if ( this.getAndAdd(x+1, y+1, yuv) ) samples++ ;
		
		return samples ;
	}
	
	public int getPixelsCross(int x, int y, int[] yuv) {
		int samples = 0 ;
		
		if ( this.get(x-1, y, yuv) ) samples++ ;
		else resetTriple(yuv) ;
		
		if ( this.getAndAdd(x+1, y, yuv) ) samples++ ;
		
		if ( this.getAndAdd(x, y-1, yuv) ) samples++ ;
		if ( this.getAndAdd(x, y+1, yuv) ) samples++ ;
		
		return samples ;
	}
	
	static private void resetTriple(int[] a) {
		a[0] = 0 ;
		a[1] = 0 ;
		a[2] = 0 ;
	}
	
	final public void set(int x, int y, int[] pixel) {
		int idx = (y*width)+x ;
		
		pixelsC1[idx] = (byte) pixel[0] ;
		pixelsC2[idx] = (byte) pixel[1] ;
		pixelsC3[idx] = (byte) pixel[2] ;
	}
	
	final public void set(int x, int y, byte[] pixel) {
		int idx = (y*width)+x ;
		
		pixelsC1[idx] = pixel[0] ;
		pixelsC2[idx] = pixel[1] ;
		pixelsC3[idx] = pixel[2] ;
	}
	
	final public void set(int x, int y, int c1, int c2, int c3) {
		set(x, y , (byte)c1 , (byte)c2 , (byte)c3 );
	}
	
	final public void set(int x, int y, byte c1, byte c2, byte c3) {
		int idx = (y*width)+x ;
		
		pixelsC1[idx] = c1 ;
		pixelsC2[idx] = c2 ;
		pixelsC3[idx] = c3 ;
	}
	
	final public void set(int x, int y, byte[] pixel, float alpha) {
		set(x, y, pixel[0]&0xFF, pixel[1]&0xFF, pixel[2]&0xFF, alpha);
	}
	
	final public void set(int x, int y, int[] pixel, float alpha) {
		set(x, y, pixel[0], pixel[1], pixel[2], alpha);
	}
	
	final public void set(int x, int y, int c1, int c2, int c3, float alpha) {
		int idx = (y*width)+x ;
		
		int pC1 = pixelsC1[idx] & 0xFF ;
		int pC2 = pixelsC2[idx] & 0xFF ; 
		int pC3 = pixelsC3[idx] & 0xFF ;
		
		float pAlpha = 1-alpha ;
		
		pixelsC1[idx] = (byte) ((pC1*pAlpha) + (c1*alpha)) ;
		pixelsC2[idx] = (byte) ((pC2*pAlpha) + (c2*alpha)) ;
		pixelsC3[idx] = (byte) ((pC3*pAlpha) + (c3*alpha)) ;
	}
	
	final public void setC1(int x, int y, int c1) {
		int idx = (y*width)+x ;
		pixelsC1[idx] = (byte) c1 ;
	}
	
	final public void setC2(int x, int y, int c2) {
		int idx = (y*width)+x ;
		pixelsC2[idx] = (byte) c2 ;
	}
	
	final public void setC3(int x, int y, int c3) {
		int idx = (y*width)+x ;
		pixelsC3[idx] = (byte) c3 ;
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
	
	final public boolean isSimillarPixel_LumaPrecise_IntegerTolerance(ImagePixels other, int i, int j, int tolerance) {
		int idx = (j*width)+i ;
		
		boolean similar = YUV.isSimilar_LumaPrecise_IntegerTolerance(
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
	
	static public boolean containsRectangle(Rectangle rect, ImagePixels img) {
		return containsRectangle(rect, img.getWidth() , img.getHeight()) ;
	}
	
	static public boolean containsRectangle(int x, int y, int w, int h, ImagePixels img) {
		return containsRectangle(x,y,w,h, img.getWidth() , img.getHeight()) ;
	}
	
	static public boolean containsRectangle(Rectangle rect, int imgWidth, int imgHeight) {
		return containsRectangle(rect.x, rect.y, rect.width, rect.height, imgWidth, imgHeight) ;
	}
	
	static public boolean containsRectangle(int x, int y, int w, int h, int imgWidth, int imgHeight) {
        if ((imgWidth | imgHeight | w | h) < 0) return false;
        
        if (x < 0 || y < 0) return false;
        
        w += x;
        
        if (w <= x) {
            if (imgWidth >= 0 || w > imgWidth) return false;
        } else {
            if (imgWidth >= 0 && w > imgWidth) return false;
        }
        
        h += y;
        
        if (h <= y) {
            if (imgHeight >= 0 || h > imgHeight) return false;
        } else {
            if (imgHeight >= 0 && h > imgHeight) return false;
        }
        
        return true;
	}
	
	final public ImagePixels createSubImagePixels(int x, int y, int width, int height) {
		if ( !containsRectangle(x, y, width, height, this) ) throw new IllegalArgumentException("Sub image out of bounds: "+ this.width+"x"+this.height +" !~ "+ x+" ; "+y+" ; "+width+" ; "+height) ;
		
		byte[] subC1 = new byte[width * height] ;
		byte[] subC2 = new byte[width * height] ;
		byte[] subC3 = new byte[width * height] ;
		
		return createSubImagePixelsImplem(x, y, width, height, subC1, subC2, subC3);
	}
	
	final public ImagePixels createSubImagePixels(int x, int y, int width, int height, byte[] subC1, byte[] subC2, byte[] subC3) {
		if ( !containsRectangle(x, y, width, height, this) ) throw new IllegalArgumentException("Sub image out of bounds: "+ this.width+"x"+this.height +" !~ "+ x+" ; "+y+" ; "+width+" ; "+height) ;
		
		int subLenght = width * height ;
		
		if (subC1.length != subLenght) throw new IllegalArgumentException("Invalid size of subC1: "+ subC1.length +" != "+ subLenght) ;
		if (subC2.length != subLenght) throw new IllegalArgumentException("Invalid size of subC2: "+ subC2.length +" != "+ subLenght) ;
		if (subC3.length != subLenght) throw new IllegalArgumentException("Invalid size of subC3: "+ subC3.length +" != "+ subLenght) ;
		
		return createSubImagePixelsImplem(x, y, width, height, subC1, subC2, subC3);
	}

	private ImagePixels createSubImagePixelsImplem(int x, int y, int width, int height, byte[] subC1, byte[] subC2, byte[] subC3) {
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
	
	public boolean containsImagePixels(int x, int y, ImagePixels img2) {
		if ( this.isYUVFormat() != img2.isYUVFormat() ) throw new IllegalArgumentException("Not of same format: "+ img2.getInternalFormat() +" != "+ this.getInternalFormat() ) ;

		int width = img2.getWidth() ;
		int height = img2.getHeight() ;
		
		if ( !containsRectangle(x, y, width, height, this) ) return false ;
		
		byte[] c1 = img2.getPixelsC1() ;
		byte[] c2 = img2.getPixelsC2() ;
		byte[] c3 = img2.getPixelsC3() ;
		
		int subSz = 0 ;
		
		for (int j = 0; j < height; j++) {
			int jIdx = (y+j)*this.width ;
			int iIdx = jIdx+x ;
			
			if ( !ArrayUtils.equals(this.pixelsC1, iIdx, c1, subSz, width) ) return false ;
			if ( !ArrayUtils.equals(this.pixelsC2, iIdx, c2, subSz, width) ) return false ;
			if ( !ArrayUtils.equals(this.pixelsC3, iIdx, c3, subSz, width) ) return false ;
			
			subSz += width ;
		}
		
		return true ;
	}

	public static void colorQuantization(byte[] data, int bits) {
		int mask = -1 << bits;
	    
	    for (int i = data.length-1 ; i >= 0 ; i--) {
	    	data[i] = (byte) ( (data[i] & 0xFF) & mask);
	    }
	}
	
	final public ImagePixels createGrayscaleImage() {
		return createGrayscaleImage(0, false) ;
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
	
	final public ImagePixels createBalancedImage() {
		convertToRGB() ;
		
		byte[] c1 = getPixelsC1().clone() ;
		byte[] c2 = getPixelsC2().clone() ;
		byte[] c3 = getPixelsC3().clone() ;
		
		ColorBalance.balanceColors(c1) ;
		ColorBalance.balanceColors(c2) ;
		ColorBalance.balanceColors(c3) ;
		
		return new ImagePixels(c1, c2, c3, getWidth(), getHeight(), true) ;
	}
	
	public ImagePixels shiftImage(int shiftX, int shiftY) {
		int commonPixel = this.getMostCommonPixel() ;
		return shiftImage(shiftX, shiftY, commonPixel) ;
	}

	public ImagePixels shiftImage(int shiftX, int shiftY, int commonPixel) {
		
		int w = this.getWidth() ;
		int h = this.getHeight() ;
		
		byte[] c1 = this.getPixelsC1() ;
		byte[] c2 = this.getPixelsC2() ;
		byte[] c3 = this.getPixelsC3() ;
		
		byte cmY = (byte) ((commonPixel >> 16) & 0xff) ;
		byte cmU = (byte) ((commonPixel >> 8) & 0xff) ;
		byte cmV = (byte) (commonPixel & 0xff) ;
		
		ImagePixels imgShift = new ImagePixels(w,h , commonPixel, this.yuvFormat) ;
		
		byte[] c1Shift = imgShift.getPixelsC1() ;
		byte[] c2Shift = imgShift.getPixelsC2() ;
		byte[] c3Shift = imgShift.getPixelsC3() ;
		
		for (int y = 0; y < h; y++) {
			int yIdx = y*w ;
			for (int x = 0; x < w; x++) {
				int idx = yIdx+x ;

				byte Y = c1[idx] ;
				byte U = c2[idx] ;
				byte V = c3[idx] ;
				
				if (cmY != Y || cmU != U || cmV != V) {
					int x2 = x+shiftX ;
					int y2 = y+shiftY ;
					
					if (x2 >= 0 && x2 < w && y2 >= 0 && y2 < h ) {
						int idx2 = y2*w + x2 ;
						
						c1Shift[idx2] = Y ;
						c2Shift[idx2] = U ;
						c3Shift[idx2] = V ;
					}
				}
			}
		}
	
		return imgShift ;
	}
	
	
	public int getMostCommonPixel(int[] rgb) {
		int p = getMostCommonPixel() ;
		
		rgb[0] = (p >> 16) & 0xff ;
		rgb[1] = (p >> 8) & 0xff ;
		rgb[2] = p & 0xff ;
		
		return p ;
	}
	
	public int getMostCommonPixel(byte[] rgb) {
		int p = getMostCommonPixel() ;
		
		rgb[0] = (byte) ((p >> 16) & 0xff) ;
		rgb[1] = (byte) ((p >> 8) & 0xff) ;
		rgb[2] = (byte) (p & 0xff) ;
		
		return p ;
	}

	public int getMostCommonPixel() {
		int w = this.getWidth() ;
		int h = this.getHeight() ;
		
		byte[] c1 = this.getPixelsC1() ;
		byte[] c2 = this.getPixelsC2() ;
		byte[] c3 = this.getPixelsC3() ;
		
		CountTable<Integer> histogram = new CountTable<>() ;
		
		for (int y = 0; y < h; y++) {
			int yIdx = y*2 ;
			for (int x = 0; x < w; x++) {
				int idx = yIdx+x ;

				int p1 = c1[idx] & 0xFF ;
				int p2 = c2[idx] & 0xFF ;
				int p3 = c3[idx] & 0xFF ;
				
				int yuv = (p1 << 16) | p2 << 8 | p3 ;
				
				histogram.increment(yuv) ;
			}
		}
		
		List<Integer> keys = histogram.getKeysOrdered() ;
		
		Integer commonPixel = keys.get( keys.size()-1 ) ;
		
		return commonPixel ;
	}

	public void drawImage(ImagePixels src, int dstX, int dstY) {
		
		int dW = this.getWidth() ;
		int dH = this.getHeight() ;
		
		int w = src.getWidth() ;
		int h = src.getHeight() ;
		
		byte[] pixel = new byte[3] ;
		
		int xInit = clip(dstX, 0, dW)  ;
		int xEnd = clip(dstX+w, 0, dW)  ;
		
		int yInit = clip(dstY, 0, dH)  ;
		int yEnd = clip(dstY+h, 0, dH)  ;
		
		xInit -= dstX ;
		xEnd -= dstX ;
		
		yInit -= dstY ;
		yEnd -= dstY ;
		
		for (int y = yInit; y < yEnd; y++) {
			for (int x = xInit; x < xEnd; x++) {
				int px = dstX+x ;
				int py = dstY+y ;
				src.get(x, y, pixel) ;
				this.set(px,py, pixel);
			}
		}
		
	}
	
	public void drawImage(ImagePixels src, int dstX, int dstY, byte[] ignorePixel, float alpha) {
		
		int dW = this.getWidth() ;
		int dH = this.getHeight() ;
		
		int w = src.getWidth() ;
		int h = src.getHeight() ;
		
		byte[] pixel = new byte[3] ;
		
		int xInit = clip(dstX, 0, dW)  ;
		int xEnd = clip(dstX+w, 0, dW)  ;
		
		int yInit = clip(dstY, 0, dH)  ;
		int yEnd = clip(dstY+h, 0, dH)  ;
		
		xInit -= dstX ;
		xEnd -= dstX ;
		
		yInit -= dstY ;
		yEnd -= dstY ;
		
		for (int y = yInit; y < yEnd; y++) {
			for (int x = xInit; x < xEnd; x++) {
				int px = dstX+x ;
				int py = dstY+y ;
				src.get(x, y, pixel) ;
				
				if ( Arrays.equals(pixel, ignorePixel) ) continue ;
				
				this.set(px,py, pixel, alpha);
			}
		}
		
	}
	

	@Override
	public String toString() {
		return this.getClass().getName()+"["+ width +"x"+ height +" ; "+ (yuvFormat ? "YUV":"RGB") +"]" ;
	}
	

}