package com.ericsson.de.ChartGen;

import java.util.Map;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;


public class chartGenerator extends JFrame {

    private static final long serialVersionUID = 1L;
    
    
    public chartGenerator(double following, double notFollowing) {
        super("Results");
      
        PieDataset dataset = createDataset(following,notFollowing);
        JFreeChart chart = createChart(dataset, "Results");
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 540));
        setContentPane(chartPanel);

    }

    public chartGenerator(Map<String, Integer> resultMap) {
    	super("Results");
        
        PieDataset dataset = createDataset(resultMap);
        JFreeChart chart = createChart(dataset, "Results");
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 540));
        setContentPane(chartPanel);
	}

	/**
     * Creates a sample dataset
     */
    private  PieDataset createDataset(double following, double notFollowing) {
        DefaultPieDataset result = new DefaultPieDataset();
        result.setValue("Following guildlines " + following +"%", following);
        result.setValue("Not following guildlines "+ notFollowing +"%", notFollowing);
        return result;
    }
    private  PieDataset createDataset(Map<String, Integer> resultMap) {
        DefaultPieDataset result = new DefaultPieDataset();
        for(Map.Entry<String, Integer> entery :resultMap.entrySet() )
        {
        result.setValue(entery.getKey() , entery.getValue());
        }
        return result;
    }
    /**
     * Creates a chart
     */
    private JFreeChart createChart(PieDataset dataset, String title) {

        JFreeChart chart = ChartFactory.createPieChart3D(
            title,                  // chart title
            dataset,                // data
            true,                   // include legend
            true,
            false
        );

        PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setStartAngle(290);
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(0.5f);
        return chart;

    }
}





     

