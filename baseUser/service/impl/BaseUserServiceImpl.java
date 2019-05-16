package com.qhit.baseUser.service.impl;

import com.qhit.baseRole.pojo.BaseRole;
import com.qhit.baseUser.service.IBaseUserService;
import java.util.List;
import com.qhit.baseUser.dao.IBaseUserDao;
import com.qhit.baseUser.dao.impl.BaseUserDaoImpl;
import com.qhit.baseUser.pojo.BaseUser;
import com.qhit.baseUserRole.pojo.BaseUserRole;
import com.qhit.baseUserRole.service.IBaseUserRoleService;
import com.qhit.baseUserRole.service.impl.BaseUserRoleServiceImpl;
import com.qhit.utils.MD5;

/**
* Created by GeneratorCode on 2018/11/26
*/

public class BaseUserServiceImpl  implements IBaseUserService {

    IBaseUserDao dao = new BaseUserDaoImpl();

    @Override 
    public boolean insert(Object object) { 
        return dao.insert(object); 
    } 


    @Override 
    public boolean update(Object object) { 
        return dao.update(object); 
    } 


    @Override 
    public boolean updateSelective(Object object) { 
        return dao.updateSelective(object); 
    } 


    @Override 
    public boolean delete(Object id) { 
        BaseUser baseUser = findById(id); 
        return dao.delete(baseUser); 
    } 


    @Override 
    public List findAll() {
        return dao.findAll();
    } 


    @Override 
    public BaseUser findById(Object id) { 
        List<BaseUser> list = dao.findById(id); 
        return  list.get(0); 
    }

    @Override
    public List<BaseUser> select(String name, String sex) {
        String sql ="select * from base_user where cname like '%"+name+"%' and sex like '%"+sex+"%'";
        List<BaseUser> list = dao.freeFind(sql);
        return list;
    }

    @Override
    public BaseUser login(BaseUser baseUser) {
        MD5 md5 = new MD5();
        String password = md5.getMD5ofStr(baseUser.getPassword());
        String sql = "SELECT * from base_user bu LEFT JOIN base_user_role bur ON bu.user_id=bur.uid\n" +
                "\t\t\t   LEFT JOIN base_role br ON bur.rid=br.rid\n" +
                "\t\t\t   LEFT JOIN base_role_function brf ON br.rid = brf.rid\n" +
                "\t\t\t   LEFT JOIN base_function bf ON brf.fid=bf.fid";
        sql += " where bu.user_name ='"+baseUser.getUserName()+"' and bu.password='"+password+"'";
        List<BaseUser> list = dao.freeFind(sql);
        if(list!=null && list.size()>0){
            return list.get(0);
        }
        return null;
    }

    @Override
    public boolean findOldPassword(BaseUser baseUser) {
        //给密码加密
        MD5 md5 = new MD5();
        String password = md5.getMD5ofStr(baseUser.getPassword());
        //用登录时的id和加密后的密码从数据库里查询旧密码 看看编码是否一样 如果找得到说明旧密码文本框内容正确
        String sql ="select * from base_user where user_id='"+baseUser.getUserId()+"' and password='"+password+"'";
        List list = dao.freeFind(sql);
        if (list!=null && list.size()>0){
            return true;
        }
        return false;
    }

    @Override
    public List<BaseRole> distributeLeft(BaseUser baseUser) {
        String sql ="SELECT * from base_role WHERE  rid NOT IN(\n" +
                "SELECT bur.`rid` from base_user_role bur JOIN base_user bu ON bur.`uid` = bu.`user_id` \n" +
                "AND user_id ='"+baseUser.getUserId()+"')";
        return dao.freeFind(sql);
    }

    @Override
    public List<BaseRole> distributeRight(BaseUser baseUser) {
        String sql ="SELECT * from base_role WHERE  rid  IN(\n" +
                "SELECT bur.`rid` from base_user_role bur JOIN base_user bu ON bur.`uid` = bu.`user_id` \n" +
                "AND user_id ='"+baseUser.getUserId()+"')";
        return dao.freeFind(sql);
    }

}