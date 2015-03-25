package wintersmethod;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the <code>WintersMethodForecaster</code> class.
 * @author bert
 */
public class WintersMethodForecasterTest {
    private static final double NAN = Double.NaN;
    
    private double[] saleVolumes = new double[] {
          4419.,     3821., 3754., 3910., 4363., 4518., 27.3333,
          6190.4761, 5755., 5352., 5540., 5650., 6143., 30.4666,
          5158.,     4779., 5464., 5828., 6714., 7872., 42.
        };
    
    /**
     * Test the ranges of the smoothing constant estimates
     */
    @Test(expected=IllegalArgumentException.class)
    public void smoothingRanges1() {
        WintersMethodForecaster wmf = new 
                WintersMethodForecaster(1., 1., new double[] {1.} , 0., .5, .5);
    }
    @Test(expected=IllegalArgumentException.class)
    public void smoothingRanges2() {
        WintersMethodForecaster wmf = new 
                WintersMethodForecaster(1., 1., new double[] {1.} , .5, 0., .5);
    }
    @Test(expected=IllegalArgumentException.class)
    public void smoothingRanges3() {
        WintersMethodForecaster wmf = new 
                WintersMethodForecaster(1., 1., new double[] {1.} , .5, .5, 0.);
    }
    @Test(expected=IllegalArgumentException.class)
    public void smoothingRanges4() {
        WintersMethodForecaster wmf = new 
                WintersMethodForecaster(1., 1., new double[] {1.} , 1., .5, .5);
    }
    @Test(expected=IllegalArgumentException.class)
    public void smoothingRanges5() {
        WintersMethodForecaster wmf = new 
                WintersMethodForecaster(1., 1., new double[] {1.} , .5, 1., .5);
    }
    @Test(expected=IllegalArgumentException.class)
    public void smoothingRanges6() {
        WintersMethodForecaster wmf = new 
                WintersMethodForecaster(1., 1., new double[] {1.} , .5, .5, 1.);
    }
    
    /**
     * Shoestore example from (Meyr, 2008). Test forecaster with given initial
     * estimates for model parameters.
     */
    @Test
    public void shoeStoreInitialParameters() {
        double a = 5849.0;
        double b = 123.3;
        double[] c = new double[] {
            1.245693, // Monday
            1.115265, // Tuesday
            1.088853, // ...
            1.135378,
            1.178552,
            1.229739,
            0.006520}; // Sunday
        double sca = .8;
        double scb = .8;
        double scc = .3;
        
        // When doing a forecast for two weeks, this should be the results
        // -> calculated using a spreadsheet.
        double[] expectedWeekForecast = new double[] {
        //  Mon      Tue      Wed      Thu      Fri      Sat      Sun
            7439.65, 6798.21, 6771.47, 7200.78, 7619.93, 8102.5,  43.73, // W4
            8514.81, 7760.79, 7711.26, 8180.74, 8637.14, 9163.89, 49.39 //W5
        };
        
        WintersMethodForecaster wmf = new 
                WintersMethodForecaster(a, b, c , sca, scb, scc);
        
        // Weekly forecast with current parameters
        for (int i = 0; i < expectedWeekForecast.length; i++) {
            assertEquals("index" + i,
                    expectedWeekForecast[i],
                    wmf.getForecast(i + 1),
                    1.);
        }

        // Day-by-day forecasts and updates
        assertEquals(7440., wmf.getNextForecast(), 1.);
        assertEquals(wmf.getNextForecast(), wmf.getForecast(1), 1.);
        assertEquals(0, wmf.seasonalCyclePosition);
        
        wmf.addObservation(8152.);
        assertEquals(7717., wmf.getNextForecast(), 1.);
        assertEquals(wmf.getNextForecast(), wmf.getForecast(1), 1.);
        assertEquals(1, wmf.seasonalCyclePosition);
        
        wmf.addObservation(7986.);
        assertEquals(8445., wmf.getNextForecast(), 1.);
        assertEquals(wmf.getNextForecast(), wmf.getForecast(1), 1.);
        assertEquals(2, wmf.seasonalCyclePosition);
        
        wmf.addObservation(8891.);
        assertEquals(10206., wmf.getNextForecast(), 1.);
        assertEquals(wmf.getNextForecast(), wmf.getForecast(1), 1.);
        assertEquals(3, wmf.seasonalCyclePosition);
        
        wmf.addObservation(11107.);
        assertEquals(13008., wmf.getNextForecast(), 1.);
        assertEquals(wmf.getNextForecast(), wmf.getForecast(1), 1.);
        assertEquals(4, wmf.seasonalCyclePosition);
        
        wmf.addObservation(12478.);
        assertEquals(14515., wmf.getNextForecast(), 1.);
        assertEquals(wmf.getNextForecast(), wmf.getForecast(1), 1.);
        assertEquals(5, wmf.seasonalCyclePosition);
        
        wmf.addObservation(14960.);
        assertEquals(88., wmf.getNextForecast(), 1.);
        assertEquals(wmf.getNextForecast(), wmf.getForecast(1), 1.);
        assertEquals(6, wmf.seasonalCyclePosition);
        
        wmf.addObservation(81.);
        assertEquals(0, wmf.seasonalCyclePosition);
    }
    
