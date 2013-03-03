
// Copyright (c) 2012-2013 Arvin Schnell

package com.example.helloremote;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import android.preference.PreferenceManager;


public class HelloRemote extends TabActivity
{
    private static final String TAG = "Remote";

    private BlockingQueue<String> send_queue = new LinkedBlockingQueue<String>();

    private Socket socket;

    private Haha send_thread;
    private Huhu receive_thread;

    private BufferedReader buffered_reader;
    private BufferedWriter buffered_writer;

    private Handler handler = new Handler();


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	Log.i(TAG, "onCreate");

	setContentView(R.layout.main);

	PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

	TabHost mTabHost = getTabHost();

	TabSpec tab1 = mTabHost.newTabSpec("mixer");
	tab1.setIndicator(getString(R.string.mixer));
	tab1.setContent(R.id.textview1);
	mTabHost.addTab(tab1);

	TabSpec tab2 = mTabHost.newTabSpec("mplayer");
	tab2.setIndicator(getString(R.string.mplayer));
	tab2.setContent(R.id.textview2);
	mTabHost.addTab(tab2);

	TabSpec tab3 = mTabHost.newTabSpec("xmms");
	tab3.setIndicator(getString(R.string.xmms));
	tab3.setContent(R.id.textview3);
	mTabHost.addTab(tab3);

	mTabHost.setCurrentTab(0);

	helper(R.id.mixer_master_up_1, "mixer", "volume", "Master", "+1dB");
	helper(R.id.mixer_master_down_1, "mixer", "volume", "Master", "-1dB");
	helper(R.id.mixer_master_down_20, "mixer", "volume", "Master", "-20dB");
	helper(R.id.mixer_master_switch, "mixer", "switch", "Master");
	helper(R.id.mixer_pcm_up_1, "mixer", "volume", "PCM", "+1dB");
	helper(R.id.mixer_pcm_down_1, "mixer", "volume", "PCM", "-1dB");
	helper(R.id.mixer_pcm_down_20, "mixer", "volume", "PCM", "-20dB");
	helper(R.id.mixer_pcm_switch, "mixer", "switch", "PCM");
	helper(R.id.mixer_front_up_1, "mixer", "volume", "Front", "+1dB");
	helper(R.id.mixer_front_down_1, "mixer", "volume", "Front", "-1dB");
	helper(R.id.mixer_front_down_20, "mixer", "volume", "Front", "-20dB");
	helper(R.id.mixer_front_switch, "mixer", "switch", "Front");
	helper(R.id.mixer_reset, "mixer", "reset");

	helper(R.id.mplayer_volume_up, "mplayer", "volume", "up");
	helper(R.id.mplayer_volume_down, "mplayer", "volume", "down");
	helper(R.id.mplayer_forward_10s, "mplayer", "forward", "10s");
	helper(R.id.mplayer_backward_10s, "mplayer", "backward", "10s");
	helper(R.id.mplayer_forward_1m, "mplayer", "forward", "1m");
	helper(R.id.mplayer_backward_1m, "mplayer", "backward", "1m");
	helper(R.id.mplayer_forward_10m, "mplayer", "forward", "10m");
	helper(R.id.mplayer_backward_10m, "mplayer", "backward", "10m");
	helper(R.id.mplayer_panscan_plus, "mplayer", "panscan", "plus");
	helper(R.id.mplayer_panscan_minus, "mplayer", "panscan", "minus");
	helper(R.id.mplayer_pause, "mplayer", "pause");
	helper(R.id.mplayer_quit, "mplayer", "quit");
	helper(R.id.mplayer_osd, "mplayer", "osd");
	helper(R.id.mplayer_fullscreen, "mplayer", "fullscreen");

