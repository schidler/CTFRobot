/**
 * 
 * @author Vincent Petrella.
 * 
 * Implements obstacle avoidance necessary methods, like 2D algebra and suff.
 *
 */

public class ObstacleAvoidance 
{
	boolean inThreshold;
	Point originPosition;
	
	Vector desiredHeading;
	Vector[] obstacleBasis= new Vector[2];
	

	public ObstacleAvoidance(Point chemPosition, Point desiredPosition)
	{
		inThreshold	= true;
		desiredHeading = new Vector(chemPosition, desiredPosition);
		
		originPosition = chemPosition;
		
		obstacleBasis[1] = desiredHeading.normalized();
		obstacleBasis[0] = desiredHeading.normalized().rotationed(-Math.PI/2);
	}
	
	public Point TransformPoint(Point position)
	{
		double newX = obstacleBasis[0].x * (position.x - originPosition.x) + obstacleBasis[0].y * (position.y - originPosition.y);
		double newY = obstacleBasis[1].x * position.x + obstacleBasis[1].y * position.y;
		return new Point(newX,newY);
	}
	
	public boolean isInThreshold(Point position, int threshold)
	{
		return (Math.abs(TransformPoint(position).x) < threshold);
	}
	
	public Vector[] getObstacleBasis() 
	{
		return obstacleBasis;
	}
	
	
}
