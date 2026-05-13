<template>
  <div id="xxPage">
    <h1>About</h1>
    <p>AnswerPass is a web application that allows users to answer questions using AI.</p>
    {{ JSON.stringify(loginUserStore.loginUser) }}
  </div>
</template>

<script setup lang="ts">
import { useLoginUserStore } from "../stores/userStore";

const loginUserStore=useLoginUserStore();
</script>

<style scoped>
#xxPage {
}

</style>
