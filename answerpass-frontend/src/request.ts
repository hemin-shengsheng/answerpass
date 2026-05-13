import axios from "axios";

const myAxios = axios.create({
  baseURL: "http://localhost:8101",
  timeout: 10000,
  withCredentials: true,// 允许跨域请求携带凭证
});

// 添加请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    // 在请求发送之前执行某些操作
    return config;
  },
  function (error) {
    // 处理请求错误
    return Promise.reject(error);
  }
);

// 添加响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    // 状态码在 2xx 范围内的响应会触发此函数
    // 处理响应数据
    console.log("响应数据:", response);
    const { data } = response;
    // 未登录
    if(data.code===40100){
        //不是获取用户信息的接口，或者不是登录页面，则跳转到登录页面
        if(!response.request.responseURL.includes("/user/info")
            &&!window.location.pathname.includes("/user/login")){
            window.location.href = `/user/login?redirect=${window.location.href}`;
        }
    }
    return response;
  },
  function (error) {
    // 状态码不在 2xx 范围内的响应会触发此函数
    // 处理响应错误
    return Promise.reject(error);
  }
);

export default myAxios;