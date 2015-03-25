# Winter's method for forecasting

This class implements Winter's method for forecasting.This method can be
used seasonal sales forecasting.

Winter's method tries to fit observations into the sales model

  *x<sub>t</sub>* = (*a* + *b* . *t*) . *c<sub>t</sub>* + *u<sub>t</sub>*

Where:

* *t* denotes a time period;
* *x<sub>t</sub>* is the sales volume in period *t*;
* *a* and *b* are linear trend parameters;
* *c<sub>t</sub>* are seasonal coefficients in- or decreasing the
    trend;
* *u<sub>t</sub>* is random noise.

Winter's method uses exponential smoothing to incrementally improve estimates of the parameters given above:

  new forecast := *sc* . latest observation + (1 - *sc*) . last forecast

where *sc* is a smoothing constant from the interval ]0, 1[ that
determines the weight of new observations. For every parameter, a different
smoothing constant can be used.

<h4>References</h4>

* Herbert Meyr, *Forecast Methods*, in "Supply Chain Management and
  Advanced Planning, Concepts, Models, Software, and Case Studies", 4th
  Edition, H. Stadtler and C. Kilger (eds.), Springer (2008).

### Author

Bert Van Vreckem (bert.vanvreckem@gmail.com)

### License

BSD 2-clause license
