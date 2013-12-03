import java.io.IOException;


public class Server {
	
	public static void printPoints(Point[] points) {
		for(int i=0; i<points.length; i++) {
			System.out.println("(" + points[i].x + ", " + points[i].y + ")");
		}
	}
	
	public static void printDNA(DNA[] dnas) {
		for(int i=0; i<dnas.length; i++) {
			String code = String.valueOf(dnas[i].code);
			System.out.println("<" + code + ">");
		}
	}
	
	public static void processConsole(String request) {
		if(request.startsWith("launch")) {
			String [] args = request.split(" ");
			if (args.length < 5) {
				System.out.println("Invalid number of arguments");
				return;
			}
			boolean isParallel = args[1].equals("parallel");
			boolean isPoints = args[2].equals("points");
			int numVals = Integer.parseInt(args[3]);
			int k = Integer.parseInt(args[4]);
			DataGenerator dg = new DataGenerator();
			if (isPoints) {
				if (args.length != 7) {
					System.out.println("Invalid number of arguments");
					return;
				}
				int width = Integer.parseInt(args[5]);
				int height = Integer.parseInt(args[6]);
				Point[] ps = dg.generatePoints(numVals, width, height);
				printPoints(ps);
				if (!isParallel) {
					SequentialKMeans seqKM = new SequentialKMeans();
					System.out.println("Launching sequential k-means for " + numVals + " points, (k=" + k + ",width=" + width + ",height=" + height + ")");
					Point[] centroids = seqKM.calculatePoints(ps, k);
					printPoints(centroids);
				}
			}
			else {
				if (args.length != 6) {
					System.out.println("Invalid number of arguments");
					return;
				}
				int length = Integer.parseInt(args[5]);
				DNA[] ds = dg.generateDNA(numVals, length);
				printDNA(ds);
				if (!isParallel) {
					SequentialKMeans seqKM = new SequentialKMeans();
					System.out.println("Launching sequential k-means for " + numVals + " dnas, (k=" + k + ",length=" + length + ")");
					DNA[] centroids = seqKM.calculateDNAs(ds, k);
					printDNA(centroids);
				}
			}
		}
	}
	
	public static void startShell() {
		String request = "";
		
		while(true) {
			try {
				if(System.in.available()>0) {
					char readByte = (char)System.in.read();
					if(readByte != '\n' && readByte != '\r') {
						request = request + readByte;
					}
					else {
						//end of request
						processConsole(request);
						request = "";
					}
				}
			} catch (IOException e) {
				
			}
		}
	}
	
	public static void main (String args[]) {
		startShell();
	}
}
