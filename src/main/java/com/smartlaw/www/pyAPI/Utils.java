package com.smartlaw.www.pyAPI;

import java.io.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.alibaba.fastjson.JSONObject;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Utils {

    public static void main(String[] srg){
//        docx2XmlJson("E:\\docx\\82号黄晓娟非法采伐毁坏国家重点保护植物一审刑事判决书.docx");
//        readXml("D:\\projects_IDEA\\smartlaw\\src\\main\\webapp\\xmlFile\\中文.xml");
        String jsonStr = getStr("D:\\projects_IDEA\\smartlaw\\src\\main\\webapp\\jsonFile\\26被告人董连元非法采伐国家重点保护植物一审刑事判决书.json");
        System.out.println(jsonStr);
        String a = JsonFormatTool.formatJson(getStr("D:\\projects_IDEA\\smartlaw\\src\\main\\webapp\\jsonFile\\26被告人董连元非法采伐国家重点保护植物一审刑事判决书.json"));
        System.out.println(a);
    }
    public static void readJson(String jsonPath){

    }

    //把一个json文件中的内容读取成一个String字符串
    public static String getStr(String filePath) {
        try {
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), "GBK");
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = bufferedReader.readLine();
                while (lineTxt != null) {
                    return lineTxt;
                }
            }
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            System.out.println("Cannot find the file specified!");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error reading file content!");
            e.printStackTrace();
        }
        return null;
    }

    private static void process(String jsonStr) {
        JSONObject jo = JSONObject.parseObject(jsonStr);

    }

    public static void readXml(String xmlPath) {
        String oldXmlPath = xmlPath;
        String newXmlPath = "D:\\projects_IDEA\\smartlaw\\src\\main\\webapp\\xmlFile\\flash\\temp.xml";  // 暂存路径
        rename(xmlPath,newXmlPath);

        try {
            // 创建解析器工厂
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();
            // 创建一个Document对象
            Document doc = db.parse(newXmlPath);
            NodeList bookList = doc.getElementsByTagName("paras");
            // 获取节点个数
            System.out.println("一共有" + bookList.getLength() + "本书");

            // 遍历每个book节点
            for (int i = 0; i < bookList.getLength(); i++) {
                System.out.println("*******************************");
                // 索引从零开始
                org.w3c.dom.Node book = bookList.item(i);
                // 获取book节点所有属性集合
                org.w3c.dom.NamedNodeMap attrs = book.getAttributes();

                System.out.println("第" + (i + 1) + "本书共有" + attrs.getLength() + "属性");
                // 遍历book属性，不知道节点属性和属性名情况
                for (int j = 0; j < attrs.getLength(); j++) {
                    // 获取某一个属性
                    org.w3c.dom.Node attr = attrs.item(j);
                    System.out.print("属性名:" + attr.getNodeName());
                    System.out.println(" --- 属性值:" + attr.getNodeValue());
                }

                // 若已经知道book节点有且只有1个ID属性,可用以下方式
                // org.w3c.dom.Element e = (org.w3c.dom.Element)
                // bookList.item(i);
                // System.out.println("Element属性值:"+e.getAttribute("id"));

                NodeList childNodes = book.getChildNodes();
                for (int k = 0; k < childNodes.getLength(); k++) {
                    // 区分,去掉空格和换行符
                    if (childNodes.item(k).getNodeType() == Node.ELEMENT_NODE) {
                        // 获取element类型的节点和节点值
                        System.out.print("节点名：" + childNodes.item(k).getNodeName());
                        System.out.print(" --- 节点值：" + childNodes.item(k).getFirstChild().getNodeValue());
                        System.out.println(" --- 节点值："+childNodes.item(k).getTextContent());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        rename(newXmlPath,oldXmlPath);  // 恢复原文件
    }

    public static void docx2XmlJson(String docxPath){
        // 通过docx文件路径，生成Python代码解析过的xml和json文件
        createPathJson(docxPath);  // 修改或生成path.json文件，用以确定需要生成xml和json的文件
        RunPythonCode.run();  // 运行bat脚本，运行Python代码
    }

    public static void createPathJson(String docxPath){
        // 修改或生成path.json文件，用以确定需要生成xml和json的文件
        String oldFile = "D:\\projects_IDEA\\smartlaw\\src\\main\\java\\com\\smartlaw\\www\\pyAPI\\path.json";
        // 生成文件内容
//        Map<String,String> map = new HashMap<>();
//        map.put("docxPath",docxPath);
//        map.put("xmlFolderPath","D:\\projects_IDEA\\smartlaw\\src\\main\\java\\com\\smartlaw\\www\\pyAPI\\api");
//        map.put("jsonFolderPath","D:\\projects_IDEA\\smartlaw\\src\\main\\java\\com\\smartlaw\\www\\pyAPI\\api");

        String jsonString = "{\"docxPath\":\""+docxPath.replace("\\","\\\\") + "\"," +
                "\"xmlFolderPath\":\"D:\\\\projects_IDEA\\\\smartlaw\\\\src\\\\main\\\\webapp\\\\xmlFile\"," +
                "\"jsonFolderPath\":\"D:\\\\projects_IDEA\\\\smartlaw\\\\src\\\\main\\\\webapp\\\\jsonFile\"" +
                "}";

        System.out.println(jsonString);
        // 生成json格式文件
        try {
            // 保证创建一个新文件
            File file = new File(oldFile);
            if (!file.getParentFile().exists()) { // 如果父目录不存在，创建父目录
                file.getParentFile().mkdirs();
            }
            if (file.exists()) { // 如果已存在,删除旧文件
                file.delete();
            }
            file.createNewFile();

            // 格式化json字符串
            jsonString = JsonFormatTool.formatJson(jsonString);

            // 将格式化后的字符串写入文件
            Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            write.write(jsonString);
            write.flush();
            write.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void rename(String oldPath,String newPath){
        // 解决xml文件无法读取中文名的问题
        File oldName = new File(oldPath);
        File newName = new File(newPath);
        if (oldName.renameTo(newName)) {
//            System.out.println("renamed success");

        } else {
            System.out.println("Error in rename");
        }
    }
}

class JsonFormatTool {
    /**
     * 单位缩进字符串。
     */
    private static String SPACE = "   ";

    /**
     * 返回格式化JSON字符串。
     *
     * @param json 未格式化的JSON字符串。
     * @return 格式化的JSON字符串。
     */
    public static String formatJson(String json) {
        StringBuffer result = new StringBuffer();

        int length = json.length();
        int number = 0;
        char key = 0;

        // 遍历输入字符串。
        for (int i = 0; i < length; i++) {
            // 1、获取当前字符。
            key = json.charAt(i);

            // 2、如果当前字符是前方括号、前花括号做如下处理：
            if ((key == '[') || (key == '{')) {
                // （1）如果前面还有字符，并且字符为“：”，打印：换行和缩进字符字符串。
                if ((i - 1 > 0) && (json.charAt(i - 1) == ':')) {
                    result.append('\n');
                    result.append(indent(number));
                }

                // （2）打印：当前字符。
                result.append(key);

                // （3）前方括号、前花括号，的后面必须换行。打印：换行。
                result.append('\n');

                // （4）每出现一次前方括号、前花括号；缩进次数增加一次。打印：新行缩进。
                number++;
                result.append(indent(number));

                // （5）进行下一次循环。
                continue;
            }

            // 3、如果当前字符是后方括号、后花括号做如下处理：
            if ((key == ']') || (key == '}')) {
                // （1）后方括号、后花括号，的前面必须换行。打印：换行。
                result.append('\n');

                // （2）每出现一次后方括号、后花括号；缩进次数减少一次。打印：缩进。
                number--;
                result.append(indent(number));

                // （3）打印：当前字符。
                result.append(key);

                // （4）如果当前字符后面还有字符，并且字符不为“，”，打印：换行。
                if (((i + 1) < length) && (json.charAt(i + 1) != ',')) {
                    result.append('\n');
                }

                // （5）继续下一次循环。
                continue;
            }

            // 4、如果当前字符是逗号。逗号后面换行，并缩进，不改变缩进次数。
            if ((key == ',')) {
                result.append(key);
                result.append('\n');
                result.append(indent(number));
                continue;
            }

            // 5、打印：当前字符。
            result.append(key);
        }

        return result.toString();
    }

    /**
     * 返回指定次数的缩进字符串。每一次缩进三个空格，即SPACE。
     *
     * @param number 缩进次数。
     * @return 指定缩进次数的字符串。
     */
    private static String indent(int number) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < number; i++) {
            result.append(SPACE);
        }
        return result.toString();
    }
}
