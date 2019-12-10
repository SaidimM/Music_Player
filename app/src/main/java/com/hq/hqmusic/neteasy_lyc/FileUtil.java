package com.hq.hqmusic.neteasy_lyc;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * <B>描述：</B>文件操作工具类<br/>
 * <B>版本：</B>v2.0<br/>
 * <B>创建时间：</B>2012-10-10<br/>
 * <B>版权：</B>flying团队<br/>
 *
 * @author zdf
 *
 */
public class FileUtil {


    /**
     * 文件转变成字符串，编程字符串格式
     *
     * @param file
     *            文件
     * @return 字符串
     */
    public static String fileToString(File file) {
        try {
            BufferedReader reader = null;
            String template = "";
            StringBuffer templateBuffer = new StringBuffer();
            String tempStr = null;
            // 读取文件，按照UTF-8的方式
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF-8"));
            // 一次读入一行，直到读入null为文件结束
            while ((tempStr = reader.readLine()) != null) {
                templateBuffer.append(tempStr + "\r\n");
            }
            // 将StringBuffer变成String进行字符操作
            template = templateBuffer.toString();
            reader.close();
            return template;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将字符串保存到文件中
     *
     * @param str
     *            字符串
     * @param file
     *            保存的文件
     */
    public static void stringToFile(String str, File file) {
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            writer.write(str);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /**
     * 将输入流转换成字符串输出
     *
     * @param is
     * @return 返回字符串
     */
    public static String streamToString(InputStream is){
        if( is != null){
            StringBuilder sb = new StringBuilder();
            String line = "";
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
                while((line = reader.readLine()) != null){
                    sb.append(line).append("\n");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return sb.toString();
        }else{
            return "";
        }
    }

    /**
     * 创建文件操作
     *
     * @param filePath 文件路径
     */
    public static File createFile(String filePath) {
        File file = new File(filePath);
        if(!file.exists()){
            if(filePath.endsWith(File.separator)){
//                throw new FlyingException("目标文件不能为目录！");
            }

            if(!file.getParentFile().exists()){
                if(!file.getParentFile().mkdirs()){
//                    throw new FlyingException("创建目标文件所在的目录失败！");
                }
            }
        }

        return file;
    }
    /**
     * 删除文件操作
     *
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.exists()) { // 判断文件是否存在
            if (file.isFile()) { // 判断是否是文件
                file.delete(); // delete()方法 你应该知道 是删除的意思;
            } else if (file.isDirectory()) { // 否则如果它是一个目录
                File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
                for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
                    FileUtil.deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
                }
            }
            file.delete();
        }
    }

    /**
     * 在本文件夹下查找
     *
     * @param s
     *            String 文件名
     * @return File[] 找到的文件
     */
    public static File[] getFiles(String s) {
        return getFiles("./", s);
    }

    /**
     * 获取文件 可以根据正则表达式查找
     *
     * @param dir
     *            String 文件夹名称
     * @param s
     *            String 查找文件名，可带*.?进行模糊查询
     * @return File[] 找到的文件
     */
    public static File[] getFiles(String dir, String s) {
        // 开始的文件夹
        File file = new File(dir);
        s = s.replace('.', '#');
        s = s.replaceAll("#", "\\\\.");
        s = s.replace('*', '#');
        s = s.replaceAll("#", ".*");
        s = s.replace('?', '#');
        s = s.replaceAll("#", ".?");
        s = "^" + s + "$";
        Pattern p = Pattern.compile(s);
        ArrayList list = filePattern(file, p);
        File[] rtn = new File[list.size()];
        list.toArray(rtn);
        return rtn;
    }

    /**
     * @param file
     *            File 起始文件夹
     * @param p
     *            Pattern 匹配类型
     * @return ArrayList 其文件夹下的文件夹
     */
    private static ArrayList filePattern(File file, Pattern p) {
        if (file == null) {
            return null;
        } else if (file.isFile()) {
            Matcher fMatcher = p.matcher(file.getName());
            if (fMatcher.matches()) {
                ArrayList list = new ArrayList();
                list.add(file);
                return list;
            }
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                ArrayList list = new ArrayList();
                for (int i = 0; i < files.length; i++) {
                    ArrayList rlist = filePattern(files[i], p);
                    if (rlist != null) {
                        list.addAll(rlist);
                    }
                }
                return list;
            }
        }
        return null;
    }

    /**
     * 重命名文件
     * @author zjx 2012-10-23
     * @param sourceFileName
     * @param destFileName
     * @return
     */
    public static boolean renameFile(String sourceFileName,String destFileName){
        File source_file = new File(sourceFileName);
        File dest_file = new File(destFileName);
        if(!source_file.exists()){
            throw new RuntimeException("重命名文件: no such file"+sourceFileName);
        }
        source_file.renameTo(dest_file);
        return true;
    }

    /**
     * 获取文件夹或者文件的大小
     * @param f
     * @return
     */
    public static long getFileSize(File f){
        long size = 0;
        if(!f.isDirectory()){    //如果是文件，直接返回文件大小
            size = f.length();
        }else{
            File[] filelist = f.listFiles();
            for(int i=0;i<filelist.length;i++){
                if(filelist[i].isDirectory()){
                    size += getFileSize(filelist[i]);
                }else{
                    size += filelist[i].length();
                }
            }
        }
        return size;
    }

    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // 获取文件大小

        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            // 文件太大，无法读取
            throw new IOException("File is to large "+file.getName());
        }

        // 创建一个数据来保存文件数据
        byte[] bytes = new byte[(int)length];
        // 读取数据到byte数组中
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length

                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        // 确保所有数据均被读取
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
    //合并两个字节数组
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    public  static void SaveFileFromInputStream(InputStream stream,String filename) throws IOException
    {
        int index = filename.lastIndexOf(File.separatorChar);
        String path = filename.substring(0,index + 1);
        File file=new File(path);
        if(!file .exists()  || !file.isDirectory()){
            file.mkdirs();
        }

        File saveFile = new File(filename);
        if(!saveFile .exists())
            saveFile.createNewFile();
        FileOutputStream fs = new FileOutputStream(filename);
        byte[] buffer =new byte[1024*1024];
        int bytesum = 0;
        int byteread = 0;
        while ((byteread=stream.read(buffer))!=-1)
        {
            bytesum+=byteread;
            fs.write(buffer,0,byteread);
            fs.flush();
        }
        fs.close();
        stream.close();
    }

    public static void main(String[] args) {
        String fileName = "C:/Users/Administrator/Desktop/Temp/";
        long size = FileUtil.getFileSize(new File(fileName));
        System.out.println("success."+size);
    }
}