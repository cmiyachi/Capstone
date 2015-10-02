package vandy.mooc.videoapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

/**
 * Created by i on 31.08.15.
 */
public class LoginActivity extends Activity {

    EditText loginEditText, passwordEditText;
    final static String TAG = "Login Activity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        loginEditText = (EditText) findViewById(R.id.edit_text_login);
        passwordEditText = (EditText) findViewById(R.id.edit_text_password);
    }

    public void loginButtonPressed (View v) {



        String login = loginEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        VideoOpsFragment.instance.login(login, password);

        finish();

    }
}
