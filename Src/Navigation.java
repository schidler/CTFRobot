/**
 * @author Vincent Petrella.
 * 
 * Main navigation Thread
 *
 */

import lejos.nxt.*;

public class Navigation extends Thread {
	private static final int FORWARD_SPEED = 150, ROTATE_SPEED = 75, TURN_TO_OBSTACLE_AVOID = 70,
							P_DISTANCE_TO_WALL = 30, FILTER_OUT_ONE = 150,  FILTER_OUT_TWO = 350, P_CLAMPED_DISTANCE = 30, P_MAGIC_CONSTANT = 5;
	private static final double ORIGINAL_ANGLE = Math.PI/2 ;
	private static final double wheelRadius = 2.75, wheelWidth = 16;
	
	private UltrasonicPoller usSensorFront, usSensorSide;
	private NXTRegulatedMotor leftMotor, rightMotor;
	private Odometer odometer;
	private int gapIndex;

	private boolean isNavigating ,isAvoidingObstacle, isOutOfThreshold , isTravelling;
	private double desiredX, desiredY;
	private double[] desiredDirection;
	private double theta;
	private double distanceToTravel;
	private double originX;
	private double originY;
	
	private boolean enableObstacleAvoidance;
	
	private int filterControl;
	private ObstacleAvoidance obstacleAvoidance;
	
	public static final int CRITICAL_DISTANCE = 10;
	
	//Constructor
	public Navigation (NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor,Odometer odometer, UltrasonicPoller USSensorFront, UltrasonicPoller USSensorSide)
	{
		this.odometer 		= 	odometer;
		this.leftMotor		= 	leftMotor;
		this.rightMotor		= 	rightMotor;		
		this.usSensorFront	=   USSensorFront;
		this.usSensorSide	=	USSensorSide;
		
		leftMotor.setSpeed(FORWARD_SPEED);		
		rightMotor.setSpeed(FORWARD_SPEED);	

		isNavigating			 	= 	false;		
		isAvoidingObstacle 			= 	false;	
		isOutOfThreshold			=   false;
		isTravelling				= 	false;

		desiredDirection = new double[2];

		filterControl 			= 	0;
		obstacleAvoidance 		=	null;
		
		enableObstacleAvoidance = true;
		gapIndex = 0;
	}

	/**
	 * 
	 * Thread Loop
	 * 
	 */
	public void run()
	{
		while(true)
		{
			if(isNavigating)
			{
				if(isTravelling)
				{	
					if((usSensorFront.GetDistance() < CRITICAL_DISTANCE) && enableObstacleAvoidance)
					{
						ObstacleAhead();
					}
					else
					{
						Travelling();
					}
				}	

				if(isAvoidingObstacle)
				{
					ObstacleFollower(usSensorSide.GetDistance());
				}
			}
		}
	}
	
	/**
	 * @param desiredX
	 * @param desiredY
	 * 
	 * Trigger the travelling process and set DesiredX, desiredY and the desiredTheta direction for the robot
	 */
	void TravelTo(double desiredX, double desiredY)
	{
		// Triggers the travelling and give infos	
		
		double[] robotPosition = new double[3];
		boolean[] update = {true,true,true};

		odometer.getPosition(robotPosition, update);

		this.desiredX = desiredX;
		this.desiredY = desiredY;

		originX = robotPosition[0];
		originY = robotPosition[1];
		

		desiredDirection[0] = desiredX - originX;
		desiredDirection[1] = desiredY - originY;

		// calculate Theta taking into account the original angle of the robot, it's current angle, and the smallest angle between 2 points (in st basis)
		// In radians.
		theta = Math.atan2(desiredDirection[1],desiredDirection[0]) - (ORIGINAL_ANGLE - Math.PI * robotPosition[2]/180) ;
		
		this.isNavigating = true;
		this.isTravelling = true;
	}
	
	/**
	 * 
	 * The actual Travelling process that stops when the distance between the desired point and the robot is less than Epsylon=1
	 * 
	 */
	void Travelling()
	{
		double[] robotPosition = new double[3];
		boolean[] update = {true,true,true};
		odometer.getPosition(robotPosition, update);

		//calculate the angle diff in radians.
		double deltaTheta = -robotPosition[2] - theta;

		LCD.drawString("dTheta "+ deltaTheta*180/Math.PI, 0, 0);
		LCD.drawString("Pos: "+ (int)robotPosition[0]+ ", "+(int)(robotPosition[1]),1,1);

		double[] distanceToPoint = new double[2];
		distanceToPoint[0] = desiredX - robotPosition[0];
		distanceToPoint[1] = desiredY - robotPosition[1];

		distanceToTravel = (desiredDirection[0] * desiredDirection[0] + desiredDirection[1] * desiredDirection[1]);
		
		//Calculate the squareMagnitude between us and our destination (dont bother taking the sqrt for optimization purposes)
		double directionSquareMagnitude = distanceToPoint[0] * distanceToPoint[0] + distanceToPoint[1] * distanceToPoint[1];

		
		double distanceTravelled = (robotPosition[0] - originX) * (robotPosition[0] - originX) + (robotPosition[1] - originY) * (robotPosition[1] - originY);
		
		//LCD.drawString("dir "+ Math.sqrt(directionSquareMagnitude),2,2);

		if(deltaTheta*180/Math.PI < 1.5 && deltaTheta*180/Math.PI > -1.5)
		{
			leftMotor.setSpeed((int)(FORWARD_SPEED));
			rightMotor.setSpeed((int)(FORWARD_SPEED-(20*deltaTheta*180/Math.PI))); // PController in theta to correct the angle on the go.

			leftMotor.forward();
			rightMotor.forward();

			if(directionSquareMagnitude < 15)
			{
				// reduces the speed proportionally to the distance left to the point (smooths it)
				leftMotor.setSpeed((int)(FORWARD_SPEED*directionSquareMagnitude/15));
				rightMotor.setSpeed((int)(FORWARD_SPEED*directionSquareMagnitude/15));

				if((directionSquareMagnitude) < 2)
				{
					//Stop the navigation until new travel infos given.
					NavigationStop();
				}
			}
			
			if(distanceToTravel - distanceTravelled < 9)
			{
				NavigationStop();
			}
		}
		else
		{
			System.out.println("TurnBy: " + deltaTheta);
			TurnBy(180/Math.PI * deltaTheta);
		}
	}
	
