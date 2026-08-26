<template>
  <div id="appStstisticPage">
    <h2>热门应用统计</h2>
    <VChart class="chart" :option="appAnswerCountOptions" style="height: 300px" />
    <h2>应用结果统计</h2>
    <div class="searchBar">
      <a-input-search
        :style="{ width: '320px' }"
        placeholder="输入appId"
        button-text="搜索"
        @search="(value) => loadAppAnswerResultCountData(value)"
        search-button
      />
    </div>
    <VChart class="chart" :option="appAnswerResultCountOptions" style="height: 300px" />
  </div>
</template>

<script setup lang="ts">
import VChart from 'vue-echarts';
import 'echarts';
import {
  getAppAnswerCountUsingGet,
  getAppAnswerResultCountUsingGet,
} from '@/api/appStatisticController';
import { computed, ref, watchEffect } from 'vue';
import { Message } from '@arco-design/web-vue';

const appAnswerCountList = ref<API.AppAnswerCountDTO[]>([]);
const appAnswerResultCountList = ref<API.AppAnswerResultCountDTO[]>([]);
/**
 * 加载应用数据
 */
const loadAppAnswerCountData = async () => {
  const res = await getAppAnswerCountUsingGet();
  if (res.data.code === 0) {
    appAnswerCountList.value = res.data.data || [];
  } else {
    Message.error('获取数据失败' + res.data.message);
  }
};
/**
 * 加载答题结果数据
 */
const loadAppAnswerResultCountData = async (appId: string | number) => {
  if (!appId) {
    return;
  }
  const res = await getAppAnswerResultCountUsingGet({
    appId: appId,
  });
  if (res.data.code === 0) {
    appAnswerResultCountList.value = res.data.data || [];
  } else {
    Message.error('获取数据失败' + res.data.message);
  }
};
/**
 * 监听变量，数据改变时触发重新加载
 */
watchEffect(() => {
  loadAppAnswerCountData();
  loadAppAnswerResultCountData('');
});
/**
 * 应用数据图表
 */
const appAnswerCountOptions = computed(() => {
  return {
    xAxis: {
      type: 'category',
      data: appAnswerCountList.value.map((item) => item.appId),
      name: '应用ID',
    },
    yAxis: {
      type: 'value',
      name: '做题用户数',
    },
    series: [
      {
        data: appAnswerCountList.value.map((item) => item.answerCount),
        type: 'bar',
      },
    ],
  };
});
/**
 * 答题结果图表
 */
const appAnswerResultCountOptions = computed(() => {
  return {
    tooltip: {
      trigger: 'item',
    },
    legend: {
      top: '5%',
      left: 'center',
    },
    series: [
      {
        name: '答题结果统计',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: false,
          position: 'center',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 40,
            fontWeight: 'bold',
          },
        },
        labelLine: {
          show: false,
        },
        data: appAnswerResultCountList.value.map((item) => {
          return {
            value: item.resultCount,
            name: item.resultName,
          };
        }),
      },
    ],
  };
});
</script>

<style scoped>
.searchBar {
  padding-bottom: 20px;
  text-align: center;
}
</style>
