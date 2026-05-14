<template>
  <Editor :value="props.value" :plugins="plugins" @change="handleChange" />
</template>

<script setup lang="ts">
import gfm from "@bytemd/plugin-gfm";
import highlight from "@bytemd/plugin-highlight";
import type { Editor } from "@bytemd/vue-next";
import "bytemd/dist/index.css"; 

// 给自己组件定义属性

/**
 * 定义组件能接受什么属性
 */
interface Props {
  value: string;
  mode?: string;
  handleChange: (v: string) => void;
}

/**
 * 创建实际的属性，并给组件指定初始值
 */
const props = withDefaults(defineProps<Props>(), {
  value: () => "",
  mode: () => "split",
  handleChange: (v: string) => {
    console.log(v);
  },
});

const plugins = [
  gfm(),
  highlight(),
];
// 当编辑器内容发生变化时，调用父组件传入的函数
const handleChange = (v: string) => {
    props.handleChange(v);
};
</script>

<style scoped>
.bytemd-toolbar-icon.bytemd-tippy.bytemd-tippy-right:last-child {
    display: none;
}
</style>
