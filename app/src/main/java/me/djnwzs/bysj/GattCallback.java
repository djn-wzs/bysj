package me.djnwzs.bysj;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;


import java.util.List;

import static me.djnwzs.bysj.MainActivity.*;

public class GattCallback {
    private static String TAG = "";
    private static String characterUUID1 = "0000ff01-0000-1000-8000-00805f9b34fb";//APP发送命令
    private static String characterUUID2 = "0000ff02-0000-1000-8000-00805f9b34fb";//BLE用于回复命令


    public static BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        //当连接状态发生改变
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.e("GATT", "onConnectionStateChange");
            String str = gatt.getDevice().getName() + ": " + gatt.getDevice().getAddress();
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    gatt.discoverServices(); //搜索连接设备所支持的service
                    //setTextViewState(str+" 已连接");
                    Log.e("BluetoothState", "STATE_CONNECTED");
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    if (mBluetoothGatt != null) mBluetoothGatt.disconnect();
                    //setTextViewState(str+" 已断开");
                    Log.e("BluetoothState", "STATE_DISCONNECTED");
                    break;
                case BluetoothProfile.STATE_CONNECTING:
                    setTextViewState(str + " 正在连接");
                    Log.e("BluetoothState", "STATE_CONNECTING");
                    break;
                case BluetoothProfile.STATE_DISCONNECTING:
                    setTextViewState(str + " 正在断开");
                    mBluetoothGatt.close();
                    Log.e("BluetoothState", "STATE_DISCONNECTING");
                    break;
            }
            super.onConnectionStateChange(gatt, status, newState);
        }

        //发现新服务，即调用了mBluetoothGatt.discoverServices()后，返回的数据
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                serviceList = gatt.getServices();
                for (int i = 0; i < serviceList.size(); i++) {
                    BluetoothGattService theService = serviceList.get(i);
                    Log.e("ServiceName", theService.getUuid().toString());
                    characterList = theService.getCharacteristics();
                    for (int j = 0; j < characterList.size(); j++) {
                        String uuid = characterList.get(j).getUuid().toString();
                        Log.e("CharacterName", uuid);
                        if (uuid.equals(characterUUID1)) {
                            mCharacteristic = characterList.get(j);
                        } else if (uuid.equals(characterUUID2)) {
                            mCharacteristic2 = characterList.get(j);
                        }
                    }
                }
                Log.e("Character2Name:", mCharacteristic2.getUuid().toString());
                boolean b = mBluetoothGatt.setCharacteristicNotification(mCharacteristic2, true);
                if (b) {
                    List<BluetoothGattDescriptor> descriptors = mCharacteristic2.getDescriptors();
                    for (BluetoothGattDescriptor descriptor : descriptors) {
                        boolean b1 = descriptor.setValue(
                                BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                        if (b1) {
                            mBluetoothGatt.writeDescriptor(descriptor);
                            Log.d("startRead: ", "监听收数据");
                        }
                    }
                }

            }
            super.onServicesDiscovered(gatt, status);
        }

        //调用mBluetoothGatt.readCharacteristic(characteristic)读取数据回调，在这里面接收数据
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            characteristic.getValue();
            Log.e(new String(characteristic.getValue()), "onCharacteristicRead: ");
            Toast.makeText(mactivity, new String(characteristic.getValue()), Toast.LENGTH_SHORT).show();
        }

        //发送数据后的回调
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic) {
            byte[] value = characteristic.getValue();
            Log.e(TAG, "onCharacteristicChanged: " + value);
            String s0 = Integer.toHexString(value[0] & 0xFF);
            String s = Integer.toHexString(value[1] & 0xFF);
            Log.e(TAG, "onCharacteristicChanged: " + s0 + "、" + s);
            for (byte b : value) {
                Log.e(TAG, "onCharacteristicChanged: " + b);
            }
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {//descriptor读
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor
                descriptor,
                                      int status) {//descriptor写
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.e("onDescriptorWrite: ", "设置成功");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        //调用mBluetoothGatt.readRemoteRssi()时的回调，rssi即信号强度
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {//读Rssi
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

}
