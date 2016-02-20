package im.point.torgash.daspoint.point;

/**
 * Created by Boss on 19.02.2016.
 */
public class Authorization {
    static String mToken, mCsrf_token, mUsername;

    public static void setUserName (String username) {
        mUsername = username;
    }
    public static void setToken(String token) {
        mToken = token;
    }

    public static void setCSRFToken(String csrfToken) {
        mCsrf_token = csrfToken;
    }
    public static String getToken(){
        return mToken;
    }

    public static String getCSRFToken() {
        return mCsrf_token;
    }
}
