package pd3;

import pd3.myhttp.MyClient;
import pd3.myhttp.MyHttpClientBuilder;

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


        trial(MyHttpClientBuilder.create(urlHttp, MyHttpClientBuilder.HTTP_2));


        /*
        String[] trials = {"平文　（HTTP/1.1）: ","暗号化（HTTP/1.1）: ","平文　（HTTP/2）  : ","暗号化（HTTP/2）  : "};
        int tryNum = 20;
        for(int j = 0;j < 4;j++){
            long sum = 0;
            for(int i = 0;i < tryNum;i++){
                long start = System.nanoTime();

                switch (j){
                    case 0:
                        trial(MyHttpClientBuilder.create(urlHttp,MyHttpClientBuilder.HTTP_1_1));
                        break;
                    case 1:
                        trial(MyHttpClientBuilder.create(urlHttps,MyHttpClientBuilder.HTTP_1_1));
                        break;
                    case 2:
                        trial(MyHttpClientBuilder.create(urlHttp,MyHttpClientBuilder.HTTP_2));
                        break;
                    case 3:
                        trial(MyHttpClientBuilder.create(urlHttps,MyHttpClientBuilder.HTTP_2));
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

    public static void trial(MyClient client){
        try{
            client.open();
            //String[] results = client.get(new String[]{"index.html","myscript.js","myscript2.js","mystyle.css"});
            client.get(new String[]{"index.html","myscript.js","myscript2.js","mystyle.css"});
            client.close();

            /*
            for(String result : results){
                System.out.println(result);
            }
            */
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