	/**
	 * Method implementing a wall follower to avoid the obstacle
	 * 
	 * @param distanceToWall
	 */
	void ObstacleFollower(int distanceToWall)
	{
		int filter;
		int filteredDistanceToWall = 255;
		if(gapIndex%2 == 0)
		{
			filter = FILTER_OUT_ONE;
		}
		else
		{
			filter = FILTER_OUT_TWO;
		}
		if (distanceToWall == 255 && filterControl < filter) {
			// bad value, do not set the distance var, however do increment the filter value
			filterControl ++;
		} else if (distanceToWall == 255)
		{
			filteredDistanceToWall = distanceToWall;
		} 
		else 
		{
			gapIndex++;
			filterControl = 0;
			filteredDistanceToWall = distanceToWall;
		}
		
		int delta = filteredDistanceToWall - P_DISTANCE_TO_WALL;

		leftMotor.forward();
		rightMotor.forward();
		int pCoefficient;
		if(Math.abs(delta) > P_CLAMPED_DISTANCE)
		{
			delta = (int) (Math.signum(delta) * P_CLAMPED_DISTANCE);
		}
		
		if(Math.abs(delta) < 5)
		{
			pCoefficient = Math.abs(delta);	
		}
		else 
		{
			pCoefficient  = P_MAGIC_CONSTANT;
		}
		
		//P controller with a coeff of 4.
		leftMotor.setSpeed(FORWARD_SPEED ); 
		rightMotor.setSpeed(FORWARD_SPEED + pCoefficient * delta);
		
		if(obstacleAvoidance != null)
		{
			if(obstacleAvoidance.isInThreshold(new Point(odometer.getX(), odometer.getY()), 10))
			{
				if(isOutOfThreshold)
				{
					isAvoidingObstacle = false;	
					this.TravelTo(desiredX,desiredY);
					gapIndex = 0;		
				}
			}
			else
			{
				isOutOfThreshold = true;
			}
		}

	}
	
	/**
	 *  What to do if an obstacle is detected ?
	 */
	void ObstacleAhead()
	{
		// if obstacle ahead, change navigation states
		isTravelling = false;
		isAvoidingObstacle = true;
		
		// then turn 55deg and rotate the sensor towards the wall
		TurnBy(TURN_TO_OBSTACLE_AVOID);
		obstacleAvoidance = new ObstacleAvoidance(new Point(odometer.getX(),odometer.getY()), new Point(desiredX,desiredY));
	}

	/**
	 * Implements the rotation of a specify angle "theta"
	 * @param theta
	 */
	void TurnBy(double theta)
	{
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		

		leftMotor.rotate(Helper.convertAngle(wheelRadius,wheelWidth,theta),true);
		rightMotor.rotate(-Helper.convertAngle(wheelRadius,wheelWidth,theta),false);
	}
	
	/**
	 * Implements the rotation to a specify angle "theta"
	 * @param theta
	 */
	void TurnTo(double theta)
	{
		theta = theta - Math.toDegrees(Helper.adjustAngle(odometer.getTheta()));
		
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		leftMotor.rotate(Helper.convertAngle(wheelRadius,wheelWidth,theta),true);
		rightMotor.rotate(-Helper.convertAngle(wheelRadius,wheelWidth,theta),false);
	}
	
	void move()
	{
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.forward();
		rightMotor.forward();
	}
	
	/**
	 * @author Ahmed
	 * Added move function to move by certain distance
	 * @param distance
	 */
	void move(double distance)
	{
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		
		leftMotor.rotate(Helper.convertDistance(wheelRadius,distance),true);
		rightMotor.rotate(Helper.convertDistance(wheelRadius,distance),false);
	}

	/**
	 *  Stop any operation in the thread until another TravelTo is called
	 */
	public void NavigationStop()
	{
		isNavigating = false;
		leftMotor.stop();
		rightMotor.stop();
	}
	
	/**
	 * Typical accessor
	 * @return the state of the robot: Is it navigating ?
	 */
	public boolean isNavigating()
	{
		return isNavigating;
	}
	
	void Rotate(){
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		leftMotor.rotate(Helper.convertAngle(wheelRadius,wheelWidth,theta),true);
		rightMotor.rotate(-Helper.convertAngle(wheelRadius,wheelWidth,theta),false);
	}

	/**
	 * 
	 * @return is the robot is set to obstacle avoidance
	 */
	public boolean isEnableObstacleAvoidance() 
	{
		return enableObstacleAvoidance;
	}

	/**
	 * Enables/Disables obstacle avoidance.
	 * 
	 * @param enableObstacleAvoidance
	 */
	public void EnableObstacleAvoidance(boolean enableObstacleAvoidance) {
		this.enableObstacleAvoidance = enableObstacleAvoidance;
	}
	
	
}