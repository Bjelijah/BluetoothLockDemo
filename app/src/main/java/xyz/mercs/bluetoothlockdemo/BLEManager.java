package xyz.mercs.bluetoothlockdemo;

import android.content.Context;

import com.cbj.sdk.libbase.utils.LOG;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.receiver.listener.BluetoothBondListener;

import java.util.UUID;

import xyz.mercs.bluetoothlockdemo.util.Util;

import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

public class BLEManager {

    public static BLEManager sInstance = new BLEManager();

    public BluetoothClient mClient = null;

    String mac = "";

    UUID serverUUId = null;
    UUID characterUUId_w = null;
    UUID characterUUId_r = null;

    interface INotifyListener{
        public void onNotify(byte [] buf);
    }
    INotifyListener mCb = null;

    private final BluetoothBondListener mBluetoothBondListener = new BluetoothBondListener() {
        @Override
        public void onBondStateChanged(String mac, int bondState) {
            // bondState = Constants.BOND_NONE, BOND_BONDING, BOND_BONDED
        }
    };

    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {

        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == STATUS_CONNECTED) {
                LOG.INSTANCE.I("123","STATUS_CONNECTED");
            } else if (status == STATUS_DISCONNECTED) {
                LOG.INSTANCE.I("123","STATUS_DISCONNECTED");
            }
        }
    };
    public void deinit(){
        closeNotify();
        mClient.unregisterConnectStatusListener(mac,mBleConnectStatusListener);
    }

    public void init(Context c,String mac,INotifyListener cb){
        mCb = cb;
        this.mac = mac;
        mClient = new BluetoothClient(c);
        mClient.registerConnectStatusListener(mac,mBleConnectStatusListener);
    }

    public void connect(){
        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)   // 连接如果失败重试3次
                .setConnectTimeout(30000)   // 连接超时30s
                .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
                .setServiceDiscoverTimeout(20000)  // 发现服务超时20s
                .build();
        mClient.connect(mac, options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile data) {
                LOG.INSTANCE.I("123","connect res  code="+code+" data="+data);
                LOG.INSTANCE.I("123","service size="+data.getServices().size());
                for (int i=0;i<data.getServices().size();i++){
                    BleGattService service = data.getServices().get(i);
                    if (service.getUUID().toString().contains("fee7")){
                        serverUUId = service.getUUID();
                        if (service.getCharacters().size()>0){
                            BleGattCharacter character = service.getCharacters().get(0);
                            characterUUId_w = character.getUuid();
                            character = service.getCharacters().get(1);
                            characterUUId_r = character.getUuid();
                            openNotify();
                            break;
                        }

                    }
                }
            }
        });
    }

    public void write(byte [] buf){


        if (serverUUId==null || characterUUId_w==null)return;
        mClient.write(mac, serverUUId, characterUUId_w, buf, new BleWriteResponse() {
            @Override
            public void onResponse(int code) {
                LOG.INSTANCE.I("123","write code="+code);
            }
        });
    }

    public void read(){
        if (serverUUId==null || characterUUId_r==null)return;
        mClient.read(mac, serverUUId, characterUUId_r, new BleReadResponse() {
            @Override
            public void onResponse(int code, byte[] data) {
                LOG.INSTANCE.I("123","read onResponse code="+code+" data="+data);
                for (int i=0;i<data.length;i++){
                    LOG.INSTANCE.I("123","read data ["+i+"] "+String.format("0x%x",data[i]));
                }
                Util.decryptMsg(data);
            }
        });
    }

    public void openNotify(){
        mClient.notify(mac, serverUUId, characterUUId_r, new BleNotifyResponse() {
            @Override
            public void onNotify(UUID service, UUID character, byte[] value) {
                LOG.INSTANCE.I("123","onNotify service="+service+" character="+character+" byte="+Util.byte2HexStr(value));
                mCb.onNotify(Util.decryptMsg(value));
            }

            @Override
            public void onResponse(int code) {
                LOG.INSTANCE.I("123","notify onResponse code="+code);
            }
        });
    }

    public void closeNotify(){
        mClient.unnotify(mac, serverUUId, characterUUId_r, new BleUnnotifyResponse() {
            @Override
            public void onResponse(int code) {

            }
        });
    }
}
