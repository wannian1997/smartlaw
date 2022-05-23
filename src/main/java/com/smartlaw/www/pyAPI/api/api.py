# -*- coding: utf-8 -*-
import json

from Paper import create_xml_test
from information_extraction import toJson


def docx2XJ(docxPath, xmlFolderPath, jsonFolderPath):
    # docx文件路径，xml文件所在文件夹路径，json文件所在文件夹路径
    # 自动生成同名文件
    create_xml_test(docxPath, xmlFolderPath)
    toJson(docxPath, jsonFolderPath)


def main():
    # 从path.json文件中读取三个路径
    dic = json.load(open(r'D:\projects_IDEA\smartlaw\src\main\java\com\smartlaw\www\pyAPI\path.json', 'r', encoding="utf-8"))
    docxPath = dic["docxPath"]
    xmlFolderPath = dic["xmlFolderPath"]
    jsonFolderPath = dic["jsonFolderPath"]
    docx2XJ(docxPath, xmlFolderPath, jsonFolderPath)


if __name__ == "__main__":
    main()