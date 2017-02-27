package com.example.riccieli.mercurio.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.riccieli.mercurio.R;

import static com.example.riccieli.mercurio.Constants.ADDRESS_CONNECTION;
import static com.example.riccieli.mercurio.Constants.INTENT_CONNECTION;

public class ListDevice extends Fragment {
    private static final int REQUEST_CODE_LOCATION = 1;
    private TextView txt;
    private ProgressBar progressBar;

    private BluetoothAdapter mBluetoothAdapter;
    public DeviceAdapter mAdapter;

    public ListDevice() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_device, container, false);
        ListView listView = (ListView) view.findViewById(R.id.listview_devices);
        mAdapter = new DeviceAdapter(getActivity());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                BluetoothDevice device = mAdapter.getItem(position);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    connect(device);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Atenção!")
                            .setMessage("Dispositivo não pareado.")
                            .setPositiveButton("OK", null);
                    AlertDialog levelDialog;
                    levelDialog = builder.create();
                    levelDialog.show();
                }
            }
        });
        listView.setAdapter(mAdapter);


        txt = (TextView) view.findViewById(R.id.nothing_found);
        txt.setVisibility(View.GONE);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                }else if (!mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.startDiscovery();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Busca em andamento!")
                            .setMessage("Aguarde a busca terminar para começar uma outra.")
                            .setPositiveButton("OK", null);
                    AlertDialog levelDialog;
                    levelDialog = builder.create();
                    levelDialog.show();
                }
            }
        });

        return view;
    }

    private void connect(BluetoothDevice device) {
        Intent intent = new Intent(INTENT_CONNECTION);
        intent.putExtra(ADDRESS_CONNECTION, device.getAddress());
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            verifyPermissions();
        } else {
            verifyIsEnable();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void verifyPermissions(){
        String permission1 = "android.permission.ACCESS_COARSE_LOCATION";
        String permission2 = "android.permission.ACCESS_FINE_LOCATION";
        int res1 = getActivity().checkCallingOrSelfPermission(permission1);
        int res2 = getActivity().checkCallingOrSelfPermission(permission2);
        if (res1 != PackageManager.PERMISSION_GRANTED || res2 != PackageManager.PERMISSION_GRANTED){
            this.requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_CODE_LOCATION);
        } else {
            verifyIsEnable();
        }
    }

    private void verifyIsEnable(){
        if (!mBluetoothAdapter.isEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Importante!")
                    .setMessage("O Bluetooth encontra-se desligado e para iniciar a busca " +
                            "é necessário deixá-lo ligado. Deseja ligar agora?")
                    .setPositiveButton("Ligar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mBluetoothAdapter.enable();
                        }
                    })
                    .setNegativeButton("Não ligar", null)

                    .setCancelable(false);
            AlertDialog levelDialog;
            levelDialog = builder.create();
            levelDialog.show();
            Button nbutton = levelDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            nbutton.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));
        } else {
            firstScan();
        }
    }

    private void firstScan() {
        mBluetoothAdapter.startDiscovery();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                verifyIsEnable();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Importante!")
                        .setMessage("É preciso dar as permissões necessárias para o aplicativo começar a busca!")
                        .setNeutralButton("OK", null)
                        .setCancelable(false);
                AlertDialog levelDialog;
                levelDialog = builder.create();
                levelDialog.show();
            }
        }
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mAdapter.clear();
                progressBar.setVisibility(View.VISIBLE);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBar.setVisibility(View.GONE);
                String msg;
            }
            else if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                txt.setVisibility(View.GONE);

                if (! mAdapter.contains(device) ) {
                    mAdapter.addDevice(device);
                }
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                firstScan();
                            }
                        }, 500);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }

    };

}
