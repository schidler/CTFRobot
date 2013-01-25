/**
 * 
 *	@author Lab2 TAs, Vincent Petrella, Reda Berrada, Ahmed Hanafy.
 *  Basically from lab2 except the fix on X and Y so that it fit with the referential of the drawing on the lab presentation.
 * 	Vincent,Reda : Actual odometer
 *  Ahmed: Implements BT connection and start corner.
 * 
 */

import lejos.nxt.Motor;
import bluetooth.*;

public class Odometer extends Thread {
	// robot position
	private double x, y, theta;

	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;
	private StartCorner corner;
	// lock object for mutual exclusion
	private Object lock;

	// default constructor
	public Odometer(StartCorner corner) {
		x = 0.0;
		y = 0.0;
		theta = 0.0;
		lock = new Object();
	x = corner.getX() * 30.48;
	y = corner.getY() * 30.48;
		
		if (corner.getX() == 0 && corner.getY() == 0)
			theta = 0;
		else if (corner.getX() == 12 && corner.getY() == 0)
			theta = -1/2 * Math.PI;
		else if (corner.getX() == 12 && corner.getY() == 12)
			theta = -Math.PI;
		else if (corner.getX() == 0 && corner.getY() == 12)
			theta =  Math.PI/2;
			
		
		
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;
		
		double wheelRadius= 2.75;
		double wheelDistance= 15.5;
		double previousTachoCountA = 0;
		double previousTachoCountB = 0;
		while (true) {
			updateStart = System.currentTimeMillis();
			
			synchronized (lock) {
				
				// compute the delta TachoCount (IN RADIANS !!)
				double deltaTachoCountA = Motor.A.getTachoCount()*Math.PI/180 - previousTachoCountA;
				double deltaTachoCountB = Motor.B.getTachoCount()*Math.PI/180 - previousTachoCountB;

				// memorize the previous Count (IN RADIANS!!)
				previousTachoCountA= Motor.A.getTachoCount()*Math.PI/180;
				previousTachoCountB= Motor.B.getTachoCount()*Math.PI/180;

				//Compute deltaTheta and deltaC
				double deltaTheta=(deltaTachoCountB*wheelRadius-deltaTachoCountA*wheelRadius)/(wheelDistance);
				double deltaC= (deltaTachoCountB*wheelRadius+deltaTachoCountA*wheelRadius)/2.0;

				// update x and y usind DeltaTheta and deltaC
				// changes made so that the robot is assumed to be in the direction of increasing Y, with increasing X at it's right.
				x+=deltaC*Math.sin((deltaTheta/2)+theta);
				y+=deltaC*Math.cos((deltaTheta/2)+theta);
				
				
				// update theta
				theta-=deltaTheta;
				System.out.println((int)x+" "+(int)y+ " "+ (int) 180/Math.PI * theta);
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expec	ted that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}
	
}