	helper(R.id.xmms_play, "xmms", "play");
	helper(R.id.xmms_pause, "xmms", "pause");
	helper(R.id.xmms_stop, "xmms", "stop");
	helper(R.id.xmms_forward_song, "xmms", "forward", "song");
	helper(R.id.xmms_backward_song, "xmms", "backward", "song");
	helper(R.id.xmms_forward_5s, "xmms", "forward", "5s");
	helper(R.id.xmms_backward_5s, "xmms", "backward", "5s");
	helper(R.id.xmms_quit, "xmms", "quit");
	helper(R.id.xmms_shuffle, "xmms", "shuffle");
	helper(R.id.xmms_repeat, "xmms", "repeat");
	helper(R.id.xmms_volume_up, "xmms", "volume", "up");
	helper(R.id.xmms_volume_down, "xmms", "volume", "down");
    }


    @Override
    public void onResume()
    {
	super.onResume();
	Log.i(TAG, "onResume");

	openConnection();
    }


    @Override
    public void onPause()
    {
	super.onPause();
	Log.i(TAG, "onPause");

	closeConnection();
    }


    private void openConnection()
    {
	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

	String name = preferences.getString("name", "");
	String port = preferences.getString("port", "51203");

	send_thread = new Haha(name, Integer.parseInt(port));
	send_thread.start();
    }


    private void closeConnection()
    {
	send_thread.interrupt();
    }


    private void helper(int id, String method, String param1)
    {
	findViewById(id).setOnClickListener(new Helper(method + " " + param1));
    }

    private void helper(int id, String method, String param1, String param2)
    {
	findViewById(id).setOnClickListener(new Helper(method + " " + param1 + " " + param2));
    }

    private void helper(int id, String method, String param1, String param2, String param3)
    {
	findViewById(id).setOnClickListener(new Helper(method + " " + param1 + " " + param2 + " " +
						       param3));
    }


    private void handle_message(String message)
    {
	String tokens[] = message.split(" ");

	if (tokens[0].equals("mixer"))
	{
	    if (tokens[1].equals("Master"))
	    {
		TextView volume = (TextView) findViewById(R.id.mixer_master_volume);
		volume.setText(tokens[2]);

		ToggleButton button = (ToggleButton) findViewById(R.id.mixer_master_switch);
		button.setChecked(tokens[3].equals("off"));
	    }
	    else if (tokens[1].equals("PCM"))
	    {
		TextView volume = (TextView) findViewById(R.id.mixer_pcm_volume);
		volume.setText(tokens[2]);
	    }
	    else if (tokens[1].equals("Front"))
	    {
		TextView volume = (TextView) findViewById(R.id.mixer_front_volume);
		volume.setText(tokens[2]);

		ToggleButton button = (ToggleButton) findViewById(R.id.mixer_front_switch);
		button.setChecked(tokens[3].equals("off"));
	    }
	}
    }


    class Helper implements OnClickListener
    {
	private String command;

	public Helper(String command)
	{
	    this.command = command;
	}

	public void onClick(View v)
	{
	    try
	    {
		send_queue.put(command);
	    }
	    catch (final InterruptedException e)
	    {
		Log.e(TAG, "InterruptedException");
	    }
	}
    }


    class Huhu extends Thread
    {
	@Override
	public void run()
	{
	    while (true)
	    {
		try
		{
		    final String message = buffered_reader.readLine();

		    handler.post(new Runnable() {
			@Override public void run() {
			    handle_message(message);
			}
		    });
		}
		catch (final IOException e)
		{
		    Log.e(TAG, "IOException");
		}
	    }
	}
    }


    class Haha extends Thread
    {
	private String name;
	private int port;


	public Haha(String name, int port)
	{
	    this.name = name;
	    this.port = port;
	}


	@Override
	public void run()
	{
	    Log.i(TAG, "socket: " + String.format("%s:%d", name, port));

	    try
	    {
		socket = new Socket(name, port);
		socket.setKeepAlive(true);

		buffered_reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		buffered_writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	    }
	    catch (final UnknownHostException e)
	    {
		Log.e(TAG, "UnknownHostException");
	    }
	    catch (final IOException e)
	    {
		Log.e(TAG, "IOException");
	    }

	    receive_thread = new Huhu();
	    receive_thread.start();

	    while (true)
	    {
		try
		{
		    String command = send_queue.take();
		    buffered_writer.write(command, 0, command.length());
		    buffered_writer.newLine();
		    buffered_writer.flush();
		}
		catch (final InterruptedException e)
		{
		    Log.e(TAG, "InterruptedException");
		    break;
		}
		catch (final IOException e)
		{
		    Log.e(TAG, "IOException");
		}
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.actions, menu);
	return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
	switch (item.getItemId())
	{
	    case R.id.preferences:
		Intent intent = new Intent(this, Preferences.class);
		startActivity(intent);
		break;
	}

	return true;
    }
}
