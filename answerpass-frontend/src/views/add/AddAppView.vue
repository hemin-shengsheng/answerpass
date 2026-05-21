<template>
  <div id="addapppage">
    <h2 style="margin-bottom: 32px">{{ props.id?'修改应用':'创建应用' }}</h2>
    <a-form
      style="max-width: 480px"
      label-align="left"
      auto-label-width
      :model="form"
      @submit="handleSubmit"
    >
      <a-form-item field="appName" label="应用名称">
        <a-input v-model="form.appName" placeholder="请输入应用名称" />
      </a-form-item>
      <a-form-item field="appDesc" label="应用描述">
        <a-input v-model="form.appDesc" placeholder="请输入应用描述" />
      </a-form-item>
      <a-form-item field="appIcon" label="应用图标">
        <a-input v-model="form.appIcon" placeholder="请输入应用图标" />
      </a-form-item>
      <!-- TODO 后端加上对象存储后，这里改成直接上传图片 -->
      <!-- <a-form-item field="appIcon" label="应用图标">
        <PictureUploader
          biz="app_icon"
          :value="form.appIcon"
          :onChange="(url) => (form.appIcon = url)"
        />
      </a-form-item> -->

      <a-form-item field="appType" label="应用类型">
        <a-select v-model="form.appType" :style="{ width: '320px' }" placeholder="请选择应用类型">
          <a-option
            v-for="(value, key) of APP_TYPE_MAP"
            :key="key"
            :value="Number(key)"
            :label="value"
          />
        </a-select>
      </a-form-item>
      <a-form-item field="scoringStrategy" label="评分策略">
        <a-select
          v-model="form.scoringStrategy"
          :style="{ width: '320px' }"
          placeholder="请选择评分策略"
        >
          <a-option
            v-for="(value, key) of APP_SCORING_STRATEGY_MAP"
            :key="key"
            :value="Number(key)"
            :label="value"
          />
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 120px"> 提交 </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref, watchEffect } from 'vue';
import message from '@arco-design/web-vue/es/message';
import { useRouter } from 'vue-router';
import { addAppUsingPost, editAppUsingPost, getAppVoByIdUsingGet } from '@/api/appController';
import { APP_SCORING_STRATEGY_MAP, APP_TYPE_MAP } from '@/constant/app';

interface Props {
  id: string;
}
const props = withDefaults(defineProps<Props>(), {
  id: () => {
    return '';
  },
});

/**
 * 表单信息
 */
const form = ref({
  appDesc: '',
  appIcon: '',
  appName: '',
  appType: 0,
  scoringStrategy: 0,
} as API.AppAddRequest);

const router = useRouter();
const oldApp = ref<API.AppVO>();
/**
 * 加载数据
 */
// const loadData = async () => {
//     if(!props.id){
//         return;
//     }
//   const res = await getAppVoByIdUsingGet({
//     id: Number(props.id),
//   });
//   if (res.data.code === 0 && res.data.data) {
//     oldApp.value = res.data.data;
//     form.value=res.data.data;
//   } else {
//     message.error('获取数据失败，' + res.data.message);
//   }
// };
const loadData = async () => {
    console.log('=== loadData 调试 ===');
    console.log('props.id 原始值:', props.id);
    console.log('Number(props.id):', Number(props.id));
    console.log('!!props.id:', !!props.id);
    
    if(!props.id){
        console.log('props.id 为空，跳过加载');
        return;
    }
    
    const res = await getAppVoByIdUsingGet({
      id: props.id,
    });
    
    console.log('接口返回:', res.data);
    
    if (res.data.code === 0 && res.data.data) {
      oldApp.value = res.data.data;
      form.value = res.data.data;
    } else {
      message.error('获取数据失败，' + res.data.message);
    }
};
watchEffect(()=>{
    loadData();
})

/**
 * 提交表单
 * @param data
 */
const handleSubmit = async () => {
    let res;
    if(props.id){
        res=await editAppUsingPost({
            id:props.id,
            ...form.value,
        })
    }else{
        res = await addAppUsingPost(form.value);
    }
  if (res.data.code === 0) {
    message.success('操作成功，即将跳转到应用详情页');
    setTimeout(() => {
        const appId=props.id??res.data.data;
      router.push(`/app/detail/${appId}`);
    }, 2000);
  } else {
    message.error('操作失败，' + res.data.message);
  }
};
</script>
