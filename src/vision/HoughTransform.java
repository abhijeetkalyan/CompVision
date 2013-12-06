
package vision;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;

public class HoughTransform {

	public HoughTransform() {
	}

	public BufferedImage imagetoHough(BufferedImage image) {
		//Set relevant initial paameters
		int threshold = 10;
		int votingPool = 50;
		int width = image.getWidth();
		int height = image.getHeight();
		int dim = Math.max(height, width);
		int radius = (int) Math.hypot(dim, dim) / 2;
		int doubleradius = radius * 2;
		float midpointY = height / 2;
		float midpointX = width / 2;
		int[][] arr = new int[180][doubleradius];
		
		//Precompute the values of sine theta and cos theta,
		//since they are used often
		double sine[] = new double[180];
		double cos[] = new double[180];
		for (int theta = 0; theta < 180; theta++) {
			double rad = theta * Math.PI / 180;
			sine[theta] = Math.sin(rad);
			cos[theta] = Math.cos(rad);
		}
		
		//For each pixel, compute all possible r.
		//r = x cos theta + y sin theta, compute for theta from 0-180.
		//Take measurements from 0 to 2r instead of -r to r to avoid array trouble
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if ((image.getRGB(i, j) & 0x00FFFFFF) != 0) {
					for (int theta = 0; theta < 180; theta++) {
						int r = (int) (((i - midpointX) * cos[theta]) + ((j - midpointY) * sine[theta]));
						r += radius;
						arr[theta][r]++;
					}
				}
			}
		}
		
		//Store the (r,theta) pairs in a HashMap
		HashMap<Double, Integer> houghLines = new HashMap<Double, Integer>(20);

		// Perform voting on the image. Analyse the hough space pixel by pixel,
		// and for each pixel in hough space, check all pixels within an n x n
		// square
		// around it. The relevant square can be determined by votingPool.
		for (int theta = 0; theta < 180; theta++) {
			for (int r = votingPool; r < doubleradius - votingPool; r++) {
				boolean isLargest = true;
				if (arr[theta][r] > threshold) {
					int max = arr[theta][r];
					for (int i = -votingPool; i <= votingPool; i++) {
						for (int j = r - votingPool; j <= r + votingPool; j++) {
							int t = theta + i;
							t = Math.abs(t);
							t = t % 180;
							if (arr[t][j] > max) {
								isLargest = false;
								break;
							}
						}
						if (!isLargest)
							break;
					}
					if (isLargest) {
						double thetaRad = Math.toRadians(theta);
						houghLines.put(thetaRad, r);
					}
				}

			}

		}
		// Draw lines on the image. Iterate over the set
		Iterator<Double> iter = houghLines.keySet().iterator();
		while (iter.hasNext()) {
			double theta = (double) iter.next();
			int r = houghLines.get(theta);
			int t = (int) Math.toDegrees(theta);
			if (theta < Math.PI * 0.25 || theta > Math.PI * 0.75) {
				for (int i = 0; i < height; i++) {
					int a = r - radius;
					float b = i - midpointY;
					int x = (int) ((a - b * sine[t]) / cos[t]);
					x += midpointX;
					if (x < width && x >= 0)
						image.setRGB(x, i, Color.RED.getRGB());
				}
			} else {
				for (int i = 0; i < width; i++) {
					int a = r - radius;
					float b = i - midpointX;
					int y = (int) ((a - b * cos[t]) / sine[t]);
					y += midpointY;
					if (y < height && y >= 0)
						image.setRGB(i, y, Color.RED.getRGB());
				}

			}
		}

		
		return image;
	}

}
