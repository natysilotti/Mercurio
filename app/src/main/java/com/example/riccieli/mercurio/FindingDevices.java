package com.example.riccieli.mercurio;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.riccieli.mercurio.bluetooth.DeviceAdapter;

import java.util.ArrayList;

public class FindingDevices extends AppCompatActivity {

    private static final int REQUEST_CHOICE_ANIMAL = 10;
    private static final int REQUEST_CODE_LOCATION = 20;
    private static final String TAG = "FINDING_DEVICES";

    private DeviceAdapter mAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;

    private TextView txt;

    private ProgressBar progressBar;
    private ProgressDialog connectProgress;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothDevice deviceForConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_list_device);

        //registerReceiver(mConnectionReceiver, makeUpdateIntentFilter());

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showUnsupported();
        }

        txt = (TextView) findViewById(R.id.nothing_found);
        txt.setVisibility(View.GONE);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        mDeviceList = new ArrayList<>();
        ListView mListView = (ListView) findViewById(R.id.listview_devices);
        mAdapter = new DeviceAdapter(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                }else if (!mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.startDiscovery();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FindingDevices.this);
                    builder.setTitle("Busca em andamento!")
                            .setMessage("Aguarde a busca terminar para começar uma outra.")
                            .setPositiveButton("OK", null);
                    AlertDialog levelDialog;
                    levelDialog = builder.create();
                    levelDialog.show();
                }

            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }

                deviceForConnect = mDeviceList.get(position);
                connectDevice();
            }
        });


        mAdapter.setData(mDeviceList);
        mListView.setAdapter(mAdapter);

        // Ativa a escuta para mudanças nas configurações do adaptador e dispositivos Bluetooth
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permission1 = "android.permission.ACCESS_COARSE_LOCATION";
            String permission2 = "android.permission.ACCESS_FINE_LOCATION";
            int res1 = this.checkCallingOrSelfPermission(permission1);
            int res2 = this.checkCallingOrSelfPermission(permission2);
            if (res1 != PackageManager.PERMISSION_GRANTED || res2 != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                }, REQUEST_CODE_LOCATION);
            } else if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            } else mBluetoothAdapter.startDiscovery();
        } else if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        } else mBluetoothAdapter.startDiscovery();
    }

    /*private IntentFilter makeUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_CONNECTED);
        intentFilter.addAction(BluetoothService.DATA_ERROR_USDCARD);
        intentFilter.addAction(BluetoothService.DATA_STATUS_DEVICE);
        intentFilter.addAction(BluetoothService.ACTION_CONNECTION_FAIL);
        return intentFilter;
    }*/

    private void connectDevice(){
        Log.d(TAG, "Connect to " + deviceForConnect.getAddress());
        /*mBluetoothService = MainActivity.getBluetoothService();

        if (mBluetoothService != null) {
            mBluetoothService.connect(deviceForConnect.getAddress());

            connectProgress = new ProgressDialog(this);
            connectProgress.setMessage("Conectando ao dispositivo " +
                    deviceForConnect.getName() + " - " +
                    db.getSinlgeBovine(deviceForConnect.getIdAnimal()).getName() + "...");
            connectProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    mBluetoothService.disconnect();
                }
            });
            connectProgress.setButton(DialogInterface.BUTTON_NEUTRAL, "Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mBluetoothService.disconnect();
                }
            });
            connectProgress.show();

        }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode  == REQUEST_CHOICE_ANIMAL) {
            if (resultCode == RESULT_OK) {
                String bovine = data.getStringExtra("bovine");
                Bovine bovineForConnect = Bovine.createBovine(bovine);
                int pos = mDeviceList.indexOf(deviceForConnect);
                deviceForConnect.setIdAnimal(bovineForConnect.getId());
                mDeviceList.set(pos,deviceForConnect);
                mAdapter.notifyDataSetChanged();

                connectDevice();
            } else {
                deviceForConnect = null;
            }
        }*/

    }

    private void showUnsupported() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dispositivo sem suporte para Bluetooth!")
                .setPositiveButton("OK", null);
        AlertDialog levelDialog;
        levelDialog = builder.create();
        levelDialog.show();
        finish();
    }

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList.clear();
                progressBar.setVisibility(View.VISIBLE);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBar.setVisibility(View.GONE);
                String msg;
                // When discovery is finished, change the Activity title
                if(mDeviceList.isEmpty()){
                    txt.setVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                    msg = "Nenhum dispositivo encontrado!";
                } else {
                    if (mDeviceList.size() == 1) msg =  mDeviceList.size() + " dispositivo encontrado!";
                    else msg = mDeviceList.size() + " dispositivos encontrados!";
                }
                /*Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // When discovery finds a device
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                txt.setVisibility(View.GONE);

                if (! mDeviceList.contains(device) ) {
                    mDeviceList.add(device);
                    mAdapter.notifyDataSetChanged();
                }

            }
            else  if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mBluetoothAdapter.startDiscovery();
                            }
                        }, 500);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };


    /**
     * Handles various events fired by the Service.
     * ACTION_CONNECTED: connected to a GATT server.
     * ACTION_CONNECTION_FAIL: the connection to a GATT server failed.
     */
    private final BroadcastReceiver mConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            /*if      (BluetoothService.ACTION_CONNECTED.equals(action)) {
                connectProgress.setMessage("Iniciando comunicação...");
            }
            else if (BluetoothService.ACTION_CONNECTION_FAIL.equals(action)) {
                connectProgress.dismiss();

                AlertDialog.Builder builder = new AlertDialog.Builder(FindingDevices.this);
                builder.setTitle("Falha na conexão!")
                        .setMessage("Conexão não realizada!")
                        .setNeutralButton("OK", null);
                AlertDialog levelDialog = builder.create();
                levelDialog.show();
            }
            else if (BluetoothService.DATA_ERROR_USDCARD.equals(action)) {
                connectProgress.dismiss();

                AlertDialog.Builder builder = new AlertDialog.Builder(FindingDevices.this);
                builder.setTitle("Atenção!")
                        .setMessage("Há erro de leitura no Cartão uSD da BEP!")
                        .setNeutralButton("OK", null);
                AlertDialog levelDialog = builder.create();
                levelDialog.show();
                mBluetoothService.disconnect();
            }
            else if (BluetoothService.DATA_STATUS_DEVICE.equals(action)) {
                connectProgress.dismiss();

                BEPDevice verified = db.getSinlgeDevice(deviceForConnect.getAddress());

                if( verified == null || verified.getId() == null ){
                    Log.e(TAG, "Sem cadastro no Banco de Dados...");

                    Calendar calendar = Calendar.getInstance();
                    String now = new SimpleDateFormat("yyyyMMddHHmmss").format(calendar.getTime()).toString();
                    deviceForConnect.setId(Long.parseLong(now));

                    db.insertDevice(deviceForConnect);
                }

                Log.d(TAG, "Id: " + deviceForConnect.getId());

                AlertDialog.Builder builder = new AlertDialog.Builder(FindingDevices.this);
                builder.setTitle("Conectado!")
                        .setMessage("Conexão realizada com sucesso!")
                        .setCancelable(false)
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
                AlertDialog levelDialog = builder.create();
                levelDialog.show();

            }*/
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                // If we got here, the User's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (!mBluetoothAdapter.isEnabled()){
                mBluetoothAdapter.enable();
            } else if (!mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.startDiscovery();
            }
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        //unregisterReceiver(mConnectionReceiver);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }
        super.onPause();
    }

}
