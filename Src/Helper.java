import lejos.nxt.LCD;
import lejos.util.TextMenu;

/**
 * 
 * 	@author Vincent Petrella.
 * 	Helper Class: Implements usefull static stuff.
 * 
 */

public class Helper 
{
	/**
	 *  Display some text and exits after a while (millisTime)
	 */
	public static void DisplayAndExit(String message, int millisTime)
	{
		System.out.println(message);
		long time = System.currentTimeMillis();
		while(System.currentTimeMillis() - time < millisTime );
		System.exit(0);
	}
	
	/**
	 * Display a Menu selection based on an array of choices, and a title,
	 * @return index of choice in the array.
	 */
	public static int MenuSelect(String[] choices, String title)
	{
		TextMenu menu = new TextMenu(choices);
		menu.setTitle(title);
		int selection = menu.select();
		menu.quit();
		LCD.clearDisplay();
		return selection;
	}
	
	/**
	 *	Converts the distance you want to travel in terms of wheel rotations
	 *	@return number of wheel rotations
	 */
	public static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	/**
	 *	Converts the Angle you want your robot to turn in terms of wheel rotations
	 *	Should be used that way: one wheel rotate by the output distance, the other by -the output distance
	 *	@return number of wheel rotations
	 */
	public static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	public static double adjustAngle(double angle){
		angle = angle%360;
		if (angle <0)
			
			angle = 360 + angle;
			
		return angle;
	}
	
}
