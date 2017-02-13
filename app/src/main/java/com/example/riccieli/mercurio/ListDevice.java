package com.example.riccieli.mercurio;

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
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.riccieli.mercurio.bluetooth.DeviceAdapter;

import java.util.ArrayList;

public class ListDevice extends Fragment {
    private static final int REQUEST_CODE_LOCATION = 1;
    private ListView listView;
    public ListDevice() { }

    private TextView txt;
    private ProgressBar progressBar;
    private BluetoothDevice deviceForConnect;

    private BluetoothAdapter mBluetoothAdapter;
    public DeviceAdapter mAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_device, container, false);
        listView = (ListView) view.findViewById(R.id.listview_devices);
        mDeviceList = new ArrayList<>();
        mAdapter = new DeviceAdapter(getActivity());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }

                deviceForConnect = mDeviceList.get(position);
                //connectDevice();
            }
        });


        mAdapter.setData(mDeviceList);
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
                            // Ativa a escuta para mudanças nas configurações do adaptador e dispositivos Bluetooth
                            IntentFilter filter = new IntentFilter();
                            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
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
            mBluetoothAdapter.startDiscovery();
        }
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
                mDeviceList.clear();
                progressBar.setVisibility(View.VISIBLE);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBar.setVisibility(View.GONE);
                String msg;
            }
            else if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                txt.setVisibility(View.GONE);

                if (! mDeviceList.contains(device) ) {
                    mDeviceList.add(device);
                    mAdapter.notifyDataSetChanged();
                }
                Log.d("List", device.getName()+ "-" + device.getAddress());
            }
        }

    };


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
