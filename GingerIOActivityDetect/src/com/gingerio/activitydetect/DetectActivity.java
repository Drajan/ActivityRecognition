package com.gingerio.activitydetect;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvSVM;
import org.opencv.ml.CvSVMParams;

import com.google.common.primitives.Floats;

public class DetectActivity {
	float[] x, y, z, mag;
	float[] labelArray;
	int sizeOfDataset = 10;
	int numOfFeatures = 40;
	Mat trainData = new Mat(sizeOfDataset, numOfFeatures, CvType.CV_32F);
	ArrayList<Float> labelList = new ArrayList<Float>();
	
	/*public DetectActivity(){
	}*/
	
	public void train(float[] x, float[] y, float[] z, float[] magnitude, float label){
		this.x = x;
		this.y = y;
		this.z = z;
		this.mag = magnitude;
		labelList.add(label);
		float[] feat = computeFeatures();
		createTrainingDataMat(feat);
		labelList.add((float) 0); // to indicate a sit activity
		labelArray = Floats.toArray(labelList);
		trainSVM();
	}
	
	public float[] computeFeatures(){
		int length = x.length;
		double  xavg, yavg, zavg, xstd, ystd, zstd, xabs, yabs, zabs, avgres;
		Mat feat = new Mat(length, 4, CvType.CV_32FC1);
		MatOfDouble mean = new MatOfDouble(1, 1, CvType.CV_32FC1);
		MatOfDouble stddev = new MatOfDouble(1, 1, CvType.CV_32FC1);	
		// create a data matrix of size 1xlength containing the respective axis' acceleration values.  
		Mat xMat = new Mat(1, length, CvType.CV_32FC1);
		Mat yMat = new Mat(1, length, CvType.CV_32FC1);
		Mat zMat = new Mat(1, length, CvType.CV_32FC1);
		Mat magMat = new Mat(1, length, CvType.CV_32FC1);
		
//		float data[][];// = {{1,2,3,4,5},{7,8,9,10,11}}; 
//		A = Mat(2, 5, CV_32FC1, &data);
		
	// Assign the matrix 
		xMat.put(0, 0, x);
		yMat.put(0, 0, y); 
		zMat.put(0, 0, z);
		magMat.put(0, 0, mag);
		
	// Compute the mean and standard deviation values for each axis. 
		Core.meanStdDev(xMat, mean, stddev);
		xavg = mean.toArray()[0]; // x-axis mean
		xstd = stddev.toArray()[0]; // x-axis std
		
		Core.meanStdDev(yMat, mean, stddev);
		yavg = mean.toArray()[0]; // y-axis mean
		ystd = stddev.toArray()[0]; // y-axis std
		
		Core.meanStdDev(zMat, mean, stddev);
		zavg = mean.toArray()[0]; // z-axis mean
		zstd = stddev.toArray()[0]; // z-axis std
		
	// Compute the average resultant magnitude
		Core.meanStdDev(magMat, mean, stddev);
		avgres = mean.toArray()[0];
		
	// Compute the average absolute difference
		float sumx = 0, sumy = 0, sumz = 0;
		for(int i = 0; i < length; i++){
			sumx += Math.abs(x[i] - xavg);
			sumy += Math.abs(y[i] - yavg);
			sumz += Math.abs(z[i] - zavg);
		}
		xabs = sumx/length; // average absolute difference of x-axis
		yabs = sumy/length; // average absolute difference of y-axis
		zabs = sumz/length; // average absolute difference of z-axis
		
	// Compute histogram of each axis vector by putting them into 10 equal intervals.
					
		float[] histX = computeHistogram(x, xMat);
		float[] histY = computeHistogram(y, yMat);
		float[] histZ = computeHistogram(z, zMat);
		
		int numFeatures = 40;
	//--------------------------------------------------------------------------------------------
		// Stack up all the features into an array.
		float[] featureVector = new float[numFeatures];
		System.arraycopy(histX, 0, featureVector, 0, 10);
		System.arraycopy(histY, 0, featureVector, 10, 10);
		System.arraycopy(histZ, 0, featureVector, 20, 10);
		featureVector[30] = (float)xavg;
		featureVector[31] = (float)yavg;
		featureVector[32] = (float)zavg;
		featureVector[33] = (float)xabs;
		featureVector[34] = (float)yabs;
		featureVector[35] = (float)zabs;
		featureVector[36] = (float)xstd;
		featureVector[37] = (float)ystd;
		featureVector[38] = (float)zstd;
		featureVector[39] = (float)avgres;
	//---------------------------------------------------------------------------------------------
		
        
        int x = 1;
		return featureVector;
	}
	
