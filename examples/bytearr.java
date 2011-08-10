
import org.jaywalk.*;


@Help("a custom converter for byte array arguments")
public class bytearr extends Commandlet {
	
	@Help("Uses special converter to receive byte array")
	public void dec (byte[] arr) {
		for (byte b : arr) {
			System.out.print(b+" ");
		}
		System.out.println();
	}
	
	@Converter
	public byte[] toByteArray(String s) {
		byte[] res = new byte[s.length()/2];
		for (int i = 0, j = 0; i < s.length(); i += 2, j++) {
			String hex = s.substring(i,i+2);
			res[j] = (byte)Short.parseShort(hex, 16);
		}
		return res;
	}
	
	public byte[] test() { return new byte[] {0x4E,0x3C,0x02}; }
	
	@Stringifier
	public String asString(byte[] arr) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arr.length; i++) {
			sb.append(String.format("%02X",arr[i]));
		}
		return sb.toString();
	}
	
	
	public static void main (String[] args) {
		new bytearr().go(args);
	}

}
