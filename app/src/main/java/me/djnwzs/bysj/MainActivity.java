package me.djnwzs.bysj;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import static me.djnwzs.bysj.Dialog.*;
import static me.djnwzs.bysj.ScanDevice.*;


public class MainActivity extends AppCompatActivity {

    public static byte[] PHS = {(byte) 0x06, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x45, (byte) 0xBE};
    public static byte[] CS = {(byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x44, (byte) 0x09};
    public static MainActivity mactivity;

    private int m;
    public static BluetoothAdapter mBluetoothAdapter;
    public static Handler mHandler = new Handler();
    public static BluetoothGattCharacteristic mCharacteristic;
    public static BluetoothGattCharacteristic mCharacteristic2;
    public static BluetoothDevice bluetoothdevice;
    public static BluetoothGatt mBluetoothGatt;


    public static TextView textView;
    private static TextView textViewPH;
    private static TextView textViewPHT;
    private static TextView textViewC;
    private static TextView textViewCT;
    private static TextView textViewState;
    private static Button button;
    private static Button button1;
    private static Button button2;
    private static Button button3;
    private static Button button4;
    private static ScrollView scrollView;

    public static Boolean isScanning = false;//是否正在搜索
    public static Boolean isClassicBluetooth = false;
    public static Boolean isReadPH = false;
    public static Boolean isReadConductivity = false;
    public static ArrayList<String> name_list = new ArrayList<>();
    public static ArrayList<String> address_list = new ArrayList<>();
    public static List<BluetoothGattService> serviceList;//服务
    public static List<BluetoothGattCharacteristic> characterList;//特征
    public static BluetoothLeScanner scanner;//用过单例的方式获取实例

    public static void setTextViewState(final String string) {
        Runnable setTextViewState = new Runnable() {
            public void run() {
                textViewState.setText("蓝牙状态: " + string);
            }
        };
        mHandler.post(setTextViewState);
    }

    public static void setTextView(String string) {
        textView.append(string + "\n");
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }


    private Runnable readPH = new Runnable() {
        public void run() {
            mHandler.postDelayed(this, 1000);//设置延迟时间，此处是1秒
            if (mCharacteristic2 == null || mBluetoothGatt == null) {
                textViewPH.setText("");
                textViewPHT.setText("");
                isReadPH = false;
                mHandler.removeCallbacksAndMessages(null);
                return;
            }
            if (mCharacteristic2 != null && mBluetoothGatt != null) {
                try {
                    SendData.byteSend(PHS);
                    isReadPH = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

//            mBluetoothGatt.setCharacteristicNotification(mCharacteristic2, true);
            mBluetoothGatt.readCharacteristic(mCharacteristic2);
//            textViewPH.setText(mCharacteristic2.getValue().toString());
            if (mCharacteristic2.getValue() != null && isReadPH == true) {
                try {
                    byte[] bytes = mCharacteristic2.getValue();
                    if (bytes[0] != (byte) 0x06 || bytes[1] != (byte) 0x03 || bytes[2] != (byte) 0x08) return;
                    double PH = 0;
                    double PHT = 0;
                    int n, m = 0;
                    n = (bytes[6] & 0xFF);
                    PH = ((bytes[3] & 0xFF) << 8) + (bytes[4] & 0xFF);
                    PH = PH / Math.pow(10, n);
                    m = (bytes[10] & 0xFF);
                    PHT = ((bytes[7] & 0xFF) << 8) + (bytes[8] & 0xFF);
                    PHT = PHT / Math.pow(10, m);
                    textViewPH.setText(String.valueOf(PH));
                    textViewPHT.setText(String.valueOf(PHT));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                textViewPH.setText("");
                textViewPHT.setText("");
                isReadPH = false;
                return;
            }
        }
    };

    private Runnable readConductivity = new Runnable() {
        public void run() {
            mHandler.postDelayed(this, 1000);//设置延迟时间，此处是1秒
            if (mCharacteristic2 == null || mBluetoothGatt == null) {
                textViewC.setText("");
                textViewCT.setText("");
                isReadConductivity = false;
                mHandler.removeCallbacksAndMessages(null);
                return;
            }
            if (mCharacteristic2 != null && mBluetoothGatt != null) {
                try {
                    SendData.byteSend(CS);
                    isReadConductivity = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //mBluetoothGatt.setCharacteristicNotification(mCharacteristic2, true);
            mBluetoothGatt.readCharacteristic(mCharacteristic2);
            if (mCharacteristic2.getValue() != null && isReadConductivity == true) {
                try {
                    byte[] bytes = mCharacteristic2.getValue();
                    if (bytes[0] != (byte) 0x01 || bytes[1] != (byte) 0x03 || bytes[2] != (byte) 0x08)
                        return;
                    double C = 0;
                    double CT = 0;
                    int n, m = 0;
                    n = (bytes[6] & 0xFF);
                    C = ((bytes[3] & 0xFF) << 8) + (bytes[4] & 0xFF);
                    C = C / Math.pow(10, n);
                    m = (bytes[10] & 0xFF);
                    CT = ((bytes[7] & 0xFF) << 8) + (bytes[8] & 0xFF);
                    CT = CT / Math.pow(10, m);
                    textViewC.setText(String.valueOf(C));
                    textViewCT.setText(String.valueOf(CT));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                textViewC.setText("");
                textViewCT.setText("");
                isReadConductivity = false;
                return;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mactivity = this;
        setContentView(R.layout.activity_main);
        checkPermissions();
        View();
        bluetooth_enable();
        if (isBlueEnable()) {
            setTextViewState("打开");
        } else {
            setTextViewState("关闭");
        }
        IntentFilter stateChangeFilter = new IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED);
        IntentFilter connectedFilter = new IntentFilter(
                BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter disConnectedFilter = new IntentFilter(
                BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mactivity.registerReceiver(mReceiver, stateChangeFilter);
        mactivity.registerReceiver(mReceiver, connectedFilter);
        mactivity.registerReceiver(mReceiver, disConnectedFilter);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetooth_enable();
                scanDevice(!isScanning);
            }


        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScanning) {
                    scanstop();
                    Toast.makeText(MainActivity.this, "已暂停扫描", Toast.LENGTH_SHORT).show();
                }
                dialog_list();
                //bluetoothdevice = mBluetoothAdapter.getRemoteDevice(address);
                //mBluetoothGatt = bluetoothdevice.connectGatt(MainActivity.this, false, mBluetoothGattCallback);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothGatt != null) {
                    try {
                        mBluetoothGatt.disconnect();//主动断开连接
                        mBluetoothGatt.close();
                        mBluetoothGatt = null;
                    } catch (Exception e) {
                        Log.e("blueTooth", "断开蓝牙失败");
                        Toast.makeText(MainActivity.this, "断开蓝牙失败", Toast.LENGTH_SHORT).show();
                    }
                }
                if (isClassicBluetooth) {
                    try {
                        BluetoothSocket socket = bluetoothdevice.createRfcommSocketToServiceRecord(UUID
                                .fromString("00001101-0000-1000-8000-00805F9B34FB"));
                        //停止搜索
                        mBluetoothAdapter.cancelDiscovery();
                        //连接
                        socket.close();
                        //创建蓝牙客户端线程
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //runPH();
                if (mCharacteristic2 != null && !isReadPH) {
                    mHandler.post(readPH);
                }
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //runC();
                if (mCharacteristic2 != null && !isReadConductivity) {
                    mHandler.post(readConductivity);
                }
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem setting = menu.findItem(R.id.action_settings);
        if (isClassicBluetooth) {
            setting.setTitle("切换至低功耗蓝牙");
        } else {
            setting.setTitle("切换至经典蓝牙");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            if (isScanning) {
                scanstop();
                name_list.clear();
                address_list.clear();
                Toast.makeText(MainActivity.this, "已暂停扫描", Toast.LENGTH_SHORT).show();
            }
            isClassicBluetooth = !isClassicBluetooth;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void bluetooth_enable() {
        if (!isBlueEnable()) {
            Log.d("00000", "Bluetooth is NOT switched on");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(enableBtIntent, 1);
            //this.startActivity(enableBtIntent);
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
    }


    /**
     * 设备是否支持蓝牙  true为支持
     *
     * @return
     */
    public static boolean isSupportBlue() {
        return mBluetoothAdapter != null;
    }

    /**
     * 蓝牙是否打开   true为打开
     *
     * @return
     */
    public static boolean isBlueEnable() {
        return isSupportBlue() && mBluetoothAdapter.isEnabled();
    }

    /**
     * 检查权限
     */
    private final int REQUEST_CODE_PERMISSION_LOCATION = 1;

    private void checkPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    /**
     * 权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;
        }
    }

    /**
     * 开启GPS
     *
     * @param permission
     */
    private final int REQUEST_CODE_OPEN_GPS = 1;

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("当前手机扫描蓝牙需要打开定位功能。")
                            .setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton("前往设置",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                    //GPS已经开启了
                }
                break;
        }
    }

    /**
     * 检查GPS是否打开
     *
     * @return
     */
    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    private void View() {
        textView = (TextView) findViewById(R.id.textView);
        textViewPH = (TextView) findViewById(R.id.textView3);
        textViewPHT = (TextView) findViewById(R.id.textView4);
        textViewC = (TextView) findViewById(R.id.textView5);
        textViewCT = (TextView) findViewById(R.id.textView6);
        textViewState = (TextView) findViewById(R.id.textView7);
        button = (Button) findViewById(R.id.button);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
    }

}
