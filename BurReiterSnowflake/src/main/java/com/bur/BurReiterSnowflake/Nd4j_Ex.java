package com.bur.BurReiterSnowflake;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;


public class Nd4j_Ex extends Nd4j {

	//numpy.roll equivalent for Nd4j
	public static INDArray roll(INDArray a, int axis, int shift){
    	int n = a.size(axis);
    	INDArray indexes;
    	shift = ((shift % n)+n)%n;
    	
    	if(shift==1){
    		 indexes = Nd4j.hstack(Nd4j.create(new double[]{n-1}),Nd4j.linspace(0, (n-shift)-1, n-shift));	
    	}else if (shift ==0){
    		return a;
    		 
    	}else{
    		
    		indexes = Nd4j.hstack(Nd4j.linspace(n-shift, n-1, shift),Nd4j.linspace(0, (n-shift)-1, n-shift));	
    	}
    	
   
    	int[] toGet2 = indexes.data().asInt();
    	INDArray out2;
    	if (axis==1){
	        out2 = a.getRows(toGet2);
		        for( int i=0; i<toGet2.length; i++ ){
		        	out2.getRow(toGet2[i]);
		        }
    	}else{
    		 out2 = a.getColumns(toGet2);
		        for( int i=0; i<toGet2.length; i++ ){
		        	out2.getRow(toGet2[i]);
		        }	
    	}

  
    	
    	
		return out2;
	}


}
