package pd3;

import pd3.myhttp.MyClient;
import pd3.myhttp.MyHttpClient;
import pd3.myhttp.http2.MyHttp2Client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class PD3Main {
    public static void main(String[] args){
        URL urlHttp = null;
        URL urlHttps = null;
        try {
            urlHttp = new URL("http://192.168.57.101/index.html");
            urlHttps = new URL("https://192.168.57.101/index.html");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        // test
        trial(urlHttp,MyClient.HTTP_2);
        //        informURL(urlHttp);
        //        informURL(urlHttps);



        /*
        String[] trials = {"平文　（HTTP/1.1）: ","暗号化（HTTP/1.1）: ","平文　（HTTP/2）  : ","暗号化（HTTP/2）  : "};
        int tryNum = 20;
        for(int j = 0;j < 4;j++){
            long sum = 0;
            for(int i = 0;i < tryNum;i++){
                long start = System.nanoTime();

                switch (j){
                    case 0:
                        trial(urlHttp,MyClient.HTTP_1_1);
                        break;
                    case 1:
                        trial(urlHttps,MyClient.HTTP_1_1);
                        break;
                    case 2:
                        trial(urlHttp,MyClient.HTTP_2);
                        break;
                    case 3:
                        trial(urlHttps,MyClient.HTTP_2);
                        break;
                    default:
                        break;
                }

                long end = System.nanoTime();
                sum += (end - start) / 1000000f;
//                System.out.println("Time : " + (end - start) / 1000000f  + "ms");

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            System.out.println(trials[j] + sum / tryNum  + "ms");

        }
        */


    }

    public static void trial(URL url, int version){
        try{
            MyClient client = null;

            // ソケット生成
            if(version == MyClient.HTTP_2){
                client =  new MyHttp2Client();
            }else{
                client = new MyHttpClient();
            }

            client.get(url, version, new String[]{"index.html","myscript.js","myscript2.js","mystyle.css"});

            /*
            for(String result : results){
                System.out.println(result);
            }
            */
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static void informURL(URL url){
        System.out.println("host : "+url.getHost());
        System.out.println("port : "+url.getDefaultPort());
        System.out.println("path : "+url.getPath());
        System.out.println("prot : "+url.getProtocol());
        System.out.println("auth : "+url.getAuthority());
    }

}
