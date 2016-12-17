package pd3.myhttp;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

public class MyHttpClient extends MyClient {

    @Override
    public String[] get(URL url, int version, String[] files) throws IOException {

        Socket socket = createSocket(url, version);
        // KeepAlive機能をONにする
        socket.setKeepAlive(true);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));


        ArrayList<String> results = new ArrayList<String>();

        // HTTPリクエスト送信
        for(String file : files) {
            bufferedWriter.write("GET " + "http://" + url.getHost() + "/" + file + " HTTP/1.1\r\n");
            bufferedWriter.write("Host: " + url.getHost() + "\r\n");
            if(files[files.length-1].equals(file)){
                bufferedWriter.write("Connection: close\r\n");
            }
            bufferedWriter.write("\r\n");
            bufferedWriter.flush();
        }

        String line;
        String result = "";
        // HTTPレスポンス受信
        while ((line = bufferedReader.readLine()) != null) {
            result += line + System.lineSeparator();
//            System.out.println(line);
        }
        results.add(result);


        bufferedWriter.close();
        bufferedReader.close();
        socket.close();

        return results.toArray(new String[0]);
    }


}
