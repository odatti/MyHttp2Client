package pd3;

import pd3.myhttp.MyClient;
import pd3.myhttp.MyHttpClient;
import pd3.myhttp.MyHttp2Client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class PD3Main {
    private static String[] trials = {"平文　（HTTP/1.1）","暗号化（HTTP/1.1）","平文　（HTTP/2）　","暗号化（HTTP/2）　"};
    private static String[] trialTypes = {"A","B","C","D"};
    private static final int TRIAL_INTERBAL = 10;// ms
    public static void main(String[] args){
        if(args.length < 2 || args == null){
            System.out.println("引数が指定されていません");
            return ;
        }

        try{
            // 使い捨ての試行（立ち上がりの遅さを回避するため）
            speedCheck(50, 1);// (n, p) n回ずつ試行、p*10kbのファイルを受信
            System.out.printf("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

            int tryNum = Integer.parseInt(args[0]);
            int requestNum = Integer.parseInt(args[1]);
            description(tryNum);
            speedCheck(tryNum, requestNum);// (n, p) n回ずつ試行、p*10kbのファイルを受信
//            test();
        }catch(Exception e){
            System.out.println("エラーが発生しました。");
            e.printStackTrace();

        }
    }

    public static void speedCheck(int tryNum, int requestNum) throws MalformedURLException {

        ArrayList<String> filesArray = new ArrayList<String>();
        filesArray.add("index.html");
        for(int k = 1;k <= requestNum;k++){
            runAllTrial(tryNum, k, filesArray);
        }
    }

    private static void description(int tryNum){
        System.out.println("各試行の実行回数："+tryNum+"回");
        System.out.println("各試行の識別子");
        for(int i = 0;i < trials.length;i++){
            System.out.println(trials[i] + "：" + trialTypes[i]);
        }
        System.out.println();
    }

    private static void runAllTrial(int tryNum, int requests, ArrayList<String> filesArray) throws MalformedURLException {
        // 取得するファイルを増やす（容量を10kb増やす）
        filesArray.add("test0" + (requests-1) + ".js");
        String path = "/index" + (requests-1) + ".html";
        System.out.println("取得するファイルのサイズ：" + requests*10 + "kbyte.");
        String[] files = filesArray.toArray(new String[0]);
        for(int j = 0;j < 4;j++){
            measureTheAllTrialTime(j,tryNum,files,path);
//            System.out.println(trialTypes[j]+ "の平均：" + sum / tryNum  + "ns　");
//            System.out.println(trialTypes[j]+ "の平均：" + sum / tryNum  + "ns　");
//            System.out.println(trialTypes[j]+ "の平均：" + sum / tryNum  + "ms/100　");
        }
        System.out.println();
    }

    private static void measureTheAllTrialTime(int caseNum, int tryNum, String[] files, String path) throws MalformedURLException {
        DecimalFormat df = new DecimalFormat("0.00");

        long sum = 0;
        ArrayList<Long> times = new ArrayList<>();
        for(int i = 0;i < tryNum;i++){
            long time = (measureTheTrialTime(caseNum, files, path) / (long)1000000f);
            times.add(time);
            sum += time;
//            sum += (long) measureTheTrialTime(caseNum, files, path) / 1000000f;
//                System.out.println("Time : " + (end - start) / 1000000f  + "ms");
            try {
                Thread.sleep(TRIAL_INTERBAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.printf("%sの平均：%sms, 標準偏差：%sms\n"
                , trialTypes[caseNum]
                , df.format((double) (sum/(double)tryNum))
                , df.format(calculateStandardDeviation(times, sum, tryNum)));
    }

    private static double calculateStandardDeviation(ArrayList<Long> times, long sum, long tryNum){
        long result = 0;
        long average = sum / tryNum;
        for(long time : times){
            result += time*time;
        }
        // ２乗の平均 - 平均の２乗
        result = result/tryNum - average*average;
        return Math.sqrt(result);
    }

    private static long measureTheTrialTime(int caseNum, String[] files, String path) throws MalformedURLException {
        long start = System.nanoTime();

        // 4つのケースに合わせて通信を行う
        selectCase(caseNum, files, path);

        long end = System.nanoTime();

        return (end - start);
    }

    private static void selectCase(int caseNum, String[] files, String path) throws MalformedURLException {
        switch (caseNum){
            case 0:
                trial(new URL("http://192.168.57.101" + path),MyClient.HTTP_1_1, files);
                break;
            case 1:
                trial(new URL("https://192.168.57.101" + path),MyClient.HTTP_1_1, files);
                break;
            case 2:
                trial(new URL("http://192.168.57.101" + path),MyClient.HTTP_2, files);
                break;
            case 3:
                trial(new URL("https://192.168.57.101" + path),MyClient.HTTP_2, files);
                break;
            default:
                break;
        }
    }

    public static void trial(URL url, int version, String[] files){
        try{
            MyClient client = null;
            // ソケット生成
            if(version == MyClient.HTTP_2){
                client =  new MyHttp2Client();
            }else{
                client = new MyHttpClient();
            }
            client.get(url, files);
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    public static void test() throws IOException {
        URL url = new URL("http://192.168.57.101/index.html");
//        MyClient client =  new MyHttpClient();
        MyClient client =  new MyHttp2Client();
        String[] results = client.get(url, new String[]{"index.html"});
        for(String result : results){
            System.out.println(result);
        }
    }
}
