package ca.pedab.datacraver.test;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import ca.pedab.datacraver.Craving;
import ca.pedab.datacraver.Data;
import ca.pedab.datacraver.DataCraver;
import ca.pedab.datacraver.R;


public class TestActivity extends Activity implements DataCraver.CraveListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        start();
    }

    private void start() {

        DataCraver craver = new DataCraver(true, "DataCraver");
        craver.crave(Craving.getInstance("some_id")
                .setMethod(Craving.CRAVEMETHOD.GET)
                .setUrl("http://eventplanner-dev.squarepin.ca/api/users"), this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_test, menu);
        return true;
    }

    @Override
    public void craveAvailable(String string_id, int int_id, Data data) {

        Log.d("DataCraver", "Message: " + data.getHttpCode().message);
        Log.d("DataCraver", "Response: " + data.getResponse());
    }

    @Override
    public void craveUnavailable(String string_id, int int_id, Data data) {

        Log.d("DataCraver", "Message: " + data.getHttpCode().message);
        Log.d("DataCraver", "Response: " + data.getResponse());
    }
}
