package com.example.riccieli.mercurio;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import com.example.riccieli.mercurio.bluetooth.ListDevice;
import com.example.riccieli.mercurio.control.ControlFragment;
//import com.example.riccieli.mercurio.control.SelectorFragment;

import java.nio.channels.Selector;

import static com.example.riccieli.mercurio.Constants.ADDRESS_CONNECTION;
import static com.example.riccieli.mercurio.Constants.INTENT_CONNECTION;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String TAG = "MainActivity";
    private ProgressDialog connectingProgress;
    private AlertDialog levelDialog;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (INTENT_CONNECTION.equals(action)) {
                String address = intent.getStringExtra(ADDRESS_CONNECTION);
                if (!address.isEmpty() && mBluetoothService != null) {
                    connect(address);
                }
            }
            else if (BluetoothService.ACTION_CONNECTION_FAIL.equals(action)) {
                connectingProgress.dismiss();
                showDialogNeutral("Falha na conexão!", "Conexão não realizada!", "OK");
            }
            else if (BluetoothService.ACTION_CONNECTED.equals(action)) {
                connectingProgress.dismiss();
                showDialogNeutral("Conectado!", "Conexão realizada com sucesso!", "OK");
                mBluetoothService.sendData("CONFIG#OK;");
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment fragment = ControlFragment.newInstance(mBinderService);
                ft.replace(R.id.main_fragment, fragment);
                ft.commit();
            }
        }
    };


    private IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_CONNECTION);

        intentFilter.addAction(BluetoothService.ACTION_CONNECTION_FAIL);
        intentFilter.addAction(BluetoothService.ACTION_CONNECTED);
        return intentFilter;
    }

    private void connect(String address) {
        if (mBluetoothService == null) {
            showDialogNeutral( "Importante",
                    "Nenhum serviço de gerenciamento do Bluetooth está em execução. Reinicie o aplicativo!",
                    "OK");
            return;
        }

        if (!mBluetoothService.isConnected()) {
            if (mBluetoothService.connect(address)) {
                connectingProgress = new ProgressDialog(this);
                connectingProgress.setMessage("Conectando ao dispositivo " +
                        address + "...");
                connectingProgress.setCancelable(false);
                connectingProgress.setButton(DialogInterface.BUTTON_NEUTRAL, "Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!mBluetoothService.disconnect()) {
                            connectingProgress.dismiss();
                        }
                    }
                });
                connectingProgress.show();
            } else {
                showDialogNeutral(
                        "Atenção", "Não é possível conectar com este dispositivo pois o endereço "
                                + "MAC não é válido.", "OK"
                );
            }
        }
    }

    private BluetoothService mBluetoothService;
    private IBinder mBinderService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBinderService = service;
            mBluetoothService = ((BluetoothService.LocalBinder) service).getService();
            if (!mBluetoothService.initialize()) {
                Log.e(TAG, "Service is not initialized!");
            } else {
                Log.d(TAG, "Service is initialized!");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "Service is disconnected");
            mBluetoothService = null;
            mBinderService = null;
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        registerReceiver(mReceiver, makeIntentFilter());
        Intent gattServiceIntent = new Intent(this, BluetoothService.class);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        init();
    }


    private void init() {
        FragmentTransaction ft =  getSupportFragmentManager().beginTransaction();
        Fragment fragment = new ListDevice();
        ft.replace(R.id.main_fragment, fragment);
        ft.commit();
    }

    // Impede de fechar o APP quando a NavigationView está ativa e é pressionado o botão "Back"
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.control) {
            /*FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment fragment = ControlFragment.newInstance(mBinderService);
            ft.replace(R.id.main_fragment, fragment);
            ft.commit();*/
        } else if (id == R.id.charts) {
         //   startActivity(new Intent(MainActivity.this, SelectorActivity.class));

        } else if (id == R.id.tables) {

        } else if (id == R.id.selectors) {
           // FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
          //  Fragment fragment = SelectorFragment.newInstance(mBinderService);
          //  ft.replace(R.id.main_fragment, fragment);
          //  ft.commit();

        } else if (id == R.id.progress_bar) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void showDialogNeutral(String title, String msg, String button) {
        if (levelDialog != null) {
            levelDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(msg)
                .setNeutralButton(button, null)
                .setCancelable(false);
        levelDialog = builder.create();
        levelDialog.show();
    }
}
