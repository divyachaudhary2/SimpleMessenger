package edu.buffalo.cse.cse486586.simplemessenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.*;

public class SimpleMessengerActivity extends Activity {
	int port;
	String ip ="10.0.2.2";
	String portStr=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simplemessenger);

		TelephonyManager tel =(TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);

		try{
			ServerSocket serverSocket = new ServerSocket (10000);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , serverSocket);

		}catch(Exception e){
			Log.v("ERROR", "In Server establish");
		}

		final EditText editText = (EditText)findViewById(R.id.editText1);
		editText.setOnKeyListener(new OnKeyListener(){ 
			public boolean onKey(View v , int keyCode, KeyEvent event){
				if((event.getAction()==KeyEvent.ACTION_DOWN)&&(keyCode==KeyEvent.KEYCODE_ENTER)){
					String msg=editText.getText().toString()+"\n";
					editText.setText("");
					new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,msg);
				}
				return false;
			}
		});
	}

	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.simplemessenger, menu);
		return true;
	}


	private class ClientTask extends AsyncTask<String, Void, Void> {

		protected Void doInBackground(String... msgs) {
			try {
				if(portStr.equals("5554")){
					port= 11112;
				}else if (portStr.equals("5556")) {
					port=11108;
				} 
				Socket socket=new Socket(ip, port);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				out.print(msgs[0]);
				out.flush();
				socket.close();
			} catch (UnknownHostException e) {
				Log.v("ERROR", "Could not create");
			} catch (IOException e) {
				Log.v("ERROR", "Could not create");
			}

			return null;
		}
	}


	private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

		@Override
		protected Void doInBackground(ServerSocket... sockets) {
			ServerSocket serverSocket = sockets[0];
			Socket socket=null;

			try{
				while(true){
					socket = serverSocket.accept();
					Log.v("Setting up connection", "Connected");
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String msg = in.readLine();
					publishProgress(msg);
					socket.close();
				}
			}

			catch(IOException e){
				Log.v("ERROR", "In server creation");
				//e.printStackTrace();
			}


			return null;
		}

		protected void onProgressUpdate(String... strings ){
			super.onProgressUpdate(strings[0]);
			TextView textView = (TextView) findViewById(R.id.textView1);
			textView.append(strings[0] + "\n");
			Log.v("Server reading message", strings[0]);
			return;
		}
	}
}

