/**
 *
 *	@author Vincent PETRELLA
 * 	Initialize the desired behaviour,
 *  Contains the main method of the program! 
 * 
 */

import java.io.IOException;

import lejos.nxt.*;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTCommConnector;
import lejos.nxt.comm.RConsole;
import lejos.nxt.remote.RemoteNXT;

import bluetooth.*;


public class RobotMain
{ 
	// Changed type from AttackerBehaviour to RobotBehaviour - Ahmed
	static AttackerBehaviour Attacker;
	static DefenderBehaviour Defender;
	
	// PLEASE VERIFY THESE PARAMETERS BEFORE TRYING TO USE/TEST/WHATEVER THE behaviour.
	public static final String  MASTER_NAME = "T05Master";
	public static final String SLAVE_NAME = "T05Slave";
	private static RemoteNXT slave;
	
	private static NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.B;
	private static UltrasonicSensor usFrontSensor = new UltrasonicSensor(SensorPort.S1);
	private static UltrasonicSensor usSideSensor = new UltrasonicSensor(SensorPort.S3);

	
	public static int initialX;
	public static int initialY;
	public static int finalX;
	public static int finalY;
	public static void main(String[] args)
	{
		SetupBehaviour();
		while(Button.readButtons() != Button.ID_ESCAPE);
		Helper.DisplayAndExit("Exiting, Thank you for exploiting me my lord", 3);
	}
	
	
	static void SetupBehaviour()
	{
		
		
		//RConsole.openUSB(20000);
		initialX = 5;
		initialY = 6;
		finalX = 0;
		finalY = 0;
		PlayerRole role = PlayerRole.ATTACKER;
		int[]  initialBeaconPosition = {1, 1};
		int[]  finalBeaconDestination = {0, 0};
		StartCorner corner = null;

//		
		BluetoothConnection conn = new BluetoothConnection();
		Transmission bluetoothTransmition = conn.getTransmission();

		if (bluetoothTransmition == null) 
		{
			LCD.drawString("Failed to read transmission", 0, 5);
		} 
		else 
		{
			corner = bluetoothTransmition.startingCorner;
			role = bluetoothTransmition.role;	//defender will go here to get the flag:
			initialX = bluetoothTransmition.fx;	//flag pos x
			initialY  = bluetoothTransmition.fy;	//flag pos y
			finalX = bluetoothTransmition.dx;	//destination pos x
			finalX = bluetoothTransmition.dy;	//destination pos y
			conn.printTransmission();
		}
		
		LCD.drawString("I X" + initialX,0,0);
		LCD.drawString("I Y" + initialY ,0,1);
		LCD.drawString("F X"+ finalX,0,2);
		LCD.drawString("FY " + + finalY,0,3);
		LCD.drawString("R" + role,0,4);
		LCD.drawString("C" + corner,0,5);
		
		
		
	
		
		 //Bluetooth brick communication
		try
		{
			NXTCommConnector connector = Bluetooth.getConnector();
			slave = new RemoteNXT(SLAVE_NAME,connector);
			LCD.drawString("Connected",0,0);
		}
		catch (IOException ioe)
		{
			Helper.DisplayAndExit("Couldnt pair to Slave", 2000);
		}
		
		// Initializing da shit for the robot.
		UltrasonicPoller usPollerFront = new UltrasonicPoller(usFrontSensor);
		usPollerFront.start();
		
		UltrasonicPoller usPollerSide = new UltrasonicPoller(usSideSensor);
		usPollerSide.start();
		
		Odometer odometer = new Odometer(corner);
		odometer.start();
		
		Navigation navigation = new Navigation(leftMotor, rightMotor, odometer, usPollerFront, usPollerSide);
		navigation.start();	
		
		ArmControl arm = new ArmControl(slave.A, Motor.C); // Slave.A , Motor.C
		arm.start();
		
		//LightSearch light = new LightSearch(odometer, navigation, new LightSensor(SensorPort.S2));

		TouchSensor touchSensor = new TouchSensor(slave.S1);

	
		LightLocalizer lsLoc = new LightLocalizer(odometer,new LightSensor(SensorPort.S4));
		
		
		try // Try to put da shit all together, Try/Catch for the slave. 
		{
			Sound.beep();
			// Added conditions for both player roles - Ahmed
			if (role == PlayerRole.ATTACKER){
				Attacker = new AttackerBehaviour(navigation, odometer, usPollerFront, arm, touchSensor,lsLoc,initialBeaconPosition,finalBeaconDestination);
				Attacker.start();
			}
			else if (role == PlayerRole.DEFENDER){
				Defender = new DefenderBehaviour(navigation, odometer, usPollerFront, arm, touchSensor,lsLoc,initialBeaconPosition,finalBeaconDestination);
				Defender.start();
			}
	
		} 
		catch (IOException e) 
		{
			System.out.println("Setup fail, probably slave issue.");
		}
		
	}
	
}
