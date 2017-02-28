package com.group25.interactivegameblock;


        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.util.Log;
        import android.widget.TextView;


public class AccelerometerEventListener implements SensorEventListener {

    GameLoopTask MyGL;
    // Low Pass Filter Constant
    private final float FILTER_CONSTANT = 12.0f;

    // FSM: Setup FSM States and Signatures here
    enum myState{WAIT, RISE_A, FALL_A, FALL_B, RISE_B, RISE_C, FALL_C, RISE_D, FALL_D, DETERMINED};
    myState state = myState.WAIT;

    enum mySig{SIG_A, SIG_B, SIG_C, SIG_D, SIG_X};
    mySig signature = mySig.SIG_X;

    // FSM: Setup Threshold Constants Here
    final float[] THRES_A = {0.6f, 2.0f, -0.4f};    // A-CURVE, RIGHT MOVEMENT
    final float[] THRES_B = {-0.6f, -2.0f, 0.4f};   // B-CURVE, LEFT MOVEMENT
    final float[] THRES_C = {0.6f, 1.0f, -0.4f};    // C-CURVE, UP MOVEMENT -- Z AXIS
    final float[] THRES_D = {-0.6f, -2.0f, 0.4f};   // D-CURVE, DOWN MOVEMENT -- Y AXIS

    // FSM: Setup FSM Sample Counter Here
    final int SAMPLEDEFAULT = 30;
    int sampleCounter = SAMPLEDEFAULT;


    // The corresponding TextView and LineGraphView on the Layout
    private TextView instanceOutput;

    // 100 Historial Readings
    private float[][] historyReading = new float[100][3];

    // First-In-First-Out 100-element rotation method
    private void insertHistoryReading(float[] values) {

        for(int i = 1; i < 100; i++) {
            historyReading[i - 1][0] = historyReading[i][0];
            historyReading[i - 1][1] = historyReading[i][1];
            historyReading[i - 1][2] = historyReading[i][2];
        }

        // Low-Pass Filter Implementation
        historyReading[99][0] += (values[0] - historyReading[99][0]) / FILTER_CONSTANT;
        historyReading[99][1] += (values[1] - historyReading[99][1]) / FILTER_CONSTANT;
        historyReading[99][2] += (values[2] - historyReading[99][2]) / FILTER_CONSTANT;

        callFSM();  // Call the FSM analysis with the most recent filtered reading
        if(sampleCounter <= 0) {    // We've had more than 30 samples

            if(state == myState.DETERMINED) {       // We've figured out which state we're in within 30 samples
                if (signature == mySig.SIG_B) {
                    instanceOutput.setText("LEFT");
                    MyGL.setDirection(GameLoopTask.gameDirection.LEFT);
                }
                else if (signature == mySig.SIG_A){
                    instanceOutput.setText("RIGHT");
                    MyGL.setDirection(GameLoopTask.gameDirection.RIGHT);
                }
                else if (signature == mySig.SIG_C){
                    instanceOutput.setText("UP");
                    MyGL.setDirection(GameLoopTask.gameDirection.UP);
                }
                else if (signature == mySig.SIG_D){
                    instanceOutput.setText("DOWN");
                    MyGL.setDirection(GameLoopTask.gameDirection.DOWN);
                }
                else
                    instanceOutput.setText("UNDETERMINED");
                   // MyGL.setDirection(GameLoopTask.gameDirection.NO_MOVEMENT);

            }

            else {                                  // We haven't figured a state after 30 samples
                state = myState.WAIT;
                instanceOutput.setText("UNDETERMINED");
                MyGL.setDirection(GameLoopTask.gameDirection.NO_MOVEMENT);
            }
            sampleCounter = SAMPLEDEFAULT;          // Reset the FSM to WAIT state and SAMPLEDEFAULT for new reading
            state = myState.WAIT;
        }
    }

    // FSM 1: Implement FSM Method Here

