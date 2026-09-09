<template>
  <a-row id="globalheader" align="center" :wrap="false">
    <a-col flex="auto">
      <a-menu mode="horizontal" @menu-item-click="doMenuClick" :selected-keys="selectedKeys">
        <a-menu-item key="0" :style="{ padding: 0, marginRight: '38px' }" disabled>
          <div class="title-bar">
            <img class="logo" src="../assets/logo.png" alt="logo" />
            <div class="title">答趣通</div>
          </div>
        </a-menu-item>
        <a-menu-item v-for="item in visibleRouter" :key="item.path">
          {{ item.name }}
        </a-menu-item>
      </a-menu>
    </a-col>
    <a-col flex="100px">
       <div v-if="loginUserStore.loginUser.id">
    <a-dropdown @select="handleUserMenuSelect">
      <a-space style="cursor: pointer;">
        <a-avatar :size="32" :image-url="loginUserStore.loginUser.userAvatar">
          <img v-if="loginUserStore.loginUser.userAvatar" :src="loginUserStore.loginUser.userAvatar" alt="头像" />
          <span v-else>{{ loginUserStore.loginUser.userName?.[0] || 'U' }}</span>
        </a-avatar>
        <span>{{ loginUserStore.loginUser.userName || '无名' }}</span>
      </a-space>
      <template #content>
        <a-doption value="profile">个人中心</a-doption>
        <a-doption value="logout">退出登录</a-doption>
      </template>
    </a-dropdown>
  </div>
      <div v-else>
        <a-button type="primary" href="/user/login">登录</a-button>
      </div>
    </a-col>
  </a-row>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { routes } from '../router/routes';
import { useRouter } from 'vue-router';
import { useLoginUserStore } from "../stores/userStore";
import { computed } from 'vue';
import checkAccess from '@/access/checkAccess';
import { userLogoutUsingPost } from '@/api/userController';
import ACCESS_ENUM from '@/access/accessEnum';
import { Message } from '@arco-design/web-vue';

const loginUserStore=useLoginUserStore();
const router = useRouter();
const visibleRouter = computed(()=>{
  return routes.filter((item) =>{
  // 显示在菜单中的路由数组
  if(item.meta?.hideInMenu){
    return false;
  }
  // 根据权限过滤菜单
  if(!checkAccess(loginUserStore.loginUser,item.meta?.access as string)){
    return false;
  }
  return true;
});
})
const selectedKeys = ref(['/']);
router.afterEach((to) => {
  selectedKeys.value = [to.path];
});
const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};

/**
 * 用户个人信息页面
 */
const handleUserMenuSelect=async (value: string | number | Record<string, unknown> | undefined)=>{
  if(value==='profile'){
    router.push('/user/profile');
  }else if(value==='logout'){
    await userLogoutUsingPost();
    loginUserStore.setLoginUser({userRole:ACCESS_ENUM.NOT_LOGIN});
    router.push('/');
    Message.success("已退出登录");
  }
}
</script>
<style scoped>
#globalheader {
  margin-bottom: 16px;
  box-shadow: #eee 1px 1px 5px;
}
.title-bar {
  display: flex;
  align-items: center;
}
.title {
  color: black;
  margin-left: 16px;
}
.logo {
  height: 48px;
}
</style>
