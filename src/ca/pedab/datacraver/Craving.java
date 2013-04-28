package ca.pedab.datacraver;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

/**
 * This class is for wrapping your HTTP request. It does not have a constructor.
 * Rather the static {@link #getInstance(String)} must be used. It will allow method chaining. <br><br>
 * Also this class provides two identifier that you can use. 1. integer id, 2. string id.<br>
 * getIntegerId() gives you the integer id and getId() gives you the string id. And
 * of-course you can use the corresponding setters to set them.<br><br>
 * User: pirdod
 * Date: 4/17/13
 * Time: 4:07 PM
 */
public class Craving extends BaseEntity {

    protected int connect_timeout;
    protected int read_timeout;
    protected int int_id;

    protected String url;
    protected String method;

    protected ArrayList<BasicNameValuePair> get_parameters;
    protected ArrayList<BasicNameValuePair> post_parameters;
    protected ArrayList<BasicNameValuePair> headers;


    protected Craving(String id, int int_id) {

        this.id = id;
        this.int_id = int_id;
        // DEFAULT SETTINGS
        this.method = CRAVEMETHOD.GET.string;
        this.connect_timeout = CRAVETIMEOUT.DEFAULT.millisecond;
        this.read_timeout = CRAVETIMEOUT.DEFAULT.millisecond;
    }


    /**
     * You may get a new Instance of Craving through this method. This will allow you to
     * chain the rest of the setter methods right after this.<b><b>
     * ex. Craving.getInstance().setMethod(CRAVEMETHOD.POST)....
     * @return A new Instance of Craving
     */
    public static Craving getInstance(String string_id, int int_id) {

        return new Craving(string_id, int_id);
    }

    /**
     * You may get a new Instance of Craving through this method. This will allow you to
     * chain the rest of the setter methods right after this.<b><b>
     * ex. Craving.getInstance().setMethod(CRAVEMETHOD.POST)....
     * @return A new Instance of Craving
     */
    public static Craving getInstance(String string_id) {

        int random_id = generateRandomInteger();
        return new Craving(string_id, random_id);
    }

    private static int generateRandomInteger() {

        //TODO: COMMPLETE THIS
        return 0;
    }

    public int getIntegerId() {

        return int_id;
    }

    public int getConnectTimeout() {

        return connect_timeout;
    }

    public int getReadTimeout() {

        return read_timeout;
    }

    public String getRequestMethod() {

        return method;
    }

    public ArrayList<BasicNameValuePair> getPostParameters() {

        return post_parameters;
    }

    public ArrayList<BasicNameValuePair> getHeaders() {

        return headers;
    }

    public String getUrl() throws IllegalStateException {

        if (url == null) throw new IllegalStateException("url cannot be null");
        if (url.endsWith("/")) throw new IllegalStateException("url cannot end with '/'");

        // www.some_server.com/some_endpoint?some_key=some_val&some_key=some_val
        String url = this.url;
        for (int i = 0; get_parameters != null && i < get_parameters.size(); i++) {

            if (i == 0) url = url + "?";
            else url = url + "&";

            String key = get_parameters.get(i).getName();
            String val = get_parameters.get(i).getValue();

            url = url + key + "=" + val;
        }

        return url;
    }

    public Craving setUrl(String url) {

        this.url = url;
        return this;
    }

    public Craving setMethod(CRAVEMETHOD method) {

        this.method = method.string;
        return this;
    }

    public Craving setTimeout(CRAVETIMEOUT connect_timeout, CRAVETIMEOUT read_timeout) {

        this.connect_timeout = connect_timeout.millisecond;
        this.read_timeout = read_timeout.millisecond;
        return this;
    }

    public Craving setTimeout(int connect_timeout, int read_timeout) {

        this.connect_timeout = connect_timeout;
        this.read_timeout = read_timeout;
        return this;
    }

    public Craving setGetParameters(ArrayList<BasicNameValuePair> get_params) {

        this.get_parameters = get_params;
        return this;
    }

    public Craving addGetParameter(String name, String value) {

        if (get_parameters == null) {
            get_parameters = new ArrayList<BasicNameValuePair>();
        }

        get_parameters.add(new BasicNameValuePair(name, value));

        return this;
    }

    public Craving setPostParameters(ArrayList<BasicNameValuePair> post_params) {

        this.post_parameters = post_params;
        return this;
    }

    public Craving addPostParameter(String name, String value) {

        if (post_parameters == null) {
            post_parameters = new ArrayList<BasicNameValuePair>();
        }

        post_parameters.add(new BasicNameValuePair(name, value));
        return this;
    }

    public Craving setHeaders(ArrayList<BasicNameValuePair> headers) {

        this.headers = headers;
        return this;
    }

    public Craving addHeader(String name, String value) {

        if (headers == null) {
            headers = new ArrayList<BasicNameValuePair>();
        }

        headers.add(new BasicNameValuePair(name, value));
        return this;
    }

    @Override
    public String toString() {
        return "Craving{" +
                "method='" + method + '\'' +
                ", connect_timeout=" + connect_timeout +
                ", read_timeout=" + read_timeout +
                '}';
    }


    // ===========================>> =========================>> =============================>>
    /**
     * This will provide the supported HTTP request methods for the {@link Craving} class. <br><br>
     * ex. CRAVEMETHOD.POST;
     */
    public enum CRAVEMETHOD {

        GET ("GET"),
        POST ("POST"),
        PUT ("PUT"),
        DELETE ("DELETE");

        String string;
        CRAVEMETHOD(String method_str) {

            this.string = method_str;
        }
    }

    /**
     * This will provide the mostly used timeout value for the {@link Craving} class. <br><br>
     * ex. CRAVETIMEOUT.FIFTEENSEC;
     */
    public enum CRAVETIMEOUT {

        DEFAULT (0),
        FIVESEC (5 * 1000),
        TENSEC (10 * 1000),
        FIFTEENSEC (15 * 1000),
        TWENTYSEC (20 * 1000),
        TWENTYFIVESEC (25 * 1000),
        THIRTYSEC (30 * 1000),
        THIRTYFIVESEC (35 * 1000),
        FOURTYSEC (40 * 1000),
        FOURTYFIVESEC (45 * 1000),
        FIFTYSEC (50 * 1000),
        FIFTYFIVESEC (55 * 1000),
        ONEMIN (60 * 1000),
        TWOMIN (60 * 2 * 1000),
        THREEMIN (60 * 3 * 1000),
        FOURMIN (60 * 4 * 1000),
        FIVEMIN (60 * 5 * 1000);

        int millisecond;
        CRAVETIMEOUT(int value) {

            this.millisecond = value;
        }
    }
    // ===========================>> =========================>> =============================>>
}
