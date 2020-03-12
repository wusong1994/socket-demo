package com.ws.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 输入、输出流操作工具类
 */
public class StreamUtil {

    public static void closeInputStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeOutputStream(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
