package pd3.myhttp;

import java.io.IOException;
import java.net.URL;

public class MyHttpClient implements MyClient{

    @Override
    public void open() throws IOException {

    }

    @Override
    public String[] get(String[] files) throws IOException {
        return new String[0];
    }

    @Override
    public void close() throws IOException {

    }
}
