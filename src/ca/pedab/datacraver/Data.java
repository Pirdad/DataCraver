package ca.pedab.datacraver;

/**
 * Data is the Class that contains or wraps the result/content for your Http Request.
 * It is used commonly with {@link DataCraver} and {@link Craving}.
 * It doesn't have Constructor, instead use Data.getInstance() to get you an instance of it.<br><br>
 * Also this class provides two identifier that you can use. 1. integer id, 2. string id.<br>
 * getIntegerId() gives you the integer id and getId() gives you the string id.<br><br>
 * User: pirdod
 * Date: 4/17/13
 * Time: 5:29 PM
 */
public class Data extends BaseEntity {

    private int int_id;
    private HTTPCODE code;
    private String response;
    private String content_encoding;

    private Data(String id, int int_id) {

        this.id = id;
        this.int_id = int_id;
    }

    public static Data getInstance(String id, int int_id) {

        return new Data(id, int_id);
    }

    public int getIntegerId() {

        return int_id;
    }

    /**
     * This method will provide the Http Response Code. The response code will have the code,
     * the meaning of the code, and a user-friendly message.
     * @return {@link HTTPCODE} enum
     */
    public HTTPCODE getHttpCode() {

        return code;
    }

    public void setCode(HTTPCODE code) {

        this.code = code;
    }

    public String getResponse() {

        return response;
    }

    public void setResponse(String response) {

        this.response = response;
    }

    public String getContentEncoding() {

        return content_encoding;
    }

    public void setContentEncoding(String content_encoding) {

        this.content_encoding = content_encoding;
    }

    // ===========================>> =========================>> =============================>>

    /**
     * HTTPCODE contains all the commonly returned responses by the server. Each Code has the code,
     * the meaning, and the message.
     */
    public enum HTTPCODE {

        // SUCCESS CODES
        /** Ok **/
        HTTP_200 (200, "Ok", "Your request has been processed successfully."),
        /** Created **/
        HTTP_201 (201, "Created", "Your request successfully resulted in a new Resource."),
        /** Accepted **/
        HTTP_202 (202, "Accepted", "Your request was Accepted however processing it has not been completed."),
        /** No Content **/
        HTTP_204 (204, "No Content", "The request was successfully processed however there was no content to return."),
        // FAIL CODES
        /** Unauthorized **/
        HTTP_401 (401, "Unauthorized", "You've made an Unauthorized Request."),
        /** Bad Request **/
        HTTP_400 (400, "Bad Request", "You've made a Bad Request."),
        /** Forbidden **/
        HTTP_403 (403, "Forbidden", "You've made a Forbidden Request."),
        /** Not Found **/
        HTTP_404 (404, "Not Found", "The request resource was Not Found."),
        /** Internal Server Error **/
        HTTP_500 (500, "Internal Server Error", "There was an Internal Server Error."),
        // APP CODES
        /** Application Failed **/
        APP_900 (900, "Application Failed", "There was an issue with the Application."),
        /** Unsupported Response Code **/
        APP_901 (901, "Unsupported Response Code", "There was an issue with the Application.");

        HTTPCODE(int code, String meaning, String message) {

            this.code = code;
            this.meaning = meaning;
            this.message = message;
        }

        public int code;
        public String meaning;
        public String message;
    }
}
