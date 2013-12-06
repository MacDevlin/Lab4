
public class DNA {
	char[] code;
	int closestCentroid;
	
	public DNA(int length) {
		code = new char[length];
	}
	
	public DNA(String code) {
		this.code = code.toCharArray();
	}
	
	public void set(int position, char val) {
		code[position] = val;
	}
	
	public int distance(DNA d) {
		int dist = 0;
		if(d.code.length != code.length){
			System.out.println("HO SHIT");
		}
		for(int i=0; i<code.length; i++) {
			if(d.code[i] != code[i]) {
				dist++;
			}
		}
		return dist;
	}
}
