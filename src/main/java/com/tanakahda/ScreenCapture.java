package com.tanakahda;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ScreenCapture {

	/**
	 * 
	 * @param filepath
	 * @param startPoint
	 * @param endPoint
	 * @throws AWTException
	 * @throws IOException
	 */
	public void execute(File filepath, Point startPoint, Point endPoint) 
			throws AWTException, IOException {
		var robot = new Robot();
//		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		var image = robot.createScreenCapture(
				new Rectangle(
					(int)startPoint.getX(),
					(int)startPoint.getY(),
					(int)(endPoint.getX() - startPoint.getX()),
					(int)(endPoint.getY() - startPoint.getY())));
		//PNGファイルの保存
		ImageIO.write(image, "PNG", filepath);
	}
}