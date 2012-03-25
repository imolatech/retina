package com.imolatech.retina.kinect.sample;

/* Based on OpenNI's SimpleViewer example
 Initialize OpenNI *without* using an XML file;
 Display a colour Kinect webcam image.
 */

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.text.DecimalFormat;
import java.io.*;
import javax.imageio.*;
import java.util.*;

import org.OpenNI.*;

import java.nio.ByteBuffer;

public class ViewerPanelV3 extends JPanel implements Runnable {
	private BufferedImage image = null;
	private int imWidth, imHeight;

	private volatile boolean isRunning;

	// used for the average ms processing information
	private int imageCount = 0;
	private long totalTime = 0;
	private DecimalFormat df;
	private Font msgFont;

	// OpenNI
	private Context context;
	private ImageGenerator imageGen;

	public ViewerPanelV3() {
		setBackground(Color.WHITE);

		df = new DecimalFormat("0.#"); // 1 dp
		msgFont = new Font("SansSerif", Font.BOLD, 18);

		configOpenNI();

		System.out.println("Image dimensions (" + imWidth + ", " + imHeight
				+ ")");

		new Thread(this).start(); // start updating the panel's image
	} // end of ViewerPanel()

	private void configOpenNI()
	// create context and image generator
	{
		try {
			context = new Context();

			// add the NITE Licence
			License licence = new License("PrimeSense",
					"0KOIk2JeIBYClPWVnMoRKn5cdY4="); // vendor, key
			context.addLicense(licence);

			imageGen = ImageGenerator.create(context);

			MapOutputMode mapMode = new MapOutputMode(640, 480, 30); // xRes,
																		// yRes,
																		// FPS
			imageGen.setMapOutputMode(mapMode);
			imageGen.setPixelFormat(PixelFormat.RGB24);

			// set Mirror mode for all
			context.setGlobalMirror(true);

			context.startGeneratingAll();
			System.out.println("Started context generating...");

			ImageMetaData imageMD = imageGen.getMetaData();
			imWidth = imageMD.getFullXRes();
			imHeight = imageMD.getFullYRes();
		} catch (Exception e) {
			System.out.println(e);
			System.exit(1);
		}
	} // end of configOpenNI()

	public Dimension getPreferredSize() {
		return new Dimension(imWidth, imHeight);
	}

	public void run()
	/*
	 * update and display the webcam image whenever the context is updated.
	 */
	{
		isRunning = true;
		while (isRunning) {
			try {
				// context.waitAnyUpdateAll();
				context.waitOneUpdateAll(imageGen);
				// wait for 'one' ('any' is safer)
			} catch (StatusException e) {
				System.out.println(e);
				System.exit(1);
			}
			long startTime = System.currentTimeMillis();
			updateImage();
			imageCount++;
			totalTime += (System.currentTimeMillis() - startTime);
			repaint();
		}

		// close down
		try {
			context.stopGeneratingAll();
		} catch (StatusException e) {
		}
		context.release();
		System.exit(1);
	} // end of run()

	public void closeDown() {
		isRunning = false;
	}

	private void updateImage()
	// get image data as bytes; convert to an image
	{
		try {
			ByteBuffer imageBB = imageGen.getImageMap().createByteBuffer();
			image = bufToImage(imageBB);
		} catch (GeneralException e) {
			System.out.println(e);
		}
	} // end of updateImage()

	private BufferedImage bufToImage(ByteBuffer pixelsRGB)
	/*
	 * Transform the ByteBuffer of pixel data into a BufferedImage Converts RGB
	 * bytes to ARGB ints with no transparency.
	 */
	{
		int[] pixelInts = new int[imWidth * imHeight];

		int rowStart = 0;
		// rowStart will index the first byte (red) in each row;
		// starts with first row, and moves down

		int bbIdx; // index into ByteBuffer
		int i = 0; // index into pixels int[]
		int rowLen = imWidth * 3; // number of bytes in each row
		for (int row = 0; row < imHeight; row++) {
			bbIdx = rowStart;
			// System.out.println("bbIdx: " + bbIdx);
			for (int col = 0; col < imWidth; col++) {
				int pixR = pixelsRGB.get(bbIdx++);
				int pixG = pixelsRGB.get(bbIdx++);
				int pixB = pixelsRGB.get(bbIdx++);
				pixelInts[i++] = 0xFF000000 | ((pixR & 0xFF) << 16)
						| ((pixG & 0xFF) << 8) | (pixB & 0xFF);
			}
			rowStart += rowLen; // move to next row
		}

		// create a BufferedImage from the pixel data
		BufferedImage im = new BufferedImage(imWidth, imHeight,
				BufferedImage.TYPE_INT_ARGB);
		im.setRGB(0, 0, imWidth, imHeight, pixelInts, 0, imWidth);
		return im;
	} // end of bufToImage()

	public void paintComponent(Graphics g)
	// Draw the depth image and statistics info
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		if (image != null)
			g2.drawImage(image, 0, 0, this);

		writeStats(g2);
	} // end of paintComponent()

	private void writeStats(Graphics2D g2)
	/*
	 * write statistics in bottom-left corner, or "Loading" at start time
	 */
	{
		g2.setColor(Color.BLUE);
		g2.setFont(msgFont);
		int panelHeight = getHeight();
		if (imageCount > 0) {
			double avgGrabTime = (double) totalTime / imageCount;
			g2.drawString("Pic " + imageCount + "  " + df.format(avgGrabTime)
					+ " ms", 5, panelHeight - 10); // bottom left
		} else
			// no image yet
			g2.drawString("Loading...", 5, panelHeight - 10);
	} // end of writeStats()

} // end of ViewerPanel class