	public float[] computeHistogram(float[] inputData, Mat dataMat){
		int numbins = 10;
		float[] histo = new float[numbins];
		

		ArrayList<Mat> list = new ArrayList<Mat>();
        list.add(dataMat);
        MatOfInt channels = new MatOfInt(0);
        Mat hist= new Mat();
        MatOfInt histSize = new MatOfInt(10);
        float min = Floats.min(inputData);
        float max = Floats.max(inputData);
        MatOfFloat ranges = new MatOfFloat(min, max);
        Imgproc.calcHist(list, channels, new Mat(), hist, histSize, ranges);
        hist.get(0, 0, histo);
         
       /* double[] hi; double[] hmm = new double[10];
		for(int i = 0; i < 10; i++){
        	hi = hist.get(i, 0);
        	hmm[i] = hi[0];
		}
		
       
		float hai[] = new float[10];*/
		
		
		
		return histo;
	}
	
	public void trainSVM(){
		Mat responses = new Mat(1, sizeOfDataset, CvType.CV_32F);
		responses.put(0, 0, labelArray); 
		
		CvSVM svm = new CvSVM();
		CvSVMParams params = new CvSVMParams();
		params.set_svm_type(CvSVM.C_SVC);
		params.set_kernel_type(CvSVM.LINEAR);
		params.set_term_crit(new TermCriteria(TermCriteria.EPS, 100, 1e-6)); // use TermCriteria.COUNT for speed

		svm.train_auto(trainData, responses, new Mat(), new Mat(), params);
	}

