<template>
  <div class="about-page">
    <CustomerHeader 
      :show-search="false" 
      :show-nav="true"
      title="关于我们"
    />
    
    <!-- 内容区 -->
    <div class="about-content-wrapper">
      <!-- 头部区域：标题+返回按钮 -->
      <div class="about-header">
        <div class="header-left">
          <h1 class="about-title">关于我们</h1>
          <p class="about-subtitle">传承川味经典，品味地道火锅</p>
        </div>
        <div class="header-actions">
          <!-- 返回首页按钮 -->
          <el-button 
            type="primary" 
            @click="$router.push('/customer/home')"
            class="back-btn"
          >
            <el-icon><ArrowLeft /></el-icon>
            返回首页
          </el-button>
        </div>
      </div>
    
    <div class="about-hero">
      <div class="hero-content">
        <p class="hero-subtitle">传承川味经典，品味地道火锅</p>
      </div>
    </div>

    <div class="about-content">
      <el-row :gutter="30">
        <el-col :xs="24" :md="16">
          <el-card class="content-card">
            <div class="section">
              <h2 class="section-title">店铺简介</h2>
              <p class="section-text">
                本店深耕川味火锅10余年，坚持每日新鲜食材，传承正宗重庆老火锅工艺，
                为您带来地道的麻辣鲜香体验。我们致力于为每一位顾客提供最优质的用餐体验，
                让您在品尝美食的同时，感受家的温暖。
              </p>
            </div>

            <el-divider />

            <div class="section">
              <h2 class="section-title">服务特色</h2>
              <el-row :gutter="20">
                <el-col :xs="12" :sm="12" :md="6" v-for="(feature, index) in features" :key="index">
                  <div class="feature-item">
                    <div class="feature-icon">
                      <el-icon :size="40"><component :is="feature.icon" /></el-icon>
                    </div>
                    <h3 class="feature-title">{{ feature.title }}</h3>
                    <p class="feature-desc">{{ feature.desc }}</p>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="8">
          <el-card class="info-card">
            <template #header>
              <span class="card-header-title">店铺信息</span>
            </template>
            
            <div class="info-item">
              <el-icon class="info-icon"><Clock /></el-icon>
              <div class="info-content">
                <h4>营业时间</h4>
                <p>周一至周日：10:00 - 24:00</p>
                <p class="info-note">全年无休</p>
              </div>
            </div>

            <el-divider />

            <div class="info-item">
              <el-icon class="info-icon"><Phone /></el-icon>
              <div class="info-content">
                <h4>订餐电话</h4>
                <p class="info-phone">0769-88888888</p>
              </div>
            </div>

            <el-divider />

            <div class="info-item">
              <el-icon class="info-icon"><Location /></el-icon>
              <div class="info-content">
                <h4>店铺地址</h4>
                <p>广东省东莞市火锅城</p>
              </div>
            </div>

            <el-divider />

            <div class="info-item">
              <el-icon class="info-icon"><Message /></el-icon>
              <div class="info-content">
                <h4>联系方式</h4>
                <p>邮箱：contact@hotpot.com</p>
                <p>微信：hotpot_restaurant</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ArrowLeft, Clock, Phone, Location, Message, Star, ShoppingBag, Service, Trophy } from '@element-plus/icons-vue'
import CustomerHeader from '@/components/customer/CustomerHeader.vue'

const features = ref([
  {
    icon: Star,
    title: '新鲜食材',
    desc: '每日采购，保证品质'
  },
  {
    icon: ShoppingBag,
    title: '多种口味',
    desc: '满足不同需求'
  },
  {
    icon: Service,
    title: '优质服务',
    desc: '贴心周到'
  },
  {
    icon: Trophy,
    title: '环境优雅',
    desc: '适合聚餐'
  }
])
</script>

<style scoped lang="scss">
.about-page {
  width: 100%;
  min-height: 100vh;
  background: #f5f5f5;
}

.about-content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

// 头部样式
.about-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #ffe7e7;
  margin-bottom: 20px;

  .header-left {
    .about-title {
      font-size: 28px;
      font-weight: bold;
      color: #ff4d4f;
      margin: 0 0 5px 0;
    }

    .about-subtitle {
      font-size: 14px;
      color: #999;
      margin: 0;
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 15px;

    .back-btn {
      display: flex;
      align-items: center;
      gap: 5px;
      padding: 8px 16px;
    }
  }
}

.about-hero {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7d45 100%);
  padding: 60px 20px;
  text-align: center;
  color: #fff;

  .hero-title {
    font-size: 36px;
    font-weight: bold;
    margin: 0 0 10px 0;
  }

  .hero-subtitle {
    font-size: 18px;
    margin: 0;
    opacity: 0.9;
  }
}

.about-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 20px;
}

.content-card {
  margin-bottom: 20px;
}

.section {
  margin-bottom: 30px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #ff4d4f;
}

.section-text {
  font-size: 16px;
  line-height: 1.8;
  color: #606266;
  margin: 0;
}

.feature-item {
  text-align: center;
  padding: 20px 10px;
  transition: transform 0.3s;

  &:hover {
    transform: translateY(-5px);
  }
}

.feature-icon {
  color: #ff4d4f;
  margin-bottom: 15px;
}

.feature-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 8px 0;
}

.feature-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.info-card {
  position: sticky;
  top: 20px;
}

.card-header-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 15px 0;
}

.info-icon {
  font-size: 24px;
  color: #ff4d4f;
  margin-top: 5px;
  flex-shrink: 0;
}

.info-content {
  flex: 1;

  h4 {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
    margin: 0 0 8px 0;
  }

  p {
    font-size: 14px;
    color: #606266;
    margin: 4px 0;
    line-height: 1.6;
  }

  .info-note {
    font-size: 12px;
    color: #909399;
  }

  .info-phone {
    font-size: 18px;
    font-weight: bold;
    color: #ff4d4f;
  }
}

@media (max-width: 768px) {
  .about-hero {
    padding: 40px 20px;

    .hero-title {
      font-size: 28px;
    }

    .hero-subtitle {
      font-size: 16px;
    }
  }

  .about-content {
    padding: 20px 15px;
  }

  .info-card {
    position: static;
    margin-top: 20px;
  }
}
</style>

