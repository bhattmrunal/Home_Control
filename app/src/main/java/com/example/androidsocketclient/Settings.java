package com.example.androidsocketclient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by mrunal on 8/8/15.
 */
public class Settings extends Activity {

Button updateSettings;
    EditText ipAddr;
    SharedPreferences shrPreferences;
    String str_ipaddr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_page);
        shrPreferences=getApplicationContext().getSharedPreferences("UISettings", Context.MODE_PRIVATE);


        ipAddr= (EditText) findViewById(R.id.editext_ip);
        updateSettings=(Button)findViewById(R.id.btn_settings_update);


                str_ipaddr=shrPreferences.getString("IP_Address","");
        if(str_ipaddr!=null)
          ipAddr.setText(str_ipaddr);


        updateSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(str_ipaddr!=null) {
                    String ip1=ipAddr.getText().toString();
                    SharedPreferences.Editor editor=shrPreferences.edit();
                    editor.putString("IP_Address",ip1);
                    editor.apply();
                    Toast.makeText(getApplicationContext(),"Settings Updated",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
