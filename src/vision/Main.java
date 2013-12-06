/*Design Project 2 - Group 17
 * Computational Methods for Image and Shape Recognition
 * Abhijeet Kalyan
 * Dady Coulibaly
 * Ruofan Wu
 * Primary program logic class, containing: 
 * Utility methods
 * Depth map
 * Calls to edge and hough classes
 */
package vision;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class Main {

	public static void main(String[] args) throws IOException {
		BufferedImage dep_image = null;
		BufferedImage color_image = null;
		
		//Depth Map - Experimental
		//TODO: filename
		try {
			File file = new File("depth_image.jpg");
			dep_image = ImageIO.read(file);
			InputStream is = new BufferedInputStream(new FileInputStream(
					"depth_image.jpg"));
			dep_image = ImageIO.read(is);
		} catch (IOException e) {
		};
		int[][] depMatrix = getRGBMatrix(dep_image);
		BufferedImage depth_image = gradientSmoother(depMatrix);
		makeJet(depth_image, "depth_jet");
		
		
		try {
			ImageIO.write(depth_image, "jpg", new File("gradientSmoothed.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//TODO:filename
		try {
			File file = new File("image.jpg");
			color_image = ImageIO.read(file);
			InputStream is = new BufferedInputStream(new FileInputStream(
					"image.jpg"));
			color_image = ImageIO.read(is);
		} catch (IOException e) {
		};
		int height = color_image.getHeight();
		int width = color_image.getWidth(); 
		// Convert image to grayscale
		BufferedImage image = new BufferedImage(width, height,BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D graphics = image.createGraphics();
		graphics.drawImage(color_image, 0, 0, null);
		graphics.dispose();
		try {
			ImageIO.write(image, "jpg", new File("grayscale.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedImage bf_image = null;
		
		//Perform gaussian blur
		bf_image = bilateralGS(image);
		try {
			ImageIO.write(bf_image, "jpg", new File("gaussianBlur.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Perform edge detection
		int[][] imageMatrix = getGrayscaleMatrix(image);
		EdgeDetector ed = new EdgeDetector();
		BufferedImage edge_img = ed.edgeDetect(imageMatrix);
		try {
			ImageIO.write(edge_img, "jpg", new File("edgeDetected.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		//Perform the Hough Transform
		HoughTransform hough = new HoughTransform();
		image = hough.imagetoHough(edge_img);
		try {
			ImageIO.write(image, "jpg", new File("hough.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		makeJet(image, "hough_jet");
	}

	public static BufferedImage bilateralGS(BufferedImage image)
			throws IOException {
		BufferedImage output = null;
		float[] matrix = { 2.0f, 4.0f, 5.0f, 4.0f, 2.0f, 4.0f, 9.0f, 12.0f,
				9.0f, 4.0f, 5.0f, 12.0f, 15.0f, 12.0f, 5.0f, 4.0f, 9.0f, 12.0f,
				9.0f, 4.0f, 2.0f, 4.0f, 5.0f, 4.0f, 2.0f };

		for (int i = 0; i < 25; i++) {
			matrix[i] = matrix[i] / 159.0f;
		}

		BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix));
		output = op.filter(image, output);
		return output;
	}

	
	//Standalone sobel filter. May be useful if full Canny edge detection 
	//is not required
	public static BufferedImage sobelFilter(BufferedImage image) throws IOException {
		BufferedImage output = null;
		float[] matrix = { -1.0f, 0.0f, 1.0f, -2.0f, 0.0f, 2.0f, -1.0f, 0.0f,
				2.0f, };

		BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix));
		output = op.filter(image, output);

		float[] matrix2 = { 1.0f, 2.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, -2.0f,
				-1.0f, };

		BufferedImageOp op2 = new ConvolveOp(new Kernel(3, 3, matrix2));
		output = op2.filter(image, output);

		return output;
	}
	//Returns the image as a matrix of values using RGB averaging
	public static int[][] getRGBMatrix(BufferedImage image) {
		int height = image.getHeight();
		int width = image.getWidth();
		int[][] matrix = new int[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				 Color color = new Color(image.getRGB(i, j));
				 int avg = (color.getRed() + color.getGreen() +
				 color.getBlue()) / 3;
				matrix[i][j] = avg;
			}
		}

		return matrix;
	}

	// Returns the image as a matrix of values
	public static int[][] getGrayscaleMatrix(BufferedImage image) {
		int height = image.getHeight();
		int width = image.getWidth();
		int[][] matrix = new int[width][height];
		int[] tmp = new int[1];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int avg = image.getRaster().getPixel(i, j, tmp)[0];
				matrix[i][j] = avg;
			}
		}

		return matrix;
	}

	// Given a matrix of values from 0-255, returns a BufferedImage
	// representation
	public static BufferedImage drawImage(int[][] matrix) {
		int width = matrix.length;
		int height = matrix[0].length;
		BufferedImage output = new BufferedImage(width, height,
				BufferedImage.TYPE_BYTE_GRAY);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int col = matrix[i][j];
				int pixel = new Color(col, col, col).getRGB();
				output.setRGB(i, j, pixel);
			}
		}
		return output;
	}

	// returns a text file with the image's pixel data
	public static void makeJet(BufferedImage image, String filename) {
		int[][] matrix = getRGBMatrix(image);
		int width = matrix.length;
		int height = matrix[0].length;

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename
					+ ".txt"));
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					out.write(matrix[i][j] + " ");
				}
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
		}

	}

	// Approximates a depth map on the image. Specialized for images of
	// hallways, provides
	// extra depth on the basis of perspective spiraling into the center. Local
	// depth
	// on the basis of colour is also analysed.
	public static BufferedImage gradientSmoother(int[][] image)
			throws IOException {
		int width = image.length;
		int height = image[0].length;
		BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		int midX = width / 2;
		int midY = height / 2;
		long average = 0;
		int count = 0;
		long temp1 = 0;
		long stdev = 0;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				count++;
				average += image[i][j];

			}
		}
		average = average/count;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				temp1 = image[i][j];
				stdev += (average - temp1) * (average - temp1);

			}
		}
		stdev = stdev / count;
		stdev = (long) Math.sqrt(stdev);
		int black = new Color(0, 0, 0).getRGB();
		int dark = new Color(70, 70, 70).getRGB();
		int white = new Color(245, 245, 245).getRGB();
		int gray = new Color(128, 128, 128).getRGB();
		int light = new Color(190, 190, 190).getRGB();
		long upperbound = (long) (average + (stdev * 1.28));
		long highmidbound = (long) (average + (stdev * 1.10));
		long lowmidbound = (long) (average + (stdev * 0.90));
		long lowerbound = (long) (average - (stdev * 0.75));
		long tempcol = 0;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				tempcol = image[i][j];
				double ratio = Math.hypot((double) (Math.abs(i - midX)),
						(double) (Math.abs(j - midY)));
				tempcol = tempcol + (int) (ratio * 0.2);
				if (tempcol > upperbound) {
					output.setRGB(i, j, white);
				} else if ((tempcol >= highmidbound) && (tempcol < upperbound)) {
					output.setRGB(i, j, light);
				} else if ((tempcol >= lowmidbound) && (tempcol < highmidbound)) {
					output.setRGB(i, j, gray);
				} else if ((tempcol >= lowerbound) && (tempcol < lowmidbound)) {
					output.setRGB(i, j, dark);
				} else if (tempcol < lowerbound) {
					output.setRGB(i, j, black);
				}

			}
		}

		return output;
	}

}
