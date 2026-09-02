<template>
  <div id="ShareModalPage">
     <a-modal v-model:visible="visible" @cancle="closeModal" :footer="false">
    <template #title>
      分享
    </template>
    <div>
      <h4 style="margin-top: 0;">复制链接</h4>
      <a-typography-paragraph copyable>
        {{ link }}
      </a-typography-paragraph>
      <h4>
        手机扫码查看
      </h4>
      <img :src="code"/>
    </div>
  </a-modal>
  </div>
</template>

<script setup lang="ts">
import QRCode from 'qrcode'
import { ref } from 'vue';

/**
 * 定义组件属性类型
 */
interface Props{
  title:string,
  link:string
}
/**
 * 给组件指定初始值
 */
const props=withDefaults(defineProps<Props>(),{
  title:()=>"分享",
  link:()=>"https://flower.nvsgames.cn/main/?from_source=fab"
})
// 是否可见
const visible=ref(false);
// 要展示的二维码
const code=ref();
// 打开弹窗
const openModal=()=>{
  visible.value=true;
}
// 关闭弹窗
const closeModal=()=>{
  visible.value=false;
}
// 二维码生成
QRCode.toDataURL(props.link)
  .then((url:string) => {
    code.value=url;
  })
  .catch(err => {
    console.error(err)
  });
  // 暴露函数给父组件
  defineExpose({
    openModal
  });
</script>

<style scoped>
#ShareModalPage {
}

</style>
