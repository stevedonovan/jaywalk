
import java.util.Map;

import org.jaywalk.*;


@Help("demonstrating a command with a custom output filter")
public class CustomStringifier extends Commandlet {
	
	// here, maps are put out in JSON format. The name of this method is not
	// important, as long as it has the Stringifier attribute.
	@Stringifier
	public String json(Map<String,Object> map) {
		return Json.asText(map," ");
	}
	
	@Help("Demonstrating defining stringifiers for arbitrary output")
	public Map<String,Object> result(int a, int b) {
		return M("one",A(a,10*a),"two",A(b,10*b));			
	}	

	public static void main(String[] args) {
		new CustomStringifier().go(args);
	}

}
