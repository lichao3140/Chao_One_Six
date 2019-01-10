package com.runvision.db;

import android.graphics.Bitmap;
import android.util.Log;
import com.runvision.bean.SocketRecord;
import com.runvision.bean.SocketRecordDao;
import com.runvision.bean.Type;
import com.runvision.core.Const;
import com.runvision.g69a_sn.MainActivity;
import com.runvision.g69a_sn.MyApplication;
import com.runvision.thread.SocketThread;
import com.runvision.utils.CameraHelp;
import com.runvision.utils.ConversionHelp;
import com.runvision.utils.DateTimeUtils;
import com.runvision.utils.SPUtil;
import com.runvision.utils.TimeCompareUtil;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

/**
 * 续传数据库操作
 */
public class SocketDataHelper {
    private static String TAG = "SocketDataHelper";

    private static List<SocketRecord> mRecordList;
    private static List<User> userList;
    private static List<SocketRecord> findRecordList;

    /**
     * 添加到数据库
     * @param time 比对时间
     * @param snapImageID 比对抓拍人脸
     * @param templateImageID 1vsN---比对人脸模板  1vs1---身份证号码
     */
    public static void addSocketRecord(String time, String snapImageID, String templateImageID) {
        SocketRecord socketRecord = new SocketRecord();
        socketRecord.setCreateTime(time);
        socketRecord.setSnapImageID(snapImageID);
        socketRecord.setTemplateImageID(templateImageID);
        socketRecord.setIsSuccess("0");
        MainActivity.socketRecordDao.insert(socketRecord);
    }

    /**
     * 三分钟内不识别同一个人
     * @param id 人脸模板id
     * @return
     */
    public static boolean isToWork(int id) {
        Boolean Flag;
        Date beginTime;
        Date endTime;
        userList = MyApplication.faceProvider.queryUser("select * from tUser where id=" + id);
        findRecordList = MainActivity.socketRecordDao.queryBuilder().where(SocketRecordDao.Properties.TemplateImageID.eq(userList.get(0).getTemplateImageID())).build().list();
        if (findRecordList.size() != 0) {
            beginTime = TimeCompareUtil.strToDate(DateTimeUtils.transferLongToDate("yyyy-MM-dd HH:mm:ss", Long.valueOf(findRecordList.get(findRecordList.size() - 1).getCreateTime())));
            endTime = new Date();
            endTime.setTime(Long.valueOf(findRecordList.get(findRecordList.size() - 1).getCreateTime()) + 3 * 60 * 1000);
            endTime = TimeCompareUtil.strToDate(DateTimeUtils.transferLongToDate("yyyy-MM-dd HH:mm:ss", endTime.getTime()));
            Flag = TimeCompareUtil.belongCalendar(new Date(), beginTime, endTime);
            Log.i(TAG, "isToWork:" + TimeCompareUtil.belongCalendar(new Date(), beginTime, endTime));
        } else {
            Flag = false;
        }
        return Flag;
    }

    /**
     * 开始续传
     */
    public static void beginSocket(SocketThread socketThread) {
        mRecordList = MainActivity.socketRecordDao.loadAll();
        for (int i = 0; i < mRecordList.size(); i++) {
            if (mRecordList.get(i).getIsSuccess().equals("0")) { //未上传记录
                userList = MyApplication.faceProvider.queryRecord("select * from tRecord where snapImageID like '%" + mRecordList.get(i).getSnapImageID() + "%'");
                //找到未上传成功到VMS的信息
                if (userList.size() != 0) {
                    sendSocketMsgInfo(socketThread, userList);
                    //上传到VMS，标志设置为isSuccess=1
                    SocketRecord failRecord = MainActivity.socketRecordDao.queryBuilder().where(SocketRecordDao.Properties.SnapImageID.eq(mRecordList.get(i).getSnapImageID())).build().unique();
                    if (failRecord != null) {
                        failRecord.setIsSuccess("1");
                        MainActivity.socketRecordDao.update(failRecord);
                    }
                }
            } else if (mRecordList.get(i).getIsSuccess().equals("1")) { //续传成功
                SocketRecord successRecord = MainActivity.socketRecordDao.queryBuilder().where(SocketRecordDao.Properties.SnapImageID.eq(mRecordList.get(i).getSnapImageID())).build().unique();
                if (successRecord != null) {
                    MainActivity.socketRecordDao.deleteByKey(successRecord.getId());
                }
            }
        }
    }

