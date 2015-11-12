package edu.temple.filewatcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

/**
 * Created by Kevin on 11/11/15.
 */
public class UpdateActivity extends Activity {

    private ListView updates;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        updates = (ListView) findViewById(R.id.update_view);

    }

    @Override
    protected void onStart(){
        super.onStart();

        Intent args = getIntent();
        List<String> temp = args.getStringArrayListExtra(getString(R.string.update_tag));
        String [] rev = temp.toArray(new String[temp.size()]);
        String [] values = new String[rev.length];

        for(int i=0;i<rev.length;i++){
            values[i] = rev[rev.length-i-1];
        }

        if(values.length != 0) {

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    android.R.id.text1, values);

            updates.setAdapter(adapter);
        }

    }

}
