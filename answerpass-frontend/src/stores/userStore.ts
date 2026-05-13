import { getLoginUserUsingGet } from '@/api/userController';
import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 登录用户信息全局状态
 */
export const useLoginUserStore = defineStore('loginUser', () => {
    const loginUser = ref<API.LoginUserVO>({
        userName:"未登录",
    });
    
    async function fetchLoginUser(){
        const res=await getLoginUserUsingGet();
        if(res.data.code===0&&res.data.data){
            loginUser.value=res.data.data;
        }
    }

    function setLoginUser(newLoginUser:API.LoginUserVO){
        loginUser.value=newLoginUser;
    }

  return { loginUser, setLoginUser, fetchLoginUser }
})
