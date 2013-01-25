/**
 * 
 * @author Vincent Petrella
 * Thread providing Ultrasonic sensor infos at 100hz
 * 
 */

import lejos.nxt.LCD;
import lejos.nxt.UltrasonicSensor;

public class UltrasonicPoller extends Thread
{
	
	private final int US_MAX_DISTANCE = 255; 
	
	private UltrasonicSensor usSensor;
	private int distance;
	
	public UltrasonicPoller(UltrasonicSensor us) 
	{
		this.usSensor = us;
		distance = US_MAX_DISTANCE;
	}
	
	public void run() 
	{
		while (true) 
		{
			if(usSensor == null)
			{
				System.out.println("The sensor in poller is Null");
				this.distance = 255;
			}
			else
			{
				//Collect distance data
				this.distance = usSensor.getDistance();
				LCD.clear(3);
				LCD.drawString("US: "+ distance,0,3);
				try { Thread.sleep(10); } catch(Exception e){} // setting it at 100 hz (wakes up every 10ms)
			}
		}
	}
	
	public int GetDistance()
	{
		return this.distance;
	}

}
