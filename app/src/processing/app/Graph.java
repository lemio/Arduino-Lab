package processing.app;
//import processing.core.*;

import processing.app.debug.MessageConsumer;


import java.io.*;
import java.util.*;
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


public class Graph {
  
  private TimeTableXYDataset data;
  private String name;
  public Graph(){
    data = new TimeTableXYDataset();
    name="a";
  }
  public void add(Millisecond y, int x){
    data.add(y,x,name);
  }
  public void reset(){
    // data=null;  //does not work :S
    // data=new TimeTableXYDataset();
  }
  public ChartPanel panel(){
    JFreeChart chart = ChartFactory.createXYLineChart(null, null, null,
        data, PlotOrientation.VERTICAL,
        false, false, false);
    ChartPanel panel = new ChartPanel(chart);
    
    return panel;
  }
}
