package vandy.mooc.videoapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class NewVideoActivity extends Activity {
EditText name, duration, url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_video);

        name = (EditText)findViewById(R.id.editText_name);
        duration  = (EditText)findViewById(R.id.editText_duration);
        url = (EditText)findViewById(R.id.editText_url);



    }
    public void AddNewVideoButtonPressed (View v) {


            final Video video = new Video (name.getText().toString(),
                                     new Long (duration.getText().toString()),
                                     url.getText().toString());

                        VideoOpsFragment.instance.addVideo(video);







        finish();
    }
}
