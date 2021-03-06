package com.runvision.g69a_sn;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.runvision.bean.DaoMaster;
import com.runvision.bean.DaoSession;
import com.runvision.bean.FaceLibCore;
import com.runvision.core.Const;
import com.runvision.db.Admin;
import com.runvision.db.FaceProvider;
import com.runvision.gpio.GPIOHelper;
import com.runvision.utils.CameraHelp;
import com.runvision.utils.FileUtils;
import com.runvision.utils.LogToFile;
import com.runvision.utils.SPUtil;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/6/5.
 */
public class MyApplication extends Application {
    private String TAG = "MyApplication";

    private static Context context;
    ArrayList<Activity> list = new ArrayList<Activity>();

    private static MyApplication myApplication;
    public static FaceProvider faceProvider;
    public static FaceLibCore mFaceLibCore = new FaceLibCore();

    public static Map<String,byte[]> mList = new HashMap<String,byte[]>();

    private final static String DB_NAME = "socket_record.db";
    private static DaoSession daoSession;
    Uri mImage;

    public static MyApplication getInstance() {
        return myApplication;
    }


    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        myApplication = this;
        context = getApplicationContext();
        GPIOHelper.init();
      //  initLeakCanary();
        //初始化日志打印
        LogToFile.init(this);
        String serlia = getSerialNumber();
       if (serlia.equals("") || serlia.length() < 4 || !serlia.substring(0, 4).equals("R50A")) {
            finishActivity();
        }
        //Log.i("Gavin_1114","路径："+context.getFilesDir().listFiles().);
        File[] fs = context.getFilesDir().listFiles();
        String ff = null;
        for (File f : fs){
            System.out.println(f);
            Log.i("Gavin_1114","路径f："+f);
            ff= f.toString();
            Log.i("Gavin_1114","路径ff："+ff);
        }
        FileUtils.copyFile(ff,Environment.getExternalStorageDirectory() + "/FaceAndroid/.asf_install.dat");
        FileUtils.copyFile(Environment.getExternalStorageDirectory() + "/FaceAndroid/.asf_install.dat", getFilesDir().getAbsolutePath() + File.separator + ".asf_install.dat");

        int ret = mFaceLibCore.initLib(context);
        if (ret == 0) {
            Toast.makeText(this, "算法初始化成功", Toast.LENGTH_SHORT).show();
        } else {
            if (ret == 90113) {
                Toast.makeText(this, "算法初始化失败,SDK 激活失败,请打开读写权限", Toast.LENGTH_LONG).show();
            } else if (ret == 90119) {
                Toast.makeText(this, "算法初始化失败,唯一标识不匹配", Toast.LENGTH_LONG).show();
            } else if (ret == 90121) {
                Toast.makeText(this, "算法初始化失败,活体检测功能已过期", Toast.LENGTH_LONG).show();
            } else if (ret == 90129) {
                Toast.makeText(this, "算法初始化失败,激活数据被破坏,请删除激活文件,重新进行激活", Toast.LENGTH_LONG).show();
            } else if (ret == 90130) {
                Toast.makeText(this, "算法初始化失败,服务端未知错误", Toast.LENGTH_LONG).show();
            } else if (ret == 94209) {
                Toast.makeText(this, "算法初始化失败,无法解析主机地址", Toast.LENGTH_LONG).show();
            } else if (ret == 94210) {
                Toast.makeText(this, "算法初始化失败,无法连接服务器", Toast.LENGTH_LONG).show();
            } else if (ret == 94211) {
                Toast.makeText(this, "算法初始化失败,网络连接超时", Toast.LENGTH_LONG).show();
            } else if (ret == 94212) {
                Toast.makeText(this, "算法初始化失败,网络未知错误", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "算法初始化失败" + ret, Toast.LENGTH_LONG).show();
            }
        }
        faceProvider=new FaceProvider(this);
        if (faceProvider.querAdminSize() == 0) {
            faceProvider.addAdmin(new Admin("admin", "123456"));
        }

        //加载模板
        loadTemper();
        //配置数据库
        setupDatabase();
        SPUtil.putString(Const.KEY_EDITION, "(V "+LogToFile.getAppVersionName(getContext())+")");
    }


    private void loadTemper() {
        String path = Environment.getExternalStorageDirectory() + "/FaceAndroid/Template/";
        File mFile = new File(path);
        if (!mFile.exists()) {
            return;
        }
        File[] files = mFile.listFiles();
        Log.i("Gavin", "files:" + files.length);

        for (File file : files) {
            byte[] temp = CameraHelp.readFile(file);
            String userName = file.getName().substring(0, file.getName().indexOf("."));
            // byteslist.add(temp);
            mList.put(userName, temp);
        }

    }



    public String getSerialNumber() {
        String serial = "";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");

            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");

        } catch (Exception e) {
            Log.i("error", e.getMessage());
        }
        return serial;
    }

    public void init() {
        // 设置该CrashHandler为程序的默认处理器
        UnCeHandler catchExcep = new UnCeHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(catchExcep);
    }

    /**
     * Activity关闭时，删除Activity列表中的Activity对象
     */
    public void removeActivity(Activity a) {
        list.remove(a);
    }

    /**
     * 向Activity列表中添加Activity对象
     */
    public void addActivity(Activity a) {
        list.add(a);
    }

    /**
     * 关闭Activity列表中的所有Activity
     */
    public void finishActivity() {
        for (Activity activity : list) {
            if (null != activity) {
                activity.finish();
            }
        }
        // 杀死该应用进程
        android.os.Process.killProcess(android.os.Process.myPid());
    }



    /**
     * @param path
     * @return
     */
    public static Bitmap decodeImage(String path) {
        Bitmap res;
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inSampleSize = 1;
            op.inJustDecodeBounds = false;
            //op.inMutable = true;
            res = BitmapFactory.decodeFile(path, op);
            //rotate and scale.
            Matrix matrix = new Matrix();

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }

            Bitmap temp = Bitmap.createBitmap(res, 0, 0, res.getWidth(), res.getHeight(), matrix, true);
            Log.d("com.arcsoft", "check target Image:" + temp.getWidth() + "X" + temp.getHeight());

            if (!temp.equals(res)) {
                res.recycle();
            }
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setCaptureImage(Uri uri) {
        mImage = uri;
    }

    public Uri getCaptureImage() {
        return mImage;
    }

    /**
     * 配置数据库
     */
    private void setupDatabase() {
        //创建数据库sing_record.db
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, DB_NAME, null);
        //获取可写数据库
        SQLiteDatabase db = helper.getWritableDatabase();
        //获取数据库对象
        DaoMaster daoMaster = new DaoMaster(db);
        //获取dao对象管理者
        daoSession = daoMaster.newSession();
    }

    public static DaoSession getDaoSession() {
        return daoSession;
    }
}
