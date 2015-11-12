package edu.temple.filewatcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText editURL;
    private Spinner spinner;
    private Button goButton;
    private FileWatcherService fileWatcherService;
    private boolean mBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editURL = (EditText) findViewById(R.id.webText);
        spinner = (Spinner) findViewById(R.id.refreshTime);
        goButton = (Button) findViewById(R.id.start);

        String[] values = getResources().getStringArray(R.array.spinner_array);
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,values);
        spinner.setAdapter(spinAdapter);

        Intent intent = new Intent(MainActivity.this,FileWatcherService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = -1;
                String url = "";

                switch (spinner.getSelectedItemPosition()){
                    case 1:
                        value = 60;
                        break;
                    case 2:
                        value = 300;
                        break;
                    case 3:
                        value = 600;
                        break;
                    case 4:
                        value = 900;
                        break;
                    default:
                        Toast.makeText(getBaseContext(),R.string.spinner_retry,Toast.LENGTH_SHORT).show();
                        break;
                }

                if(value != -1){

                    url = editURL.getText().toString();
                    if(!url.startsWith(getString(R.string.http))){
                        url = getString(R.string.http) + url;
                    }

                    fileWatcherService.addFile(url,value);

                    spinner.setSelection(0);
                    editURL.setText(R.string.url);

                    Toast.makeText(getBaseContext(),R.string.success,Toast.LENGTH_SHORT).show();
                }
            }
        });
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            FileWatcherService.LocalBinder binder = (FileWatcherService.LocalBinder) service;
            fileWatcherService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onDestroy(){
        if(mBound){
            unbindService(mConnection);
        }
        super.onDestroy();
    }

}
