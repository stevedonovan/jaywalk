
import org.jaywalk.Json;
import org.jaywalk.MiniJsonServer;
import org.jaywalk.Param;


public class JsonTest extends MiniJsonServer {

	public String alice;
	public int n;
	
	
	// Passing parameters as fields
	public Json one() {
		return J("alice",alice,"n",n);
	}
	
	public Json whoareyou() {
		return J("name",System.getProperty("user.name"));
	}
	
	// Passing parameters as named arguments
	public Json add (
		@Param("x") double x,
		@Param("y") double y
	) {
		return J("result",x+y);
	}
	
	// you need to override this method if passing parameters as flags, 
	// to reset between invocations.
	protected void resetFields() {
		alice = null;
		n = 0;
	}	
	
	public static void main(String[] args) {
		new JsonTest().run(5557);
	}

}