    /**
     * Shoestore example from (Meyr, 2008). Test forecaster with given sale
     * volumes for three weeks.
     */
    @Test
    public void shoeStoreCalculateParameters() {
  
        WintersMethodForecaster wmf = 
                new WintersMethodForecaster(saleVolumes, 7, .8, .8, .3);
        
        assertEquals(wmf.getNextForecast(), wmf.getForecast(1), 1.);
        assertEquals(7440., wmf.getNextForecast(), 1.);
        assertEquals(0, wmf.seasonalCyclePosition);
        
        wmf.addObservation(8152.);
        assertEquals(7717., wmf.getNextForecast(), 1.);
        assertEquals(wmf.getNextForecast(), wmf.getForecast(1), 1.);
        assertEquals(1, wmf.seasonalCyclePosition);
        
        wmf.addObservation(7986.);
        assertEquals(8445., wmf.getNextForecast(), 1.);
        assertEquals(wmf.getNextForecast(), wmf.getForecast(1), 1.);
        assertEquals(2, wmf.seasonalCyclePosition);
        
        wmf.addObservation(8891.);
        assertEquals(10206., wmf.getNextForecast(), 1.);
        assertEquals(wmf.getNextForecast(), wmf.getForecast(1), 1.);
        assertEquals(3, wmf.seasonalCyclePosition);
        
        wmf.addObservation(11107.);
        assertEquals(13008., wmf.getNextForecast(), 1.);
        assertEquals(wmf.getNextForecast(), wmf.getForecast(1), 1.);
        assertEquals(4, wmf.seasonalCyclePosition);
        
        wmf.addObservation(12478.);
        assertEquals(14515., wmf.getNextForecast(), 1.);
        assertEquals(wmf.getNextForecast(), wmf.getForecast(1), 1.);
        assertEquals(5, wmf.seasonalCyclePosition);
        
        wmf.addObservation(14960.);
        assertEquals(88., wmf.getNextForecast(), 1.);
        assertEquals(wmf.getNextForecast(), wmf.getForecast(1), 1.);
        assertEquals(6, wmf.seasonalCyclePosition);
        
        wmf.addObservation(81.);
        assertEquals(0, wmf.seasonalCyclePosition);
    }

    @Test
    public void centralMovingAverages() {
        
        double[] expectedMas = new double[] {
            NAN,    NAN,    NAN,    3544.6, 3797.7, 4074.0, 4302.3,
            4535.1, 4719.0, 4951.1, 4951.6, 4804.1, 4664.6, 4680.6, 
            4721.8, 4873.8, 5120.8, 5122.4, NAN,    NAN,    NAN
        };
        double[] mas = WintersMethodForecaster.centralMovingAverages(
                saleVolumes, 7);
        
        assertEquals(saleVolumes.length, mas.length);
        assertArrayEquals(expectedMas, mas, .1);
        
    }
    
