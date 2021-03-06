package com.example.bluetooth_chat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class ChatUtils {
    private Context context;
    //In android Handler is mainly used to update the main thread from
    // background thread or other than main thread.
    private final android.os.Handler handler;
    //bluetooth adapter
    private BluetoothAdapter bluetoothAdapter;

    private ConnectThread connectThread;

    private AcceptThread acceptThread;

    private final UUID APP_UUID = UUID.fromString("05067dec-e350-11eb-ba80-0242ac130004");
    private final String APP_NAME = "BluetoothChatApp";

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING =2;
    public static final int STATE_CONNECTED =3;
    private int state;

    public ChatUtils(Context context, Handler handler){
        this.context = context;
        this.handler = handler;
        state = STATE_NONE;

        //Get a handle to the default local Bluetooth adapter.
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    public int getState() {
        return state;
    }
    //they're preventing two threads from accessing the same variable from different sections of code.
    //state should be changed and synchronized
    //bluetooth is an synchronized device
    public synchronized void setState(int state) {
        this.state = state;
        //send the state back to the main activity
        //Value to assign to the returned Message.what field.
        //Value to assign to the returned Message.obj field. This value may be null.
        //Returns a new Message from the global message pool. More efficient than creating and allocating new instances.
        // The retrieved message has its handler set to this instance
        //hen you call msg.sendToTarget(), it is delivered to that handler.
        handler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGED,state,-1).sendToTarget();
    }

    private synchronized void start(){
        //check if there is any instance of the connecting thread
        if(connectThread!=null){
            connectThread.cancel();
            connectThread = null;
        }
        if(acceptThread == null){
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    public synchronized void stop(){
        if(connectThread!= null){
            connectThread.cancel();
            connectThread = null;
        }
        if(acceptThread != null){
            acceptThread.cancel();
            acceptThread = null;
        }
        setState(STATE_NONE);
    }

    //this will be called from the main activity and will be start all the working of the chat-utils
    public void connect(BluetoothDevice device){
        if(state == STATE_CONNECTING){
            //we will stop the connection and will restart it
            connectThread.cancel();
            connectThread = null;
        }
        connectThread = new ConnectThread(device);
        connectThread.start();

        setState(STATE_CONNECTING);
    }

    //now we have to add a thread which will accept these connections
    private class AcceptThread extends Thread{
        //A listening Bluetooth socket.
        private BluetoothServerSocket serverSocket;

        public AcceptThread(){
            //create a temporary server socket
            BluetoothServerSocket tmp = null;
            try {
                //A remote device connecting to this socket will be authenticated and
                // communication on this socket will be encrypted.
                //The system will also register a Service Discovery Protocol (SDP) record with the local
                // SDP server containing the specified UUID, service name, and auto-assigned channel.
                //service name for SDP record
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME,APP_UUID);
            }catch (IOException e){
                Log.e("Accept-Constructor",e.toString());

            }
            //if the above code gets executed without throwing any exceptions
            serverSocket =tmp;
        }
        //here we will try to connect the serverSocet
        public void run(){
            BluetoothSocket socket = null;
            try{
                //After the listening BluetoothServerSocket is created,
                // call accept() to listen for incoming connection requests.
                // This call will block until a connection is established,
                // at which point, it will return a BluetoothSocket to manage the connection.
                socket = serverSocket.accept();
            }catch (IOException e){
                Log.e("ACCEPT - RUn",e.toString());
                //if exception is there we will try to close our serve socket
                try{
                    //Closing the BluetoothServerSocket will not close any BluetoothSocket received from accept().
                    serverSocket.close();
                }catch (IOException e1){
                    Log.e("ACCEPT-CLOSE",e.toString());
                }
            }
            //if accept was successful
            if(socket!=null){
                switch (state){
                    case STATE_LISTEN:
                    case STATE_CONNECTING:
                        connect(socket.getRemoteDevice());
                        break;
                    case STATE_NONE:
                    case STATE_CONNECTED:
                        try{
                            socket.close();
                        }catch (IOException e){
                            Log.e("ACCEPT-CLOSE-SOCKET",e.toString());
                        }
                        break;
                }
            }
        }
        public void cancel(){
            try{
                serverSocket.close();
            }catch (IOException e){
                Log.e("CLOSE-SERVER-SOCKET",e.toString());
            }
        }

    }

    //thread which will handle all our connectivity
    //Thread class provide constructors and methods to create and perform operations on a thread.
    private class ConnectThread extends Thread{
        //The most common type of Bluetooth socket is RFCOMM, which is the type supported by the Android APIs.
        // RFCOMM is a connection-oriented, streaming transport over Bluetooth.
        //A connected or connecting Bluetooth socket.
        //On the client side, use a single BluetoothSocket to both initiate an outgoing connection
        // and to manage the connection.
        private final BluetoothSocket bluetoothSocket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device){
            this.device = device;
            //a temporary socket
            BluetoothSocket tmp = null;
            try {
                //To create a BluetoothSocket for connecting to a known device,
                // use BluetoothDevice.createRfcommSocketToServiceRecord(). Then call connect()
                // to attempt a connection to the remote device.
                // This call will block until a connection is established or the connection fails.
                //Create an RFCOMM BluetoothSocket ready to start a secure outgoing connection to
                // this remote device using SDP lookup of uuid.
                // immutable universally unique identifier (UUID). A UUID represents a 128-bit value.
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
            }catch (IOException e){
                Log.e("Connect-constructor",e.toString());
            }
            bluetoothSocket =tmp;

        }
        public void run(){
            //try to connect our socket
            try{
                bluetoothSocket.connect();
            }catch (IOException e){
                Log.e("Constructor-Run",e.toString());
                //Closes this stream and releases any system resources associated with it.
                // If the stream is already closed then invoking this method has no effect.
                try{
                    bluetoothSocket.close();
                }catch (IOException e1){
                    Log.e("Connect-closeSocket",e1.toString());

                }
                //now to handle the ioexception
                connectionFailed();
                return;

            }
            //It means that this block of code is synchronized meaning no more than
            // one thread will be able to access the code inside that block.
            synchronized (ChatUtils.this){
                connectThread = null;
            }
            connected(device);
        }

        public void cancel(){
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e("CONNECT-CANCEL",e.toString());
            }
        }

    }

    private synchronized void connectionFailed(){
        //send a toast message to the main activity stating that our connection has failed
        Message message = handler.obtainMessage(MainActivity.MESSAGE_TOAST);
        //A mapping from String keys to various Parcelable values.
        Bundle bundle = new Bundle();
        //Inserts a String value into the mapping of this Bundle, replacing any existing value for the given key.
        bundle.putString(MainActivity.TOAST,"Can't Connect to the device");
        //Sets a Bundle of arbitrary data values.
        message.setData(bundle);
        //Pushes a message onto the end of the message queue after all pending messages before the current time.
        // It will be received in handleMessage(Message), in the thread attached to this handler.
        handler.sendMessage(message);
        //restart our chat utility to start listening again
        ChatUtils.this.start();
    }

    private synchronized void connected(BluetoothDevice device){
        //now we need close all the connected threads because we have already got the connected device
        if(connectThread != null){
                connectThread.cancel();
                connectThread = null;
        }
        //send the device name
        Message message = handler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME,device.getName());
        message.setData(bundle);
        handler.sendMessage(message);

        //set the state to connected
        setState(STATE_CONNECTED);

    }
}
