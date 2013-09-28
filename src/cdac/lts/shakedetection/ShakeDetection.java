/* 
 * Copyright (C) 2013 by the Centre for Development of Advanced Computing Trivandrum
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cdac.lts.shakedetection;





import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.speech.tts.TextToSpeech;

/**
 * @author root
 * 
 */
public class ShakeDetection {

	private TextToSpeech textSpeech;
	
	
	/* The connection to the hardware */
    private SensorManager mySensorManager;

    /* Here we store the current values of acceleration, one for each axis */
    private float xAccel;
    private float yAccel;
    private float zAccel;

    /* And here the previous ones */
    private float xPreviousAccel;
    private float yPreviousAccel;
    private float zPreviousAccel;

    /* Used to suppress the first shaking */
    private boolean firstUpdate = true;

    /* What acceleration difference would we assume as a rapid movement? */
    private final float shakeThreshold = 1.5f;

    /* Has a shaking motion been started (one direction) */
    private boolean shakeInitiated = false;

    
    public ShakeDetection(TextToSpeech textSpeech) {
    	this.textSpeech = textSpeech;
	}

	
    
    /**
     * Registering the shake sensor
     */
    public void registerShakeSensor(Context context) {
	mySensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE); // (1)
	mySensorManager.registerListener(mySensorEventListener,
		mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
		SensorManager.SENSOR_DELAY_NORMAL); // (2)

    }

    public void unregisterShakeSensor() {
	mySensorManager.unregisterListener(mySensorEventListener);
    }
   
    
        
    
    /* Store the acceleration values given by the sensor */
    private void updateAccelParameters(float xNewAccel, float yNewAccel,
	    float zNewAccel) {
	/*
	 * we have to suppress the first change of acceleration, it results from
	 * first values being initialized with 0
	 */
	if (firstUpdate) {
	    xPreviousAccel = xNewAccel;
	    yPreviousAccel = yNewAccel;
	    zPreviousAccel = zNewAccel;
	    firstUpdate = false;
	} else {
	    xPreviousAccel = xAccel;
	    yPreviousAccel = yAccel;
	    zPreviousAccel = zAccel;
	}
	xAccel = xNewAccel;
	yAccel = yNewAccel;
	zAccel = zNewAccel;
    }

    /*
     * If the values of acceleration have changed on at least two axises, we are
     * probably in a shake motion
     */
    private boolean isAccelerationChanged() {
	float deltaX = Math.abs(xPreviousAccel - xAccel);
	float deltaY = Math.abs(yPreviousAccel - yAccel);
	float deltaZ = Math.abs(zPreviousAccel - zAccel);
	return (deltaX > shakeThreshold && deltaY > shakeThreshold)
		|| (deltaX > shakeThreshold && deltaZ > shakeThreshold)
		|| (deltaY > shakeThreshold && deltaZ > shakeThreshold);
    }

    /* The SensorEventListener lets us wire up to the real hardware events */
    private final SensorEventListener mySensorEventListener = new SensorEventListener() {

	public void onSensorChanged(SensorEvent se) {
	    /* we will fill this one later */

	    updateAccelParameters(se.values[0], se.values[1], se.values[2]); // (1)
	    if ((!shakeInitiated) && isAccelerationChanged()) { // (2)
		shakeInitiated = true;
	    } else if ((shakeInitiated) && isAccelerationChanged()) { // (3)
		executeShakeAction();
	    } else if ((shakeInitiated) && (!isAccelerationChanged())) { // (4)
		shakeInitiated = false;
	    }

	}
	

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	    /* can be ignored in this example */
	}
    };
    
    private void executeShakeAction() {
    	
	   if(textSpeech != null){
		   textSpeech.stop();
	   }
		   
	   //mySensorManager.unregisterListener(mySensorEventListener);
    }
}
