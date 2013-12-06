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
	
	public Point[] runServer(Point[] ps, int k, int width, int height) throws MPIException {
		int size = MPI.COMM_WORLD.Size();
		int totalPoints = ps.length;
		//make the initial centroids
		DataGenerator dg = new DataGenerator();
		Point[] cs = dg.generatePoints(k, width, height);
		String centroidsStr = points2String(cs,0, k);
		char[] centroidsAr = centroidsStr.toCharArray();
		
		try {
			
			//calculate points per node
			int avgPoints = totalPoints/(MPI.COMM_WORLD.Size()-1);
			int rem = totalPoints % (MPI.COMM_WORLD.Size()-1);
			
			//send data out to each node
			for(int node = 1; node < MPI.COMM_WORLD.Size(); node++) {
				int numPoints = avgPoints;
				if(node == MPI.COMM_WORLD.Size()-1) {
					numPoints += rem; //add the remainder to the last node
				}
				
				//send the number of points and the number of centroids, and the algorithm
				char[] message = ("points " + numPoints + " " + k + " ").toCharArray();
				MPI.COMM_WORLD.Send(message, 0, message.length, MPI.CHAR, node, node);
				
				//send the points and centroids
				String pointsStr = points2String(ps,(node-1)*avgPoints, numPoints);
				char[] pointsAr = pointsStr.toCharArray();
				MPI.COMM_WORLD.Send(pointsAr, 0, pointsAr.length, MPI.CHAR, node, node);
				
				MPI.COMM_WORLD.Send(centroidsAr, 0, centroidsAr.length, MPI.CHAR, node, node);
				
			}
			//initialization complete, now start the loop
			while(true) {
				Point[] newCentroids = new Point[k];
				for(int c=0; c<k; c++) {
					newCentroids[c] = new Point(0,0);
				}
				for(int node = 1; node<size; node++) {
					int numPoints = avgPoints;
					if(node == size-1) {
						numPoints += rem;
					}
					char[] curCentroidsAr = new char[33*k];
					MPI.COMM_WORLD.Recv(curCentroidsAr, 0, curCentroidsAr.length, MPI.CHAR, node, node);
					Point[] curCentroids = parsePoints(curCentroidsAr);
					for(int c = 0; c<k; c++) {
						newCentroids[c].x += curCentroids[c].x*numPoints;
						newCentroids[c].y += curCentroids[c].y*numPoints;
					}
				}
				//normalize
				for(int c = 0; c<k; c++) {
					newCentroids[c].x /= totalPoints;
					newCentroids[c].y /= totalPoints;
				}
				
				//given the new centroids, we need to send them to all nodes if changed
				boolean isChanged = false;
				for(int c = 0; c<k; c++) {
					if(cs[c].x != newCentroids[c].x || cs[c].y != newCentroids[c].y) {
						isChanged = true;
					}
				}
				
				if(!isChanged) {
					//send quit message
					for(int node = 1; node<size; node++) {
						MPI.COMM_WORLD.Send("end".toCharArray(), 0, "end".toCharArray().length, MPI.CHAR, node, node);
					}
					return cs;
				} else {
					//send new centroid data
					char[] newCentroidsAr = points2String(newCentroids, 0, newCentroids.length).toCharArray();
					for(int node = 1; node<size; node++) {
						MPI.COMM_WORLD.Send(newCentroidsAr, 0, newCentroidsAr.length, MPI.CHAR, node, node);
					}
					
					//update centroid buffers
					cs = newCentroids;
				}
				
				
			}
			
		} catch (MPIException e) {
			System.out.println("Send failed");
		
		}
		
		return null;
	}
	
	public void initialize(String[] comArgs) throws MPIException {
		int rank = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		int numPoints = Integer.parseInt(comArgs[1]);
		int k = Integer.parseInt(comArgs[2]);
		//get the point and initial centroid data
		char[] pointsAr = new char[numPoints*33]; //lets take a guess
		MPI.COMM_WORLD.Recv(pointsAr, 0, pointsAr.length, MPI.CHAR, 0, rank);
		char[] centroidsAr = new char[k*33];
		MPI.COMM_WORLD.Recv(centroidsAr, 0, centroidsAr.length, MPI.CHAR, 0, rank);
		
		Point[] points = parsePoints(pointsAr);
		Point[] centroids = parsePoints(centroidsAr); 
		System.out.println("Node " + rank + " initialized with " + points.length + " points, " + centroids.length + " centroids");
		calculatePoints(points, k, centroids);
	}
	
	public static String points2String(Point[] points, int start, int count) {
		String str = "";
		for(int p = start; p<start+count; p++) {
			str += "p";
			str += points[p].x;
			str += ",";
			str += points[p].y;
		}
		return str;
	}
	
	public static Point[] parsePoints(char[] pointsAr) {
		String pointsStr = String.valueOf(pointsAr);
		String[] p1 = pointsStr.split("p");
		Point[] points = new Point[p1.length-1];
		for(int p = 1; p<p1.length; p++) {
			String[] comps = p1[p].split(",");
			float x = Float.parseFloat(comps[0]);
			float y = Float.parseFloat(comps[1]);
			points[p-1] = new Point(x,y);
		}
		return points;
	}
	
	public void calculatePoints(Point[] points, int k, Point[] centroids) throws MPIException {
		int rank = MPI.COMM_WORLD.Rank();
		
		while (true) {
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
					centroids[i].x = meanX;
					centroids[i].y = meanY;
				}
			}
			//send adjusted centroids back
			String cStr = points2String(centroids,0,centroids.length);
			char[] cAr = cStr.toCharArray();
			MPI.COMM_WORLD.Send(cAr, 0, cAr.length, MPI.CHAR, 0, rank);
			char[] newCar = new char[k*33];
			MPI.COMM_WORLD.Recv(newCar, 0, newCar.length, MPI.CHAR, 0, rank);
			if(String.valueOf(newCar).startsWith("end")) {
				//we are done
				return;
			}
			centroids = parsePoints(newCar);
		}
	}
	
}
