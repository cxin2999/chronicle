<template>
  <div id="userRegisterPage">
    <div class="auth-wrapper">
      <div class="auth-container">
        <!-- 左侧品牌区域 -->
        <div class="brand-section">
          <div class="brand-bg"></div>
          <div class="brand-content">
            <div class="brand-logo">
              <img src="@/assets/logo.png" alt="Logo" class="logo-img" />
            </div>
            <h1 class="brand-title">Chronicle</h1>
            <p class="brand-subtitle">沉淀思考 量化行动</p>
          </div>
        </div>

        <!-- 右侧表单区域 -->
        <div class="form-section">
          <div class="form-card">
            <h2 class="form-title">创建账号</h2>
            <p class="form-subtitle">注册开启您成长记录</p>

            <van-form @submit="handleSubmit" class="register-form">
              <van-field
                v-model="formState.userAccount"
                name="userAccount"
                placeholder="请输入账号"
                :rules="[{ required: true, message: '请输入账号' }]"
                size="large"
                class="form-input"
              >
                <template #left-icon>
                  <van-icon name="user" class="input-icon" />
                </template>
              </van-field>
              <van-field
                v-model="formState.userPassword"
                type="password"
                name="userPassword"
                placeholder="请输入密码"
                :rules="[
                  { required: true, message: '请输入密码' },
                  { pattern: /^.{8,}$/, message: '密码不能小于 8 位' },
                ]"
                size="large"
                class="form-input"
              >
                <template #left-icon>
                  <van-icon name="lock" class="input-icon" />
                </template>
              </van-field>
              <van-field
                v-model="formState.checkPassword"
                type="password"
                name="checkPassword"
                placeholder="请确认密码"
                :rules="[
                  { required: true, message: '请确认密码' },
                  { pattern: /^.{8,}$/, message: '密码不能小于 8 位' },
                  { validator: validateCheckPassword },
                ]"
                size="large"
                class="form-input"
              >
                <template #left-icon>
                  <van-icon name="shield-o" class="input-icon" />
                </template>
              </van-field>

              <div style="margin: 16px 0">
                <van-button
                  round
                  block
                  type="primary"
                  native-type="submit"
                  size="large"
                  class="submit-btn"
                >
                  注册
                </van-button>
              </div>
            </van-form>

            <div class="form-footer">
              <span class="footer-text">已有账号？</span>
              <RouterLink to="/user/login" class="login-link">立即登录</RouterLink>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ICP 备案信息 -->
    <div class="icp-footer">
      <a href="https://beian.miit.gov.cn/" target="_blank" rel="noreferrer">粤ICP备2026037954号-2</a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController.ts'
import { showToast } from 'vant'
import { reactive } from 'vue'

const router = useRouter()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (value: string) => {
  if (value && value !== formState.userPassword) {
    return '两次输入密码不一致'
  }
  return true
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  const res = await userRegister(values)
  // 注册成功，跳转到登录页面
  if (res.data.code === 0) {
    showToast({ type: 'success', message: '注册成功' })
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    showToast({ type: 'fail', message: '注册失败，' + res.data.message })
  }
}
</script>

<style scoped>
#userRegisterPage {
  min-height: calc(100vh - 40px);
  background: var(--color-background-secondary);
  display: flex;
  flex-direction: column;
  padding: 40px 20px 0;
}

.auth-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-container {
  display: flex;
  width: 100%;
  max-width: 900px;
  min-height: 580px;
  background: white;
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
}

/* 左侧品牌区域 */
.brand-section {
  flex: 1;
  padding: 48px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.brand-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 50%, #15803d 100%);
}

.brand-bg::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 60%);
  animation: pulse-bg 8s ease-in-out infinite;
}

