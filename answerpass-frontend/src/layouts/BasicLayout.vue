<template>
  <div id="basicLayout">
    <a-layout style="height: 400px;">
      <a-layout-header>
        <GlobalHeader/>
      </a-layout-header>
      <a-layout-content>
        <router-view/>
      </a-layout-content>
      <a-layout-footer class="footer">
        <a href="#">程序员沈笙，链接待添加</a>
        </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from './GlobalHeader.vue';
</script>

<style scoped>
#basicLayout {
}
#basicLayout .footer {
    background-color: antiquewhite;
    padding: 16px;
    position:fixed;
    bottom: 0;
    left: 0;
    right: 0;
    text-align: center;
}
#basicLayout .content {
    background-color: linear-gradient(to right, #fefefe, #fff)  ;
    margin-bottom: 28px;
    padding: 20px;
}
</style>
