import { type RouteRecordRaw } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import ACCESS_ENUM from '../access/accessEnum';
export const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: '主页',
    component: HomeView,
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('../views/AboutView.vue'),
  },
  {
    path: '/app/detail/:id',
    name: '应用详情',
    component: () => import('@/views/app/AppDetailView.vue'),
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: '/add/app/:id?',
    name:'创建应用',
    component:()=>import('@/views/add/AddAppView.vue'),
    props:true,
  },
  {
    path:'/add/question/:appId',
    name:'创建题目',
    component:()=>import('@/views/add/AddQuestionView.vue'),
    props:true,
    meta:{
      hideInMenu:true,
    }
  },
  {
    path:'/add/scoring_result/:appId',
    name:'创建评分',
    component:()=>import('@/views/add/AddScoringResultView.vue'),
    props:true,
    meta:{
      hideInMenu:true,
    }
  },
  {
    path: '/admin',
    name: '管理员页面',
    component: () => import('../views/admin/AdminLayoutl.vue'),
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
    children: [
      {
        path: '',
        redirect: '/admin/app',
      },
      {
        path: '/admin/user',
        name: '用户管理',
        component: () => import('../views/admin/AdminUserView.vue'),
      },
      {
        path: '/admin/app',
        name: '应用管理',
        component: () => import('../views/admin/AdminAppview.vue'),
      },
      {
        path: '/admin/question',
        name: '题目管理',
        component: () => import('../views/admin/AdminQuestionView.vue'),
      },
      {
        path: '/admin/scoring_result',
        name: '评分管理',
        component: () => import('../views/admin/AdminScoringResultView.vue'),
      },
      {
        path: '/admin/user_answer',
        name: '回答管理',
        component: () => import('../views/admin/AdminUserAnswerView.vue'),
      },
    ],
  },
  {
    path: '/noAuth',
    name: '未授权',
    component: () => import('../views/NoAuth.vue'),
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: '/user',
    name: '用户',
    meta: {
      hideInMenu: true,
    },
    component: () => import('../views/user/UserLayout.vue'),
    children: [
      {
        path: '/user/login',
        name: '用户登录',
        component: () => import('../views/user/UserLoginView.vue'),
      },
      {
        path: '/user/register',
        name: '用户注册',
        component: () => import('../views/user/UserRegisterView.vue'),
      },
    ],
  },
];
