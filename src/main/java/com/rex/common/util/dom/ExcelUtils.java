package com.rex.common.util.dom;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ExcelUtils {
    /**
     * 读取excel
     * 基于hutools 的读取
     * ExcelReader reader = ExcelUtil.getReader();
     * List<List<Object>> readAll = reader.read();
     *
     * @param in       文件流
     * @param fileName 文件名称
     * @return
     * @throws IOException
     */
    public static List<Map<String, Object>> readExcel(InputStream in, String fileName) throws IOException {
        if (StringUtils.isBlank(fileName) || !fileName.contains(".")) {
            return null;
        }
        String type = fileName.split("[.]")[1];
        List<List<String>> list = new ArrayList<>();
        Workbook wb;
        if ("xls".equals(type)) {
            wb = new HSSFWorkbook(in);
        } else if ("xlsx".equals(type)) {
            wb = new XSSFWorkbook(in);
        } else {
            return null;
        }
        // 获取第一个sheet, getSheet(name)通过名称获取sheet
        Sheet sheet = wb.getSheetAt(0);
        int rowCount = sheet.getPhysicalNumberOfRows();
        if (rowCount > 0) {
            for (int i = 0; i < rowCount; i++) {
                List<String> rowList = new ArrayList<>();
                Row row = sheet.getRow(i);
                int cellCount = row.getPhysicalNumberOfCells();
                if (cellCount > 0) {
                    for (int j = 0; j < cellCount; j++) {
                        HSSFDataFormatter formatter = new HSSFDataFormatter();
                        Cell cell = row.getCell(j);
                        String value = formatter.formatCellValue(cell);
                        rowList.add(value);
                    }
                }
                list.add(rowList);
            }
        }
        return transform(list);
    }

    /**
     * 数据类型转换
     *
     * @param temList
     * @return
     */
    public static List<Map<String, Object>> transform(List<List<String>> temList) {
        List<Map<String, Object>> list = new LinkedList<>();
        if (temList != null && temList.size() > 0) {
            for (int i = 1; i < temList.size(); i++) {
                List<String> tem = temList.get(i);
                if (tem != null && tem.size() > 0) {
                    Map<String, Object> map = new HashMap<>();
                    for (int j = 0; j < tem.size(); j++) {
                        map.put(temList.get(0).get(j), tem.get(j));
                    }
                    list.add(map);
                }
            }
        }
        return list;
    }
}