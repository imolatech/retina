package com.imolatech.retina.kinect.sample;

/* Based on OpenNI's SimpleViewer example
 Initialize OpenNI with SAMPLE_XML_FILE;
 Display a grayscale depthmap (darker means further away, although black
 means "too close" for a depth value to be calculated).
 */

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.text.DecimalFormat;

import org.OpenNI.*;

import java.nio.ShortBuffer;

public class ViewerPanelV1 extends JPanel implements Runnable {
	
	private static final long serialVersionUID = 1L;

	private static final int MAX_DEPTH_SIZE = 10000;

	private static final String SAMPLE_XML_FILE = "./SamplesConfig.xml";

	// image vars
	private byte[] imgbytes;
	private BufferedImage image = null; // for displaying the depth image
	private int imWidth, imHeight;
	private float histogram[]; // for the depth values
	private int maxDepth = 0; // largest depth value

	private volatile boolean isRunning;

	// used for the average ms processing information
	private int imageCount = 0;
	private long totalTime = 0;
	private DecimalFormat df;
	private Font msgFont;

	// OpenNI
	private Context context;
	private DepthMetaData depthMD;

	public ViewerPanelV1() {
		setBackground(Color.WHITE);

		df = new DecimalFormat("0.#"); // 1 dp
		msgFont = new Font("SansSerif", Font.BOLD, 18);

		try {
			OutArg<ScriptNode> scriptNode = new OutArg<ScriptNode>();
			context = Context.createFromXmlFile(SAMPLE_XML_FILE, scriptNode);

			DepthGenerator depthGen = DepthGenerator.create(context);
			depthMD = depthGen.getMetaData();
			// use depth metadata to access depth info (avoids bug with
			// DepthGenerator)
		} catch (GeneralException e) {
			System.out.println(e);
			System.exit(1);
		}

		histogram = new float[MAX_DEPTH_SIZE];

		imWidth = depthMD.getFullXRes();
		imHeight = depthMD.getFullYRes();
		System.out.println("Image dimensions (" + imWidth + ", " + imHeight
				+ ")");

		// create empty image object of correct size and type
		imgbytes = new byte[imWidth * imHeight];
		image = new BufferedImage(imWidth, imHeight,
				BufferedImage.TYPE_BYTE_GRAY);

		new Thread(this).start(); // start updating the panel's image
	} // end of ViewerPanel()

	public Dimension getPreferredSize() {
		return new Dimension(imWidth, imHeight);
	}

	public void run()
	/*
	 * update and display the depth image whenever the context is updated.
	 */
	{
		isRunning = true;
		while (isRunning) {
			try {
				context.waitAnyUpdateAll();
			} catch (StatusException e) {
				System.out.println(e);
				System.exit(1);
			}
			long startTime = System.currentTimeMillis();
			updateDepthImage();
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
		System.exit(0);
	} // end of run()

	public void closeDown() {
		isRunning = false;
	}

	private void updateDepthImage()
	/*
	 * build a new histogram of depth grayscales. and convert it to image pixels
	 */
	{
		ShortBuffer depthBuf = depthMD.getData().createShortBuffer();
		calcHistogram(depthBuf); // convert depths to grayscales
		depthBuf.rewind();

		// store grayscale at correct (1D) position in imgbytes[] pixel array
		while (depthBuf.remaining() > 0) {
			int pos = depthBuf.position();
			short depth = depthBuf.get();
			imgbytes[pos] = (byte) histogram[depth]; // values will be 0-255
		}
	} // end of updateDepthImage()

	private void calcHistogram(ShortBuffer depthBuf)
	// convert depths to grayscales
	{
		// reset histogram[]
		for (int i = 0; i <= maxDepth; i++)
			histogram[i] = 0;

		// record number of different depths in histogram[];
		// each depth (an integer mm value) is used as an index into the array
		int numPoints = 0;
		maxDepth = 0;
		while (depthBuf.remaining() > 0) {
			short depthVal = depthBuf.get();
			if (depthVal > maxDepth)
				maxDepth = depthVal;
			if ((depthVal != 0) && (depthVal < MAX_DEPTH_SIZE)) { // skip
																	// histogram[0]
				histogram[depthVal]++;
				numPoints++;
			}
		}
		// System.out.println("No. of numPoints: " + numPoints);
		// System.out.println("Maximum depth: " + maxDepth);

		// convert into a cummulative depth count (skipping histogram[0])
		for (int i = 1; i <= maxDepth; i++)
			histogram[i] += histogram[i - 1];

		/*
		 * convert cummulative depth into grayscales (0-255) - darker means
		 * further away, although black means "too close" for a depth value to
		 * be calculated).
		 */
		if (numPoints > 0) {
			for (int i = 1; i <= maxDepth; i++)
				// skip histogram[0]
				histogram[i] = (int) (256 * (1.0f - (histogram[i] / (float) numPoints)));
		}
	} // end of calcHistogram()

	public void paintComponent(Graphics g)
	// Draw the depth image and statistics info
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		// convert image pixel array into an image
		DataBufferByte dataBuffer = new DataBufferByte(imgbytes, imWidth
				* imHeight);
		Raster raster = Raster.createPackedRaster(dataBuffer, imWidth,
				imHeight, 8, null);
		image.setData(raster);
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

