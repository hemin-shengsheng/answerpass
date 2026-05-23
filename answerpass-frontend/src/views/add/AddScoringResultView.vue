<template>
  <div id="addscoringResultpage">
    <h2 style="margin-bottom: 32px">设置评分</h2>
    <a-form
      style="max-width: 480px"
      label-align="left"
      auto-label-width
      :model="form"
      @submit="handleSubmit"
    >
      <a-form-item label="应用id">
        {{ appId }}
      </a-form-item>
      <a-form-item v-show="updataId" label="修改评分id">
        {{ updataId }}
      </a-form-item>
      <a-form-item field="scoringResultName" label="结果名称">
        <a-input v-model="form.resultName" placeholder="请输入结果名称" />
      </a-form-item>
      <a-form-item field="scoringResultDesc" label="结果描述">
        <a-input v-model="form.resultDesc" placeholder="请输入结果描述" />
      </a-form-item>
      <a-form-item field="scoringResultPicture" label="结果图标">
        <a-input v-model="form.resultPicture" placeholder="请输入结果图片地址" />
      </a-form-item>
      <a-form-item field="resultProp" label="结果集">
        <a-input-tag
          v-model="form.resultProp"
          :style="{ width: '320px' }"
          placeholder="请输入结果集，按回车确认"
          allow-clear
        />
      </a-form-item>
      <a-form-item field="resultScoreRange" label="结果得分范围">
        <a-input-number v-model="form.resultScoreRange" placeholder="请输入结果得分范围" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 120px"> 提交 </a-button>
      </a-form-item>
    </a-form>
    <h2>评分管理</h2>
    <ScoringResultTable :appId :doUpdata="doUpdata" ref="tableRef" />
  </div>
  {{ form }}
</template>

<script setup lang="ts">
import { ref } from 'vue';
import message from '@arco-design/web-vue/es/message';
import {
  addScoringResultUsingPost,
  editScoringResultUsingPost,
} from '@/api/scoringResultController';
import ScoringResultTable from './components/ScoringResultTable.vue';

interface Props {
  appId: string;
}
const props = withDefaults(defineProps<Props>(), {
  appId: () => {
    return '';
  },
});

const tableRef = ref();
/**
 * 表单信息
 */
const form = ref({
  resultDesc: '',
  resultName: '',
  resultPicture: '',
} as API.ScoringResultAddRequest);

const updataId = ref<string>();
const doUpdata = (scoringResult: API.ScoringResultVO) => {
  updataId.value = scoringResult.id?.toString();
  form.value = scoringResult;
};

/**
 * 提交表单
 * @param data
 */
const handleSubmit = async () => {
  if (!props.appId) {
    return;
  }
  let res;
  // 如果是修改
  if (updataId.value) {
    res = await editScoringResultUsingPost({
      id: updataId.value as any,
      ...form.value,
    });
  } else {
    // 创建
    res = await addScoringResultUsingPost({
      appId: props.appId as any,
      ...form.value,
    });
  }
  if (res.data.code === 0) {
    message.success('操作成功');
    if (tableRef.value) {
      tableRef.value.loadData();
      updataId.value = undefined;
      form.value = {
        resultDesc: '',
        resultName: '',
        resultPicture: '',
      } as API.ScoringResultAddRequest;
    }
  } else {
    message.error('操作失败，' + res.data.message);
  }
};
</script>
