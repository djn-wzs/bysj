package me.djnwzs.bysj;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import static me.djnwzs.bysj.MainActivity.*;

public class ScanDevice {
    //10秒搜索时间
    private static final long SCAN_PERIOD = 10000;

    public static void scanDevice(final boolean enable) {
        if (!isBlueEnable()) {
            Log.e("scanDevice", "Bluetooth not enable!");
            return;
        } else if (enable) {//true
            //15秒后停止搜索
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    scanstop();
                }
            }, SCAN_PERIOD);
            //mBluetoothAdapter.startLeScan(mLeScanCallback); //开始搜索
            scanstart();
        } else {
            return;
        }
        /*else {
            //mBluetoothAdapter.stopLeScan(mLeScanCallback);//停止搜索
            scanstop();
        }*/
    }


    public static void scanstart() {
        isScanning = true;
        name_list.clear();
        address_list.clear();
        if (!isBlueEnable()) {
            Log.e("scanstart", "Bluetooth not enable!");
            isScanning = false;
            return;
        }
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        if (isClassicBluetooth) {
            scanBlue();
        }
        if (!isClassicBluetooth && isScanning) {
            setTextView("开始扫描ble设备");
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            //安卓6.0及以下版本BLE操作的代码
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            // 安卓7.0及以上版本BLE操作的代码
            scanner.startScan(scanCallback);
        }
    }

    public static void scanstop() {
        if (!isBlueEnable()) {
            Log.e("scanstop", "Bluetooth not enable!");
            isScanning = false;
            return;
        }
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            //this.unregisterReceiver(mReceiver);
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            //安卓6.0及以下版本BLE操作的代码
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        } else {
            // 安卓7.0及以上版本BLE操作的代码
            scanner.stopScan(scanCallback);
        }
        if (!isClassicBluetooth && isScanning) {
            setTextView("扫描完毕");
        }
        isScanning = false;
    }


    public static void scanBlue() {
        if (!isBlueEnable()) {
            Log.e("scanBlue", "Bluetooth not enable!");
            return;
        }
        //当前是否在扫描，如果是就取消当前的扫描，重新扫描
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        //注册设备被发现时的广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mactivity.registerReceiver(mReceiver, filter);
        //注册一个搜索结束时的广播
        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mactivity.registerReceiver(mReceiver, filter2);
        setTextView("开始搜索蓝牙设备");
        //此方法是个异步操作，一般搜索12秒
        mBluetoothAdapter.startDiscovery();
    }

    public static BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String str="";
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device!=null){str=device.getName()+": "+device.getAddress();}
            Log.e("BroadcastReceiver", "action:" + action);
            switch (intent.getAction()) {
                case BluetoothDevice.ACTION_FOUND:
                    Log.e("BroadcastReceiver", "发现蓝牙设备");
                    Log.e("BroadcastReceiver", "btInfo:" + str);
                    if (!address_list.contains(device.getAddress())) {
                        setTextView(str);
                        name_list.add(device.getName());
                        address_list.add(device.getAddress());
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.e("BroadcastReceiver", "搜索完毕");
                    if (!textView.getText().toString().endsWith("搜索完毕\n")) {
                        setTextView("搜索完毕");
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    setTextViewState(str+" 已连接");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    setTextViewState(str+" 已断开");
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_ON:
                            setTextViewState("正在打开");
                            Log.e("BluetoothState", "STATE_TURNING_ON");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            setTextViewState("打开");
                            Log.e("BluetoothState", "STATE_ON");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            setTextViewState("正在关闭");
                            Log.e("BluetoothState", "STATE_TURNING_OFF");
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            setTextViewState("关闭");
                            Log.e("BluetoothState", "STATE_OFF");
                            break;
                    }
                    break;
            }

        }
    };

    //region Android M 以上的回调
    private static ScanCallback scanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            int rssi = result.getRssi();
            //获取rssi
            // 这里写你自己的逻辑
            if (!address_list.contains(device.getAddress())) {
                setTextView(device.getName() + ": " + device.getAddress());
                name_list.add(device.getName());
                address_list.add(device.getAddress());
            }
            Log.e(device.getName() + ": " + device.getAddress(), "run: ");
        }
    };

    //region Android M 以下的回调
    private static BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            //这里是个子线程，下面把它转换成主线程处理
            mactivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //在这里可以把搜索到的设备保存起来
                    //bluetoothdevice.getName();获取蓝牙设备名字
                    //bluetoothdevice.getAddress();获取蓝牙设备mac地址
                    //这里的rssi即信号强度，即手机与设备之间的信号强度。
                    if (!address_list.contains(device.getAddress())) {
                        setTextView(device.getName() + ": " + device.getAddress());
                        name_list.add(device.getName());
                        address_list.add(device.getAddress());
                    }
                    Log.e(device.getName() + ": " + device.getAddress(), "run: ");
                }
            });
        }

    };
}
