
import java.util.*;

import org.jaywalk.Commandlet;
import org.jaywalk.Json;
import org.jaywalk.Stringifier;

public class JDataTest extends Commandlet {
	
	static class JData extends HashMap<String,Object> {}
	
	@Stringifier
	public String json(JData map) {
		return Json.asText(map," ");
	}	
	
	static JData J(Object...objects ) {
		JData res = new JData();
		extendMap(res,objects);
		return res;		
	}
	
	public JData result(int a, int b) {
		return J("one",A(a,10*a),"two",A(b,10*b));			
	}		

	public static void main(String[] args) {
		new JDataTest().go(args);
	}

}
