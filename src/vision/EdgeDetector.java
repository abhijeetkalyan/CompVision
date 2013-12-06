
package vision;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class EdgeDetector {
	private int white;
	private int lowThreshold;
	private int highThreshold;

	//Recommended value for room images, otherwise too much noise comes in
	public EdgeDetector() {

		white = Color.WHITE.getRGB();
		lowThreshold = 130;
		highThreshold = 140;
	}

	//Detects edges in an image
	public BufferedImage edgeDetect(int[][] image) {
		int width = image.length;
		int height = image[0].length;
		BufferedImage output = new BufferedImage(width, height,
				BufferedImage.TYPE_BYTE_GRAY);
		int black = Color.BLACK.getRGB();
		int temp = Color.GRAY.getRGB();
		double[][] Gx = new double[width][height];
		double[][] Gy = new double[width][height];
		double[][] theta = new double[width][height];
		double[][] G = new double[width][height];

		// Perform sobel filtering in the X and Y directions
		for (int i = 1; i < width - 1; i++) {
			for (int j = 1; j < height - 1; j++) {
				int xConv = 0;
				xConv += image[i - 1][j - 1];
				xConv += 2 * image[i - 1][j];
				xConv += image[i - 1][j + 1];
				xConv -= image[i + 1][j - 1];
				xConv -= 2 * image[i + 1][j];
				xConv -= image[i + 1][j + 1];
				Gx[i][j] = xConv;
				
				int yConv = 0;
				yConv += image[i - 1][j - 1];
				yConv += 2 * image[i][j - 1];
				yConv += image[i + 1][j - 1];
				yConv -= image[i - 1][j + 1];
				yConv -= 2 * image[i][j + 1];
				yConv -= image[i + 1][j + 1];
				Gy[i][j] = yConv;
			}
		}

		// Calculate G and theta
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				G[i][j] = Math.hypot(Gy[i][j], Gx[i][j]);
				if (Gx[i][j] == 0) {
					theta[i][j] = Math.PI / 2.0;
				} else {
					theta[i][j] = Math.atan(Gy[i][j] / Gx[i][j]);
				}
			}
		}

		// Categorize edges by angle bands
		// TODO: modularize
		for (int i = 1; i < width - 1; i++) {
			for (int j = 1; j < height - 1; j++) {
				if ((theta[i][j] > Math.PI / 8.0)
						&& theta[i][j] <= 3.0 * Math.PI / 8.0) {
					if ((G[i][j] > G[i - 1][j - 1])
							&& (G[i][j] > G[i + 1][j + 1])) {
						if ((G[i][j] > lowThreshold)
								&& (G[i][j] < highThreshold)) {
							output.setRGB(i, j, white);
						} else if (G[i][j] > highThreshold) {
							output.setRGB(i, j, temp);
						}
					} else {
						output.setRGB(i, j, black);
					}
				} else if ((theta[i][j] > -Math.PI / 8.0)
						&& theta[i][j] <= Math.PI / 8.0) {
					if ((G[i][j] > G[i - 1][j]) && (G[i][j] > G[i + 1][j])) {
						if ((G[i][j] > lowThreshold)
								&& (G[i][j] < highThreshold)) {
							output.setRGB(i, j, white);
						} else if (G[i][j] > highThreshold) {
							output.setRGB(i, j, temp);
						}
					} else {
						output.setRGB(i, j, black);
					}
				} else if ((theta[i][j] > -3.0 * Math.PI / 8.0)
						&& theta[i][j] <= -Math.PI / 8.0) {
					if ((G[i][j] > G[i - 1][j - 1])
							&& (G[i][j] > G[i + 1][j + 1])) {
						if ((G[i][j] > lowThreshold)
								&& (G[i][j] < highThreshold)) {
							output.setRGB(i, j, white);
						} else if (G[i][j] > highThreshold) {
							output.setRGB(i, j, temp);
						}
					} else {
						output.setRGB(i, j, black);
					}
				} else if ((theta[i][j] > 3.0 * Math.PI / 8.0)
						|| (theta[i][j] <= -3.0 * Math.PI / 8.0)) {
					if ((G[i][j] > G[i][j - 1]) && (G[i][j] > G[i][j - 1])) {
						if ((G[i][j] > lowThreshold)
								&& (G[i][j] < highThreshold)) {
							output.setRGB(i, j, white);
						} else if (G[i][j] > highThreshold) {
							output.setRGB(i, j, temp);
						}
					} else {
						output.setRGB(i, j, black);
					}
				} else {
					output.setRGB(i, j, black);
				}

			}
		}
		
		//Draw lines
		for (int i = 1; i < width - 1; i++) {
			for (int j = 1; j < height - 1; j++) {
				int col = output.getRGB(i, j);
				if (col == white) {
					follow(output, i, j);
				}
			}
		}
		
		//Remove all random gray pixels. 
		//Can also be set to white for any 'mini-edges' but picks up more noise
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int col = output.getRGB(i, j);
				if (col == temp) {
					output.setRGB(i, j,black);
				}
			}
		}

		return output;
	}
	
	//Recursively draw lines
	private void follow(BufferedImage output, int x, int y) {
		for (int i = x - 1; i < x + 2; i++) {
			for (int j = y - 1; j < y + 2; j++) {
				Color col = new Color(output.getRGB(i, j));
				int colour = (col.getBlue() + col.getGreen() + col.getRed()) / 3;
				if (colour == 128) {
					output.setRGB(i, j, white);
					follow(output, i, j);
				}
			}
		}

	}

}
