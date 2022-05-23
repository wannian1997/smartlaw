package com.smartlaw.www.controller;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

import com.smartlaw.www.pyAPI.Utils;

@RestController
public class FileUploadController {

    List<String> docxPaths = new ArrayList<>();  // 用来存储读取的所有文件名

    //POST请求接口为/upload
    @PostMapping(value = "/upload",produces = "application/json;charset=utf-8")
    //@RequestParam指向前端input file的name,加入HttpServletRequest请求
    public String upload(@RequestParam("uploadFile") MultipartFile[] multipartFiles, HttpServletRequest request){
        //设置当前日期
        String uploaddate= new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        //设置文件上传保存文件路径：保存在项目运行目录下的uploadFile文件夹+当前日期
        String savepath = request.getSession().getServletContext().getRealPath("/uploadFile/")+uploaddate;
        String realLocalFolder = "D:\\projects_IDEA\\smartlaw\\src\\main\\webapp\\uploadFile\\" + uploaddate +"\\";  // 本地文件夹地址
        //创建文件夹,当文件夹不存在时，创建文件夹
        File folder = new File(savepath);
        if(!folder.isDirectory()){
            folder.mkdir();
        }

        List<String> notdocx = new ArrayList<>();  // 不符合文件类型的文件

        //建立一个循环分别接收多文件
        for(MultipartFile file:multipartFiles){
            //重命名上传的文件,为避免重复,我们使用UUID对文件分别进行命名
            String oldname = file.getOriginalFilename();//getOriginalFilename()获取文件名带后缀
            // 检查文件类型
            if (!check(oldname)){
                notdocx.add(oldname);
                continue;
            }

            Map<String,Object> map=new HashMap<>();  //建立每一个文件上传的返回参数

            //文件保存操作
            try {
                file.transferTo(new File(folder,oldname));
                //建立新文件路径,在前端可以直接访问如http://localhost:8080/uploadFile/2021-07-16/新文件名(带后缀)
                String filepath=request.getScheme()+"://"+request.getServerName()+":"+
                        request.getServerPort()+"/uploadFile/"+uploaddate+"/"+oldname;
                //写入返回参数
                docxPaths.add(oldname);
                System.out.println(docxPaths);
                Utils.docx2XmlJson(realLocalFolder + oldname);  // 生成xml文件和json文件，成功会有提示
            }catch (IOException ex){
                //操作失败报错
                ex.printStackTrace();
                map.put("[oldname]", oldname);
                map.put("[filepath]", "");
                map.put("[result]", "失败!");
            }
        }
        //将执行结果返回
        String jsonStr = Utils.getStr("D:\\projects_IDEA\\smartlaw\\src\\main\\webapp\\jsonFile\\82号黄晓娟非法采伐毁坏国家重点保护植物一审刑事判决书.json");
        Utils.readXml("D:\\projects_IDEA\\smartlaw\\src\\main\\webapp\\xmlFile\\中文.xml");
        System.out.println(jsonStr);
        String message = jsonStr + docxPaths.toString() + "上传成功\n" + notdocx.toString() + "文件类型不符合";
        return message;
    }

    public Boolean check(String filename){
        // 检查文件类型
        if (filename == null) return false;
        String[] temp = filename.split("\\.");
        String ans = temp[temp.length-1];
        Set<String> types = new HashSet<>();  // 指定文件类型
        types.add("docx");
        if (types.contains(ans)){
            return true;
        }else {
            return false;
        }
    }
}