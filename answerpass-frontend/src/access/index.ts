import router from "@/router";
import { useLoginUserStore } from "@/stores/userStore";
import ACCESS_ENUM from "./accessEnum";
import checkAccess from "./checkAccess";

router.beforeEach(async(to,from,next)=>{
    // 获取当前登录用户
    const loginUserStore=useLoginUserStore();
    let loginUser=loginUserStore.loginUser;

    // 如果之前没有尝试过获取登录用户信息，才自动登录
    if(!loginUser||!loginUser.userRole){
        await loginUserStore.fetchLoginUser();
        loginUser=loginUserStore.loginUser;
    }

    console.log("登录用户信息",loginUser);
    const needAcess=(to.meta?.access as string)??ACCESS_ENUM.NOT_LOGIN;
    // 要跳转的页面必须要登录
    if(needAcess!==ACCESS_ENUM.NOT_LOGIN){
        // 如果没登录则跳转到登录页面
        if(!loginUser||!loginUser.userRole||loginUser.userRole===ACCESS_ENUM.NOT_LOGIN){
            next({path:`/user/login`,query:{redirect:to.fullPath},replace:true});
            return;
        }
        // 如果已经登录了，但是权限不足，则跳转到无权限页面
        if(!checkAccess(loginUser,needAcess)){
            next("/noAuth");
            return;
        }
    }
    next();
});