package com.example.religious.bluetoothreciever;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by religious on 11/22/17.
 */

public class BluetoothReciever extends Service {

    private Timer timer;
    static boolean status = false;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
    Handler handler_b = new Handler();

    static boolean isStatus = false;
    static int num=0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        timer = new Timer("Updater");

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                handler_b.post(new Runnable() {
                    @Override
                    public void run() {
                        findBT();
                        openBT();
                    }
                });


                if (isStatus) {
                    if (num==2) {
                   //     sendSms();
                        num=0;
                    }
                    num++;
                }


            /*    handler_b.post(new Runnable() {
                    @Override
                    public void run() {
                        beginListenForData();
                    }
                });*/

            }
        }, 0, 20000);// 20 sec

    }

    void findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //  myLabel.setText("No bluetooth adapter available");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            //startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.e("name", device.getName().trim());

                if (device.getName().trim().equals("HC-05")) {
                    mmDevice = device;
                    break;
                }
            }
        }
        //  myLabel.setText("Bluetooth Device Found");

        Toast.makeText(BluetoothReciever.this, "Bluetooth Device Found", Toast.LENGTH_SHORT);
    }

    void openBT() {
        Log.e("msg", "openx");
        try {


            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            if (mmSocket.isConnected()) {
                mmOutputStream = mmSocket.getOutputStream();
                mmInputStream = mmSocket.getInputStream();

                beginListenForData();

                Log.e("msg", "open");
            }
        } catch (Exception e) {
            Log.e("ER", e.toString());
        }
    }


    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    if (mmInputStream != null) {
                        try {
                            int bytesAvailable = mmInputStream.available();
                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == delimiter) {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;
                                        Log.e("msgxxx", data);
                                        if (data.trim().equals("done")) {
                                            isStatus = true;
                                        }


                                       /* handler.post(new Runnable() {
                                            public void run() {
                                                Toast.makeText(BluetoothReciever.this, data, Toast.LENGTH_SHORT);
                                                Log.e("msg", data);
                                            }
                                        });*/
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }
                        } catch (IOException ex) {
                            stopWorker = true;
                        }
                    }
                }
            }
        });

        workerThread.start();
    }


    void sendSms() {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("+94777894188", null, "done", null, null);
        isStatus = false;
    }

}
