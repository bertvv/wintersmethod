package wintersmethod;

/**
 * This class implements Winter's method for forecasting. This method can be
 * used seasonal sales forecasting.
 * 
 * <p>Winter's method tries to fit observations into the sales model</p>
 * 
 * <blockquote>
 *   <p><em>x<sub>t</sub></em> = (<em>a</em> + <em>b</em> . <em>t</em>) . 
 *     <em>c<sub>t</sub></em> + <em>u<sub>t</sub></em><p>
 * </blockquote>
 * 
 * <p>Where:
 * <ul>
 *   <li><em>t</em> denotes a time period;</li>
 *   <li><em>x<sub>t</sub></em> is the sales volume in period <em>t</em>;</li>
 *   <li><em>a</em> and <em>b</em> are linear trend parameters;</li>
 *   <li><em>c<sub>t</sub></em> are seasonal coefficients in- or decreasing the 
 *     trend;</li>
 *   <li><em>u<sub>t</sub></em> is random noise.
 * </ul></p>
 * 
 * <p>Winter's method uses exponential smoothing to incrementally improve 
 * estimates of the parameters given above:</p>
 * 
 * <blockquote>
 *   <p>new forecast := <em>sc</em> . latest observation + (1 - <em>sc</em>) .
 *     last forecast</p>
 * </blockquote>
 * 
 * <p>where <em>sc</em> is a smoothing constant from the interval ]0, 1[ that 
 * determines the weight of new observations. For every parameter, a different
 * smoothing constant can be used.
 * 
 * <h4>References</h4>
 * 
 * <ul>
 * <li>Herbert Meyr, <em>Forecast Methods</em>, in "Supply Chain Management and
 *   Advanced Planning, Concepts, Models, Software, and Case Studies", 4th 
 *   Edition, H. Stadtler and C. Kilger (eds.), Springer (2008).</li>
 * </ul>
 * 
 * @author Bert Van Vreckem <a href="mailto:Bert.Van.Vreckem@vub.ac.be">Bert
 * Van Vreckem</a>.
 */
public class WintersMethodForecaster {
    
    //--------------------------------------------------------------------------
    // Attributes
    //--------------------------------------------------------------------------
    
    private double trendParameterA;

    public double[] getSeasonalCoefficients() {
        return seasonalCoefficients;
    }

    public double getTrendParameterA() {
        return trendParameterA;
    }

    public double getTrendParameterB() {
        return trendParameterB;
    }
    private double trendParameterB;
    private double[] seasonalCoefficients;
    int seasonalCyclePosition;
    private int seasonalCycleLength;
    
    private double smoothingConstantA;
    private double smoothingConstantB;
    private double smoothingConstantC;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /**
     * Creates a new forecasting object with the specified estimated parameters.
     * Use this constructor if you have not enough observations to bootstrap
     * initial estimations.
     * 
     * @param a initial estimate for trend parameter <em>a</em>.
     * @param b initial estimate for trend parameter <em>b</em>.
     * @param c initial estimates for the seasonal coefficients. If you observe
     *     a seasonal cycle of <em>T</em> time periods, you need as many 
     *     estimates
     * @param sca the smoothing constant for trend parameter <em>a</em>.
     *     This should be a value in ]0, 1[. Generally, [0.002, 0.51] is
     *     recommended.
     * @param scb the smoothing constant for trend parameter <em>b</em>.
     *     This should be a value in ]0, 1[. Generally, [0.005, 0.176] is
     *     recommended.
     * @param scc the smoothing constant for seasonal coefficients <em>c</em>.
     *     This should be a value in ]0, 1[. Generally, [0.05, 0.5] is
     *     recommended.
     */
    public WintersMethodForecaster(double a, double b, double[] c, 
            double sca, double scb, double scc) {
        initializeParameters(a, b, c, sca, scb, scc);
    }
    
    /**
     * Creates a new forecasting object with the specified history of sales 
     * volumes and seasonal cycle length.
     * 
     * @param aSalesVolumes array containing observed sales volumes for a number
     * of time periods.
     * @param aSeasonalCycleLength length of the seasonal cycle.
     */
    public WintersMethodForecaster(double[] aSalesVolumes, 
            int aSeasonalCycleLength, 
            double sca, double scb, double scc) {
        
        // Determine seasonal coefficients
        double[] movingAverages = centralMovingAverages(aSalesVolumes, 
                aSeasonalCycleLength);
        double[] osc = observedSeasonalCoefficients(aSalesVolumes, 
                movingAverages, aSeasonalCycleLength);
        double[] initialSeasonalCoefficients = 
                averageSeasonalCoefficients(osc, aSeasonalCycleLength);
        normalize(initialSeasonalCoefficients);
        
        // Determine trend parameters
        double[] trendPars = trendParameters(aSalesVolumes, 
                initialSeasonalCoefficients);
        
        initializeParameters(trendPars[0], trendPars[1],
            initialSeasonalCoefficients, sca, scb, scc);
    }

