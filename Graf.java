package projekat;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.text.CollationElementIterator;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class Graf extends JPanel{

	private static final int padding = 10;
	private static final int minYValue = 10;
	private static final int yCount = 10;
	private static final Stroke graphStroke = new BasicStroke(3f);
	private static final int widthLine = 10;
	private static final int graphPointWidth = 8;
	private Reader interfaceReader = null;
	private boolean protok;
	private JLabel tekstLabel = null;
	
	public Graf(boolean protok) {
		super();
		this.protok=protok;
		setBackground(Color.white);
	}
	
	@Override
	public void paint(Graphics arg0) {
		super.paint(arg0);
		if(interfaceReader == null)
			return;
		
		ArrayList<Integer> tmpList;
		ArrayList<Integer> dataArrayList;
		int max = minYValue;

		Color graphColor;
		Color graphPointColor;
		synchronized (interfaceReader) {
			if(protok)
			{
				tmpList = interfaceReader.GetProtok();
				graphColor = Color.black;
				graphPointColor = Color.blue;
			}
			else
			{
				tmpList = interfaceReader.GetPaketi();
				graphColor = Color.gray;
				graphPointColor = Color.red;
			}
			dataArrayList = new ArrayList<Integer>(tmpList);
		}
		for(Integer tmpInteger:dataArrayList)
			if(tmpInteger >= max)
				max = tmpInteger + 1;
		
		Graphics2D g2 = (Graphics2D)arg0;
	    double xScale = ((double) getWidth() - 2 * padding) / (Reader.maxPoints - 1);
	    double yScale = ((double) getHeight() - 2 * padding) / (max - 1);	    
	    g2.drawLine(padding, getHeight() - padding, padding, padding);
	    g2.drawLine(padding, getHeight() - padding, getWidth() - padding, getHeight() - padding);
	    int x, y;
	    for (int i = 0; i < yCount; i++) {
	    	x = padding;
	    	y = getHeight() - (((i + 1) * (getHeight() - padding * 2)) / (yCount - 1) + padding);
	    	g2.drawLine(x, y, x + widthLine, y);
	    }
	    for (int i = 0; i < Reader.maxPoints - 1; i++) {
	    	x = (i + 1) * (getWidth() - padding * 2) / (Reader.maxPoints - 1) + padding;
	    	y = getHeight() - padding;
	    	g2.drawLine(x, y, x, y - widthLine);
	    }	    
		ArrayList<Point> graphPoints = new ArrayList<Point>();
		for (int i = 0; i < dataArrayList.size(); i++) {
			x = (int) (i * xScale + padding);
			y = (int) (((max - 1) - dataArrayList.get(i)) * yScale + padding);
			graphPoints.add(new Point(x, y));
		}		
	    Stroke oldStroke = g2.getStroke();
	    g2.setColor(graphColor);
	    g2.setStroke(graphStroke);
	    for (int i = 0; i < graphPoints.size() - 1; i++)
	    	g2.drawLine(graphPoints.get(i).x, graphPoints.get(i).y, graphPoints.get(i + 1).x, graphPoints.get(i + 1).y);
	    g2.setStroke(oldStroke);      
	    g2.setColor(graphPointColor);
	    for (int i = 0; i < graphPoints.size(); i++) {
	    	x = graphPoints.get(i).x - graphPointWidth / 2;
	    	y = graphPoints.get(i).y - graphPointWidth / 2;
	    	g2.fillOval(x, y, graphPointWidth, graphPointWidth);
	    }
	    
	    if(tekstLabel != null && dataArrayList.size() != 0){
	    	if(protok)
	    		tekstLabel.setText("Protok:"+dataArrayList.get(dataArrayList.size()-1));
		    else
		    	tekstLabel.setText("Paketi:"+dataArrayList.get(dataArrayList.size()-1));
	    }
	}
	
	public synchronized void setInterfaceReader(Reader interfaceReader) {
		this.interfaceReader = interfaceReader;
	}

	public Reader getInterfaceReader() {
		return interfaceReader;
	}

	public synchronized void Signal()
	{
		repaint();
	}

	public JLabel getTekstLabel() {
		return tekstLabel;
	}

	public void setTekstLabel(JLabel tekstLabel) {
		this.tekstLabel = tekstLabel;
	}

}
