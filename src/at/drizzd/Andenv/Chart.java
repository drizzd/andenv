package at.drizzd.Andenv;

import android.app.Activity;

import org.achartengine.GraphicalView;
import org.achartengine.ChartFactory;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class Chart {
    private GraphicalView mChart;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private XYSeries mCurrentSeries;
    private XYSeriesRenderer mCurrentRenderer;
    private Activity mActivity;
    private double mGraphWidth;

    public Chart(Activity activity, double graphWidth) {
        mActivity = activity;
        mGraphWidth = graphWidth;

        mRenderer.setPanEnabled(false, false);
        mRenderer.setZoomEnabled(false, false);
        mRenderer.setClickEnabled(false);
        mRenderer.setShowGridX(true);

        mCurrentSeries = new XYSeries("Sample Data");
        mDataset.addSeries(mCurrentSeries);

        mCurrentRenderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(mCurrentRenderer);

        mChart = ChartFactory.getLineChartView(mActivity, mDataset, mRenderer);
    }

    public GraphicalView getView() {
        return mChart;
    }

    public void publishUpdate(double x, double y) {
        mActivity.runOnUiThread(new PublishUpdate(x, y));
    }

    class PublishUpdate implements Runnable {
        double mX;
        double mY;

        public PublishUpdate(double x, double y) {
            mX = x;
            mY = y;
        }

        public void run() {
            mCurrentSeries.add(mX, mY);

            double stepSize = 10;
            double max = mCurrentSeries.getMaxY();
            max = stepSize*Math.ceil((max+1)/stepSize);
            double min = mCurrentSeries.getMinY();
            min = stepSize*Math.floor((min-1)/stepSize);

            mRenderer.setRange(new double[] {mX-mGraphWidth, mX, min, max});
            mChart.repaint();
        }
    };
};
