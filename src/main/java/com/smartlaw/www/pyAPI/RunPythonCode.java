package com.smartlaw.www.pyAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunPythonCode {

    public static void main(String[] srg){
        run();
    }

    public static void run(){
        // TODO Auto-generated method stub
        Process proc;
        try {
            proc = Runtime.getRuntime().exec("cmd /c D:\\projects_IDEA\\smartlaw\\src\\main\\java\\com\\smartlaw\\www\\pyAPI\\runPython.bat");// 执行py文件，需要使用绝对路径

            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


