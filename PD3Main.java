package pd3;

import pd3.myhttp.MyClient;
import pd3.myhttp.MyHttpClientBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class PD3Main {
    public static void main(String[] args){
        URL url = null;
        try {
            url = new URL("http://192.168.57.101/index.html");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        MyClient client = MyHttpClientBuilder.create(url,MyHttpClientBuilder.HTTP_2);
        try{
            long start = System.nanoTime();

            client.open();
            String[] results = client.get();
            client.close();

            long end = System.nanoTime();
            System.out.println("Time:" + (end - start) / 1000000f + "ms");

            for(String result : results){
                System.out.println(result);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
