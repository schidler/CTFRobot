/**
 * @author Vincent Petrella.
 * 
 * Implementation of Arm Controller
 * Implemented as a thread.
 * 
 */
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.RegulatedMotor;

public class ArmControl extends Thread
{

	private final double 	COMPLETE_CYCLE_WIRE_LENGTH = 5.0; //in CM
	private final double 	COMPLETE_CYCLE = 360; //in degrees
	private final double 	LIFT_THRESHOLD = 0.5;
	private final double	HAND_THRESHOLD = 2;
	private final int		LIFT_SPEED = 100;
	private final int 		HAND_SPEED = 250;

	private double liftPosition;
	public double handClosure;
	private double desiredLiftPosition;
	public double desiredHandClosure;
	private double previousLiftTachoCount;
	private double previousHandTachoCount;
	private double desiredDistance;
	private double liftOrigin;
	private RegulatedMotor liftMotor; // NXTRegulated instead of Regulated just foe testing purposes - Ahmed
	private NXTRegulatedMotor handMotor;

	// States
	private boolean isMovingLift;
	private boolean isMovingHand;

	 // NXTRegulated instead of Regulated just foe testing purposes - Ahmed
	public ArmControl(RegulatedMotor liftMotor,NXTRegulatedMotor handMotor)
	{
		this.liftMotor = liftMotor;
		this.handMotor = handMotor;

		liftMotor.setSpeed(LIFT_SPEED);
		handMotor.setSpeed(HAND_SPEED);
		handMotor.stop();

		liftPosition	= 0;
		handClosure		= 0;
		desiredLiftPosition = 0;
		desiredHandClosure = 0;
		liftOrigin = 0;

		isMovingLift = false;
		isMovingHand = false;
	}

	/**
	 * Thread Loop.
	 */
	public void run()
	{
		while(true)
		{
			int distanceTravelled = (int) Math.round((liftPosition - liftOrigin));
			if(isMovingLift)
			{	

				if(Math.abs(desiredDistance) - Math.abs(distanceTravelled) < 0 )
				{
					isMovingLift = false;
					liftMotor.stop();
				}
				
				UpdateLiftPosition();
				previousLiftTachoCount = liftMotor.getTachoCount();
				
				System.out.println("DTo: "+(Math.abs(desiredDistance) - Math.abs(distanceTravelled))+ " DTr: "+ (int) distanceTravelled);
				System.out.println("LiftP: "+liftPosition);
			}

			try 
			{
				Thread.sleep(50);
			}
			catch (InterruptedException e) 
			{}
			UpdateHandClosure();
			previousHandTachoCount = handMotor.getTachoCount();
		}
	}


	public boolean isMovingLift() 
	{
		return isMovingLift;
	}

	public boolean isMovingHand()
	{
		return isMovingHand;
	}

	/**
	 * 
	 * Moves lift to a specific position.
	 * In Centimeter.
	 * 
	 * @param desiredPosition
	 */
	public void MoveLiftTo(int desiredPosition)
	{
		liftOrigin = liftPosition;
		desiredLiftPosition = desiredPosition;
		desiredDistance = desiredPosition - liftOrigin;
		isMovingLift = true;
		
		int deltaPosition = (int) (desiredLiftPosition - liftPosition);
		if( deltaPosition > 0 )
		{
			liftMotor.forward();
		}
		if( deltaPosition < 0 )
		{
			liftMotor.backward();
		}
	}

	private void UpdateLiftPosition()
	{
		double currentLiftTachoCount = liftMotor.getTachoCount();
		liftPosition += ((currentLiftTachoCount - previousLiftTachoCount) / COMPLETE_CYCLE) * COMPLETE_CYCLE_WIRE_LENGTH;
	}

	private void UpdateHandClosure()
	{
		int currentHandTachoCount = handMotor.getTachoCount();
		handClosure -= (currentHandTachoCount - previousHandTachoCount);;
	}

	/**
	 * Opens hand to a precise degree
	 * 
	 * @param degree
	 */
	public void OpenHandTo(int degree)
	{
		desiredHandClosure = degree;
		double deltaClosure = desiredHandClosure - handClosure;
		handMotor.rotate(-(int)deltaClosure,false);
	}
	
	/**
	 * 
	 * Hold the arm to its current position
	 * 
	 */
	public void HoldArm()
	{
		liftMotor.setSpeed(0);
		liftMotor.stop();
	}
}