<template>
  <div id="basicLayout">
    <a-layout >
      <a-layout-header>
        <GlobalHeader/>
      </a-layout-header>
      <a-layout-content class="content">
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
  min-height: 100vh;
}
.footer {
    background-color: antiquewhite;
    padding: 16px;
    bottom: 0;
    left: 0;
    right: 0;
    text-align: center;
}
.content {
    background-color: linear-gradient(to right, #fefefe, #fff);
    max-width: 1200px;
    width: 100%;
    margin:0 auto;
    box-sizing: border-box;
    margin:0 auto 28px;
    padding: 20px;
}
</style>
