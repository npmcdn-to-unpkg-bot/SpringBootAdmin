package com.fortran.admin.modules.sys.controller;

import com.fortran.admin.modules.core.common.BaseController;
import com.fortran.admin.modules.core.common.constant.Constants;
import com.fortran.admin.modules.core.config.mybatis.Page;
import com.fortran.admin.modules.core.exception.ServiceException;
import com.fortran.admin.modules.core.message.RespMsg;
import com.fortran.admin.modules.sys.domain.Role;
import com.fortran.admin.modules.sys.enumeration.DelStatus;
import com.fortran.admin.modules.sys.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * <p>角色控制器</p>
 * Created by lin on 16/8/9.
 */
@Controller
@Slf4j
public class RoleController extends BaseController {

    @Autowired
    private RoleService roleService;

    private final String ROLE_FORM = "modules/sys/roleForm";

    private final String ROLE_LIST = "modules/sys/roleList";


    /**
     * <p>查询角色列表</p>
     *
     * @param role
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = "/role/findAll")
    public String list(HttpServletRequest request, HttpServletResponse response, Model model, Role role) {
        Page<Role> page = roleService.findRoles(Page.getInstance(request, role));
        model.addAttribute("page", page);
        return ROLE_LIST;
    }


    /**
     * <p>新增或修改角色</p>
     * @param request
     * @param redirectAttributes
     * @param role
     * @return
     */
    @RequestMapping(value = "/role/save")
    public String save(HttpServletRequest request, RedirectAttributes redirectAttributes, Role role) {
        if (!beanValidator(redirectAttributes, role)) {
            rtnMessage(redirectAttributes, Constants.MSG_TYPE_DANGER, Constants.SERVER_CHECK_FAIL_MSG);
            return ROLE_LIST;
        }
        try {
            role.setCurrentUser(getCurrentUser(request));
            if (role.getRoleId()== null) {
                roleService.insert(role);
            } else {
                roleService.update(role);
            }
        } catch (ServiceException e) {
            log.error("save role error.", e);
            rtnMessage(redirectAttributes, Constants.MSG_TYPE_DANGER,Constants.FAIL_MSG);
        }
        rtnMessage(redirectAttributes, Constants.MSG_TYPE_SUCCESS,Constants.SUCCESS_MSG);
        return "redirect:/role/findAll";
    }


    /**
     * <p>删除</p>
     * @param model
     * @param id
     * @return
     */
    @RequestMapping(value = "/role/{action}/{id}")
    @ResponseBody
    public RespMsg<Role> delete(Model model,@PathVariable String action, @PathVariable String id) {

        Role role = new Role();
        int flag = 0;
        try {
            if (Constants.ACTION_START.equalsIgnoreCase(action)){
                role.setRoleId(Long.parseLong(id));
                role.setStatus(DelStatus.NOMAL.getValue());
                flag = roleService.updateStatus(role);
            }
            if (Constants.ACTION_STOP.equalsIgnoreCase(action)){
                role.setRoleId(Long.parseLong(id));
                flag = roleService.delete(role);
            }

        } catch (ServiceException e) {
           return error(role);
        }
        return flag > 0 ? ok(role):error(role);
    }


    /**
     * <p>角色名是否唯一</p>
     * @param roleName
     * @return
     */
    @RequestMapping(value = "/role/isExist")
    @ResponseBody
    public boolean isExist(String roleName){
        return roleService.isExist(roleName);
    }



}
