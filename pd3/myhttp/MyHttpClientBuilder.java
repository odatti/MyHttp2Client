package pd3.myhttp;

import pd3.myhttp.http2.MyHttp2Client;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;


public class MyHttpClientBuilder{

    public static final int HTTP_1_1 = 0;
    public static final int HTTP_2 = 1;


    public static MyClient create(URL url, int version){
        /*
        System.out.println("host : "+url.getHost());
        System.out.println("port : "+url.getDefaultPort());
        System.out.println("path : "+url.getPath());
        System.out.println("prot : "+url.getProtocol());
        System.out.println("auth : "+url.getAuthority());
        System.out.println("vrsn : "+version);
*/
        if(version == HTTP_2){
            return new MyHttp2Client(url);
        }
        return new MyHttpClient(url);
    }


    public static Socket openSocket(URL url, int version) throws IOException {
        // httpならsocketをそのまま作成して返す
        if(url.getDefaultPort() == 80){
            return new Socket(url.getHost(), url.getDefaultPort());
        }

        // httpsなら証明書の検証をスキップするsslsocketを返す
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
        // HTTP/2を使用する場合はALPNでプロトコルバージョンを伝達しなければならない
        if(version == HTTP_2){
            p.setApplicationProtocols(new String[]{"h2"});
        }
        sslSocket.setSSLParameters(p);

        // Handshakeを行う前にalpn領域(ClientHello)に{0x02, 0x68, 0x32 }を送る設定する必要がある？
        sslSocket.startHandshake();

        return sslSocket;
    }

}
