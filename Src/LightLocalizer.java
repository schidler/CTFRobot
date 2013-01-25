/**
 * 
 * @author Ahmed Hanafy, Damien Doucet.
 * 
 * Light localization.
 * 
 */

import lejos.nxt.Button;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.Sound;
import lejos.nxt.comm.RConsole;

public class LightLocalizer {
	private Odometer odo;
	
	private LightSensor ls;
	private double lightValue = 500; // Light Value
	public static double ROTATION_SPEED = 30;
	
	
	
	public LightLocalizer(Odometer odo, LightSensor ls) {
		this.odo = odo;
		this.ls = ls;
		// turn on the light
		ls.setFloodlight(true);
	}
	
	public void doLocalization() {
		
		// Declaring angles that will be used to calculate x and y and initilizing them all to 0
		double angle1 = 0;
		double angle2 = 0;
		double angle3 = 0;
		double angle4 = 0;
		double angleX = 0;
		double angleY = 0;
			
		// A count to make sure, all 4 grid lines are captured by the light sensor
		int line = 1;
	
		
	//	robot.setRotationSpeed(ROTATION_SPEED);		
		
		Motor.A.setSpeed(90);
		Motor.B.setSpeed(90);
		Motor.A.forward();
		Motor.B.backward();
		try {Thread.sleep(1000);} catch (InterruptedException e) {}
		
		while (true){
			if (ls.getNormalizedLightValue() < lightValue){
				
				// If the sensor detects a gridline, depending on its current heading, the angle corresponding to each
				// line is recorded to be able to calculate angleX and angleY
				// IF line reaches 5, it means all 4 lines have been captured and the loop is broken.
				
				if (line == 5)
					break;
				
				if (Helper.adjustAngle(Math.toDegrees(odo.getTheta())) >50 && Helper.adjustAngle(Math.toDegrees(odo.getTheta())) < 130 && line == 1){
					// Left line (-x line)
					angle1 =Helper.adjustAngle(Math.toDegrees (odo.getTheta())); 
					RConsole.println("A " + angle1);
					line = 2;
					Sound.beep();
				}else if (Helper.adjustAngle(Math.toDegrees(odo.getTheta())) >140 && Helper.adjustAngle(Math.toDegrees(odo.getTheta())) < 220 && line == 2){
					// Top line (y line)
					angle2 =Helper.adjustAngle(Math.toDegrees (odo.getTheta())); 
					RConsole.println("B " + angle2);
					line = 3;
					Sound.beep();
				}else if (Helper.adjustAngle(Math.toDegrees(odo.getTheta())) >230 && Helper.adjustAngle(Math.toDegrees(odo.getTheta())) < 310 && line == 3){
					// right line (x line)
					angle3 =Helper.adjustAngle(Math.toDegrees (odo.getTheta())); 
					RConsole.println("C " + angle3);
					line = 4;
					Sound.beep();
				}else if (Helper.adjustAngle(Math.toDegrees(odo.getTheta())) >320 ||  Helper.adjustAngle(Math.toDegrees(odo.getTheta())) < 40 && line == 4 ){
					// bottom line (-y line)

					angle4 =Helper.adjustAngle(Math.toDegrees (odo.getTheta())); 
					RConsole.println("D " + angle4);
					line = 5;
					Sound.beep();
					break;
				}

				try {Thread.sleep(1000);} catch (InterruptedException e) {}
				
			}
		}	
		
		// Calculating angleX and angleY
		angleY = (angle4 - angle2);
		angleX = (angle3 - angle1);
		
		if (angleX < 0)
			angleX = 360 - angleX;
		
		if (angleY < 0)
			angleY = 360 - angleY;
			
		RConsole.println("X " + angleX);
		RConsole.println("Y " + angleY);
		double gridLines []= {0,30,60,90,120,150,180,210,240,270,300};
		
		
		double currentY = odo.getY();
		double currentX = odo.getX();
		double gridDifferenceX = 0;
		double gridDifferenceY = 0;
		double minGridDifferenceX = 1000;
		double minGridDifferenceY = 1000;
		double closestGridX = 0;
		double closestGridY = 0;
		
		for (int i = 0; i<gridLines.length; i++){
			gridDifferenceX = Math.abs(currentX - gridLines[i]);
			gridDifferenceY = Math.abs(currentY - gridLines[i]);
			
			if (gridDifferenceX < minGridDifferenceX){
				minGridDifferenceX = gridDifferenceX;
				closestGridX = gridLines[i];
			}
			if (gridDifferenceY < minGridDifferenceY){
				minGridDifferenceY = gridDifferenceY;
				closestGridY = gridLines[i];
			}
		
		}
		
		
		// Calculating the correct x and y position according to angleX and Y and the distance between the center of rotation and sensor
		double newX = closestGridX + (-22 * Math.cos(Math.toRadians(angleY/2)));
		double newY = closestGridY + (-22*Math.cos(Math.toRadians(angleX/2)));
		
		// Correcting odometer
		odo.setPosition(new double [] {newX,newY, 0}, new boolean [] {true, true, false});
		
		Motor.A.stop();
		Motor.B.stop();

}
	
	
}
