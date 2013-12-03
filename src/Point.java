
public class Point {
	
	float x;
	float y;
	int closestCentroid;
	
	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public float distance(Point p) {
		return (float) Math.sqrt((p.x-x)*(p.x-x) + (p.y-y)*(p.y-y));
	}
}
