<template>
  <div :style="{ backgroundColor: color }" class="ball"></div>
</template>

<script>
export default {
  props: ['color']
}
</script>

<style>
.ball {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: inline-block;
  margin: 10px;
}
</style>
