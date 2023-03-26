<template>
  <div v-bind:class="['ball', color + '-ball']"></div>
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
  background-color: white;
}

.red-ball {
  background-color: red;
}

.blue-ball {
  background-color: blue;
}

.green-ball {
  background-color: green;
}
</style>
