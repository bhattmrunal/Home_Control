package com.example.androidsocketclient;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import android.os.Handler;


public class MainActivity extends Activity {

    protected static final int RESULT_SPEECH = 1;
    private static final int SERVERPORT = 8080;
    String SERVER_IP="0.0.0.0";
    String HomeId="*1302";
    public ToggleButton lamp1;
    public Button mic, button2;
    public TextView voiceCommandBox, regulatorStatusLabel;
    public EditText edText1;
    public SeekBar regulator1;
    String regulatorProgress="0";
    Thread t;
    Drawable lamp1_on_image, lamp1_off_image;
    private Socket socket;
    Handler updateConversationHandler;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences shrPreferences=getApplicationContext().getSharedPreferences("UISettings", Context.MODE_PRIVATE);
        SERVER_IP=shrPreferences.getString("IP_Address","");
        regulator1= (SeekBar) findViewById(R.id.regulator1);
        regulatorStatusLabel =(TextView) findViewById(R.id.textView);
        voiceCommandBox = (TextView) findViewById(R.id.textView1);
        mic = (Button) findViewById(R.id.button1);
        lamp1_on_image = getResources().getDrawable(R.drawable.light_on);
        lamp1_off_image = getResources().getDrawable(R.drawable.light_off);
        lamp1 = (ToggleButton) findViewById(R.id.toggleButton1);

        addClickListener_toggleButton();
        addClickListener_micButton();

        t = new Thread(new ClientThread());
        t.start();
        regulator1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean b) {
                regulatorProgress = String.valueOf(progresValue);

                try {

                    regulatorStatusLabel.setText(regulatorProgress);
                    String command = HomeId + "0102" + regulatorProgress;
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    out.println(command);
                }
                catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent refreshIntnt = new Intent(getApplicationContext(),
                        Settings.class);
                startActivity(refreshIntnt);
                break;

		/*case R.id.action_refresh:
			Intent refreshIntnt = new Intent(getApplicationContext(),
					ThirdActivity.class);
			startActivity(refreshIntnt);
			break;*/

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addClickListener_micButton() {
        mic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

                try {
                    startActivityForResult(intent, RESULT_SPEECH);
                    voiceCommandBox.setText("");
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Ops! Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    voiceCommandBox.setText(text.get(0));
                    filter(text.get(0).toString());
                }
                break;
            }

        }
    }

    void filter(String line) {

        if( line.toLowerCase().contains("dim") || line.toLowerCase().contains("reduce") || line.toLowerCase().contains("dimmer") || line.toLowerCase().contains("less") || line.toLowerCase().contains("decrease"))
        {
            if(line.toLowerCase().contains("fan") || line.toLowerCase().contains("light") || line.toLowerCase().contains("lights") || line.toLowerCase().contains("lamp") || line.toLowerCase().contains("intensity"))
            {
                int regProgress=Integer.parseInt(regulatorProgress);
                if(regProgress>0)
                    regulator1.setProgress(regProgress-1);

                Toast to = Toast.makeText(getApplicationContext(),
                        "Regulator reduced!!!",
                        Toast.LENGTH_SHORT);
            }
        }
        else if(line.toLowerCase().contains("increase") || line.toLowerCase().contains("brighter") || line.toLowerCase().contains("more") || line.toLowerCase().contains("bright"))
        {
            if(line.toLowerCase().contains("fan") || line.toLowerCase().contains("light") || line.toLowerCase().contains("lights") || line.toLowerCase().contains("lamp") || line.toLowerCase().contains("intensity"))
            {
                int regProgress=Integer.parseInt(regulatorProgress);
                if(regProgress<5)
                    regulator1.setProgress(regProgress+1);

                Toast to = Toast.makeText(getApplicationContext(),
                        "Regulator Increased!!!",
                        Toast.LENGTH_SHORT);
            }
        }
        else if (line.toLowerCase().contains("lamp") || line.toLowerCase().contains("light") || line.toLowerCase().contains("bulb") || line.toLowerCase().contains("lights") || (line.toLowerCase().contains("dark"))) {

            if (line.toLowerCase().contains("so") || line.toLowerCase().contains("too") || line.toLowerCase().contains("on") && !line.toLowerCase().contains("not")) {
                Toast to = Toast.makeText(getApplicationContext(),
                        "Light on!!!",
                        Toast.LENGTH_SHORT);
                to.show();
                try {
                    t = new Thread(new ClientThread());
                    t.start();
                    String str = HomeId+"01011";
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    out.println(str);
                    socket.close();
                    lamp1.setChecked(true);
                    lamp1.setBackground(lamp1_on_image);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (line.toLowerCase().contains("off") && !line.toLowerCase().contains("not")) {
                try {
                    t = new Thread(new ClientThread());
                    t.start();
                    String str = HomeId+"01010";
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    out.println(str);
                    socket.close();
                    Toast to = Toast.makeText(getApplicationContext(),
                            "Light off!!!",
                            Toast.LENGTH_SHORT);
                    to.show();
                    lamp1.setChecked(false);
                    lamp1.setBackground(lamp1_off_image);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addClickListener_toggleButton() {

        lamp1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (lamp1.isChecked()) {
                    try {
                        t = new Thread(new ClientThread());
                        t.start();
                        String str = HomeId+"01011";
//                        Thread readThread=new Thread(new ReadFromServerThread(socket));
//                        readThread.start();
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())),
                                true);
                        out.println(str);
                       // Toast.makeText(getApplicationContext(),SERVER_IP,Toast.LENGTH_SHORT).show();
                        socket.close();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    lamp1.setBackgroundDrawable(lamp1_on_image);
                } else {
                    try {
                        t = new Thread(new ClientThread());
                        t.start();
                        String str = HomeId+"01010";
//                        Thread readThread=new Thread(new ReadFromServerThread(socket));
//                        readThread.start();
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())),
                                true);
                        out.println(str);
                       // Toast.makeText(getApplicationContext(),SERVER_IP,Toast.LENGTH_SHORT).show();
                        socket.close();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    lamp1.setBackground(lamp1_off_image);
                }
            }
        });

    }

    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                socket = new Socket(serverAddr, SERVERPORT);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }
    class ReadFromServerThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public ReadFromServerThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    String read = input.readLine();

                   // updateConversationHandler.post(new updateUIThread(read));
                    Toast to = Toast.makeText(getApplicationContext(),
                            read,
                            Toast.LENGTH_SHORT);
                    to.show();

                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}