package ca.pedab.datacraver.test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import ca.pedab.datacraver.Craving;
import ca.pedab.datacraver.Data;
import ca.pedab.datacraver.DataCraver;
import ca.pedab.datacraver.R;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


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

        DataCraver craver_custom = new DataCraver();
        craver_custom.crave(Craving.getInstance(this, "test_bitmap", 22234)
                .setMethod(Craving.CRAVEMETHOD.GET)
                .setUrl("http://purehdgallery.com/wp-content/uploads/2013/04/HD-Wallpaper-Renault-Alpine-Concept-Car.jpg")
                .setDebug(true), this, new DataCraver.CravingProcessor() {

            @Override
            public Data processCraving(Craving craving) {

                Data data = Data.getInstance(craving.getId(), craving.getIntegerId());
                try {

                    HttpGet rqst = new HttpGet(new URI(craving.getUrl()));
                    HttpClient client = new DefaultHttpClient();
                    HttpResponse rspns = client.execute(rqst);

                    int status = rspns.getStatusLine().getStatusCode();

                    if (status == 200) {

                        data.setCode(Data.HTTPCODE.HTTP_200);
                        HttpEntity entity = rspns.getEntity();
                        Bitmap bitmap = BitmapFactory.decodeStream(entity.getContent());
                        data.setResponse(bitmap);

                    } else {

                        data.setCode(Data.HTTPCODE.HTTP_400);
                    }

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    data.setCode(Data.HTTPCODE.APP_900);
                    data.getHttpCode().message = "URISyntaxException caught";
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                    data.setCode(Data.HTTPCODE.APP_900);
                    data.getHttpCode().message = "ClientProtocalException caught";
                } catch (IOException e) {
                    e.printStackTrace();
                    data.setCode(Data.HTTPCODE.APP_900);
                    data.getHttpCode().message = "IOException caught";
                }

                return data;
            }
        });
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

        if (int_id == 22234) {

            if (data.getResponse() != null && data.getResponse() instanceof Bitmap) {

                ImageView img_test = (ImageView) findViewById(R.id.img_test);
                img_test.setImageBitmap((Bitmap) data.getResponse());
            }
        }
    }

    @Override
    public void craveUnavailable(String string_id, int int_id, Data data) {

        Log.d("Craving", "Message: " + data.getHttpCode().message);
        Log.d("Craving", "Response: " + data.getResponse());
    }
}
