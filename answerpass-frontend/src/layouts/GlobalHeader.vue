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
      <div>
        <a-button type="primary" href="/user/login">登录</a-button>
      </div>
    </a-col>
  </a-row>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { routes } from '../router/routes';
import { useRouter } from 'vue-router';
const router = useRouter();
const visibleRouter = routes.filter((item) => item.meta?.hideInMenu !== true);
const selectedKeys = ref(['/']);
router.afterEach((to) => {
  selectedKeys.value = [to.path];
});
const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};
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
