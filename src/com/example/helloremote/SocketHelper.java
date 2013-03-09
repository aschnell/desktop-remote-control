
// Copyright (c) 2012-2013 Arvin Schnell

package com.example.helloremote;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Message;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


class SocketHelper extends Thread
{
    private static final String TAG = "Remote";

    private String name;
    private int port;

    private Socket socket;

    private SendThread send_thread;
    private BufferedWriter buffered_writer;

    private ReceiveThread receive_thread;
    private BufferedReader buffered_reader;

    private OnReceiveListener on_receive_listener = null;

    private Handler handler = null;


    public SocketHelper(String name, int port)
    {
	this.name = name;
	this.port = port;

	handler = new Handler();

	send_thread = new SendThread(this);
	receive_thread = new ReceiveThread(this);
    }


    public void send(String str)
    {
	if (send_thread == null || send_thread.handler == null)
	    return;

	Message message = Message.obtain();
	message.obj = str;
	send_thread.handler.sendMessage(message);
    }


    public interface OnReceiveListener
    {
	void onReceive(SocketHelper socket_helper, String str);
    }


    public void setOnReceiveListener(OnReceiveListener listener)
    {
	on_receive_listener = listener;
    }


    public void callOnReceive(String str)
    {
	if (on_receive_listener == null)
	    return;

	on_receive_listener.onReceive(this, str);
    }


    @Override
    public void run()
    {
	Log.i(TAG, "socket: " + String.format("%s:%d", name, port));

	try
	{
	    socket = new Socket(name, port);
	    socket.setKeepAlive(true);

	    buffered_writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	    buffered_reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	catch (final UnknownHostException e)
	{
	    Log.e(TAG, "UnknownHostException");
	    return;
	}
	catch (final IOException e)
	{
	    Log.e(TAG, "IOException");
	    return;
	}

	send_thread.start();
	receive_thread.start();

	try
	{
	    send_thread.join();
	    receive_thread.join();
	}
	catch (final InterruptedException e)
	{
	    Log.e(TAG, "InterruptedException");
	}

	try
	{
	    socket.close();
	}
	catch (final IOException e)
	{
	    Log.e(TAG, "IOException");
	}
    }


    class SendThread extends Thread
    {
	SocketHelper socket_helper = null;

	public Handler handler = null;

	public SendThread(SocketHelper socket_helper)
	{
	    this.socket_helper = socket_helper;
	}

	@Override
	public void run()
	{
	    Looper.prepare();

	    handler = new Handler() {
		@Override
		public void handleMessage(Message message)
		{
		    try
		    {
			String str = (String) message.obj;
			socket_helper.buffered_writer.write(str, 0, str.length());
			socket_helper.buffered_writer.newLine();
			socket_helper.buffered_writer.flush();
		    }
		    catch (final IOException e)
		    {
			Log.e(TAG, "IOException");
		    }
		}
	    };

	    Looper.loop();
	}
    }


    class ReceiveThread extends Thread
    {
	SocketHelper socket_helper = null;

	public ReceiveThread(SocketHelper socket_helper)
	{
	    this.socket_helper = socket_helper;
	}

	@Override
	public void run()
	{
	    try
	    {
		while (true)
		{
		    final String str = socket_helper.buffered_reader.readLine();
		    if (str == null)
			break;

		    socket_helper.handler.post(new Runnable() {
			@Override
			public void run()
			{
			    callOnReceive(str);
			}
		    });
		}
	    }
	    catch (final IOException e)
	    {
		Log.e(TAG, "IOException");
	    }
	}
    }
}