    /**
     * 发送未成功的对比信息
     * @param socketThread
     * @param userList
     */
    public static void sendSocketMsgInfo(SocketThread socketThread, List<User> userList) {
        byte[] cardByte = new byte[0];
        byte[] faceByte = new byte[0];
        // 识别类型 1:1 和 1:N  对比图片
        char type = 0;
        Bitmap cardBmp;//模板图片
        Bitmap nFaceBmp;//1:n抓拍图片
        Bitmap oneFaceBmp;//1:1抓拍图片
        if (userList.get(0).getRecord().getComperType().equals("1:N")) {
            type = 0x02;
        } else if (userList.get(0).getRecord().getComperType().equals("人证")) {
            type = 0x01;
        }
        if(type == Const.TYPE_ONEVSMORE) {
            cardBmp = CameraHelp.getSmallBitmap(Const.ImagePath + userList.get(0).getTemplateImageID() + ".jpg");
            nFaceBmp = CameraHelp.getSmallBitmap(userList.get(0).getRecord().getSnapImageID() + ".jpg");
            if(cardBmp != null && nFaceBmp != null) {
                cardByte = ConversionHelp.bitmapToByte(cardBmp);
                faceByte = ConversionHelp.bitmapToByte(nFaceBmp);
            }
        } else {
            cardBmp = CameraHelp.getSmallBitmap(userList.get(0).getTemplateImageID() + ".jpg");
            oneFaceBmp = CameraHelp.getSmallBitmap(userList.get(0).getRecord().getSnapImageID() + ".jpg");
            if(cardBmp != null && oneFaceBmp != null) {
                cardByte = ConversionHelp.bitmapToByte(cardBmp);
                faceByte = ConversionHelp.bitmapToByte(oneFaceBmp);
            }
        }

        // 协议格式
        byte[] sendComperMsgData = new byte[1306 + cardByte.length + faceByte.length];
        byte[] temp = null;
        temp = ConversionHelp.intToByte(SPUtil.getInt("UUID", 0));
        System.arraycopy(temp, 0, sendComperMsgData, 0, temp.length);
        temp = ConversionHelp.charToByte(type);
        System.arraycopy(temp, 0, sendComperMsgData, 4, temp.length);
        temp = SPUtil.getString(Const.KEY_VMSUSERNAME, getSerialNumber()).getBytes();
        System.arraycopy(temp, 0, sendComperMsgData, 5, temp.length);
        try {
            temp = SPUtil.getString("name", "").getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.arraycopy(temp, 0, sendComperMsgData, 69, temp.length);

        boolean flag;
        if (userList.get(0).getRecord().getCompertResult().equals("失败")) {
            flag = false;
        } else {
            flag = true;
        }
        if (flag) {
            temp = ConversionHelp.charToByte((char) 0x01);
        } else {
            temp = ConversionHelp.charToByte((char) 0x02);
        }
        System.arraycopy(temp, 0, sendComperMsgData, 101, temp.length);

        // 识别类型 1:1 和 1:N  对比分数
        if(type == Const.TYPE_ONEVSMORE) {
            temp = ConversionHelp.intToByte((int) (Float.parseFloat(userList.get(0).getRecord().getScore()) * 100) * 1000000);
            System.arraycopy(temp, 0, sendComperMsgData, 102, temp.length);
        } else {
            temp = ConversionHelp.intToByte((int) (Float.parseFloat(userList.get(0).getRecord().getScore()) * 100) * 1000000);
            System.arraycopy(temp, 0, sendComperMsgData, 102, temp.length);
        }

        temp = ConversionHelp.getTimeByte(userList.get(0).getTime());
        System.arraycopy(temp, 0, sendComperMsgData, 106, temp.length);
        // 人员类型
        if (type == Const.TYPE_ONEVSMORE) {
            temp = ConversionHelp.shortToByte((short) Type.valueOf(userList.get(0).getType()).getCode());
            System.arraycopy(temp, 0, sendComperMsgData, 110, temp.length);
        }

        // 2个字节跳过 身份证号码
        if (type == Const.TYPE_ONEVSMORE) {
            temp = userList.get(0).getCardNo().getBytes();
            System.arraycopy(temp, 0, sendComperMsgData, 112, temp.length);
        } else {
            temp = userList.get(0).getCardNo().getBytes();
            System.arraycopy(temp, 0, sendComperMsgData, 112, temp.length);
        }

        // 人脸子模版名称
        try {
            if (type == Const.TYPE_ONEVSMORE) {
                temp = userList.get(0).getName().getBytes("GBK");
            } else {
                temp = userList.get(0).getName().getBytes("GBK");
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "name to gbk error");
        }

        // 人脸子模版性别
        System.arraycopy(temp, 0, sendComperMsgData, 144, temp.length);
        if (type == Const.TYPE_CARD) {
            if (userList.get(0).getSex().equals("男")) {
                temp = ConversionHelp.charToByte((char) 0x01);
            } else {
                temp = ConversionHelp.charToByte((char) 0x02);
            }
            System.arraycopy(temp, 0, sendComperMsgData, 208, temp.length);
        } else {
            if(userList.get(0).getSex().equals("男")) {
                temp = ConversionHelp.charToByte((char) 0x01);
            } else if(userList.get(0).getSex().equals("女")) {
                temp = ConversionHelp.charToByte((char) 0x02);
            } else {
                temp = ConversionHelp.charToByte((char) 0x00);
            }
            System.arraycopy(temp, 0, sendComperMsgData, 208, temp.length);
        }

        // 年龄
        if (type == Const.TYPE_CARD) {

        } else {
            temp = ConversionHelp.charToByte((char) userList.get(0).getAge());
            System.arraycopy(temp, 0, sendComperMsgData, 209, temp.length);
        }
        //其他字段json 这三个数据库没有存，暂时续传空null给VMS
        JSONObject obj = new JSONObject();
        try {
            obj.put("addr", "");
            obj.put("birthday", "");
            obj.put("nation", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String json = obj.toString();
        try {
            temp = json.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.arraycopy(temp, 0, sendComperMsgData, 210, temp.length);
        temp = "".getBytes();
        System.arraycopy(temp, 0, sendComperMsgData, 1234, temp.length);

        temp = ConversionHelp.intToByte(cardByte.length);
        System.arraycopy(temp, 0, sendComperMsgData, 1298, temp.length);
        //添加card图片
        System.arraycopy(cardByte, 0, sendComperMsgData, 1302, cardByte.length);

        temp = ConversionHelp.intToByte(faceByte.length);
        System.arraycopy(temp, 0, sendComperMsgData, 1302 + cardByte.length, temp.length);
        //添加face图片
        System.arraycopy(faceByte, 0, sendComperMsgData, 1302 + cardByte.length + 4, faceByte.length);

        socketThread.mSend(getSendData(Const.NMSG_FACE_CMPRESULT, sendComperMsgData));
    }

    /**
     * 发送有参数的请求
     * @param msgType
     * @param data
     * @return
     */
    public static byte[] getSendData(int msgType, byte[] data) {
        char a = 0x7E;
        byte[] b = new byte[19];
        byte[] temp = null;
        temp = ConversionHelp.charToByte(a);
        System.arraycopy(temp, 0, b, 0, temp.length);
        temp = ConversionHelp.intToByte(ConversionHelp.SeqNumber());
        System.arraycopy(temp, 0, b, 1, temp.length);
        temp = ConversionHelp.shortToByte((short) 0x0000);
        System.arraycopy(temp, 0, b, 5, temp.length);
        temp = ConversionHelp.shortToByte((short) 0xF101);
        System.arraycopy(temp, 0, b, 7, temp.length);

        temp = ConversionHelp.intToByte(msgType);
        System.arraycopy(temp, 0, b, 9, temp.length);

        temp = ConversionHelp.shortToByte((short) 0x0001);
        System.arraycopy(temp, 0, b, 13, temp.length);
        temp = ConversionHelp.intToByte(data.length);
        System.arraycopy(temp, 0, b, 15, temp.length);

        byte[] resultData = null;

        resultData = new byte[b.length + data.length + 3];
        System.arraycopy(b, 0, resultData, 0, b.length);

        System.arraycopy(data, 0, resultData, 19, data.length);

        temp = ConversionHelp.shortToByte((short) ConversionHelp.getCheck(resultData, resultData.length - 3));
        System.arraycopy(temp, 0, resultData, 19 + data.length, temp.length);

        temp = ConversionHelp.charToByte((char) 0x7F);
        System.arraycopy(temp, 0, resultData, resultData.length - 1, temp.length);

        return resultData;
    }

    /**
     * 获取SN序列号
     *
     * @return
     */
    private static String getSerialNumber() {
        String serial = "";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
        return serial;
    }
}
