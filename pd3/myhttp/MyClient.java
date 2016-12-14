package pd3.myhttp;


import java.io.IOException;

public interface MyClient {
    void open() throws IOException;
    String[] get(String[] files) throws IOException;
    void close() throws IOException;
}
