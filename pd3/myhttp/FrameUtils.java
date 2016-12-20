package pd3.myhttp;

import java.net.URL;
import java.nio.ByteBuffer;

public class FrameUtils {
    public static final int HEADER_FRAME_LENGTH = 9;
    public static final int FRAME_TYPE = 3;
    public static final int FRAME_FLAG = 4;


    public static final byte FLAG_NONE = 0x00;
    public static final byte FLAG_ACK = 0x01;
    public static final byte FLAG_END_STREAM = 0x01;
    public static final byte FLAG_END_HEADERS = 0x04;


    public static final byte TYPE_DATA = 0x00;
    public static final byte TYPE_HEADERS = 0x01;
    public static final byte TYPE_PRIORITY = 0x02;
    public static final byte TYPE_RST_STREAM = 0x03;
    public static final byte TYPE_SETTINGS = 0x04;
    public static final byte TYPE_PUSH_PROMISE = 0x05;
    public static final byte TYPE_PING = 0x06;
    public static final byte TYPE_GOAWAY = 0x07;
    public static final byte TYPE_WINDOW_UPDATE = 0x08;
    public static final byte TYPE_CONTINUATION = 0x09;

    /* GOAWAY ペイロードのフォーマット
     0                   1                   2                   3
     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |R|                  Last-Stream-ID (31)                        |
    +-+-------------------------------------------------------------+
    |                      Error Code (32)                          |
    +---------------------------------------------------------------+
    |                  Additional Debug Data (*)                    |
    +---------------------------------------------------------------+

     */
    public static final byte[] GOAWAY_FRAME = { 0x00, 0x00, 0x08, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,0x00,0x00,0x01,0x00,0x00,0x00,0x00 };


    public static byte[] createHeaderFrame(byte type, byte flag){
        byte[] header = {0x00, 0x00, 0x00, type, flag, 0x00, 0x00, 0x00, 0x00};
        return header;
    }

    public static byte[] createHeadersFrame(URL url, byte[] header){
        // Stream IDを1に設定する
        header[header.length-1] = 0x01;

        // 圧縮->0 , 7byte -> :method
        byte[] method = {0x00, 0x07, 0x3a, 0x6d, 0x65, 0x74, 0x68, 0x6f, 0x64};
        // 3byte -> :get
        byte[] methodData = {0x03, 0x47, 0x45, 0x54};

        // 圧縮->0 , 5byte -> :path
        byte[] path = {0x00, 0x05, 0x3a, 0x70, 0x61, 0x74, 0x68};
        byte[] pathData = url.getPath().getBytes();

        // 圧縮->0 , 7byte -> :scheme
        byte[] scheme = {0x00, 0x07, 0x3a, 0x73, 0x63, 0x68, 0x65, 0x6d, 0x65};
        byte[] schemeData = url.getProtocol().getBytes();

        // 圧縮->0 , 10byte -> :authority
        byte[] authority = {0x00, 0x0a, 0x3a, 0x61, 0x75, 0x74, 0x68, 0x6f, 0x72, 0x69, 0x74, 0x79};
        byte[] authorityData = url.getAuthority().getBytes();

        int headersLength = header.length + 3 +
                        method.length + methodData.length +
                        path.length + pathData.length +
                        scheme.length + schemeData.length +
                        authority.length + authorityData.length;

        header[2] = (byte)(headersLength - header.length);

        ByteBuffer bb = ByteBuffer.allocate(headersLength);
        bb.put(header);
        bb.put(method);
        bb.put(methodData);
        bb.put(path);
        bb.put((byte)pathData.length);
        bb.put(pathData);
        bb.put(scheme);
        bb.put((byte)schemeData.length);
        bb.put(schemeData);
        bb.put(authority);
        bb.put((byte)authorityData.length);
        bb.put(authorityData);

        return bb.array();
    }

    public static int getPayloadLength(byte[] payloadLength){
        return (unsignedByte(payloadLength[0]) << 16) + (unsignedByte(payloadLength[1]) << 8) + unsignedByte(payloadLength[2]);
    }

    public static int unsignedByte(byte b){
        return b & 0xFF;
    }
}
