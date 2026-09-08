<template>
  <div id="addquestionpage">
    <h2 style="margin-bottom: 32px">设置题目</h2>
    <a-form
      style="max-width: 480px"
      label-align="left"
      auto-label-width
      :model="questionContent"
      @submit="handleSubmit"
    >
      <a-form-item label="应用id">
        {{ appId }}
      </a-form-item>
      <a-form-item label="题目列表" :content-flex="false" :merge-props="false">
        <a-space size="medium">
          <a-button size="small" @click="addQuestion(questionContent.length)">
            底部添加题目
          </a-button>
          <!-- AI生成抽屉 -->
          <AiGenerateQuestionDrawer :appId="appId" :onSuccess="onAiGenerateSuccess" :onQuestionGenerated="onQuestionGenerated" />
        </a-space>
        <!-- 遍历每道题目 -->
        <div v-for="(question, index) in questionContent" :key="index">
          <a-space size="large">
            <h3>题目{{ index + 1 }}</h3>
            <a-button size="small" @click="addQuestion(index + 1)">添加题目</a-button>
            <a-button size="small" status="danger" @click="deleteQuestion(index)"
              >删除题目</a-button
            >
          </a-space>
          <a-form-item field="posts.post1" :label="`题目${index + 1}标题`">
            <a-input v-model="question.title" placeholder="请输入标题" />
          </a-form-item>
          <!-- 题目选项 -->
          <a-space>
            <h4>题目{{ index + 1 }}选项列表</h4>
            <a-button size="small" @click="addQuestionOption(question, questionContent.length)">
              底部添加选项
            </a-button>
          </a-space>
          <a-form-item
            v-for="(option, optionIndex) in question.options"
            :key="optionIndex"
            :label="`选项${optionIndex + 1}`"
            :content-flex="false"
            :merge-props="false"
          >
            <a-form-item field="posts.post1" label="选项key">
              <a-input v-model="option.key" placeholder="请输入选项key" />
            </a-form-item>
            <a-form-item field="posts.post1" label="选项值">
              <a-input v-model="option.value" placeholder="请输入选项值" />
            </a-form-item>
            <a-form-item field="posts.post1" label="选项结果">
              <a-input v-model="option.result" placeholder="请输入选项结果" />
            </a-form-item>
            <a-form-item field="posts.post1" label="选项得分">
              <a-input-number v-model="option.score" placeholder="请输入选项得分" />
            </a-form-item>
            <a-space size="large">
              <a-button size="mini" @click="addQuestionOption(question, optionIndex + 1)"
                >添加选项</a-button
              >
              <a-button
                size="mini"
                status="danger"
                @click="deleteQuestionOption(question, optionIndex)"
                >删除选项</a-button
              >
            </a-space>
          </a-form-item>
          <!-- 题目选项结尾 -->
        </div>
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
import {
  addQuestionUsingPost,
  editQuestionUsingPost,
  listQuestionVoByPageUsingPost,
} from '@/api/questionController';
import AiGenerateQuestionDrawer from './components/AiGenerateQuestionDrawer.vue';

interface Props {
  appId: string;
}
const props = withDefaults(defineProps<Props>(), {
  appId: () => {
    return '';
  },
});
// 题目结构内容，理解为题目列表
const questionContent = ref<API.QuestionContentDTO[]>([]);
// sessionStorage 会话存储
// 草稿持久化--可以在用户离开页面后，保留用户的编辑内容
// 每个应用拥有独立的草稿 key
const getDraftKey=()=>{
  return `questionEditDraft_${props.appId}`;
};
/**
 * 从 sessionStorage 恢复草稿
 */
const restoreDraft=():API.QuestionContentDTO[]|null=>{
  try{
    const saved=sessionStorage.getItem(getDraftKey());
    if(saved){
      return JSON.parse(saved);
    }
  }catch(e){
    console.error('恢复草稿失败',e);
  }
  return null;
}
/**
 * 保存草稿到 sessionStorage
 */
const saveDraft=()=>{
  try{
    sessionStorage.setItem(getDraftKey(),JSON.stringify(questionContent.value));
  }catch(e){
    console.error("保存草稿失败",e);
  }
}
/**
 * 清除草稿
 */
const clearDraft=()=>{
  try{
    sessionStorage.removeItem(getDraftKey());
  }catch(e){
    console.error("清除草稿失败",e);
  }
}
/**
 * 添加题目
 * @param index
 */
const addQuestion = (index: number) => {
  questionContent.value.splice(index, 0, {
    title: '',
    options: [],
  });
  saveDraft();
};
/**
 * 删除题目
 * @param index
 */
const deleteQuestion = (index: number) => {
  questionContent.value.splice(index, 1);
  saveDraft();
};
/**
 * 添加题目选项
 * @param index
 */
const addQuestionOption = (question: API.QuestionContentDTO, index: number) => {
  question.options?.splice(index, 0, {
    key: '',
    value: '',
    result: '',
    score: 0,
  });
  saveDraft();
};
/**
 * 删除题目选项
 * @param index
 */
const deleteQuestionOption = (question: API.QuestionContentDTO, index: number) => {
  if (!question.options) {
    question.options = [];
  }
  question.options.splice(index, 1);
  saveDraft();
};
const router = useRouter();
const oldQuestion = ref<API.QuestionVO>();
/**
 * 加载数据
 */
const loadData = async () => {
  if (!props.appId) {
    return;
  }
  // 先尝试恢复草稿
  const draft=restoreDraft();
  if(draft){
    questionContent.value=draft;
    return;
  }
  // 没有草稿，正常从后端加载
  const res = await listQuestionVoByPageUsingPost({
    appId: props.appId,
    current: 1,
    pageSize: 1,
    sortField: 'createTime',
    sortOrder: 'descend',
  });
  if (res.data.code === 0 && res.data.data?.records) {
    oldQuestion.value = res.data.data?.records[0];
    if (oldQuestion.value) {
      questionContent.value = oldQuestion.value.questionContent ?? [];
    }
  } else {
    message.error('获取数据失败，' + res.data.message);
  }
};
watchEffect(() => {
  loadData();
});
/**
 * AI 生成题目成功后的回调函数
 * @param result 生成的题目内容
 */
const onAiGenerateSuccess = (result: API.QuestionContentDTO[]) => {
  questionContent.value = [...questionContent.value, ...result];
  message.success(`AI 生成题目成功，已新增 ${result.length} 道题目`);
};
const onQuestionGenerated=(question:API.QuestionContentDTO)=>{
  questionContent.value.push(question);
  saveDraft();
}
/**
 * 提交表单
 * @param data
 */
const handleSubmit = async () => {
  if (!props.appId || !questionContent.value) {
    return;
  }
  let res;
  // 如果是修改
  if (oldQuestion.value?.id) {
    res = await editQuestionUsingPost({
      id: oldQuestion.value?.id,
      questionContent: questionContent.value,
    });
  } else {
    // 创建
    res = await addQuestionUsingPost({
      appId: props.appId,
      questionContent: questionContent.value,
    });
  }
  if (res.data.code === 0) {
    // 提交成功后清除草稿
    clearDraft();
    message.success('操作成功，即将跳转到应用详情页');
    setTimeout(() => {
      const appId = props.appId ?? res.data.data;
      router.push(`/app/detail/${appId}`);
    }, 2000);
  } else {
    message.error('操作失败，' + res.data.message);
  }
};
</script>
