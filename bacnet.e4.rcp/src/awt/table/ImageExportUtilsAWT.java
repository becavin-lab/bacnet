package awt.table;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import bacnet.utils.FileUtils;




public class ImageExportUtilsAWT {


	/**
	 * Save SWT composite to a PNG image file
	 * @param composite the composite to save
	 * @param fileName the name of the destination file
	 */
	public static void exportSWTtoPNG(Composite composite,String fileName){

		GC gc = new GC(composite);
		final Image image = new Image(composite.getDisplay(), composite.getClientArea().width, composite.getClientArea().height);
		//gc.copyArea(image, 0, 0);
		gc.dispose();

		//		ImageLoader imageLoader = new ImageLoader(); 
		//		imageLoader.data = new ImageData[] { gc.getImageData() }; 
		//		imageLoader.save(fileName, SWT.IMAGE_PNG); // fails 
		//		Canvas canvas = new Canvas(null, null);
		//		canvas.get

	}

	/**
	 * Save SWT composite to a PNG image file and embedd it in an HTML file
	 * @param composite the composite to save
	 * @param fileName the name of the destination file
	 */
	public static void exportSWTtoHTML(Composite composite,String fileName){

		GC gc = new GC(composite);
		final Image image = new Image(composite.getDisplay(), composite.getClientArea().width, composite.getClientArea().height);
		//gc.copyArea(image, 0, 0);
		gc.dispose();

		ImageLoader imageLoader = new ImageLoader(); 
		imageLoader.data = new ImageData[] { image.getImageData() }; 
		imageLoader.save(fileName+".png", SWT.IMAGE_PNG); // fails 

		String html ="<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"ISO-8859-1\">\n<title>"+fileName+"</title>\n</head>\n<body>\n";
		html+="<img style=\"-webkit-user-select: none; cursor: zoom-in;\" src=\"file:///"+fileName+".png"+"\"";
		html+="</body>\n</html>";
		FileUtils.saveText(html, fileName);

	}

	/**
	 * Convert an AWT image to a SWT image <br>
	 * From: package org.eclipse.swt.snippets.Snippet156
	 * @param bufferedImage
	 * @return
	 */
	public static ImageData convertAWTToSWT(BufferedImage bufferedImage) {
		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel)bufferedImage.getColorModel();
			PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask());
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int rgb = bufferedImage.getRGB(x, y);
					int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF)); 
					data.setPixel(x, y, pixel);
					if (colorModel.hasAlpha()) {
						data.setAlpha(x, y, (rgb >> 24) & 0xFF);
					}
				}
			}
			return data;		
		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
			IndexColorModel colorModel = (IndexColorModel)bufferedImage.getColorModel();
			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
			}
			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return data;
		}
		return null;
	}

	public static BufferedImage resizeImage(String fileName,int newWidth,String fileNameOutput) throws IOException{
		File img = new File(fileName);
		BufferedImage image = ImageIO.read(img);
		double ratio = (double)newWidth/(double)image.getWidth();
		System.out.println(ratio+"   "+image.getHeight());
		System.out.println((int)(image.getHeight()*ratio));
		BufferedImage resizedImage = new BufferedImage(newWidth, (int)(image.getHeight()*ratio), image.getType());
		Graphics2D g = resizedImage.createGraphics();

		g.drawImage(image, 0, 0, newWidth, (int)(image.getHeight()*ratio), null);
		//g.dispose();
		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		File outputfile = new File(fileNameOutput);
		System.out.println("Resized: "+fileName);
		ImageIO.write(resizedImage, "png", outputfile);
		return resizedImage;
	}

	/**
	 * Export a Swing component (AWT) into SVG file
	 * @param component
	 * @param fileName
	 * @throws Exception
	 */
	public static void exportAWTasSVG(JComponent component,String fileName) throws Exception {
		// Get a DOMImplementation and create an XML document
		System.out.println("DOM");
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		Document document = domImpl.createDocument(null, "svg", null);

		// Create an instance of the SVG Generator
		SVGGraphics2D svgGraphics = new SVGGraphics2D(document);
		svgGraphics.getGeneratorContext().setPrecision(6);

		// draw the chart in the SVG generator
		component.paint(svgGraphics);

		// Write svg file
		boolean useCSS = true; 
		OutputStream outputStream = new FileOutputStream(new File(fileName));
		Writer out = new OutputStreamWriter(outputStream, "UTF-8");
		svgGraphics.stream(out, useCSS);						
		outputStream.flush();
		outputStream.close();
		System.out.println("SVG saved in: "+fileName);
	}
}
