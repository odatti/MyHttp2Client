package pd3.myhttp;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

public class MyHttpClient implements MyClient{

    private Socket socket = null;
    private BufferedWriter bufferedWriter = null;
    private BufferedReader bufferedReader = null;

    private URL url;

    public MyHttpClient(URL url) {
        this.url = url;
    }
    @Override
    public void open() throws IOException {
        socket = MyHttpClientBuilder.openSocket(url, MyHttpClientBuilder.HTTP_1_1);
        // KeepAlive機能をONにする
        socket.setKeepAlive(true);
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    @Override
    public void close() throws IOException {
        bufferedWriter.close();
        bufferedReader.close();
        socket.close();
    }

    @Override
    public String[] get(String[] files) throws IOException {
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

        return results.toArray(new String[0]);
    }


}
