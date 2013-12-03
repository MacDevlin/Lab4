
public class DNA {
	char[] code;
	int closestCentroid;
	
	public DNA(int length) {
		code = new char[length];
	}
	
	public void set(int position, char val) {
		code[position] = val;
	}
	
	public int distance(DNA d) {
		int dist = 0;
		for(int i=0; i<code.length; i++) {
			if(d.code[i] != code[i]) {
				dist++;
			}
		}
		return dist;
	}
}
