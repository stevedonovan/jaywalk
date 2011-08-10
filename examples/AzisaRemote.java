// client.java
// server.java
import java.text.DateFormat;
import java.lang.Object;
import java.io.*;
import java.util.*;

import org.jaywalk.Json;
import org.jaywalk.MiniJsonServer;
import org.jaywalk.Stringifier;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.PortableServer.*;

// result of idlj remote.idl went in this package
import remote.*;

public class AzisaRemote extends MiniJsonServer {
	static ORB orb;
	static POA rootPOA;
	static NamingContextExt nce;
	static Remote remote;

    static DateFormat df;

    static void init() {
        try {
			String[] orbArgs = {
					"-ORBInitialPort", "1050",
					"-ORBInitialHost", "146.64.150.53"
			};
			orb = ORB.init(orbArgs, null);
			rootPOA = POAHelper.narrow(
					orb.resolve_initial_references("RootPOA"));
			rootPOA.the_POAManager().activate();

			nce = NamingContextExtHelper.narrow(
					orb.resolve_initial_references("NameService"));

			remote = RemoteHelper.unchecked_narrow(nce.resolve_str("Remote"));

        } catch(Exception e) {
            e.printStackTrace();
        }

    }
    
	@Stringifier
	public String json(Json map) { return Json.asText(map," "); }
	
	private Map<String,Object> sensorToMap(Sensor s, TimeSpan span) {
		return M("device",s.device,"description",s.description,
				"position",M("x",s.position.x,"y",s.position.y,"z",s.position.z,"description",s.position.description),
				"t1",df.format(span.start),"t2",df.format(span.finish));
	}
	
	private Json error(String msg) {
		return J("error",msg);
	}
	
	private long parseSid(String ssid) {
        try {
            return Long.parseLong(ssid,16);
        } catch(Exception e) {
            return 0;
        }		
	}
	
	public Json getSensors() {
        Sensor[] ss = remote.getSensors();
        Map<String,Object> sout = new HashMap<String,Object>();
        for (Sensor s : ss) {
            TimeSpan span = remote.getTimeSpan(s.sid,1);
            sout.put(s.identifier, sensorToMap(s,span));
        }    
		return J("sensors",sout);
	}
	
	public String ssid;
	public int did;
	public long t1;
	public long t2;	
	
	protected void resetFields() {
		ssid = null;
		did = 0;
		t1 = 0;
		t2 = 0;
	}
	
	private TimeSpan getTimeSpan(long sid) {
		TimeSpan span;
		if (t1 == 0 && t2 == 0)
			span = remote.getTimeSpan(sid, did);
		else
			span = new TimeSpan(t1,t2);
		return span;
	}                        
	
	
	public Json getSensor() {
		if (ssid == null) return error("must provide 'ssid'");
		long sid = parseSid(ssid);
		if (sid == 0) return error("cannot parse "+ssid);
		Sensor s = remote.getSensor(sid);
//		 /*
		TimeSpan span = remote.getTimeSpan(s.sid,1);
		Object[] dout = new Object[s.detectors.length];
		int i = 0;
		for (Detector d : s.detectors) {
			dout[i] = M("did",d.did,"description",d.description,"dataType",d.dataType,
					"phenomenon",d.phenomenon,"units",d.units
					);
			++i;
		}
		return J("id",s.identifier,"sensor",sensorToMap(s,span),"detectors",dout);
		// */
//		return J(new FieldMap(s));
		
	}
	
	public Json getSingleDataRange() {
		if (ssid == null) return error("must provide 'ssid'");
		long sid = parseSid(ssid);
		if (sid == 0) return error("cannot parse "+ssid);
		if (did == 0) return error("must provide 'did'");
		Results results = remote.getSingleDataRange(sid, did, getTimeSpan(sid));
		// have to explicitly box the primitive arrays to serialize out to JSON
		int len = results.times.length;
		Object[] times = new Object[len];
		Object[] values = new Object[len];
		for (int i = 0; i < len; ++i) {
			times[i] = results.times[i];
			values[i] = results.values[i];
		}
		return J("times",times,"values",values);
	}

	   
    public static void main(String[] args) {
        init();
        df = DateFormat.getDateTimeInstance();
        //consoleTest(args);
        new AzisaRemote().run(5557);
    }   

}