	public void createTrainingDataMat(float[] feat){
		double trainMat[][] = new double[][]{
				                  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, {0.07,0.09,0.14,	0,	0.18,	0.21,	0,	0.17,	0.06,	0.11,	0.1,	0.08,	0,	0,	0.2,	0,	0,	0.34,	0.13,	0.16,	0.03,	0.02,	0,	0.08,	0.21,	0,	0.29,	0.23,	0.1,	0.06,	0,	9.3,	1.15,	3.2,	0.05,	0.04,	3.21,	0.08,	3.21,	9.9}};
								 // {0.07,0.09,0.14,0	0.18,0.21,0,0.17	0.06	0.11	0.1	0.08	0	0	0.2	0	0	0.34	0.13	0.16	0.03	0.02	0	0.08	0.21	0	0.29	0.23	0.1	0.06	0	9.3	1.15	3.2	0.05	0.04	3.21	0.08	3.21	9.9
//}
								/*  {0.07,0.09,0.14,0,0.18,0.21,0,0.17,0.06,0.11,0.1,0.08,0,0,0.2,0,0,0.34,0.13,0.16,0.03,0.02,0,0.08,0.21,0,0.29,0.23,0.1,0.06,0,9.3,1.15,387.5,315.52,351.79,3.2,0.05,0.04,3.21,0.08,3.21,9.9},
								  {0.06,0.15,0.15,0.07,0.07,0.1,0.09,0.08,0.12,0.11,0.15,0.09,0.12,0.09,0.09,0.04,0.08,0.15,0.09,0.05,0.19,0.12,0.08,0.07,0.06,0.08,0.11,0.09,0.13,0.09,0,7.61,-1.59,552.94,380,1400,8.75,8.11,5.95,10.63,9.11,10.63,15.83},
								  {0,0,0,0,0.04,0.11,0.26,0.34,0.21,0,0,0,0,0,0,0.02,0.03,0.2,0,0,0.04,0.22,0,0,0,0.52,0,0,0.21,0,0,0.23,9.66,409.09,958.33,361.11,1.49,0.03,0.02,1.49,0.05,1.49,9.77},
								  {0.29,0.1,0.05,0.14,0.11,0.11,0.06,0.03,0.07,0.07,0.13,0.11,0.13,0.17,0.13,0.07,0.1,0.1,0.07,0.02,0.2,0.12,0.09,0.09,0.07,0.06,0.09,0.05,0.04,0.2,0,6,-2.74,2825,1500,3375,6.57,5.16,3.98,8.52,6.07,8.52,11.33},
								  {0.03,0.13,0.16,0.17,0.11,0.09,0.1,0.1,0.08,0.06,0.25,0.17,0.09,0.07,0.16,0.08,0.07,0.06,0.06,0.01,0.1,0.12,0.06,0.12,0.1,0.09,0.09,0.12,0.12,0.1,0,-9.09,1.13,2375,4275,1425,6.9,8.61,3.87,8.12,9.79,8.12,15.67},
								  {29,0.01,0.02,0.03,0,0.06,0,0,0.07,0.04,0,0.02,0.03,0,0,0,0.09,0,0,0.1,0,0.03,0,0.04,0,0,0.08,0,0,0.08,0,0,0.62,1.73,240,350,213.64,2.02,0.45,1.26,7.5,1.66,7.5,2.73},
								  {0.08,0.09,0.11,0.12,0.12,0.13,0.1,0.12,0.09,0.08,0.08,0.09,0.11,0.12,0.12,0.11,0.07,0.09,0.13,0.01,0.08,0.04,0.1,0.11,0.13,0.15,0.12,0.13,0.08,0.1,0,9.24,2.03,4125,423.81,1562.5,2.95,7.58,4.12,3.63,8.63,3.63,12.81},
								  {0.03,0.38,0.09,0.08,0,0.13,0.13,0,0.14,0.04,0.12,0.07,0.19,0,0,0.36,0,0.14,0,0.13,0,0.01,0,0,0.04,0,0.1,0.13,0.23,0.51,0,3.01,8.14,267.14,375,263.51,4.21,0.06,0.05,4.21,0.21,4.21,9.64},
								  {0.63,0,0.09,0.19,0.05,0.04,0,0,0,0.02,0,0,0,0,0.01,0.01,0.02,0.07,0.14,0.73,0.03,0.02,0,0.06,0,0,0,0.17,0,0.69,0,2.86,7.9,259.46,2483.33,250,5.5,0.07,0.08,5.5,0.15,5.5,10.04},
								  {0.08,0.15,0.07,0.1,0.09,0.13,0.08,0.07,0.13,0.12,0.11,0.12,0.13,0.1,0.1,0.07,0.07,0.08,0.1,0,0.12,0.12,0.09,0.08,0.07,0.1,0.11,0.12,0.08,0.14,0,8.05,-1.42,1950,320,1135.71,5.57,8.76,8.17,6.89,10.51,6.89,17.12}};*/
		
		
		
		
		int rows = 2;
		int cols = 40;
		
		//double[] featrow2 = {0.07,0.09,0.14,	0,	0.18,	0.21,	0,	0.17,	0.06,	0.11,	0.1,	0.08,	0,	0,	0.2,	0,	0,	0.34,	0.13,	0.16,	0.03,	0.02,	0,	0.08,	0.21,	0,	0.29,	0.23,	0.1,	0.06,	0,	9.3,	1.15,	3.2,	0.05,	0.04,	3.21,	0.08,	3.21,	9.9};
		
			for(int j = 0; j < cols; j++){
				trainMat[1][j] = feat[j];
			}
		
			/*for(int i = 0; i < cols; i++){
				trainMat[2][i] = featrow2[i];
			}*/
		
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < cols; j++){
				trainData.put(i+1, j+1, (float)trainMat[i][j]);
			}
		}
		
		//float labels[] = {0,1,0,1,1,0,1,0,0,1}; // 0 = implies sitting, 1 = implies jogging
		
		//return labels;
	}
	
	
}
