<template>
  <a-space direction="vertical" :style="{ width: '100%' }">
    <a-upload
      :fileList="fileList"
      :show-file-list="false"
      :custom-request="customRequest"
    >
      <template #upload-button>
        <div
          :class="`arco-upload-list-item${
            file && file.status === 'error'
              ? ' arco-upload-list-item-error'
              : ''
          }`"
        >
          <div
            class="arco-upload-list-picture custom-upload-avatar"
            v-if="file && file.url"
          >
            <img :src="file.url" />
            <div class="arco-upload-list-picture-mask">
              <IconEdit />
            </div>
            <a-progress
              v-if="file.status === 'uploading' && file.percent < 100"
              :percent="file.percent"
              type="circle"
              size="mini"
              :style="{
                position: 'absolute',
                left: '50%',
                top: '50%',
                transform: 'translateX(-50%) translateY(-50%)',
              }"
            />
          </div>
          <div class="arco-upload-picture-card" v-else>
            <div class="arco-upload-picture-card-text">
              <IconPlus />
              <div style="margin-top: 10px; font-weight: 600">上传</div>
            </div>
          </div>
        </div>
      </template>
    </a-upload>
  </a-space>
</template>

<script setup lang="ts">
import { IconEdit, IconPlus } from "@arco-design/web-vue/es/icon";
import { ref,computed, watch } from "vue";
import { uploadFileUsingPost } from "@/api/fileController";
import { Message } from "@arco-design/web-vue";
import type { RequestOption } from "@arco-design/web-vue/es/upload";

/**
 * 定义组件属性类型
 * biz:业务标识，上传时必要参数
 * onChange:上传成功后的回调函数
 * value:外部传入的图片 URL，用于回显
 */
interface Props {
  biz: string;
  onChange?: (url: string) => void;
  value?: string;
}

/**
 * 给组件指定初始值
 */
const props = withDefaults(defineProps<Props>(), {
  value: () => "",
  onChange:undefined,
});
/**
 * 组件内部状态
 */
// 显示当前文件信息
const file = ref();
// 计算属性：给a-upload用的fileList
const fileList=computed(()=> file.value?[file.value]:[]);
/**
 * 根据外部value同步内部状态
 */
const syncFileFromValue=(url?:string)=>{
    if(url){
        file.value={
            url:url,
            percent:100,
            status:"done",
        };
    }else{
        file.value=undefined;
    }
}
// 初始化时同步
syncFileFromValue(props.value);
// 监听外部value变化
watch(()=>props.value,(newValue)=>{
    syncFileFromValue(newValue);
});
/**
 * 自定义上传请求
 */
const customRequest = (option: RequestOption) => {
  const { onError, onSuccess, fileItem } = option;

  const controller = new AbortController();

  uploadFileUsingPost({ biz: props.biz }, {}, fileItem.file, {
    signal: controller.signal,
  })
    .then((res) => {
      // 明确告诉 TypeScript，res.data 的结构是 API.BaseResponseString_
      const responseData = res.data as API.BaseResponseString_;
      
      if (responseData.code === 0 && responseData.data) {
        const url = responseData.data;
        
        // 更新内部状态
        file.value = {
          name: fileItem.name,
          file: fileItem.file,
          url,
          percent: 100,
          status: "done",
        };      
        // 通知父组件
        props.onChange?.(url);
        onSuccess();
      } else {
        Message.error("上传失败，" + (responseData.message || "未知错误"));
        onError(new Error(responseData.message || "上传失败"));
      }
    })
    .catch((error) => {
      Message.error("上传异常");
      onError(error);
    });

  return {
    abort: () => {
      controller.abort();
    },
  };
};


</script>
