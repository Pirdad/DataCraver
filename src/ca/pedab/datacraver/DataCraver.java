package ca.pedab.datacraver;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import org.apache.http.message.BasicNameValuePair;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;


/**
 * DataCraver is a convenient and easy to use Http Request Executor. It can be used either by
 * Class instance or statically.<br><br>
 * <b>Class Instance:</b><br>
 * *Thread Safe*<br>
 * DataCraver craver = new DataCraver();<br>
 * craver.crave(*provide_craving_object, *provide_crave_listener_to_notify_you, *provide_custom_processor);<br><br>
 * *provide_custom_processor: [optional] means you could pass your own implementation of processing the request ({@link CravingProcessor}).<br><br>
 * <b>Statically:</b><br>
 * *Not Thread Safe*<br>
 * DataCraver.processCraving(*provide_craving_object, *provide_optional_result_container);<br><br>
 * User: pirdod
 * Date: 4/17/13
 * Time: 4:01 PM
 */
public class DataCraver {

    public void crave(Craving craving, CraveListener listener) {

        crave(craving, listener, null);
    }

    public void crave(Craving craving, CraveListener listener, CravingProcessor processor) {

        new RushForCraving(craving, listener, processor).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static Data processCraving(Craving craving, Data result_container) {

        long start_time = System.currentTimeMillis();
        if (result_container == null) result_container = Data.getInstance("none_supplied", -1);

        Quicky.logDebugMessage(craving.shouldDebug(), craving.getDebugTag(), //DEBUG
                "[" + craving.getIntegerId() + "] Time to process craving: " + craving.getId());
        Quicky.logRequest(craving.shouldDebug(), craving.getDebugTag(), craving); //DEBUG

        String url = craving.getUrl();
        boolean is_https = Quicky.isHttps(url);
        HttpURLConnection con = null;

        try {

            boolean network_available = Quicky.checkNetworkConnection(craving.context);
            if (network_available == false) throw new NoNetworkConnectionException();

            if (is_https) {
                con = (HttpsURLConnection) new URL(url).openConnection();
            } else {
                con = (HttpURLConnection) new URL(url).openConnection();
            }

            con.setConnectTimeout(craving.getConnectTimeout());
            con.setReadTimeout(craving.getReadTimeout());
            con.setRequestMethod(craving.getRequestMethod());

            Quicky.setDoOutPutTrue(con, craving.getRequestMethod());
            Quicky.setRequestHeaders(con, craving); //HAS DEBUG
            Quicky.writePostBody(con, craving); //HAS DEBUG

            start_time = System.currentTimeMillis(); //DEBUG
            int response_code = con.getResponseCode(); //GET RESPONSE CODE
            long milli_rspns = Quicky.calculateResponseTimeInMillis(start_time); //DEBUG
            String sec_rspns = Quicky.convertResponseTimeToSeconds(milli_rspns); //DEBUG
            Quicky.logDebugMessage(craving.shouldDebug(), craving.getDebugTag(), //DEBUG
                    "[" + craving.getIntegerId() + "] Response took " + sec_rspns + " seconds or " + milli_rspns + " milliseconds.");

            Data.HTTPCODE code = Quicky.getHTTPCODE(response_code);
            result_container.setCode(code);

            Quicky.logResponseHeaders(con, craving); //DEBUG

            InputStream in = con.getInputStream();
            String cnt_ncding = con.getContentEncoding();
            result_container.setContentEncoding(cnt_ncding);
            if (cnt_ncding != null && cnt_ncding.equals("gzip")) in = new GZIPInputStream(in);

            start_time = System.currentTimeMillis(); //DEBUG
            String result_str = Quicky.readInputStream(in); //PARSE INPUT STREAM
            long milli_prse = Quicky.calculateResponseTimeInMillis(start_time); //DEBUG
            String sec_prse = Quicky.convertResponseTimeToSeconds(milli_prse); //DEBUG
            Quicky.logDebugMessage(craving.shouldDebug(), craving.getDebugTag(), //DEBUG
                    "[" + craving.getIntegerId() + "] Parsing to String took " + sec_prse + " seconds or " + milli_prse + " milliseconds.");

            result_container.setResponse(result_str);

        } catch (NoNetworkConnectionException e) {

            String ui_message = "Seems like your Network Connection is down at the moment.";
            String debug_message = "[" + craving.getIntegerId() + "] " + ui_message + " Detail: NoNetworkConnectionException > " + e.getMessage();
            appCaughtException(result_container, ui_message, craving.shouldDebug(), craving.getDebugTag(), debug_message);

        } catch (MalformedURLException e) {

            String ui_message = "The application made a bad request to the server.";
            String debug_message = "[" + craving.getIntegerId() + "] " + ui_message + " Detail: MalformedURLException > " + e.getMessage();
            appCaughtException(result_container, ui_message, craving.shouldDebug(), craving.getDebugTag(), debug_message);

        } catch (SocketTimeoutException e) {

            long millis = Quicky.calculateResponseTimeInMillis(start_time);
            String ui_message = "The application timed out after " + Quicky.convertResponseTimeToSeconds(millis) + " seconds.";
            String debug_message = "[" + craving.getIntegerId() + "] " + ui_message + " In Milliseconds: " + millis;
            appCaughtException(result_container, ui_message, craving.shouldDebug(), craving.getDebugTag(), debug_message);

        } catch (ProtocolException e) {

            String ui_message = "The application failed to make the request.";
            String debug_message = "[" + craving.getIntegerId() + "] " + ui_message + " Detail: ProtocolException > " + e.getMessage();
            appCaughtException(result_container, ui_message, craving.shouldDebug(), craving.getDebugTag(), debug_message);

        } catch (FileNotFoundException e) {

            Quicky.getErrorStream(con, result_container);
            String ui_message = "The application failed to make the request.";
            String debug_message = "[" + craving.getIntegerId() + "] " + ui_message + " Detail: FileNotFoundException > " + e.getMessage();
            appCaughtException(result_container, ui_message, craving.shouldDebug(), craving.getDebugTag(), debug_message);

        } catch (IOException e) {

            String ui_message = "The application failed to make the request.";
            String debug_message = "[" + craving.getIntegerId() + "] " + ui_message + " Detail: IOException > " + e.getMessage();
            appCaughtException(result_container, ui_message, craving.shouldDebug(), craving.getDebugTag(), debug_message);

        } finally {

            if (con != null) con.disconnect();
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
            return DataCraver.processCraving(craving, result_container);
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

            Quicky.logDebugMessage(craving.shouldDebug(), craving.getDebugTag(), "[" + craving.getIntegerId() + "] " + "Your craving was processed.");

            if (response_code.code >= 400 && listener != null) {

                Quicky.logDebugMessage(craving.shouldDebug(), craving.getDebugTag(), "[" + craving.getIntegerId() + "] " + "Craving is unavailable.");
                listener.craveUnavailable(data.getId(), data.getIntegerId(), data);

            } else if (response_code.code >= 200 && listener != null) {

                Quicky.logDebugMessage(craving.shouldDebug(), craving.getDebugTag(), "[" + craving.getIntegerId() + "] " + "Craving is available.");
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

        public static boolean checkNetworkConnection(Context context) {

            if (context != null) {

                ConnectivityManager connection_manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo network_info = connection_manager.getActiveNetworkInfo();

                if (network_info == null || !network_info.isConnected()) {

                    return false;

                } else {

                    return true;
                }
            }

            return false;
        }

        public static boolean isHttps(String url) {

            if (url.toLowerCase().indexOf("https") != -1) return true;
            return false;
        }

        public static void setDoOutPutTrue(HttpURLConnection conn, String method) {

            if ((method).toLowerCase().equals("POST".toLowerCase())  ||
                    (method).toLowerCase().equals("PUT".toLowerCase()))  {

                conn.setDoOutput(true);
            }
        }

        public static void setRequestHeaders(HttpURLConnection con, Craving craving) {

            if (con != null && craving != null) {

                ArrayList<BasicNameValuePair> headers = craving.getHeaders();

                String debug_tag = craving.getDebugTag();
                boolean debug = craving.shouldDebug();
                int debug_id = craving.getIntegerId();

                if (headers != null) {

                    for (int i = 0; i < headers.size(); i++) {

                        String name = headers.get(i).getName();
                        String value = headers.get(i).getValue();
                        Quicky.logDebugMessage(debug, debug_tag, "[" + debug_id + "] REQUEST_HEADER = " + name + " : " + value);

                        con.addRequestProperty(name, value);
                    }
                }
            }
        }

        public static void writePostBody(HttpURLConnection con, Craving craving) throws IOException {

            try {

                if (con != null && craving != null) {

                    String method = craving.getRequestMethod();
                    String post_body = null;
                    ArrayList<BasicNameValuePair> post_parameters = craving.getPostParameters();

                    if (!TextUtils.isEmpty(craving.getPostBody())) {

                        post_body = craving.getPostBody();
                        Quicky.logDebugMessage(craving.shouldDebug(), craving.getDebugTag(),
                                "[" + craving.getIntegerId() + "] POST_BODY = " + post_body);

                    } else if (post_parameters != null) {

                        post_body = Quicky.getPostParametersInString(post_parameters, null,
                                craving.shouldDebug(), craving.getDebugTag(), craving.getIntegerId());
                    }

                    if (!TextUtils.isEmpty(post_body) &&
                            (method).toLowerCase().equals("POST".toLowerCase()) ||
                            (method).toLowerCase().equals("PUT".toLowerCase())) {

                        Quicky.writeToOutputStream(con, post_body);
                    }
                }

            } catch (UnsupportedEncodingException e) { throw e; }
            catch (IOException e) { throw e; }
        }

        public static String getPostParametersInString(ArrayList<BasicNameValuePair> post_parameters, String encoding,
                                                       boolean debug, String debug_tag, int debug_id) throws UnsupportedEncodingException {

            String post_params = "";
            if (post_parameters != null) {

                try {

                    for (int i = 0; i < post_parameters.size(); i++) {

                        if (encoding == null) encoding = "UTF-8";
                        if (i != 0) post_params = post_params + "&";

                        String key = URLEncoder.encode(post_parameters.get(i).getName(), encoding);
                        String val = URLEncoder.encode(post_parameters.get(i).getValue(), encoding);

                        Quicky.logDebugMessage(debug, debug_tag, "[" + debug_id + "] REQUEST_BODY = " + key + " : " + val);

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

        public static String convertResponseTimeToSeconds(long millis) {

            DecimalFormat df = new DecimalFormat("0.###");
            return df.format((double) millis / 1000);
        }

        public static void getErrorStream(HttpURLConnection con, Data result_container) {

            if (con != null && result_container != null) {

                try {

                    InputStream in = con.getErrorStream();
                    String error_response = Quicky.readInputStream(in);
                    result_container.setResponse(error_response);

                } catch (IOException e) { e.printStackTrace(); }
            }
        }

        public static void logRequest(boolean debug, String debug_tag, Craving craving) {

            if (craving != null) {

                //DEBUG
                Quicky.logDebugMessage(debug, debug_tag, "[" + craving.getIntegerId() + "] URL: " + craving.getUrl());
                Quicky.logDebugMessage(debug, debug_tag, "[" + craving.getIntegerId() + "] METHOD: " + craving.getRequestMethod());
                Quicky.logDebugMessage(debug, debug_tag, "[" + craving.getIntegerId() + "] CONN_TIMEOUT: " + craving.getConnectTimeout());
                Quicky.logDebugMessage(debug, debug_tag, "[" + craving.getIntegerId() + "] READ_TIMEOUT: " + craving.getReadTimeout());
            }
        }

        public static void logResponseHeaders(HttpURLConnection con, Craving craving) {

            if (con != null && craving != null) {

                String debug_tag = craving.getDebugTag();
                boolean debug = craving.shouldDebug();
                int debug_id = craving.getIntegerId();

                Map<String, List<String>> headers = con.getHeaderFields();
                Set<String> keys = headers.keySet();
                for (String key : keys) {

                    List<String> values = headers.get(key);

                    logDebugMessage(debug, debug_tag, "[" + debug_id + "] RESPONSE_HEADER = " + key + " : " + values.toString());
                }
            }
        }

        public static void logDebugMessage(boolean debug, String debug_tag, String debug_message) {

            if (debug) {

                Log.d(debug_tag, debug_message);
            }
        }
    }

    public static class NoNetworkConnectionException extends IllegalStateException {

        public NoNetworkConnectionException() {

            super("There's no Network connection.");
        }
    }
}
