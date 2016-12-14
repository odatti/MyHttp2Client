package pd3.myhttp;

import pd3.myhttp.http2.MyHttp2Client;

import java.net.URL;


public class MyHttpClientBuilder{

    public static final int HTTP_1_1 = 0;
    public static final int HTTP_2 = 1;


    public static MyClient create(URL url, int version){
        System.out.println("host : "+url.getHost());
        System.out.println("port : "+url.getDefaultPort());
        System.out.println("path : "+url.getPath());
        System.out.println("prot : "+url.getProtocol());
        System.out.println("auth : "+url.getAuthority());

        if(version == HTTP_2){
            // client = new HttpClient();
            return new MyHttp2Client(url);
        }
        return null;
    }


}
