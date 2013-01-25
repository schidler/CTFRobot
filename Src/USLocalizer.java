/**
 * 
 * @author Ahmed Hanafy, Damien Doucet
 * 
 * Implements USLocalization
 * 
 */

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.comm.RConsole;
import bluetooth.*;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static double ROTATION_SPEED = 30;
	private int FILTER_OUT = 5;
	private int filterControl = 0;
	private Odometer odo;
	private UltrasonicPoller us;
	private LocalizationType locType;
	private static double rr = 2.7;
	private static double lr = 2.8;
	private static double w = 16;
	public static double distance;
	public static double previousDistance;
	private Navigation navigate;
	int pastDistance = 255; 
	private DataFiltering filter = new DataFiltering(6);




	public USLocalizer(Odometer odo, UltrasonicPoller us) {
		this.odo = odo;
		this.us = us;
		this.locType = locType;
	}

	public void doLocalization() {
		double [] pos = new double [3];

		double angleA = -1;
		double angleB = -1;
		double headingChange =0;
		int n = 0;
		boolean moving= true;
	
		Motor.A.setSpeed(90);
		Motor.B.setSpeed(90);
		Motor.A.forward();
		Motor.B.backward();

		previousDistance = distance;
		while(moving){

			distance = getFilteredData();
			RConsole.println("" + distance);

			LCD.drawString("T " + Math.toDegrees(odo.getTheta()), 0, 0);
			LCD.drawString("D " + distance,0,1);


			//RConsole.println(""+ distance);

			//Rising Edge
			if (distance > 200 && previousDistance > 10 && previousDistance <30 && angleA == -1){
				Sound.beep();
				// Sleep for 500 milliseconds and set distance and previous distance to 20
				//to avoid the 2nd angle being captured instantly
				angleA = Helper.adjustAngle(Math.toDegrees(odo.getTheta()));
				RConsole.println("A " + angleA);
				LCD.drawString("A " + Math.toDegrees(angleA), 0, 5);
				try {Thread.sleep(500);} catch (InterruptedException e) {}
				distance = 20;
				previousDistance = 20;

				// Falling edge
			}else if (distance > 10 && distance < 30 && previousDistance > 200 && angleB == -1){
				Sound.beep();
				// Sleep for 500 milliseconds and set distance and previous distance to 255
				//to avoid the 2nd angle being captured instantly
				angleB = Helper.adjustAngle(Math.toDegrees(odo.getTheta()));
				RConsole.println("B " + angleB);
				LCD.drawString("B " + Math.toDegrees(angleB), 0, 6);
				try {Thread.sleep(500);} catch (InterruptedException e) {}
				distance = 255;
				previousDistance = 255;
			}

			// If both angles are captured, break loop
			if (angleA !=-1 && angleB!= -1){
				moving = false;
				break;
			}

			previousDistance = distance; // Setting previous distance at the end of the loop
		}

		if (angleA > angleB){
			headingChange = 225  - ((angleA + angleB)/2); // added - 90 to both
		}else{
			headingChange = 45 - ((angleA + angleB)/2);
		}
		RConsole.println("dH " + headingChange);
		Motor.A.stop();
		Motor.B.stop();

		// Correcting odometer

		//odo.setPosition(new double [] {0.0, 0.0, odo.getTheta() + headingChange}, new boolean [] {false, false, true});

		//odo.setTheta(Helper.adjustAngle(Math.toDegrees(odo.getTheta())) + Math.toRadians(headingChange));
		odo.setTheta( Math.toRadians((Helper.adjustAngle(Math.toDegrees(odo.getTheta()) + headingChange))));

	}


	private int getFilteredData() { 

		int distance;

		// do a ping
		// us.ping();

		// wait for the ping to complete
		//	try { Thread.sleep(50); } catch (InterruptedException e) {}

		// there will be a delay here
		distance = us.GetDistance();


		if (distance == 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the filter value
			filterControl ++;
			distance = pastDistance;
		} else if (distance == 255){
			// true 255, therefore set distance to 255
			this.distance = distance;
		} else {
			// distance went below 255, therefore reset everything.
			filterControl = 0;
			this.distance = distance;
		}

		pastDistance = distance;

		// If the distance is more than 50, the value to set to 255
		//if (distance > 50) return 255

		return distance;


	}

	private  int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private  int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}


}
