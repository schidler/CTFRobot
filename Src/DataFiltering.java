/**
 * 
 * 	@author Vincent Petrella.
 * 	
 * This instance holds datas and implements a methods to get an average 
 * and a differential average on the go (using convolutions)
 * 
 */

import java.util.*;

public class DataFiltering 
{
	private Queue<Integer> valuesQueue;
	public final int filterSize;
	
	
	/**
	 * MIGHT WANT TO REWRITE THIS AS A CIRCULAR INDEXED ARRAY
	 * SINCE OUR TABLE IS FIXED, WOULD BE QUITE EFFICIENT
	 * I.E FIND 10 MINUTES TO DO THAT IN THE FUTURE !
	 */
	
	public DataFiltering(int filterSize)
	{
		this.filterSize = filterSize;
		valuesQueue = new Queue<Integer>();
	}
	
	/**
	 * Add a value to the computation table.
	 */
	
	public void PushData(int value)
	{
		valuesQueue.push(new Integer(value));
		
		// If my queue contains enough samples, then POP the oldest one
		if(valuesQueue.size() == filterSize)
		{	
			valuesQueue.pop();
		}	
	}
	
	/**
	 * 
	 * Computes a moving average of the data pushed into the instance
	 * 
	 * @return The moving average
	 */
	public double GetAverageData() 
	{
		double movingAverage = 0;	

		//Transferring my Queue to an array for calculation purposes
		int[] currentValues = new int[valuesQueue.size()];
		for(int index = 0; index<currentValues.length; index++)
		{
			currentValues[index] = (Integer) valuesQueue.elementAt(index);
		}
		
		//Calculate the average
		for(int i =0 ; i < currentValues.length; i++)
		{
			movingAverage += currentValues[i]/currentValues.length;
		}	

		return movingAverage;		
	}
	
	/**
	 * 
	 * Computes a Differential average of the data pushed into the instance 
	 * using a "convolution mask".
	 * 
	 * @return The differential average
	 */
	
	public double GetDifferentialData() 
	{
		double movingAverage = 0;	

		//Transferring my Queue to an array for calculation purposes
		int[] currentValues = new int[valuesQueue.size()];
		for(int index = 0; index < currentValues.length; index++)
		{
			currentValues[index] = (Integer) valuesQueue.elementAt(index);
		}
		
		for(int i =0; i<currentValues.length;i++)
		{
			if(i < currentValues.length/2)
			{
				movingAverage += currentValues[i]/currentValues.length;
			}
			else
			{
				movingAverage -= currentValues[i]/currentValues.length;
			}
		}
		return movingAverage;		
	}
}
