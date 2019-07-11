package me.djnwzs.bysj;

import android.util.Log;
import android.widget.Toast;

import static me.djnwzs.bysj.MainActivity.*;

public class SendData {
    /**
     * 向蓝牙发送数据
     */
   /*public static void stringSend(String str) {
        if (mCharacteristic == null) {
            Toast.makeText(mactivity, "发送数据失败", Toast.LENGTH_SHORT).show();
            Log.e("dataSend", "fail");
            return;
        }
        *//*if (BluetoothProfile.STATE_CONNECTED != BluetoothProfile.STATE_CONNECTED) {
            Toast.makeText(MainActivity.this, "未连接蓝牙设备", Toast.LENGTH_SHORT).show();
            return;
        }*//*
        //BluetoothGattService gattService = mBluetoothGatt.getService(uuid);
        //byte[] send={(byte) 0xaa,0x01,0x01,(byte)0x81,(byte) 0xff};
        byte[] send = new byte[20];
        send = str.getBytes();
        byte[] sendData = new byte[send.length + 2];
        sendData[0] = (byte) 0xaa;
        sendData[sendData.length - 1] = (byte) 0xff;
        for (int i = 1; i < sendData.length - 1; i++) {
            sendData[i] = send[i - 1];
        }
        Log.e("dataSend", new String(sendData));
        //Log.e("dataSend", linkLossService +"");

        mCharacteristic.setValue(sendData);
        mBluetoothGatt.writeCharacteristic(mCharacteristic);
        Log.e(String.valueOf(mCharacteristic), "String: ");
    }*/
    /*public static void dataSend(int d) {
        if (mCharacteristic == null) {
            Toast.makeText(mactivity, "发送数据失败", Toast.LENGTH_SHORT).show();
            Log.e("dataSend", "fail");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(mCharacteristic, true);
        byte[] result = new byte[4];
        result[0] = (byte)((d >> 24) & 0xFF);
        result[1] = (byte)((d >> 16) & 0xFF);
        result[2] = (byte)((d >> 8) & 0xFF);
        result[3] = (byte)(d & 0xFF);
        mCharacteristic.setValue(result);
        mBluetoothGatt.writeCharacteristic(mCharacteristic);
        Log.e("dataSend: ", result.toString());
    }*/
    public static void byteSend( byte[] bytes) {
        if (mCharacteristic == null) {
            Toast.makeText(mactivity, "发送数据失败", Toast.LENGTH_SHORT).show();
            Log.e("dataSend", "fail");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(mCharacteristic, true);
        mCharacteristic.setValue(bytes);
        mBluetoothGatt.writeCharacteristic(mCharacteristic);
        Log.e("dataSend: ", bytes.toString());
    }
}
