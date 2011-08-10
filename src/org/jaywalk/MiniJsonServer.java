package org.jaywalk;
import java.io.*;
import java.net.*;
import java.util.*;

public class MiniJsonServer extends Commandlet {
	

	protected static Json J(Object...objects ) {
		Json res = new Json();
		extendMap(res,objects);
		return res;		
	}	

	static final String eoln = "\r\n";
	
	private String buildHeaders(String code, String type, int length) {
		return "HTTP/1.1 "+code + eoln
		+ "Content-Type: " + type + eoln
		+ "Content-Length: " + length + eoln
		+ "Connection: close" + eoln + eoln;		
	}
	
	protected Writer outf = null;
	protected BufferedReader inf = null;
	
	protected void println(String out, boolean error) {
		if (error) {
			out = Json.asText(J("error",out),null);
		}
		String headers = buildHeaders("200 OK","text/json",((String)out).length());
		if (outf != null) {
			try {
				System.out.println(out);
				outf.write(headers);
				outf.write((String)out);
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.print(headers);
			System.out.println(out);			
		}
	
	}
	
	Map<String,String> readHeaders() throws IOException {
		Map<String,String> out = new HashMap<String,String>();
		String line = inf.readLine();
		while (! line.equals("")) {
			String[] pair = line.split(":");
			out.put(pair[0], pair[1]);
			line = inf.readLine();
		}
		return out;
	}
	


	public void decode (String url) throws Exception {
		String[] parts = url.split("\\?");
		List<String> out = new ArrayList<String>();
		out.add(parts[0]);
		if (parts.length > 1) {
			String[] vars = parts[1].split("&");		
			for (String v : vars) {
				String[] pair = v.split("=");
				out.add("-"+pair[0]);
				out.add(URLDecoder.decode(pair[1],"UTF-8"));
			}
		}
		go(out.toArray(new String[0]));
	}	

	public static void main(String[] args) {
//		new UrlDecode().go(args);
		MiniJsonServer me = new MiniJsonServer();
		me.run(5557);
	}
	
	@Stringifier
	public String json(Json map) { return Json.asText(map," "); }
	

	protected void run(int port) {
		try {
			ServerSocket server = new ServerSocket(port);
			while (true) {
				Socket client = server.accept();
				Writer out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				System.setProperty("line.separator", "\n");
				String request = in.readLine();
				outf = out;
				inf = in;
				String[] parts = request.split(" ");
				if (parts[0].equals("GET")) {
					resetFields();
					Map<String,String> headers = readHeaders();
//					for (String head : headers.keySet()) {
//						System.out.println(head+"="+headers.get(head));
//					}
					decode(parts[1].substring(1));					
				}
				out.close();
				client.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void resetFields() {
	
	}

}
