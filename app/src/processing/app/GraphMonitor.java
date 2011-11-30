/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
//TODO: change to Graphmonitor it is still a copy of serialmonitor, also create 1 virtual class for handling serial displaying.
package processing.app;

import processing.app.debug.MessageConsumer;
import processing.core.*;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import org.jfree.chart.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeTableXYDataset;
public class GraphMonitor extends JFrame implements MessageConsumer {
  private Serial serial;
  private String port;
  private JTextArea textArea;
  private JTextArea graphArea;
  private JScrollPane scrollPane;
  private JScrollPane graphScrollPane;
  private GraphToolbar toolbar;
  private JTextField textField;
  private JButton sendButton;
  private JCheckBox autoscrollBox;
  private JComboBox lineEndings;
  private JComboBox serialRates;
  private int serialRate;
  private boolean start=false;
  private long starttime;
  private LinkedList <Character> stringdata;
  private Graph graph;
  private int hz=1000;
  public GraphMonitor(String port) {
    super(port);
  
    this.port = port;
    graph= new Graph();
    addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          closeSerialPort();
        }
      });  
      
    // obvious, no?
    KeyStroke wc = Editor.WINDOW_CLOSE_KEYSTROKE;
    getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(wc, "close");
    getRootPane().getActionMap().put("close", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        closeSerialPort();
        setVisible(false);
      }});
  
    getContentPane().setLayout(new BorderLayout());
    stringdata= new LinkedList<Character>();
    Font font = Theme.getFont("console.font");
    toolbar= new GraphToolbar(this,null);
    textArea = new JTextArea(16, 10);
    textArea.setEditable(false);    
    textArea.setFont(font);
    // don't automatically update the caret.  that way we can manually decide
    // whether or not to do so based on the autoscroll checkbox.
    ((DefaultCaret)textArea.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    textField = new JTextField(40);
    textField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hz=1000/Integer.valueOf(textField.getText()).intValue();
      }});
    scrollPane = new JScrollPane(textArea);
    JPanel pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    pane.setBorder(new EmptyBorder(4, 4, 4, 4));
    pane.add(graph.panel());
    pane.add(scrollPane);
    getContentPane().add(toolbar,BorderLayout.NORTH);
    getContentPane().add(pane, BorderLayout.CENTER);
    getContentPane().add(textField, BorderLayout.SOUTH);
    pack();
    
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    if (Preferences.get("last.screen.height") != null) {
      // if screen size has changed, the window coordinates no longer
      // make sense, so don't use them unless they're identical
      int screenW = Preferences.getInteger("last.screen.width");
      int screenH = Preferences.getInteger("last.screen.height");
      if ((screen.width == screenW) && (screen.height == screenH)) {
        String locationStr = Preferences.get("last.serial.location");
        if (locationStr != null) {
          int[] location = PApplet.parseInt(PApplet.split(locationStr, ','));
          setPlacement(location);
        }
      }
    }
  }
  
  protected void setPlacement(int[] location) {
    setBounds(location[0], location[1], location[2], location[3]);
  }

  protected int[] getPlacement() {
    int[] location = new int[4];

    // Get the dimensions of the Frame
    Rectangle bounds = getBounds();
    location[0] = bounds.x;
    location[1] = bounds.y;
    location[2] = bounds.width;
    location[3] = bounds.height;

    return location;
  }

  private void send(String s) {
    if (serial != null) {
      switch (lineEndings.getSelectedIndex()) {
        case 1: s += "\n"; break;
        case 2: s += "\r"; break;
        case 3: s += "\r\n"; break;
      }
      serial.write(s);
    }
  }
  
  public void openSerialPort() throws SerialException {
    if (serial != null) return;
  
    serial = new Serial(port, 9600);
    serial.addListener(this);
  }
  
  public void closeSerialPort() {
    if (serial != null) {
      int[] location = getPlacement();
      String locationStr = PApplet.join(PApplet.str(location), ",");
      Preferences.set("last.serial.location", locationStr);
      textArea.setText("");
      serial.dispose();
      serial = null;
    }
  }
  
  public void message(final String s) {
    //System.err.println(serial.readString());
    SwingUtilities.invokeLater(new Runnable() {//TODO implement 2 arrays for x and y values
      public void run() {
        if (start) {
          addtobuffer(s);
        }
        textArea.append(s);
        textArea.setCaretPosition(textArea.getDocument().getLength());
      }});
  }
  public void addtobuffer(String s){
    for (int i=0; i==(s.length()-1);i++ ) {
        stringdata.add(s.charAt(i));
    }
    
  }
  public void handleStart(){
    start=true;
    starttime=System.currentTimeMillis();
    //serial.write("****");
    //graph.reset();
    if (textField.getText()!="") {
      hz=1000/Integer.valueOf(textField.getText()).intValue();
      
    }
  }
  public void handleStop(){
    start=false;
    
    //serial.write("****");
    parse();
  }
  public void parse(){
    graph.reset();
    int count=0;
    String number="";
    char c;
    int isNumber=1;
    while(stringdata.size()!=0){
      while(isNumber==1){
         c=stringdata.poll();
         if(Character.isDigit(c)){
           number +=c;          
        }else{
           isNumber=0;
        }
      }
      if(number.length()>0){
        int temp= Integer.valueOf( number ).intValue();
        graph.add(new Millisecond(new Date(count*hz)),temp);
        number="";
        count++;
      }
      isNumber=1;
    }
    //textArea.append("====");
    
  }
}
