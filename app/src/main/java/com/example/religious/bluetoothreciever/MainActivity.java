package com.example.religious.bluetoothreciever;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sdsmdg.harjot.crollerTest.Croller;
import com.sdsmdg.harjot.crollerTest.OnCrollerChangeListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Exchanger;

public class MainActivity extends AppCompatActivity {
    TextView myLabel;
    EditText myTextbox;
    private View color_view;
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
    boolean statusC = false;
    int red, green, blue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button openButton = (Button) findViewById(R.id.open);
        Button sendButton = (Button) findViewById(R.id.send);
        Button btn_on = (Button) findViewById(R.id.btn_on);
        Button btn_off = (Button) findViewById(R.id.btn_off);
        Button closeButton = (Button) findViewById(R.id.close);
        myLabel = (TextView) findViewById(R.id.label);
        myTextbox = (EditText) findViewById(R.id.entry);
        color_view = (View) findViewById(R.id.color_view);

        Croller crollerR = (Croller) findViewById(R.id.croller_r);
        crollerR.setIndicatorWidth(10);
        crollerR.setMax(256);
        crollerR.setStartOffset(45);
        crollerR.setIsContinuous(false);
        crollerR.setLabelColor(Color.BLACK);
        crollerR.setLabel("R");


        crollerR.setOnCrollerChangeListener(new OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(Croller croller, int progress) {
                // use the progress

                croller.setLabel("R   " + progress + "");
                red = progress - 1;

                try {
                    setColorV();
                } catch (Exception e) {
                }

                try {
                    if (statusC)
                        sendData();

                } catch (Exception ex) {


                }


            }

            @Override
            public void onStartTrackingTouch(Croller croller) {
                // tracking started
                statusC = true;
            }

            @Override
            public void onStopTrackingTouch(Croller croller) {
                // tracking stopped
                statusC = false;
            }
        });
        Croller crollerG = (Croller) findViewById(R.id.croller_g);
        crollerG.setIndicatorWidth(10);
        crollerG.setMax(256);
        crollerG.setStartOffset(45);
        crollerG.setIsContinuous(false);
        crollerG.setLabelColor(Color.BLACK);
        crollerG.setLabel("G");


        crollerG.setOnCrollerChangeListener(new OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(Croller croller, int progress) {
                // use the progress

                croller.setLabel("G   " + progress + "");
                green = progress - 1;
                try {
                    setColorV();
                } catch (Exception e) {
                }

                try {
                    if (statusC)
                        sendData();

                } catch (Exception ex) {


                }


            }

            @Override
            public void onStartTrackingTouch(Croller croller) {
                // tracking started
                statusC = true;
            }

            @Override
            public void onStopTrackingTouch(Croller croller) {
                // tracking stopped
                statusC = false;
            }
        });
        Croller crollerB = (Croller) findViewById(R.id.croller_b);
        crollerB.setIndicatorWidth(10);
        crollerB.setMax(256);
        crollerB.setStartOffset(45);
        crollerB.setIsContinuous(false);
        crollerB.setLabelColor(Color.BLACK);
        crollerB.setLabel("B");


        crollerB.setOnCrollerChangeListener(new OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(Croller croller, int progress) {
                // use the progress

                croller.setLabel("B   " + progress + "");
                blue = progress - 1;
                try {
                    setColorV();
                } catch (Exception e) {
                }

                try {
                    if (statusC)
                        sendData();

                } catch (Exception ex) {


                }


            }

            @Override
            public void onStartTrackingTouch(Croller croller) {
                // tracking started
                statusC = true;
            }

            @Override
            public void onStopTrackingTouch(Croller croller) {
                // tracking stopped
                statusC = false;
            }
        });

        //Open Button
        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    findBT();
                    openBT();
                } catch (Exception ex) {
                    Log.e("xxx", ex.toString());
                }
            }
        });

        //Send Button


        btn_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sendData();
                } catch (Exception ex) {
                    try {
                        findBT();
                        openBT();
                    } catch (Exception eex) {
                        Log.e("xxx", eex.toString());
                    }

                }
            }
        });

        btn_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sendData();
                } catch (Exception ex) {
                }
            }
        });

        //Close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    closeBT();
                } catch (IOException ex) {
                }
            }
        });
    }

    void findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            myLabel.setText("No bluetooth adapter available");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
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
        myLabel.setText("Bluetooth Device Found");
    }

    void openBT() {
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();

            myLabel.setText("Bluetooth Opened");
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

                                    handler.post(new Runnable() {
                                        public void run() {
                                            myLabel.setText(data);
                                        }
                                    });
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
        });

        workerThread.start();
    }

    void sendData() throws Exception {

        String msg=red+","+green+","+blue;

        msg += ";";
        mmOutputStream.write(msg.getBytes());
        myLabel.setText("Data Sent");
    }

    void closeBT() throws IOException {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        myLabel.setText("Bluetooth Closed");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            closeBT();
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }


    private void setColorV() {

        color_view.setBackgroundColor(Color.rgb(red, green, blue));
    }
}