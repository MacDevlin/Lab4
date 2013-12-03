
public class SequentialKMeans {

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
	
	public DNA[] calculateDNAs(DNA[] dnas, int k) {
		DataGenerator dg = new DataGenerator();
		int length = dnas[0].code.length;
		DNA[] centroids = dg.generateDNA(k, length);
		
		boolean isChanged = true;
		
		while (isChanged) {
			isChanged = false;
			for (int p = 0; p < dnas.length; p++) {
				float minDist = -1;
				for(int i = 0; i<k; i++) {
					float curDist = dnas[p].distance(centroids[i]);
					if(minDist == -1 || curDist < minDist) {
						minDist = curDist;
						dnas[p].closestCentroid = i;
					}
				}
			}
			for (int i=0; i<k; i++) {
				int[] As = new int[length];
				int[] Cs = new int[length];
				int[] Ts = new int[length];
				int[] Gs = new int[length];
				
				int numClosest = 0;
				for(int p=0; p<dnas.length; p++) {
					if(dnas[p].closestCentroid == i) {
						for(int c = 0; c<length; c++) {
							char elem = dnas[p].code[c];
							if(elem == 'A') {
								As[c] ++;
							}
							if(elem == 'C') {
								Cs[c] ++;
							}
							if(elem == 'T') {
								Ts[c] ++;
							}
							if(elem == 'G') {
								Gs[c] ++;
							}
							
						}
						numClosest++;
					}
				}
				if(numClosest > 0) {
					for(int c = 0; c<length; c++) {
						char curVal = centroids[i].code[c];
						int mostAtPosition = 0;
						char newVal = 'A';
						if(As[c] > mostAtPosition) {
							mostAtPosition = As[c];
							newVal = 'A';
						}
						if(Cs[c] > mostAtPosition) {
							mostAtPosition = Cs[c];
							newVal = 'C';
						}
						if(Ts[c] > mostAtPosition) {
							mostAtPosition = Ts[c];
							newVal = 'T';
						}
						if(Gs[c] > mostAtPosition) {
							mostAtPosition = Gs[c];
							newVal = 'G';
						}
						if(curVal != newVal) {
							isChanged=true;
						}
						centroids[i].code[c] = newVal;
					}
				}
			}
		}
		return centroids;
	}
	
}
