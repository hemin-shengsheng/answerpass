<template>
  <div id="adminLayout">
    <a-layout style="min-height: 100vh">
      <!-- 左侧侧边栏 -->
      <a-layout-sider v-model:collapsed="collapsed" collapsible>
        <div class="logo-container">
          <span v-if="!collapsed" class="title">管理后台</span>
        </div>
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="inline"
          @menu-item-click="handleMenuClick"
        >
          <a-menu-item v-for="item in adminRoutes" :key="item.path">
            <span>{{ item.name }}</span>
          </a-menu-item>
        </a-menu>
      </a-layout-sider>

      <!-- 右侧内容区 -->
      <a-layout>
        <a-layout-content class="content">
          <router-view />
        </a-layout-content>
      </a-layout>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const selectedKeys = ref([route.path]);
const collapsed = ref(false);

// 从当前路由的父路由中获取子路由作为菜单，并拼接完整路径
const adminRoutes = computed(() => {
  const adminRoute = router.options.routes.find((r) => r.path === '/admin');
  return adminRoute?.children
    ?.filter((child) => child.path !== '')
    .map((child) => ({
      ...child,
      path: `/admin/${child.path}`.replace(/\/+/g, '/'), // 拼接完整路径
    })) || [];
});

// 监听路由变化
watch(
  () => route.path,
  (newPath) => {
    selectedKeys.value = [newPath];
  },
);
const handleMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};
</script>

<style scoped>
#adminLayout {
  min-height: 100vh;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: #758dfa;
}

.logo {
  width: 32px;
  height: 32px;
  margin-right: 8px;
}

.title {
  color: white;
  font-size: 16px;
  font-weight: bold;
}

.content {
  margin: 24px 16px;
  padding: 24px;
  background: #fff;
  border-radius: 4px;
  min-height: calc(100vh - 48px);
}
</style>
