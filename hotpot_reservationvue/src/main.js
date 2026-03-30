import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import router from './router'
import App from './App.vue'
import './style.css'
// 图片懒加载插件
import VueLazyloadNext from 'vue-lazyload-next'

const app = createApp(App)
const pinia = createPinia()

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 配置图片懒加载
app.use(VueLazyloadNext, {
  // 加载失败时显示的占位图
  error: '/image/placeholder.png',
  // 加载中显示的占位图
  loading: '/image/loading.gif',
  // 预加载高度（距离视口多少像素时开始加载）
  preLoad: 1.3,
  // 尝试加载次数
  attempt: 3
})

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
