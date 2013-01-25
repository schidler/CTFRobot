/**
 * @author Vincent Petrella.
 *
 *	Well, a Point class to use points. 
 *
 */

public class Point {
	
	public double x;
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	
	public double y;
	
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	
	public Point(){
		x=0;
		y=0;
	}
	
	public Point(double a, double b){
		x=a;
		y=b;
	}
	
	public String toString()
	{
		return "x: "+this.x + " y: " + this.y;
	}
	
}
