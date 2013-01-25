/**
 * @author Vincent Petrella.
 *
 *	Well, a Vector class to do Vector stuffs ! 
 *
 */
public class Vector {

		public double x;
		public double y;
		public Point origin;

		
		public Vector(double x, double y){
			this.x = x;
			this.y = y;
			origin = new Point();
		}
		
		public Vector(){
			this.x = 0;
			this.y = 0;
			origin = new Point();
		 }
		
		public Vector(Point A, Point B){
			this.origin = A;
			x = B.getX() - A.getX();
			y = B.getY() - A.getY();
		}
		
		public Vector(Point B){
			this.origin = new Point();
			this.x = B.x;
			this.y = B.y;
		}
		
		public double getLength(){
			return Math.sqrt((this.x*this.x)+(this.y*this.y));
		}

		public void rotation(double argument){
			double a=x;
			double b=y;
			this.x= (double) (a*Math.cos(argument) + b*Math.sin(argument));
			this.y= (double) ((-1)*a*Math.sin(argument) + b*Math.cos(argument));
		}
		
		public Vector rotationed(double argument){
			double a=x;
			double b=y;
			double newX = (double) (a*Math.cos(argument) + b*Math.sin(argument));
			double newY = (double) ((-1)*a*Math.sin(argument) + b*Math.cos(argument));
			
			Point newB = new Point( this.origin.x + newX, this.origin.y + newY);
			
			return new Vector(this.origin,newB);
			
		}
		
		public String toString(){
			return "("+x+","+y+",length: "+this.getLength()+")";
		}
		
		public Vector normalized()
		{
			double length = (double) Math.sqrt(this.x * this.x + this.y * this.y);
			double newX =  x / length;
			double newY =  y / length;
			Point newB = new Point( this.origin.x + newX, this.origin.y + newY);
			
			return new Vector(this.origin,newB);
		}
		
		
//		Was Used for testing purpose. No opengl method anymore :D
		
//		public void draw(){
//			GL11.glColor3d(1,1,1);
//			double a;
//			double b;
//			double t;
//			GL11.glBegin(GL11.GL_POINTS);
//			for(t = 0; t <= 1;  t = t + 0.001)	{     
//				a = this.origin.getX() + t*x;
//				b = this.origin.getY() + t*y;
//				GL11.glVertex3d(a,b,0);
//			}
//			GL11.glEnd();
//		}
//		
//		public void draw(int multiplier){
//			GL11.glColor3d(1,1,1);
//			double a;
//			double b;
//			double t;
//			GL11.glBegin(GL11.GL_POINTS);
//			for(t = 0; t <= multiplier;  t = t + 0.001)	{     
//				a = origin.getX() + t*x;
//				b = origin.getY() + t*y;
//				GL11.glVertex3d(a,b,0);
//			}
//			GL11.glEnd();
//		}
//		
//		public void drawFromPoint(int xP, int yP, int multiplier)
//		{
//			GL11.glColor3d(1,1,1);
//			double a;
//			double b;
//			double t;
//			GL11.glBegin(GL11.GL_POINTS);
//			for(t = 0; t <= multiplier;  t = t + 0.001)	{     
//				a = origin.getX() + t*x;
//				b = origin.getY() + t*y;
//				GL11.glVertex3d(a,b,0);
//			}
//			GL11.glEnd();
//		}
//		
//		public void draw(double R, double G, double B){
//			GL11.glColor3d(R,G,B);
//			double a;
//			double b;
//			double t;
//			GL11.glBegin(GL11.GL_POINTS);
//			for(t = 0; t <= 1;  t = t + 0.001)	{     
//				a = origin.getX() + t*x;
//				b = origin.getY() + t*y;
//				GL11.glVertex3d(a,b,0);
//			}
//			GL11.glEnd();
//		}
		
}
