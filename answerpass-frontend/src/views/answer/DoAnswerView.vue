<template>
  <div id="doAnswerPage">
    <a-card>
      <h1>{{ app.appName }}</h1>
      <p>{{ app.appDesc }}</p>
      <h2 style="margin-bottom: 16px">
        {{ current }}、{{ currentQuestion?.title?.replace(/^\d+\.\s*/, "") }}
      </h2>
      <div>
        <a-radio-group
          direction="vertical"
          v-model="currentAnswer"
          :options="questionOptions"
          @change="doRadioChange"
        />
      </div>
      <div style="margin-top: 24px">
        <a-space size="large">
          <a-button
            type="primary"
            circle
            v-if="current < questionContent.length"
            :disabled="!currentAnswer"
            @click="current += 1"
          >
            下一题
          </a-button>
          <a-button
            type="primary"
            v-if="current === questionContent.length"
            :loading="submitting"
            circle
            :disabled="!currentAnswer"
            @click="doSubmit"
          >
            {{ submitting ? "评分中" : "查看结果" }}
          </a-button>
          <a-button v-if="current > 1" circle @click="current -= 1">
            上一题
          </a-button>
        </a-space>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import {
  computed,
  reactive,
  ref,
  watchEffect,
} from "vue";
import { useRouter } from "vue-router";
import { listQuestionVoByPageUsingPost } from "@/api/questionController";
import message from "@arco-design/web-vue/es/message";
import { getAppVoByIdUsingGet } from "@/api/appController";
import {
  addUserAnswerUsingPost,
  generateUserAnswerIdUsingGet,
} from "@/api/userAnswerController";

interface Props {
  appId: number;
}

const props = withDefaults(defineProps<Props>(), {
  appId: () => {
    return 0;
  },
});

const router = useRouter();

const app = ref<API.AppVO>({});
// 题目内容结构（理解为题目列表）
const questionContent = ref<API.QuestionContentDTO[]>([]);

// 当前题目的序号（从 1 开始）
const current = ref(1);
// 当前题目
const currentQuestion = ref<API.QuestionContentDTO>({});
// 当前题目选项
const questionOptions = computed(() => {
  return currentQuestion.value?.options
    ? currentQuestion.value.options.map((option) => {
        return {
          label: `${option.key}. ${option.value}`,
          value: option.key??"",
        };
      })
    : [];
});
// 当前答案
const currentAnswer = ref<string>();
// 回答列表
const answerList = reactive<string[]>([]);
// 是否正在提交结果
const submitting = ref(false);

// 唯一 id
const id = ref<number>();

// 生成唯一 id
const generateId = async () => {
  const res = await generateUserAnswerIdUsingGet();
  if (res.data.code === 0) {
    id.value = res.data.data;
  } else {
    message.error("获取唯一 id 失败，" + res.data.message);
  }
};

// 进入页面时，生成唯一 id
watchEffect(() => {
  generateId();
});

/**
 * 加载数据
 */
const loadData = async () => {
  if (!props.appId) {
    return;
  }
  // 获取 app
  const res = await getAppVoByIdUsingGet({
    id: props.appId,
  });
  if (res.data.code === 0&&res.data.data) {
    app.value = res.data.data;
  } else {
    message.error("获取应用失败，" + res.data.message);
  }
  // 获取题目
  const questionRes = await listQuestionVoByPageUsingPost({
    appId: props.appId,
    current: 1,
    pageSize: 1,
    sortField: "createTime",
    sortOrder: "descend",
  });
  if (questionRes.data.code === 0 && questionRes.data.data?.records) {
    questionContent.value = questionRes.data.data.records[0].questionContent??[];
  } else {
    message.error("获取题目失败，" + questionRes.data.message);
  }
;}

// 获取旧数据
watchEffect(() => {
  loadData();
});

// 改变 current 题号后，会自动更新当前题目和答案
watchEffect(() => {
  currentQuestion.value = questionContent.value[current.value - 1];
  currentAnswer.value = answerList[current.value - 1];
});

/**
 * 选中选项后，保存选项记录
 * @param value
 */
const doRadioChange = (value: string|number|boolean) => {
  answerList[current.value - 1] = value as string;
};

/**
 * 提交
 */
const doSubmit = async () => {
  if (!props.appId || !answerList) {
    return;
  }
  submitting.value = true;
  try{
    const res = await addUserAnswerUsingPost({
    appId: props.appId,
    choices: answerList,
    id: id.value,
  });
  if (res.data.code === 0 && res.data.data) {
    router.push(`/answer/result/${res.data.data}`);
  } else {
    message.error("提交答案失败，" + res.data.message);
  }
  }catch(error){
    if(error instanceof Error&&error.message?.includes('fetch')){
      message.error("网络连接失败，请检查网络后重试");
    }else{
      message.error("提交失败，请稍后重试");
    }
  }finally{
    submitting.value=false;
  }
};
</script>
