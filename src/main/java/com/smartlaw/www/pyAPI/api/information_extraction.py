# coding=utf-8
import os
import re
import json

from utils import minDistance, ChineseDate2Date
from Paper import Paper
# """抽取裁判文书关键信息并生成json文件"""
# 必要文件：'..\Data_Access\data\anyou.txt'


class PaperInfo(Paper):
    # """对裁判文书进行信息抽取"""
    # 状态信息

    # paper_info 表格信息
    case_ID = ''  # 案号
    cause_of_action = ''  # 案由
    paper_name = ''  # 文件名
    court = ''
    paper_type = ''
    prosecution = ''   # 原告，检察院
    indictment_ID = ''  # 起诉书编号
    time_of_case = ''  # 指控段落中的案发时间
    date = ''  # 裁判文书判决日期

    # defendants 表格信息（被告人信息）
    defendants = []

    # judge

    # related_laws
    def __init__(self, docx_path):
        super().__init__(docx_path)  # 调用父类的构造函数
        self.extract_paper_info()

    def extract_paper_info(self):
        # paper_info 表格信息抽取
        self.case_ID = self.paras[self.dict_label['label30'][0]]
        self.cause_of_action = self.cause_of_action_func()
        self.paper_name = self.case_name
        self.court = self.paras[self.dict_label['label10'][0]]
        self.paper_type = self.paras[self.dict_label['label20'][0]]
        self.prosecution = self.prosecution_func()
        self.indictment_ID = self.indictment_ID_func()
        # 此处缺少一项内容的提取time_of_case,原因：可能没用
        self.date = self.date_func()
        self.xml = self.xml_func()

        # defendants 表格信息（被告人信息）
        self.defendants = self.defendants_func()
        pass

    def cause_of_action_func(self):
        #"""提取案由"""
        # 处理文件名，去掉路径信息
        dir, file_name = os.path.split(self.case_name)
        self.case_name = file_name.split('.')[0]

        # 通过文本匹配算法进行案由抽取
        anyou = ''
        f01 = open(r'D:\projects_IDEA\smartlaw\src\main\java\com\smartlaw\www\pyAPI\api\data\anyou.txt', "r")  # 设置文件对象，其中txt文件编码类型为ANSI
        all_cause_of_action = f01.readlines()  # 直接将文件中按行读到list里，效果与方法2一样
        f01.close()  # 关闭文件

        # 文本匹配算法（编辑距离）
        all_distance = []
        for ay in all_cause_of_action:
            ay = ay.replace('\n', '')  # 删除回车
            all_distance.append(minDistance(ay, self.case_name))
            if ay[1:len(ay)-1] in self.case_name:
                anyou = ay
        if anyou == '':
            miniDistance = 99999999
            miniDistance_index = -1
            for i in range(len(all_distance)):
                if all_distance[i]<miniDistance:
                    miniDistance_index = i
            anyou = all_cause_of_action[miniDistance_index]
        return anyou.replace('\n', '')

    def prosecution_func(self):
        #"""提取公诉机关"""
        para = self.paras[self.dict_label['label40'][0]]  # 定位指定段落
        para = para.replace('公诉机关', '')
        para = para.replace('暨附带民事公益诉讼起诉人', '')
        para = para.replace('暨附带公益诉讼起诉人', '')
        para = para.replace('：', '')
        return para

    def indictment_ID_func(self):
        # 获取指控段第一段
        para = str
        for label, paras_index in self.dict_label.items():
            if label[5] == '6':
                para = self.paras[paras_index[0]].split('。')[0]
                break
        pattern = r'以(.*)起诉'
        para = re.search(pattern, para).group(1)
        return para

    def date_func(self):
        #"""判决日期"""
        para_date = str
        paras_index = self.dict_label['label80']
        for p in paras_index:
            if not re.search(r'.*年.*月.*日', self.paras[p]) is None:
                para_date = self.paras[p]
                break
        return ChineseDate2Date(para_date)

    def xml_func(self):
        #"""将全文结构化存储，保留有效信息(生成文本格式见example.xml文件，但不带回车)"""
        xml = ""
        for label, paras_list in self.dict_label.items():
            head = f"<paras label=\"{label}\">"  # 头
            body = ""
            tail = "</paras>"  # 尾
            # 生成body
            for para_index in paras_list:
                head0 = f"<para index = \"{para_index}\">"  # 段落编号
                body0 = self.paras[para_index]  # 段落文字
                tail0 = "</para>"
                body += head0 + body0 + tail0
            xml += head + body + tail
        return xml

    def defendants_func(self):
        #"""获取被告人信息:姓名，性别，出生年月，民族，教育背景，籍贯，出生地，居住地，辩护人，辩护人律师事务所未抽取：罪名，拘留时间，拘留地点，取保候审时间，取保候审机关"""
        defendents_list = []
        for index in self.dict_label["label50"]:
            defendents_list.append(self.paras[index])
        defendents_info = []  # 被告人抽取信息列表，二维
        for i in range(len(defendents_list)):
            if not re.search("被告人", defendents_list[i][0:10]):  # 辩护人段，跳过
                continue
            str = defendents_list[i]
            defendent_info = []  # 单个被告的信息
            # 基本信息，抽取
            name = re.search(r"(?<=告人|单位|被告)([\u4e00-\u9fa5]{1,5})(?=[，。])", str)
            gender = re.search(r"([男女])", defendents_list[i])
            birthday = re.search(r"(\d{4}年\d{1,2}月\d{1,2}日)(?=出生)", str)
            nation = re.search(r"([\u4e00-\u9fa5]{1,6}族)(?=[，。])", str)
            education_level = re.search(r"([\u4e00-\u9fa5]{1,10})(?=文化|毕业)|(文盲)", str)
            register_residence = re.search(r"(?<=户籍地|所在地)([\u4e00-\u9fa5A-Za-z0-9]{1,20})(?=[，。])", str)  # 户籍所在地
            birthday_place = re.search(r"(?<=出生地|出生于)([\u4e00-\u9fa5A-Za-z0-9]{1,20})(?=[，。])",str)
            current_residence = re.search(r"(?=住)([\u4e00-\u9fa5A-Za-z0-9]{1,20})(?=[，。])",str)
            # 存储（顺序存储）
            if name is None:
                defendent_info.append(None)
            else:
                defendent_info.append(name.group())
            if gender is None:
                defendent_info.append(None)
            else:
                defendent_info.append(gender.group())
            if birthday is None:
                defendent_info.append(None)
            else:
                defendent_info.append(birthday.group())
            if nation is None:
                defendent_info.append(None)
            else:
                defendent_info.append(nation.group())
            if education_level is None:
                defendent_info.append(None)
            else:
                defendent_info.append(education_level.group())
            if register_residence is None:
                defendent_info.append(None)
            else:
                defendent_info.append(register_residence.group())
            if birthday_place is None:
                defendent_info.append(None)
            else:
                defendent_info.append(birthday_place.group())
            if current_residence is None:
                defendent_info.append(None)
            else:
                defendent_info.append(current_residence.group())
            # 辩护人信息 抽取
            if i+1 < len(defendents_list):
                str_next = defendents_list[i+1]
                name_of_advocate = re.search(r"(?=辩护人)([\u4e00-\u9fa5]{1,5})(?=[，。])", str_next)
                law_offices = re.search(r"(?=[，。])([\u4e00-\u9fa5]{1,5})(?=律师)", str_next)
            else:
                name_of_advocate = None
                law_offices = None
            # 存储
            if name_of_advocate is None:
                defendent_info.append(None)
            else:
                defendent_info.append(name_of_advocate.group())
            if law_offices is None:
                defendent_info.append(None)
            else:
                defendent_info.append(law_offices.group())
            defendents_info.append(defendent_info)  # 将单个被告信息加入列表
        return defendents_info


