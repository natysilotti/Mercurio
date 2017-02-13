package com.example.riccieli.mercurio;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.indext.nexlabapp.BLeService;
import com.indext.nexlabapp.database.DatabaseHelper;
import com.indext.nexlabapp.types.ConsumerUnit;
import com.indext.nexlabapp.types.Device;

import java.util.ArrayList;

import static com.indext.nexlabapp.MainActivity.CONSUMER_UNIT;

public class DevicesActivity extends AppCompatActivity {
    private static final String TAG = "DevicesActivity";

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private BluetoothAdapter bluetoothAdapter;
    private ProgressDialog connectingProgress;
    private ProgressDialog disconnectingProgress;

    private Boolean isResumed = false;

    private static Boolean receivingData = false;

    public static String
            DATA = "DevicesActivity.data",
            DEVICE = "DevicesActivity.device",
            BUSCAR = "DevicesActivity.refresh",
            toDISCONNECT = "DevicesActivity.disconnect",
            INIT_DATA_RECEIVER = "DevicesActivity.INIT_DATA_RECEIVER",
            toCONNECT = "DevicesActivity.connect";

    private BluetoothService mBLeService;
    private IBinder mBinderService;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BUSCAR.equals(action)) {
                new finding().execute();
            }
            else if (toCONNECT.equals(action)) {
                String address = intent.getStringExtra(DATA);
                if (!address.isEmpty() && mBLeService != null) {
                    connect(address);
                }
            }
            else if (toDISCONNECT.equals(action)) {
                disconnectingProgress = new ProgressDialog(DevicesActivity.this);
                disconnectingProgress.setMessage("Desconectando do dispositivo ...");
                disconnectingProgress.setCancelable(false);
                disconnectingProgress.show();

                new Thread(new Runnable() {
                    public void run() {
                        mBLeService.disconnect();
                    }
                }).start();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mBLeService.isConnected()) {
                            disconnectingProgress.dismiss();
                            AlertDialog levelDialog = getDialogNeutral(
                                    "Falha na desconexão!", R.drawable.ic_dialog_show_cancel, "Algo deu errado!", getString(R.string.support_message_button_ok), 1);
                            levelDialog.show();
                        }
                    }
                }, 2000);
            }
            else if (BluetoothService.ACTION_CONNECTION_FAIL.equals(action)) {
                connectingProgress.dismiss();
                AlertDialog levelDialog = getDialogNeutral(
                        "Falha na conexão!", R.drawable.ic_dialog_show_cancel, "Conexão não realizada!", getString(R.string.support_message_button_ok), 1);
                levelDialog.show();
            }
            else if (BluetoothService.ACTION_CONNECTED.equals(action)) {
                connectingProgress.dismiss();
                String msg = intent.getStringExtra(BLeService.EXTRA_DATA);
                BluetoothDevice device = intent.getParcelableExtra(BLeService.EXTRA_DEVICE);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment fragment = DeviceConnectedFragment.newInstance(unit, device, msg, mBinderService);
                ft.replace(R.id.devices_placeholder, fragment);
                ft.commit();
            }
            else if (BluetoothService.ACTION_DISCONNECTED.equals(action)) {
                disconnectingProgress.dismiss();
                AlertDialog levelDialog = getDialogNeutral(
                        "Desconectado!", R.drawable.ic_dialog_show_check, "Dispositivo desconectado com sucesso!", getString(R.string.support_message_button_ok), 10);
                levelDialog.show();

                new finding().execute();
            }
            else if (BluetoothService.ACTION_CONNECTION_DROP.equals(action)) {
                AlertDialog levelDialog = getDialogNeutral(
                        getString(R.string.support_message_title_attetion), R.drawable.ic_dialog_show_warning, "Conexão foi perdida!", getString(R.string.support_message_button_ok), 10);
                levelDialog.show();

                new finding().execute();
            }
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBinderService = service;
            mBLeService = ((BluetoothService.LocalBinder) service).getService();
            if (!mBLeService.initialize()) {
                Log.e(TAG, "Service is not initialized!");
            } else {
                Log.d(TAG, "Service is initialized!");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "Service is disconnected");
            mBLeService = null;
        }
    };

    public static Boolean getReceivingData() {
        return receivingData;
    }

    public static void setReceivingData(Boolean receivingData) {
        DevicesActivity.receivingData = receivingData;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_devices);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        registerReceiver(mReceiver, makeIntentFilter());
        Intent gattServiceIntent = new Intent(this, BLeService.class);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        init();
    }

    private IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BUSCAR);
        intentFilter.addAction(toCONNECT);
        intentFilter.addAction(toDISCONNECT);
        intentFilter.addAction(INIT_DATA_RECEIVER);

        intentFilter.addAction(BLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLeService.ACTION_GATT_CONNECTION_FAIL);
        intentFilter.addAction(BLeService.ACTION_GATT_CONNECTION_DROP);

        return intentFilter;
    }

    private void init() {
        ((TextView) findViewById(R.id.toolbar_title)).setText(unit.getName());
        new finding().execute();
    }

    private class finding extends AsyncTask<Void, Void, ArrayList<Device>> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(DevicesActivity.this);
            progressDialog.setMessage("Procurando dispositivos cadastrados...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected ArrayList<Device> doInBackground(Void... voids) {
            return db.getAllDevices(unit.getId());
        }

        @Override
        protected void onPostExecute(ArrayList<Device> result) {
            progressDialog.dismiss();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment fragment;

            if (result.size() < 1) {
                fragment = nothing;
            } else {
                fragment = DeviceDisconnectedFragment.newInstance(result);
            }

            ft.replace(R.id.devices_placeholder, fragment);
            if (isResumed) {
                ft.commit();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        isResumed = true;
    }

    @Override
    public void onPause(){
        super.onPause();
        isResumed = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)  {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private void connect(String address) {
        if (mBLeService == null) {
            AlertDialog levelDialog = getDialogNeutral(
                    getString(R.string.support_message_title_important), R.drawable.ic_dialog_show_cancel,
                    "Nenhum serviço de gerenciamento do Bluetooth está em execução. Reinicie o aplicativo!",
                    getString(R.string.support_message_button_ok), 1);
            levelDialog.show();
            return;
        }

        if (address.equals("Address:TEST")) {
            AlertDialog alertDialog = getDialogNeutral(
                    getString(R.string.support_message_title_attetion), R.drawable.ic_dialog_show_warning, "Este dispositivo é somente para teste de layout" +
                            " e não é possível iniciar uma conexão.", getString(R.string.support_message_button_ok), 1
            );
            alertDialog.show();
        }
        else if (!mBLeService.isConnected()) {
            if (mBLeService.connect(address)) {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                connectingProgress = new ProgressDialog(this);
                connectingProgress.setMessage("Conectando ao dispositivo " +
                        device.getName() + " - " + device.getAddress() + "...");
                connectingProgress.setCancelable(false);
                connectingProgress.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.support_message_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!mBLeService.disconnect()) {
                            connectingProgress.dismiss();
                        }
                    }
                });
                connectingProgress.show();
            } else {
                AlertDialog alertDialog = getDialogNeutral(
                        getString(R.string.support_message_title_attetion), R.drawable.ic_dialog_show_warning, "Não é possível conectar com este dispositivo pois o endereço "
                                + "MAC não é válido.", getString(R.string.support_message_button_ok), 1
                );
                alertDialog.show();
            }
        }
    }

    private AlertDialog getDialogNeutral(String title, int icon, String msg, String button, int state) {
        Drawable ic;
        switch (icon) {
            case R.drawable.ic_dialog_show_cancel:
                ic = ContextCompat.getDrawable(this, R.drawable.ic_dialog_show_cancel);
                ic.setColorFilter(ContextCompat.getColor(this, R.color.red_dark), PorterDuff.Mode.SRC_IN);
                break;
            case R.drawable.ic_dialog_show_check:
                ic = ContextCompat.getDrawable(this, R.drawable.ic_dialog_show_check);
                ic.setColorFilter(ContextCompat.getColor(this, R.color.green_dark), PorterDuff.Mode.SRC_IN);
                break;
            default:
                ic = ContextCompat.getDrawable(this, R.drawable.ic_dialog_show_warning);
                ic.setColorFilter(ContextCompat.getColor(this, R.color.yellow_dark), PorterDuff.Mode.SRC_IN);
                break;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setIcon(ic)
                .setMessage(msg)
                .setNeutralButton(button, null);

        if (state == 0 || state == 10) {
            builder.setCancelable(false)
                    .setNeutralButton(getString(R.string.support_message_button_ok), null);
        }
        return builder.create();
    }
}
