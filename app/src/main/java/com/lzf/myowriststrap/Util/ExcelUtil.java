package com.lzf.myowriststrap.Util;

import android.content.Context;

import com.lzf.myowriststrap.bean.Orientation;

import java.io.File;
import java.util.List;

import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Excel 文件
 * <p>
 * Created by MJCoder on 2018-06-06.
 */
public class ExcelUtil {
    // Excel：sheet的头部标题
    private static final String[] TITLE = {"dateTime", "content", "arm", "xDirection", "pose", "orientation", "x", "y", "z", "w", "roll", "pitch", "yaw"};

    /**
     * 创建Excel并写入初始化数据
     *
     * @param context
     * @param fileName Excel文件名称
     * @throws Exception 抛出异常
     */
    public static File createExcel(Context context, String dirName, String fileName, List<Orientation> orientationList) throws Exception {
        File excelFile = FileUtil.getFile(context, dirName, fileName);
        // 创建Excel工作表
        WritableWorkbook writableWorkbook = Workbook.createWorkbook(excelFile);
        // 添加第一个工作表并设置第一个Sheet的名字
        WritableSheet sheet = writableWorkbook.createSheet(fileName, 0);
        for (int i = 0; i < TITLE.length; i++) {
            // Label(x,y,z) 代表单元格的第x+1列，第y+1行, 内容z
            // 在Label对象的子对象中指明单元格的位置和内容
            // 将定义好的单元格添加到工作表中
            sheet.addCell(new Label(i, 0, TITLE[i], getHeader()));
        }
        for (int i = 0; i < orientationList.size(); i++) {
            Orientation orientation = orientationList.get(i);
            sheet.addCell(new Label(0, i + 1, orientation.getDateTime()));
            sheet.addCell(new Label(1, i + 1, orientation.getContent()));
            sheet.addCell(new Label(2, i + 1, orientation.getArm() + ""));
            sheet.addCell(new Label(3, i + 1, orientation.getxDirection() + ""));
            sheet.addCell(new Label(4, i + 1, orientation.getPose() + ""));
            sheet.addCell(new Label(5, i + 1, orientation.getOrientation() + ""));
            sheet.addCell(new Number(6, i + 1, orientation.getX()));
            sheet.addCell(new Number(7, i + 1, orientation.getY()));
            sheet.addCell(new Number(8, i + 1, orientation.getZ()));
            sheet.addCell(new Number(9, i + 1, orientation.getW()));
            sheet.addCell(new Number(10, i + 1, orientation.getRoll()));
            sheet.addCell(new Number(11, i + 1, orientation.getPitch()));
            sheet.addCell(new Number(12, i + 1, orientation.getYaw()));
        }
        writableWorkbook.write(); // 写入数据
        writableWorkbook.close(); // 关闭文件
        return excelFile;
    }

    /**
     * 设置 Excel：sheet的头部标题
     *
     * @return
     * @throws WriteException
     */
    private static WritableCellFormat getHeader() throws WriteException {
        WritableFont font = new WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD);// 定义字体
        font.setColour(Colour.BLUE);// 蓝色字体
        WritableCellFormat format = new WritableCellFormat(font);
        format.setAlignment(jxl.format.Alignment.CENTRE);// 左右居中
        format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 上下居中
        format.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);// 黑色边框
        format.setBackground(Colour.YELLOW);// 黄色背景
        return format;
    }
}
