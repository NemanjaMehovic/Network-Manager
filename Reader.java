package projekat;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import com.ireasoning.protocol.TimeoutException;
import com.ireasoning.protocol.snmp.SnmpConst;
import com.ireasoning.protocol.snmp.SnmpPdu;
import com.ireasoning.protocol.snmp.SnmpSession;
import com.ireasoning.protocol.snmp.SnmpVarBind;

public class Reader implements Runnable {

	public static final int maxPoints = 30;
	public static String oidInByte = ".1.3.6.1.2.1.2.2.1.10.";
	public static String oidOutByte = ".1.3.6.1.2.1.2.2.1.16.";
	public static String oidInUPkt = ".1.3.6.1.2.1.2.2.1.11.";
	public static String oidOutUPkt = ".1.3.6.1.2.1.2.2.1.17.";
	public static String oidInNUPkt = ".1.3.6.1.2.1.2.2.1.12.";
	public static String oidOutNUPkt = ".1.3.6.1.2.1.2.2.1.18.";
	private static ArrayList<Reader> readers = new ArrayList<Reader>();
	private int interfaceNumber;
	private String address;
	private int port;
	private String communityRead;
	private String communityWrite;
	private SnmpSession session = null;
	private Thread thread=null;
	private int oldProtok = 0;
	private int newProtok = 0;
	private int oldPaketi = 0;
	private int newPaketi = 0;
	private boolean selected = false;
	private ArrayList<Integer> protok = new ArrayList<Integer>();
	private ArrayList<Integer> paketi = new ArrayList<Integer>();
	
	
	
	
	public Reader(String address, int port, String communityRead, String communityWrite, int interfaceNumber) {
		super();
		this.address = address;
		this.port = port;
		this.communityRead = communityRead;
		this.communityWrite = communityWrite;
		this.interfaceNumber = interfaceNumber;
		readers.add(this);
		thread= new Thread(this);
	}

	private int getValFromSession(String oidString) throws IOException
	{
		int ret=0;
		session = new SnmpSession(address,port,communityRead, communityWrite,SnmpConst.SNMPV2);
		session.setTimeout(10000);
		SnmpPdu tmpPdu = session.snmpGetRequest(oidString+interfaceNumber);
		SnmpVarBind varBind = tmpPdu.getVarBind(0);
		ret += Integer.parseInt(varBind.getValue().toString());
		session.close();
		return ret;
	}
	
	@Override
	public void run() {
		while (!thread.interrupted())
		{
			try
			{
				oldProtok = newProtok;
				oldPaketi = newPaketi;
				newProtok = newPaketi = 0;
				newProtok += getValFromSession(oidInByte);
				newProtok += getValFromSession(oidOutByte);
				newPaketi += getValFromSession(oidInUPkt);
				newPaketi += getValFromSession(oidOutUPkt);
				newPaketi += getValFromSession(oidInNUPkt);
				newPaketi += getValFromSession(oidOutNUPkt);
				synchronized (this) {
					if(oldProtok != 0)
						protok.add(8*(newProtok-oldProtok)/10);
					if(protok.size() > maxPoints)
						protok.remove(0);
					if(oldPaketi != 0)
						paketi.add(newPaketi-oldPaketi);
					if(paketi.size() > maxPoints)
						paketi.remove(0);
					if(!paketi.isEmpty() && !protok.isEmpty() && selected)
					{
						Main.prozorMain.getProtokGraf().Signal();
						Main.prozorMain.getPaketiGraf().Signal();
					}
				}
				thread.sleep(10000);
			}
			catch (InterruptedException e)
			{
				break;
			}catch (TimeoutException e) {
				synchronized (System.out) {
					System.out.println("Timeout");
				}
			}catch (IOException e) {
				synchronized (System.out) {
					System.out.println("Failed to creat session");
				}
			}
		}
	}
	
	public void start()
	{
		thread.start();
	}
	
	public void close()
	{
		if(session!=null)
			session.close();
		thread.interrupt();
	}
	
	public synchronized ArrayList<Integer> GetProtok()
	{
		return protok;
	}
	
	public synchronized ArrayList<Integer> GetPaketi()
	{
		return paketi;
	}

	public synchronized void setSelected(boolean selected)
	{
		this.selected = selected;
	}
	
}
