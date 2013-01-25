/**
 * 
 * @author Ahmed Hanafy.
 * 
 * Defender Behaviour class
 * Implements the strategy of the defender.
 * 
 * 
 */
import java.io.IOException;

import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.TouchSensor;
import lejos.nxt.remote.RemoteNXT;


public class DefenderBehaviour extends Thread
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
	private int status;
	private double averageLight;
	private double maxLight;
	private double maxAngle;
	private int previousIndex = -1;
	private int closestIndex = 0;
	private int currentIndex = 0;


	public DefenderBehaviour(Navigation navigation, Odometer odometer,
			UltrasonicPoller USPoller, ArmControl arm,
			TouchSensor touchSensor,LightLocalizer lsLoc,int[] initialBeaconPosition,int[] finalBeaconDestination) throws IOException 
			{
		this.navigation = navigation;
		this.usFrontPoller = USPoller;
		this.odometer = odometer;
		this.arm = arm;
		this.touchSensor = touchSensor;
		this.usLocalizer = new USLocalizer(odometer, USPoller);
		this.lsLoc = lsLoc;
			}

	@Override
	public void run()
	{
		arm.OpenHandTo(10);
		while(arm.isMovingHand());
		try { Thread.sleep(1000); } catch(Exception e){}
		arm.MoveLiftTo(1);
		try { Thread.sleep(1000); } catch(Exception e){}

		// Localization


		usLocalizer.doLocalization();
		lsLoc.doLocalization();

		LightSensor lightSensor = new LightSensor(SensorPort.S2);
		light = new LightSearch(odometer,navigation,lightSensor);


		// Hold arm and raise lift by 2



		// Find average light
		double [] lightData = light.findLight();
		averageLight = lightData[0];
		boolean found = false;

		navigation.TravelTo ( ((RobotMain.initialX * 30.48) - 30) , (RobotMain.initialY*30.48) - 30);
		while(navigation.isNavigating());

		while(!found){
			// Travel to a point close to the position of the beacon

			// Do another light to accurately locate beaocn
			lightData = light.findLight();
			averageLight = lightData[0];
			maxLight = lightData[1];
			maxAngle = lightData[2];	
//			System.out.println("AVG " + averageLight);
//			System.out.println("MAX " + maxLight);
//			System.out.println("ANG " + maxAngle);

			if (maxLight > averageLight + 20){
				navigation.TurnTo(maxAngle);
				break;
			}else{
				navigation.TravelTo( ((RobotMain.initialX * 30.48) + 40) , (RobotMain.initialY*30.48) +40);
			}

		}
		GrabBeacon();
		//navigation.travelTo((RobotMain.finalX * 30.48) , (RobotMain.finalY*30.48));
		//	while(navigation.isNavigating());
		HideBeacon();
		ReleaseBeacon();
	}

	void GrabBeacon(){

		while (usFrontPoller.GetDistance() > 30)
			navigation.move();

		Motor.A.stop();
		Motor.B.stop();

		arm.MoveLiftTo(0);
		while(arm.isMovingLift());
		arm.OpenHandTo(90);
		try { Thread.sleep(1000); } catch(Exception e){}
		navigation.move(60);
		arm.OpenHandTo(-90);
		try { Thread.sleep(3000); } catch(Exception e){}
		arm.MoveLiftTo(1);

	}


	void ReleaseBeacon(){
		arm.OpenHandTo(100);
		try { Thread.sleep(3000); } catch(Exception e){}
		navigation.move(-30);
		navigation.TurnBy(180);
	}

	void HideBeacon(){

		double[][] coordinates= {{300,300},{300,0},{0,300}};
		int index = closestPoint(coordinates);
		navigation.TravelTo(coordinates[index][0] - 20, coordinates[index][1] - 20);
		while(navigation.isNavigating());
		arm.OpenHandTo(100);
		try { Thread.sleep(3000); } catch(Exception e){}
		navigation.move(-30);
		navigation.TurnBy(180);
	}

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