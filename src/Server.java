import java.io.IOException;
import mpi.*;

public class Server {
	
	public static void printPoints(Point[] points) {
		if(points == null) {
			return;
		}
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
	
	public static void processConsole(String request) throws MPIException {
		if(request.equals("test")) {
			processConsole("launch parallel points 13 2 10 10");
		}
		if(request.equals("test2")) {
			processConsole("launch parallel dna 13 2 12");
		}
		
		if(request.startsWith("quit")) {
			for(int i=1; i<MPI.COMM_WORLD.Size(); i++) {
				MPI.COMM_WORLD.Send("quit".toCharArray(), 0, "quit".toCharArray().length, MPI.CHAR, i, i);
			}
			//MPI.Finalize();
			MPI.COMM_WORLD.Abort(0);
		}
		
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
				if (!isParallel) {
					
					printPoints(ps);
					SequentialKMeans seqKM = new SequentialKMeans();
					System.out.println("Launching sequential k-means for " + numVals + " points, (k=" + k + ",width=" + width + ",height=" + height + ")");
					Point[] centroids = seqKM.calculatePoints(ps, k);
					printPoints(centroids);
				} else {
					System.out.println("Launching parallel k-means for " + numVals + " points, (k=" + k + ",width=" + width + ",height=" + height + ")");
					
					ParallelKMeansPoints pkp = new ParallelKMeansPoints();
					Point[] centroids = pkp.runServer(ps, k, width, height);
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
				} else {
					System.out.println("Launching parallel k-means for " + numVals + " dna, (k=" + k + ",length=" + length + ")");
					
					ParallelKMeansDNA pkd = new ParallelKMeansDNA();
					DNA[] centroids = pkd.runServer(ds, k, length);
					printDNA(centroids);
					
				}
			}
		}
	}
	
	public static void startShell() throws MPIException {
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
	
	
	public static void beComputeNode() throws MPIException {
		int rank = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		//wait to receive the data
		char[] command = new char[100]; //100 character commands
		MPI.COMM_WORLD.Recv(command, 0, command.length, MPI.CHAR, 0, rank);
		String commandStr = String.valueOf(command);
		String[] comArgs = commandStr.split(" ");
		if(comArgs[0].equals("points")) {
			ParallelKMeansPoints pkp = new ParallelKMeansPoints();
			pkp.initialize(comArgs);
		} else if(comArgs[0].equals("quit")) {
			MPI.Finalize();
			MPI.COMM_WORLD.Abort(0);
		} else if(comArgs[0].equals("dna")) {
			ParallelKMeansDNA pkd = new ParallelKMeansDNA();
			pkd.initialize(comArgs);
		}
		
	}
	
	public static void main (String args[]) throws MPIException {
		MPI.Init(args);
		int rank = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		System.out.println("Rank: " + rank + " totalSize: " + size);
		
		if(rank==0) {
			//send the data
			startShell();
		}
		else {
			while(true) {
				beComputeNode();
			}
		}
		
		MPI.Finalize();
	}
}
