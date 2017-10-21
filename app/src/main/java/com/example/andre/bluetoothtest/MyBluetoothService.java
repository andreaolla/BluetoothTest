package com.example.andre.bluetoothtest;


import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class MyBluetoothService {
    private static final String TAG = "MY_APP_DEBUG_TAG";

    public static class BTListenerThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private byte[] mmBuffer; // mmBuffer store for the stream
        Handler mHandler;
        String str = "ciao";
        private byte[] B;

        public BTListenerThread(BluetoothSocket socket, Handler mHandler) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            this.mHandler = mHandler;
            Log.i("POSITION", "We are into a BTListenerThread");

            // Get the input and output streams; using temp objects because
            // member streams are final.

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {Log.e(TAG, "Error occurred when creating input stream", e);}

            mmInStream = tmpIn;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                System.out.println("Ciaooo");
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    B = new byte[numBytes];
                    B= Arrays.copyOfRange(mmBuffer,0,numBytes);

                    str = new String (B , "UTF-8");
                    System.out.println(str + numBytes);
                    // Send the Message to the Main activity.
                    Message readMsg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("value", str);
                   // b.putInt("length", numBytes);
                    readMsg.setData(b); //save the Bundle into the Message
                    mHandler.sendMessage(readMsg);

                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }


        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}