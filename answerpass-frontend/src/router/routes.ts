import { type RouteRecordRaw } from "vue-router";
import HomeView from "../views/HomeView.vue";
import  ACCESS_ENUM  from "../access/accessEnum";
export const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "home",
    component: HomeView,
  },
  {
    path: "/about",
    name: "about",
    component: () =>import("../views/AboutView.vue"),
  },
  {
    path: "/hide",
    name:"隐藏页面",
    component: HomeView,
    meta:{
        hideInMenu:true,
    }
  },{
    path: "/admin",
    name: "管理员页面",
    component: HomeView,
    meta:{
        access:ACCESS_ENUM.ADMIN,
    },
    children:[
      {
      path:"/admin/user",
      name:"用户管理",
      component:()=>import("../views/admin/AdminUserView.vue")
    },
    ]
  },
  {
    path: "/noAuth",
    name: "未授权",
    component: () =>import("../views/NoAuth.vue"),
    meta:{
      hideInMenu:true,
    },
  },
  {
    path: "/user",
    name: "用户",
    meta:{
      hideInMenu:true
    },
    component: () =>import("../views/user/UserLayout.vue"),
    children:[
      {
        path:"/user/login",
        name:"用户登录",
        component: () =>import("../views/user/UserLoginView.vue"),
      },
      {
        path:"/user/register",
        name:"用户注册",
        component: () =>import("../views/user/UserRegisterView.vue"),
      }
    ]
  }
];
