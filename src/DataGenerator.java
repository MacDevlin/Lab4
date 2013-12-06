import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


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
	
	
	public void generatePointsToFile(int n, String filename, int width, int height) throws IOException {
		Point[] points = generatePoints(n,width,height);
		File f = new File(filename);
		if(f.exists()) {
			f.delete();
		}
		FileOutputStream fout = new FileOutputStream(f);
		for(int p=0; p<points.length; p++) {
			String s = points[p].x + "," + points[p].y + "\n";
			fout.write(s.getBytes());
		}
	}
	
	public void generateDNAToFile(int n, String filename, int length) throws IOException {
		DNA[] dna = generateDNA(n,length);
		File f = new File(filename);
		if(f.exists()) {
			f.delete();
		}
		FileOutputStream fout = new FileOutputStream(f);
		for(int p=0; p<dna.length; p++) {
			String s = String.valueOf(dna[p].code) + "\n";
			fout.write(s.getBytes());
		}
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
