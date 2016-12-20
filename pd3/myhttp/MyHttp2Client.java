package pd3.myhttp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

public class MyHttp2Client extends MyClient {

    public MyHttp2Client(){
        super(HTTP_2);
    }
    @Override
    public String[] get(URL url, String[] files) throws IOException {
        Socket socket = createSocket(url);
        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();

        // PRISM送信
        byte[] data = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes();
        os.write(data);
        os.flush();

        // setting frame を送信
//        byte[] settingFrame = { 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00 };
        os.write(FrameUtils.createHeaderFrame(FrameUtils.TYPE_SETTINGS, FrameUtils.FLAG_NONE));
        os.flush();

        // setting ackを送信
//        byte[] settingFrameACK = { 0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00 };
        os.write(FrameUtils.createHeaderFrame(FrameUtils.TYPE_SETTINGS, FrameUtils.FLAG_ACK));
        os.flush();

        byte[] header = FrameUtils.createHeaderFrame(FrameUtils.TYPE_HEADERS, (byte) (FrameUtils.FLAG_END_HEADERS | FrameUtils.FLAG_END_STREAM));

        byte[] headersFrame = FrameUtils.createHeadersFrame(url, header);

        // headers frameの送信
        os.write(headersFrame);
        os.flush();

        String[] result = waitResponse(is, os);

        os.close();
        is.close();
        socket.close();

        return result;
    }


    private String[] waitResponse(InputStream is, OutputStream os) throws IOException {
        ArrayList<String> results = new ArrayList<String>();
        int pushPromiseCount = 0;
        while(true){
            // ヘッダーを取得する
            byte[] header = new byte[FrameUtils.HEADER_FRAME_LENGTH];
            is.readNBytes(header, 0, header.length);

            // TODO 多分ペイロードの読み込みがWINDOW_SIZE?を越えると読み込めなくなるから
            // 残りのWINDOW_SIZEを計測しながら分割して読み込む必要があると思う
            // 多分全体で80kb読み込んだらリセットかけないといけないんじゃないかな？

            // ヘッダからペイロードを取得し、ペイロードがあればそれを読み込む
            int payload = FrameUtils.getPayloadLength(header);
            byte[] payloadData = new byte[payload];
            if(payload != 0){
                is.readNBytes(payloadData,0,payloadData.length);
            }

            boolean breakLoop = false;
            switch (header[FrameUtils.FRAME_TYPE]){
                case FrameUtils.TYPE_DATA:// data
//                    System.out.println("[LOG]TYPE_DATA");
                    // ペイロードにデータが入っている
                    results.add(new String(payloadData, "UTF-8"));
//                    System.out.println(result);
                    if((header[FrameUtils.FRAME_FLAG] & FrameUtils.FLAG_END_STREAM) == FrameUtils.FLAG_END_STREAM){
                        if(pushPromiseCount==0){
//                            breakLoop = true;
                            // 接続終了時にはGOAWAYフレームを送信する
                            os.write(FrameUtils.GOAWAY_FRAME);
                            os.flush();
                        }
                        pushPromiseCount--;
                    }
                    break;
                case FrameUtils.TYPE_GOAWAY:// goaway
//                    System.out.println("[LOG]TYPE_GOAWAY");
                    breakLoop = true;
                    break;
                case FrameUtils.TYPE_PUSH_PROMISE:// push_promise
//                    System.out.println("[LOG]TYPE_PUSH_PROMISE");
                    pushPromiseCount++;
                    break;
                case FrameUtils.TYPE_SETTINGS:// settings
//                    System.out.println("[LOG]TYPE_SETTINGS");
                case FrameUtils.TYPE_WINDOW_UPDATE:// windows_update
//                    System.out.println("[LOG]TYPE_WINDOW_UPDATE");
                case FrameUtils.TYPE_HEADERS:// headers
//                    System.out.println("[LOG]TYPE_HEADERS");
                case FrameUtils.TYPE_PRIORITY:// priorityy
                case FrameUtils.TYPE_RST_STREAM:// rst_stream
                case FrameUtils.TYPE_PING:// ping
                case FrameUtils.TYPE_CONTINUATION:// continuation
                default:
                    break;
            }

            if(breakLoop){
                break;
            }
        }

        return results.toArray(new String[0]);
    }
}