    //--------------------------------------------------------------------------
    // Public methods
    //--------------------------------------------------------------------------
    
    /**
     * Add the next observed sales volume to the model and update trend
     * parameters.
     * 
     * @param aSalesVolume the next observed sales volume.
     */
    public void addObservation(double aSalesVolume) {
        
        // Update trend parameter a
        double newA = smoothingConstantA * aSalesVolume / 
                seasonalCoefficients[seasonalCyclePosition] +
                (1 - smoothingConstantA) * (trendParameterA + trendParameterB);
        
        // Update trend parameter b
        double newB = smoothingConstantB * (newA - trendParameterA) +
                (1 - smoothingConstantB) * trendParameterB;
        
        // Update seasonal coefficient of the current time period in the 
        // seasonal cycle.
        double newC = smoothingConstantC * aSalesVolume / newA +
                (1 - smoothingConstantC) * 
                seasonalCoefficients[seasonalCyclePosition];
        
        trendParameterA = newA;
        trendParameterB = newB;
        seasonalCoefficients[seasonalCyclePosition] = newC;
        seasonalCyclePosition = (seasonalCyclePosition + 1) % 
                seasonalCycleLength;
    }
    
    /**
     * Returns the forecasted value for the next time period.
     * @return the forecasted value for the next time period.
     */
    public double getNextForecast() {
        
        //return (trendParameterA + trendParameterB) * 
        //        seasonalCoefficients[seasonalCyclePosition];
        
        return getForecast(1);
    }
    
    /**
     * Returns the forecasted sales value for the specified time period in the
     * future. 
     * 
     * @param aTimePeriod a nonnegative integer specifying a time period in the
     * future (the current time period is 0).
     * @return the forecasted sales value for the specified time period in the
     * future. 
     */
    public double getForecast(int aTimePeriod) {
        return (trendParameterA + trendParameterB * aTimePeriod) 
                * seasonalCoefficients[(seasonalCyclePosition + aTimePeriod - 1) 
                                        % seasonalCycleLength];
    }
    
    //--------------------------------------------------------------------------
    // Helper methods
    //--------------------------------------------------------------------------
    
    /**
     * Initialises all model parameters.
     */
    private void initializeParameters(double a, double b, double[] c, 
            double sca, double scb, double scc) {
        // Parameter checking
        checkSmoothingConstant(sca);
        smoothingConstantA = sca;
        checkSmoothingConstant(scb);
        smoothingConstantB = scb;
        checkSmoothingConstant(scc);
        smoothingConstantC = scc;
        
        // Set trend parameters
        trendParameterA = a;
        trendParameterB = b;
        
        // Set seasonal coefficients
        seasonalCoefficients = c;
        seasonalCycleLength = c.length;
        seasonalCyclePosition = 0;
        
    }
    
    /**
     * Checks whether the specified smoothing constant lies within the interval
     * ]0, 1[. When this is the case, the method returns normally. If not, an
     * exception is thrown.
     * 
     * @param aSmoothingConstant a smoothing constant.
     * @throws IllegalArgumentException if the specified smoothing constant does
     * not lie within the interval ]0, 1[.
     */
    private static void checkSmoothingConstant(double aSmoothingConstant) {
        if (! (aSmoothingConstant > 0 && aSmoothingConstant < 1)) {
            throw new IllegalArgumentException(
                "Smoothing constant should be a value between 0 and 1, but was"
                + aSmoothingConstant);
        }
    }

    /**
     * Returns an array with central moving averages for the specified sample 
     * with a specified period. This is a helper method used by the 
     * {@link #WintersMethodForecaster(double[],int)} constructor.
     * 
     * @param sample sample array for which the moving averages should be
     * calculated.
     * @param period period used for calculating averages.
     * @return the central moving averages for the specified sample.  When the
     * moving average for a certain time period can't be calculated,
     * the corresponding array element contains <code>Double.NaN</code>.
     */
    static double[] centralMovingAverages(double[] sample, int period) {
        
        int i;
        double[] ma = new double[sample.length];
        int halfPeriod = period / 2;
        double movingSum = 0.;
        
        for (i = 0; i < period; i++) {
            ma[i] = Double.NaN;
            movingSum += sample[i];
        }
        
        for (i = halfPeriod; i < sample.length - halfPeriod - 1; i++) {
            ma[i] = movingSum / period;
            movingSum = movingSum 
                    - sample[i - halfPeriod] 
                    + sample[i + halfPeriod + 1];
        }
        
        ma[i] = movingSum / period;
        
        for(i = i + 1; i < sample.length; i++) {
            ma[i] = Double.NaN;
        }
        
        return ma;
    }
    
