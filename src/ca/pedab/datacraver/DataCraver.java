package ca.pedab.datacraver;

import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.message.BasicNameValuePair;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;


/**
 * DataCraver is a convenient and easy to use Http Request Executor. It can be used either by
 * Class instance or statically.<br><br>
 * <b>Class Instance:</b><br>
 * *Thread Safe*<br>
 * DataCraver craver = new DataCraver();<br>
 * craver.crave(*provide_craving_object, *provide_crave_listener_to_notify_you, *provide_custom_processor);<br><br>
 * *provide_custom_processor: means you could pass your own implementation of processing the request ({@link CravingProcessor}).<br><br>
 * <b>Statically:</b><br>
 * *Not Thread Safe*<br>
 * DataCraver.prepareCraving(*provide_craving_object, *provide_optional_result_container);<br>
 * DataCraver.processRequest(*provide_url_connection_object, *provide_optional_result_container);<br><br>
 * User: pirdod
 * Date: 4/17/13
 * Time: 4:01 PM
 */
public class DataCraver {

    private String debug_tag = "DataCraver";
    private boolean debug = true;

    public DataCraver() { }

    /**
     * Create a new DataCraver and enable or disable Debugging.
     * @param debug true or false.
     * @param debug_tag the tag to use for the log debug messages.
     */
    public DataCraver(boolean debug, String debug_tag) {

        this.debug = debug;
        if (debug_tag != null) this.debug_tag = debug_tag;
    }

    public void crave(Craving craving, CraveListener listener) {

        crave(craving, listener, null);
    }

