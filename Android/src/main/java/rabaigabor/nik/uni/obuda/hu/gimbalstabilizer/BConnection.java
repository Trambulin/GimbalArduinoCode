package rabaigabor.nik.uni.obuda.hu.gimbalstabilizer;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by TramNote on 2016.01.14..
 */
public class BConnection extends Application {

    final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private ConnectedThread conThread;
    private GimbalOrientation gimbal;
    private ConnectThread clientT;

    public boolean isConnected=false;

    @Override
    public void onCreate(){
        super.onCreate();
        gimbal=new GimbalOrientation();
    }

    public void initConnect(BluetoothDevice dev, BluetoothAdapter bAdapter){
        clientT=new ConnectThread(dev,bAdapter);
        clientT.start();
    }

    private final Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    conThread=new ConnectedThread((BluetoothSocket)msg.obj);
                    byte[] tmp=new byte[1];
                    tmp[0]=55;
                    conThread.write(tmp);
//                    String s="success connect";
//                    conThread.write(s.getBytes());
                    isConnected=true;
                    conThread.start();
                    break;
                case 1:
                    byte[] readbuffer=(byte[])msg.obj;
                    getGimbal().newData(readbuffer,msg.arg1);
//                    String sr=new String(readbuffer);
//                    listAdapt.add(sr.substring(0,msg.arg1)); //arg1 a byte db szám
                    break;
            }
        }
    };

    public GimbalOrientation getGimbal() {
        return gimbal;
    }

    public ConnectedThread getConThread() {
        return conThread;
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[2048];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(1, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final BluetoothAdapter mBluetoothAdapter;

        public ConnectThread(BluetoothDevice device, BluetoothAdapter bAdapter) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mBluetoothAdapter=bAdapter;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            mHandler.obtainMessage(0,mmSocket).sendToTarget();
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}
