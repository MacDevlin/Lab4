import mpi.MPI;
import mpi.MPIException;


public class ParallelKMeansDNA {
	
	public DNA[] runServer(DNA[] dnas, int k, int length) throws MPIException {
		int size = MPI.COMM_WORLD.Size();
		int totalDNA = dnas.length;
		//make the initial centroids
		DataGenerator dg = new DataGenerator();
		DNA[] cs = dg.generateDNA(k, length);
		String centroidsStr = dnas2String(cs,0, k);
		char[] centroidsAr = centroidsStr.toCharArray();
		
		try {
			
			//calculate dnas per node
			int avgDNA = totalDNA/(MPI.COMM_WORLD.Size()-1);
			int rem = totalDNA % (MPI.COMM_WORLD.Size()-1);
			
			//send data out to each node
			for(int node = 1; node < MPI.COMM_WORLD.Size(); node++) {
				int numDNA = avgDNA;
				if(node == MPI.COMM_WORLD.Size()-1) {
					numDNA += rem; //add the remainder to the last node
				}
				
				//send the number of dnas and the number of centroids, and the algorithm
				System.out.println("Sending " + numDNA + " to node " + node);
				char[] message = ("dna " + numDNA + " " + k + " " + length + " ").toCharArray();
				MPI.COMM_WORLD.Send(message, 0, message.length, MPI.CHAR, node, node);
				
				//send the dna and centroids
				String dnaStr = dnas2String(dnas,(node-1)*avgDNA, numDNA);
				char[] dnaAr = dnaStr.toCharArray();
				MPI.COMM_WORLD.Send(dnaAr, 0, dnaAr.length, MPI.CHAR, node, node);
				
				MPI.COMM_WORLD.Send(centroidsAr, 0, centroidsAr.length, MPI.CHAR, node, node);
				
			}
			//initialization complete, now start the loop
			while(true) {
				DNA[] newCentroids = new DNA[k];
				int[][][] counter = new int[k][4][length];
				
				for(int c=0; c<k; c++) {
					newCentroids[c] = new DNA(length);
				}
				for(int node = 1; node<size; node++) {
					int numDNA = avgDNA;
					if(node == size-1) {
						numDNA += rem;
					}
					char[] curCentroidsAr = new char[(length+2)*k];
					MPI.COMM_WORLD.Recv(curCentroidsAr, 0, curCentroidsAr.length, MPI.CHAR, node, node);
					DNA[] curCentroids = parseDNA(curCentroidsAr);
					for(int c = 0; c<k; c++) {
						for(int i=0; i<length; i++) {
							char letter = curCentroids[c].code[i];
							if(letter == 'A') {
								counter[c][0][i] += numDNA;
							} else if(letter == 'C') {
								counter[c][1][i] += numDNA;
							} else if(letter == 'T') {
								counter[c][2][i] += numDNA ;
							} else {
								counter[c][3][i] += numDNA;
							}
						}
					}
				}
				//normalize
				boolean isChanged = false;
				for(int c = 0; c<k; c++) {
					for(int i=0; i<length; i++) {
						int max = 0;
						char maxChar = 'A';
						if(counter[c][0][i] > max) {
							max = counter[c][0][i];
							maxChar = 'A';
						} else if(counter[c][1][i] > max) {
							max = counter[c][1][i];
							maxChar = 'C';
						} else if(counter[c][2][i] > max) {
							max = counter[c][2][i];
							maxChar = 'T';
						} else if(counter[c][3][i] > max) {
							max = counter[c][3][i];
							maxChar = 'G';
						}
						if(cs[c].code[i] != maxChar) {
							//there has been a change
							isChanged=true;
						}
						newCentroids[c].code[i] = maxChar;
					}
				}
				
				//given the new centroids, we need to send them to all nodes if changed
				
				if(!isChanged) {
					//send quit message
					for(int node = 1; node<size; node++) {
						MPI.COMM_WORLD.Send("end".toCharArray(), 0, "end".toCharArray().length, MPI.CHAR, node, node);
					}
					return cs;
				} else {
					//send new centroid data
					char[] newCentroidsAr = dnas2String(newCentroids, 0, newCentroids.length).toCharArray();
					for(int node = 1; node<size; node++) {
						MPI.COMM_WORLD.Send(newCentroidsAr, 0, newCentroidsAr.length, MPI.CHAR, node, node);
					}
					
					//update centroid buffers
					cs = newCentroids;
				}
				
				
			}
			
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			System.out.println("Send failed");
		
		}
		
		return null;
	}
	
	public static void printDNA(DNA[] dnas) {
		for(int i=0; i<dnas.length; i++) {
			String code = String.valueOf(dnas[i].code);
			System.out.println("<" + code + ">");
		}
	}
	
	public void initialize(String[] comArgs) throws MPIException {
		int rank = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		int numDNA = Integer.parseInt(comArgs[1]);
		int k = Integer.parseInt(comArgs[2]);
		int length = Integer.parseInt(comArgs[3]);
		//get the point and initial centroid data
		char[] dnaAr = new char[numDNA*(length+2)]; //lets take a guess
		MPI.COMM_WORLD.Recv(dnaAr, 0, dnaAr.length, MPI.CHAR, 0, rank);
		char[] centroidsAr = new char[k*(length+2)];
		MPI.COMM_WORLD.Recv(centroidsAr, 0, centroidsAr.length, MPI.CHAR, 0, rank);
		
		DNA[] dnas = parseDNA(dnaAr);
		DNA[] centroids = parseDNA(centroidsAr); 
		System.out.println("Node " + rank + " initialized with " + dnas.length + " DNA, " + centroids.length + " centroids");
		calculateDNAs(dnas, k, centroids);
	}
	
	public static String dnas2String(DNA[] dnas, int start, int count) {
		String str = "";
		for(int p = start; p<start+count; p++) {
			str += String.valueOf(dnas[p].code);
			str += "p";
		}
		return str;
	}
	
	public static DNA[] parseDNA(char[] dnaAr) {
		//System.out.println("ACTUAL CHAR ARRAY:" + String.valueOf(dnaAr));
		String dnaStr = String.valueOf(dnaAr);
		String[] p1 = dnaStr.split("p");
		//System.out.println("num sections:" + p1.length);
		DNA[] dnas = new DNA[p1.length-1];
		for(int p = 0; p<p1.length-1; p++) {
			dnas[p] = new DNA(p1[p]);
			//System.out.println("CHECK: " + String.valueOf(dnas[p].code));
		}
		return dnas;
	}
	
	public void calculateDNAs(DNA[] dnas, int k, DNA[] centroids) throws MPIException {
		int rank = MPI.COMM_WORLD.Rank();
		int length = dnas[0].code.length;
		
		while (true) {
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
						centroids[i].code[c] = newVal;
					}
				}
			}
			
			
			//send adjusted centroids back
			String cStr = dnas2String(centroids,0,centroids.length);
			char[] cAr = cStr.toCharArray();
			MPI.COMM_WORLD.Send(cAr, 0, cAr.length, MPI.CHAR, 0, rank);
			char[] newCar = new char[k*(length+2)];
			MPI.COMM_WORLD.Recv(newCar, 0, newCar.length, MPI.CHAR, 0, rank);
			if(String.valueOf(newCar).startsWith("end")) {
				//we are done
				return;
			}
			centroids = parseDNA(newCar);
		}
	}
	
}
