
public class DataGenerator {

	public Point[] generatePoints (int n, float width, float height) {
		Point[] points = new Point[n];
		for (int i = 0; i < n; i++) {
			
			float x = (float) (Math.random()*width);
			float y = (float) (Math.random()*height);
			Point p = new Point(x,y);
			points[i] = p;
		}
		return points;
	}
	
	public DNA[] generateDNA (int n, int length) {
		DNA[] dnas = new DNA[n];
		for (int i = 0; i < n; i++) {
			DNA dna = new DNA(length);
			for (int j = 0; j < length; j++) {
				int num = (int)(Math.random()*4);
				char val=' ';
				if(num == 0) {
					val = 'A';
				}
				if(num == 1) {
					val = 'T';
				}
				if(num == 2) {
					val = 'C';
				}
				if(num == 3) {
					val = 'G';
				}
				dna.set(j,val);
			}
			dnas[i] = dna;
		}
		return dnas;
	}
}
