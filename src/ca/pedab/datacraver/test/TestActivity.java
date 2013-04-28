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

        // GOOGLE FEED TEST: https://ajax.googleapis.com/ajax/services/feed/find?v=1.0&q=Official%20Google%20Blogs
        DataCraver craver = new DataCraver();
        craver.crave(Craving.getInstance(this, "evt_planner", 2432)
//                // GET
//                .setMethod(Craving.CRAVEMETHOD.GET)
//                .setUrl("http://eventplanner-dev.squarepin.ca/api/users")
//                // POST
//                .setMethod(Craving.CRAVEMETHOD.POST)
//                .setUrl("http://eventplanner-dev.squarepin.ca/api/users")
//                .addHeader("Content-Type", "application/json")
//                .setPostBody("{\"first_name\":\"Sarah\",\"last_name\":\"Millawi\"}")
                // DELETE
                .setMethod(Craving.CRAVEMETHOD.DELETE)
                .setUrl("http://eventplanner-dev.squarepin.ca/api/users")
                .addGetParameter("id", "9")
                .setDebug(true)
            , this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_test, menu);
        return true;
    }

    @Override
    public void craveAvailable(String string_id, int int_id, Data data) {

        Log.d("Craving", "Message: " + data.getHttpCode().message);
        Log.d("Craving", "Response: " + data.getResponse());
    }

    @Override
    public void craveUnavailable(String string_id, int int_id, Data data) {

        Log.d("Craving", "Message: " + data.getHttpCode().message);
        Log.d("Craving", "Response: " + data.getResponse());
    }
}
