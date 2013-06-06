Barometer Altitude and Floor Prediction
================================

An Android test application that uses the device barometer to predict the altitude and a building's floor level.

# Features

## Altitude prediction

Altitude is predicted according to device pressure and sea level pressure. The altitude is calculated according to the [Barometric Formula](http://hyperphysics.phy-astr.gsu.edu/hbase/kinetic/barfor.html)

![Altitude prediction][1]

*Results*: Not accurate. Method will not be used

## Sealevel pressure

Sealevel pressure according to the nearest weather station. The weather info is retrieved form [yr.no](http://yr.no).  

![Altitude prediction][2]

*Results*: The retrieved sealevel pressures are not accurate enough for the Barometric Formula

## Floor prediction

Building floor is predicted according to pressures registered on other floors. The floor is predicted based on the average floor pressure. 

![Floor prediction][3]

### Register floor pressure

The high, low, and average floor pressure (over 5 sec.) is registered for a floor. The data is stored in the phone's internal database. 

![Register floor pressure][4]

### Sync data to Dropbox

The application can be linked to Dropbox and the registered floor pressures can be pushed to the account. 

![Sync data to Dropbox][5]

### Floor measurements

[Floor measurements](https://github.com/Skarbo/BarometerAltitudeFloorPrediction/blob/master/floor_measurements.html) for analyzing the floors pressure data.

![8] ![6] ![7]

*Results*: A floor prediction can be done by having two base stations at two different floors (preferably top and bottom floor). The base stations measures the current pressure for its floor. The device predicts the floor according to the measurements and its current pressure. Tests show that the floor can be predicted with low error margin using a method like this. 

 [1]: https://lh4.googleusercontent.com/-_i4XDdZbojg/UbCcfLuniwI/AAAAAAAACU0/75kWyQ5XY-c/s400/Screenshot_2013-06-06-16-26-20.png
 [2]: https://lh3.googleusercontent.com/-YaSHm0XVgKc/UbCchQLZTTI/AAAAAAAACU8/r6CAHUg5Y-I/s400/Screenshot_2013-06-06-16-26-26.png
 [3]: https://lh5.googleusercontent.com/-nQ5TkjdpDAg/UbCck9fh3uI/AAAAAAAACVM/P6pUicsVo_g/s400/Screenshot_2013-06-06-16-26-45.png
 [4]: https://lh5.googleusercontent.com/-m6zhsnwIc9I/UbCci1VV1SI/AAAAAAAACVE/_Lq_iDgD31g/s400/Screenshot_2013-06-06-16-27-29.png
 [5]: https://lh4.googleusercontent.com/-wtNqJadI6zM/UbCcmCSvZ6I/AAAAAAAACVU/Ft3zWst5XrM/s400/Screenshot_2013-06-06-16-26-54.png
 [6]: http://i.imgur.com/bv62GiP.png
 [7]: http://i.imgur.com/f2rsKwb.png
 [8]: http://i.imgur.com/rzYG839.png
