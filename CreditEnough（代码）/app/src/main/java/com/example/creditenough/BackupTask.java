package com.example.creditenough;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupTask {
    private static final String COMMAND_BACKUP = "backupDatabase";
    private static final String COMMAND_RESTORE = "restoreDatabase";
    private final static String EXTERNAL_STORAGE_FOLDER = "Never Forget";
    private final static String EXTERNAL_STORAGE_BACKUP_DIR = "Backup";
    public String backup_version;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;

    public BackupTask(Context context) {
        this.mContext = context;
    }

    private static File getExternalStoragePublicDir() {//    /sdcard/Never Forget/
        String path = Environment.getExternalStorageDirectory() + File.separator + EXTERNAL_STORAGE_FOLDER + File.separator;
        File dir = new File(path);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public String doInBackground(String command) {
        File dbFile = mContext.getDatabasePath("creditenough.db");// 默认路径是 /data/data/(包名)/databases/*
        File exportDir = new File(getExternalStoragePublicDir(), EXTERNAL_STORAGE_BACKUP_DIR);//    /sdcard/Never Forget/Backup
        if (!exportDir.exists()){
            exportDir.mkdirs();
        }
        File backup = new File(exportDir, dbFile.getName());//备份文件与原数据库文件名一致
        if (command.equals(COMMAND_BACKUP)) {
            try {
                backup.createNewFile();
                fileCopy(dbFile, backup);//数据库文件拷贝至备份文件
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                Date date = new Date(System.currentTimeMillis());
                backup_version = simpleDateFormat.format(date);
                //backup.setLastModified(MyTimeUtils.getTimeLong());
                Log.d("myLog", "backup ok! 备份文件名："+backup.getName()+"\t"+backup_version);
                return backup_version;
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("myLog", "backup fail! 备份文件名："+backup.getName());
                return null;
            }
        } else if (command.equals(COMMAND_RESTORE)) {
            try {
                fileCopy(backup, dbFile);//备份文件拷贝至数据库文件
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                Date date = new Date(System.currentTimeMillis());
                backup_version = simpleDateFormat.format(date);
                Log.d("myLog", "restore success! 数据库文件名："+dbFile.getName()+"\t"+backup_version);
                return backup_version;
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("myLog", "restore fail! 数据库文件名："+dbFile.getName());
                return null;
            }
        } else {
            return null;
        }
    }

    private void fileCopy(File dbFile, File backup) throws IOException {
        FileChannel inChannel = new FileInputStream(dbFile).getChannel();
        FileChannel outChannel = new FileOutputStream(backup).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }
}
