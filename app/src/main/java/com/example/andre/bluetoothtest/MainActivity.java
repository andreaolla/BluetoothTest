package com.example.andre.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    /**********************************************************************************************
     *                                      FIELDS
     ***********************************************************************************************/

    private BluetoothAdapter btAdapter;
    private ListView lv;
    private ArrayAdapter<String> listAdapter = null;
    private static final int BLUETOOTH_ON = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        provabytes();

        // If this device have not a Bluetooth module ".getDefaultAdapter" return null
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //The following lines designed to initiate a ListView and give it to ArrayAdapter
        lv = (ListView) findViewById(R.id.listview);
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        lv.setAdapter(listAdapter);

        // When you click he will be called a scan method
        final Button btn1 = (Button) findViewById(R.id.btn);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                scan(btn1);
            }
        });
    }


    /*Check whether that the bluetooth has been enabled using poupup*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLUETOOTH_ON && resultCode == RESULT_OK) {
            load();
        }
    }


    /**********************************************************************************************
     *                                      METHODS
     ***********************************************************************************************/


    /*The scan method check whether Bluetooth is enabled. If is enabler call the load method,
    If is not enabled it call issues a request to enable Bluetooth*/
    public void scan(View v) {
        if (!btAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, BLUETOOTH_ON);
        } else
            load();
    }

    // This method get the devices names using ".getBondedDevice" and give they to listAdapter
    private void load() {
        Set<BluetoothDevice> dispositivi = btAdapter.getBondedDevices();
        listAdapter.clear();
        for (BluetoothDevice bt : dispositivi)
            listAdapter.add(bt.getName());

        // With a clicklistener it is selected the device you want to connect to
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String listElement = (String) parent.getItemAtPosition(position);
                //sayHelloToDevice(listElement);
                callConnectThread(listElement);
            }
        });
    }

    //This method get a BluetoohDevice object and call "ConnectThread" that connect the devices
    private void callConnectThread(String deviceName) {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        BluetoothDevice targetDevice = null;
        for (BluetoothDevice pairedDevice : pairedDevices)
            if (pairedDevice.getName().equals(deviceName)) {
                targetDevice = pairedDevice;
                break;
            }
        MyHandler mHandler = new MyHandler();
        ConnectThread CT = new ConnectThread(targetDevice, btAdapter, mHandler);
        CT.start();
    }

    //Handler
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            if (bundle.containsKey("value")) { // check wherether exist a "value" key
                String value = bundle.getString("value"); //get the value saved in the bundle
                Button btn = (Button) findViewById(R.id.btn);
                System.out.println("DEBUG: Handler(Main): - " + value);
                btn.setText(value + " mV"); //set the button text
            }
        }
    }

    private void provabytes(){
        String S = "ciao";
        Log.i("provabytes" , "String S = \"ciao\";" + S);
        byte[] B = S.getBytes();
        Log.i("provabytes" , "byte[] B = S.getBytes();" + Arrays.toString(B));
        S = new String(B);
        Log.i("provabytes" , "String S = new String (B);" + S);
    }

    //This method is not used
    private void sayHelloToDevice(String deviceName) {

        UUID SPP_UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        //Seleziona il dispositivo attraverso il suo nome
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        BluetoothDevice targetDevice = null;
        for (BluetoothDevice pairedDevice : pairedDevices)
            if (pairedDevice.getName().equals(deviceName)) {
                targetDevice = pairedDevice;
                break;
            }

        // Se il dispositivo non viene rilevato nelle vicinanze compare un segnale d'errore
        if (targetDevice == null) {
            Toast.makeText(this, "Dispositivo non trovato", Toast.LENGTH_SHORT).show();
            return;
        }

        // Creata una connessione SPP UUID con il secondo dispositivo
        BluetoothSocket btSocket = null;
        try {
            btSocket = targetDevice.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
        } catch (IOException e) {
            Toast.makeText(this, "Unable to open a serial socket with the device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si connette al dispositivo
        try {
            btSocket.connect();
        } catch (IOException e) {
            Toast.makeText(this, "Unable to connect to the device", Toast.LENGTH_SHORT).show();
            return;
        }

        //Manda il messaggio
        try {
            OutputStreamWriter writer = new OutputStreamWriter(btSocket.getOutputStream());
            writer.write("Hello World!\r\n");
            writer.flush();
        } catch (IOException e) {
            Toast.makeText(this, "Unable to send message to the device", Toast.LENGTH_SHORT).show();
        }

        //Interrompe la connessione
        try {
            btSocket.close();
            Toast.makeText(this, "Message successfully sent to the device", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Unable to close the connection to the device", Toast.LENGTH_SHORT).show();
        }
    }

}