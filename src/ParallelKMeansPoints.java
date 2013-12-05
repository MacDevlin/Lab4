import mpi.*;


public class ParallelKMeansPoints {

	public float getMaxWidth(Point[] points) {
		float maxWidth = 0;
		for (int i = 0; i < points.length; i++) {
			Point cur = points[i];
			if (cur.x > maxWidth) {
				maxWidth = cur.x;
			}
		}
		return maxWidth;
	}
	
	public float getMaxHeight(Point[] points) {
		float maxHeight = 0;
		for (int i = 0; i < points.length; i++) {
			Point cur = points[i];
			if (cur.y > maxHeight) {
				maxHeight = cur.y;
			}
		}
		return maxHeight;
	}
	
	public Point[] calculatePoints(Point[] points, int k) {
		DataGenerator dg = new DataGenerator();
		float width = getMaxWidth(points);
		float height = getMaxHeight(points);
		Point[] centroids = dg.generatePoints(k, width, height);
		
		boolean isChanged = true;
		
		while (isChanged) {
			isChanged = false;
			for (int p = 0; p < points.length; p++) {
				float minDist = -1;
				for(int i = 0; i<k; i++) {
					float curDist = points[p].distance(centroids[i]);
					if(minDist == -1 || curDist < minDist) {
						minDist = curDist;
						points[p].closestCentroid = i;
					}
				}
			}
			for (int i=0; i<k; i++) {
				float meanX = 0;
				float meanY = 0;
				int numClosest = 0;
				for(int p=0; p<points.length; p++) {
					if(points[p].closestCentroid == i) {
						meanX += points[p].x;
						meanY += points[p].y;
						numClosest++;
					}
				}
				if(numClosest > 0) {
					meanX /= numClosest;
					meanY /= numClosest;
					if(centroids[i].x != meanX || centroids[i].y != meanY) {
						isChanged = true;
					}
					centroids[i].x = meanX;
					centroids[i].y = meanY;
				}
			}
		}
		return centroids;
	}
	
	public static void main(String[] args) throws MPIException {
		int numPoints = Integer.parseInt(args[0]);
		int k = Integer.parseInt(args[1]);
		int width = Integer.parseInt(args[2]);
		int height = Integer.parseInt(args[3]);
		
		MPI.Init(args);
		int rank = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		MPI.Finalize();
		
	}
	
	
	
}