    private void callFSM() {


        float deltaX = historyReading[99][0] - historyReading[98][0];   // X-axis readings for left/right gestures
        float deltaY = historyReading[99][1] - historyReading[98][1];   // Y-axis readings for up/down gestures ** not used in this code
        float deltaZ = historyReading[99][2] - historyReading[98][2];   // Z-axis readings for up/down gestures

        switch(state){

            case WAIT:

                sampleCounter = SAMPLEDEFAULT;  // In wait we always reset to sample default - only when we've begun detecting for a gesture do we start counting down
                signature = mySig.SIG_X;        // Always reset the signature back to undetermined

                if(deltaX > THRES_A[0]) {
                    state = myState.RISE_A;
                    // Curve A rising acceleration threshold met
                }
                else if(deltaX < THRES_B[0]){
                    state = myState.FALL_B;
                    // Curve B falling acceleration threshold met
                }
                else if(deltaZ > THRES_C[0]){
                    state = myState.RISE_C;
                    // Curve C (z-axis) rising acceleration threshold met - UP gesture started
                }
                else if(deltaZ < THRES_D[0]){
                    state = myState.FALL_D;
                    // Curve D (z-axis) falling acceleration threshold met - DOWN gesture started
                }

                break;

            case RISE_A:            // A CURVE - RIGHT (RISING)
                Log.d("FSM: ", "State RISE_A"); // For debug purposes - output in console triggered every time case occurs
                if(deltaX <= 0) {   // We've reached the maximum and are beginning to fall
                    if(historyReading[99][0] >= THRES_A[1]){
                        state = myState.FALL_A;
                    }
                    else {
                        state = myState.DETERMINED; // Why is the state determined here? If we've hit the peak but not went into the FALL state, we never really completed a motion?
                    }
                }
                break;

            case FALL_A:            // A CURVE - RIGHT (FALLING)
                Log.d("FSM: ", "State FALL_A");
                if(deltaX >= 0) {   // "Curve rebound" We've returned to complete one complete cycle
                    if(historyReading[99][0] <= THRES_A[2]){
                        signature = mySig.SIG_A;
                    }
                    state = myState.DETERMINED;
                }
                break;

            case FALL_B:            // B CURVE - LEFT gesture initiated (X-VALUE FALLING)
                Log.d("FSM: ", "State FALL_B");
                if(deltaX >= 0) {   // We've reached the minimum and are beginning to rise
                    if(historyReading[99][0] <= THRES_B[1]){
                        state = myState.RISE_B;
                    }
                    else {
                        state = myState.DETERMINED;
                    }
                }
                break;

            case RISE_B:            // B CURVE - LEFT gesture completed (X-VALUE RISING)
                Log.d("FSM: ", "State RISE_B");
                if(deltaX <= 0){
                    if (historyReading[99][0] >= THRES_B[2]) {
                        signature = mySig.SIG_B;
                    }
                    state = myState.DETERMINED;
                }
                break;

            case RISE_C:            // C CURVE - Up gesture initiated (z-value rising)
                Log.d("FSM: ", "State RISE_C");
                if(deltaZ <= 0){    // We've hit the absolute maximum of the curve, derivative is becoming negative...
                    if (historyReading[99][2] >= THRES_C[1]) {  // This asks: Did we go above our threshold?
                        state = myState.FALL_C;
                    }
                    else {
                        state = myState.DETERMINED;
                    }
                }
                break;

            case FALL_C:            // C CURVE - Up gesture rebound/falling (z-value falling)
                Log.d("FSM: ", "State FALL_C");
                if(deltaZ >= 0){     // We've hit the absolute minimum of the curve, derivative is becoming positive to return to 0
                    if (historyReading[99][2] <= THRES_C[2]) {  // This asks: Did we go below our rebound threshold?
                        signature = mySig.SIG_C;
                    }
                    state = myState.DETERMINED;
                }
                break;

            case FALL_D:            // D CURVE - Down gesture initialized (z-value falling)
                Log.d("FSM: ", "State FALL_D");
                if(deltaZ >=0) {     // We've hit the absolute minimum of the curve, derivative is becoming positive
                    if (historyReading[99][2] <= THRES_D[1]) {  // This asks: Did we go below our threshold?
                        state = myState.RISE_D;
                    } else {
                        state = myState.DETERMINED;
                    }
                }
                break;

            case RISE_D:            // D CURVE - Down gesture rebound/rising (z-value rising)
                Log.d("FSM: ", "State RISE_D");
                if(deltaZ <= 0) {   // We've hit the absolute maximum of the curve, derivative is becoming negative to return to 0
                    if (historyReading[99][2] >= THRES_D[2]) {  // This asks: Did we go above our rebound threshold?
                        signature = mySig.SIG_D;
                    }
                    state = myState.DETERMINED;
                }

            case DETERMINED:
                Log.d("FSM: ", "State DETERMINED " + signature.toString()); // Debug
                break;

            default:
                state = myState.WAIT;
                break;
        }

        sampleCounter--;

    }

    // FSM 2: Implement FSM Gesture Computation Method Here

    // FSM 3: Implement a FSM Reset Method Here

    // Constructor: Get the references of the TextView and LineGraphView from the Layout
    public AccelerometerEventListener(TextView outputView, GameLoopTask gameLoopTask) {
        this.MyGL = gameLoopTask;
        instanceOutput = outputView;
    }

    // Getter method for the history readings
    public float[][] getHistoryReading() {
        return historyReading;
    }

    // Required Method for SensorEventListener
    public void onAccuracyChanged(Sensor s, int i) { }

    // Required Method for SensorEventListner
    public void onSensorChanged(SensorEvent se) {

        if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            insertHistoryReading(se.values);

        }
    }
}
