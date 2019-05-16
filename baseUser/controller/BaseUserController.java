package com.qhit.baseUser.controller;

import com.qhit.baseRole.pojo.BaseRole;
import com.qhit.baseUser.pojo.BaseUser;
import com.qhit.baseUser.service.IBaseUserService;
import com.qhit.baseUser.service.impl.BaseUserServiceImpl;
import com.qhit.baseUserRole.pojo.BaseUserRole;
import com.qhit.baseUserRole.service.IBaseUserRoleService;
import com.qhit.baseUserRole.service.impl.BaseUserRoleServiceImpl;
import com.qhit.utils.MD5;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Created by Lenovo on 2018/11/26.
 */
@Controller
@RequestMapping("/baseUser")
public class BaseUserController {
    private IBaseUserService iBaseUserService = new BaseUserServiceImpl();

    @RequestMapping("/list")
    public String list(HttpServletRequest request){
        List<BaseUser> list = iBaseUserService.findAll();
        request.setAttribute("list",list);
        return "baseUser/list";
    }

    @RequestMapping("/insert")
    public String insert(BaseUser baseUser){
        MD5 md5 = new MD5();
        baseUser.setPassword(md5.getMD5ofStr(baseUser.getPassword()));
        boolean insert = iBaseUserService.insert(baseUser);
        return "forward:list.action";
    }

    @RequestMapping("/select")
    public String select(BaseUser baseUser,HttpServletRequest request){
        List<BaseUser> list = iBaseUserService.select(baseUser.getCname(),baseUser.getSex());
        request.setAttribute("cname",baseUser.getCname());
        request.setAttribute("sex",baseUser.getSex());
        request.setAttribute("list",list);
        return "baseUser/list";
    }

    @RequestMapping("/delete")
    public String delete(BaseUser baseUser){
        boolean delete = iBaseUserService.delete(baseUser.getUserId());
        return "forward:list.action";
    }

    @RequestMapping("/load")
    public String load(BaseUser baseUser,Model model){
        BaseUser user = iBaseUserService.findById(baseUser.getUserId());
        model.addAttribute("baseUser",user);
        return "baseUser/edit";
    }

    @RequestMapping("/update")
    public String update(BaseUser baseUser){
        boolean update = iBaseUserService.update(baseUser);
        return "forward:list.action";
    }

    @RequestMapping("/login")
    public String login(BaseUser baseUser,HttpSession session,Model model){
      baseUser = iBaseUserService.login(baseUser);
      if (baseUser!=null){
          //登录成功把该用户信息放进session
          session.setAttribute("sessionUser",baseUser);
          return "index/home";
      }else{
        model.addAttribute("error","用户名或者密码不正确");
        return "baseUser/login";
      }
    }

    @RequestMapping("/logout")
    public String logout(HttpServletRequest request){
        //获取登录session
        HttpSession session = request.getSession();
        //清空
        session.removeAttribute("sessionUser");
        return "baseUser/login";
    }

    //判断输入的旧密码是否正确
    @RequestMapping("/oldPassword")
    public void oldPassword(BaseUser baseUser, HttpServletResponse response) throws IOException {
        boolean flag = iBaseUserService.findOldPassword(baseUser);
        //如果输入的旧密码是正确的则返回给ajax"Y"否则返回"N"
        response.getWriter().write(flag?"Y":"N");
    }

    @RequestMapping("/updatePassword")
    public void updatePassword(BaseUser baseUser,HttpServletResponse response) throws IOException {
       //加密并修改密码
        MD5 md5 = new MD5();
       baseUser.setPassword(md5.getMD5ofStr(baseUser.getPassword()));
        boolean flag = iBaseUserService.updateSelective(baseUser);
        response.getWriter().write(flag?"Y":"N");
    }

    @RequestMapping("/distributeLoad")
    public String distributeLoad(BaseUser baseUser,Model model){
        //查询角色表里，该用户未拥有的角色
       List<BaseRole> leftList  = iBaseUserService.distributeLeft(baseUser);
       //查询角色表里，该用户已拥有的角色
       List<BaseRole> rightList  = iBaseUserService.distributeRight(baseUser);
       //放进model里，用于页面显示
       model.addAttribute("leftList",leftList);
       model.addAttribute("rightList",rightList);
       model.addAttribute("userId",baseUser.getUserId());
        return "baseUser/distribute";
    }

    @RequestMapping("/distributeUpdate")
    public String distributeUpdate(BaseUser baseUser,HttpServletRequest request) {
        //获取所有的rid
        String[] rids =request.getParameterValues("rid");
        //角色表的service
        IBaseUserRoleService baseUserRoleService = new BaseUserRoleServiceImpl();
        //根据该用户id删除该用户  与base_user_role表相关的数据
        String sql ="delete from base_user_role where uid="+baseUser.getUserId();
        baseUserRoleService.freeUpdate(sql);
        //遍历存放所有rid的数组
        for (String rid:rids){
            BaseUserRole baseUserRole = new BaseUserRole();
            baseUserRole.setRid(Integer.parseInt(rid));
            baseUserRole.setUid(baseUser.getUserId());

            baseUserRoleService.insert(baseUserRole);
        }
        return "forward:list.action";
    }




}
