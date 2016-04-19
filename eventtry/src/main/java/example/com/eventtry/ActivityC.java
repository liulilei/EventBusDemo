package example.com.eventtry;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.greenrobot.event.EventBus;

public class ActivityC extends AppCompatActivity {

    private TextView textView;

    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c);
        textView = (TextView) findViewById(R.id.activity_c);
        btn = (Button) findViewById(R.id.button_c);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new Event.EventC("CCCCC"));
                finish();
            }
        });
    }

}
