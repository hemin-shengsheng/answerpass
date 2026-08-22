<template>
  <a-button type="outline" @click="handleClick">AI 生成题目</a-button>
  <a-drawer :width="340" :visible="visible" @ok="handleOk" @cancel="handleCancel" unmountOnClose>
    <template #title> AI 生成题目</template>
    <div>
      <a-form :model="form" label-align="left" auto-label-width @submit="handleSubmit">
        <a-form-item label="应用 id">
          {{ appId }}
        </a-form-item>
        <a-form-item field="questionNumber" label="题目数量">
          <a-input-number
            v-model="form.questionNumber"
            :min="0"
            :max="20"
            placeholder="请输入题目数量"
          />
        </a-form-item>
        <a-form-item field="optionNumber" label="选项数量">
          <a-input-number
            v-model="form.optionNumber"
            :min="0"
            :max="6"
            placeholder="请输入选项数量"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button :loading="submitting" type="primary" html-type="submit" style="width: 120px">
              {{ submitting ? '生成中' : '一键生成' }}
            </a-button>
            <a-button :loading="submitting" style="width: 120px" @click="doSSESubmit">
              {{ submitting ? '生成中' : '实时生成' }}
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>
  </a-drawer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { aiGenerateQuestionUsingPost } from '@/api/questionController';
import message from '@arco-design/web-vue/es/message';

interface Props {
  appId: string;
  onSuccess?: (result: API.QuestionContentDTO[]) => void;
  onQuestionGenerated?: (question: API.QuestionContentDTO) => void; 
}

const props = withDefaults(defineProps<Props>(), {
  appId: () => {
    return '';
  },
});

const visible = ref(false);
const submitting = ref(false);

const form = reactive({
  questionNumber: 10,
  optionNumber: 2,
} as API.AiGenerateQuestionRequest);

const handleClick = () => {
  visible.value = true;
};
const handleOk = () => {
  visible.value = false;
};
const handleCancel = () => {
  visible.value = false;
};

/**
 * 同步生成
 */
const handleSubmit = async () => {
  if (!props.appId) {
    return;
  }
  submitting.value = true;
  const res = await aiGenerateQuestionUsingPost({
    appId: props.appId,
    ...form,
  });
  if (res.data.code === 0 && res.data.data && res.data.data.length > 0) {
    if (props.onSuccess) {
      props.onSuccess(res.data.data);
    } else {
      message.success('生成成功');
    }
    handleCancel();
  } else {
    message.error('操作失败，' + res.data.message);
  }
  submitting.value = false;
};
/**
 * 使用 SSE 实时生成题目
 */
const doSSESubmit = () => {
  if (!props.appId) return;
  submitting.value = true;

  const questions: API.QuestionContentDTO[] = [];

  const url = `http://localhost:8101/api/question/ai_generate/sse`
  +`?appId=${props.appId}&questionNumber=${form.questionNumber}&optionNumber=${form.optionNumber}`;
  const eventSource = new EventSource(url);

  eventSource.addEventListener('question', (event) => {
    console.log('收到 question 事件，原始数据:', event.data); 
    const question = JSON.parse(event.data);
    questions.push(question);
    if(props.onQuestionGenerated){
      props.onQuestionGenerated(question);
    }
    console.log('实时收到题目:', question);
  });

  eventSource.addEventListener('done', () => {
    eventSource.close();
    message.success(`生成 ${questions.length} 道题目`);
    handleCancel();
    submitting.value = false;
  });

  eventSource.addEventListener('error', () => {
    eventSource.close();
    message.error('生成失败');
    submitting.value = false;
  });
};
</script>
