<template>
  <div id="appdetailpage">
    <a-card>
      <a-row class="grid-demo" style="margin-bottom: 16px">
        <a-col flex="auto" class="contentWrapper">
          <h2>{{ data.appName }}</h2>
          <p>{{ data.appDesc }}</p>
          <p>应用类型：{{ data.appDesc }}</p>
          <p>评分策略：{{ data.appDesc }}</p>
          <a-space>
            作者：
            <div :style="{ display: 'flex', alignItems: 'center', color: '#1D2129' }">
              <a-avatar
                :size="24"
                :image-url="data.user?.userAvatar"
                :style="{ marginRight: '8px' }"
              />
              <a-typography-text>{{ data.user?.userName ?? '无名' }}</a-typography-text>
            </div>
          </a-space>
          <p>创建时间：
            {{ dayjs(data.createTime).format("YYYY-MM-DD HH:mm:ss") }}
          </p>
          <a-space size="large">
            <a-button type="primary">开始答题</a-button>
            <a-button>分享应用</a-button>
          </a-space>
        </a-col>
        <a-col flex="320px">
          <a-image width="100%" :src="data.appIcon" />
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { getAppVoByIdUsingGet } from '@/api/appController';
import { dayjs } from '@arco-design/web-vue/es/_utils/date';
import message from '@arco-design/web-vue/es/message';
import { ref, watchEffect } from 'vue';

interface Props {
  id: number;
}
const props = withDefaults(defineProps<Props>(), {
  id: () => {
    return 0;
  },
});

const data = ref<API.AppVO>({});

/**
 * 加载数据
 */
const loadData = async () => {
  const res = await getAppVoByIdUsingGet({
    id: props.id,
  });
  if (res.data.code === 0 && res.data.data) {
    data.value = res.data.data;
  } else {
    message.error('获取数据失败，' + res.data.message);
  }
};

/**
 * 监听 searchParams 变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  loadData();
});
</script>

<style scoped>
#appdetailpage {
}
.contentWrapper >*{
  margin-bottom: 24px;
}
</style>
