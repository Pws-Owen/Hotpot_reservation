<template>
  <div class="empty-container">
    <el-empty :description="description" :image-size="imageSize">
      <template v-if="showButton">
        <el-button type="primary" @click="handleClick">
          {{ buttonText }}
        </el-button>
      </template>
    </el-empty>
  </div>
</template>

<script setup>
defineProps({
  description: {
    type: String,
    default: '暂无数据'
  },
  imageSize: {
    type: Number,
    default: 120
  },
  showButton: {
    type: Boolean,
    default: false
  },
  buttonText: {
    type: String,
    default: '立即创建'
  }
})

const emit = defineEmits(['click'])

const handleClick = () => {
  emit('click')
}
</script>

<style scoped>
.empty-container {
  padding: 40px 20px;
}
</style>

