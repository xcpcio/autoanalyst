package charts;

import java.awt.Font;
import java.util.List;

import model.Contest;
import model.InitialSubmission;
import model.Problem;
import model.Judgement;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class SubmissionsPerProblem implements ContestChart {

	private CategoryDataset getDataset(Contest contest, int currentTime) {
		String totalSeries = "Total";
		String acceptedSeries = "Accepted";
		int cutoff=30;
		
		DefaultCategoryDataset target = new DefaultCategoryDataset();
		List<Judgement> submissions = contest.getSubmissions();
		for (Problem p : contest.getProblems()) {
			int total = 0;
			int accepted = 0;
			for (Judgement s : submissions) {
				InitialSubmission initialSubmission = s.getInitialSubmission();
				if (s.getProblem() == p && initialSubmission.minutesFromStart+cutoff > currentTime && initialSubmission.minutesFromStart<=currentTime) {
					if (s.isAccepted()) {
						accepted++;
					}
					total++;
				}
			}
			target.addValue(total, totalSeries, p.getLabel());
			target.addValue(accepted, acceptedSeries, p.getLabel());
		}
		return target;
	}
	
	@Override
	public JFreeChart createChart(Contest contest, int currentTime) {
	    	JFreeChart chart = ChartFactory.createBarChart("Submissions last 30 minutes", "Problem", "Submissions", getDataset(contest, currentTime), PlotOrientation.VERTICAL, true, false, false);
	    	Font font = new Font("Sans-serif", Font.BOLD, 12); 
	    	chart.getTitle().setFont(font);
	    	CategoryPlot plot = chart.getCategoryPlot();
	    	//plot.getDomainAxis().setCategoryMargin(0.07d);

	    	NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
	    	rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	    	
	    	
	    	return chart;
	}

	@Override
	public String getName() {
		return "SubsPerProblem";
	}

}
