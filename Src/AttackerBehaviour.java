/**
 * 
 * @author Ahmed Hanafy.
 * 
 * Attacker Behaviour class
 * Implements the strategy of the attacker.
 * 
 * 
 */


import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;

import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.TouchSensor;
import lejos.nxt.comm.RConsole;
import lejos.nxt.remote.RemoteNXT;
import lejos.nxt.*;


public class AttackerBehaviour extends Thread
{

	private Odometer odometer;
	private Navigation navigation;
	private UltrasonicPoller usFrontPoller;
	private ArmControl arm;
	private LightSearch light;
	private LightSearch searchLight;
	private TouchSensor touchSensor;
	private RemoteNXT slave;
	private boolean exitRequest;
	private int DISTANCE_TO_BEACON = 10;
	private USLocalizer usLocalizer;
	private int[] initialBeaconPosition;
	private int[] finalBeaconDestination;
	private LightLocalizer lsLoc;
	private double lightDifference = 20;
	private int previousIndex = -1;
	private int closestIndex = 0;
	private int currentIndex = 0;
	double lightValuesBottom [] ;
	double averageLightBottom;
	double maxLightBottom ;
	double maxAngleBottom;
	double overallLightAverage;

	public AttackerBehaviour(Navigation navigation, Odometer odometer,
			UltrasonicPoller USPoller, ArmControl arm,
			TouchSensor touchSensor,LightLocalizer lsLoc,int[] initialBeaconPosition,int[] finalBeaconDestination) throws IOException 
			{
		this.navigation = navigation;
		this.usFrontPoller = USPoller;
		this.odometer = odometer;
		this.arm = arm;
		this.searchLight = light;
		this.touchSensor = touchSensor;
		this.usLocalizer = new USLocalizer(odometer, USPoller);
		this.lsLoc = lsLoc;

			}

	@Override
	public void run()
	{

		Button.waitForPress(300000) ;

		// Hold arm and raise lift by 2
		arm.OpenHandTo(10);
		while(arm.isMovingHand());
		try { Thread.sleep(1000); } catch(Exception e){}
		arm.MoveLiftTo(1);
		try { Thread.sleep(1000); } catch(Exception e){}

		// Localization
		usLocalizer.doLocalization();

		LightSensor lightSensor = new LightSensor(SensorPort.S2);
		light = new LightSearch(odometer,navigation,lightSensor);

		boolean foundBeacon = false;

		if (light != null){
			while (!foundBeacon){

				// Search at bottom

				while(arm.isMovingLift());
				double lightValuesBottom [] = light.findLight();
				double averageLightBottom = lightValuesBottom[0];
				double maxLightBottom =lightValuesBottom[1];
				double maxAngleBottom = lightValuesBottom[2];
				overallLightAverage = averageLightBottom;


				if (maxLightBottom > averageLightBottom + lightDifference){

					navigation.TurnTo(maxAngleBottom );
					navigation.TravelTo(odometer.getX() + (60 * Math.sin(Math.toRadians((maxAngleBottom)))), odometer.getY() + (60 * Math.sin(Math.toRadians(maxAngleBottom))));
					while(navigation.isNavigating());
					if(foundBeacon = SearchForBeacon())
						break;
					else
						navigation.TravelTo(odometer.getX() + (30 * Math.sin(Math.toRadians((maxAngleBottom)))), odometer.getY() + (30 * Math.sin(Math.toRadians(maxAngleBottom))));


				}
				else
				{
					double[][] coordinates= {{0,0},{150,0},{150,150},{300,150},{300,300}};
					int index = closestPoint(coordinates);
					navigation.TravelTo(coordinates[index][0], coordinates[index][1]);
					while(navigation.isNavigating());
				}
			}

		}

		// Travel to desired position and drop beacon
		navigation.TravelTo(RobotMain.finalX, RobotMain.finalY);
		while(navigation.isNavigating());
		this.ReleaseBeacon();


	}

	boolean GrabBeacon(){

		while (usFrontPoller.GetDistance() > 30)
			navigation.move();

		Motor.A.stop();
		Motor.B.stop();

		arm.MoveLiftTo(0);
		while(arm.isMovingLift());
		arm.OpenHandTo(90);
		try { Thread.sleep(1000); } catch(Exception e){}
		navigation.move(40);
		arm.OpenHandTo(-90);
		try { Thread.sleep(3000); } catch(Exception e){}
		arm.MoveLiftTo(2);

		return true;

	}

	void ReleaseBeacon(){

		arm.MoveLiftTo(0);
		arm.OpenHandTo(100);
		try { Thread.sleep(3000); } catch(Exception e){}
		navigation.move(-30);
		navigation.TurnBy(180);
	}


	boolean SearchForBeacon() // and bring it back to 0,0
	{	
		boolean captured = false;

		double lightValuesBottom [] = light.findLight();
		double averageLightBottom = lightValuesBottom[0];
		double maxLightBottom =lightValuesBottom[1];
		double maxAngleBottom = lightValuesBottom[2];

		navigation.TurnTo(maxAngleBottom + 5);

		captured = GrabBeacon();
		return captured;

	}


	
	//Checks array of points for the closes point which is not the current position
	private int closestPoint(double [] [] points){
		double x,y, distance, currentX, currentY;
		double smallestDistance = 10000;

		currentIndex = closestIndex;

		for (int i = 0; i < points.length;i++){
			currentX = odometer.getX();
			currentY = odometer.getY();
			x = points[i][0];
			y = points[i][1];

			if (i!= previousIndex && i!= currentIndex){
				distance = Math.sqrt(Math.pow(y - currentY, 2) + Math.pow(x - currentX, 2));
				if (distance > 10 && distance < smallestDistance){
					smallestDistance = distance;
					closestIndex = i;
				}
			}
			System.out.println("d: " + smallestDistance+ "." + points[i][0]+"," + points[i][1]);
		}

		previousIndex = currentIndex;
		currentIndex = closestIndex;

		return closestIndex;
	}
}
