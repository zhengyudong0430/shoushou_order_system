package util;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class OrderSystemUtil {
    public static String readBody(HttpServletRequest request) throws UnsupportedEncodingException {
        int length = request.getContentLength();
        byte[] buffer = new byte[length];
        try (InputStream inputStream = request.getInputStream()) {
            inputStream.read(buffer, 0, length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(buffer, "UTF-8");
    }
}