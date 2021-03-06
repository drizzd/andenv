package com.example.andenv;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;

import org.achartengine.GraphicalView;
import org.achartengine.ChartFactory;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class Chart {
    public static final String TAG = "Chart";
    private GraphicalView mChart;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private XYSeries mCurrentSeries;
    private XYSeriesRenderer mCurrentRenderer;
    private Activity mActivity;
    private double mGraphWidth;

    public Chart(Activity activity, double graphWidth, String curveTitle,
                 String xTitle, String yTitle) {
        mActivity = activity;
        mGraphWidth = graphWidth;

        mRenderer.setPanEnabled(false, false);
        mRenderer.setZoomEnabled(false, false);
        mRenderer.setClickEnabled(false);
        mRenderer.setShowGridX(true);
        if (xTitle != null) {
            mRenderer.setXTitle(xTitle);
        }
        if (yTitle != null) {
            mRenderer.setYTitle(yTitle);
        }

        mCurrentSeries = new XYSeries(curveTitle);
        mDataset.addSeries(mCurrentSeries);

        mCurrentRenderer = new XYSeriesRenderer();
        mCurrentRenderer.setLineWidth(2);
        mCurrentRenderer.setColor(Color.RED);
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
            Log.d(TAG, String.format("%f %f", mX, mY));

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