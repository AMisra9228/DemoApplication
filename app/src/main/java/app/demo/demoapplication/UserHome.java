package app.demo.demoapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class UserHome extends AppCompatActivity {

    private TextView uname,umail;
    private Button btn_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_home);

        uname = (TextView) findViewById(R.id.uname);
        umail = (TextView) findViewById(R.id.umail);
        btn_logout = (Button) findViewById(R.id.btn_logout);

        Intent receiveIntent = getIntent();
        if(receiveIntent.hasExtra("name") && receiveIntent.hasExtra("email")) {
            String name = receiveIntent.getStringExtra("name");
            String mail = receiveIntent.getStringExtra("email");

            uname.setText(name);
            umail.setText(mail);
        }
    }
}
