/*
Util.java
Fatima Ali
The Util class contains static utility methods that are used as helpers in the other classes. This class was made to avoid creating similar methods multiple times in different classes.
 */

import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;


public class Util{
    /* loadBuffImage():
    Return type : BufferedImage
    Parameters: String n, int width, int height
    Description: loads an image from a file and returns it as a BufferedImage with the specified width and height
     */
    public static BufferedImage loadBuffImage(String n, int width, int height){
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics bg = bi.getGraphics();
    	Image img = new ImageIcon(n).getImage();
    	bg.drawImage(img, 0, 0, width, height, null);
    	return bi;
	}

    /* is_transparent():
    Return type : boolean
    Parameters: BufferedImage img, int x, int y
    Description: checks if a pixel in the given image at the given coordinates x,y is transparent
     */

    public static boolean is_transparent(BufferedImage img, int x, int y) {
    if (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight()) return false;
        return (img.getRGB(x, y) >> 24 & 0xff) == 0;
    }

    /* pixel_collide():
    Return type : boolean
    Parameters: int x1, int y1, BufferedImage img1, int x2, int y2, BufferedImage img2
    Description: checks if two images collide at the given coordinates by checking if both the colliding pixels are opaque. This filters out the transparent pixels that are included in the rectangles of the images but shouldn't count as collisions since they are transparent.
     It does this by first determining the boundaries of the intersection of the two images, and then checking each pixel in that area to see if the alpha value is greater than 0 for both images at the same pixel, that indicates a collision.
     */
    
    public static boolean pixel_collide(int x1, int y1, BufferedImage img1, int x2, int y2, BufferedImage img2){

        //determine the boundaries of the intersection
        int left = Math.max(x1, x2);
        int top = Math.max(y1, y2);
        int right = Math.min(x1 + img1.getWidth(), x2 + img2.getWidth());
        int bottom = Math.min(y1 + img1.getHeight(), y2 + img2.getHeight());

        for (int y = top; y < bottom; y++) {
            for (int x = left; x < right; x++) {
                //get alpha of pixel in both images
                //(pixel >> 24) & 0xFF extracts the alpha channel, 0 = transparent, 255 = opaque
                //I had to google the following 2 lines but basically it checks the alpha value of the pixel in both images at the current coordinates (adjusted for the position of each image) 
                // and if both are greater than 0, it means both images are opaque at that pixel and therefore there is a collision
                int alpha1 = (img1.getRGB(x - x1, y - y1) >> 24) & 0xff;
                int alpha2 = (img2.getRGB(x - x2, y - y2) >> 24) & 0xff;

                //if both are opaque at the same pixel it means theres a collision
                if (alpha1 > 0 && alpha2 > 0) {
                    return true;
                }
            }
        }
    return false;
}
}
