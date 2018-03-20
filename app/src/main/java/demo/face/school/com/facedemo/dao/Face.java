package demo.face.school.com.facedemo.dao;

import com.arcsoft.facerecognition.AFR_FSDKFace;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.converter.PropertyConverter;


/**
 * 本地face的数据模型
 *
 * @author liuyanhong
 * @Description: (用一句话描述该类作用)
 * @CreateDate: 2018/03/10 11:12
 * @email 296488320@qq.com
 * @UpdateUser:
 * @UpdateDate:
 * @UpdateRemark:
 */
@Entity
public class Face {

    @Id
    private Long id;

    private String username;

    @Property
    private AFR_FSDKFace faceregist;

    @Generated(hash = 1987236073)
    public Face(Long id, String username, AFR_FSDKFace faceregist) {
        this.id = id;
        this.username = username;
        this.faceregist = faceregist;
    }

    @Generated(hash = 601504354)
    public Face() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public AFR_FSDKFace getFaceregist() {
        return this.faceregist;
    }

    public void setFaceregist(AFR_FSDKFace faceregist) {
        this.faceregist = faceregist;
    }


}
