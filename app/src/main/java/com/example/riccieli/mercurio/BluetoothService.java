package com.example.riccieli.mercurio;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService extends Service {
    private static final String TAG = "SERVICE";

    private final IBinder mBinder = new LocalBinder();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private ManageConnectedSocket manageConnectedSocket;
    private ConnectThread connectThread;

    private String mBluetoothDeviceAddress;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTING = 0;
    private static final int STATE_DISCONNECTED = 1;
    private static final int STATE_CONNECTING = 2;
    private static final int STATE_CONNECTED = 3;

    public final static String
            ACTION_CONNECTED        = "com.example.riccieli.ACTION_CONNECTED",
            ACTION_CONNECTION_DROP  = "com.example.riccieli.ACTION_CONNECTION_DROP",
            ACTION_CONNECTION_FAIL  = "com.example.riccieli.ACTION_CONNECTION_FAIL",
            ACTION_DISCONNECTED     = "com.example.riccieli.ACTION_DISCONNECTED",
            NUMBER_OF_SWITCHES = "com.example.riccieli.NUMBER_OF_SWITCHES",
            EXTRA_DATA_TEXT = "com.example.riccieli.EXTRA_DATA_TEXT",
            ITENS_OF_COMBOBOX = "com.example.riccieli.ITENS_OF_COMBOBOX";


    public final static String
            SWITCH_CMD = "com.example.riccieli.SWITCH_CMD",
            LABEL_SWITCH = "com.example.riccieli.LABEL_SWITCH",
            VALUES_SWITCH = "com.example.riccieli.VALUES_SWITCH",
            DISPLAY_CMD = "com.example.riccieli.DISPLAY_CMD",
            LABEL_DISPLAY = "com.example.rAiccieli.LBEL_DISPLAY",
            TEXT_DISPLAY = "com.example.rAiccieli.TEXT_DISPLAY",
            UPDATE_DISPLAY_CMD = "com.example.riccieli.UPDATE_DISPLAY_CMD",
            UPDATE_TEXT_DISPLAY ="com.example.riccieli.UPDATE_TEXT_DISPLAY",
            TEXT_CMD = "com.example.riccieli.TEXT_CMD",
            COMBOBOX_CMD = "com.example.riccieli.COMBOBOX_CMD",
            LABEL_COMBOBOX = "com.example.riccieli.LABEL_COMBOBOX",
            NUMBER_OF_ITENS_COMBOBOX = "com.example.riccieli.NUMBER_OF_ITENS_COMBOBOX",
            RANGE_CMD = "com.example.riccieli.RANGE_CMD",
            LABEL_RANGE = "com.example.riccieli.LABEL_RANGE",
            MIN_MAX_RANGE = "com.example.riccieli.MIN_MAX_RANGE",
            SET_RANGE_CMD = "com.example.riccieli.SET_RANGE_CMD",
            UPDATE_RANGE =  "com.example.riccieli.UPDATE_RANGE";


    public final static UUID UUID_DEVICE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        /**
         * After using a given device, you should make sure that BluetoothGatt.close() is called
         * such that resources are cleaned up properly.  In this particular example, close() is
         * invoked when the UI is disconnected from the Service.
         */
        new Thread(new Runnable() {
            public void run() {
                close();
            }
        }).start();
        return super.onUnbind(intent);
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        Log.w(TAG, "BluetoothGatt close");
        if (isConnected()){
            disconnect();
        }
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    public boolean connect(String address) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null ) return false;
        connectThread = new ConnectThread(device);
        connectThread.start();
        return true;
    }

    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUID_DEVICE);
                Log.d(TAG, "Socket created");
            } catch (IOException e) {
                Log.e(TAG, "Socket not created");
            }
            mmSocket = tmp;

        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();
            mConnectionState = STATE_CONNECTING;

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException ignored) { }

                Log.e(TAG, "Socket not connected");
                Log.e(TAG, "The connection from server failed.");
                mConnectionState = STATE_DISCONNECTED;
                mBluetoothDeviceAddress = null;
                broadcastUpdate(ACTION_CONNECTION_FAIL);
                return;
            }

            mConnectionState = STATE_CONNECTED;
            mBluetoothDeviceAddress = mmDevice.getAddress();
            broadcastUpdate(ACTION_CONNECTED);

            Log.i(TAG, "Socket connected");

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket = new ManageConnectedSocket(mmSocket);
            manageConnectedSocket.start();
            connectThread = null;
        }

        /** Will cancel an in-progress connection, and close the socket */
        void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) { }
        }
    }

    private class ManageConnectedSocket extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private String data = "";
        private String aux = "";

        ManageConnectedSocket(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

            Log.d(TAG, "Iniciando conexão...");
            write("c".getBytes());
        }

        public void run() {
            Long initTheConnection = System.currentTimeMillis();

            Log.d(TAG, "Ativando escuta de msgs...");
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    int bytesAvailable = mmInStream.available();
                    if (bytesAvailable > 0) {
                        // Send the obtained bytes to the UI activity
                        byte[] buffer = new byte[bytesAvailable];   // buffer store for the stream available
                        int bytes = mmInStream.read(buffer);        // bytes returned from read()

                        data = aux + new String (buffer);
                        data = data.replace("\n", "").replace("\r", "").replace("\r\n", "");
                        if (data.contains(";")) {
                            String cmd[] = data.split(";"); // divide os dados em mensagens para processar
                            int length = cmd.length;

                            if (data.charAt(data.length()-1) != ';') {
                                length -= 1;
                                aux = cmd[length];
                            } else {
                                aux = "";
                            }

                            for (int i = 0; i < length; i++) {
                                String tags[] = cmd[i].split("#"); // divide as mensagens em comandos e valores para processar
                                Intent intent = null;
                                if (tags.length > 1) {
                                    switch (tags[0]){
                                        case "SWITCH":
                                            Log.d(TAG, "data: " + cmd[i]);
                                            intent = new Intent(SWITCH_CMD);
                                            intent.putExtra(NUMBER_OF_SWITCHES, tags[1]);
                                            intent.putExtra(LABEL_SWITCH,tags[2]);
                                            intent.putExtra(VALUES_SWITCH,tags[3]);
                                            break;

                                        case "TEXT":
                                            Log.d(TAG, "data: " + cmd[i]);
                                            intent = new Intent(TEXT_CMD);
                                            intent.putExtra(EXTRA_DATA_TEXT, tags[1]);
                                            break;

                                        case "COMBOBOX":
                                            Log.d(TAG, "data: " + cmd[i]);
                                            intent = new Intent(COMBOBOX_CMD);
                                            intent.putExtra(LABEL_COMBOBOX,tags[1]);
                                            intent.putExtra(NUMBER_OF_ITENS_COMBOBOX,tags[2]);
                                            intent.putExtra(ITENS_OF_COMBOBOX, tags[3]);
                                            break;

                                        case "RANGE":
                                            Log.d(TAG, "data: " + cmd[i]);
                                            intent = new Intent(RANGE_CMD);
                                            intent.putExtra(LABEL_RANGE,tags[1]);
                                            intent.putExtra(MIN_MAX_RANGE, tags[2]);
                                            break;
                                        case "DISPLAY":
                                            Log.d(TAG, "data: " + cmd[i]);
                                            intent = new Intent(DISPLAY_CMD);
                                            intent.putExtra(LABEL_DISPLAY,tags[1]);
                                            intent.putExtra(TEXT_DISPLAY,tags[2]);
                                            break;

                                        case "UPDATE_DISPLAY":
                                            Log.d(TAG, "data: " + cmd[i]);
                                            intent = new Intent(UPDATE_DISPLAY_CMD);
                                            intent.putExtra(UPDATE_TEXT_DISPLAY,tags[1]);
                                            break;
                                        case "SET_RANGE":
                                            Log.d(TAG, "data: " + cmd[i]);
                                            intent = new Intent(SET_RANGE_CMD);
                                            intent.putExtra(UPDATE_RANGE,tags[1]);
                                            break;


                                        case "e":
                                            Log.e(TAG, cmd[i]);
                                            break;

                                        default:
                                            Log.d(TAG, "Unknowed Command: " + tags[0] + " - Value: " + tags[1]);
                                            break;
                                    }
                                }
                                if (intent != null) sendBroadcast(intent);
                            }
                        } else {
                            aux = data;
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Socket is closed!");
                    if (mConnectionState == STATE_CONNECTED ) {
                        Log.e(TAG, "The connection from GATT server dropped.");
                        mBluetoothDeviceAddress = null;
                        broadcastUpdate(ACTION_CONNECTION_DROP);
                    } else {
                        Log.i(TAG, "Disconnected from GATT server.");
                        broadcastUpdate(ACTION_DISCONNECTED);
                    }
                    mConnectionState = STATE_DISCONNECTED;
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException ignored) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) {}
        }
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public Boolean disconnect() {

        if (connectThread != null){
            connectThread.cancel();
            return true;
        }

        if (mBluetoothAdapter == null || manageConnectedSocket == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }

        if (mConnectionState != STATE_CONNECTING ){
            manageConnectedSocket.write("p".getBytes());

            new Thread(new Runnable() {
                public void run() {
                    Log.w("disconnect", "Desconectando de forma natural...!!");
                    Long time = System.currentTimeMillis();
                    while (System.currentTimeMillis() - time < 1000) ;
                    mConnectionState = STATE_DISCONNECTING;
                    manageConnectedSocket.cancel();
                }
            }).start();
            return true;
        }
        Log.w("disconnect", "Desvinculado com a Main...!!");
        mConnectionState = STATE_DISCONNECTING;
        manageConnectedSocket.cancel();

        return true;
    }

    /**
     * Metodos auxiliares
     */

    public boolean isConnected() {
        return mConnectionState == STATE_CONNECTED;
    }

    public String getDeviceAddress() {
        return mBluetoothDeviceAddress;
    }

    public void sendData(String str) {
        manageConnectedSocket.write(str.getBytes());
    }
}
