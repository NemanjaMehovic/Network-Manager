package projekat;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.security.PublicKey;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.ireasoning.protocol.TimeoutException;
import com.ireasoning.protocol.snmp.SnmpConst;
import com.ireasoning.protocol.snmp.SnmpPdu;
import com.ireasoning.protocol.snmp.SnmpSession;
import com.ireasoning.protocol.snmp.SnmpTableModel;
import com.ireasoning.protocol.snmp.SnmpVarBind;

public class Main extends JFrame{
	
	public static Main prozorMain;
	public static String oidNumInterfaces = "1.3.6.1.2.1.2.1.0";
	public static String oidDescription =".1.3.6.1.2.1.2.2.1.2.";
	public static String oidStatus = ".1.3.6.1.2.1.2.2.1.7.";
	private ArrayList<ArrayList<String>> interfaceNameLists = new ArrayList<ArrayList<String>>();
	private ArrayList<ArrayList<Reader>> interfacesLists = new ArrayList<ArrayList<Reader>>();
	private SnmpSession session = null;
	private JPanel inputPanel;
	private JPanel grafPanel;
	private JTextArea addressArea;
	private JTextArea portArea;
	private JTextArea communityReadArea;
	private JTextArea communityWriteArea;
	private JButton addButton;
	private JComboBox<String> routerBox;
	private JComboBox<String> interfaceBox;
	private Graf protokGraf;
	private Graf paketiGraf;
	private JLabel protokLabel;
	private JLabel paketiLabel;
	
	private static void addToPanel(JComponent component,JPanel panel,Dimension dimension)
	{
		component.setPreferredSize(dimension);
		panel.add(component);
	}
	
	public Main()
	{
		super("Rm projekat");
		setSize(1000,700);
		setLayout(new FlowLayout());
		
		inputPanel = new JPanel();
		inputPanel.setSize(getWidth()/3-50,getHeight()-50);
		inputPanel.setPreferredSize(inputPanel.getSize());
		
		Dimension standarDimension = new Dimension(inputPanel.getWidth(),inputPanel.getHeight()/30);
		addToPanel(new JLabel("Adresa"), inputPanel, standarDimension);
		addToPanel(addressArea = new JTextArea(), inputPanel, standarDimension);
		addToPanel(new JLabel("Port"), inputPanel, standarDimension);
		addToPanel(portArea = new JTextArea("161"), inputPanel, standarDimension);
		addToPanel(new JLabel("Community read"), inputPanel, standarDimension);
		addToPanel(communityReadArea = new JTextArea("si2019"), inputPanel, standarDimension);
		addToPanel(new JLabel("Community write"), inputPanel, standarDimension);
		addToPanel(communityWriteArea = new JTextArea("si2019"), inputPanel, standarDimension);
		addButton = new JButton("Dodaj ruter");
		addButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					ArrayList<String> tmpArrayList = new ArrayList<String>();
					ArrayList<Reader> tmpReaders = new ArrayList<Reader>();
					int port = Integer.parseInt(portArea.getText());
					session = new SnmpSession(addressArea.getText(),port,communityReadArea.getText(), communityWriteArea.getText(),SnmpConst.SNMPV2);
					session.setTimeout(10000);
					SnmpPdu tmpPdu = session.snmpGetRequest(oidNumInterfaces);
					SnmpVarBind varBind = tmpPdu.getVarBind(0);
					int numInterfaces=Integer.parseInt(varBind.getValue().toString());;
					for(int i=1;i<=numInterfaces;i++)
					{
						tmpPdu = session.snmpGetRequest(oidDescription+i);
						varBind = tmpPdu.getVarBind(0);
						String description = varBind.getValue().toString();
						if(description.equals("No Such Instance"))
							numInterfaces++;
						else if(!description.equals("Null0"))
						{
							tmpPdu=session.snmpGetRequest(oidStatus+i);
							varBind = tmpPdu.getVarBind(0);
							int status = Integer.parseInt(varBind.getValue().toString());
							if(status==1)
							{
								tmpArrayList.add(description);
								tmpReaders.add(new Reader(addressArea.getText(), port, communityReadArea.getText(), communityWriteArea.getText(),i));
							}
						}
						else 
							break;
					}
					for(Reader tmpReader:tmpReaders)
						tmpReader.start();
					interfaceNameLists.add(tmpArrayList);
					interfacesLists.add(tmpReaders);
					routerBox.addItem(addressArea.getText());
					routerBox.setEnabled(true);
					interfaceBox.setEnabled(true);
				}catch (TimeoutException e) {
					System.out.println("Timeout");
				}catch (Exception e) {
					System.out.println(e.getStackTrace());
					if(session!=null)
						session.close();
				}
			}
		});
		inputPanel.add(addButton);
		addToPanel(new JLabel(), inputPanel, new Dimension(inputPanel.getWidth(),0));
		routerBox = new JComboBox<String>();
		routerBox.setEnabled(false);
		routerBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
			          int index = routerBox.getSelectedIndex();
			          interfaceBox.removeAllItems();
			          for(String tmpString:interfaceNameLists.get(index))
			        	  interfaceBox.addItem(tmpString);
				}
			}
		});
		addToPanel(routerBox, inputPanel, new Dimension(inputPanel.getWidth()/2-5,inputPanel.getHeight()/25));
		interfaceBox = new JComboBox<String>();
		interfaceBox.setEnabled(false);
		interfaceBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					Reader tmpReader = protokGraf.getInterfaceReader();
					if(tmpReader != null)
						tmpReader.setSelected(false);
					tmpReader = interfacesLists.get(routerBox.getSelectedIndex()).get(interfaceBox.getSelectedIndex());
					protokGraf.setInterfaceReader(tmpReader);
					paketiGraf.setInterfaceReader(tmpReader);
					tmpReader.setSelected(true);
					protokGraf.Signal();
					paketiGraf.Signal();
				}
			}
		});
		addToPanel(interfaceBox, inputPanel, new Dimension(inputPanel.getWidth()/2-5,inputPanel.getHeight()/25));
		
		
		grafPanel = new JPanel();
		grafPanel.setSize(getWidth()*2/3,getHeight()-50);
		grafPanel.setPreferredSize(grafPanel.getSize());
		standarDimension = new Dimension(grafPanel.getWidth(),grafPanel.getHeight()/2-10);
		addToPanel(protokGraf = new Graf(true), grafPanel, standarDimension);
		addToPanel(paketiGraf = new Graf(false), grafPanel, standarDimension);
		protokLabel = new JLabel();
		paketiLabel = new JLabel();
		protokLabel.setPreferredSize(interfaceBox.getPreferredSize());
		paketiLabel.setPreferredSize(interfaceBox.getPreferredSize());	
		protokGraf.setTekstLabel(protokLabel);
		paketiGraf.setTekstLabel(paketiLabel);
		inputPanel.add(paketiLabel);
		inputPanel.add(protokLabel);
		
		add(inputPanel);
		add(grafPanel);
		setLocationRelativeTo(null);
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
		        for(ArrayList<Reader> tmpList:interfacesLists)
		        	for(Reader tmpReader:tmpList)
		        		tmpReader.close();
		    }
		});
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
		prozorMain = this;
	}
	
	
	
	public Graf getProtokGraf() {
		return protokGraf;
	}

	public Graf getPaketiGraf() {
		return paketiGraf;
	}

	public static void main(String[] args) {
		new Main();
	}

}
