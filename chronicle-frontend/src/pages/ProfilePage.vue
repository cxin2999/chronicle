<template>
  <div class="profile-page mobile-page">
    <!-- 返回按钮 -->
    <div class="profile-page__nav">
      <van-button
        icon="arrow-left"
        size="small"
        plain
        :border="false"
        class="profile-page__back-btn"
        @click="router.push('/record')"
      />
    </div>

    <!-- 头部用户信息 -->
    <div class="profile-page__hero">
      <div class="profile-page__avatar-wrap">
        <van-image
          round
          fit="cover"
          :src="loginUser.userAvatar || defaultAvatar"
          width="4.5rem"
          height="4.5rem"
          class="profile-page__avatar"
        />
      </div>
      <div class="profile-page__name">
        {{ loginUser.userName || loginUser.userAccount || '用户' }}
      </div>
      <div v-if="loginUser.userProfile" class="profile-page__bio">
        {{ loginUser.userProfile }}
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="profile-page__menu-group">
      <van-cell-group inset>
        <van-cell title="修改资料" icon="edit" is-link @click="showToast('功能开发中')" />
        <van-cell title="设置" icon="setting-o" is-link @click="showToast('功能开发中')" />
        <van-cell title="关于" icon="info-o" is-link @click="showToast('Chronicle - 每日记录')" />
      </van-cell-group>
    </div>

    <!-- 退出登录按钮 -->
    <div class="profile-page__logout">
      <van-button block round plain type="danger" :loading="loggingOut" @click="handleLogout">
        退出登录
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { useLoginUserStore } from '@/stores/loginUser'
import { userLogout } from '@/api/userController'
import defaultAvatar from '@/assets/default_avatar.png'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const loginUser = computed(() => loginUserStore.loginUser)

const loggingOut = ref(false)

async function handleLogout() {
  try {
    await showConfirmDialog({ title: '确认退出', message: '确认要退出登录吗？' })
  } catch {
    return
  }
  loggingOut.value = true
  try {
    await userLogout()
  } finally {
    loggingOut.value = false
  }
  loginUserStore.setLoginUser({ userName: '未登录' })
  showToast({ type: 'success', message: '已退出登录' })
  router.replace('/user/login')
}
</script>

<style scoped>
.profile-page {
  background: var(--color-background-tertiary);
}

/* 返回导航栏：绿色背景 + 底部分隔阴影 */
.profile-page__nav {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  height: 2.75rem;
  padding: 0 0.5rem;
  background: linear-gradient(160deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.profile-page__back-btn {
  color: #fff !important;
  background: transparent !important;
}

/* 顶部用户信息卡：底部有阴影做分隔线 */
.profile-page__hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1.25rem 1rem 1.5rem;
  background: linear-gradient(160deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.profile-page__avatar-wrap {
  margin-bottom: 0.75rem;
  border: 3px solid rgba(255, 255, 255, 0.6);
  border-radius: 50%;
}

.profile-page__name {
  font-size: 1.125rem;
  font-weight: 600;
  color: #fff;
  margin-bottom: 0.25rem;
}

.profile-page__bio {
  font-size: 0.8125rem;
  color: rgba(255, 255, 255, 0.8);
  text-align: center;
  max-width: 80%;
}

/* 菜单组：flex:1 撑满剩余空间，把退出按钮挤到底部 */
.profile-page__menu-group {
  flex: 1;
  overflow-y: auto;
  margin-top: 0.875rem;
}

/* 退出按钮：固定在底部，安全区适配 */
.profile-page__logout {
  flex-shrink: 0;
  padding: 0.75rem 1rem calc(0.875rem + env(safe-area-inset-bottom, 0px));
  border-top: 1px solid var(--color-border-light);
  background: var(--color-background-tertiary);
}
</style>
