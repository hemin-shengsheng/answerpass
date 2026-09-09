<template>
  <div id="UserProfilePage">
    <a-card title="个人中心" :bordered="false" style="max-width: 600px; margin: 0 auto;">
      <a-form :model="form" :style="{ width: '500px' }" @submit="handleSubmit">
        <a-form-item field="userName" label="用户名">
          <a-input v-model="form.userName" placeholder="请输入用户名" />
        </a-form-item>
        
        <a-form-item field="userAvatar" label="头像">
          <div class="avatar-upload">
            <a-avatar :size="64" :image-url="form.userAvatar">
              <img v-if="form.userAvatar" :src="form.userAvatar" alt="头像" />
              <span v-else>{{ form.userName?.[0] || 'U' }}</span>
            </a-avatar>
            <a-input 
              v-model="form.userAvatar" 
              placeholder="请输入头像URL" 
              style="margin-left: 16px; flex: 1"
            />
          </div>
        </a-form-item>

        <a-form-item field="userProfile" label="个人简介">
          <a-textarea 
            v-model="form.userProfile" 
            placeholder="请输入个人简介" 
            :max-length="200"
            show-word-limit
          />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading">
            保存修改
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useLoginUserStore } from '@/stores/userStore';
import { updateMyUserUsingPost } from '@/api/userController';
import message from '@arco-design/web-vue/es/message';

const loginUserStore = useLoginUserStore();
const form = ref<API.UserUpdateMyRequest>({});
const loading = ref(false);

// 初始化表单数据
onMounted(() => {
  form.value = {
    userName: loginUserStore.loginUser.userName,
    userAvatar: loginUserStore.loginUser.userAvatar,
    userProfile: loginUserStore.loginUser.userProfile,
  };
});

/**
 * 提交修改
 */
const handleSubmit = async () => {
  if (!form.value.userName) {
    message.warning('用户名不能为空');
    return;
  }

  loading.value = true;
  try {
    const res = await updateMyUserUsingPost({
      userName: form.value.userName,
      userAvatar: form.value.userAvatar,
      userProfile: form.value.userProfile,
    });

    if (res.data.code === 0) {
      message.success('修改成功');
      // 更新 Pinia 中的用户信息
      await loginUserStore.fetchLoginUser();
    } else {
      message.error('修改失败，' + res.data.message);
    }
  } catch (e) {
    console.error('网络错误，请稍后重试',e);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
#UserProfilePage {
  padding: 20px;
}

.avatar-upload {
  display: flex;
  align-items: center;
}

:deep(.arco-avatar) {
  flex-shrink: 0;
}
</style>