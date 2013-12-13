This Android application project detects two activities using accelerometer data obtained from mobile phones. 

The MainActivity class controls the application flow and responds to the user input, collects data and calls the necessary methods for data processing.

The AccDeviceView class draws the graph widget present in the UI.

The DetectActivity class comprises of methods to generate feature vectors and train a classifier to distinguish between two activities.

The activities chosen are sitting and jogging.


How the app works:

1. Place the phone in the right-side waist pocket on your leg.
2. Tap the 'Start' button to stream accelerometer data.
3. Do a jogging or sitting activity for 5 seconds.
4. Tap 'Detect' to see if the device got it right!


To use the activity detection functionality:

1. Create a new instance of the DetectActivity class
2. Call the detect method and pass the x-axis, y-axis, z-axis, and magnitude of the accelerometer signal arrays. (Include the activity label specified by the user as well in case of self-training).
3. To extend the functionality to recognize other activites, modify the 'computeFeatures' and 'createTrainingDataMat' methods to include different training data and generate their corresponding activity specific features.
4. In addition, the labels for the respective activities must be specified. 
