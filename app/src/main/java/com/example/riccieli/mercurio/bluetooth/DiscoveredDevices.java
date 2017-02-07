package com.example.riccieli.mercurio.bluetooth;

/**
 * Created by Riccieli on 04/12/2016.
 */

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.riccieli.mercurio.R;

public class DiscoveredDevices extends ListActivity {

    private static int SELECT_DISCOVERED_DEVICE = 2;
    public static int REQUEST_BLUETOOTH = 1;
    /*  Um adaptador para conter os elementos da lista de dispositivos descobertos.
         */
    ArrayAdapter<String> arrayAdapter;
    private ProgressDialog mProgressDlg;
    BluetoothAdapter btAdapter;
    private static TextView statusMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.text_header);



        statusMessage = (TextView) findViewById(R.id.txt_bluetooth);

         /*  Esse trecho não é essencial, mas dá um melhor visual à lista.
            Adiciona um título à lista de dispositivos pareados utilizando
        o layout text_header.xml.
        */

        ListView lv = getListView();
        LayoutInflater inflater = getLayoutInflater();
        View header = inflater.inflate(R.layout.text_header, lv, false);
        ((TextView) header.findViewById(R.id.txtBluetoothFounds)).setText("\nNearby Devices\n");
        lv.addHeaderView(header, null, false);

        /*  Cria um modelo para a lista e o adiciona à tela.
            Para adicionar um elemento à lista, usa-se arrayAdapter.add().
         */

        ListView list = (ListView) findViewById(R.id.list);

        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
        setListAdapter(arrayAdapter);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        btAdapter.startDiscovery();

        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setMessage("Procurando...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                btAdapter.cancelDiscovery();
            }
        });



        /*  Usa o adaptador Bluetooth padrão para iniciar o processo de descoberta.
         */
        //btAdapter.startDiscovery();

        /*  Cria um filtro que captura o momento em que um dispositivo é descoberto.
            Registra o filtro e define um receptor para o evento de descoberta.
         */
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);




        /*  Cria um modelo para a lista e o adiciona à tela.
            Para adicionar um elemento à lista, usa-se arrayAdapter.add().
         */





    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                statusMessage.setText("Bluetooth ativado :D");
            } else {
                statusMessage.setText("Bluetooth não ativado :(");
            }
        } else if (requestCode == SELECT_DISCOVERED_DEVICE) {
            if (resultCode == RESULT_OK) {
                statusMessage.setText("Você selecionou " + data.getStringExtra("btDevName") + "\n"
                        + data.getStringExtra("btDevAddress"));

            } else {
                statusMessage.setText("Nenhum dispositivo selecionado :(");
            }
        }
    }




    /*  Este método é executado quando o usuário seleciona um elemento da lista.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        /*  Extrai nome e endereço a partir do conteúdo do elemento selecionado.
            Nota: position-1 é utilizado pois adicionamos um título à lista e o
        valor de position recebido pelo método é deslocado em uma unidade.
         */
        String item = (String) getListAdapter().getItem(position-1);
        String devName = item.substring(0, item.indexOf("\n"));
        String devAddress = item.substring(item.indexOf("\n")+1, item.length());

        /*  Utiliza um Intent para encapsular as informações de nome e endereço.
            Informa à Activity principal que tudo foi um sucesso!
            Finaliza e retorna à Activity principal.
         */
        Intent returnIntent = new Intent();
        returnIntent.putExtra("btDevName", devName);
        returnIntent.putExtra("btDevAddress", devAddress);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    /*  Define um receptor para o evento de descoberta de dispositivo.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
                arrayAdapter.clear();
                mProgressDlg.show();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
                mProgressDlg.dismiss();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                showToast("Device found" + device.getName());

                arrayAdapter.add(device.getName() + "\n" + device.getAddress());

            }
        }
    };

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /*  Executado quando a Activity é finalizada.
     */
    @Override
    protected void onDestroy() {


        /*  Remove o filtro de descoberta de dispositivos do registro.
         */
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_paired_devices, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}