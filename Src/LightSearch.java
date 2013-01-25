/**
 * @author Ahmed Hanafy.
 * 
 * Implements LightSearch.
 * 
 */
import lejos.nxt.*;
import lejos.nxt.comm.RConsole;
public class LightSearch {

	private static LightSensor LSensor;
	private static Odometer odo;
	private static Navigation navigation;
	
	
	public LightSearch (Odometer odo, Navigation navigation, LightSensor sensor){
		this.odo = odo;
		this.LSensor = sensor;
		this.LSensor.setFloodlight(false);
		this.navigation = navigation;
	}
	
	
	
	public static double[] findLight(){
		
		
		double startingAngle =Helper.adjustAngle( Math.toDegrees(odo.getTheta()));
		double maxLight = LSensor.getNormalizedLightValue();
		double maxAngle = Helper.adjustAngle(Math.toDegrees(odo.getTheta()));
		double lightValue;
		
		Motor.A.setSpeed(90);
		Motor.B.setSpeed(90);
		Motor.A.forward();
		Motor.B.backward();
		int count = 0;
		double averageLight= 0;
		// Sleep for 0.5 seconds so that the loop is not broken instantly
		
		
		// Rotating the robot 360 degrees and finding max light value
		//if current value > max then the max is set to the current value
		// And the heading at that point is captured.
		
		//while (Math.abs(startingAngle - Helper.adjustAngle(Math.toDegrees(odo.getTheta()))) >1 )
		
		Motor.A.setSpeed(75);
		Motor.B.setSpeed(75);
		

		Motor.A.rotate(Helper.convertAngle(2.75,15.5,360),true);
		Motor.B.rotate(-Helper.convertAngle(2.75,15.5,360),true);
		try { Thread.sleep(500); } catch (InterruptedException e) {}
		
		while (Motor.A.isMoving() || Motor.B.isMoving())
		{
			lightValue = LSensor.getNormalizedLightValue();
			System.out.println("L " + lightValue);
			averageLight = averageLight + lightValue;
			count++;
			if (lightValue > maxLight)
			{
				maxLight = lightValue;
				maxAngle = Helper.adjustAngle((180/Math.PI) * odo.getTheta());
			}
		}
		averageLight = averageLight/count;
		
		Motor.A.stop();
		Motor.B.stop();
		
		double [] data = new double[3];
		data[0] = averageLight;
		data[1] = maxLight;
		data[2] = maxAngle;
		

		return data;
		
	}
}
