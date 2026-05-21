<template>
  <div id="appdetailpage">
    <a-card>
      <a-row class="grid-demo" style="margin-bottom: 16px">
        <a-col flex="auto" class="contentWrapper">
          <h2>{{ data.appName }}</h2>
          <p>{{ data.appDesc }}</p>
          <p>应用类型：{{ AppTypeMap[data.appType as number] }}</p>
          <p>评分策略：{{ AppScoringStrategyMap[data.scoringStrategy as number] }}</p>
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
          <p>
            创建时间：
            {{ dayjs(data.createTime).format('YYYY-MM-DD HH:mm:ss') }}
          </p>
          <a-space size="large">
            <a-button type="primary">开始答题</a-button>
            <a-button>分享应用</a-button>
            <a-button v-if="isMy" :href="`/add/question/${props.id}`">设置题目</a-button>
            <a-button v-if="isMy" :href="`/add/scoring_result/${props.id}`">设置评分</a-button>
            <a-button v-if="isMy" @click="editApp">修改应用</a-button>
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
import { useLoginUserStore } from '@/stores/userStore';
import { dayjs } from '@arco-design/web-vue/es/_utils/date';
import message from '@arco-design/web-vue/es/message';
import { computed, ref, watchEffect } from 'vue';
import { APP_TYPE_MAP, APP_SCORING_STRATEGY_MAP } from '@/constant/app';
import { useRouter } from 'vue-router';
type NumberKeyMap={[key:number]:string};
const AppTypeMap:NumberKeyMap=APP_TYPE_MAP;
const AppScoringStrategyMap:NumberKeyMap=APP_SCORING_STRATEGY_MAP;

interface Props {
  id: number;
}
const props = withDefaults(defineProps<Props>(), {
  id: () => {
    return 0;
  },
});

const router=useRouter();
const editApp=()=>{
  router.push(`/add/app/${props.id}`);
}

const data = ref<API.AppVO>({});
// 获取当前登录用户
const loginUserStore = useLoginUserStore();
const loginUserId=loginUserStore.loginUser?.id;
// 验证是否为本人创建
const isMy=computed(()=>{
  return loginUserId&&loginUserId===data.value.userId;
})

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
.contentWrapper > * {
  margin-bottom: 24px;
}
</style>