@keyframes pulse-bg {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.3;
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.brand-logo {
  margin-bottom: 24px;
}

.logo-img {
  width: 80px;
  height: 80px;
  object-fit: contain;
  background: rgba(255, 255, 255, 0.95);
  border-radius: var(--radius-xl);
  padding: 8px;
}

.brand-title {
  font-size: 26px;
  font-weight: 700;
  margin: 0 0 10px;
  letter-spacing: -0.5px;
}

.brand-subtitle {
  font-size: 15px;
  opacity: 0.9;
  margin: 0 0 36px;
}

.brand-features {
  text-align: left;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  backdrop-filter: blur(8px);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  font-size: 14px;
}

.feature-item:last-child {
  margin-bottom: 0;
}

.feature-check {
  font-size: 18px;
  color: white;
}

/* 右侧表单区域 */
.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  background: #f1e3e3;
}

.form-card {
  width: 100%;
  max-width: 320px;
}

.form-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text);
  margin: 0 0 6px;
  letter-spacing: -0.5px;
}

.form-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0 0 28px;
}

.register-form {
  margin-bottom: 24px;
}

.form-input {
  margin-bottom: 2px;
  border-radius: var(--radius-xl) !important;
  background: #fff !important;
  transition:
    box-shadow 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.form-input :deep(.van-field__body) {
  border: 1.5px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  background: rgba(120, 120, 128, 0.025);
  padding: 2px 0;
  transition:
    border-color 0.3s cubic-bezier(0.16, 1, 0.3, 1),
    box-shadow 0.3s cubic-bezier(0.16, 1, 0.3, 1),
    background 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.form-input:hover :deep(.van-field__body) {
  border-color: color-mix(in srgb, var(--color-primary) 25%, transparent);
  background: rgba(120, 120, 128, 0.04);
}

.form-input:focus,
.form-input:focus-within {
  box-shadow: none !important;
}

.form-input:focus-within :deep(.van-field__body) {
  border-color: color-mix(in srgb, var(--color-primary) 55%, transparent);
  background: #fff;
  box-shadow:
    0 0 0 4px color-mix(in srgb, var(--color-primary) 7%, transparent),
    0 1px 2px rgba(0, 0, 0, 0.04);
}

.form-input :deep(.van-field__control) {
  padding: 9px 15px;
  font-size: 0.9375rem;
  color: var(--color-text);
  letter-spacing: 0.01em;
}

.form-input :deep(.van-field__control::placeholder) {
  color: var(--color-text-muted);
  font-weight: 400;
}

.form-input :deep(.van-field__left-icon) {
  margin-left: 4px;
}

.input-icon {
  color: var(--color-text-muted);
  font-size: 17px;
  transition: color 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.form-input:focus-within :deep(.van-field__left-icon) .input-icon {
  color: var(--color-primary);
}

.submit-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-lg);
  background: var(--gradient-primary) !important;
  border: none !important;
  color: white !important;
  box-shadow: var(--shadow-green) !important;
  transition: opacity var(--transition-normal) !important;
}

.submit-btn:hover,
.submit-btn:focus,
.submit-btn:active {
  background: var(--gradient-primary) !important;
  border: none !important;
  color: white !important;
  box-shadow: var(--shadow-green) !important;
  opacity: 0.92;
}

.form-footer {
  text-align: center;
}

.footer-text {
  color: var(--color-text-secondary);
  font-size: 14px;
}

.login-link {
  color: var(--color-primary);
  font-weight: 600;
  margin-left: 4px;
  transition: color var(--transition-fast);
}

.login-link:hover {
  color: var(--color-primary-dark);
}

/* 响应式 */
@media (max-width: 768px) {
  .auth-container {
    flex-direction: column;
    min-height: auto;
    border-radius: var(--radius-xl);
  }

  .brand-section {
    padding: 32px 24px;
  }

  .brand-title {
    font-size: 22px;
  }

  .brand-features {
    display: none;
  }

  .form-section {
    padding: 32px 24px;
  }

  .form-title {
    font-size: 22px;
  }
}

/* ICP 备案 */
.icp-footer {
  flex-shrink: 0;
  text-align: center;
  padding: 12px 20px calc(12px + env(safe-area-inset-bottom, 0px));
}

.icp-footer a {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  text-decoration: none;
}
</style>
