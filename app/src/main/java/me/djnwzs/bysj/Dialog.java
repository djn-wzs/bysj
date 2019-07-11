package me.djnwzs.bysj;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

import static me.djnwzs.bysj.GattCallback.mBluetoothGattCallback;
import static me.djnwzs.bysj.MainActivity.*;
import static me.djnwzs.bysj.ScanDevice.*;

public class Dialog {
    public static String address = "";

    public static void dialog_list() {
        if (address_list.isEmpty()) {
            Toast.makeText(mactivity, "无可用蓝牙设备", Toast.LENGTH_SHORT).show();
            return;
        }
        View bottomView = View.inflate(mactivity, R.layout.list_dialog, null);//填充ListView布局
        ListView listView = (ListView) bottomView.findViewById(R.id.listView);
        listView.setAdapter(new DailogAdapter(mactivity));//ListView设置适配器
        AlertDialog listdialog = new AlertDialog.Builder(mactivity)
                .setTitle("选择蓝牙设备").setView(bottomView)//在这里把写好的这个listview的布局加载dialog中
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bluetoothdevice = mBluetoothAdapter.getRemoteDevice(address);
                        if (!isClassicBluetooth) {
                            mBluetoothGatt = bluetoothdevice.connectGatt(mactivity, false, mBluetoothGattCallback);
                        } else if (isClassicBluetooth) {
                            mBluetoothGatt = bluetoothdevice.connectGatt(mactivity, false, mBluetoothGattCallback);
                        }
                    }
                }).create();
        listdialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击
        listdialog.show();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                address = address_list.get(i);
            }
        });
    }

}