    /**
     * Calculates seasonal coefficients for the specified sales volumes,
     * deseasonalised volumes (calculated using moving averages) and seasonal
     * cycle length.  This is a helper method used by the 
     * {@link #WintersMethodForecaster(double[],int)} constructor.
     * 
     * @param aSalesVolumes observed sales volumes.
     * @param aMovingAverages deseasonalised sales volumes.
     * @param aSeasonalCycleLength seasonal cycle length.
     * @return array containing observed seasonal coefficients.
     */
    static double[] observedSeasonalCoefficients(double[] aSalesVolumes, 
            double[] aMovingAverages, int aSeasonalCycleLength) {
        
        double[] osc = new double[aSalesVolumes.length];
        
        for (int i = 0; i < osc.length; i++) {
           osc[i] = aSalesVolumes[i] / aMovingAverages[i];
        }
        
        return osc;
    }

    /**
     * Calculate average seasonal coefficients for each time period in a
     * seasonal cycle. This is a first estimate of seasonal coefficients.
     *  This is a helper method used by the 
     * {@link #WintersMethodForecaster(double[],int)} constructor.
     * 
     * @param aObservedSeasonalCoefficients observed seasonal coefficients
     * @param aSeasonalCycleLength the number of time periods that make up a
     * seasonal cycle.
     * @return average seasonal coefficients for each time period in a
     * seasonal cycle.
     */
    static double[] averageSeasonalCoefficients(
            double[] aObservedSeasonalCoefficients, int aSeasonalCycleLength) {
        
        double[] asc = new double[aSeasonalCycleLength];
        double sum;
        int numObservations;
        
        for (int t = 0; t < asc.length; t++) {
            sum = 0.;
            numObservations = 0;
            
            for (int i = t; i < aObservedSeasonalCoefficients.length; 
                 i += aSeasonalCycleLength) {
                
                if (! Double.isNaN(aObservedSeasonalCoefficients[i])) {
                    sum += aObservedSeasonalCoefficients[i];
                    numObservations++;
                }
            }
            asc[t] = sum / numObservations;
        }
        return asc;
    }
    
    /**
     * Normalises the elements of the specified array. That is, divide every
     * element in the array by a factor, so that the sum becomes the length of
     * the array. Normalisation is in-place. This is a helper method used by the 
     * {@link #WintersMethodForecaster(double[],int)} constructor.
     * 
     * @param aArray the array to be normalised.
     */
    static void normalize(double[] aArray) {
        
        // Step 1. calculate the sum
        int idx;
        double sum = 0.;
        
        for (idx = 0; idx < aArray.length; idx++) {
            sum += aArray[idx];
        }
        
        // Step 2. normalise
        for (idx = 0; idx < aArray.length; idx++) {
            aArray[idx] = aArray[idx] * aArray.length / sum;
        }
    }

    /**
     * Calculate estimates for the trend parameters for the specified historical 
     * sales volumes and seasonal coefficients.
     * 
     * @param aSalesVolumes observed sales volumes.
     * @param seasonalCoefficients seasonal coefficients.
     * @return an array of size 2, with on the first position trend parameter
     * <em>a</em> and on the second position trend parameter <em>b</em>.
     */
    static double[] trendParameters(double[] aSalesVolumes,
            double[] seasonalCoefficients) {
        
        double[] deseasonalizedSales = new double[aSalesVolumes.length];
        double averageDeseasonalizedSales = 0.;
        
        for (int i = 0; i < deseasonalizedSales.length; i++) {
            deseasonalizedSales[i] = aSalesVolumes[i] 
                    / seasonalCoefficients[i % seasonalCoefficients.length];
            averageDeseasonalizedSales += deseasonalizedSales[i];
        }
        
        // number of time periods for which sales volumes are available
        double numT = aSalesVolumes.length;
        // average value of time periods
        double averageT = (1 - numT) / 2;
        // average deseasonalised sales
        averageDeseasonalizedSales /= numT;
        
        // calculate trend parameter b
        double numerator = 0.;
        double denominator = 0;
        
        for (int idx = 0; idx < deseasonalizedSales.length; idx++) {    
            numerator += (idx - averageT)
                    * (deseasonalizedSales[idx] - averageDeseasonalizedSales);
            
            denominator += (idx + averageT) * (idx + averageT);
        }
        
        double estimatedB = numerator / denominator;
        double estimatedA = averageDeseasonalizedSales - estimatedB * averageT;
        return new double[] {estimatedA, estimatedB};
    }
}
