package com.runvision.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

@Entity
public class SocketRecord {

    @Id(autoincrement = true)
    private Long id;

    /**
     * 比对时间
     */
    @Property(nameInDb = "createTime")
    private String createTime;

    /**
     * 识别成功文件名
     */
    @Property(nameInDb = "snapImageID")
    private String snapImageID;

    /**
     * 人脸模板文件名
     */
    @Property(nameInDb = "templateImageID")
    private String templateImageID;

    /**
     * 上传是否成功
     * 成功1 失败0
     */
    @Property(nameInDb = "isSuccess")
    private String isSuccess;

    @Generated(hash = 1730564358)
    public SocketRecord(Long id, String createTime, String snapImageID,
            String templateImageID, String isSuccess) {
        this.id = id;
        this.createTime = createTime;
        this.snapImageID = snapImageID;
        this.templateImageID = templateImageID;
        this.isSuccess = isSuccess;
    }

    @Generated(hash = 471031797)
    public SocketRecord() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getSnapImageID() {
        return this.snapImageID;
    }

    public void setSnapImageID(String snapImageID) {
        this.snapImageID = snapImageID;
    }

    public String getTemplateImageID() {
        return this.templateImageID;
    }

    public void setTemplateImageID(String templateImageID) {
        this.templateImageID = templateImageID;
    }

    public String getIsSuccess() {
        return this.isSuccess;
    }

    public void setIsSuccess(String isSuccess) {
        this.isSuccess = isSuccess;
    }
    
}