def temp(path):
    # 单文档标注测试
    paper = PaperInfo(path)
    for key, value in paper.dict_label.items():
        print(key, value)
    for p in paper.paras:
        print(p)
    # for s in paper.sentences:
    #     print(s)


def toJson(docxPath, jsonFileFolder = ""):
    #"""将抽取的信息存储为json文件"""
    # jsonFileFolder 为空时，保存在相同文件夹中
    paper_info = PaperInfo(docxPath)
    jsonDict = {}
    # paper_info 表格信息
    jsonDict["case_ID"] = paper_info.case_ID  # 案号
    jsonDict["cause_of_action"] = paper_info.cause_of_action  # 案由
    jsonDict["paper_name"] = paper_info.paper_name  # 文件名
    jsonDict["court"] = paper_info.court
    jsonDict["paper_type"] = paper_info.paper_type
    jsonDict["prosecution"] = paper_info.prosecution   # 原告，检察院
    jsonDict["indictment_ID"] = paper_info.indictment_ID  # 起诉书编号
    jsonDict["time_of_case"] = paper_info.time_of_case  # 指控段落中的案发时间
    jsonDict["date"] = paper_info.date  # 裁判文书判决日期

    # defendants 表格信息（被告人信息）
    # defendants = [['董某', '男', '1951年10月14日', '汉族', '小学', None, '黑龙江省呼兰县', None, None, None]]
    jsonDict["defendants"] = paper_info.defendants  # 被告人信息
    try:
        # 生成json文件名
        jsonPath = docxPath.split('\\')[-1]
        jsonPath = jsonPath.split('.')[0] + '.json'
        jsonPath = jsonFileFolder + "\\" + jsonPath
    except:
        print(f'docxPath:{docxPath}\njsonPath{jsonPath}')
        print("jsonPath文件路径解析出错，请检查")
    # 写入文件
    with open(jsonPath, 'w') as file_obj:
        json.dump(jsonDict, file_obj, ensure_ascii=False)
    # print(f"成功生成《{jsonPath}》文件")
    print("json file success")

def main():
    docxPath = r'E:\docx\熊建飞非法采伐毁坏国家重点保护植物一审刑事判决书.docx'
    jsonFileFolder = r'D:\projects_pycharm\LawProcess\Data_Access'  # json文件所在文件夹,
    toJson(docxPath, jsonFileFolder)


if __name__ == '__main__':
    main()