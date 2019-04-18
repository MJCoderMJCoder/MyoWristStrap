package com.lzf.myowriststrap.Util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 复制相关内容到SD卡中以便可视化查看
 * Created by MJCoder on 2017-10-09.
 */
public class CopyFileToSD {
    /**
     * 将过大的内容或是过长的字符串复制到SD卡中以便可视化查看
     *
     * @param context     环境上下文
     * @param dirName     新建的文件所在的上级目录；可以为空（为空时该文件将直接插入项目的根目录）
     * @param fileName    复制到SD卡的文件名称（切记：需要包含后缀）
     * @param fileContent 过大的内容或是过长的字符串（例如：json、log、服务端返回的大数据等）
     * @return 返回已经复制到SD卡的文件对象
     */
    public static File txtFile(Context context, String dirName, String fileName, String fileContent) {
        FileOutputStream fos = null;
        RandomAccessFile randomFile = null;
        FileWriter writer = null;
        File returnFile = null;
        try {
            //            //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件,如果为 true，则将字节写入文件末尾处，而不是写入文件开始处
            //            writer = new FileWriter(fileName, append);
            //            writer.write(fileContent);
            //            if (append) {
            //                // 打开一个随机访问文件流，按读写方式
            //                randomFile = new RandomAccessFile(fileName, "rw");
            //                // 文件长度，字节数
            //                long fileLength = randomFile.length();
            //                //将写文件指针移到文件尾。在该位置发生下一个读取或写入操作。
            //                randomFile.seek(fileLength);
            //                //按字节序列将该字符串写入该文件。
            //                randomFile.writeBytes(fileContent);
            //            } else {
            //文件复制到sd卡中；覆盖源文件的内容。
            returnFile = FileUtil.getFile(context, dirName, fileName);
            fos = new FileOutputStream(returnFile);
            fos.write(fileContent.getBytes());  //将String字符串以字节流的形式写入到输出流中
            fos.close();
            fos.flush();
            //            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭数据流
            try {
                if (fos != null)
                    fos.close();
                if (randomFile != null)
                    randomFile.close();
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return returnFile;
    }

    /**
     * 将Database文件复制到SD卡中以便可视化查看
     *
     * @param context      环境上下文
     * @param dirName      新建的文件所在的上级目录；可以为空（为空时该文件将直接插入项目的根目录）
     * @param databaseName Database文件的名称（切记：需要包含后缀）
     * @param packageName  该APP的包名
     * @return 返回已经复制到SD卡的文件对象
     */
    public static File databaseFile(Context context, String dirName, String databaseName, String packageName) {
        //找到文件的路径  /data/data/包名/databases/数据库名称
        File databaseFile = new File(Environment.getDataDirectory().getAbsolutePath() + "/data/" + packageName + "/databases/" + databaseName);
        FileInputStream fis = null;
        FileOutputStream fos = null;
        File returnFile = null;
        try {
            //文件复制到sd卡中
            fis = new FileInputStream(databaseFile);
            returnFile = FileUtil.getFile(context, dirName, databaseName);
            fos = new FileOutputStream(returnFile);
            int len = 0;
            byte[] buffer = new byte[20480];
            while (-1 != (len = fis.read(buffer))) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭数据流
            try {
                if (fos != null)
                    fos.close();
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return returnFile;
    }


    /**
     * 将SharedPrefs文件复制到SD卡中以便可视化查看
     *
     * @param context         环境上下文
     * @param dirName         新建的文件所在的上级目录；可以为空（为空时该文件将直接插入项目的根目录）
     * @param sharedPrefsName SharedPrefs文件的名称（切记：需要包含后缀）
     * @param packageName     该APP的包名
     * @return 返回已经复制到SD卡的文件对象
     */
    public static File sharedPrefsFile(Context context, String dirName, String sharedPrefsName, String packageName) {
        //找到文件的路径  /data/data/包名/databases/数据库名称
        File sharedPrefsFile = new File(Environment.getDataDirectory().getAbsolutePath() + "/data/" + packageName + "/shared_prefs/" + sharedPrefsName);
        FileInputStream fis = null;
        FileOutputStream fos = null;
        File returnFile = null;
        try {
            //文件复制到sd卡中
            fis = new FileInputStream(sharedPrefsFile);
            returnFile = FileUtil.getFile(context, dirName, sharedPrefsName);
            fos = new FileOutputStream(returnFile);
            int len = 0;
            byte[] buffer = new byte[20480];
            while (-1 != (len = fis.read(buffer))) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭数据流
            try {
                if (fos != null)
                    fos.close();
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return returnFile;
    }
}