    public void crave(Craving craving, CraveListener listener, CravingProcessor processor) {

        new RushForCraving(craving, listener, processor).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static Data processCraving(Craving craving, Data result_container, boolean debug, String debug_tag) {

        long start_time = System.currentTimeMillis();
        if (result_container == null) result_container = Data.getInstance("none_supplied", -1);

        String url = craving.getUrl();
        boolean is_https = Quicky.isHttps(url);
        HttpURLConnection con = null;

        try {

            if (is_https) {
                con = (HttpsURLConnection) new URL(url).openConnection();
            } else {
                con = (HttpURLConnection) new URL(url).openConnection();
            }

            con.setConnectTimeout(craving.getConnectTimeout());
            con.setReadTimeout(craving.getReadTimeout());
            con.setRequestMethod(craving.getRequestMethod());

            Quicky.setDoOutPutTrue(con, craving.getRequestMethod());
            Quicky.setRequestHeaders(con, craving.getHeaders());
            Quicky.writePostParameters(con, craving.getRequestMethod(), craving.getPostParameters());

            Data.HTTPCODE code = Quicky.getHTTPCODE(con.getResponseCode());
            result_container.setCode(code);

            InputStream in = con.getInputStream();
            String cnt_ncding = con.getContentEncoding();
            result_container.setContentEncoding(cnt_ncding);
            if (cnt_ncding != null && cnt_ncding.equals("gzip")) in = new GZIPInputStream(in);

            String result_str = Quicky.readInputStream(in);
            result_container.setResponse(result_str);

        } catch (MalformedURLException e) {

            String ui_message = "The application made a bad request to the server.";
            String debug_message = ui_message + " Detail: MalformedURLException > " + e.getMessage();
            appCaughtException(result_container, ui_message, debug, debug_tag, debug_message);

        } catch (SocketTimeoutException e) {

            long millis = Quicky.calculateResponseTimeInMillis(start_time);
            String ui_message = "The application timed out after " + Quicky.convertResponseTimeToSeconds(millis) + " seconds.";
            String debug_message = ui_message + " In Milliseconds: " + millis;
            appCaughtException(result_container, ui_message, debug, debug_tag, debug_message);

        } catch (ProtocolException e) {

            String ui_message = "The application failed to make the request.";
            String debug_message = ui_message + " Detail: ProtocolException > " + e.getMessage();
            appCaughtException(result_container, ui_message, debug, debug_tag, debug_message);

        } catch (IOException e) {

            String ui_message = "The application failed to make the request.";
            String debug_message = ui_message + " Detail: IOException > " + e.getMessage();
            appCaughtException(result_container, ui_message, debug, debug_tag, debug_message);
        }

        return result_container;
    }

    private static void appCaughtException(Data result_container, String message, boolean debug, String debug_tag, String debug_message) {

        result_container.setCode(Data.HTTPCODE.APP_900);
        result_container.getHttpCode().message = message;

        Quicky.logDebugMessage(debug, debug_tag, debug_message);
    }

    // ===========================>> =========================>> =============================>>

    /**
     * RushForCraving is the AsyncTask that is responsible for creating a new Thread which will
     * execute the Http Craving Request (Craving) object. It's also responsible for notifying the
     * requester with the results.
     */
    private class RushForCraving extends AsyncTask<Void, Void, Data> {

        private CraveListener listener;
        private CravingProcessor processor;
        private Craving craving;

        public RushForCraving(Craving craving, CraveListener listener, CravingProcessor processor) {

            this.craving = craving;
            this.listener = listener;
            this.processor = processor;
        }

        @Override
        protected Data doInBackground(Void... params) {

            Data result = null;

            Quicky.logDebugMessage(debug, debug_tag, "Time to process your craving");
            if (processor != null) result = processor.processCraving(craving);
            else result = processCraving(craving);

            return result;
        }

        /**
         * This is the default processor.
         * @param craving Craving object which is the request itself.
         * @return Data object which contains the result.
         */
        private Data processCraving(Craving craving) {

            Data result_container = Data.getInstance(craving.getId(), craving.getIntegerId());
            return DataCraver.processCraving(craving, result_container, debug, debug_tag);
        }

        @Override
        protected void onPostExecute(Data data) {

            cravingProcessed(data);
        }

        /**
         * This is responsible for notifying the requester once the craving is processed.
         * @param data Data object with the results.
         */
        private void cravingProcessed(Data data) {

            Data.HTTPCODE response_code = data.getHttpCode();
            Quicky.logDebugMessage(debug, debug_tag, "Your craving was processed");
            Quicky.logDebugMessage(debug, debug_tag, data.toString());

            if (response_code.code >= 400 && listener != null) {

                Quicky.logDebugMessage(debug, debug_tag, "Craving is unavailable");
                listener.craveUnavailable(data.getId(), data.getIntegerId(), data);

            } else if (response_code.code >= 200 && listener != null) {

                Quicky.logDebugMessage(debug, debug_tag, "Craving is available");
                listener.craveAvailable(data.getId(), data.getIntegerId(), data);
            }
        }
    }

    // ===========================>> =========================>> =============================>>

    /**
     * Implement this interface to get notified when your Http {@link Craving} Requests gets processed by the
     * server. craveAvailable() gets called when the request executed successfully with the 200+
     * responses. craveUnavailable() will get called when the server responds with 400+ responses.<br><br>
     * Note: that some servers responds back with content even if the response is 400+. In that case,
     * Data parameter for craveUnavailable() will have the response content.
     */
    public interface CraveListener {

        public void craveAvailable(String string_id, int int_id, Data data);
        public void craveUnavailable(String string_id, int int_id, Data data);
    }

    /**
     * Implement this interface to give {@link DataCraver} Custom {@link Craving} Process. It means your {@link Craving}
     * requests could be processed by your code but executed by {@link DataCraver}. {@link #processCraving(Craving)}
     * can contain your code.
     */
    public interface CravingProcessor {

        /**
         * Implementation of this method allows a custom processing to execute your {@link Craving}.
         * @param craving This Craving object provides all the Craving Request options.
         * @return Once completed, the result/content will be returned by Data object.
         */
        public Data processCraving(Craving craving);
    }

    // ===========================>> =========================>> =============================>>

    /**
     * Quicky provides quick and convenient helper methods for Networking.
     */
    public static class Quicky {

        public static boolean isHttps(String url) {

            if (url.toLowerCase().indexOf("https") != -1) return true;
            return false;
        }

        public static void setDoOutPutTrue(HttpURLConnection conn, String method) {

            //TODO: find out if setDoOutput needs to be true for DELETE option
            if ((method).toLowerCase().equals("POST".toLowerCase())  ||
                    (method).toLowerCase().equals("PUT".toLowerCase()))  { // ||
                    //(method).toLowerCase().equals("DELETE".toLowerCase())) {

                conn.setDoOutput(true);
            }
        }

        public static void setRequestHeaders(HttpURLConnection con, ArrayList<BasicNameValuePair> headers) {

            if (headers != null) {

                for (int i = 0; i < headers.size(); i++) {

                    con.addRequestProperty(headers.get(i).getName(), headers.get(i).getValue());
                }
            }
        }

        public static void writePostParameters(HttpURLConnection con, String method, ArrayList<BasicNameValuePair> post_parameters)
                throws IOException {

            //TODO: find out if writeToOutputStream needs to be done for DELETE option
            try {

                String post_params = Quicky.getPostParametersInString(post_parameters, null);
                if (post_parameters != null &&
                        (method).toLowerCase().equals("POST".toLowerCase()) ||
                        (method).toLowerCase().equals("PUT".toLowerCase())) { // ||
                        //(method).toLowerCase().equals("DELETE".toLowerCase())) {

                    Quicky.writeToOutputStream(con, post_params);
                }

            } catch (UnsupportedEncodingException e) { throw e; }
            catch (IOException e) { throw e; }
        }

        public static String getPostParametersInString(ArrayList<BasicNameValuePair> post_parameters, String encoding)
                throws UnsupportedEncodingException {

            String post_params = "";
            if (post_parameters != null) {

                try {

                    for (int i = 0; i < post_parameters.size(); i++) {

                        if (encoding == null) encoding = "UTF-8";
                        if (i != 0) post_params = post_params + "&";

                        String key = URLEncoder.encode(post_parameters.get(i).getName(), encoding);
                        String val = URLEncoder.encode(post_parameters.get(i).getValue(), encoding);

                        post_params = post_params + key + "=" + val;
                    }

                } catch (UnsupportedEncodingException e) { throw e; }
            }

            return post_params;
        }

        public static void writeToOutputStream(HttpURLConnection conn, String post_params)
                throws IOException {

            try {

                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                out.write(post_params);
                out.close();

            } catch (IOException e) { throw e; }
        }

        public static String readInputStream(InputStream in)
                throws IOException {

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder builder = new StringBuilder();

            try {

                for (String line = br.readLine(); line != null; line = br.readLine()) {

                    builder.append(line);
                    builder.append('\n');
                }

            } catch (IOException e) { throw e; }

            return builder.toString();
        }

        public static Data.HTTPCODE getHTTPCODE(int response_code) {

            for (Data.HTTPCODE http_code : Data.HTTPCODE.values()) {

                if (response_code == http_code.code) return http_code;
            }

            return Data.HTTPCODE.APP_901;
        }

        public static long calculateResponseTimeInMillis(long start_time) {

            long end_time = System.currentTimeMillis();
            return (end_time - start_time);
        }

        public static long convertResponseTimeToSeconds(long millis) {

            return (millis / 1000);
        }

        public static void logDebugMessage(boolean debug, String debug_tag, String debug_message) {

            if (debug) {

                Log.d(debug_tag, debug_message);
            }
        }
    }
}
