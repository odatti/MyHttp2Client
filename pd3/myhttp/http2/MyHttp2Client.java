package pd3.myhttp.http2;

import pd3.myhttp.MyClient;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 * Created by shoichi on 2016/12/14.
 */
public class MyHttp2Client implements MyClient{

    private Socket socket = null;
    private OutputStream os = null;
    private InputStream is = null;

    private URL url;

    public MyHttp2Client(URL url) {
        this.url = url;
    }

    @Override
    public void open() throws IOException {
        socket = prepareSocket(url);
        os = socket.getOutputStream();
        is = socket.getInputStream();

        // PRISM送信
        byte[] data = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes();
        os.write(data);
        os.flush();

        // setting frame を送信
//        byte[] settingFrame = { 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00 };
        os.write(FrameBuilder.createHeaderFrame(FrameBuilder.TYPE_SETTINGS, FrameBuilder.FLAG_NONE));
        os.flush();

        // setting ackを送信
//        byte[] settingFrameACK = { 0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00 };
        os.write(FrameBuilder.createHeaderFrame(FrameBuilder.TYPE_SETTINGS, FrameBuilder.FLAG_ACK));
        os.flush();

/*      多分WINDOW_UPDATE関係で大きなサイズのファイルを送信できない
        // window_updateを送信
        ByteBuffer bb = ByteBuffer.allocate(13);
        bb.put(FrameBuilder.createHeaderFrame(FrameBuilder.TYPE_WINDOW_UPDATE, FrameBuilder.FLAG_NONE));
        bb.put(new byte[]{0x7f, (byte) 0xff, 0x00,0x00});
        byte[] windowUpdateFrame = bb.array();
        windowUpdateFrame[3] = 0x04;// ペイロードの長さを指定
        os.write(windowUpdateFrame);
        os.flush();
*/
    }

    @Override
    public String[] get(String[] files) throws IOException {
        byte[] header = FrameBuilder.createHeaderFrame(FrameBuilder.TYPE_HEADERS, (byte) (FrameBuilder.FLAG_END_HEADERS | FrameBuilder.FLAG_END_STREAM));

        byte[] headersFrame = FrameBuilder.createHeadersFrame(url, header);

        os.write(headersFrame);
        os.flush();

        String[] result = waitResponse(is);


        return result;
    }

    @Override
    public void close() throws IOException {
        os.close();
        is.close();
        socket.close();
    }

    private Socket prepareSocket(URL url) throws IOException {
        if(url.getDefaultPort() == 80){
            return new Socket(url.getHost(), url.getDefaultPort());
        }

        SSLSocket sslSocket = null;
        // 証明書の検証をしない
        TrustManager[] tm = { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] xc, String type) {
            }

            public void checkServerTrusted(X509Certificate[] xc, String type) {
            }
        } };

        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tm, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        SSLSocketFactory sf = ctx.getSocketFactory();

        sslSocket = (SSLSocket) sf.createSocket(url.getHost(), url.getDefaultPort());

        SSLParameters p = sslSocket.getSSLParameters();
        p.setProtocols(new String[]{"TLSv1","TLSv1.1","TLSv1.2"});
        p.setCipherSuites(new String[]{"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"});
        p.setApplicationProtocols(new String[]{"h2"});
        sslSocket.setSSLParameters(p);




        // Handshakeを行う前にalpn領域(ClientHello)に{0x02, 0x68, 0x32 }を送る設定する必要がある？
        sslSocket.startHandshake();

        return sslSocket;
    }

    private String[] waitResponse(InputStream is) throws IOException {
        ArrayList<String> results = new ArrayList<String>();
        int pushPromiseCount = 0;
        while(true){
            // ヘッダーを取得する
            byte[] header = new byte[FrameBuilder.HEADER_FRAME_LENGTH];
            is.readNBytes(header, 0, header.length);

            // ヘッダからペイロードを取得し、ペイロードがあればそれを読み込む
            int payload = getPayloadLength(header);
            byte[] payloadData = new byte[payload];
            if(payload != 0){
                is.readNBytes(payloadData,0,payloadData.length);
            }

            boolean breakLoop = false;
            switch (header[FrameBuilder.FRAME_TYPE]){
                case FrameBuilder.TYPE_DATA:// data
                    System.out.println("[LOG]TYPE_DATA");
                    // ペイロードにデータが入っている
                    results.add(new String(payloadData, "UTF-8"));
//                    System.out.println(result);
                    if((header[FrameBuilder.FRAME_FLAG] & FrameBuilder.FLAG_END_STREAM) == FrameBuilder.FLAG_END_STREAM){
                        if(pushPromiseCount==0){
//                            breakLoop = true;
                            // 接続終了時にはGOAWAYフレームを送信する
                            os.write(FrameBuilder.GOAWAY_FRAME);
                            os.flush();
                        }
                        pushPromiseCount--;
                    }
                    break;
                case FrameBuilder.TYPE_HEADERS:// headers
                    System.out.println("[LOG]TYPE_HEADERS");
                    // ペイロードには付加的な情報が書いてある
                    break;
                case FrameBuilder.TYPE_GOAWAY:// goaway
                    System.out.println("[LOG]TYPE_GOAWAY");
                    breakLoop = true;
                    break;
                case FrameBuilder.TYPE_PUSH_PROMISE:// push_promise
                    System.out.println("[LOG]TYPE_PUSH_PROMISE");
                    pushPromiseCount++;
                    break;
                case FrameBuilder.TYPE_SETTINGS:// settings
                    System.out.println("[LOG]TYPE_SETTINGS");
                    break;
                case FrameBuilder.TYPE_PRIORITY:// priorityy
                    System.out.println("[LOG]TYPE_PRIORITY");
                    break;
                case FrameBuilder.TYPE_RST_STREAM:// rst_stream
                    System.out.println("[LOG]TYPE_RST_STREAM");
                    break;
                case FrameBuilder.TYPE_PING:// ping
                    System.out.println("[LOG]TYPE_PING");
                    break;
                case FrameBuilder.TYPE_WINDOW_UPDATE:// windows_update
                    System.out.println("[LOG]TYPE_WINDOW_UPDATE");
                    break;
                case FrameBuilder.TYPE_CONTINUATION:// continuation
                    System.out.println("[LOG]Continuation");
                    break;
                default:
                    System.err.println("[ERR]UNKNOWN FRAME");
                    break;
            }

            if(breakLoop){
                break;
            }
        }

        return results.toArray(new String[0]);
    }

    public static int getPayloadLength(byte[] b){
        return (unsignedByte(b[0]) << 16) + (unsignedByte(b[1]) << 8) + unsignedByte(b[2]);
    }

    public static int unsignedByte(byte b){
        return b & 0xFF;
    }
}
