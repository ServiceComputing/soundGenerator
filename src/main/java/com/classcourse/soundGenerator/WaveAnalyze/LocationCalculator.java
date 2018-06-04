package com.classcourse.soundGenerator.WaveAnalyze;

/**
 * Created by sshhsh on 17-11-23.
 * Calculate the location base on distance differences
 */

public class LocationCalculator {
    private double[] nodeX;
    private double[] nodeY;
    private double[] dd;
    private int n; //number of distance difference
    private double[] result;
    private double[] f;//value of functions
    private final static double error = 1e-6;
    private final static double lambda = 0.05;

    /**
     * @param speakNum number of speakers
     */
    public LocationCalculator(int speakNum) {
        nodeX = new double[speakNum];
        nodeY = new double[speakNum];

        n = speakNum - 1;

        f = new double[n];
        result = new double[2];
    }

    public void updatePosition(double[] x, double[] y) {
        if (x.length != nodeX.length) return;
        for (int i = 0; i < x.length; ++i) {
            nodeX[i] = x[i];
            nodeY[i] = y[i];
        }
    }

    public double[] cal(double[] dd) {
        this.dd = dd;
        calLocation();
        return result;
    }

    private void calLocation() {
        double x = 0, y = 0;
        for (int i = 0; i < n + 1; ++i) {
            x += nodeX[i];
            y += nodeY[i];
        }
        x /= (n + 1);
        y /= (n + 1);

        double F = 56789999, previousF = 654656546;
        int count = 0;

        while (Math.abs(F - previousF) > error && count < 10000) {
            previousF = F;
            F = 0;
            for (int i = 0; i < n; ++i) {
                f[i] = functionF(i, x, y);
                F += f[i] * f[i];
            }

            double dFx = 0, dFy = 0;
            for (int i = 0; i < n; ++i) {
                dFx += (2 * f[i] * ((x - nodeX[i]) / getDistance(x, y, nodeX[i], nodeY[i])
                        - (x - nodeX[i + 1]) / getDistance(x, y, nodeX[i + 1], nodeY[i + 1])));
                dFy += (2 * f[i] * ((y - nodeY[i]) / getDistance(x, y, nodeX[i], nodeY[i])
                        - (y - nodeY[i + 1]) / getDistance(x, y, nodeX[i + 1], nodeY[i + 1])));
            }
            x -= lambda * dFx;
            y -= lambda * dFy;
            count++;
        }
        result[0] = x;
        result[1] = y;
    }

    /**
     * @param index the index of the function
     * @return the value of the function
     */
    private double functionF(int index, double x, double y) {
        return getDistance(x, y, nodeX[index], nodeY[index]) - getDistance(x, y, nodeX[index + 1], nodeY[index + 1]) - dd[index];
    }

    private double getDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}