    @Test
    public void observedSeasonalCoefficients() {
        
        int cycleLength = 7;
        double[] mas = WintersMethodForecaster.centralMovingAverages(
                saleVolumes, 7);
        double[] expectedOsc = new double[] {
            NAN,    NAN,    NAN,    1.1031, 1.1489, 1.1090, 0.0064,
            1.3650, 1.2195, 1.0810, 1.1188, 1.1761, 1.3169, 0.0065,
            1.0924, 0.9806, 1.0670, 1.1377, NAN,    NAN,    NAN
        };
        double[] actualOsc = 
                WintersMethodForecaster.observedSeasonalCoefficients(
                    saleVolumes, mas, cycleLength);
        
        assertArrayEquals(expectedOsc, actualOsc, .0001);
    }
    
    @Test
    public void averageSeasonalCoefficients() {
        
        double[] osc = new double[] {
            NAN,    NAN,    NAN,    1.1031, 1.1489, 1.1090, 0.0064,
            1.3650, 1.2195, 1.0810, 1.1188, 1.1761, 1.3169, 0.0065,
            1.0924, 0.9806, 1.0670, 1.1377, NAN,    NAN,    NAN
        };
        double[] expectedAsc = new double[] {
            1.2287, 1.10005, 1.0740, 1.1199, 1.1625, 1.21295, 0.00645 
        };
        double[] actualAsc = 
                WintersMethodForecaster.averageSeasonalCoefficients(osc, 7);
        //System.out.println(java.util.Arrays.toString(actualAsc));
        assertArrayEquals(expectedAsc, actualAsc, .0001);
    }
    
    @Test
    public void normalise() {
        double[] input = new double[] {
            1.2287, 
            1.1000, 
            1.0740,
            1.1199, 
            1.1625, 
            1.2130, 
            0.0064
        };
        double[] expected = new double[] {
            1.2457, // Monday
            1.1153, // Tuesday
            1.0889, // ...
            1.1354,
            1.1786,
            1.2297,
            0.0065}; // Sunday
        
        WintersMethodForecaster.normalize(input);
        //System.out.println(java.util.Arrays.toString(input));
        double sum = 0.;
        for (int i = 0; i < input.length; i++) {
            sum += input[i];
        }
        
        // The sum of normalised seasonal coefficients should be equal to the
        // seasonal cycle length.
        assertEquals(input.length, sum, .0001);
        
        assertArrayEquals(expected, input, .001);
    }
    
    @Test
    public void trendParameters() {
        double[] saleVolumes = new double[] {
            4419.,     3821., 3754., 3910., 4363., 4518., 27.3333,
            6190.4761, 5755., 5352., 5540., 5650., 6143., 30.4666,
            5158.,     4779., 5464., 5828., 6714., 7872., 42.
        };
        double[] seasonalCoefficients = new double[] {
            1.2457, // Monday
            1.1153, // Tuesday
            1.0889, // ...
            1.1354,
            1.1786,
            1.2297,
            0.0065}; // Sunday
        double[] trendPars = 
                WintersMethodForecaster.trendParameters(saleVolumes, 
                    seasonalCoefficients);
        
        assertEquals(2, trendPars.length);
        assertEquals(5849, trendPars[0], 5.);
        assertEquals(123.3, trendPars[1], 1.);
    }
    
    private static void assertArrayEquals(
            double[] expected, double[] actual, double tolerance) {
        
        assertEquals(expected.length, actual.length);
        
        for (int i = 0; i < actual.length; i++) {
            assertEquals("position "+ i, expected[i], actual[i], tolerance);
        }
    }
}
