package pd3.myhttp;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

public class MyHttpClient extends MyClient {

    public MyHttpClient(){
        super(HTTP_1_1);
    }

    @Override
    public String[] get(URL url, String[] files) throws IOException {
        Socket socket = createSocket(url);
        // KeepAlive機能をONにする
        socket.setKeepAlive(true);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // GETしたファイル(文字列)を格納する
        ArrayList<String> results = new ArrayList<String>();

        // HTTPリクエスト送信
        for(String file : files) {
            bw.write("GET " + "http://" + url.getHost() + "/" + file + " HTTP/1.1\r\n");
            bw.write("Host: " + url.getHost() + "\r\n");
            if(files[files.length-1].equals(file)){
                bw.write("Connection: close\r\n");
            }
            bw.write("\r\n");
            bw.flush();
        }

        String line;
        String result = "";
        // HTTPレスポンス受信
        while ((line = br.readLine()) != null) {
            result += line + System.lineSeparator();
//            System.out.println(line);
        }
        results.add(result);


        bw.close();
        br.close();
        socket.close();

        return results.toArray(new String[0]);
    }
}
